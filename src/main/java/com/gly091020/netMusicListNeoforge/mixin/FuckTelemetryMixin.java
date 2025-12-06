package com.gly091020.netMusicListNeoforge.mixin;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Advancement.class)
public class FuckTelemetryMixin {
    @Mutable
    @Shadow
    @Final
    private boolean sendsTelemetryEvent;

    @Inject(method = "sendsTelemetryEvent", at = @At("RETURN"))
    public void fuckTelemetry(CallbackInfoReturnable<Boolean> cir){
        // TM的数据包整那么难用还默认收集数据，收你M呢
        sendsTelemetryEvent = false;
    }
}
