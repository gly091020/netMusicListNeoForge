package com.gly091020.netMusicListNeoforge.mixin;

import com.github.tartaricacid.netmusic.client.gui.CDBurnerMenuScreen;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.api.musicSource.ExtraMusicSourceManager;
import com.gly091020.netMusicListNeoforge.api.musicSource.IExtraMusicSource;
import com.gly091020.netMusicListNeoforge.client.MusicSearchScreen;
import com.gly091020.netMusicListNeoforge.media.NetMusicListMedias;
import com.gly091020.netMusicListNeoforge.mixin.accessor.AbstractContainerScreenAccessor;
import com.gly091020.netMusicListNeoforge.mixin.accessor.ScreenAccessor;
import com.gly091020.netMusicListNeoforge.util.MSMixinFunctions;
import com.gly091020.netMusicListNeoforge.util.URLType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 刻录界面增强：音乐源切换按钮 + 搜索按钮；非网易云源由本模组接管刻录逻辑。
 * 旧的剪贴板自动补全（URL/歌单识别）仅在音乐源为网易云时生效。
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

    @Unique
    private static boolean isNetEaseSource() {
        return MSMixinFunctions.currentSource.equals(NetMusicListMedias.NETEASE_EXTRA_MUSIC_SOURCE);
    }

    @Unique
    private boolean netMusicListNeoForge$autoFillNetease(){
        var clipBoard = Minecraft.getInstance().keyboardHandler.getClipboard();
        var m = URLType.SONG.getMatch(clipBoard);
        if (m != null) {
            netMusicList$suggestion = m;
            return true;
        }
        m = URLType.DJ.getMatch(clipBoard);
        if (m != null) {
            netMusicList$suggestion = "dj/" + m;
            return true;
        }
        m = URLType.SONG_LIST.getMatch(clipBoard);
        if (m != null) {
            netMusicList$suggestion = "list/" + m;
            return true;
        }
        return false;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"), remap = true)
    public int renderTip(GuiGraphics instance, Font font, Component component, int left, int top, int color, boolean b) {
        if (netMusicList$suggestion != null) {
            return instance.drawString(font, Component.literal(netMusicList$suggestion).withStyle(ChatFormatting.ITALIC), left, top, color, b);
        }
        return instance.drawString(font, component, left, top, color, b);
    }

    @Inject(method = "handleCraftButton", at = @At("HEAD"), cancellable = true)
    public void onCraft(CallbackInfo ci) {
        if (textField.getValue().isEmpty() && netMusicList$suggestion != null)
            textField.setValue(netMusicList$suggestion);
        if (netMusicList$cycleButton.getValue().equals(NetMusicListMedias.NETEASE_EXTRA_MUSIC_SOURCE)) {
            // 网易云源：list/<id> 歌单走新 API，其余交给 NetMusic 原逻辑
            if (MSMixinFunctions.isPlaylistInput(textField.getValue())) {
                tips = MSMixinFunctions.tryCraftPlaylist(NetMusicListMedias.NETEASE_EXTRA_MUSIC_SOURCE,
                        MSMixinFunctions.getPlaylistId(textField.getValue()), readOnlyButton.selected());
                ci.cancel();
            }
            return;
        }
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
            netMusicListNeoForge$updateAutoFill();
        });
        if (netMusicList$lastSource != null)
            netMusicList$cycleButton.setValue(netMusicList$lastSource);
        MSMixinFunctions.currentSource = netMusicList$cycleButton.getValue();
        netMusicListNeoForge$autoSelectSource();

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

    @Unique
    private boolean netMusicListNeoForge$updateAutoFill(){
        if(isNetEaseSource()){
            return netMusicListNeoForge$autoFillNetease();
        }
        netMusicList$suggestion = netMusicList$cycleButton.getValue().autoParseFromClipboard(Minecraft.getInstance().keyboardHandler.getClipboard());
        if(netMusicList$suggestion == null){
            var l = netMusicList$cycleButton.getValue().autoParseListFromClipboard(Minecraft.getInstance().keyboardHandler.getClipboard());
            if(l == null)return false;
            netMusicList$suggestion = "list/" + l;
            return true;
        }
        return true;
    }

    @Unique
    private void netMusicListNeoForge$autoSelectSource(){
        for (IExtraMusicSource musicSource: ExtraMusicSourceManager.getAllParser()){
            netMusicList$cycleButton.setValue(musicSource);
            MSMixinFunctions.currentSource = musicSource;
            if(netMusicListNeoForge$updateAutoFill()){
                if (netMusicList$searchButton != null) {
                    netMusicList$searchButton.active = MSMixinFunctions.currentSource.searchable();
                }
                return;
            }
        }
        // 剪贴板无法匹配任何源：恢复上次手动选择的源（无记忆时用网易云）
        if (netMusicList$lastSource != null) {
            netMusicList$cycleButton.setValue(netMusicList$lastSource);
        } else {
            netMusicList$cycleButton.setValue(NetMusicListMedias.NETEASE_EXTRA_MUSIC_SOURCE);
        }
        MSMixinFunctions.currentSource = netMusicList$cycleButton.getValue();
        netMusicListNeoForge$updateAutoFill();
        if (netMusicList$searchButton != null) {
            netMusicList$searchButton.active = MSMixinFunctions.currentSource.searchable();
        }
    }
}
