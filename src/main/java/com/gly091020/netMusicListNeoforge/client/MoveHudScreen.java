package com.gly091020.netMusicListNeoforge.client;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.hud.MusicInfoHud;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import static com.gly091020.netMusicListNeoforge.NetMusicList.CONFIG;

public class MoveHudScreen extends Screen {
    public int x;
    public int y;
    private final Screen parent;
    protected MoveHudScreen(Screen parent) {
        super(Component.empty());
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.translatable("config.net_music_list.hud.reset"), button -> {
            x = 10;
            y = 10;
        }).pos(width - 170, height - 30)
                .size(50, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("config.net_music_list.hud.close"), button -> onClose()).pos(width - 60, height - 30)
                .size(50, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("config.net_music_list.hud.not_save"), button -> Minecraft.getInstance().setScreen(parent)).pos(width - 115, height - 30)
                .size(50, 20)
                .build());
        x = NetMusicList.CONFIG.x;
        y = NetMusicList.CONFIG.y;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        extractBackground(graphics, mouseX, mouseY, a);
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.fill(x, y, x + 100, y + 40, 0xFFAAAAAA);
        graphics.centeredText(Minecraft.getInstance().font,
                Component.translatable("config.net_music_list.hud.text"),
                x + 50, y + 20 - Minecraft.getInstance().font.lineHeight / 2, 0xFFFFFFFF);
        graphics.centeredText(Minecraft.getInstance().font,
                Component.translatable("config.net_music_list.hud.title"),
                width / 2, 10, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if(super.mouseDragged(event, dx, dy))return false;
        x = (int) event.x() - 50;
        y = (int) event.y() - 20;
        return true;
    }

    @Override
    public void onClose() {
        NetMusicList.CONFIG.x = x;
        NetMusicList.CONFIG.y = y;
        NetMusicListUtil.reloadConfig();
        Minecraft.getInstance().setScreen(parent);
        MusicInfoHud.setPos(CONFIG.x, CONFIG.y);
    }

    public static void open(){
        Minecraft.getInstance().setScreen(new MoveHudScreen(Minecraft.getInstance().screen));
    }
}
