package com.gly091020.netMusicListNeoforge.media;

import com.github.tartaricacid.netmusic.client.api.AudioStreamHandlerManager;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.api.musicParser.MusicParserManager;
import com.gly091020.netMusicListNeoforge.api.musicSource.ExtraMusicSourceManager;
import com.gly091020.netMusicListNeoforge.media.bilibili.BiliBiliUtil;
import com.gly091020.netMusicListNeoforge.media.bilibili.BilibiliAudioStreamHandler;
import com.gly091020.netMusicListNeoforge.media.bilibili.BilibiliExtraSource;
import com.gly091020.netMusicListNeoforge.media.bilibili.BilibiliParser;
import com.gly091020.netMusicListNeoforge.media.netease.NeteaseExtraMusicSource;
import com.gly091020.netMusicListNeoforge.media.qq_music.QQMusicExtraSource;
import com.gly091020.netMusicListNeoforge.media.qq_music.QQMusicParser;
import com.gly091020.netMusicListNeoforge.sounds.FFmpegAudioStream;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

public final class NetMusicListMedias {
    public static final Logger LOGGER = NetMusicList.LOGGER;

    public static final NeteaseExtraMusicSource NETEASE_EXTRA_MUSIC_SOURCE = new NeteaseExtraMusicSource();

    public static final QQMusicParser QQ_MUSIC_PARSER = new QQMusicParser();
    public static final QQMusicExtraSource QQ_MUSIC_EXTRA_SOURCE = new QQMusicExtraSource();

    public static final BilibiliParser BILIBILI_PARSER = new BilibiliParser();
    public static final BilibiliExtraSource BILIBILI_EXTRA_SOURCE = new BilibiliExtraSource();

    public static void init(){
        ExtraMusicSourceManager.registry(NetMusicListMedias.NETEASE_EXTRA_MUSIC_SOURCE);

        MusicParserManager.registry(QQ_MUSIC_PARSER);
        ExtraMusicSourceManager.registry(QQ_MUSIC_EXTRA_SOURCE);

        MusicParserManager.registry(BILIBILI_PARSER);
        ExtraMusicSourceManager.registry(BILIBILI_EXTRA_SOURCE);

        if (FMLEnvironment.dist.isClient()){
            FFmpegAudioStream.addArgCustomizer(BiliBiliUtil::ffmpegInputArgs);
            AudioStreamHandlerManager.registerHandler(new BilibiliAudioStreamHandler());
        }
    }
}
