package com.gly091020.netMusicListNeoforge.create.musicPlayerSource;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LyricSource extends DisplaySource {
    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        if(!NetMusicList.CONFIG.allowLyricToServer)return Collections.emptyList();
        if(!(context.getSourceBlockEntity() instanceof TileEntityMusicPlayer musicPlayer))return Collections.emptyList();
        if(!musicPlayer.isPlay())return Collections.emptyList();
        var info = ItemMusicCD.getSongInfo(musicPlayer.getPlayerInv().getStackInSlot(0));
        if(info == null)return Collections.emptyList();
        var lyric = musicPlayer.lyricRecord;
        if(lyric == null)return Collections.emptyList();
        lyric.updateCurrentLine(info.songTime * 20 - musicPlayer.getCurrentTime());
        var list = new ArrayList<MutableComponent>();

        String t1 = null, t2 = null;
        if(!lyric.getLyrics().isEmpty())
            t1 = lyric.getLyrics().get(lyric.getLyrics().firstIntKey());
        if(lyric.getTransLyrics() != null && !lyric.getTransLyrics().isEmpty())
            t2 = lyric.getTransLyrics().get(lyric.getTransLyrics().firstIntKey());

        if(t1 != null)list.add(Component.literal(t1));
        if(t2 != null)list.add(Component.literal(t2));
        return list;
    }
}
