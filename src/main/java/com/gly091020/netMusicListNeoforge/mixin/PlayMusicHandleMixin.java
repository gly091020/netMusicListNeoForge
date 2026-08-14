package com.gly091020.netMusicListNeoforge.mixin;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.gly091020.netMusicListNeoforge.api.musicSource.MusicSource;
import com.gly091020.netMusicListNeoforge.util.CacheManager;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.function.Function;

@Mixin(value = MusicPlayManager.class, remap = false)
public abstract class PlayMusicHandleMixin {
    @Shadow
    private static void playMusic(String url, String songName, Function<URL, SoundInstance> sound) {
    }

    @Shadow
    public static Optional<String> getFinalUrl(String url) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private static void onPlayMusic(String url, String songName, Function<URL, SoundInstance> sound, CallbackInfo ci){
        var t = new Thread(() -> {
            try{
                var ms = MusicSource.pasteFromSongUrl(URI.create(url));
                if(CacheManager.hasCache(ms)) {
                    var songUrl = CacheManager.getSongCache(ms);
                    if(songUrl != null){
                        Minecraft.getInstance().execute(
                                () -> playMusic(songUrl, songName, sound)
                        );
                        return;
                    }
                }
            } catch (Exception ignored) {}

            try{
                var id = NetMusicListUtil.getIdFromUrl(url);
                if(CacheManager.hasCache(id)){
                    var songUrl = CacheManager.getSongCache(id);
                    if(songUrl != null){
                        Minecraft.getInstance().execute(
                                () -> playMusic(songUrl, songName, sound)
                        );
                        return;
                    }
                }
            } catch (Exception ignored) {
            }
            netMusicListNeoForge$origin(url, songName, sound);
        });
        t.start();
        ci.cancel();
    }

    @Unique
    private static void netMusicListNeoForge$origin(String url, String songName, Function<URL, SoundInstance> sound) {
        Optional<String> finalUrl = getFinalUrl(url);

        if (finalUrl.isPresent()) {
            playMusic(finalUrl.get(), songName, sound);
        } else {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.sendSystemMessage(Component.translatable("message.netmusic.music_player.404", url).withStyle(ChatFormatting.RED));
            }
            NetMusic.LOGGER.info("Music not found: {}", url);
        }
    }
}
