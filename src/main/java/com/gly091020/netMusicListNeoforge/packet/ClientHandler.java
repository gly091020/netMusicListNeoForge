package com.gly091020.netMusicListNeoforge.packet;

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.client.MusicInfoHud;
import com.gly091020.netMusicListNeoforge.item.NetMusicPlayerItem;
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
                    MusicPlayManager.play(packet.playUrl(), packet.info().songName, url -> {
                        var sound = new PlayerNetMusicSound(player, url, packet.info().songTime, packet.slot());
                        SoundManager.sounds.put(packet.uuid(), sound);
                        if(player == Minecraft.getInstance().player){
                            var stack = player.getInventory().getItem(packet.slot());
                            if(stack.is(NetMusicList.MUSIC_PLAYER_ITEM)){
                                var stack1 = NetMusicPlayerItem.getContainer(stack).getItem(0);
                                if(stack1.getItem() instanceof ItemMusicCD){
                                    MusicInfoHud.setInfo(packet.info(), stack, packet.slot());
                                }
                            }
                        }
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
