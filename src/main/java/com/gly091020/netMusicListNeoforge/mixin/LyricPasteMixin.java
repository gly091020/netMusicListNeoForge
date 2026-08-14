package com.gly091020.netMusicListNeoforge.mixin;

import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.client.audio.NetMusicSound;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.network.message.MusicToClientMessage;
import com.gly091020.netMusicListNeoforge.api.musicSource.ExtraMusicSourceManager;
import com.gly091020.netMusicListNeoforge.api.musicSource.MusicSource;
import com.gly091020.netMusicListNeoforge.util.CacheManager;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;

/**
 * 为 netmusiclib 音乐源补充歌词：NetMusic 本体只会解析网易云直链的歌词，
 * 这里在播放前检查是否有额外音乐源能提供歌词，有则直接接管播放。
 */
@Mixin(value = MusicToClientMessage.class, remap = false)
public class LyricPasteMixin {
    @Inject(method = "onHandle", at = @At("HEAD"), cancellable = true)
    private static void pasteLyric(MusicToClientMessage message, CallbackInfo ci) {
        if (!GeneralConfig.ENABLE_PLAYER_LYRICS.get()) return;

        try{
            var ms = MusicSource.pasteFromSongUrl(URI.create(message.url()));
            var lyric = CacheManager.getLycCache(ms);
            if(lyric != null) {
                MusicPlayManager.play(message.url(), message.songName(), (url) -> new NetMusicSound(message.pos(), url, message.timeSecond(), lyric.toLyricRecord()));
                ci.cancel();
                return;
            }
        } catch (Exception e) {
            try {
                var id = NetMusicListUtil.getIdFromUrl(message.url());
                var lyric = CacheManager.getLycCache(id);
                if(lyric != null) {
                    MusicPlayManager.play(message.url(), message.songName(), (url) -> new NetMusicSound(message.pos(), url, message.timeSecond(), lyric.toLyricRecord()));
                    ci.cancel();
                    return;
                }
            } catch (Exception ignored) {}
        }

        var ms = MusicSource.tryPasteFromURI(URI.create(message.url()));
        if (ms == null) return;
        var p = ExtraMusicSourceManager.getParser(ms);
        if (p == null) return;
        LyricRecord l = p.parseLyric(ms.identifier());
        if (l != null) {
            MusicPlayManager.play(message.url(), message.songName(),
                    url -> new NetMusicSound(message.pos(), url, message.timeSecond(), l));
            ci.cancel();
        }
    }
}
