package com.gly091020.netMusicListNeoforge.mixin;

import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.client.audio.NetMusicAudioStream;
import com.github.tartaricacid.netmusic.client.audio.NetMusicSound;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.mixin.accessor.AbstractSoundInstanceAccessor;
import com.gly091020.netMusicListNeoforge.sounds.FFmpegAudioStream;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import net.minecraft.Util;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

@Mixin(NetMusicSound.class)
public class NetMusicSoundMixin {
    @Shadow
    @Final
    private URL songUrl;

    @Inject(method = "getStream", at = @At("HEAD"), cancellable = true)
    public void ffmpegHandle(SoundBufferLibrary soundBuffers, Sound sound, boolean looping, CallbackInfoReturnable<CompletableFuture<AudioStream>> cir){
        cir.setReturnValue(CompletableFuture.supplyAsync(() -> {
            try {
                if (NetMusicList.CONFIG.forceFFmpeg) {
                    FFmpegAudioStream.applyConfig();
                    return new FFmpegAudioStream(songUrl);
                }
                return new NetMusicAudioStream(songUrl);
            } catch (Exception e) {
                NetMusicList.LOGGER.error("出现错误：", e);
                return null;
            }
        }, Util.backgroundExecutor()));
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void setVolume(BlockPos pos, URL songUrl, int timeSecond, LyricRecord lyricRecord, CallbackInfo ci){
        ((AbstractSoundInstanceAccessor) this).setVolume(NetMusicListUtil.getVolume());
    }
}
