package com.gly091020.netMusicListNeoforge.media.bilibili;

import com.gly091020.netMusicListNeoforge.api.musicParser.IMusicParser;
import com.gly091020.netMusicListNeoforge.api.musicSource.MusicSource;

import java.net.URI;
import java.net.URL;

public class BilibiliParser implements IMusicParser {
    @Override
    public String getType() {
        return BiliBiliUtil.ID;
    }

    @Override
    public URL parse(MusicSource musicSource) {
        try {
            return URI.create(BiliBiliUtil.fetchAudioUrl(musicSource.identifier())).toURL();
        } catch (Exception e) {
            return null;
        }
    }
}
