package com.gly091020.netMusicListNeoforge.create.musicPlayerSource;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.item.components.MusicListComponent;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MusicIndexSource extends DisplaySource {
    @Nonnull
    private List<ItemMusicCD.SongInfo> getSongInfo(DisplayLinkContext context, ItemStack stack){
        var config = context.sourceConfig();
        if(!stack.is(NetMusicList.MUSIC_LIST_ITEM.get()))return Collections.emptyList();
        var component = stack.getOrDefault(NetMusicList.MUSIC_LIST_COMPONENT, MusicListComponent.getInstance());
        int input_index;
        try{
            input_index = Integer.parseInt(config.getString("Index"));
        }catch (NumberFormatException e) {
            input_index = 0;
        }
        var index = config.getInt("IndexMode") == 1 ? input_index :
                input_index + component.index();
        var songInfos = component.songInfos();
        if(index < 0 || index >= songInfos.size())return Collections.emptyList();

        var songInfoList = new ArrayList<ItemMusicCD.SongInfo>();
        int input_count;
        try{
            input_count = Integer.parseInt(config.getString("Count"));
        }catch (NumberFormatException e) {
            input_count = 1;
        }
        if(input_count <= 0)input_count = 1;
        // gly应该写不出这么优美的代码
        if (songInfos.size() <= input_count) {
            songInfoList.addAll(songInfos);
        } else {
            int leftCount = input_count / 2;
            int rightCount = input_count - leftCount - 1;

            int start = Math.max(0, index - leftCount);
            int end = Math.min(songInfos.size() - 1, index + rightCount);

            int actualCount = end - start + 1;
            if (actualCount < input_count) {
                if (start == 0) {
                    end = Math.min(songInfos.size() - 1, end + (input_count - actualCount));
                } else {
                    start = Math.max(0, start - (input_count - actualCount));
                }
            }
            for (int i = start; i <= end; i++) {
                songInfoList.add(songInfos.get(i));
            }
        }
        return songInfoList;
    }

    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        if(!(context.getSourceBlockEntity() instanceof TileEntityMusicPlayer musicPlayer))return Collections.emptyList();
        if(!musicPlayer.isPlay())return Collections.emptyList();
        var infos = getSongInfo(context, musicPlayer.getPlayerInv().getStackInSlot(0));
        var output = new ArrayList<MutableComponent>();
        var hasNumber = context.sourceConfig().getInt("HasNumber") == 0;
        for (int i = 0; i < infos.size(); i++) {
            if(hasNumber)
                output.add(Component.literal(Objects.toString(i + 1)).append(". ")
                        .append(MusicInfoSource.SongInfos.values()[context.sourceConfig().getInt("InfoMode")].getString(infos.get(i))));
            else
                output.add(MusicInfoSource.SongInfos.values()[context.sourceConfig().getInt("InfoMode")].getString(infos.get(i)));
        }
        return output;
    }

    @Override
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        if (isFirstLine) {
            builder.addIntegerTextInput(69, 68, (editBox, tooltipArea) ->
                    tooltipArea.withTooltip(List.of(Component.translatable("tooltips.net_music_list.count")
                            .withColor(AbstractSimiWidget.HINT_RGB.getRGB()))), "Count");
            builder.addIntegerTextInput(0, 68, (editBox, tooltipArea) ->
                    tooltipArea.withTooltip(List.of(Component.translatable("tooltips.net_music_list.index")
                            .withColor(AbstractSimiWidget.HINT_RGB.getRGB()))), "Index");
            return;
        }
        builder.addSelectionScrollInput(46, 45,
                (si, l) -> si.forOptions(CreateLang.translatedOptions("tooltips.net_music_list", "relative", "absolute"))
                        .titled(CreateLang.translateDirect("display_source.fill_level.display")),
                "IndexMode");
        builder.addSelectionScrollInput(0, 45,
                (si, l) -> si.forOptions(CreateLang.translatedOptions("tooltips.net_music_list", "number", "no_number"))
                        .titled(CreateLang.translateDirect("tooltips.net_music_list.is_number")),
                "HasNumber");
        builder.addSelectionScrollInput(92, 45, (selectionScrollInput, label) ->
                selectionScrollInput.forOptions(List.of(
                MusicInfoSource.SongInfos.SONG_NAME.getName(),
                MusicInfoSource.SongInfos.TRANS_NAME.getName(),
                MusicInfoSource.SongInfos.ARTISTS.getName(),
                MusicInfoSource.SongInfos.TIME.getName()
        )).titled(Component.translatable("tooltips.net_music_list.cd.song_info")), "InfoMode");
    }
}
