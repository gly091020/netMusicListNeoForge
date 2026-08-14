package com.gly091020.netMusicListNeoforge.util;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.api.musicParser.MusicParserManager;
import com.gly091020.netMusicListNeoforge.api.musicSource.ExtraMusicSourceManager;
import com.gly091020.netMusicListNeoforge.api.musicSource.MusicSource;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApiStatus.Experimental
public class CacheManager {
    /**
     * 缓存索引：键为 "loaderType:identifier"（网易云是 "netease:歌曲ID"），值为缓存文件的 UUID 前缀。
     * index.json 与 <uuid>.mp3/.png/.lyc.json 的文件格式保持不变，旧版纯数字键会自动迁移。
     */
    private static Map<String, String> musicCache = new HashMap<>();
    private static final Gson GSON = new Gson();
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);
    public static final String DIR_NAME = "netMusicListCache";
    public static final String INDEX_FILE_NAME = "index.json";
    public static Path PATH = FMLPaths.CONFIGDIR.get().resolve(DIR_NAME);
    public static final List<FileDownloadThread> threads = new CopyOnWriteArrayList<>();

    /** 将音乐源转换为缓存键：loaderType:identifier */
    public static String getKey(MusicSource source) {
        return source.loaderType() + ":" + source.identifier();
    }

    /**
     * 兼容旧调用：纯数字视为网易云 ID；netmusiclib:// URI 按新 API 解析；
     * 已经是 "loaderType:identifier" 的字符串则直接使用。
     */
    private static MusicSource fromLegacy(String resourceId) {
        if (resourceId == null || resourceId.isEmpty()) return MusicSource.EMPTY;
        if (resourceId.matches("\\d+")) return new MusicSource("netease", resourceId, 0);
        if (resourceId.startsWith("netmusiclib://")) {
            try {
                var ms = MusicSource.tryPasteFromURI(URI.create(resourceId));
                if (ms != null) return ms;
            } catch (IllegalArgumentException ignored) {
            }
        }
        int idx = resourceId.indexOf(':');
        if (idx > 0 && !resourceId.startsWith("http")) {
            return new MusicSource(resourceId.substring(0, idx), resourceId.substring(idx + 1), 0);
        }
        return new MusicSource("netease", resourceId, 0);
    }

    public static void firstTimeInit(){
        if(!NetMusicList.CONFIG.enableCache)return;
        try {
            if (PATH.toFile().isFile()) {
                Files.createDirectory(PATH);
            }
            Files.writeString(PATH.resolve(INDEX_FILE_NAME), "{}");
        } catch (IOException e) {
            NetMusicList.LOGGER.error("缓存无法创建：", e);
        }
    }

    @SuppressWarnings("all")
    public static void load(){
        if(!NetMusicList.CONFIG.enableCache)return;
        if(NetMusicList.CONFIG.globalCache){
            PATH = Paths.get(System.getProperty("user.home")).resolve(DIR_NAME);
        }
        try{
            musicCache = (Map<String, String>)GSON.fromJson(Files.readString(PATH.resolve(INDEX_FILE_NAME)),
                    TypeToken.get(Object.class));
            if(musicCache == null)musicCache = new HashMap<>();
            migrateLegacyKeys();
            checkCache();
        }catch (Exception e){
            NetMusicList.LOGGER.error("缓存读取失败：", e);
            firstTimeInit();
        }
    }

    /** 把旧版纯数字键迁移为 "netease:ID"，保证 index.json 结构与文件格式不变 */
    private static void migrateLegacyKeys() {
        Map<String, String> migrated = new HashMap<>();
        musicCache.forEach((k, v) -> migrated.put(getKey(fromLegacy(k)), v));
        if(!migrated.equals(musicCache)){
            musicCache = migrated;
            save();
        }
    }

    public static void reload(){
        if(!NetMusicList.CONFIG.enableCache)return;
        musicCache.clear();
        load();
    }

    public static void save(){
        if(!NetMusicList.CONFIG.enableCache)return;
        try{
            Files.writeString(PATH.resolve(INDEX_FILE_NAME), GSON.toJson(musicCache));
        } catch (Exception e) {
            NetMusicList.LOGGER.error("缓存写入失败：", e);
        }
    }

    public static int checkCache(boolean andClear){
        if(!NetMusicList.CONFIG.enableCache)return 0;
        var keys = new ArrayList<String>();
        musicCache.forEach((k, v) -> {
            var file = PATH.resolve(v + ".mp3");
            try {
                if(!file.toFile().isFile() || isHtmlOrErrorResponse(Files.readAllBytes(file)))keys.add(k);
            } catch (IOException e) {
                keys.add(k);
            }
        });
        keys.forEach(k -> {
            if(andClear)deleteCache(k);
            musicCache.remove(k);
        });
        if(!keys.isEmpty())save();
        return keys.size();
    }

    public static int checkCache(){
        return checkCache(false);
    }

    private static void startDownload(String downloadUrl, String key, String fileType, String uuid){
        if(!NetMusicList.CONFIG.enableCache)return;
        var thread = new FileDownloadThread(downloadUrl, key, fileType, uuid);
        EXECUTOR_SERVICE.submit(thread);
        threads.add(thread);
    }

    // ===== 新 API：以 MusicSource 为缓存对象 =====

    public static void startImgDownload(MusicSource source, String uuid){
        if(!NetMusicList.CONFIG.enableCache)return;
        EXECUTOR_SERVICE.submit(() -> {
            try {
                var extra = ExtraMusicSourceManager.getParser(source);
                if(extra == null){
                    NetMusicList.LOGGER.warn("没有找到音乐额外信息源：{}", source.loaderType());
                    return;
                }
                var icon = extra.parseIcon(source.identifier());
                if(icon == null)return;
                Files.createDirectories(PATH);
                icon.writeToFile(PATH.resolve(uuid + ".png"));
            } catch (Exception e) {
                NetMusicList.LOGGER.error("出现错误：", e);
            }
        });
    }

    public static void startSongDownload(MusicSource source, String uuid){
        if(!NetMusicList.CONFIG.enableCache)return;
        EXECUTOR_SERVICE.submit(() -> {
            try {
                var parser = MusicParserManager.getParser(source);
                if(parser == null){
                    // 网易云目前未注册 IMusicParser，回退到旧的直链逻辑，保证旧缓存流程不变
                    if("netease".equals(source.loaderType())){
                        startDownload(pasteUrl(Long.parseLong(source.identifier())), getKey(source), ".mp3", uuid);
                        return;
                    }
                    NetMusicList.LOGGER.warn("没有找到歌曲解析器：{}", source.loaderType());
                    return;
                }
                var url = parser.parse(source);
                if(url == null)return;
                startDownload(url.toString(), getKey(source), ".mp3", uuid);
            } catch (Exception e) {
                NetMusicList.LOGGER.error("出现错误：", e);
            }
        });
    }

    public static void startLycDownload(MusicSource source, String uuid){
        if(!NetMusicList.CONFIG.enableCache)return;
        EXECUTOR_SERVICE.submit(() -> {
            try {
                var extra = ExtraMusicSourceManager.getParser(source);
                if(extra == null){
                    NetMusicList.LOGGER.warn("没有找到音乐额外信息源：{}", source.loaderType());
                    return;
                }
                var record = extra.parseLyric(source.identifier());
                if(record == null)return;
                var lyric = NetMusicListUtil.Lyric.fromLyricRecord(record);
                Files.createDirectories(PATH);
                Files.writeString(PATH.resolve(uuid + ".lyc.json"), lyric.toJson(),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            } catch (Exception e) {
                NetMusicList.LOGGER.error("出现错误：", e);
            }
        });
    }

    // ===== 旧兼容入口：String/long 统一委托给 MusicSource =====

    public static void startImgDownload(String resourceId, String uuid){
        startImgDownload(fromLegacy(resourceId), uuid);
    }

    public static void startImgDownload(long resourceId, String uuid){
        startImgDownload(fromLegacy(String.valueOf(resourceId)), uuid);
    }

    public static void startSongDownload(String resourceId, String uuid){
        startSongDownload(fromLegacy(resourceId), uuid);
    }

    public static void startSongDownload(long resourceId, String uuid){
        startSongDownload(fromLegacy(String.valueOf(resourceId)), uuid);
    }

    public static void startLycDownload(String resourceId, String uuid){
        startLycDownload(fromLegacy(resourceId), uuid);
    }

    public static void startLycDownload(long resourceId, String uuid){
        startLycDownload(fromLegacy(String.valueOf(resourceId)), uuid);
    }

    private static void addCache(String key, String uuid){
        if(!NetMusicList.CONFIG.enableCache)return;
        musicCache.put(key, uuid);
        save();
    }

    public static boolean hasCache(String resourceId){
        if(!NetMusicList.CONFIG.enableCache)return false;
        return musicCache.containsKey(getKey(fromLegacy(resourceId)));
    }

    public static boolean hasCache(long resourceId){
        return hasCache(String.valueOf(resourceId));
    }

    public static boolean hasCache(MusicSource source){
        if(!NetMusicList.CONFIG.enableCache)return false;
        return musicCache.containsKey(getKey(source));
    }

    public static String getCacheUUID(long resourceID){
        return musicCache.get(getKey(fromLegacy(String.valueOf(resourceID))));
    }

    public static String getCacheUUID(MusicSource source){
        return musicCache.get(getKey(source));
    }

    public static Path getImageCache(String resourceId){
        return getImageCache(fromLegacy(resourceId));
    }

    public static Path getImageCache(long resourceId){
        return getImageCache(String.valueOf(resourceId));
    }

    public static Path getImageCache(MusicSource source){
        if(!NetMusicList.CONFIG.enableCache)return null;
        if(!hasCache(source))return null;
        var path = PATH.resolve(musicCache.get(getKey(source)) + ".png");
        if(path.toFile().isFile()){
            return path;
        }
        return null;
    }

    public static String getSongCache(String resourceId){
        return getSongCache(fromLegacy(resourceId));
    }

    public static String getSongCache(long resourceId){
        return getSongCache(String.valueOf(resourceId));
    }

    public static String getSongCache(MusicSource source){
        if(!NetMusicList.CONFIG.enableCache)return null;
        if(!hasCache(source))return null;
        var path = PATH.resolve(musicCache.get(getKey(source)) + ".mp3");
        if(path.toFile().isFile()){
            return path.toFile().toURI().toString();
        }
        return null;
    }

    public static NetMusicListUtil.Lyric getLycCache(String resourceId){
        return getLycCache(fromLegacy(resourceId));
    }

    public static NetMusicListUtil.Lyric getLycCache(long resourceId){
        return getLycCache(String.valueOf(resourceId));
    }

    public static NetMusicListUtil.Lyric getLycCache(MusicSource source){
        if(!NetMusicList.CONFIG.enableCache)return null;
        if(!hasCache(source))return null;
        var path = PATH.resolve(musicCache.get(getKey(source)) + ".lyc.json");
        if(path.toFile().isFile()){
            try {
                return NetMusicListUtil.Lyric.fromJson(Files.readString(path));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static void tick() {
        if(!NetMusicList.CONFIG.enableCache)return;
        List<FileDownloadThread> toRemove = new ArrayList<>();
        for (FileDownloadThread t : threads) {
            if (t.isCompleted()) {
                if(Objects.equals(t.getFileType(), ".mp3")) {
                    addCache(t.getResourceId(), t.getThreadId());
                }
                toRemove.add(t);
            } else if (t.isFailed()) {
                NetMusicList.LOGGER.error("缓存出现错误：{}", t.getErrorMessage());
                toRemove.add(t);
            }
        }
        threads.removeAll(toRemove);
    }

    public static float getDownloadProgress(String resourceId){
        return getDownloadProgress(fromLegacy(resourceId));
    }

    public static float getDownloadProgress(long resourceId){
        return getDownloadProgress(String.valueOf(resourceId));
    }

    public static float getDownloadProgress(MusicSource source){
        if(!NetMusicList.CONFIG.enableCache)return 0;
        var key = getKey(source);
        for(FileDownloadThread thread: threads){
            if(Objects.equals(thread.getResourceId(), key) && Objects.equals(thread.getFileType(), ".mp3")){
                return thread.getProgress();
            }
        }
        return 0f;
    }

    public static List<FileDownloadThread> getThreads(){
        return new ArrayList<>(threads);
    }

    public static void deleteCache(String resourceId){
        deleteCache(fromLegacy(resourceId));
    }

    public static void deleteCache(MusicSource source){
        if(!NetMusicList.CONFIG.enableCache)return;
        var key = getKey(source);
        if(!musicCache.containsKey(key))return;
        List<Path> paths = new ArrayList<>();
        paths.add(PATH.resolve(musicCache.get(key) + ".lyc.json"));
        paths.add(PATH.resolve(musicCache.get(key) + ".png"));
        paths.add(PATH.resolve(musicCache.get(key) + ".mp3"));
        paths.forEach(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {}
        });
        musicCache.remove(key);
        save();
    }

    @SuppressWarnings("all")
    public static String pasteUrl(long resourceId){
        if(NetMusicListUtil.hasLoginNeed()){
            var r = LoginNeedUtil.getUrl("?id=" + resourceId);
            if(r != null)return r;
        }
        try {
            return NetMusicListUtil.resolveRedirect(new URL(String.format("https://music.163.com/song/media/outer/url?id=%s.mp3", resourceId)), 3, Map.of()).toString();
        } catch (IOException e) {
            return String.format("https://music.163.com/song/media/outer/url?id=%s.mp3", resourceId);
        }
    }

    private static final String[] HTML_STARTS = {
            "<!DOCTYPE html",
            "<html",
            "<?xml",
            "{",
            "[",
            "HTTP/",
            "Error",
            "404",
            "500"
    };
    /**
     * 快速检查文件是否是HTML或错误响应
     */
    public static boolean isHtmlOrErrorResponse(byte[] data) {
        if (data == null || data.length < 10) {
            return false;
        }

        // 将前1KB转换为字符串进行检查
        int checkLength = Math.min(data.length, 1024);
        String fileStart = new String(data, 0, checkLength, StandardCharsets.UTF_8).trim();

        // 检查常见的HTML/错误响应开头
        for (String htmlStart : HTML_STARTS) {
            if (fileStart.startsWith(htmlStart)) {
                return true;
            }
        }

        // 检查是否包含明显的HTML标签
        if (fileStart.contains("<head>") ||
                fileStart.contains("<body>") ||
                fileStart.contains("<title>") ||
                fileStart.contains("</html>") ||
                fileStart.contains("DOCTYPE") ||
                fileStart.contains("html>")) {
            return true;
        }

        // 检查JSON响应（常见于API错误）
        return (fileStart.startsWith("{") && fileStart.contains("\"error\"")) ||
                (fileStart.startsWith("[") && fileStart.contains("\"error\""));
    }
}
