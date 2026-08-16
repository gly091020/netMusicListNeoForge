package com.gly091020.netMusicListNeoforge.client;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.entity.MusicPlayerModel;
import com.gly091020.netMusicListNeoforge.entity.MusicPlayerRenderer;
import com.gly091020.netMusicListNeoforge.client.cdpreview.CDPreviewClient;
import com.gly091020.netMusicListNeoforge.client.cdpreview.CDPreviewIconManager;
import com.gly091020.netMusicListNeoforge.hud.MusicInfoHud;
import com.gly091020.netMusicListNeoforge.hud.MusicListLayer;
import com.gly091020.netMusicListNeoforge.item.NetMusicListItem;
import com.gly091020.netMusicListNeoforge.item.NetMusicPlayerItem;
import com.gly091020.netMusicListNeoforge.packet.MusicPlayerActionPacket;
import com.gly091020.netMusicListNeoforge.util.CacheManager;
import com.gly091020.netMusicListNeoforge.util.MusicManager;
import com.gly091020.netMusicListNeoforge.util.NetMusicListKeyMapping;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import com.gly091020.netMusicListNeoforge.util.PlayMode;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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
        fastStop();
        CacheManager.tick();
        tickKey();
        MusicManager.tick();
        CDPreviewIconManager.tick();
    }

    @SubscribeEvent
    public static void onQuitWorld(LevelEvent.Unload event){
        try{
            CacheManager.checkCache(true);
        }catch (Exception e){
            NetMusicList.LOGGER.error("缓存清理时出现问题：", e);
        }
        MusicManager.clearSound();
    }

    private static void fastStop(){
        if(NetMusicListKeyMapping.FAST_STOP.consumeClick()){
            NetMusicListUtil.globalStopMusic = !NetMusicListUtil.globalStopMusic;
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
        CDPreviewClient.onRegisterClientExtensions(event);
    }

    private static void tickKey(){
        // todo:修复歌曲切换时直接停止的问题
        if (Minecraft.getInstance().screen != null) {
            wasSwitchMusicPressed = false;
            return;
        }
        var player = Minecraft.getInstance().player;
        if(player == null)return;

        boolean isPressed = NetMusicListKeyMapping.SWITCH_MUSIC.isDown();

        if (isPressed && !wasSwitchMusicPressed) {

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if(player.getInventory().getItem(i).is(NetMusicList.MUSIC_PLAYER_ITEM.get())){
                    MusicListLayer.isRender = true;
                    MusicListLayer.slot = i;
                    break;
                }
            }
        }

        if (!isPressed && wasSwitchMusicPressed) {
            if(MusicListLayer.isRender){
                MusicListLayer.isRender = false;
                NetMusicListUtil.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
                var musicPlayer = player.getInventory().getItem(MusicListLayer.slot);
                if(MusicListLayer.index >= 0 && musicPlayer.is(NetMusicList.MUSIC_PLAYER_ITEM.get())){
                    var disc = NetMusicPlayerItem.getContainer(musicPlayer).getItem(0);
                    if(MusicListLayer.index == NetMusicListItem.getSongIndex(disc)){
                        return;
                    }
                    PacketDistributor.sendToServer(new MusicPlayerActionPacket(
                            MusicPlayerActionPacket.Action.SELECT_INDEX, MusicListLayer.slot, MusicListLayer.index, PlayMode.LOOP, null));
                }
            }
        }

        wasSwitchMusicPressed = isPressed;
    }

    private static boolean wasSwitchMusicPressed = false; // 用一个全局变量的方法感觉一点也不优雅

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new PreparableReloadListener() {
            @Override
            public @NotNull CompletableFuture<Void> reload(@NotNull PreparationBarrier preparationBarrier, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller, @NotNull ProfilerFiller profilerFiller1, @NotNull Executor executor, @NotNull Executor executor1) {
                return CompletableFuture.runAsync(() -> {
                    NetMusicListUtil.needReload = true;
                }, executor).thenCompose(preparationBarrier::wait); // 俺寻思能加载
            }
        });
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MusicPlayerModel.LAYER_LOCATION,
                MusicPlayerModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NetMusicList.MUSIC_PLAYER_ENTITY.get(),
                MusicPlayerRenderer::new);
    }
}
