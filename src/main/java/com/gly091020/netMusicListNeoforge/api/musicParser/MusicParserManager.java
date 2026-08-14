package com.gly091020.netMusicListNeoforge.api.musicParser;

import com.gly091020.netMusicListNeoforge.api.musicSource.MusicSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 自定义音乐解析器管理器
 */
public class MusicParserManager {
    private static final List<IMusicParser> MUSIC_PARSERS = new ArrayList<>();

    /**
     * 注册新的解析器
     *
     * @param parser 音乐解析器
     */
    public static void registry(IMusicParser parser) {
        MUSIC_PARSERS.add(parser);
        MUSIC_PARSERS.sort(Comparator.comparingInt(IMusicParser::getPriority));
    }

    /**
     * 根据音乐源选择解析器
     *
     * @param musicSource 音乐源
     * @return 解析器，不存在返回 {@code null}
     */
    @Nullable
    public static IMusicParser getParser(MusicSource musicSource) {
        if (musicSource.isEmpty()) return null;
        for (IMusicParser mp : MUSIC_PARSERS) {
            if (Objects.equals(musicSource.loaderType(), mp.getType())) return mp;
        }
        return null;
    }
}
