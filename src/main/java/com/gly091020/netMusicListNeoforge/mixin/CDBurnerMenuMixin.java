package com.gly091020.netMusicListNeoforge.mixin;

import com.github.tartaricacid.netmusic.client.gui.CDBurnerMenuScreen;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.client.MusicSearchScreen;
import com.gly091020.netMusicListNeoforge.media.NetMusicListMedias;
import com.gly091020.netMusicListNeoforge.api.musicSource.IExtraMusicSource;
import com.gly091020.netMusicListNeoforge.mixin.accessor.AbstractContainerScreenAccessor;
import com.gly091020.netMusicListNeoforge.mixin.accessor.ScreenAccessor;
import com.gly091020.netMusicListNeoforge.util.MSMixinFunctions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在 NetMusic 刻录界面加入音乐源选择按钮；非网易云源由本模组接管刻录逻辑。
 */
@Mixin(value = CDBurnerMenuScreen.class, remap = false)
public class CDBurnerMenuMixin {
    @Shadow
    private Component tips;
    @Shadow
    private EditBox textField;
    @Shadow
    private Checkbox readOnlyButton;

    @Unique
    private CycleButton<IExtraMusicSource> netMusicList$cycleButton;
    @Unique
    @Nullable
    private String netMusicList$suggestion;
    @Unique
    private Button netMusicList$searchButton;

    @Unique
    private static IExtraMusicSource netMusicList$lastSource;

    @Inject(method = "handleCraftButton", at = @At("HEAD"), cancellable = true)
    public void onCraft(CallbackInfo ci) {
        if (textField.getValue().isEmpty() && netMusicList$suggestion != null)
            textField.setValue(netMusicList$suggestion);
        if (netMusicList$cycleButton.getValue().equals(NetMusicListMedias.NETEASE_EXTRA_MUSIC_SOURCE)) return;
        ci.cancel();
        var source = netMusicList$cycleButton.getValue();
        if (MSMixinFunctions.isPlaylistInput(textField.getValue())) {
            tips = MSMixinFunctions.tryCraftPlaylist(source, MSMixinFunctions.getPlaylistId(textField.getValue()), readOnlyButton.selected());
            return;
        }
        var self = (CDBurnerMenuScreen) (Object) this;
        ItemStack cd = self.getMenu().getInput().getStackInSlot(0);
        if (cd.isEmpty()) {
            tips = Component.translatable("gui.netmusic.cd_burner.cd_is_empty");
            return;
        }
        ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(cd);
        if (songInfo != null && songInfo.readOnly) {
            tips = Component.translatable("gui.netmusic.cd_burner.cd_read_only");
            return;
        }
        if (StringUtils.isBlank(textField.getValue())) {
            tips = Component.translatable("gui.netmusic.cd_burner.no_music_id");
            return;
        }
        tips = MSMixinFunctions.tryCraftMusicCD(source, textField.getValue(), readOnlyButton.selected());
    }

    @Inject(method = "init", at = @At("TAIL"), remap = true)
    public void addButton(CallbackInfo ci) {
        netMusicList$cycleButton = MSMixinFunctions.addButton((CDBurnerMenuScreen) (Object) this, (button, extraMusicSource) -> {
            netMusicList$lastSource = extraMusicSource;
            MSMixinFunctions.currentSource = extraMusicSource;
            if (netMusicList$searchButton != null) {
                netMusicList$searchButton.active = extraMusicSource.searchable();
            }
            netMusicList$suggestion = netMusicList$cycleButton.getValue().autoParseFromClipboard(Minecraft.getInstance().keyboardHandler.getClipboard());
        });
        if (netMusicList$lastSource != null)
            netMusicList$cycleButton.setValue(netMusicList$lastSource);
        MSMixinFunctions.currentSource = netMusicList$cycleButton.getValue();
        netMusicList$suggestion = netMusicList$cycleButton.getValue().autoParseFromClipboard(Minecraft.getInstance().keyboardHandler.getClipboard());

        var self = (CDBurnerMenuScreen) (Object) this;
        var accessor = (AbstractContainerScreenAccessor) self;
        netMusicList$searchButton = Button.builder(
                        Component.translatable("gui.net_music_list.search.button"),
                        b -> Minecraft.getInstance().setScreen(
                                new MusicSearchScreen(self, MSMixinFunctions.currentSource)))
                .bounds(accessor.getLeftPos() + 7 + 88 + 4, accessor.getTopPos() + 70, 35, 20)
                .build();
        netMusicList$searchButton.active = MSMixinFunctions.currentSource.searchable();
        ((ScreenAccessor) self).invokeAddRenderableWidget(netMusicList$searchButton);
    }
}
