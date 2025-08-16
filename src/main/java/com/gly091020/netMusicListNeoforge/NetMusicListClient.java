package com.gly091020.netMusicListNeoforge;

import com.gly091020.netMusicListNeoforge.client.MusicInfoHud;
import com.gly091020.netMusicListNeoforge.client.MusicListLayer;
import com.gly091020.netMusicListNeoforge.config.ConfigScreenGetter;
import com.gly091020.netMusicListNeoforge.packet.PacketRegistry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@Mod(value = NetMusicList.ModID, dist = Dist.CLIENT)
public class NetMusicListClient {
    public NetMusicListClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) ->
                ConfigScreenGetter.getConfigScreen(parent)
        );
        modEventBus.addListener(PacketRegistry::registryClient);
        NetMusicListUtil.reloadConfig();
    }
}

@EventBusSubscriber(modid = NetMusicList.ModID, value = Dist.CLIENT)
class EventHandler{
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "music_list_layer"),
                new MusicListLayer());
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "music_info_layer"),
                new MusicInfoHud());
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if(MusicListLayer.isRender){
            MusicListLayer.onMouseScroll(event);
            event.setCanceled(true);
        }
    }
}
