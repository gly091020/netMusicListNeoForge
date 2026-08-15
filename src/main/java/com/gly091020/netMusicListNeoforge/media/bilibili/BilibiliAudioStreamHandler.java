package com.gly091020.netMusicListNeoforge.media.bilibili;

import com.github.tartaricacid.netmusic.client.api.IAudioStreamHandler;
import com.github.tartaricacid.netmusic.client.audio.ChunkedAudioStream;
import com.github.tartaricacid.netmusic.client.audio.MusicBufferedInputStream;
import com.github.tartaricacid.netmusic.util.Mp3Util;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;

public class BilibiliAudioStreamHandler implements IAudioStreamHandler {
    @Override
    public boolean canHandle(URL url) {
        String protocol = url.getProtocol();
        return ("http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol))
                && BiliBiliUtil.isBilibiliUrl(url);
    }

    @Override
    public AudioInputStream handle(URL url) throws UnsupportedAudioFileException, IOException {
        ChunkedAudioStream chunked = new ChunkedAudioStream(offset -> buildRequest(url, offset));
        MusicBufferedInputStream buffered = new MusicBufferedInputStream(chunked);
        Mp3Util.skipID3(buffered);
        return AudioSystem.getAudioInputStream(buffered);
    }

    private static HttpRequest buildRequest(URL url, long offset) {
        return HttpRequest.newBuilder(URI.create(url.toString()))
                .header("Range", "bytes=" + offset + "-")
                .header("Referer", "https://www.bilibili.com/")
                .header("User-Agent", BiliBiliUtil.USER_AGENT)
                .header("Origin", "https://www.bilibili.com")
                .GET()
                .build();
    }

    @Override
    public int getPriority() {
        return 10;
    }
}
