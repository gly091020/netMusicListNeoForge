package com.gly091020.netMusicListNeoforge.server;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.item.NetMusicPlayerItem;
import com.gly091020.netMusicListNeoforge.server.music.MusicRingManager;
import com.gly091020.netMusicListNeoforge.server.music.PortablePlayerRinger;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = NetMusicList.ModID)
public class ServerRingerEvents {
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        var server = event.getServer();
        var manager = MusicRingManager.get(server);
        if (server.getTickCount() % 20 == 0) {
            for (var level : server.getAllLevels()) {
                for (var player : level.players()) {
                    ensureItemRingers(manager, player);
                }
            }
        }
        manager.tick();
    }

    private static void ensureItemRingers(MusicRingManager manager, ServerPlayer player) {
        var inventory = player.getInventory();
        for (var stack : inventory.items) {
            ensureItemRinger(manager, player, stack);
        }
        ensureItemRinger(manager, player, player.getOffhandItem());
    }

    private static void ensureItemRinger(MusicRingManager manager, ServerPlayer player, net.minecraft.world.item.ItemStack stack) {
        if (!stack.is(NetMusicList.MUSIC_PLAYER_ITEM.get())) {
            return;
        }
        var ringerId = NetMusicPlayerItem.getRingerId(stack);
        if (ringerId == null) {
            ringerId = NetMusicPlayerItem.getOrCreateRingerId(stack);
        }
        if (ringerId == null || manager.getRinger(ringerId) != null) {
            return;
        }
        var inner = NetMusicPlayerItem.getContainer(stack).getItem(0);
        if (inner.isEmpty()) {
            return;
        }
        var ringer = new PortablePlayerRinger(manager, player.serverLevel(), ringerId, player.getUUID());
        manager.addRinger(ringer);
        if (ringer.isRingerPlaying()) {
            ringer.resume();
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        var manager = MusicRingManager.get(event.getServer());
        manager.persistAll();
        MusicRingManager.clear(event.getServer());
    }
}
