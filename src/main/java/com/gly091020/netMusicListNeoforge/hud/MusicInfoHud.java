package com.gly091020.netMusicListNeoforge.hud;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.api.musicSource.ExtraMusicSourceManager;
import com.gly091020.netMusicListNeoforge.api.musicSource.MusicSource;
import com.gly091020.netMusicListNeoforge.util.CacheManager;
import com.gly091020.netMusicListNeoforge.util.MusicManager;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class MusicInfoHud{
    private static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
            "textures/gui/default.png");

    private static ItemMusicCD.SongInfo info;
    private volatile static ResourceLocation icon;
    @Nullable
    private static Long id = null;
    private static NetMusicListUtil.Lyric lyric;
    private static int left = 10;
    private static int top = 10;
    private static String rawUrl;

    private static Thread thread;

    @Nullable
    private static MusicSource musicSource;
    /** 歌曲切换代际：防止旧的后台任务把已释放的纹理写回 icon */
    private static int generation = 0;

    public static void render(@NotNull GuiGraphics guiGraphics) {
        if(!NetMusicList.CONFIG.musicHUD)return;
        if(NetMusicListUtil.globalStopMusic)return;
        if(Minecraft.getInstance().options.hideGui){return;}
        if(info == null){
            var sounds = MusicManager.getSelfSounds();
            if(!sounds.isEmpty()){
                var sound = sounds.getFirst();
                setInfo(sound.getInfo(), sound.getSlot(), sound.getRawUrl(), sound.getRingerId());
                return;
            }
            return;
        }

        var font = Minecraft.getInstance().font;
        guiGraphics.blit(Objects.requireNonNullElse(icon, DEFAULT_TEXTURE), left, top, 0, 0, 40, 40, 40, 40);
        var text = "";
        if(info.transName.isEmpty()){
            text = info.songName;
        }else{
            text = String.format("%s(%s)", info.songName, info.transName);
        }
        guiGraphics.drawString(font, text, left + 50, top, 0xFFFFFFFF);

        var count = info.songTime;
        var tickWidth = 100;

        var sounds = MusicManager.getSelfSounds();
        if(sounds.isEmpty()){
            clearInfo();
            return;
        }
        var tick = sounds.getLast().getTick();
        guiGraphics.fill(left + 50, top + font.lineHeight + 4, left + 50 + tickWidth, top + font.lineHeight + 6, 0xFFAAAAAA);
        guiGraphics.fill(left + 50, top + font.lineHeight + 4, (int) (left + 50 + tickWidth * clamp((count - tick / 20f) / count, 0, 1)), top + font.lineHeight + 6, 0xFFFFFFFF);
        if(id != null && CacheManager.getDownloadProgress(id) > 0){
            guiGraphics.fill(left + 50, top + font.lineHeight + 4, (int) (left + 50 + tickWidth * clamp(CacheManager.getDownloadProgress(id), 0, 1)), top + font.lineHeight + 6, 0xFF00FF00);
        }
        guiGraphics.drawString(font, String.format("%s/%s", NetMusicListUtil.secondsToMinutesSeconds((int) (count - (tick / 20f))), NetMusicListUtil.secondsToMinutesSeconds(count)), left + 50 + tickWidth + 5, top + font.lineHeight + 1, 0xFFFFFFFF);

        if(lyric != null) {
            var lyricPart = lyric.getLyric(clamp(count - tick / 20f, 0, Float.MAX_VALUE));
            guiGraphics.drawString(font, lyricPart.getA(), left + 50, top + (font.lineHeight * 2 + 1), 0xFFFFFFFF);
            if (lyricPart.getB() != null && !lyricPart.getB().isEmpty()) {
                guiGraphics.drawString(font, lyricPart.getB(), left + 50, top + (font.lineHeight + 1) * 3, 0xFFFFFFFF);
            }
        }
    }

    public static float clamp(float value, float min, float max) {
        return Math.clamp(value, min, max);
    }

    public static ItemMusicCD.SongInfo getInfo() {
        return info;
    }

    public static void setPos(int x, int y){
        left = x;
        top = y;
    }

    public static void setInfo(@Nullable ItemMusicCD.SongInfo info, int slot, @NotNull String rawUrl, @Nullable UUID ringerId){
        if(info == null){
            MusicInfoHud.info = null;
            return;
        }
        id = null;
        MusicInfoHud.info = info;
        final int gen = ++generation;
        if(thread != null){
            thread.interrupt();
            thread = null;
        }
        if(icon != null){
            Minecraft.getInstance().getTextureManager().release(icon);
            icon = null;
        }
        lyric = null;
        MusicInfoHud.rawUrl = rawUrl;
        musicSource = MusicSource.tryPasteFromSongInfo(info);
        getData(gen);
        if(musicSource == null){
            try {
                var id = NetMusicListUtil.getIdFromUrl(rawUrl);
                if(!CacheManager.hasCache(id)){
                    var uuid = UUID.randomUUID().toString();
                    CacheManager.startImgDownload(id, uuid);
                    CacheManager.startSongDownload(id, uuid);
                    CacheManager.startLycDownload(id, uuid);
                }
            } catch (IllegalAccessException ignored) {

            }
        }else {
            if(!CacheManager.hasCache(musicSource)){
                var uuid = UUID.randomUUID().toString();
                CacheManager.startImgDownload(musicSource, uuid);
                CacheManager.startSongDownload(musicSource, uuid);
                CacheManager.startLycDownload(musicSource, uuid);
            }
        }
    }

    public static void clearInfo(){
        info = null;
        rawUrl = null;
    }

    public static void getData(){
        getData(generation);
    }

    private static void getData(int gen){
        if(info != null){
            try {
                Path imagePath;
                if(musicSource == null) {
                    var id = NetMusicListUtil.getIdFromUrl(rawUrl);
                    if (CacheManager.hasCache(id)) {
                        lyric = CacheManager.getLycCache(id);
                        imagePath = CacheManager.getImageCache(id);
                    } else {
                        imagePath = null;
                        try {
                            getTextureFromLocal(info);
                            id = NetMusicListUtil.getIdFromUrl(rawUrl);
                            long finalId = id;
                            thread = new Thread(() -> getDataByThread(gen, finalId));
                            thread.start();
                        } catch (Exception e) {
                            NetMusicList.LOGGER.error("解析出现错误", e);
                        }
                    }
                }else {
                    if (CacheManager.hasCache(musicSource)) {
                        lyric = CacheManager.getLycCache(musicSource);
                        imagePath = CacheManager.getImageCache(musicSource);
                    } else {
                        imagePath = null;
                        thread = new Thread(() -> getDataByThread(gen, 0)); // 自己会处理
                        thread.start();
                    }
                }
                if(imagePath == null){
                    icon = DEFAULT_TEXTURE;
                }else{
                    Minecraft.getInstance().execute(() -> {
                        if (gen != generation) return;
                        var resourceLocation = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
                                String.format("icon_%s", UUID.randomUUID()));
                        try {
                            Minecraft.getInstance().getTextureManager().register(resourceLocation,
                                    NetMusicListUtil.getTextureFromPath(imagePath));
                        } catch (IOException ignored) {}
                        icon = resourceLocation;
                    });
                    return;
                }
            } catch (IllegalAccessException ignored) {}
        }
    }

    private static void getDataByThread(int gen, long id){
        try {
            var ms = musicSource;
            if(ms == null){
                var icon_url = NetMusicListUtil.getIconUrl(NetMusic.NET_EASE_WEB_API.song(id));
                var resourceLocation = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
                        String.format("icon_%s", UUID.randomUUID()));
                var texture = NetMusicListUtil.getTextureFromURL(icon_url);
                Minecraft.getInstance().execute(() -> {
                    if (gen != generation) {
                        texture.close();
                        return;
                    }
                    Minecraft.getInstance().getTextureManager().register(resourceLocation, texture);
                    icon = resourceLocation;
                });
                var l = NetMusicListUtil.getLyric(NetMusic.NET_EASE_WEB_API.lyric(id));
                if (gen == generation) lyric = l;
            }else{
                var p = ExtraMusicSourceManager.getParser(ms);
                if(p == null)return;
                var iconNative = p.parseIcon(ms.identifier());
                if (Thread.currentThread().isInterrupted()) return;
                if(iconNative != null) {
                    var resourceLocation = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
                            String.format("icon_%s", UUID.randomUUID()));
                    Minecraft.getInstance().execute(() -> {
                        if (gen != generation) {
                            iconNative.close();
                            return;
                        }
                        Minecraft.getInstance().getTextureManager().register(resourceLocation,
                                new DynamicTexture(iconNative));
                        icon = resourceLocation;
                    });
                }else if (gen == generation) icon = DEFAULT_TEXTURE;
                var l = p.parseLyric(ms.identifier());
                if (gen == generation && l != null) lyric = NetMusicListUtil.Lyric.fromLyricRecord(l);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            if (Thread.currentThread().isInterrupted()) return;
            NetMusicList.LOGGER.error("解析出现错误", e);
        }
    }

    private static void getTextureFromLocal(ItemMusicCD.SongInfo info){
        var musicPath = convertFileURLToPath(info.songUrl);
        if(musicPath != null){
            var imagePath = getPicturePath(musicPath);
            if(imagePath == null)return;
            var resourceLocation = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
                    String.format("icon_%s", UUID.randomUUID().toString().toLowerCase()));
            try {
                Minecraft.getInstance().getTextureManager().register(resourceLocation,
                        NetMusicListUtil.getTextureFromPath(imagePath));
                icon = resourceLocation;
            } catch (IOException ignored) {
            }
        }
    }

    private static Path getPicturePath(Path musicPath){
        var p1 = musicPath.getParent().resolve(musicPath.getFileName().toFile().getName() + ".png");
        if(p1.toFile().isFile()){return p1;}
        p1 = musicPath.getParent().resolve(musicPath.getFileName().toFile().getName() + ".jpg");
        if(p1.toFile().isFile()){return p1;}
        p1 = musicPath.getParent().resolve(musicPath.getFileName().toFile().getName() + ".jpeg");
        if(p1.toFile().isFile()){return p1;}
        return null;
    }

    public static Path convertFileURLToPath(String urlString) {
        try {
            URL url = new URL(urlString);
            if (!"file".equals(url.getProtocol())) {
                return null;
            }

            // 使用 URI 转换更安全，可以正确处理特殊字符
            URI uri = url.toURI();
            return Paths.get(uri);

        } catch (Exception e) {
            return null;
        }
    }
}
