package com.gly091020.netMusicListNeoforge.mixin;

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.client.audio.NetMusicSound;
import com.github.tartaricacid.netmusic.network.client.MusicToClientMessageClient;
import com.github.tartaricacid.netmusic.network.message.MusicToClientMessage;
import com.gly091020.netMusicListNeoforge.util.CacheManager;
import com.gly091020.netMusicListNeoforge.util.NetMusicListClientUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MusicToClientMessageClient.class, remap = false)
public class LyricCacheMixin {
    @Inject(method = "onHandle", at = @At("HEAD"), cancellable = true)
    private static void getLyric(MusicToClientMessage message, CallbackInfo ci){
        // 很烂的Mixin
        // TODO:为什么女仆的代码我不写呢？因为我不会
        try {
            var id = NetMusicListClientUtil.getIdFromUrl(message.url());
            var lyric = CacheManager.getLycCache(id);
            if(lyric == null)return;
            MusicPlayManager.play(message.url(), message.songName(), (url) -> new NetMusicSound(message.pos(), url, message.timeSecond(), lyric.toLyricRecord()));
            ci.cancel();
        } catch (IllegalAccessException ignored) {

        }
    }
}
