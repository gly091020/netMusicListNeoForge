package com.gly091020.netMusicListNeoforge.util;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.server.music.MusicRingManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 玩家下线/换维度时把名下的物品 Ringer 移出活跃集合。
 * 播放状态保留在 SavedData 中，重新上线后由 ServerRingerEvents 的周期扫描恢复续播。
 */
@EventBusSubscriber(modid = NetMusicList.ModID)
public class EventHandler {
    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MusicRingManager.get(player.server).removeOwner(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MusicRingManager.get(player.server).removeOwner(player.getUUID());
        }
    }
}
