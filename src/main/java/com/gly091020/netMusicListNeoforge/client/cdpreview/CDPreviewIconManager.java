package com.gly091020.netMusicListNeoforge.client.cdpreview;

import com.github.tartaricacid.netmusic.NetMusic;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.api.musicSource.ExtraMusicSourceManager;
import com.gly091020.netMusicListNeoforge.api.musicSource.MusicSource;
import com.gly091020.netMusicListNeoforge.util.CacheManager;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 唱片封面缓存：后台线程调用 {@code IExtraMusicSource.parseIcon} 下载封面，
 * 渲染线程把 NativeImage 注册成 DynamicTexture。
 */
@OnlyIn(Dist.CLIENT)
public final class CDPreviewIconManager {
    private static final Map<String, ResourceLocation> ICONS = new HashMap<>();
    private static final List<Pair<String, MusicSource>> WAITING = new ArrayList<>();
    private static final List<Pair<String, NativeImage>> PENDING = new ArrayList<>();
    private static final Set<String> DOWNLOADING = new HashSet<>();
    private static int timer = 0;

    private CDPreviewIconManager() {
    }

    public static void requestIcon(String key, MusicSource source) {
        // 先查本地缓存，命中就直接进注册队列，不再下载
        Path cached = CacheManager.getImageCache(source);
        if (cached != null) {
            boolean alreadyHave = false;
            boolean queued = false;
            synchronized (PENDING) {
                if (ICONS.containsKey(key) || PENDING.stream().anyMatch(p -> p.getKey().equals(key))) {
                    alreadyHave = true;
                } else {
                    try (InputStream in = Files.newInputStream(cached)) {
                        PENDING.add(Pair.of(key, NativeImage.read(in)));
                        queued = true;
                    } catch (Exception e) {
                        NetMusicList.LOGGER.debug("CD 预览缓存封面读取失败，走下载：{}", key, e);
                    }
                }
            }
            if (alreadyHave || queued) return;
            // 读取失败则继续走下载
        }
        synchronized (WAITING) {
            if (ICONS.containsKey(key) || DOWNLOADING.contains(key)) return;
            for (Pair<String, MusicSource> p : WAITING) {
                if (p.getKey().equals(key)) return;
            }
            WAITING.add(Pair.of(key, source));
        }
    }

    public static ResourceLocation getIcon(String key) {
        return ICONS.get(key);
    }

    /** 客户端 tick：攒一批后起后台线程下载，下载完的封面排队等渲染线程注册 */
    public static void tick() {
        timer++;
        List<Pair<String, MusicSource>> batch;
        synchronized (WAITING) {
            if (timer >= 10 && !WAITING.isEmpty()) {
                timer = 0;
                batch = new ArrayList<>(WAITING);
                WAITING.clear();
                batch.forEach(p -> DOWNLOADING.add(p.getKey()));
            } else {
                batch = null;
            }
        }
        if (batch != null) {
            Thread thread = new Thread(() -> download(batch), "CD-Preview-Icon");
            thread.setDaemon(true);
            thread.start();
        }
        processPending();
    }

    private static void download(List<Pair<String, MusicSource>> batch) {
        try {
            Map<String, MusicSource> sourceByKey = new HashMap<>();
            batch.forEach(p -> sourceByKey.put(p.getKey(), p.getValue()));
            // 网易云批量获取封面，减少单曲请求次数
            List<Pair<String, Long>> netease = batch.stream()
                    .filter(p -> "netease".equals(p.getValue().loaderType()))
                    .map(p -> {
                        try {
                            return Pair.of(p.getKey(), Long.parseLong(p.getValue().identifier()));
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    })
                    .filter(p -> p != null)
                    .toList();
            if (!netease.isEmpty()) {
                fetchNeteaseIcons(netease, sourceByKey);
            }
            for (Pair<String, MusicSource> p : batch) {
                if ("netease".equals(p.getValue().loaderType())) continue;
                try {
                    var parser = ExtraMusicSourceManager.getParser(p.getValue());
                    if (parser == null) continue;
                    NativeImage image = parser.parseIcon(p.getValue().identifier());
                    if (image != null) {
                        addToPending(p.getKey(), p.getValue(), image);
                    }
                } catch (Exception e) {
                    NetMusicList.LOGGER.debug("CD 预览封面加载失败：{}", p.getKey(), e);
                }
            }
        } finally {
            synchronized (WAITING) {
                batch.forEach(p -> DOWNLOADING.remove(p.getKey()));
            }
        }
    }

    /** 批量请求网易云歌曲信息，解析出封面 URL 后逐张下载；失败时回退单曲 parseIcon */
    private static void fetchNeteaseIcons(List<Pair<String, Long>> ids, Map<String, MusicSource> sourceByKey) {
        try {
            String json = NetMusic.NET_EASE_WEB_API.songs(ids.stream().mapToLong(Pair::getValue).toArray());
            SongsIcon data = NetMusicListUtil.GSON.fromJson(json, SongsIcon.class);
            Map<Long, String> urlById = new HashMap<>();
            if (data != null && data.songs() != null) {
                for (SongsIcon.Song song : data.songs()) {
                    if (song.album() != null && song.album().picUrl() != null) {
                        urlById.put(song.id(), song.album().picUrl());
                    }
                }
            }
            for (Pair<String, Long> p : ids) {
                String picUrl = urlById.get(p.getValue());
                if (picUrl == null) continue;
                try {
                    NativeImage image = NetMusicListUtil.getNativeImageFromURL(URI.create(picUrl).toURL());
                    if (image != null) {
                        addToPending(p.getKey(), sourceByKey.get(p.getKey()), image);
                    }
                } catch (Exception e) {
                    NetMusicList.LOGGER.debug("CD 预览网易云封面下载失败：{}", p.getKey(), e);
                }
            }
        } catch (Exception e) {
            // 批量接口失败时逐个回退到单曲接口
            NetMusicList.LOGGER.debug("CD 预览网易云批量封面失败，回退单曲：", e);
            for (Pair<String, Long> p : ids) {
                try {
                    var parser = ExtraMusicSourceManager.getParser(new MusicSource("netease", String.valueOf(p.getValue()), 0));
                    if (parser == null) continue;
                    NativeImage image = parser.parseIcon(String.valueOf(p.getValue()));
                    if (image != null) {
                        addToPending(p.getKey(), sourceByKey.get(p.getKey()), image);
                    }
                } catch (Exception ex) {
                    NetMusicList.LOGGER.debug("CD 预览网易云封面回退失败：{}", p.getKey(), ex);
                }
            }
        }
    }

    private static void addToPending(String key, MusicSource source, NativeImage image) {
        saveToCache(source, image);
        synchronized (PENDING) {
            PENDING.add(Pair.of(key, image));
        }
    }

    /** 把下载到的封面写入本地缓存并更新索引，下次直接读缓存 */
    private static void saveToCache(MusicSource source, NativeImage image) {
        if (source == null || !NetMusicList.CONFIG.enableCache) return;
        try {
            Files.createDirectories(CacheManager.PATH);
            String uuid = CacheManager.getCacheUUID(source);
            if (uuid == null) uuid = UUID.randomUUID().toString();
            image.writeToFile(CacheManager.PATH.resolve(uuid + ".png"));
            CacheManager.addImageCache(source, uuid);
        } catch (Exception e) {
            NetMusicList.LOGGER.debug("CD 预览封面写缓存失败：{}", source, e);
        }
    }

    /** 必须在渲染线程调用：把下载好的 NativeImage 注册进 TextureManager */
    public static void processPending() {
        List<Pair<String, NativeImage>> toProcess;
        synchronized (PENDING) {
            if (PENDING.isEmpty()) return;
            toProcess = new ArrayList<>(PENDING);
            PENDING.clear();
        }
        for (Pair<String, NativeImage> p : toProcess) {
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(
                    NetMusicList.ModID, "cd_preview/" + UUID.randomUUID());
            ResourceLocation old = ICONS.get(p.getKey());
            if (old != null) {
                Minecraft.getInstance().getTextureManager().release(old);
            }
            Minecraft.getInstance().getTextureManager().register(loc, new DynamicTexture(p.getValue()));
            ICONS.put(p.getKey(), loc);
            NetMusicList.LOGGER.debug("CD 预览封面已加载：{}", p.getKey());
        }
    }

    /** 网易云 songs 接口的响应结构，只取需要的字段 */
    public record SongsIcon(List<Song> songs) {
        public record Song(long id, Album album) {
            public record Album(String picUrl) {
            }
        }
    }
}
