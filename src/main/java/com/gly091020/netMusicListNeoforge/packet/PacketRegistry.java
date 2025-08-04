package com.gly091020.netMusicListNeoforge.packet;

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.gly091020.netMusicListNeoforge.item.NetMusicListItem;
import com.gly091020.netMusicListNeoforge.sounds.PlayerNetMusicSound;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;

import static com.gly091020.netMusicListNeoforge.NetMusicList.MUSIC_LIST_ITEM;

public class PacketRegistry {
    public static void registry(final RegisterPayloadHandlersEvent event){
        var CHANNEL = event.registrar("1.0");
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
                    MusicPlayManager.play(packet.url(), packet.songName(), url ->
                            new PlayerNetMusicSound(player, url, packet.timeSecond(), packet.slot()));
                }
            }
        }, Util.backgroundExecutor());
    }

    private static void handleServerPlayerPlayPacket(PlayerPlayMusicCTSPacket packet, IPayloadContext handler){
        PacketDistributor.sendToAllPlayers(new PlayerPlayMusicSTCPacket(packet.playerID(), packet.url(), packet.timeSecond(), packet.songName(), packet.slot()));
    }
}
