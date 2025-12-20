package com.gly091020.netMusicListNeoforge.create.musicPlayerSource;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.PercentOrProgressBarDisplaySource;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;
import org.jetbrains.annotations.Nullable;

public class PlayerProgressSource extends PercentOrProgressBarDisplaySource {
    @Override
    protected @Nullable Float getProgress(DisplayLinkContext context) {
        if(!(context.getSourceBlockEntity() instanceof TileEntityMusicPlayer musicPlayer))return null;
        if(!musicPlayer.isPlay())return 1f;
        var info = ItemMusicCD.getSongInfo(musicPlayer.getPlayerInv().getStackInSlot(0));
        if(info == null)return null;
        return 1 - Math.clamp(((float) musicPlayer.getCurrentTime() / (info.songTime * 20)), 0f, 1f);
    }

    @Override
    protected boolean progressBarActive(DisplayLinkContext context) {
        return context.sourceConfig()
                .getInt("Mode") != 0;
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
        builder.addSelectionScrollInput(0, 137,
                (si, l) -> si.forOptions(CreateLang.translatedOptions("display_source.fill_level", "percent", "progress_bar"))
                        .titled(CreateLang.translateDirect("display_source.fill_level.display")),
                "Mode");
    }
}
