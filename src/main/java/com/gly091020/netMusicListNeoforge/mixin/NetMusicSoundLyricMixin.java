package com.gly091020.netMusicListNeoforge.mixin;

import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.client.audio.NetMusicSound;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.gly091020.netMusicListNeoforge.packet.UpdateBlockLyricPacket;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URL;

import static com.gly091020.netMusicListNeoforge.NetMusicList.CONFIG;

@Mixin(NetMusicSound.class)
public class NetMusicSoundLyricMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    public void sendLyric(BlockPos pos, URL songUrl, int timeSecond, LyricRecord lyricRecord, CallbackInfo ci){
        if(CONFIG.allowLyricToServer && lyricRecord != null){
            NetworkHandler.sendToServer(new UpdateBlockLyricPacket(pos,
                    NetMusicListUtil.Lyric.fromLyricRecord(lyricRecord)));
        }
    }
}
