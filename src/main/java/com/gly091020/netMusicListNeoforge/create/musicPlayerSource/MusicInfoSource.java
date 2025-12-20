package com.gly091020.netMusicListNeoforge.create.musicPlayerSource;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class MusicInfoSource extends SingleLineDisplaySource {
    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if(!(context.getSourceBlockEntity() instanceof TileEntityMusicPlayer musicPlayer))return Component.empty();
        if(!musicPlayer.isPlay())return Component.empty();
        var info = ItemMusicCD.getSongInfo(musicPlayer.getPlayerInv().getStackInSlot(0));
        if(info == null)return Component.empty();
        return SongInfos.values()[context.sourceConfig().getInt("Mode")].getString(info);
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }

    @Override
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        super.initConfigurationWidgets(context, builder, isFirstLine);
        if (isFirstLine)
            return;
        builder.addSelectionScrollInput(0, 137, (selectionScrollInput, label) -> {
            selectionScrollInput.forOptions(List.of(
                    SongInfos.SONG_NAME.getName(),
                    SongInfos.TRANS_NAME.getName(),
                    SongInfos.ARTISTS.getName(),
                    SongInfos.TIME.getName()
            )).titled(Component.translatable("tooltips.net_music_list.cd.song_info"));
        }, "Mode");
    }

    public enum SongInfos{
        SONG_NAME("tooltips.net_music_list.cd.song_name"),
        TRANS_NAME("tooltips.net_music_list.cd.trans_name"),
        ARTISTS("tooltips.netmusic.cd.artists"),
        TIME("tooltips.netmusic.cd.time");

        private final String id;
        SongInfos(String id){
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public Component getName(){
            return Component.translatable(id);
        }

        public MutableComponent getString(ItemMusicCD.SongInfo songInfo){
            switch (this){
                case SONG_NAME -> {
                    return Component.literal(songInfo.songName);
                }
                case TRANS_NAME -> {
                    return songInfo.transName == null ? Component.translatable("tooltips.net_music_list.cd.null") : Component.literal(songInfo.transName);
                }
                case ARTISTS -> {
                    if(songInfo.artists == null || songInfo.artists.isEmpty())
                        return Component.translatable("tooltips.net_music_list.cd.null");
                    return Component.literal(String.join(", ", songInfo.artists));
                }
                case TIME -> {
                    return Component.literal(NetMusicListUtil.getSongTime(songInfo.songTime));
                }
            }
            return Component.empty();
        }
    }
}
