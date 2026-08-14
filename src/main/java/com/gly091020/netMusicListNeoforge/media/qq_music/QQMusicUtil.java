package com.gly091020.netMusicListNeoforge.media.qq_music;

import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.api.musicSource.MusicSource;
import com.gly091020.netMusicListNeoforge.media.qq_music.resource.QQMusicData;
import com.gly091020.netMusicListNeoforge.media.qq_music.resource.QQMusicURLData;
import com.gly091020.netMusicListNeoforge.media.qq_music.resource.QQPlaylistData;
import com.gly091020.netMusicListNeoforge.media.qq_music.resource.QQSearchData;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class QQMusicUtil {
    public static final String ID = "qq_music";
    /** mp3 品质候选前缀，从高到低；只请求 mp3，避免返回 flac/m4a */
    private static final String[] MP3_CANDIDATES = {"M800", "M500", "RS02"};
    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    public static final Gson GSON = new Gson();
    public static final URI MUSIC_U_URL = URI.create("https://u.y.qq.com/cgi-bin/musicu.fcg");
    public static final Duration TIMEOUT = Duration.ofSeconds(5);
    public static final Logger LOGGER = LogManager.getLogger(QQMusicUtil.class);

    public static String getCookie(){
        return NetMusicList.CONFIG.qqCookie;
    }

    @Nullable
    public static QQMusicData getSongData(long musicID){
        try {
            var data = buildSongInfoData(musicID);
            var request = HttpRequest.newBuilder(MUSIC_U_URL)
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(data)))
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Referer", "https://y.qq.com/")
                    .header("Content-Type", "application/json")
                    .header("Cookie", getCookie())
                    .timeout(TIMEOUT)
                    .build();
            var result = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if(result.statusCode() != 200)return null;
            return GSON.fromJson(result.body(), QQMusicData.class);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            return null;
        }catch (Exception e){
            LOGGER.debug("Error:", e);
        }
        return null;
    }

    @Nullable
    public static QQPlaylistData getPlaylistData(long listID){
        try {
            var data = buildPlaylistData(listID);
            var request = HttpRequest.newBuilder(MUSIC_U_URL)
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(data)))
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Referer", "https://y.qq.com/")
                    .header("Content-Type", "application/json")
                    .header("Cookie", getCookie())
                    .timeout(TIMEOUT)
                    .build();
            var result = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if(result.statusCode() != 200)return null;
            return GSON.fromJson(result.body(), QQPlaylistData.class);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            return null;
        }catch (Exception e){
            LOGGER.debug("Error:", e);
        }
        return null;
    }

    @Nullable
    public static String getMusicUrl(String musicMID){
        try {
            var data = buildSongURLData(musicMID);
            var request = HttpRequest.newBuilder(MUSIC_U_URL)
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(data)))
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Referer", "https://y.qq.com/")
                    .header("Content-Type", "application/json")
                    .header("Cookie", getCookie())
                    .timeout(TIMEOUT)
                    .build();
            var result = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if(result.statusCode() != 200)return null;
            var r = GSON.fromJson(result.body(), QQMusicURLData.class);
            return r.getUrl();
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            return null;
        }catch (Exception e){
            LOGGER.debug("Error:", e);
        }
        return null;
    }

    public static JsonElement buildRequestData(String moduleName, JsonElement module){
        var result = new JsonObject();
        var common = new JsonObject();
        common.addProperty("ct", 24);
        common.addProperty("cv", 4747474);
        common.addProperty("guid", String.valueOf(System.currentTimeMillis()));
        common.addProperty("format", "json");
        common.addProperty("inCharset", "utf-8");
        common.addProperty("outCharset", "utf-8");
        common.addProperty("notice", 0);
        common.addProperty("platform", "yqq.json");
        common.addProperty("needNewCode", 1);
        common.addProperty("uin", "0");
        result.add("comm", common);
        result.add(moduleName, module);
        return result;
    }

    public static JsonElement buildSearchData(String keyword) {
        var search = new JsonObject();
        search.addProperty("module", "music.search.SearchCgiService");
        search.addProperty("method", "DoSearchForQQMusicDesktop");

        var param = new JsonObject();
        param.addProperty("query", keyword);
        param.addProperty("search_type", 0);
        param.addProperty("page_num", 1);
        param.addProperty("num_per_page", 30);
        param.addProperty("remoteplace", "txt.yqq.top");

        search.add("param", param);

        return buildRequestData("req_0", search);
    }

    public static JsonElement buildSongInfoData(long musicID){
        var songInfo = new JsonObject();
        songInfo.addProperty("module", "music.pf_song_detail_svr");
        songInfo.addProperty("method", "get_song_detail");
        var param = new JsonObject();
        param.addProperty("song_id", musicID);
        songInfo.add("param", param);
        return buildRequestData("songinfo", songInfo);
    }

    public static JsonElement buildSongURLData(String musicMID){
        var req = new JsonObject();
        req.addProperty("module", "vkey.GetVkeyServer");
        req.addProperty("method", "CgiGetVkey");
        var param = new JsonObject();
        param.addProperty("guid", String.valueOf(System.currentTimeMillis()));
        var filename = new JsonArray();
        var mid = new JsonArray();
        var songType = new JsonArray();
        // todo:由于网络音乐机缺陷，限制MP3格式
        for (String prefix : MP3_CANDIDATES) {
            filename.add(prefix + musicMID + ".mp3");
            mid.add(musicMID);
            songType.add(0);
        }
        param.add("filename", filename);
        param.add("songmid", mid);
        param.add("songtype", songType);
        param.addProperty("uin", "0");
        param.addProperty("loginflag", 1);
        param.addProperty("platform", "20");
        req.add("param", param);
        return buildRequestData("req_0", req);
    }

    public static JsonElement buildPlaylistData(long listID) {
        var playlist = new JsonObject();
        playlist.addProperty("module", "music.srfDissInfo.aiDissInfo");
        playlist.addProperty("method", "uniform_get_Dissinfo");
        var param = new JsonObject();
        param.addProperty("disstid", listID);
        param.addProperty("userinfo", 1);
        param.addProperty("tag", 1);
        playlist.add("param", param);
        return buildRequestData("req_0", playlist);
    }

    public static LyricRecord getLyric(String id){
        long musicID;
        try{
            musicID = Long.parseLong(id.replace("qq_music:", ""));
        }catch (NumberFormatException e) {
            return null;
        }
        var data = getSongData(musicID);
        if(data == null)return null;
        var lyric = data.getLyric();
        if(lyric == null)return new LyricRecord(new Int2ObjectLinkedOpenHashMap<>());
        return lyric;
    }

    public static QQSearchData search(String keyword) {
        try {
            var data = buildSearchData(keyword);

            var request = HttpRequest.newBuilder(MUSIC_U_URL)
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(data)))
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Referer", "https://y.qq.com/")
                    .header("Content-Type", "application/json")
                    .header("Cookie", getCookie())
                    .timeout(TIMEOUT)
                    .build();

            var result = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            return GSON.fromJson(result.body(), QQSearchData.class);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            LOGGER.debug("Error:", e);
        }
        return null;
    }

    public static List<ItemMusicCD.SongInfo> searchMusicResult(String keyword){
        if(keyword.isEmpty())return List.of();
        var search = search(keyword);
        if(search == null)return List.of();
        return search.getSongs().stream().map(
                info -> new ItemMusicCD.SongInfo(
                        new MusicSource(ID, String.valueOf(info.id), info.length).toURI(),
                        info.name,
                        (int) info.length,
                        info.subtitle,
                        false,
                        false,
                        info.singers.stream().map(s -> s.name).toList()
                )
        ).toList();
    }
}
