package com.gly091020.netMusicListNeoforge.packet;

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.item.MusicPlayerContainer;
import com.gly091020.netMusicListNeoforge.item.NetMusicListItem;
import com.gly091020.netMusicListNeoforge.item.NetMusicPlayerItem;
import com.gly091020.netMusicListNeoforge.item.component.MusicListComponent;
import com.gly091020.netMusicListNeoforge.sounds.PlayerNetMusicSound;
import com.gly091020.netMusicListNeoforge.sounds.SoundManager;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;

import static com.gly091020.netMusicListNeoforge.NetMusicList.MUSIC_LIST_ITEM;
import static com.gly091020.netMusicListNeoforge.NetMusicList.MUSIC_PLAYER_ITEM;

public class PacketRegistry {
    public static void registry(final RegisterPayloadHandlersEvent event){
        var CHANNEL = event.registrar("1.1");
        CHANNEL.commonToServer(
                DeleteMusicDataPacket.TYPE,
                DeleteMusicDataPacket.STREAM_CODEC,
                PacketRegistry::handleServerDeleteMusicDataPacket
        );
        CHANNEL.commonToServer(
                MusicListDataPacket.TYPE,
                MusicListDataPacket.STREAM_CODEC,
                PacketRegistry::handleServerMusicListDataPacket
        );

        CHANNEL.commonToServer(MoveMusicDataPacket.TYPE,
                MoveMusicDataPacket.STREAM_CODEC,
                PacketRegistry::handleServerMoveMusicDataPacket
        );

        CHANNEL.commonToClient(
                PlayerPlayMusicSTCPacket.TYPE,
                PlayerPlayMusicSTCPacket.STREAM_CODEC,
                PacketRegistry::handleClientPlayerPlayPacket
        );

        CHANNEL.commonToServer(
                PlayerPlayMusicCTSPacket.TYPE,
                PlayerPlayMusicCTSPacket.STREAM_CODEC,
                PacketRegistry::handleServerPlayerPlayPacket
        );
        CHANNEL.commonToClient(
                StopMusicSTCPacket.TYPE,
                StopMusicSTCPacket.STREAM_CODEC,
                PacketRegistry::handleStopMusicSTCPacket
        );
        CHANNEL.commonToServer(
                StopMusicCTSPacket.TYPE,
                StopMusicCTSPacket.STREAM_CODEC,
                PacketRegistry::handleStopMusicCTSPacket
        );
        CHANNEL.commonToServer(
                UpdatePlayerMusicPacket.TYPE,
                UpdatePlayerMusicPacket.STREAM_CODEC,
                PacketRegistry::handleUpdatePlayerMusicPacket
        );
    }

    private static void handleServerMusicListDataPacket(MusicListDataPacket packet, IPayloadContext handler) {
        Player player = handler.player();
        ItemStack stack = player.getMainHandItem();
        if (stack.is(MUSIC_LIST_ITEM.get())) {
            NetMusicListItem.setSongIndex(stack, packet.index());
            NetMusicListItem.setPlayMode(stack, packet.playMode());
        }
    }

    private static void handleServerDeleteMusicDataPacket(DeleteMusicDataPacket packet, IPayloadContext handler){
        Player player = handler.player();
        ItemStack stack = player.getMainHandItem();
        if (stack.is(MUSIC_LIST_ITEM.get())) {
            NetMusicListItem.deleteSong(stack, packet.index());
        }
    }

    private static void handleServerMoveMusicDataPacket(MoveMusicDataPacket packet, IPayloadContext handler){
        Player player = handler.player();
        ItemStack stack = player.getMainHandItem();
        if (stack.is(MUSIC_LIST_ITEM.get())) {
            NetMusicListItem.moveSong(stack, packet.fromIndex(), packet.toIndex());
        }
    }

    private static void handleClientPlayerPlayPacket(PlayerPlayMusicSTCPacket packet, IPayloadContext handler){
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

    private static void handleServerPlayerPlayPacket(PlayerPlayMusicCTSPacket packet, IPayloadContext handler){
        PacketDistributor.sendToAllPlayers(new PlayerPlayMusicSTCPacket(packet.playerID(), packet.url(), packet.timeSecond(), packet.songName(), packet.slot(), packet.uuid()));
    }

    private static void handleStopMusicSTCPacket(StopMusicSTCPacket packet, IPayloadContext context){
        SoundManager.stopMusic(packet.uuid());
    }

    private static void handleStopMusicCTSPacket(StopMusicCTSPacket packet, IPayloadContext context){
        PacketDistributor.sendToAllPlayers(new StopMusicSTCPacket(packet.uuid()));
    }

    private static void handleUpdatePlayerMusicPacket(UpdatePlayerMusicPacket packet, IPayloadContext context){
        var stack = context.player().getInventory().getItem(packet.slot());
        if(stack.is(MUSIC_PLAYER_ITEM)){
            var container = new MusicPlayerContainer(stack);
            var stack1 = container.getItem(0);
            var c = stack1.getOrDefault(NetMusicList.MUSIC_LIST_COMPONENT, MusicListComponent.getDefault());
            stack1.set(NetMusicList.MUSIC_LIST_COMPONENT, new MusicListComponent(c.songList(), c.playMode(), packet.index()));
            container.setItem(0, stack1);
            stack.set(NetMusicList.MUSIC_PLAYER_UUID, packet.uuid());
            var uuid = packet.uuid();
            stack.set(NetMusicList.MUSIC_PLAYER_UUID, uuid);
            NetMusicPlayerItem.playSound(stack, context.player(), packet.slot());
        }
    }
}
