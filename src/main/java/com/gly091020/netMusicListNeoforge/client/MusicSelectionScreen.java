package com.gly091020.netMusicListNeoforge.client;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.config.ConfigScreenGetter;
import com.gly091020.netMusicListNeoforge.packet.DeleteMusicDataPacket;
import com.gly091020.netMusicListNeoforge.packet.MoveMusicDataPacket;
import com.gly091020.netMusicListNeoforge.packet.MusicListDataPacket;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import com.gly091020.netMusicListNeoforge.util.PlayMode;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class MusicSelectionScreen extends Screen {
    private final List<ItemMusicCD.SongInfo> musicList;
    private static final Identifier BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath(NetMusicList.ModID,
            "textures/gui/bg.png");
    private static final Identifier GLY091020 = Identifier.fromNamespaceAndPath(NetMusicList.ModID,
            "textures/gui/gly091020.png");
    private static Identifier PLAYER_HAND = null;
    private static final Identifier BUTTON_TEXTURE = Identifier.fromNamespaceAndPath(NetMusicList.ModID, "button/button");
    private final int backgroundWidth = 321;
    private final int backgroundHeight = 161;
    private int left, top;
    private PlayModeButton playModeButton;
    private MusicListWidget listWidget;
    private Integer index;
    private final PlayMode mode;
    private Button deleteButton;
    private Button upButton;
    private Button downButton;

    private float lastScroll = 0;
    private float CDRotation = 0;
    private float nowSpeed = 0;

    private float pointerRotation = 0;

    public MusicSelectionScreen(List<ItemMusicCD.SongInfo> musicList, PlayMode mode, Integer index) {
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
        for (ItemMusicCD.SongInfo music : musicList) {
            listWidget.addMusicEntry(music);
        }
        listWidget.addEntry(new AddMusicEntry());

        var count = listWidget.children().size();
        if(index < 0 || index >= count){
            listWidget.setSelected(listWidget.children().get(count - 1));
            index = count - 1;
        }else{
            listWidget.setSelected(listWidget.children().get(index));
        }
        this.addRenderableWidget(listWidget);

        playModeButton = new PlayModeButton(left + 4 + 3, top + 133, button -> {
            playModeButton.playMode = playModeButton.playMode.getNext();
            playModeButton.setTooltip(Tooltip.create(playModeButton.playMode.getName()));
            sendPackage();
        }, mode);
        this.addRenderableWidget(playModeButton);
        deleteButton = new Button(Button.builder(Component.translatable("gui.net_music_list.delete"),
                        _ -> deleteMusic())
                .pos(left + 4 + 66 + 3, top + 133)
                .size(50, 22)){
            @Override
            protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {
                guiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, BUTTON_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight());
                super.extractDefaultLabel(guiGraphicsExtractor.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
            }
        };

        upButton = new MoveButton(left + 4 + 22 + 3,
                top + 133, button -> moveMusic(true), true);
        downButton = new MoveButton(left + 4 + 44 + 3,
                top + 133, button -> moveMusic(false), false);
        Button settingButton = new SettingButton(left + backgroundWidth + 3, top);

        deleteButton.active = canDelete();
        upButton.active = canMove(true);
        downButton.active = canMove(false);

        this.addRenderableWidget(deleteButton);
        this.addRenderableWidget(upButton);
        this.addRenderableWidget(downButton);
        this.addRenderableWidget(settingButton);

        lastScroll = (float) listWidget.scrollAmount();
        nowSpeed = 0;
        if(musicList.size() == listWidget.getSelectedIndex()){
            pointerRotation = 45;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, left, top, 0, 0, backgroundWidth, backgroundHeight, 512, 256);
    }

    public void renderCD(@NotNull GuiGraphicsExtractor guiGraphics, float delta){
        guiGraphics.pose().pushMatrix();
        float scrollSpeed = (float) Math.abs(listWidget.scrollAmount() - lastScroll);
        if(scrollSpeed <= 0.3){
            scrollSpeed = 0;
        }
        if(Math.abs(scrollSpeed - nowSpeed) > 0.01){
            if (scrollSpeed > nowSpeed) {
                nowSpeed += 0.1f;
            } else {
                nowSpeed -= 0.1f;
            }
        }else{
            nowSpeed = scrollSpeed;
        }
        nowSpeed = (float) Math.clamp(nowSpeed, 0, 0.5);
        CDRotation += nowSpeed * delta * 10;
        if(Math.abs(lastScroll - listWidget.scrollAmount()) <= 3){
            lastScroll = (float) listWidget.scrollAmount();
        }else {
            if (lastScroll < listWidget.scrollAmount()) {
                lastScroll += (float) (Math.abs(lastScroll - listWidget.scrollAmount()) / 10);
            } else {
                lastScroll -= (float) (Math.abs(lastScroll - listWidget.scrollAmount()) / 10);
            }
        }

        var x = left + 15;
        var y = top + 15;
        var size = 100;
        guiGraphics.pose().translate(x + (float) size / 2, y + (float) size / 2);
        guiGraphics.pose().rotate((float) Math.toRadians(CDRotation));
        guiGraphics.pose().translate(-x - (float) size / 2, -y - (float) size / 2);
        if(NetMusicListUtil.isGLY()){
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GLY091020, x, y, size, size, 0, 0, 256, 256, 256, 256);
        }
        else if(NetMusicListUtil.isN44()) {
            if(PLAYER_HAND == null){
                try {
                    var result = Minecraft.getInstance().getSkinManager().get(Minecraft.getInstance().getGameProfile()).get();
                    result.ifPresent(playerSkin -> PLAYER_HAND = playerSkin.body().texturePath());
                } catch (InterruptedException | ExecutionException _) {}
            }
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, PLAYER_HAND, x, y, size, size, 8, 8, 8, 8, 64, 64);
        }else{
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, x, y, 322, 0, size, size, 128, 128, 512, 256);
        }
        guiGraphics.pose().popMatrix();
    }

    public void renderPointer(@NotNull GuiGraphicsExtractor guiGraphics, float delta){
        if(musicList.size() == listWidget.getSelectedIndex()){
            if(pointerRotation < 45) {
                pointerRotation += 10f * delta;
                if(pointerRotation > 44){
                    pointerRotation = 45;
                }
            }
        }else{
            if(pointerRotation > 0) {
                pointerRotation -= 10f * delta;
                if(pointerRotation < 1){
                    pointerRotation = 0;
                }
            }
        }
        var x = left + 50;
        var y = top + 3;
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x + 55, y + 3);
        guiGraphics.pose().rotate((float) Math.toRadians(pointerRotation));
        guiGraphics.pose().translate(-x - 55, -y - 3);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, x, y, 0, 256 - 69, 64, 58, 69, 66, 512, 256);
        guiGraphics.pose().popMatrix();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        renderCD(graphics, a);
        renderPointer(graphics, a);
        for(MusicListEntry entry: listWidget.children()){
            if(entry.hovered)entry.renderTooltip(graphics, mouseX, mouseY);
        }
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
        private Component musicName;
        private final ItemMusicCD.SongInfo info;
        private boolean hovered = false;

        public MusicListEntry(ItemMusicCD.SongInfo info) {
            this.info = info;
            if(info.artists.isEmpty()){
                musicName = Component.literal(info.songName);
            } else {
                var a = new StringBuilder();
                for(String artist: info.artists){
                    a.append(artist);
                    a.append("、");
                }
                var t = Component.empty();
                if(info.vip){
                    t.append(Component.translatable("gui.net_music_list.vip").withStyle(ChatFormatting.RED));
                }
                if(info.readOnly){
                    t.append(Component.translatable("gui.net_music_list.read_only").withStyle(ChatFormatting.YELLOW));
                }
                var AT = a.toString();
                musicName = Component.literal(info.songName).append(t).append(" —— ").append(AT.substring(0, AT.length() - 1));
            }
        }

        @Override
        public @NotNull Component getNarration() {
            return musicName;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
            this.hovered = b;
            // 渲染背景
            if (hovered) {
                guiGraphicsExtractor.fill(getX(), getY(), getX() + getContentWidth() + 3, getY() + getContentHeight() + 3, 0x80FFFFFF);
            }

            // 渲染文本
            guiGraphicsExtractor.enableScissor(getX(), getY(), getX() + getContentWidth() - 10, getY() + getContentHeight() + 2);
            guiGraphicsExtractor.text(
                    font,
                    musicName,
                    getX() + 5,
                    getY() + (getContentHeight() - 10) / 2 + 3,
                    0xFFFFFFFF
            );
            guiGraphicsExtractor.disableScissor();
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            super.mouseClicked(event, doubleClick);
            MusicSelectionScreen.this.index = listWidget.children().indexOf(this);
            listWidget.setSelectedIndex(index);
            sendPackage();
            updateButton();
            return true;
        }

        public ItemMusicCD.SongInfo getInfo() {
            return info;
        }

        public void renderTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY){
            var pose = guiGraphics.pose();
            pose.pushMatrix();
            var tooltip = new ArrayList<Component>();
            ItemMusicCD.SongInfo info = getInfo();
            if (info != null) {
                var component1 = Component.literal(info.songName);
                if(info.vip)component1.append(Component.translatable("gui.net_music_list.vip").withStyle(ChatFormatting.RED));
                if(info.readOnly)component1.append(Component.translatable("gui.net_music_list.read_only").withStyle(ChatFormatting.YELLOW));
                tooltip.add(component1);

                if (StringUtils.isNoneBlank(info.transName)) {
                    String var10000 = I18n.get("tooltips.netmusic.cd.trans_name");
                    String text = "§a▍ §7" + var10000 + ": §6" + info.transName;
                    tooltip.add(Component.literal(text));
                }

                if (info.artists != null && !info.artists.isEmpty()) {
                    String artistNames = StringUtils.join(info.artists, " | ");
                    String var12 = I18n.get("tooltips.netmusic.cd.artists");
                    String text = "§a▍ §7" + var12 + ": §3" + artistNames;
                    tooltip.add(Component.literal(text));
                }

                String var13 = I18n.get("tooltips.netmusic.cd.time");
                String text = "§a▍ §7" + var13 + ": §5" + getSongTime(info.songTime);
                tooltip.add(Component.literal(text));
            } else {
                tooltip.add(Component.translatable("tooltips.netmusic.cd.empty").withStyle(ChatFormatting.RED));
            }
            List<ClientTooltipComponent> components =
                    tooltip.stream()
                            .map(c -> ClientTooltipComponent.create(c.getVisualOrderText()))
                            .toList();

//            guiGraphics.tooltip(
//                    font,
//                    components,
//                    mouseX,
//                    mouseY,
//                    DefaultTooltipPositioner.INSTANCE,
//                    null
//            );
            pose.popMatrix();
        }

        public void setMusicName(Component musicName) {
            this.musicName = musicName;
        }
    }

    private class AddMusicEntry extends MusicListEntry{
        public AddMusicEntry() {
            super(new ItemMusicCD.SongInfo("", "", 0, true));
            setMusicName(Component.translatable("gui.net_music_list.add"));
        }

        @Override
        public void renderTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {

        }
    }

    private String getSongTime(int songTime) {
        int min = songTime / 60;
        int sec = songTime % 60;
        String minStr = min <= 9 ? "0" + min : "" + min;
        String secStr = sec <= 9 ? "0" + sec : "" + sec;
        String format = Language.getInstance().getOrDefault("tooltips.netmusic.cd.time.format");
        return String.format(format, minStr, secStr);
    }

    public void sendPackage(){
        this.index = listWidget.getSelectedIndex();
        sendToServer(new MusicListDataPacket(index, this.playModeButton.playMode));
    }

    private class MusicListWidget extends ObjectSelectionList<MusicListEntry> {
        private static final Identifier BAR = Identifier.fromNamespaceAndPath(NetMusicList.ModID,
                "bar/bar");
        public MusicListWidget() {
            super(Minecraft.getInstance(), 195,
                    153, 12, 12);
            this.setX(left + 122);
            this.setY(top + 4);
        }

        @Override
        protected int scrollBarX() {
            return this.getX() + this.width - 4;
        }

        @Override
        public int scrollbarWidth() {
            return super.scrollbarWidth() - 1;
        }

        @Override
        public int getRowWidth() {
            return this.width - 16;
        }

        public void addMusicEntry(ItemMusicCD.SongInfo info) {
            this.addEntry(new MusicListEntry(info));
        }

        @Override
        public int addEntry(@NotNull MusicListEntry entry) {
            return super.addEntry(entry);
        }

        @Override
        protected void extractListBackground(GuiGraphicsExtractor graphics) {
        }

        @Override
        protected void extractListSeparators(GuiGraphicsExtractor graphics) {
        }

        @Override
        protected void extractScrollbar(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
            int scrollbarX = this.scrollBarX();
            int scrollerHeight = this.scrollerHeight();
            int scrollerY = this.scrollBarY();
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BAR, scrollbarX, scrollerY, this.scrollbarWidth(), scrollerHeight);
        }

        @Override
        protected void extractItem(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, MusicListEntry entry) {
            super.extractItem(graphics, mouseX, mouseY, a, entry);
            int l = this.getItemCount();
            for(int i1 = 0; i1 < l; ++i1) {
                int j1 = this.getRowTop(i1);
                int k1 = this.getRowBottom(i1);
                if (!(k1 >= this.getY() && j1 <= this.getBottom())) {
                    entry.hovered = false;
                }
            }
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
        if(NetMusicList.CONFIG.oldGUI){
            OldMusicSelectionScreen.open(musicList, mode, index);
            return;
        }
        Minecraft.getInstance().setScreen(new MusicSelectionScreen(musicList, mode, index));
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
            var x = 0;
            switch (this.playMode){
                case SEQUENTIAL -> x = 1;
                case RANDOM -> x = 26;
                case LOOP -> x = 51;
            }
            guiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, BUTTON_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight());
            guiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.getX(), this.getY(),
                    x, 162, this.width, this.height, 512, 256);
        }
    }

    public static boolean hasShiftDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 344);
    }

    public static class SettingButton extends Button{
        public static final Random random = new Random();
        protected SettingButton(int x, int y) {
            super(x, y, 22, 22, Component.literal("⚙"), button -> {
                if(hasShiftDown()){
                    NetMusicList.CONFIG.debug = !NetMusicList.CONFIG.debug;
                    NetMusicListUtil.reloadConfig();
                }else{
                    Minecraft.getInstance().setScreen(ConfigScreenGetter.getConfigScreen(Minecraft.getInstance().screen));
                }
            }, Button.DEFAULT_NARRATION);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {
            if(NetMusicList.CONFIG.debug){
                setTooltip(Tooltip.create(Component.translatable("text.net_music_list.debug_mode").withStyle(ChatFormatting.RED)));
            }else{
                setTooltip(null);
            }
            guiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, BUTTON_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight());
            if(NetMusicList.CONFIG.debug)setFGColor(16733525);
            var pose = guiGraphicsExtractor.pose();
            pose.pushMatrix();
            if(NetMusicList.CONFIG.debug && NetMusicListUtil.isGLY()){
                // GLY特有的突然发电
                pose.translate(random.nextFloat() * 3, random.nextFloat() * 3);
            }
            extractDefaultLabel(guiGraphicsExtractor.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
            pose.popMatrix();
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
            guiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, BUTTON_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight());
            guiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.getX(), this.getY(),
                    isUp ? 76 : 101, 162, this.width, this.height, 512, 256);
        }
    }
}