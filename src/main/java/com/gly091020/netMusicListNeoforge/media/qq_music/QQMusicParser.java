package com.gly091020.netMusicListNeoforge.media.qq_music;

import com.gly091020.netMusicListNeoforge.api.musicParser.IMusicParser;
import com.gly091020.netMusicListNeoforge.api.musicSource.MusicSource;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class QQMusicParser implements IMusicParser {
    @Override
    public String getType() {
        return QQMusicUtil.ID;
    }

    @Override
    public URL parse(MusicSource musicSource) {
        long id;
        try{
            id = Long.parseLong(musicSource.identifier());
        } catch (NumberFormatException e) {
            return null;
        }
        var data = QQMusicUtil.getSongData(id);
        if(data == null || data.getMID() == null)return null;
        var url = QQMusicUtil.getMusicUrl(data.getMID());
        if(url == null)return null;
        try {
            return URI.create(url).toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
