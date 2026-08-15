package com.gly091020.netMusicListNeoforge.media.bilibili;

import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.api.musicSource.IExtraMusicSource;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import com.mojang.blaze3d.platform.NativeImage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.List;

public class BilibiliExtraSource implements IExtraMusicSource {
    @Override
    public @NotNull String getType() {
        return BiliBiliUtil.ID;
    }

    @Override
    public @Nullable ItemMusicCD.SongInfo parseMusic(String identifier, boolean readOnly) {
        return BiliBiliUtil.getMusicMediaResultFromBV(identifier);
    }

    @Override
    public @Nullable NativeImage parseIcon(String identifier) {
        try {
            String url = BiliBiliUtil.getIcon(identifier);
            if(url == null)return null;
            return NetMusicListUtil.getNativeImageFromURL(URI.create(url).toURL());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public @Nullable LyricRecord parseLyric(String identifier) {
        try {
            return BiliBiliUtil.fetchLyrics(identifier, NetMusicList.CONFIG.bilibiliCookie);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public @NotNull List<ItemMusicCD.SongInfo> parsePlaylist(String identifier) {
        long id;
        try{
            id = Long.parseLong(identifier);
        } catch (NumberFormatException e) {
            return List.of();
        }
        try {
            var bvids = BiliBiliUtil.fetchFavBvids(id);
            return bvids.stream().map(BiliBiliUtil::getMusicMediaResultFromBV).toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public @Nullable String autoParseFromClipboard(String clipboardText) {
        return BiliBiliUtil.getBvidFromUrl(clipboardText);
    }

    @Override
    public @Nullable String autoParseListFromClipboard(String clipboardText) {
        return BiliBiliUtil.getFidFromUrl(clipboardText);
    }
}
