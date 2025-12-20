package com.gly091020.netMusicListNeoforge.mixin.create;

import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.gly091020.netMusicListNeoforge.create.MusicPlayerPointType;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(targets = "com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity", remap = false)
@Pseudo
public class ArmDancingMixin {
    @Inject(method = "checkForMusicAmong", at = @At("RETURN"), cancellable = true)
    public void dance(List<ArmInteractionPoint> list, CallbackInfoReturnable<Boolean> cir){
        // 兼容做在了一些奇怪的地方……
        for(ArmInteractionPoint point: list){
            if(!(point instanceof MusicPlayerPointType.NetMusicPlayerArmInteractionPoint playerPointType))continue;
            if(!(playerPointType.getLevel().getBlockEntity(point.getPos()) instanceof TileEntityMusicPlayer player))continue;
            if(player.isPlay())cir.setReturnValue(true);
        }
    }
}
