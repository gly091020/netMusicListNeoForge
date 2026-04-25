package com.gly091020.netMusicListNeoforge.mixin;

import com.github.tartaricacid.netmusic.inventory.io.CDInput;
import com.github.tartaricacid.netmusic.inventory.io.CDOutput;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class CdOutputAndInputMixin {
    @Mixin(CDOutput.class)
    public static class Output{
        @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
        public void valid(ItemResource resource, CallbackInfoReturnable<Boolean> cir){
            if(resource.is(NetMusicList.MUSIC_LIST_ITEM))cir.setReturnValue(true);
        }
    }

    @Mixin(CDInput.class)
    public static class Input{
        @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
        public void valid(ItemResource resource, CallbackInfoReturnable<Boolean> cir){
            if(resource.is(NetMusicList.MUSIC_LIST_ITEM))cir.setReturnValue(true);
        }
    }
}
