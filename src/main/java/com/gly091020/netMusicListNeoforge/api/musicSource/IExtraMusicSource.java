package com.gly091020.netMusicListNeoforge.api.musicSource;

import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * 歌曲额外信息源
 * <p>注意：此处的函数不需要异步处理</p>
 */
public interface IExtraMusicSource {
    /**
     * 获取类型
     *
     * @return 源类型
     */
    @NotNull
    String getType();

    /**
     * 源的显示名称，会在刻录界面显示
     *
     * @return 显示名称
     */
    @NotNull
    default Component getDisplayName() {
        return Component.translatable("text.netmusiclib.music_source." + getType());
    }

    /**
     * 刻录时的提示词，类似于 "请输入 ID"
     *
     * @return 提示词
     */
    @NotNull
    default Component getHint() {
        return Component.translatable("gui.netmusic.cd_burner.id.tips");
    }

    /**
     * 解析音乐，此步在刻录唱片时进行
     *
     * @param identifier 歌曲 ID（用户输入）
     * @param readOnly   是否只读
     * @return 音乐信息，注意返回{@code ItemMusicCD.SongInfo}中的 url 必须由{@code MusicSource}得到
     */
    @Nullable
    default ItemMusicCD.SongInfo parseMusic(String identifier, boolean readOnly) {
        return null;
    }

    /**
     * 解析封面
     *
     * @param identifier 歌曲 ID
     * @return 封面图片，解析失败或不能解析时返回 {@code null}
     */
    @Nullable
    default NativeImage parseIcon(String identifier) {
        return null;
    }

    /**
     * 解析歌词
     *
     * @param identifier 歌曲 ID
     * @return 歌词，解析失败或不能解析时返回 {@code null}
     */
    @Nullable
    default LyricRecord parseLyric(String identifier) {
        return null;
    }

    /**
     * 解析播放列表
     *
     * @param identifier 播放列表 ID
     * @return {@code ItemMusicCD.SongInfo}列表，解析失败或不能解析时返回空列表
     */
    @NotNull
    default List<ItemMusicCD.SongInfo> parsePlaylist(String identifier) {
        return Collections.emptyList();
    }

    /**
     * 搜索
     *
     * @param keywords 关键词
     * @return {@code ItemMusicCD.SongInfo}列表，搜索失败或不能搜索时返回空列表
     */
    @NotNull
    default List<ItemMusicCD.SongInfo> search(String keywords) {
        return Collections.emptyList();
    }

    /**
     * 是否可搜索
     *
     * @return 布尔值，如果不可搜索刻录界面中不会显示搜索按钮
     */
    default boolean searchable() {
        return false;
    }

    /**
     * 从剪贴板自动识别
     *
     * @param clipboardText 剪贴板文本
     * @return 歌曲 ID，如果无法识别返回{@code null}
     */
    @Nullable
    default String autoParseFromClipboard(String clipboardText) {
        return null;
    }
}
