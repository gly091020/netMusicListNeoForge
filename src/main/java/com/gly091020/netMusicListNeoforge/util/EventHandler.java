package com.gly091020.netMusicListNeoforge.util;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.item.NetMusicPlayerItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = NetMusicList.ModID)
public class EventHandler {
    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event){
        handlePlayerEvent(event);
    }

    @SubscribeEvent
    public static void onSpawn(PlayerEvent.PlayerRespawnEvent event){
        handlePlayerEvent(event);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event){
        handlePlayerEvent(event);
    }

    @SubscribeEvent
    public static void onGetItem(ItemEntityPickupEvent.Post event){
        if(event.getOriginalStack().is(NetMusicList.MUSIC_PLAYER_ITEM)){
            playerPlayMusic(event.getPlayer());
        }
    }

    private static void handlePlayerEvent(PlayerEvent event){
        if(hasPlayer(event.getEntity())){
            playerPlayMusic(event.getEntity());
        }
    }

    private static boolean hasPlayer(Player player){
        return player.getInventory().countItem(NetMusicList.MUSIC_PLAYER_ITEM.get()) > 0;
    }

    private static void playerPlayMusic(Player player){
        for(ItemStack stack: player.getInventory()){
            if(stack.is(NetMusicList.MUSIC_PLAYER_ITEM.get())){
                NetMusicPlayerItem.playSound(stack, player,
                        player.getInventory().findSlotMatchingItem(stack));
                NetMusicPlayerItem.sendPacket(stack, player,
                        player.getInventory().findSlotMatchingItem(stack));
            }
        }
    }
}
