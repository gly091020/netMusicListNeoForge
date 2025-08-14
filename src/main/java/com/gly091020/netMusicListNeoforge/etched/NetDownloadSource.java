package com.gly091020.netMusicListNeoforge.etched;

import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.google.gson.JsonParseException;
import gg.moonflower.etched.api.record.TrackData;
import gg.moonflower.etched.api.sound.download.SoundDownloadSource;
import gg.moonflower.etched.api.util.DownloadProgressListener;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

public class NetDownloadSource implements SoundDownloadSource {
    private static final Pattern PATTERN = Pattern.compile("https://.*163.*[^user]id=([0-9]+).*");

    @Override
    @SuppressWarnings("deprecation")
    public Collection<URL> resolveUrl(String s, @Nullable DownloadProgressListener downloadProgressListener, Proxy proxy) throws IOException, URISyntaxException, JsonParseException {
        return Collections.singleton(resolveRedirect(new URL(s), 5, HashMap.newHashMap(1)));
    }

    public static URL resolveRedirect(URL originalUrl, int maxRedirects, Map<String, String> headers) throws IOException {
        // md写晕了让deepseek写的
        URL currentUrl = originalUrl;
        HttpURLConnection connection;

        while (maxRedirects-- > 0) {
            connection = (HttpURLConnection) currentUrl.openConnection();
            connection.setInstanceFollowRedirects(false); // 手动处理重定向

            // 设置请求头
            if (headers != null) {
                headers.forEach(connection::setRequestProperty);
            }

            int responseCode = connection.getResponseCode();

            // 如果是 200，直接返回当前 URL
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return currentUrl;
            }

            // 处理 302/301/307/308 等重定向
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                    responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == HttpURLConnection.HTTP_SEE_OTHER ||
                    responseCode == 307 || responseCode == 308) {

                String location = connection.getHeaderField("Location");
                connection.disconnect(); // 关闭当前连接

                if (location == null) {
                    throw new IOException("Redirect with no Location header: " + responseCode);
                }

                // 处理相对路径的 Location
                currentUrl = new URL(currentUrl, location);
            } else {
                throw new IOException("Unexpected HTTP status: " + responseCode);
            }
        }

        throw new IOException("Too many redirects (max: " + maxRedirects + ")");
    }

    @Override
    public Collection<TrackData> resolveTracks(String s, @Nullable DownloadProgressListener downloadProgressListener, Proxy proxy) throws IOException, URISyntaxException, JsonParseException {
        try {
            String[] parts = s.split("[?&]id=");  // 为 什 么 要 用 这 种 代 码
            String idPart;
            if (parts.length > 1) {
                idPart = parts[1].split("&")[0];
            } else {
                throw new URISyntaxException(s, "解析失败");
            }
            var info = MusicListManage.get163Song(Long.parseLong(idPart));
            var artist = new StringBuilder();
            for (String a : info.artists) {
                artist.append(a);
                artist.append("、");
            }
            var as = artist.toString();
            return List.of(new TrackData(info.songUrl, as.substring(0, as.length() - 1), Component.literal(info.songName)));
        } catch (Exception e) {
            throw new URISyntaxException(s, "解析失败");
        }
    }

    @Override
    public Optional<String> resolveAlbumCover(String s, @Nullable DownloadProgressListener downloadProgressListener, Proxy proxy, ResourceManager resourceManager) throws IOException, URISyntaxException, JsonParseException {
        return Optional.empty();
    }

    @Override
    public boolean isValidUrl(String s) {
        return PATTERN.matcher(s).find();
    }

    @Override
    public boolean isTemporary(String s) {
        return true;
    }

    @Override
    public String getApiName() {
        return "netease";
    }
}
