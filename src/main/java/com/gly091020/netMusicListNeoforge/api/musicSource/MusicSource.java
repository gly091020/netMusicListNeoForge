package com.gly091020.netMusicListNeoforge.api.musicSource;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 音乐源
 *
 * @param loaderType 加载器类型
 * @param identifier 歌曲 ID
 * @param duration   音乐时长
 */
public record MusicSource(String loaderType, String identifier, long duration) {
    public static final MusicSource EMPTY = new MusicSource("", "", 0);
    public static final Codec<MusicSource> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.STRING.fieldOf("loaderType").forGetter(MusicSource::loaderType),
                    Codec.STRING.fieldOf("identifier").forGetter(MusicSource::identifier),
                    Codec.LONG.fieldOf("duration").forGetter(MusicSource::duration)
            ).apply(i, MusicSource::new)
    );
    public static final StreamCodec<ByteBuf, MusicSource> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public boolean isEmpty() {
        return this == EMPTY || (loaderType.isEmpty() && identifier.isEmpty());
    }

    /**
     * 转换为 URI
     * <p>协议格式：{@code netmusiclib://source/<loaderType>?id=<identifier>&duration=<duration>}
     * <p>loaderType 放在 path 而不是 host，是因为 Java 的 {@link URI#getHost()} 不识别下划线，
     * 会导致 {@code qq_music} 这类源类型解析出 null host。
     *
     * @return 字符串，用于{@code ItemMusicCD.SongInfo}
     */
    public @NotNull String toURI() {
        try {
            String query = String.format(
                    "id=%s&duration=%d",
                    URLEncoder.encode(identifier, StandardCharsets.UTF_8),
                    duration
            );

            return new URI(
                    "netmusiclib",
                    "source",
                    "/" + loaderType,
                    query,
                    null
            ).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从{@code ItemMusicCD.SongInfo}解析
     *
     * @return 音乐源，解析失败报错
     */
    @NotNull
    public static MusicSource pasteFromSongInfo(ItemMusicCD.SongInfo songInfo) throws IllegalArgumentException {
        URI uri = URI.create(songInfo.songUrl);
        return pasteFromSongUrl(uri);
    }

    /**
     * 从 uri 解析
     *
     * @return 音乐源，解析失败报错
     */
    @NotNull
    public static MusicSource pasteFromSongUrl(URI uri) {
        if (!"netmusiclib".equals(uri.getScheme())) {
            throw new IllegalArgumentException("Invalid scheme: " + uri.getScheme());
        }

        // 新格式：loaderType 在 path（/loaderType）；旧格式仍可从 host 读取
        String loader = null;
        if (uri.getPath() != null && !uri.getPath().isEmpty()) {
            String p = uri.getPath();
            loader = p.startsWith("/") ? p.substring(1) : p;
        }
        if (loader == null || loader.isEmpty()) {
            loader = uri.getHost();
        }
        if (loader == null || loader.isEmpty()) {
            throw new IllegalArgumentException("Missing loader");
        }

        String query = uri.getRawQuery();
        if (query == null || query.isEmpty()) {
            throw new IllegalArgumentException("Missing query");
        }

        Map<String, String> params = parseQuery(query);

        String id = params.get("id");
        String durationStr = params.get("duration");

        if (id == null || durationStr == null) {
            throw new IllegalArgumentException("Missing required params (id/duration)");
        }

        long duration;
        try {
            duration = Long.parseLong(durationStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid duration: " + durationStr);
        }

        return new MusicSource(loader, id, duration);
    }

    /**
     * 从{@code ItemMusicCD.SongInfo}解析
     *
     * @return 音乐源，解析失败为{@code null}
     */
    @Nullable
    public static MusicSource tryPasteFromSongInfo(ItemMusicCD.SongInfo songInfo) {
        try {
            return pasteFromSongInfo(songInfo);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 从 uri 解析
     *
     * @return 音乐源，解析失败为{@code null}
     */
    @Nullable
    public static MusicSource tryPasteFromURI(URI uri) {
        try {
            return pasteFromSongUrl(uri);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();

        for (String pair : query.split("&")) {
            if (pair.isEmpty()) continue;

            String[] kv = pair.split("=", 2);
            String key = decode(kv[0]);
            String value = kv.length > 1 ? decode(kv[1]) : "";

            map.put(key, value);
        }

        return map;
    }

    private static String decode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}
