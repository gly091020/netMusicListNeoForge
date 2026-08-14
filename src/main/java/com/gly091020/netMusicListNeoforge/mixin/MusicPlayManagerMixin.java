package com.gly091020.netMusicListNeoforge.mixin;

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.gly091020.netMusicListNeoforge.media.NetMusicListMedias;
import com.gly091020.netMusicListNeoforge.api.musicParser.MusicParserManager;
import com.gly091020.netMusicListNeoforge.api.musicSource.MusicSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.URI;
import java.util.Optional;

@Mixin(value = MusicPlayManager.class, remap = false)
public class MusicPlayManagerMixin {
    @Inject(method = "getFinalUrl", at = @At("HEAD"), cancellable = true)
    private static void parser(String url, CallbackInfoReturnable<Optional<String>> cir) {
        var ms = MusicSource.tryPasteFromURI(URI.create(url));
        if (ms == null) return;
        var p = MusicParserManager.getParser(ms);
        if (p == null) {
            NetMusicListMedias.LOGGER.error("无效链接：{}", url);
            cir.setReturnValue(Optional.empty());
            return;
        }
        var r = p.parse(ms);
        if (r == null) {
            NetMusicListMedias.LOGGER.error("解析失败：{}", url);
            cir.setReturnValue(Optional.empty());
            return;
        }
        cir.setReturnValue(Optional.of(r.toString()));
    }
}
