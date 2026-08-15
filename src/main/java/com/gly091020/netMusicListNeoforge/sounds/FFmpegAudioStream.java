// 配置强制解码器：开启配置后，所有音乐都用 ffmpeg 进程转成 PCM s16le，
// 通过 stdout 管道喂给 Minecraft 声音引擎。不依赖任何第三方 ffmpeg 封装库。
package com.gly091020.netMusicListNeoforge.sounds;

import com.github.tartaricacid.netmusic.api.NetWorker;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.api.ffmpeg.FFmpegArgCustomizer;
import net.minecraft.client.sounds.AudioStream;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class FFmpegAudioStream implements AudioStream {
    public static final int SAMPLE_RATE = 44100;
    public static final int CHANNELS = 2;
    private static final int STDERR_LIMIT = 64 * 1024;
    private static final int PROBE_CACHE_MAX = 64;
    private static final Map<String, ProbeResult> PROBE_CACHE = new HashMap<>();
    private static final List<FFmpegArgCustomizer> ARG_CUSTOMIZERS = new CopyOnWriteArrayList<>();

    /** ffmpeg 所在目录；null 表示从系统 PATH 查找。 */
    private static volatile Path ffmpegDir;
    /** ffprobe 所在目录；预留，null 表示从系统 PATH 查找。 */
    private static volatile Path ffprobeDir;
    /** 手动覆盖的 HTTP 代理；null 表示使用原版 NetWorker 的代理配置。 */
    private static volatile String httpProxy;
    private static final AtomicBoolean BINARY_WARNED = new AtomicBoolean();

    private final AudioFormat format;
    private final Process process;
    private final InputStream in;
    private final ByteArrayOutputStream stderrBuffer = new ByteArrayOutputStream(STDERR_LIMIT);
    private volatile boolean closed;
    private volatile Throwable failure;

    public FFmpegAudioStream(URL url) throws IOException {
        ProbeResult probe = probe(url);
        int sampleRate = probe != null ? probe.sampleRate : SAMPLE_RATE;
        // 原版 NetMusicAudioStream 的行为：EnableStereo=true 反而强制单声道
        int channels = GeneralConfig.ENABLE_STEREO.get() ? 1 : 2;
        this.format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                sampleRate, 16, channels, channels * 2, sampleRate, false);
        try {
            this.process = new ProcessBuilder(buildCommand(url, sampleRate, channels)).start();
        } catch (IOException e) {
            if (isMissingBinary(e)) {
                warnMissingBinary();
            }
            throw new IOException("无法启动 ffmpeg 进程", e);
        }
        this.in = process.getInputStream();
        Thread stderrDrainer = new Thread(this::drainStderr, "ffmpeg-stderr");
        stderrDrainer.setDaemon(true);
        stderrDrainer.start();
        Thread watcher = new Thread(this::waitForExit, "ffmpeg-watcher");
        watcher.setDaemon(true);
        watcher.start();
        NetMusicList.LOGGER.info("使用 ffmpeg 解码：{}", url);
    }

    @Override
    public AudioFormat getFormat() {
        return format;
    }

    @Override
    public ByteBuffer read(int size) throws IOException {
        byte[] buffer = new byte[size];
        int total = 0;
        while (total < size) {
            int n;
            try {
                n = in.read(buffer, total, size - total);
            } catch (IOException e) {
                if (failure != null) {
                    throw new IOException("ffmpeg 解码失败", failure);
                }
                throw e;
            }
            if (n < 0) {
                break;
            }
            total += n;
        }
        if (total == 0) {
            if (failure != null) {
                throw new IOException("ffmpeg 解码失败", failure);
            }
            return null;
        }
        // alBufferData 要求直接缓冲区（原生内存），heap 缓冲区会导致 OpenAL 访问违规崩溃
        ByteBuffer direct = BufferUtils.createByteBuffer(total);
        direct.put(buffer, 0, total);
        direct.flip();
        return direct;
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        try {
            in.close();
        } catch (IOException ignored) {
        }
        if (process.isAlive()) {
            process.destroy();
            try {
                if (!process.waitFor(2, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
            }
        }
    }

    /** 指定 ffmpeg 可执行文件所在目录；null 表示从系统 PATH 查找。 */
    public static void setFfmpegDir(Path dir) {
        ffmpegDir = dir;
    }

    /** 读取配置文件中的 ffmpeg/ffprobe 目录并应用；留空则退回系统 PATH。 */
    public static void applyConfig() {
        String path = NetMusicList.CONFIG.ffmpegPath;
        ffmpegDir = (path == null || path.isBlank()) ? null : Path.of(path);
        String probePath = NetMusicList.CONFIG.ffprobePath;
        ffprobeDir = (probePath == null || probePath.isBlank()) ? null : Path.of(probePath);
    }

    /** 指定 ffprobe 可执行文件所在目录；预留，null 表示从系统 PATH 查找。 */
    public static void setFfprobeDir(Path dir) {
        ffprobeDir = dir;
    }

    /** 手动指定 ffmpeg 使用的 HTTP 代理；null 表示使用原版 NetWorker 的代理配置。 */
    public static void setHttpProxy(String proxy) {
        httpProxy = proxy;
    }

    /** 注册自定义 ffmpeg 输入参数的扩展点，可注册多个，按注册顺序生效。 */
    public static void addArgCustomizer(FFmpegArgCustomizer customizer) {
        if (customizer != null) {
            ARG_CUSTOMIZERS.add(customizer);
        }
    }

    private static void addCustomArgs(List<String> command, URL url) {
        for (FFmpegArgCustomizer customizer : ARG_CUSTOMIZERS) {
            List<String> args = customizer.customize(url);
            if (args != null) {
                command.addAll(args);
            }
        }
    }

    /** 解析 ffmpeg 使用的代理：优先手动指定，否则取原版 NetWorker 的代理配置。 */
    private static String resolveProxy() {
        if (httpProxy != null) {
            return httpProxy;
        }
        Proxy proxy = NetWorker.getProxyFromConfig();
        if (proxy == null || proxy.type() == Proxy.Type.DIRECT) {
            return null;
        }
        if (!(proxy.address() instanceof InetSocketAddress address)) {
            return null;
        }
        String scheme = proxy.type() == Proxy.Type.SOCKS ? "socks5" : "http";
        return scheme + "://" + address.getHostString() + ":" + address.getPort();
    }

    private List<String> buildCommand(URL url, int sampleRate, int channels) {
        List<String> command = new ArrayList<>();
        command.add(ffmpegDir == null ? "ffmpeg" : ffmpegDir.resolve("ffmpeg").toString());
        command.add("-hide_banner");
        command.add("-loglevel");
        command.add("error");
        if ("file".equalsIgnoreCase(url.getProtocol())) {
            // 本地缓存文件：ffmpeg 打不开 file:/C:/... 这种 URL，直接转成本地路径
            try {
                command.add("-i");
                command.add(Path.of(url.toURI()).toString());
            } catch (Exception e) {
                command.add("-i");
                command.add(url.toString());
            }
        } else {
            command.add("-reconnect");
            command.add("1");
            command.add("-reconnect_streamed");
            command.add("1");
            command.add("-reconnect_delay_max");
            command.add("5");
            String proxy = resolveProxy();
            if (proxy != null && !proxy.isBlank()) {
                command.add("-http_proxy");
                command.add(proxy);
            }
            addCustomArgs(command, url);
            command.add("-i");
            command.add(url.toString());
        }
        command.add("-f");
        command.add("s16le");
        command.add("-ac");
        command.add(String.valueOf(channels));
        command.add("-ar");
        command.add(String.valueOf(sampleRate));
        command.add("pipe:1");
        return command;
    }

    /** 探测音频流的采样率和声道数；失败返回 null（调用方回退默认值）。 */
    private static ProbeResult probe(URL url) {
        String key = url.toString();
        synchronized (PROBE_CACHE) {
            ProbeResult cached = PROBE_CACHE.get(key);
            if (cached != null) {
                return cached;
            }
        }
        ProbeResult result = doProbe(url);
        if (result != null) {
            synchronized (PROBE_CACHE) {
                if (PROBE_CACHE.size() >= PROBE_CACHE_MAX) {
                    PROBE_CACHE.clear();
                }
                PROBE_CACHE.put(key, result);
            }
        }
        return result;
    }

    private static ProbeResult doProbe(URL url) {
        List<String> command = new ArrayList<>();
        command.add(ffprobeDir == null ? "ffprobe" : ffprobeDir.resolve("ffprobe").toString());
        command.add("-v");
        command.add("error");
        command.add("-select_streams");
        command.add("a:0");
        command.add("-show_entries");
        command.add("stream=sample_rate,channels");
        command.add("-of");
        command.add("default=noprint_wrappers=1");
        addProbeInput(command, url.toString());
        try {
            Process process = new ProcessBuilder(command).start();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Thread reader = new Thread(() -> {
                try (InputStream input = process.getInputStream()) {
                    input.transferTo(output);
                } catch (IOException ignored) {
                }
            });
            reader.setDaemon(true);
            reader.start();
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return null;
            }
            if (process.exitValue() != 0) {
                return null;
            }
            int rate = 0;
            int channels = 0;
            for (String line : output.toString(StandardCharsets.UTF_8).split("\\R")) {
                if (line.startsWith("sample_rate=")) {
                    rate = Integer.parseInt(line.substring(12).trim());
                } else if (line.startsWith("channels=")) {
                    channels = Integer.parseInt(line.substring(9).trim());
                }
            }
            if (rate > 0 && channels > 0) {
                return new ProbeResult(rate, channels);
            }
        } catch (Exception ignored) {
            // 探测失败就回退默认值
        }
        return null;
    }

    /**
     * 批量探测音频时长并构造 {@link ItemMusicCD.SongInfo}，并行执行。
     * <p>
     * 探测不到时长的输入（文件不存在、解析失败等）会直接跳过。
     *
     * @param inputs 音频 URL（http/https/file）或本地路径
     * @return SongInfo 列表（songUrl 为原始输入，songName 为不带后缀的文件名，songTime 为探测到的秒数）
     */
    public static List<ItemMusicCD.SongInfo> probeSongInfos(Collection<String> inputs) {
        if(!NetMusicList.CONFIG.forceFFmpeg)return List.of();
        if (inputs == null || inputs.isEmpty()) {
            return List.of();
        }
        int threads = Math.clamp(inputs.size(), 1, 8);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<ItemMusicCD.SongInfo> result = new CopyOnWriteArrayList<>();
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (String input : inputs) {
                futures.add(pool.submit(() -> {
                    Double duration = probeDuration(input);
                    if (duration != null) {
                        int seconds = Math.max(1, (int) Math.ceil(duration));
                        String url = new File(input).toURI().toString();
                        result.add(new ItemMusicCD.SongInfo(
                                url, fileNameWithoutExtension(input), seconds, "", false, false, List.of()));
                    }
                }));
            }
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception ignored) {
                }
            }
        } finally {
            pool.shutdown();
        }
        return result;
    }

    /** 取文件名（去掉 URL/路径前缀与查询参数）并去掉扩展名。 */
    private static String fileNameWithoutExtension(String input) {
        String name = input;
        int lastSlash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (lastSlash >= 0 && lastSlash < name.length() - 1) {
            name = name.substring(lastSlash + 1);
        }
        int query = name.indexOf('?');
        if (query >= 0) {
            name = name.substring(0, query);
        }
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            name = name.substring(0, lastDot);
        }
        return name;
    }

    private static Double probeDuration(String input) {
        List<String> command = new ArrayList<>();
        command.add(ffprobeDir == null ? "ffprobe" : ffprobeDir.resolve("ffprobe").toString());
        command.add("-v");
        command.add("error");
        command.add("-show_entries");
        command.add("format=duration");
        command.add("-of");
        command.add("default=noprint_wrappers=1");
        addProbeInput(command, input);
        try {
            Process process = new ProcessBuilder(command).start();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Thread reader = new Thread(() -> {
                try (InputStream stream = process.getInputStream()) {
                    stream.transferTo(output);
                } catch (IOException ignored) {
                }
            });
            reader.setDaemon(true);
            reader.start();
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return null;
            }
            if (process.exitValue() != 0) {
                return null;
            }
            for (String line : output.toString(StandardCharsets.UTF_8).split("\\R")) {
                if (line.startsWith("duration=")) {
                    return Double.parseDouble(line.substring(9).trim());
                }
            }
        } catch (Exception ignored) {
            // 探测失败返回 null
        }
        return null;
    }

    /** 把 ffprobe 的输入参数加入命令：file URL 转路径，http(s) 加代理和自定义参数。 */
    private static void addProbeInput(List<String> command, String input) {
        try {
            URL url = new URL(input);
            if ("file".equalsIgnoreCase(url.getProtocol())) {
                command.add(Path.of(url.toURI()).toString());
                return;
            }
            if ("http".equalsIgnoreCase(url.getProtocol()) || "https".equalsIgnoreCase(url.getProtocol())) {
                String proxy = resolveProxy();
                if (proxy != null && !proxy.isBlank()) {
                    command.add("-http_proxy");
                    command.add(proxy);
                }
                addCustomArgs(command, url);
                command.add(input);
                return;
            }
        } catch (Exception ignored) {
            // 不是合法 URL，按普通路径处理
        }
        command.add(input);
    }

    private void drainStderr() {
        byte[] buffer = new byte[2048];
        try (InputStream err = process.getErrorStream()) {
            int n;
            while ((n = err.read(buffer)) > 0) {
                synchronized (stderrBuffer) {
                    if (stderrBuffer.size() < STDERR_LIMIT) {
                        stderrBuffer.write(buffer, 0, n);
                    }
                }
            }
        } catch (IOException ignored) {
            // 进程被强制结束时流关闭，属正常现象
        }
    }

    private void waitForExit() {
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        if (closed) {
            return;
        }
        int code = process.exitValue();
        if (code != 0) {
            String message;
            synchronized (stderrBuffer) {
                message = stderrBuffer.toString(StandardCharsets.UTF_8).trim();
            }
            if (isMissingBinaryMessage(message)) {
                warnMissingBinary();
                failure = new IOException("未找到 ffmpeg 或无法启动 ffmpeg 进程");
            } else {
                failure = new IOException("ffmpeg 退出码 " + code + (message.isEmpty() ? "" : "：" + message));
            }
            NetMusicList.LOGGER.error("ffmpeg 解码失败：{}", failure.getMessage());
        }
    }

    private static boolean isMissingBinary(IOException e) {
        String message = e.getMessage();
        return message != null && isMissingBinaryMessage(message);
    }

    private static boolean isMissingBinaryMessage(String message) {
        if (message == null) {
            return false;
        }
        String lower = message.toLowerCase();
        return lower.contains("error=2") || lower.contains("no such file")
                || lower.contains("cannot find") || lower.contains("createprocess");
    }

    private static void warnMissingBinary() {
        if (BINARY_WARNED.compareAndSet(false, true)) {
            NetMusicList.LOGGER.error("未找到 ffmpeg：请安装 ffmpeg 并加入系统 PATH，"
                    + "或在配置中设置 ffmpegPath 指定目录");
        }
    }

    private static final class ProbeResult {
        final int sampleRate;
        final int channels;

        ProbeResult(int sampleRate, int channels) {
            this.sampleRate = sampleRate;
            this.channels = channels;
        }
    }
}
