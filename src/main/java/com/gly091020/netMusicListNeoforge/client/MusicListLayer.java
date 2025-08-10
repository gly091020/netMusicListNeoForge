package com.gly091020.netMusicListNeoforge.client;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.item.NetMusicPlayerItem;
import com.gly091020.netMusicListNeoforge.item.component.MusicListComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.InputEvent;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class MusicListLayer implements LayeredDraw.Layer{
    public static boolean isRender = false;
    public static int index = -1;
    private static int count = 0;
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker deltaTracker) {
        if(!isRender){
            index = -1;
            return;
        }
        if(Minecraft.getInstance().options.hideGui){return;}
        var p = Minecraft.getInstance().player;
        if(p == null){return;}
        var i = p.getMainHandItem();
        if(!i.is(NetMusicList.MUSIC_PLAYER_ITEM)){
            isRender = false;
            return;
        }
        var disc = NetMusicPlayerItem.getContainer(i).getItem(0);
        if(!disc.is(NetMusicList.MUSIC_LIST_ITEM)){
            isRender = false;
            return;
        }
        var m = disc.getOrDefault(NetMusicList.MUSIC_LIST_COMPONENT, MusicListComponent.getDefault());
        if(index == -1){
            index = m.index();
        }
        var j = 0;
        var font = Minecraft.getInstance().font;
        var width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        var height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        var max_width = 0;
        var songList = m.songList();
        count = songList.size();
        if(m.index() == songList.size()){
            isRender = false;
            return;
        }

        if(index - 5 < 0){
            for(int l = 0; l < 10; l++){
                if(l >= songList.size()){continue;}
                var name = songList.get(l).songName;
                if(font.width(name) > max_width){
                    max_width = font.width(name);
                }
            }
        } else if (index + 5 >= songList.size()) {
            for (int l = songList.size() - 10; l < songList.size(); l++) {
                if(l < 0){continue;}
                var name = songList.get(l).songName;
                if (font.width(name) > max_width) {
                    max_width = font.width(name);
                }
            }
        }else{
            for(int l = index - 5; l < index + 5; l++){
                if(l >= songList.size() || l < 0){continue;}
                var name = songList.get(l).songName;
                if(font.width(name) > max_width){
                    max_width = font.width(name);
                }
            }
        }

        if(max_width <= 0){
            isRender = false;
            return;
        }

        RenderSystem.enableBlend();
        guiGraphics.fill(width - max_width - 9, (int) (height * 0.3) - 4, width - 5, (int) (height * 0.3 + 10 * (font.lineHeight + 2)), 0x80000000);

        if(index - 5 < 0){
            for(int l = 0; l < 10; l++){
                if(l >= songList.size()){continue;}
                guiGraphics.drawString(font, songList.get(l).songName, width - max_width - 7, (int) (height * 0.3 + j * (font.lineHeight + 2)), 0xFFFFFF);
                if(l == index){
                    guiGraphics.fill(width - max_width - 7, (int) (height * 0.3 + j * (font.lineHeight + 2)) - 2,
                            width - 7, (int) (height * 0.3 + (j + 1) * (font.lineHeight + 2)) - 2, 0x80FFFFFF);
                }
                j++;
            }
        } else if (index + 5 >= songList.size()) {
            for(int l = songList.size() - 10; l < songList.size(); l++){
                if(l < 0){continue;}
                guiGraphics.drawString(font, songList.get(l).songName, width - max_width - 7, (int) (height * 0.3 + j * (font.lineHeight + 2)), 0xFFFFFF);
                if(l == index){
                    guiGraphics.fill(width - max_width - 7, (int) (height * 0.3 + j * (font.lineHeight + 2)) - 2,
                            width - 7, (int) (height * 0.3 + (j + 1) * (font.lineHeight + 2)) - 2, 0x80FFFFFF);
                }
                j++;
            }
        }else{
            for(int l = index - 5; l < index + 5; l++){
                if(l >= songList.size() || l < 0){continue;}
                guiGraphics.drawString(font, songList.get(l).songName, width - max_width - 7, (int) (height * 0.3 + j * (font.lineHeight + 2)), 0xFFFFFF);
                if(l == index){
                    guiGraphics.fill(width - max_width - 7, (int) (height * 0.3 + j * (font.lineHeight + 2)) - 2,
                            width - 7, (int) (height * 0.3 + (j + 1) * (font.lineHeight + 2)) - 2, 0x80FFFFFF);
                }
                j++;
            }
        }

        RenderSystem.disableBlend();
    }

    public static void onMouseScroll(InputEvent.MouseScrollingEvent event){
        var d = event.getScrollDeltaY();
        if(Math.abs(d) >= 1){
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
        }
        index -= (int)(d * (Minecraft.getInstance().player != null &&
                Minecraft.getInstance().player.isShiftKeyDown() ? 5 : 1));
        if(index < 0){index = 0;}
        if(index >= count){index = count - 1;}
    }
}
