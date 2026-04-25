package com.gly091020.netMusicListNeoforge.packet;

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.hud.MusicInfoHud;
import com.gly091020.netMusicListNeoforge.item.NetMusicPlayerItem;
import com.gly091020.netMusicListNeoforge.sounds.PlayerEntityNetMusicSound;
import com.gly091020.netMusicListNeoforge.sounds.PlayerNetMusicSound;
import com.gly091020.netMusicListNeoforge.util.MusicManager;
import com.gly091020.netMusicListNeoforge.util.NetMusicListClientUtil;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;

public class ClientHandler {
    public static void handleClientPlayerPlayPacket(PlayerPlayMusicPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> CompletableFuture.runAsync(() -> {
            if (Minecraft.getInstance().level != null) {
                var p = Minecraft.getInstance().level.getEntity(packet.playerID());
                if (p instanceof Player player) {
                    var finalUrl = packet.url();
//                    if(NetMusicListUtil.hasLoginNeed()){
//                        var url1 = LoginNeedUtil.getUrl(finalUrl);
//                        if(url1 != null)finalUrl = url1;
//                    }
                    MusicPlayManager.play(finalUrl, packet.songName(), url -> {
                        var sound = new PlayerNetMusicSound(player, url, packet.timeSecond(), packet.slot());
                        MusicManager.addSound(sound);
                        return sound;
                    });

                    if (player == Minecraft.getInstance().player) {
                        var stack = player.getInventory().getItem(packet.slot());
                        if (stack.is(NetMusicList.MUSIC_PLAYER_ITEM.get())) {
                            var stack1 = NetMusicPlayerItem.getContainer(stack).getItem(0);
                            if (stack1.getItem() instanceof ItemMusicCD) {
                                Minecraft.getInstance().execute(() ->
                                        MusicInfoHud.setInfo(packet.info(), stack, packet.slot()));
                            }
                        }
                    }
                }
            }
        }, Util.backgroundExecutor()));
    }

    public static void handleStopMusicPacket(StopMusicPacket packet, IPayloadContext ctx){
        ctx.enqueueWork(() -> CompletableFuture.runAsync(() -> {
            var sounds = NetMusicListClientUtil.getTickableSounds();
            for(TickableSoundInstance soundInstance: sounds){
                if(soundInstance instanceof PlayerNetMusicSound sound && sound.getPlayer().getId() == packet.playerID()){
                    sound.stopMusic();
                }
            }
        }));
    }

    public static void handleClientMusicPlayerEntityPlayMusicPacket(MusicPlayerEntityPlayMusicPacket packet, IPayloadContext iPayloadContext) {
        iPayloadContext.enqueueWork(() -> CompletableFuture.runAsync(() -> {
            var finalUrl = packet.url();
//            if(NetMusicListUtil.hasLoginNeed()){
//                var url1 = LoginNeedUtil.getUrl(finalUrl);
//                if(url1 != null)finalUrl = url1;
//            }
            MusicPlayManager.play(finalUrl, packet.songName(), url ->
                    new PlayerEntityNetMusicSound(packet.entityID(), packet.url(), url, packet.timeSecond()));
        }));
    }
}
