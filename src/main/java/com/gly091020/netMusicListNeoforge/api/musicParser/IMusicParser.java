package com.gly091020.netMusicListNeoforge.api.musicParser;

import com.gly091020.netMusicListNeoforge.api.musicSource.MusicSource;

import java.net.URL;

/**
 * 歌曲解析器接口，用于在 音乐ID -> 可播放直链 这步进行处理
 */
public interface IMusicParser {
    /**
     * 获取歌曲解析器类型
     *
     * @return 类型字符串
     */
    String getType();

    /**
     * 获取解析器权重
     *
     * @return 权重，数字越大越靠前
     */
    default int getPriority() {
        return 0;
    }

    /**
     * 解析歌曲为可播放直链
     * <p>注意：不要在此方法中抛出错误，而是日志记录</p>
     *
     * @param musicSource 音乐源
     * @return 可播放直链，解析失败时返回 {@code null}
     */
    URL parse(MusicSource musicSource);
}
