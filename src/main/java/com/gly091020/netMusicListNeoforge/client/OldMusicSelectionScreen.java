package com.gly091020.netMusicListNeoforge.client;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.packet.DeleteMusicDataPacket;
import com.gly091020.netMusicListNeoforge.packet.MoveMusicDataPacket;
import com.gly091020.netMusicListNeoforge.packet.MusicListDataPacket;
import com.gly091020.netMusicListNeoforge.util.PlayMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class OldMusicSelectionScreen extends Screen {
    private final List<String> musicList;
    private static final Identifier BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath(NetMusicList.ModID,
            "textures/gui/old_bg.png");
    private final int backgroundWidth = 256;
    private final int backgroundHeight = 230;
    private int left, top;
    private PlayModeButton playModeButton;
    private MusicListWidget listWidget;
    private Integer index;
    private final PlayMode mode;
    private Button deleteButton;
    private Button upButton;
    private Button downButton;

    public OldMusicSelectionScreen(List<String> musicList, PlayMode mode, Integer index) {
        super(Component.translatable("gui.net_music_list.title"));
        this.musicList = musicList;
        this.mode = mode;
        this.index = index;
    }

    @Override
    protected void init() {
        super.init();

        // 计算UI位置（居中）
        this.left = (this.width - this.backgroundWidth) / 2;
        this.top = (this.height - this.backgroundHeight) / 2;

        // 创建音乐列表组件（非全屏）
        listWidget = new MusicListWidget();

        // 添加所有音乐条目
        for (String music : musicList) {
            listWidget.addMusicEntry(music);
        }
        listWidget.addMusicEntry(Component.translatable("gui.net_music_list.add").getString());

        listWidget.setSelected(listWidget.children().get(index));
        this.addRenderableWidget(listWidget);

        // 关闭按钮
        this.addRenderableWidget(Button.builder(Component.translatable("gui.net_music_list.close"), button -> {
                    sendPackage();
                    this.onClose();
                })
                .pos(left + backgroundWidth / 2 - 50, top + backgroundHeight - 24)
                .size(100, 20)
                .build());
        playModeButton = new PlayModeButton(left + 10, top + backgroundHeight - 90, button -> {
            playModeButton.playMode = playModeButton.playMode.getNext();
            playModeButton.setTooltip(Tooltip.create(playModeButton.playMode.getName()));
            sendPackage();
        }, mode);
        this.addRenderableWidget(playModeButton);
        deleteButton = Button.builder(Component.translatable("gui.net_music_list.delete"),
                        button -> deleteMusic())
                .pos(left + backgroundWidth - 90 - 23, top + backgroundHeight - 90)
                .size(80, 22).build();

        upButton = new MoveButton(left + backgroundWidth - 27,
                top + backgroundHeight - 90, button -> moveMusic(true), true);
        downButton = new MoveButton(left + backgroundWidth - 27,
                top + backgroundHeight - 90 + 22, button -> moveMusic(false), false);

        deleteButton.active = canDelete();
        upButton.active = canMove(true);
        downButton.active = canMove(false);

        this.addRenderableWidget(deleteButton);
        this.addRenderableWidget(upButton);
        this.addRenderableWidget(downButton);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, left, top, 0, 0, backgroundWidth, backgroundHeight, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        var fontHeight = font.lineHeight;

        graphics.centeredText(
                font,
                this.title,
                left + backgroundWidth / 2,
                top + 6,
                0xFF404040
        );

        graphics.text(
                font,
                Component.translatable("gui.net_music_list.play_list"),
                left + 10,
                top + 6 + fontHeight + 6,
                0xFF000000, false
        );
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_DELETE && canDelete()) {
            deleteMusic();
            return true;
        }
        if(event.key() == GLFW.GLFW_KEY_UP && canMove(true)){
            moveMusic(true);
            return true;
        }
        if(event.key() == GLFW.GLFW_KEY_DOWN && canMove(false)){
            moveMusic(false);
            return true;
        }
        if(event.key() == GLFW.GLFW_KEY_ESCAPE && super.keyPressed(event)){
            sendPackage();
            return true;
        }
        return super.keyPressed(event);
    }

    public void deleteMusic(){
        if (this.listWidget.getSelectedIndex() != musicList.size()) {
            var o = this.listWidget.getSelectedIndex();
            var o1 = o;
            this.musicList.remove(o);
            if (o == this.musicList.size()) {
                o--;
            }
            if (o < 0) {
                o = 0;
            }
            index = o;
            this.clearWidgets();
            this.init();
            listWidget.setSelectedIndex(o);
            this.index = listWidget.getSelectedIndex();
            sendToServer(new DeleteMusicDataPacket(o1));
            updateButton();
            sendPackage();
        }
    }

    public void moveMusic(boolean isUp){
        if (this.listWidget.getSelectedIndex() != musicList.size()) {
            var i1 = listWidget.getSelectedIndex() - (isUp ? 1 : -1);
            sendToServer(new MoveMusicDataPacket(listWidget.getSelectedIndex(), i1));
            var l = listWidget.getSelected();
            var l1 = musicList.get(listWidget.getSelectedIndex());
            musicList.set(listWidget.getSelectedIndex(), musicList.get(i1));
            musicList.set(i1, l1);
            listWidget.setEntry(listWidget.getSelectedIndex(), listWidget.children().get(i1));
            listWidget.setEntry(i1, l);
            this.index = i1;
            listWidget.setSelectedIndex(i1);
            updateButton();
            sendPackage();
        }
    }

    public static void sendToServer(CustomPacketPayload payload){
        ClientPacketDistributor.sendToServer(payload);
    }

    public void updateButton(){
        deleteButton.active = canDelete();
        upButton.active = canMove(true);
        downButton.active = canMove(false);
    }

    public boolean canDelete(){
        return this.index != musicList.size();
    }

    public boolean canMove(boolean isUp){
        if(isUp){
            if(!canDelete()){return false;}
            return this.index > 0;
        }else{
            if(!canDelete()){return false;}
            return this.index < musicList.size() - 1;
        }
    }

    private class MusicListEntry extends ObjectSelectionList.Entry<MusicListEntry> {
        private final String musicName;

        public MusicListEntry(String musicName) {
            this.musicName = musicName;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.literal(musicName);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
            // 渲染背景
            if (b) {
                guiGraphicsExtractor.fill(getX(), getY(), getX() + getContentWidth() + 3, getY() + getContentHeight() + 3, 0x80FFFFFF);
            }

            // 渲染文本
            guiGraphicsExtractor.text(
                    font,
                    Component.literal(musicName),
                    getX() + 5,
                    getY() + (getContentHeight() - 10) / 2 + 3,
                    0xFFFFFFFF
            );
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            super.mouseClicked(event, doubleClick);
            OldMusicSelectionScreen.this.index = listWidget.children().indexOf(this);
            listWidget.setSelectedIndex(index);
            sendPackage();
            updateButton();
            return true;
        }
    }

    public void sendPackage(){
        this.index = listWidget.getSelectedIndex();
        sendToServer(new MusicListDataPacket(index, this.playModeButton.playMode));
    }

    private class MusicListWidget extends ObjectSelectionList<MusicListEntry> {
        public MusicListWidget() {
            super(Minecraft.getInstance(), backgroundWidth - 10,
                    backgroundHeight - 130, 0, 12);
            this.setX(left + 5);
            this.setY(top + 35);
        }

        @Override
        protected int scrollBarX() {
            return super.scrollBarX() - 6;
        }

        @Override
        public int getRowWidth() {
            return this.width - 16;
        }

        public void addMusicEntry(String musicName) {
            this.addEntry(new MusicListEntry(musicName));
        }

        public int getSelectedIndex(){
            return this.children().indexOf(this.getSelected());
        }

        public void setSelectedIndex(int index){
            this.setSelected(this.children().get(index));
        }

        public void setEntry(int index, MusicListEntry entry){
            var l = children();
            l.set(index, entry);
        }
    }

    public static void open(List<ItemMusicCD.SongInfo> musicList, PlayMode mode, Integer index) {
        var l = new ArrayList<String>();
        for(ItemMusicCD.SongInfo info: musicList){
            if(info.artists.isEmpty()){
                l.add(info.songName);
            }else {
                var a = new StringBuilder();
                for(String artist: info.artists){
                    a.append(artist);
                    a.append("、");
                }
                var t = "";
                if(info.readOnly){
                    t = Component.translatable("gui.net_music_list.read_only").getString();
                }else if(info.vip){
                    t = Component.translatable("gui.net_music_list.vip").getString();
                }
                l.add(String.format("%s —— %s %s", a, info.songName, t));
            }
        }
        if(index < 0 || index > musicList.size()){
            NetMusicList.LOGGER.error("错误的索引：{}", index);
            return;
        }
        Minecraft.getInstance().setScreen(new OldMusicSelectionScreen(l, mode, index));
    }

    public static class PlayModeButton extends Button{
        public PlayMode playMode;
        protected PlayModeButton(int x, int y, OnPress onPress, PlayMode playMode) {
            super(x, y, 22, 22, Component.empty(), onPress, Button.DEFAULT_NARRATION);
            this.playMode = playMode;
            setTooltip(Tooltip.create(this.playMode.getName()));
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {
            extractDefaultSprite(guiGraphicsExtractor);
            float x = 0;
            switch (this.playMode){
                case SEQUENTIAL -> x = 44;
                case RANDOM -> x = 66;
                case LOOP -> x = 88;
            }
            guiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.getX(), this.getY(),
                    x, 230f, this.width, this.height, 256, 256);
        }
    }

    public static class MoveButton extends Button{
        boolean isUp;
        protected MoveButton(int x, int y, OnPress onPress, boolean isUP) {
            super(x, y, 22, 22, Component.empty(), onPress, Button.DEFAULT_NARRATION);
            isUp = isUP;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {
            extractDefaultSprite(guiGraphicsExtractor);
            guiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.getX(), this.getY(),
                    isUp ? 110 : 132, 230, this.width, this.height, 256, 256);
        }
    }
}