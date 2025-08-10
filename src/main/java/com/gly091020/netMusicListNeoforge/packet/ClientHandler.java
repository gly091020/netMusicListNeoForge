package com.gly091020.netMusicListNeoforge.packet;

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.gly091020.netMusicListNeoforge.sounds.PlayerNetMusicSound;
import com.gly091020.netMusicListNeoforge.sounds.SoundManager;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;

public class ClientHandler {
    public static void handleClientPlayerPlayPacket(PlayerPlayMusicSTCPacket packet, IPayloadContext handler){
        CompletableFuture.runAsync(() -> {
            if (Minecraft.getInstance().level != null) {
                var p = Minecraft.getInstance().level.getEntity(packet.playerID());
                if(p instanceof Player player){
                    MusicPlayManager.play(packet.url(), packet.songName(), url -> {
                        var sound = new PlayerNetMusicSound(player, url, packet.timeSecond(), packet.slot());
                        SoundManager.sounds.put(packet.uuid(), sound);
                        return sound;
                    });
                }
            }
        }, Util.backgroundExecutor());
    }

    public static void handleStopMusicSTCPacket(StopMusicSTCPacket packet, IPayloadContext context){
        SoundManager.stopMusic(packet.uuid());
    }
}
