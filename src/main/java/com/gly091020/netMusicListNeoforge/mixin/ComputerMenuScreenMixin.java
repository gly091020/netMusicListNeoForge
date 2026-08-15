package com.gly091020.netMusicListNeoforge.mixin;

import com.github.tartaricacid.netmusic.client.gui.ComputerMenuScreen;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.SetMusicIDMessage;
import com.gly091020.netMusicListNeoforge.util.MSMixinFunctions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(ComputerMenuScreen.class)
public class ComputerMenuScreenMixin {
    @Shadow
    private Component tips;

    @Shadow
    private EditBox urlTextField;

    @Inject(method = "handleCraftButton", at = @At("RETURN"), cancellable = true)
    public void craftDir(CallbackInfo ci){
        Path dir;
        try{
            dir = Path.of(urlTextField.getValue());
        } catch (Exception e) {
            return;
        }
        if(!dir.toFile().isDirectory()) {
            return;
        }

        var result = MSMixinFunctions.tryCraftLocalDir(dir);
        if(result.isEmpty()){
            tips = Component.translatable("gui.netmusic.computer.url.error");
            ci.cancel();
            return;
        }

        result.forEach(songInfo -> NetworkHandler.sendToServer(new SetMusicIDMessage(songInfo)));
        tips = Component.empty();
        ci.cancel();
    }

    @Inject(method = "handleCraftButton", at = @At("HEAD"))
    private void removeQuotes(CallbackInfo ci){
        urlTextField.setValue(MSMixinFunctions.trimQuotes(urlTextField.getValue()));
    }
}
