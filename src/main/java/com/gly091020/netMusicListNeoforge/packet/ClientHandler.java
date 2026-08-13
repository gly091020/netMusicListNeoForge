package com.gly091020.netMusicListNeoforge.packet;

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.hud.MusicInfoHud;
import com.gly091020.netMusicListNeoforge.sounds.EnderPlayerNetMusicSound;
import com.gly091020.netMusicListNeoforge.sounds.RingerSound;
import com.gly091020.netMusicListNeoforge.util.MusicManager;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientHandler {
    /**
     * 播放命令：URL 已由服务端解析，客户端直接播放并替换同 Ringer 的旧声音。
     */
    public static void handleMusicPlayCommand(MusicPlayCommandPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            NetMusicList.LOGGER.info("[Ringer] 客户端收到播放命令 ringer={} entity={} url={}", packet.ringerId(), packet.entityId(), packet.url());
            if (Minecraft.getInstance().level == null) {
                return;
            }
            var entity = Minecraft.getInstance().level.getEntity(packet.entityId());
            if (entity == null) {
                return;
            }
            MusicManager.stop(packet.ringerId());
            MusicPlayManager.play(packet.url(), packet.songName(), url -> {
                var sound = new RingerSound(packet.ringerId(), entity, url, packet.rawUrl(),
                        packet.startTick(), packet.songTime() * 20, packet.slot(), packet.info());
                MusicManager.addSound(sound);
                return sound;
            });
            if (entity == Minecraft.getInstance().player) {
                MusicInfoHud.setInfo(packet.info(), packet.slot(), packet.rawUrl(), packet.ringerId());
            }
        });
    }

    public static void handleMusicStopCommand(MusicStopCommandPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> MusicManager.stop(packet.ringerId()));
    }

    public static void handleMusicSyncCommand(MusicSyncCommandPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> MusicManager.setServerPosition(packet.ringerId(), packet.positionTick()));
    }

    public static void handleClientEnderPlayerPlayPacket(PlayEnderMusicPlayerPacket packet, IPayloadContext ctx){
        ctx.enqueueWork(() -> MusicPlayManager.play(packet.url(), packet.songName(), url ->
                new EnderPlayerNetMusicSound(packet.pos(), url, packet.timeSecond())));
    }
}
