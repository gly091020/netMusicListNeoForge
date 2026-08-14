package com.gly091020.netMusicListNeoforge.media.netease;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.ExtraMusicList;
import com.github.tartaricacid.netmusic.api.lyric.LyricParser;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicList;
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicSong;
import com.github.tartaricacid.netmusic.config.MusicListManage;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.SetMusicIDMessage;
import com.gly091020.netMusicListNeoforge.media.NetMusicListMedias;
import com.gly091020.netMusicListNeoforge.api.musicSource.IExtraMusicSource;
import com.google.gson.Gson;
import com.mojang.blaze3d.platform.NativeImage;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gly091020.netMusicListNeoforge.NetMusicList.CONFIG;

public class NeteaseExtraMusicSource implements IExtraMusicSource {
    private static final Pattern ID_REG = Pattern.compile("https?://.*163.*song.*?(?!user)id=([0-9]+)");
    private static final Pattern PURE_ID_REG = Pattern.compile("^\\d{4,}$");
    private static final Gson GSON = new Gson();

    @Override
    public @NotNull String getType() {
        return "netease";
    }

    @Override
    public @Nullable ItemMusicCD.SongInfo parseMusic(String identifier, boolean readOnly) {
        Matcher matcher = ID_REG.matcher(identifier);
        if (matcher.matches()) {
            identifier = matcher.group(1);
        }
        if (!PURE_ID_REG.matcher(identifier).matches()) {
            return null;
        }
        long id = Long.parseLong(identifier);
        try {
            ItemMusicCD.SongInfo song = MusicListManage.get163Song(id);
            if (StringUtils.isBlank(song.songUrl) || StringUtils.isBlank(song.songName)) {
                return null;
            }
            song.readOnly = readOnly;
            NetworkHandler.sendToServer(new SetMusicIDMessage(song));
            return song;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            NetMusic.LOGGER.error("Failed to get song info for CD burner, song id: {}", id, e);
            return null;
        }
    }

    @Override
    public @Nullable NativeImage parseIcon(String identifier) {
        try {
            var song = GSON.fromJson(NetMusic.NET_EASE_WEB_API.song(Long.parseLong(identifier)),
                    ExtraNetEaseMusicSong.class).getSong();
            if (song == null) return null;
            return fetchImage(song.getPicUrl());
        } catch (Exception e) {
            NetMusicListMedias.LOGGER.error("获取封面失败：", e);
        }
        return null;
    }

    @Override
    public @Nullable LyricRecord parseLyric(String identifier) {
        try {
            String lyric = NetMusic.NET_EASE_WEB_API.lyric(Long.parseLong(identifier));
            return LyricParser.parseLyric(lyric, identifier);
        } catch (Exception e) {
            NetMusicListMedias.LOGGER.error("解析歌词失败：", e);
        }
        return null;
    }

    @Override
    public @NotNull List<ItemMusicCD.SongInfo> parsePlaylist(String identifier) {
        // 从网络音乐机里拿的代码
        var SONGS = new ArrayList<ItemMusicCD.SongInfo>();
        NetEaseMusicList pojo;
        try {
            pojo = GSON.fromJson(NetMusic.NET_EASE_WEB_API.list(Long.parseLong(identifier)), NetEaseMusicList.class);
            int count = pojo.getPlayList().getTracks().size();
            int size = Math.min(pojo.getPlayList().getTrackIds().size(), CONFIG.maxImportList);
            if (count < size) {
                // ids过多时会无法解析，这里分开解析
                long[] ids = new long[size - count];

                for (int i = count; i < size; ++i) {
                    ids[i - count] = pojo.getPlayList().getTrackIds().get(i).getId();
                }

                if (ids.length <= 100) {
                    String extraTrackInfo = NetMusic.NET_EASE_WEB_API.songs(ids);
                    ExtraMusicList extra = GSON.fromJson(extraTrackInfo, ExtraMusicList.class);
                    pojo.getPlayList().getTracks().addAll(extra.getTracks());
                } else {
                    int batchSize = 100;
                    for (int i = 0; i < ids.length; i += batchSize) {
                        int end = Math.min(i + batchSize, ids.length);
                        long[] batchIds = Arrays.copyOfRange(ids, i, end);
                        String extraTrackInfo = NetMusic.NET_EASE_WEB_API.songs(batchIds);
                        ExtraMusicList extra = GSON.fromJson(extraTrackInfo, ExtraMusicList.class);
                        pojo.getPlayList().getTracks().addAll(extra.getTracks());
                    }
                }
            }
            for (NetEaseMusicList.Track track : pojo.getPlayList().getTracks()) {
                SONGS.add(new ItemMusicCD.SongInfo(track));
            }
            return SONGS;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        } catch (Exception e) {
            NetMusicListMedias.LOGGER.error("解析播放列表失败：", e);
            return Collections.emptyList();
        }
    }

    @Override
    public @NotNull List<ItemMusicCD.SongInfo> search(String keywords) {
        try {
            var data = GSON.fromJson(NeteaseSearch.search(keywords), NetEaseMusicSearch.class);
            var SONGS = new ArrayList<ItemMusicCD.SongInfo>();
            for (NetEaseMusicSong song : data.getSongs()) {
                SONGS.add(new ItemMusicCD.SongInfo(song));
            }
            return SONGS;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        } catch (Exception e) {
            NetMusicListMedias.LOGGER.error("搜索失败：", e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean searchable() {
        return true;
    }

    @Override
    public String autoParseFromClipboard(String clipboardText) {
        var m = ID_REG.matcher(clipboardText);
        if (m.find())
            return m.group(1);
        return null;
    }

    @Nullable
    private static NativeImage fetchImage(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            byte[] bytes = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofByteArray())
                    .body();

            return NativeImage.read(new ByteArrayInputStream(bytes));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            NetMusicListMedias.LOGGER.error("获取图片失败: {}", url, e);
            return null;
        }
    }
}
