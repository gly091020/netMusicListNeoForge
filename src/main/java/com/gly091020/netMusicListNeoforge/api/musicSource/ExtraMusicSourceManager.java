package com.gly091020.netMusicListNeoforge.api.musicSource;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 歌曲额外信息源管理器
 * <p>在此处注册歌曲额外信息源</p>
 */
public class ExtraMusicSourceManager {
    private static final List<IExtraMusicSource> EXTRA_MUSIC_SOURCES = new ArrayList<>();

    /**
     * 注册歌曲额外信息源
     *
     * @param extraMusicSource 歌曲额外信息源
     */
    public static void registry(IExtraMusicSource extraMusicSource) {
        EXTRA_MUSIC_SOURCES.add(extraMusicSource);
    }

    /**
     * 根据音乐源获取歌曲额外信息源
     *
     * @param musicSource 音乐源
     * @return 歌曲额外信息源，失败返回{@code null}
     */
    @Nullable
    public static IExtraMusicSource getParser(MusicSource musicSource) {
        if (musicSource.isEmpty()) return null;
        for (IExtraMusicSource ems : EXTRA_MUSIC_SOURCES) {
            if (Objects.equals(musicSource.loaderType(), ems.getType())) return ems;
        }
        return null;
    }

    /**
     * 获取所有歌曲额外信息源
     *
     * @return 歌曲额外信息源列表
     */
    public static List<IExtraMusicSource> getAllParser() {
        return List.copyOf(EXTRA_MUSIC_SOURCES);
    }
}
