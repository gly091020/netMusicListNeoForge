package com.gly091020.netMusicListNeoforge.util;

import com.github.tartaricacid.netmusic.client.gui.CDBurnerMenuScreen;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.SetMusicIDMessage;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.media.NetMusicListMedias;
import com.gly091020.netMusicListNeoforge.api.musicSource.ExtraMusicSourceManager;
import com.gly091020.netMusicListNeoforge.api.musicSource.IExtraMusicSource;
import com.gly091020.netMusicListNeoforge.mixin.accessor.AbstractContainerScreenAccessor;
import com.gly091020.netMusicListNeoforge.mixin.accessor.ScreenAccessor;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;

import java.util.regex.Pattern;

public class MSMixinFunctions {
    private static final Pattern LIST_ID_REG = Pattern.compile("^list/(\\d+)$");

    /** 刻录界面当前选中的音乐源，供歌单解析等逻辑使用 */
    public static IExtraMusicSource currentSource = NetMusicListMedias.NETEASE_EXTRA_MUSIC_SOURCE;

    public static boolean isPlaylistInput(String input) {
        return LIST_ID_REG.matcher(input).matches();
    }

    public static String getPlaylistId(String input) {
        var m = LIST_ID_REG.matcher(input);
        return m.find() ? m.group(1) : null;
    }

    /** 用新 API 解析播放列表并刻录，返回空 Component 表示成功 */
    public static Component tryCraftPlaylist(IExtraMusicSource musicSource, String identifier, boolean readOnly) {
        try {
            var songs = musicSource.parsePlaylist(identifier);
            if (songs.isEmpty()) return Component.translatable("gui.netmusic.cd_burner.get_info_error");
            for (ItemMusicCD.SongInfo info : songs) {
                info.readOnly = readOnly;
                if (NetMusicList.CONFIG.debug && NetMusicList.CONFIG.only5Second)
                    info.songTime = 5;
                NetworkHandler.sendToServer(new SetMusicIDMessage(info));
            }
        } catch (Exception e) {
            NetMusicListMedias.LOGGER.error("刻录歌单时遇到错误：", e);
            return Component.translatable("gui.netmusic.cd_burner.get_info_error");
        }
        return Component.empty();
    }

    public static CycleButton<IExtraMusicSource> addButton(CDBurnerMenuScreen self, CycleButton.OnValueChange<IExtraMusicSource> onValueChange) {
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) self;
        var button = CycleButton.<IExtraMusicSource>builder(IExtraMusicSource::getDisplayName)
                .withInitialValue(NetMusicListMedias.NETEASE_EXTRA_MUSIC_SOURCE)
                .withValues(ExtraMusicSourceManager.getAllParser())
                .create(accessor.getLeftPos() + 7, accessor.getTopPos() + 70, 88, 20,
                        Component.translatable("text.netmusiclib.musicSource"), onValueChange);
        return ((ScreenAccessor) self).invokeAddRenderableWidget(button);
    }

    public static Component tryCraftMusicCD(IExtraMusicSource musicSource, String input, boolean reedOnly) {
        try {
            var r = musicSource.parseMusic(input, reedOnly);
            if (r != null) {
                NetworkHandler.sendToServer(new SetMusicIDMessage(r));
            } else return Component.translatable("gui.netmusic.cd_burner.get_info_error");
        } catch (Exception e) {
            NetMusicListMedias.LOGGER.error("刻录时遇到错误：", e);
            return Component.translatable("gui.netmusic.cd_burner.get_info_error");
        }
        return Component.empty();
    }
}
