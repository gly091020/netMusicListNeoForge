package com.gly091020.netMusicListNeoforge.media.qq_music;

import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.api.musicSource.IExtraMusicSource;
import com.gly091020.netMusicListNeoforge.api.musicSource.MusicSource;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import com.mojang.blaze3d.platform.NativeImage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.List;

public class QQMusicExtraSource implements IExtraMusicSource {
    @Override
    public @NotNull String getType() {
        return QQMusicUtil.ID;
    }

    @Override
    public @Nullable ItemMusicCD.SongInfo parseMusic(String identifier, boolean readOnly) {
        long id;
        try{
            id = Long.parseLong(identifier);
        } catch (NumberFormatException e) {
            return null;
        }
        var data = QQMusicUtil.getSongData(id);
        if(data == null || data.getSecond() == null)return null;
        var source = new MusicSource(QQMusicUtil.ID, identifier, data.getSecond());
        return new ItemMusicCD.SongInfo(
                source.toURI(),
                data.getName(),
                Math.toIntExact(data.getSecond()),
                data.getSubtitle(),
                false,
                readOnly,
                data.getSingers()
        );
    }

    @Override
    public @Nullable NativeImage parseIcon(String identifier) {
        long id;
        try{
            id = Long.parseLong(identifier);
        } catch (NumberFormatException e) {
            return null;
        }
        var data = QQMusicUtil.getSongData(id);
        if(data == null || data.getPicUrl() == null)return null;
        try {
            return NetMusicListUtil.getNativeImageFromURL(URI.create(data.getPicUrl()).toURL());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public @Nullable LyricRecord parseLyric(String identifier) {
        long id;
        try{
            id = Long.parseLong(identifier);
        } catch (NumberFormatException e) {
            return null;
        }
        var data = QQMusicUtil.getSongData(id);
        if(data == null)return null;
        return data.getLyric();
    }

    @Override
    public @NotNull List<ItemMusicCD.SongInfo> parsePlaylist(String identifier) {
        long id;
        try{
            id = Long.parseLong(identifier);
        } catch (NumberFormatException e) {
            return List.of();
        }
        var data = QQMusicUtil.getPlaylistData(id);
        if(data == null || data.getSongs() == null)return List.of();
        return data.getSongs().stream().map(
                info -> {
                    var source = new MusicSource(QQMusicUtil.ID, String.valueOf(info.id), info.length);
                    return new ItemMusicCD.SongInfo(
                            source.toURI(),
                            info.name,
                            Math.toIntExact(info.length),
                            info.subtitle,
                            false,
                            false,
                            info.singers.stream().map(singer -> singer.name).toList()
                    );
                }
        ).toList();
    }

    @Override
    public boolean searchable() {
        return true;
    }

    @Override
    public @NotNull List<ItemMusicCD.SongInfo> search(String keywords) {
        return QQMusicUtil.searchMusicResult(keywords);
    }
}
