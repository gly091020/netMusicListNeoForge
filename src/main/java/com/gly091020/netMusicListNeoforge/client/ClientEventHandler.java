package com.gly091020.netMusicListNeoforge.client;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.hud.MusicInfoHud;
import com.gly091020.netMusicListNeoforge.hud.MusicListLayer;
import com.gly091020.netMusicListNeoforge.sounds.PlayerNetMusicSound;
import com.gly091020.netMusicListNeoforge.util.CacheManager;
import com.gly091020.netMusicListNeoforge.util.NetMusicListKeyMapping;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(value = Dist.CLIENT, modid = NetMusicList.ModID)
public class ClientEventHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderGui(RenderGuiEvent.Post event){
        var graphics = event.getGuiGraphics();
        MusicInfoHud.render(graphics);
        MusicListLayer.render(graphics);
        if(NetMusicListUtil.globalStopMusic){
            var w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            var h = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            event.getGuiGraphics().drawCenteredString(Minecraft.getInstance().font,
                    Component.translatable("text.net_music_list.fast_stoping"),
                    w / 2, (int) (h * 0.1f), 0xFFFFFFFF
            );
        }
    }

    @SubscribeEvent
    public static void onKeyClick(InputEvent.Key event){
        if(MusicListLayer.isRender && event.getKey() == InputConstants.KEY_ESCAPE){
            MusicListLayer.isRender = false;
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event){
        if(!MusicListLayer.isRender){return;}
        var d = event.getScrollDeltaY();
        if(Math.abs(d) >= 1){
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
        }
        MusicListLayer.index -= (int)(d * (NetMusicListKeyMapping.TOGGLE_MUSIC_SPEED_UP.isDown() ? 5 : 1));
        if(MusicListLayer.index < 0){ MusicListLayer.index = 0;}
        if(MusicListLayer.index >= MusicListLayer.count){MusicListLayer.index = MusicListLayer.count - 1;}
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event){
        soundFix();
        fastStop();
        CacheManager.tick();
    }

    @SubscribeEvent
    public static void onQuitWorld(LevelEvent.Save event){
        try{
            CacheManager.checkCache(true);
        }catch (Exception e){
            NetMusicList.LOGGER.error("缓存清理时出现问题：", e);
        }
    }

    private static void fastStop(){
        if(NetMusicListKeyMapping.FAST_STOP.consumeClick()){
            NetMusicListUtil.globalStopMusic = !NetMusicListUtil.globalStopMusic;
        }
    }

    private static void soundFix(){
        var server = Minecraft.getInstance().getSingleplayerServer();
        if(!Minecraft.getInstance().isLocalServer() || (server != null && server.isPublished())){return;}
        var sounds = NetMusicListUtil.getTickableSounds();
        var screen = Minecraft.getInstance().screen;
        // 猜猜我用了几个Mixin？
        // SB MOJANG
        if(screen != null && !NetMusicListUtil.isPaused() && screen.isPauseScreen()){
            for(TickableSoundInstance instance: sounds){
                if(instance instanceof PlayerNetMusicSound playerNetMusicSound){
                    playerNetMusicSound.onlyTickUpdate();
                }
            }
        }
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(
                new IClientItemExtensions() {
                    @Override
                    public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        return new NetMusicListManualRenderer();
                    }
                },
                NetMusicList.MANUAL
        );
    }
}
