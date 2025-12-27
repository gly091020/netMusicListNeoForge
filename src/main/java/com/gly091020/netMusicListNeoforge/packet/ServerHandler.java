package com.gly091020.netMusicListNeoforge.packet;

import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.gly091020.netMusicListNeoforge.item.NetMusicListItem;
import com.gly091020.netMusicListNeoforge.item.NetMusicPlayerItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.gly091020.netMusicListNeoforge.NetMusicList.MUSIC_LIST_ITEM;
import static com.gly091020.netMusicListNeoforge.NetMusicList.MUSIC_PLAYER_ITEM;

public class ServerHandler {
    public static void handleServerMusicListDataPacket(MusicListDataPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            ItemStack stack = player.getMainHandItem();
            if (stack.is(MUSIC_LIST_ITEM.get())) {
                NetMusicListItem.setSongIndex(stack, packet.index());
                NetMusicListItem.setPlayMode(stack, packet.playMode());
            }
        });
    }
    public static void handleServerDeleteMusicDataPacket(DeleteMusicDataPacket packet, IPayloadContext ctx){
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            ItemStack stack = player.getMainHandItem();
            if (stack.is(MUSIC_LIST_ITEM.get())) {
                NetMusicListItem.deleteSong(stack, packet.index());
            }
        });
    }

    public static void handleServerMoveMusicDataPacket(MoveMusicDataPacket packet, IPayloadContext ctx){
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            ItemStack stack = player.getMainHandItem();
            if (stack.is(MUSIC_LIST_ITEM.get())) {
                NetMusicListItem.moveSong(stack, packet.fromIndex(), packet.toIndex());
            }
        });
    }

    public static void handleServerPlayerPlayPacket(PlayerPlayMusicPacket packet, IPayloadContext ctx){
        PacketDistributor.sendToAllPlayers(packet);
    }

    public static void handleServerUpdateMusicPacket(UpdatePlayerMusicPacket packet, IPayloadContext ctx){
        var player = ctx.player();
        var stack = player.getInventory().getItem(packet.slot());
        if(stack.is(MUSIC_PLAYER_ITEM.get())){
            var container = NetMusicPlayerItem.getContainer(stack);
            var stack1 = container.getItem(0);
            NetMusicListItem.setSongIndex(stack1, packet.index());
            container.setItem(0, stack1);
            NetMusicPlayerItem.playSound(stack, player, packet.slot());
        }
    }

    public static void handleStopMusicPacket(StopMusicPacketServer packet, IPayloadContext ctx){
        PacketDistributor.sendToAllPlayers(new StopMusicPacket(packet.playerID(),packet.url()));
    }

    public static void handleUpdateMusicIndexCTSPacket(UpdateMusicIndexCTSPacket packet, IPayloadContext context) {
        var player = context.player();
        var stack = player.getInventory().getItem(packet.slot());
        if(stack.is(MUSIC_PLAYER_ITEM.get())){
            var c = NetMusicPlayerItem.getContainer(stack);
            NetMusicListItem.setSongIndex(c.getItem(0), packet.index());
            c.setChanged();
        }
    }

    public static void handleUpdateBlockLyricPacket(UpdateBlockLyricPacket updateBlockLyricPacket, IPayloadContext context) {
        var level = context.player().level();
        if(level.getBlockEntity(updateBlockLyricPacket.pos()) instanceof TileEntityMusicPlayer musicPlayer){
            musicPlayer.lyricRecord = updateBlockLyricPacket.lyric().toLyricRecord();
        }
    }

    public static void handleServerMusicPlayerEntityPlayMusicPacket(MusicPlayerEntityPlayMusicPacket packet, IPayloadContext iPayloadContext) {
        // 这名字这么越来越长了？
        PacketDistributor.sendToAllPlayers(packet);
    }
}
