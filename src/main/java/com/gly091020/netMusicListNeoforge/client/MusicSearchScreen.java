package com.gly091020.netMusicListNeoforge.client;

import com.github.tartaricacid.netmusic.client.gui.CDBurnerMenuScreen;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.api.musicSource.IExtraMusicSource;
import com.gly091020.netMusicListNeoforge.api.musicSource.MusicSource;
import com.gly091020.netMusicListNeoforge.mixin.accessor.CDBurnerMenuScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * 刻录界面的音乐搜索页：按关键词调用当前音乐源（{@link IExtraMusicSource#search}），
 * 点击结果直接刻录到唱片。
 */
public class MusicSearchScreen extends Screen {
    private static final int ROW_WIDTH = 570;
    private final IExtraMusicSource source;
    private final Screen parent;
    private EditBox keywordBox;
    private SearchResultList resultList;
    private final List<ItemMusicCD.SongInfo> results = new ArrayList<>();

    public MusicSearchScreen(Screen parent, IExtraMusicSource source) {
        super(Component.translatable("gui.net_music_list.search.title"));
        this.parent = parent;
        this.source = source;
    }

    @Override
    protected void init() {
        super.init();
        // 整行按 输入框:搜索:关闭 = 30% : 50% : 20% 分配
        int inputWidth = (int) (ROW_WIDTH * 0.3f);
        int searchWidth = (int) (ROW_WIDTH * 0.5f);
        int closeWidth = ROW_WIDTH - inputWidth - searchWidth;
        int left = this.width / 2 - ROW_WIDTH / 2;

        keywordBox = new EditBox(font, left, 30, inputWidth, 20, Component.literal(""));
        keywordBox.setMaxLength(64);
        keywordBox.setFocused(true);
        this.addRenderableWidget(keywordBox);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.net_music_list.search.button"), b -> search())
                .bounds(left + inputWidth, 30, searchWidth, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.net_music_list.close"), b -> onClose())
                .bounds(left + inputWidth + searchWidth, 30, closeWidth, 20).build());

        resultList = new SearchResultList(this.width, this.height - 88, 68, 24);
        this.addRenderableWidget(resultList);
    }

    private void search() {
        String keyword = keywordBox.getValue();
        if (keyword.isBlank()) return;
        new Thread(() -> {
            try {
                List<ItemMusicCD.SongInfo> list = source.search(keyword);
                Minecraft.getInstance().execute(() -> {
                    results.clear();
                    results.addAll(list);
                    resultList.refresh();
                });
            } catch (Exception e) {
                NetMusicList.LOGGER.error("搜索失败：", e);
            }
        }, "NetMusicSearch").start();
    }

    /** 把搜索结果填回刻录界面的输入框，由玩家自行决定是否刻录 */
    private void select(ItemMusicCD.SongInfo info) {
        if (parent instanceof CDBurnerMenuScreen) {
            String value = info.songUrl;
            var ms = MusicSource.tryPasteFromURI(URI.create(info.songUrl));
            if (ms != null && !ms.identifier().isEmpty()) {
                value = ms.identifier();
            }
            ((CDBurnerMenuScreenAccessor) parent).getTextField().setValue(value);
        }
        onClose();
    }

    @Override
    public void onClose() {
        if (parent != null) {
            Minecraft.getInstance().setScreen(parent);
        } else {
            super.onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(font, this.title, this.width / 2, 12, 0xFFFFFF);
        int left = this.width / 2 - ROW_WIDTH / 2;
        guiGraphics.drawString(font, Component.translatable("gui.net_music_list.search.source", source.getDisplayName()),
                left, 56, 0xA0A0A0);
    }

    private class SearchResultList extends ObjectSelectionList<SearchResultList.Entry> {
        public SearchResultList(int width, int height, int y, int itemHeight) {
            super(Minecraft.getInstance(), width, height, y, itemHeight);
            this.setX(0);
        }

        public void refresh() {
            this.clearEntries();
            for (ItemMusicCD.SongInfo info : results) {
                this.addEntry(new Entry(info));
            }
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getX() + this.width - 6;
        }

        @Override
        public int getRowWidth() {
            return this.width - 30;
        }

        private class Entry extends ObjectSelectionList.Entry<Entry> {
            private final ItemMusicCD.SongInfo info;

            public Entry(ItemMusicCD.SongInfo info) {
                this.info = info;
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.literal(info.songName);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
                guiGraphics.drawString(font, info.songName, left + 4, top + 2, 0xFFFFFF);
                String artists = info.artists == null ? "" : String.join(" / ", info.artists);
                guiGraphics.drawString(font, artists, left + 4, top + 2 + font.lineHeight, 0xA0A0A0);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                select(info);
                return true;
            }
        }
    }
}
