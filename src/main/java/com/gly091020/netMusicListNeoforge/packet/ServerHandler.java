package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.item.MusicPlayerContainer;
import com.gly091020.netMusicListNeoforge.item.NetMusicListItem;
import com.gly091020.netMusicListNeoforge.item.NetMusicPlayerItem;
import com.gly091020.netMusicListNeoforge.item.component.MusicListComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.gly091020.netMusicListNeoforge.NetMusicList.MUSIC_LIST_ITEM;
import static com.gly091020.netMusicListNeoforge.NetMusicList.MUSIC_PLAYER_ITEM;

public class ServerHandler {
    public static void handleServerPlayerPlayPacket(PlayerPlayMusicCTSPacket packet, IPayloadContext handler){
        PacketDistributor.sendToAllPlayers(new PlayerPlayMusicSTCPacket(packet.playerID(), packet.url(), packet.timeSecond(), packet.songName(), packet.slot(), packet.uuid()));
    }

    public static void handleStopMusicCTSPacket(StopMusicCTSPacket packet, IPayloadContext context){
        PacketDistributor.sendToAllPlayers(new StopMusicSTCPacket(packet.uuid()));
    }

    public static void handleUpdatePlayerMusicPacket(UpdatePlayerMusicPacket packet, IPayloadContext context){
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

    public static void handleServerMusicListDataPacket(MusicListDataPacket packet, IPayloadContext handler) {
        Player player = handler.player();
        ItemStack stack = player.getMainHandItem();
        if (stack.is(MUSIC_LIST_ITEM.get())) {
            NetMusicListItem.setSongIndex(stack, packet.index());
            NetMusicListItem.setPlayMode(stack, packet.playMode());
        }
    }

    public static void handleServerDeleteMusicDataPacket(DeleteMusicDataPacket packet, IPayloadContext handler){
        Player player = handler.player();
        ItemStack stack = player.getMainHandItem();
        if (stack.is(MUSIC_LIST_ITEM.get())) {
            NetMusicListItem.deleteSong(stack, packet.index());
        }
    }

    public static void handleServerMoveMusicDataPacket(MoveMusicDataPacket packet, IPayloadContext handler){
        Player player = handler.player();
        ItemStack stack = player.getMainHandItem();
        if (stack.is(MUSIC_LIST_ITEM.get())) {
            NetMusicListItem.moveSong(stack, packet.fromIndex(), packet.toIndex());
        }
    }
}
