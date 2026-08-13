package com.gly091020.netMusicListNeoforge.packet;

import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.gly091020.netMusicListNeoforge.item.NetMusicListItem;
import com.gly091020.netMusicListNeoforge.item.NetMusicPlayerItem;
import com.gly091020.netMusicListNeoforge.server.music.MusicRingManager;
import com.gly091020.netMusicListNeoforge.server.music.PortablePlayerRinger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

    public static void handleMusicPlayerAction(MusicPlayerActionPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return;
            }
            var stack = serverPlayer.getInventory().getItem(packet.slot());
            if (!stack.is(MUSIC_PLAYER_ITEM.get())) {
                return;
            }
            switch (packet.action()) {
                case SET_MODE -> {
                    var inner = NetMusicPlayerItem.getContainer(stack).getItem(0);
                    if (inner.is(MUSIC_LIST_ITEM.get())) {
                        NetMusicListItem.setPlayMode(inner, packet.mode());
                        var container = NetMusicPlayerItem.getContainer(stack);
                        container.setItem(0, inner);
                        container.setChanged();
                    }
                }
                case STOP -> {
                    var ringer = getRinger(serverPlayer, stack);
                    if (ringer != null) {
                        ringer.stop();
                    }
                }
                case PLAY, NEXT, SELECT_INDEX -> {
                    var ringer = getOrCreateRinger(serverPlayer, stack);
                    if (ringer == null) {
                        return;
                    }
                    switch (packet.action()) {
                        case PLAY -> ringer.play(packet.info());
                        case NEXT -> ringer.next();
                        case SELECT_INDEX -> ringer.selectIndex(packet.index());
                        default -> {
                        }
                    }
                }
            }
        });
    }

    public static void handleUpdateBlockLyricPacket(UpdateBlockLyricPacket updateBlockLyricPacket, IPayloadContext context) {
        var level = context.player().level();
        if(level.getBlockEntity(updateBlockLyricPacket.pos()) instanceof TileEntityMusicPlayer musicPlayer){
            musicPlayer.lyricRecord = updateBlockLyricPacket.lyric().toLyricRecord();
        }
    }

    private static PortablePlayerRinger getRinger(ServerPlayer player, ItemStack stack) {
        var ringerId = NetMusicPlayerItem.getRingerId(stack);
        if (ringerId == null) {
            return null;
        }
        var ringer = MusicRingManager.get(player.serverLevel()).getRinger(ringerId);
        return ringer instanceof PortablePlayerRinger ppr ? ppr : null;
    }

    private static PortablePlayerRinger getOrCreateRinger(ServerPlayer player, ItemStack stack) {
        var ringer = getRinger(player, stack);
        if (ringer != null) {
            return ringer;
        }
        var ringerId = NetMusicPlayerItem.getOrCreateRingerId(stack);
        var manager = MusicRingManager.get(player.serverLevel());
        var created = new PortablePlayerRinger(manager, player.serverLevel(), ringerId, player.getUUID());
        manager.addRinger(created);
        return created;
    }
}
