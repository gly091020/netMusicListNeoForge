package com.gly091020.netMusicListNeoforge.util;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.client.PauseSoundManager;
import com.gly091020.netMusicListNeoforge.mixin.TickableSoundGetterMixins;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class NetMusicListClientUtil {
    @OnlyIn(Dist.CLIENT)
    public static void playSound(SoundEvent event){
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, 1));
    }

    @OnlyIn(Dist.CLIENT)
    public static long getIdFromInfo(ItemMusicCD.SongInfo info) throws IllegalAccessException {
        var s = info.songUrl;
        return getIdFromUrl(s);
    }

    public static long getIdFromUrl(String url) throws IllegalAccessException {
        String[] parts = url.split("[?&]id=");  // 为 什 么 要 用 这 种 代 码
        String idPart;
        if (parts.length > 1) {
            idPart = parts[1].split("&")[0];
        } else {
            throw new IllegalAccessException("解析失败");
        }
        return Long.parseLong(idPart.replace(".mp3", ""));
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("all")
    public static URL getIconUrl(String json) throws Exception{
        var data = (Map<String, Object>) NetMusicListUtil.GSON.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
        var song = (Map<String, Object>)((List<Object>)data.get("songs")).get(0);
        var album = song.get("album");
        // 大力出奇迹.png
        return new URL((String) ((Map<String, Object>)album).get("picUrl"));
    }

    @OnlyIn(Dist.CLIENT)
    public static NativeImage getImageFromURL(URL imageUrl) throws IOException {
        try (InputStream stream = imageUrl.openConnection().getInputStream()) {
            BufferedImage bufferedImage = ImageIO.read(stream);
            if (bufferedImage == null) {
                throw new IOException("无法读取图片 - 不支持的格式或损坏的文件");
            }
            NativeImage nativeImage = new NativeImage(bufferedImage.getWidth(), bufferedImage.getHeight(), false);
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    int argb = bufferedImage.getRGB(x, y);
                    int a = (argb >> 24) & 0xFF;
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;
                    int rgba = (a << 24) | (b << 16) | (g << 8) | r;
                    nativeImage.setPixelABGR(x, y, rgba | 0xFF000000);
                }
            }
            return nativeImage;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static NativeImage getImageFromPath(Path imagePath) throws IOException {
        try (InputStream stream = Files.newInputStream(imagePath)) {
            BufferedImage bufferedImage = ImageIO.read(stream);
            if (bufferedImage == null) {
                throw new IOException("无法读取图片 - 不支持的格式或损坏的文件");
            }
            NativeImage nativeImage = new NativeImage(bufferedImage.getWidth(), bufferedImage.getHeight(), false);
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    int argb = bufferedImage.getRGB(x, y);
                    int a = (argb >> 24) & 0xFF;
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;
                    int rgba = (a << 24) | (b << 16) | (g << 8) | r;
                    nativeImage.setPixelABGR(x, y, rgba | 0xFF000000);
                }
            }
            return nativeImage;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static DynamicTexture createTextureFromImg(NativeImage nativeImage){
        return new DynamicTexture(() -> "net_music_list_icon", nativeImage);
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isPaused(){
        return ((PauseSoundManager)Minecraft.getInstance().getSoundManager()).isPaused();
    }

    public static List<TickableSoundInstance> getTickableSounds(){
        return ((TickableSoundGetterMixins.SoundEngineMixin)((TickableSoundGetterMixins.SoundManagerMixin) Minecraft.getInstance().getSoundManager()).getSoundEngine()).getTickableSoundInstances();
    }
}
