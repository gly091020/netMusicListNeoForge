package com.gly091020.netMusicListNeoforge.mixin;

import com.github.tartaricacid.netmusic.client.gui.CDBurnerMenuScreen;
import com.gly091020.netMusicListNeoforge.util.MSMixinFunctions;
import com.gly091020.netMusicListNeoforge.util.URLType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CDBurnerMenuScreen.class, remap = false)
public class BurnerSongListMixin {
    @Shadow
    private EditBox textField;
    @Shadow
    private Component tips;
    @Shadow
    private Checkbox readOnlyButton;
    @Unique
    private String netmusiclistforge$idTip;
    @Inject(method = "handleCraftButton", at = @At(value = "INVOKE", target = "Ljava/util/regex/Matcher;matches()Z"), cancellable = true)
    public void onCraft(CallbackInfo ci){
        if (MSMixinFunctions.isPlaylistInput(textField.getValue())) {
            tips = MSMixinFunctions.tryCraftPlaylist(MSMixinFunctions.currentSource,
                    MSMixinFunctions.getPlaylistId(textField.getValue()), readOnlyButton.selected());
            ci.cancel();
        }
    }

    @Inject(method = "handleCraftButton", at = @At("HEAD"))
    public void setId(CallbackInfo ci){
        if(netmusiclistforge$idTip != null && textField.getValue().isEmpty()){
            textField.setValue(netmusiclistforge$idTip);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"), remap = true)
    public int renderTip(GuiGraphics instance, Font font, Component component, int left, int top, int color, boolean b){
        return instance.drawString(font, netmusiclistforge$idTip == null ? component : Component.literal(netmusiclistforge$idTip).withStyle(ChatFormatting.ITALIC), left, top, color, b);
    }

    @Inject(method = "init", at = @At("TAIL"), remap = true)
    public void addTip(CallbackInfo ci){
        var clipBoard = Minecraft.getInstance().keyboardHandler.getClipboard();
        var m = URLType.SONG.getMatch(clipBoard);
        if(m != null){
            netmusiclistforge$idTip = m;
        }
        m = URLType.DJ.getMatch(clipBoard);
        if(m != null){
            netmusiclistforge$idTip = "dj/" + m;
        }
        m = URLType.SONG_LIST.getMatch(clipBoard);
        if(m != null){
            netmusiclistforge$idTip = "list/" + m;
        }
    }
}
