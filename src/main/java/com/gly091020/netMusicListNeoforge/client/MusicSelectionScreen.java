package com.gly091020.netMusicListNeoforge.client;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.config.ConfigScreenGetter;
import com.gly091020.netMusicListNeoforge.packet.DeleteMusicDataPacket;
import com.gly091020.netMusicListNeoforge.packet.MoveMusicDataPacket;
import com.gly091020.netMusicListNeoforge.packet.MusicListDataPacket;
import com.gly091020.netMusicListNeoforge.util.MUIUtil;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import com.gly091020.netMusicListNeoforge.util.PlayMode;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class MusicSelectionScreen extends Screen {
    private final List<ItemMusicCD.SongInfo> musicList;
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
            "textures/gui/bg.png");
    private static final ResourceLocation GLY091020 = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
            "textures/gui/gly091020.png");
    private static ResourceLocation PLAYER_HAND = null;
    private static final ResourceLocation BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "button/button");
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
                        button -> deleteMusic())
                .pos(left + 4 + 66 + 3, top + 133)
                .size(50, 22)){
            @Override
            protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blitSprite(BUTTON_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight());
                this.renderString(guiGraphics, font, 0xFFFFFFFF);
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

        lastScroll = (float) listWidget.getScrollAmount();
        nowSpeed = 0;
        if(musicList.size() == listWidget.getSelectedIndex()){
            pointerRotation = 45;
        }
        initIAM();
    }

    private boolean showIAM = ModList.get().isLoaded("modernui") &&
            !ModList.get().isLoaded("iammusicplayer") && !NetMusicList.CONFIG.showedIAM;
    private static final ResourceLocation IAM_ICON = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
            "textures/manual/iam/icon.png");
    private static final Component IAM_TEXT1 = Component.translatable("gui.net_music_list.iam.text1");
    private static final Component IAM_TEXT2 = Component.translatable("gui.net_music_list.iam.text2");

    private void initIAM(){
        if(!showIAM)return;
        var b1 = Button.builder(Component.translatable("gui.net_music_list.iam.yes"),
                        button -> {
                            showIAM = false;
                            NetMusicList.CONFIG.showedIAM = true;
                            NetMusicListUtil.reloadConfig();
                            button.visible = false;
                            if(ModList.get().isLoaded("modernui"))
                                MUIUtil.openIAMScreen();
                            NetMusicList.LOGGER.info("q(≧▽≦q)");
                        })
                .pos(left + backgroundWidth - 50 - 2 - 50 - 50, top - 5 - 2 - 20)
                .size(50, 20).build();
        addRenderableWidget(b1);
        var b2 = Button.builder(Component.translatable("gui.net_music_list.iam.no"),
                        button -> {
                            showIAM = false;
                            NetMusicList.CONFIG.showedIAM = true;
                            NetMusicListUtil.reloadConfig();
                            button.visible = false;
                            b1.visible = false;
                            NetMusicList.LOGGER.info("╥﹏╥...");
                        })
                .pos(left + backgroundWidth - 50 - 2 - 50, top - 5 - 2 - 20)
                .size(50, 20).build();
        addRenderableWidget(b2);
    }

    private void renderIAM(@NotNull GuiGraphics guiGraphics){
        if(!showIAM)return;
        guiGraphics.fill(left + 50, top - 60, left + backgroundWidth - 50, top - 5, 0xFFA1A1A1);
        guiGraphics.fill(left + 50 + 2, top - 60 + 2, left + backgroundWidth - 50 - 2, top - 5 - 2, 0xFFD2D2D2);
        guiGraphics.blit(IAM_ICON, left + 50 + 2, top - 60 + 2, 0, 0, 51, 51, 51, 51);
        guiGraphics.fill(left + 50 + 2 + 51, top - 60 + 2, left + 50 + 2 + 51 + 2, top - 5 - 2, 0xFFA1A1A1);

        guiGraphics.drawString(font, IAM_TEXT1, left + 50 + 2 + 51 + 5, top - 60 + 2 + 2, 0xFFFFFFFF);
        guiGraphics.drawString(font, IAM_TEXT2, left + 50 + 2 + 51 + 5, top - 60 + 2 + 2 + font.lineHeight + 2, 0xFFFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(BACKGROUND_TEXTURE, left, top, 0, 0, backgroundWidth, backgroundHeight, 512, 256);
        renderIAM(guiGraphics);
    }

    public void renderCD(@NotNull GuiGraphics guiGraphics, float delta){
        guiGraphics.pose().pushPose();
        float scrollSpeed = (float) Math.abs(listWidget.getScrollAmount() - lastScroll);
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
        if(Math.abs(lastScroll - listWidget.getScrollAmount()) <= 3){
            lastScroll = (float) listWidget.getScrollAmount();
        }else {
            if (lastScroll < listWidget.getScrollAmount()) {
                lastScroll += (float) (Math.abs(lastScroll - listWidget.getScrollAmount()) / 10);
            } else {
                lastScroll -= (float) (Math.abs(lastScroll - listWidget.getScrollAmount()) / 10);
            }
        }

        var x = left + 15;
        var y = top + 15;
        var size = 100;
        guiGraphics.pose().translate(x + (float) size / 2, y + (float) size / 2, 0);
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(CDRotation));
        guiGraphics.pose().translate(-x - (float) size / 2, -y - (float) size / 2, 0);
        if(NetMusicListUtil.isGLY()){
            guiGraphics.blit(GLY091020, x, y, size, size, 0, 0, 256, 256, 256, 256);
        }
        else if(NetMusicListUtil.isN44()) {
            if(PLAYER_HAND == null){
                PLAYER_HAND = Minecraft.getInstance().getSkinManager().getInsecureSkin(Minecraft.getInstance().getGameProfile()).texture();
            }
            guiGraphics.blit(PLAYER_HAND, x, y, size, size, 8, 8, 8, 8, 64, 64);
        }else{
            guiGraphics.blit(BACKGROUND_TEXTURE, x, y, size, size, 322, 0, 128, 128, 512, 256);
        }
        guiGraphics.pose().popPose();
    }

    public void renderPointer(@NotNull GuiGraphics guiGraphics, float delta){
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
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + 55, y + 3, 0);
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(pointerRotation));
        guiGraphics.pose().translate(-x - 55, -y - 3, 0);
        guiGraphics.blit(BACKGROUND_TEXTURE, x, y, 64, 58, 0, 256 - 69, 69, 66, 512, 256);
        guiGraphics.pose().popPose();
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderCD(context, delta);
        renderPointer(context, delta);
        for(MusicListEntry entry: listWidget.children()){
            if(entry.hovered)entry.renderTooltip(context, mouseX, mouseY);
        }
    }

    @Override
    public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
        if (p_96552_ == GLFW.GLFW_KEY_DELETE && canDelete()) {
            deleteMusic();
            return true;
        }
        if(p_96552_ == GLFW.GLFW_KEY_UP && canMove(true)){
            moveMusic(true);
            return true;
        }
        if(p_96552_ == GLFW.GLFW_KEY_DOWN && canMove(false)){
            moveMusic(false);
            return true;
        }
        if(p_96552_ == GLFW.GLFW_KEY_ESCAPE && super.keyPressed(p_96552_, p_96553_, p_96554_)){
            sendPackage();
            return true;
        }
        return super.keyPressed(p_96552_, p_96553_, p_96554_);
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
        PacketDistributor.sendToServer(payload);
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
        public void render(@NotNull GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.hovered = hovered;
            // 渲染背景
            if (hovered) {
                context.fill(x, y, x + entryWidth - 4, y + entryHeight, 0x80FFFFFF);
            }

            // 渲染文本
            context.enableScissor(x, y, x + entryWidth - 10, y + entryHeight);
            context.drawString(
                    font,
                    musicName,
                    x + 5,
                    y + (entryHeight - 10) / 2 + 1,
                    0xFFFFFF
            );
            context.disableScissor();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            super.mouseClicked(mouseX, mouseY, button);
            MusicSelectionScreen.this.index = listWidget.children().indexOf(this);
            listWidget.setSelectedIndex(index);
            sendPackage();
            updateButton();
            return true;
        }

        public ItemMusicCD.SongInfo getInfo() {
            return info;
        }

        public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY){
            var pose = guiGraphics.pose();
            pose.pushPose();
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
            guiGraphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
            pose.popPose();
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
        public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {

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
        public MusicListWidget() {
            super(Minecraft.getInstance(), 197,
                    153, 12, 12);
            this.setX(left + 122);
            this.setY(top + 4);
            this.setRenderHeader(false, 0);
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getX() + this.width - 5;
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
        protected void renderListBackground(@NotNull GuiGraphics guiGraphics) {

        }

        @Override
        protected void renderListSeparators(@NotNull GuiGraphics guiGraphics) {

        }

        @Override
        public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

            if (this.getMaxScroll() > 0) {
                int l = this.getScrollbarPosition();
                int i1 = (int) ((float) (this.height * this.height) / (float) this.getMaxPosition());
                i1 = Mth.clamp(i1, 32, this.height - 8);
                int k = (int) this.getScrollAmount() * (this.height - i1) / this.getMaxScroll() + this.getY();
                if (k < this.getY()) {
                    k = this.getY();
                }
                guiGraphics.blitSprite(ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
                        "bar/bar"), l - 1, k, 5, i1);
            }
        }

        @Override
        protected void renderListItems(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.renderListItems(guiGraphics, mouseX, mouseY, partialTick);
            int l = this.getItemCount();
            for(int i1 = 0; i1 < l; ++i1) {
                int j1 = this.getRowTop(i1);
                int k1 = this.getRowBottom(i1);
                if (!(k1 >= this.getY() && j1 <= this.getBottom()) && getEntry(i1) instanceof MusicListEntry entry) {
                    entry.hovered = false;
                }
            }
        }

        @Override
        protected boolean scrollbarVisible() {
            return false;
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
        protected void renderWidget(@NotNull GuiGraphics context, int p_282682_, int p_281714_, float p_282542_) {
            super.renderWidget(context, p_282682_, p_281714_, p_282542_);
            var x = 0;
            switch (this.playMode){
                case SEQUENTIAL -> x = 1;
                case RANDOM -> x = 26;
                case LOOP -> x = 51;
            }
            context.blitSprite(BUTTON_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight());
            context.blit(BACKGROUND_TEXTURE, this.getX(), this.getY(),
                    x, 162, this.width, this.height, 512, 256);
        }
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
        protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if(NetMusicList.CONFIG.debug){
                setTooltip(Tooltip.create(Component.translatable("text.net_music_list.debug_mode").withStyle(ChatFormatting.RED)));
            }else{
                setTooltip(null);
            }
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            // 其实这个方法绘制9切贴图比13个参数好多了
            guiGraphics.blitSprite(BUTTON_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }

        @Override
        public void renderString(@NotNull GuiGraphics guiGraphics, @NotNull Font font, int color) {
            if(NetMusicList.CONFIG.debug)color = 16733525;
            var pose = guiGraphics.pose();
            pose.pushPose();
            if(isHovered)
                pose.rotateAround(Axis.ZP.rotationDegrees((System.currentTimeMillis() % 36000) / 2.0f),
                        getX() + width / 2f + 0.25f, getY() + height / 2f + 0.25f, 0);
            if(NetMusicList.CONFIG.debug && NetMusicListUtil.isGLY()){
                // GLY特有的突然发电
                pose.translate(random.nextFloat() * 3, random.nextFloat() * 3, 30);
                pose.scale(1, 1, 5);
                pose.rotateAround(Axis.ZP.rotationDegrees((System.currentTimeMillis() % 36000) / 2.0f * random.nextFloat()),
                        getX() + width / 2f + 0.25f, getY() + height / 2f + 0.25f, 0);
                pose.rotateAround(Axis.XP.rotationDegrees((System.currentTimeMillis() % 36000) / 2.0f * random.nextFloat()),
                        getX() + width / 2f + 0.25f, getY() + height / 2f + 0.25f, 0);
                pose.rotateAround(Axis.YP.rotationDegrees((System.currentTimeMillis() % 36000) / 2.0f * random.nextFloat()),
                        getX() + width / 2f + 0.25f, getY() + height / 2f + 0.25f, 0);
            }
            super.renderString(guiGraphics, font, color);
            pose.popPose();
        }
    }

    public static class MoveButton extends Button{
        boolean isUp;
        protected MoveButton(int x, int y, OnPress onPress, boolean isUP) {
            super(x, y, 22, 22, Component.empty(), onPress, Button.DEFAULT_NARRATION);
            isUp = isUP;
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics context, int p_282682_, int p_281714_, float p_282542_) {
            super.renderWidget(context, p_282682_, p_281714_, p_282542_);
            context.blitSprite(BUTTON_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight());
            context.blit(BACKGROUND_TEXTURE, this.getX(), this.getY(),
                    isUp ? 76 : 101, 162, this.width, this.height, 512, 256);
        }
    }
}