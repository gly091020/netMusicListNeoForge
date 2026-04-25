package com.gly091020.netMusicListNeoforge.hud;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.util.CacheManager;
import com.gly091020.netMusicListNeoforge.util.MusicManager;
import com.gly091020.netMusicListNeoforge.util.NetMusicListClientUtil;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
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
    private static final Identifier DEFAULT_TEXTURE = Identifier.fromNamespaceAndPath(NetMusicList.ModID,
            "textures/gui/default.png");

    private static ItemMusicCD.SongInfo info;
    private volatile static Identifier icon;
    @Nullable
    private static Long id = null;
    private static NetMusicListUtil.Lyric lyric;
    private static ItemStack stack;
    private static int slot;
    private static int left = 10;
    private static int top = 10;

    private static volatile NativeImage texture;

    private static Thread thread;

    public static void render(@NotNull GuiGraphicsExtractor guiGraphics) {
        if(!NetMusicList.CONFIG.musicHUD)return;
        if(NetMusicListUtil.globalStopMusic)return;
        if(Minecraft.getInstance().options.hideGui){return;}
        if(info == null){
            var sounds = MusicManager.getSelfSounds();
            if(!sounds.isEmpty()){
                var sound = sounds.getFirst();
                setInfo(sound.getInfo(), sound.getPlayer().getInventory().getItem(sound.getSlot()), sound.getSlot());
                return;
            }
            return;
        }

        var font = Minecraft.getInstance().font;
        if(texture != null) {
            icon = Identifier.fromNamespaceAndPath(NetMusicList.ModID,
                    String.format("icon_%s", UUID.randomUUID()));
            Minecraft.getInstance().getTextureManager().register(icon,
                    NetMusicListClientUtil.createTextureFromImg(texture));
            texture = null;
        }
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Objects.requireNonNullElse(icon, DEFAULT_TEXTURE), left, top, 0, 0, 40, 40, 40, 40);
        var text = "";
        if(info.transName.isEmpty()){
            text = info.songName;
        }else{
            text = String.format("%s(%s)", info.songName, info.transName);
        }
        guiGraphics.text(font, text, left + 50, top, 0xFFFFFFFF);

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
        guiGraphics.text(font, String.format("%s/%s", NetMusicListUtil.secondsToMinutesSeconds((int) (count - (tick / 20f))), NetMusicListUtil.secondsToMinutesSeconds(count)), left + 50 + tickWidth + 5, top + font.lineHeight + 1, 0xFFFFFFFF);

        if(lyric != null) {
            var lyricPart = lyric.getLyric(clamp(count - tick / 20f, 0, Float.MAX_VALUE));
            guiGraphics.text(font, lyricPart.getA(), left + 50, top + (font.lineHeight * 2 + 1), 0xFFFFFFFF);
            if (lyricPart.getB() != null && !lyricPart.getB().isEmpty()) {
                guiGraphics.text(font, lyricPart.getB(), left + 50, top + (font.lineHeight + 1) * 3, 0xFFFFFFFF);
            }
        }
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static ItemMusicCD.SongInfo getInfo() {
        return info;
    }

    public static void setPos(int x, int y){
        left = x;
        top = y;
    }

    public static void setInfo(@Nullable ItemMusicCD.SongInfo info, @NotNull ItemStack playerStack, int slot){
        if(info == null){
            MusicInfoHud.info = null;
            return;
        }
        id = null;
        MusicInfoHud.info = info;
        MusicInfoHud.slot = slot;
        if(thread != null){
            thread.interrupt();
            thread = null;
        }
        if(icon != null){
            Minecraft.getInstance().getTextureManager().release(icon);
            icon = null;
        }
        lyric = null;
        stack = playerStack;
        getData();
        try {
            var id = NetMusicListClientUtil.getIdFromInfo(info);
            if(!CacheManager.hasCache(id)){
                var uuid = UUID.randomUUID().toString();
                CacheManager.startImgDownload(id, uuid);
                CacheManager.startSongDownload(id, uuid);
                CacheManager.startLycDownload(id, uuid);
            }
        } catch (IllegalAccessException ignored) {

        }
    }

    public static void clearInfo(){
        info = null;
    }

    public static void getData(){
        if(info != null){
            try {
                var id = NetMusicListClientUtil.getIdFromInfo(info);
                if(CacheManager.hasCache(id)){
                    lyric = CacheManager.getLycCache(id);
                    var imagePath = CacheManager.getImageCache(id);
                    if(imagePath == null){
                        icon = DEFAULT_TEXTURE;
                    }else{
                        Minecraft.getInstance().execute(() -> {
                            try {
                                texture = NetMusicListClientUtil.getImageFromPath(imagePath);
                            } catch (IOException ignored) {}
                        });
                        return;
                    }
                }
            } catch (IllegalAccessException ignored) {}
            try {
                getTextureFromLocal(info);
                id = NetMusicListClientUtil.getIdFromInfo(info);
                thread = new Thread(() -> getDataByThread(id));
                thread.start();
            } catch (Exception e) {
                NetMusicList.LOGGER.error("解析出现错误", e);
            }
        }
    }

    private static void getDataByThread(long id){
        try {
            var icon_url = NetMusicListClientUtil.getIconUrl(NetMusic.NET_EASE_WEB_API.song(id));
            MusicInfoHud.texture = NetMusicListClientUtil.getImageFromURL(icon_url);
            var l = NetMusicListUtil.getLyric(NetMusic.NET_EASE_WEB_API.lyric(id));
            if(Thread.currentThread().isInterrupted()){return;}
            lyric = l;
        } catch (Exception e) {
            NetMusicList.LOGGER.error("解析出现错误", e);
        }
    }

    private static void getTextureFromLocal(ItemMusicCD.SongInfo info){
        var musicPath = convertFileURLToPath(info.songUrl);
        if(musicPath != null){
            var imagePath = getPicturePath(musicPath);
            if(imagePath == null)return;
            try {
                texture = NetMusicListClientUtil.getImageFromPath(imagePath);
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
