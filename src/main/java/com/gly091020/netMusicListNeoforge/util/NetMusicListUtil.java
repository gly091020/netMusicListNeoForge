package com.gly091020.netMusicListNeoforge.util;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.ExtraMusicList;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicList;
import com.github.tartaricacid.netmusic.compat.cloth.MenuIntegration;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.client.PauseSoundManager;
import com.gly091020.netMusicListNeoforge.client.manual.Entries;
import com.gly091020.netMusicListNeoforge.client.manual.EntriesRegistry;
import com.gly091020.netMusicListNeoforge.config.ConfigScreenGetter;
import com.gly091020.netMusicListNeoforge.config.NetMusicListConfig;
import com.gly091020.netMusicListNeoforge.hud.MusicInfoHud;
import com.gly091020.netMusicListNeoforge.mixin.TickableSoundGetterMixins;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.loading.FMLEnvironment;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gly091020.netMusicListNeoforge.NetMusicList.CONFIG;

public class NetMusicListUtil {
    public static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    public static final UUID _5112151111121 = UUID.fromString("91bd580f-5f17-4e30-872f-2e480dd9a220");
    public static final UUID N44 = UUID.fromString("5a33e9b0-35bc-44ed-9b4e-03e3e180a3d2");
    // 自己的石山还得让别人来修……
    public static final UUID WANG_REN_ZE_9788 = UUID.fromString("21b900df-8ea3-47e4-81cb-ed1146714b14");
    public static boolean globalStopMusic = false;
    public static boolean needReload = false;

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
        var data = (Map<String, Object>)GSON.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
        var song = (Map<String, Object>)((List<Object>)data.get("songs")).get(0);
        var album = song.get("album");
        // 大力出奇迹.png
        return new URL((String) ((Map<String, Object>)album).get("picUrl"));
    }

    @OnlyIn(Dist.CLIENT)
    public static AbstractTexture getTextureFromURL(URL imageUrl) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(imageUrl.toString()))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        HttpResponse<InputStream> response;
        try {
            response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("图片下载被中断", e);
        }
        if (response.statusCode() != 200) {
            throw new IOException("图片下载失败: HTTP " + response.statusCode());
        }
        try (InputStream stream = response.body()) {
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
                    nativeImage.setPixelRGBA(x, y, rgba | 0xFF000000);
                }
            }
            return new DynamicTexture(nativeImage);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static AbstractTexture getTextureFromPath(Path imagePath) throws IOException {
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
                    nativeImage.setPixelRGBA(x, y, rgba | 0xFF000000);
                }
            }
            return new DynamicTexture(nativeImage);
        }
    }

    public static boolean isNeedReload(){
        var b = needReload;
        needReload = false;
        return b;
    }

    public static class Lyric {
        @SerializedName("lyric")
        private final LinkedHashMap<Float, String> lyric;

        @SerializedName("transform_lyric")
        @Nullable
        private final LinkedHashMap<Float, String> transformLyric;

        public Lyric(LinkedHashMap<Float, String> lyric, @Nullable LinkedHashMap<Float, String> transformLyric) {
            this.lyric = lyric;
            if (transformLyric != null && transformLyric.isEmpty())
                transformLyric = null;
            this.transformLyric = transformLyric;
        }

        public Lyric() {
            this.lyric = new LinkedHashMap<>();
            this.transformLyric = null;
        }

        public Pair<String, String> getLyric(float second) {
            var keyList = lyric.keySet().stream().toList();
            var valueList = lyric.values().stream().toList();
            var text = "";
            var lyricTime = 0f;
            String transformText = null;
            for (int i = 0; i < lyric.size(); i++) {
                if (i == lyric.size() - 1) {
                    text = valueList.get(i);
                    lyricTime = keyList.get(i);
                    break;
                }
                if (keyList.get(i) <= second && keyList.get(i + 1) > second) {
                    text = valueList.get(i);
                    lyricTime = keyList.get(i);
                    break;
                }
            }
            if (transformLyric != null) {
                transformText = transformLyric.getOrDefault(lyricTime, null);
            }
            return new Pair<>(text, transformText);
        }

        public LinkedHashMap<Float, String> getLyric() {
            return lyric;
        }

        public LinkedHashMap<Float, String> getTransformLyric() {
            if(transformLyric == null)return new LinkedHashMap<>();
            return transformLyric;
        }

        public String toJson() {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            return gson.toJson(this);
        }

        public static Lyric fromJson(String json) {
            Gson gson = new Gson();
            return gson.fromJson(json, Lyric.class);
        }

        public LyricRecord toLyricRecord(){
            Int2ObjectSortedMap<String> lyric1 = new Int2ObjectRBTreeMap<>();
            Int2ObjectSortedMap<String> lyric2;
            lyric.forEach((k, v) -> lyric1.put((int)(k / 0.05f), v));
            if(transformLyric != null){
                lyric2 = new Int2ObjectRBTreeMap<>();
                transformLyric.forEach((k, v) -> lyric2.put((int)(k / 0.05f), v));
            } else {
                lyric2 = null;
            }
            return new LyricRecord(lyric1, lyric2);
        }

        public static Lyric fromLyricRecord(LyricRecord record){
            LinkedHashMap<Float, String> f1 = new LinkedHashMap<>();
            LinkedHashMap<Float, String> f2 = new LinkedHashMap<>();

            record.getLyrics().forEach((integer, s) -> f1.put(integer * 0.05f, s));
            if (record.getTransLyrics() != null) {
                record.getTransLyrics().forEach((integer, s) -> f2.put(integer * 0.05f, s));
            }

            return new Lyric(f1, f2);
        }

        public static final StreamCodec<FriendlyByteBuf, Lyric> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.map(
                        LinkedHashMap::new,
                        ByteBufCodecs.FLOAT,
                        ByteBufCodecs.stringUtf8(32767)
                ),
                Lyric::getLyric,
                ByteBufCodecs.map(
                        LinkedHashMap::new,
                        ByteBufCodecs.FLOAT,
                        ByteBufCodecs.stringUtf8(32767)
                ),
                Lyric::getTransformLyric,
                Lyric::new
        );
    }

    public static String secondsToMinutesSeconds(int totalSeconds) {
        if (totalSeconds < 0) {
            return "00:00";
        }

        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    @SuppressWarnings("all")
    public static Lyric getLyric(String json){
        var data = (Map<String, Object>)GSON.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
        var lrc = (String)((Map<String, Object>)data.get("lrc")).get("lyric");
        var transformlLrc = "";
        if(data.containsKey("tlyric")){
            transformlLrc = (String)((Map<String, Object>)data.get("tlyric")).get("lyric");
        }
        if(lrc.isEmpty()){
            return null;
        }
        LinkedHashMap<Float, String> lyricMap;
        LinkedHashMap<Float, String> transformLyricMap = null;
        if(!transformlLrc.isEmpty()){
            transformLyricMap = new LinkedHashMap<>();
            for(String part: transformlLrc.split("\n")){
                var p = getLyricPair(part);
                if(p != null){transformLyricMap.put(p.getA(), p.getB());}
            }
        }
        lyricMap = new LinkedHashMap<>();
        for(String part: lrc.split("\n")){
            var p = getLyricPair(part);
            if(p != null){lyricMap.put(p.getA(), p.getB());}
        }
        return new Lyric(lyricMap, transformLyricMap);
    }

    private static Pair<Float, String> getLyricPair(String input){
        Pattern pattern = Pattern.compile("^\\[(\\d+):(\\d+)[.:](\\d+)](.*)$");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            int minutes = Integer.parseInt(matcher.group(1));
            int seconds = Integer.parseInt(matcher.group(2));
            int milliseconds = Integer.parseInt(matcher.group(3));
            String text = matcher.group(4);

            // 计算总秒数（带小数）
            float totalSeconds = minutes * 60 + seconds + milliseconds / 1000f;
            return new Pair<>(totalSeconds, text.trim());
        }
        return null;
    }

    public static boolean isGLY(){
        return Objects.equals(Minecraft.getInstance().getUser().getProfileId(), _5112151111121);
    }

    public static boolean isN44(){
        return Objects.equals(Minecraft.getInstance().getUser().getProfileId(), N44);
    }

    public static List<TickableSoundInstance> getTickableSounds(){
        return ((TickableSoundGetterMixins.SoundEngineMixin)((TickableSoundGetterMixins.SoundManagerMixin) Minecraft.getInstance().getSoundManager()).getSoundEngine()).getTickableSoundInstances();
    }

    public static boolean isWangRenZe9788(){
        return Objects.equals(Minecraft.getInstance().getUser().getProfileId(), WANG_REN_ZE_9788);
    }

    public static void reloadConfig(){
        var holder = AutoConfig.getConfigHolder(NetMusicListConfig.class);
        holder.setConfig(CONFIG);
        holder.save();
        if(FMLEnvironment.dist.isClient()){
            MusicInfoHud.setPos(CONFIG.x, CONFIG.y);
        }
        CacheManager.reload();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isPaused(){
        return ((PauseSoundManager)Minecraft.getInstance().getSoundManager()).isPaused();
    }

    public static boolean hasLoginNeed(){
        return ModList.get().isLoaded("net_music_login_need");
    }

    /** 统一的服务端 VIP 拦截判断：登录模组就绪或配置允许时放行。 */
    public static boolean isVipBlocked(ItemMusicCD.SongInfo info){
        return info.vip && !CONFIG.noVIP && !hasLoginNeed();
    }

    public static List<ItemMusicCD.SongInfo> getMusicList(long id) throws Exception {
        // 从网络音乐机里拿的代码
        var SONGS = new ArrayList<ItemMusicCD.SongInfo>();
        NetEaseMusicList pojo = GSON.fromJson(NetMusic.NET_EASE_WEB_API.list(id), NetEaseMusicList.class);
        int count = pojo.getPlayList().getTracks().size();
        int size = Math.min(pojo.getPlayList().getTrackIds().size(), CONFIG.maxImportList);
        if (count < size) {
            // ids过多时会无法解析，这里分开解析
            long[] ids = new long[size - count];

            for(int i = count; i < size; ++i) {
                ids[i - count] = pojo.getPlayList().getTrackIds().get(i).getId();
            }

            if(ids.length <= 100){
                String extraTrackInfo = NetMusic.NET_EASE_WEB_API.songs(ids);
                ExtraMusicList extra = GSON.fromJson(extraTrackInfo, ExtraMusicList.class);
                pojo.getPlayList().getTracks().addAll(extra.getTracks());
            }else{
                int batchSize = 100;
                for(int i = 0; i < ids.length; i += batchSize){
                    int end = Math.min(i + batchSize, ids.length);
                    long[] batchIds = Arrays.copyOfRange(ids, i, end);
                    String extraTrackInfo = NetMusic.NET_EASE_WEB_API.songs(batchIds);
                    ExtraMusicList extra = GSON.fromJson(extraTrackInfo, ExtraMusicList.class);
                    pojo.getPlayList().getTracks().addAll(extra.getTracks());
                }
            }
        }
        for(NetEaseMusicList.Track track : pojo.getPlayList().getTracks()) {
            SONGS.add(new ItemMusicCD.SongInfo(track));
        }
        return SONGS;
    }

    public static URL resolveRedirect(URL originalUrl, int maxRedirects, Map<String, String> headers) throws IOException {
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

    public static String loadStringFromFile(String path) {
        String resourcePath = "/assets/" + NetMusicList.ModID + "/" + path;
        try (InputStream inputStream = NetMusicListUtil.class.getResourceAsStream(resourcePath)) {
            if (inputStream != null) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                throw new RuntimeException("Could not find resource: " + resourcePath);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource: " + resourcePath, e);
        }
    }

    public static void loadAllMD(){
        EntriesRegistry.clear();
        List<Object> data;
        var language = Minecraft.getInstance().getLanguageManager().getSelected();
        try{
            initMDButtons();
            try{
                data = GSON.fromJson(loadStringFromFile("manual/" + language + "/all_entries.json"),
                        new TypeToken<List<Object>>(){}.getType());
                registryMD(null, data);
            }catch (RuntimeException e){
                data = GSON.fromJson(loadStringFromFile("manual/en_us/all_entries.json"),
                        new TypeToken<List<Object>>(){}.getType());
                registryMD(null, data);
            }
            if(!FMLEnvironment.production)
                EntriesRegistry.registryNewEntries(Entries.createFromMD("dev.md"));
        }catch (Exception e){
            NetMusicList.LOGGER.error("加载手册出现错误：", e);
        }
        NetMusicList.LOGGER.info("已加载{}手册", language);
    }

    @SuppressWarnings("all")
    private static void registryMD(@Nullable EntriesRegistry.Directory directory, List<Object> data){
        for(Object item: data){
            if(item instanceof List list){
                EntriesRegistry.Directory d;
                if(directory == null){
                    d = EntriesRegistry.registryNewParent();
                }else{
                    d = directory.registryNewParent();
                }
                registryMD(d, (List<Object>) list);
            }else if(item instanceof String path){
                if(directory == null){
                    EntriesRegistry.registryNewEntries(Entries.createFromMD(path));
                }else{
                    directory.registryNewEntries(Entries.createFromMD(path));
                }
            }
        }
    }

    public static void initMDButtons(){
        EntriesRegistry.registryButtonGroup("example", List.of(
                new Entries.Button("示例按钮", () -> {})
        ));

        var buttons = new ArrayList<Entries.Button>();
        buttons.add(new Entries.Button(
                Component.translatable("itemGroup.netmusic").getString() +
                        Component.translatable("text.cloth-config.config").getString(),
                () -> Minecraft.getInstance().setScreen(MenuIntegration.getConfigBuilder()
                        .setParentScreen(Minecraft.getInstance().screen).build())
        ));
        buttons.add(new Entries.Button(
                Component.translatable("modmenu.nameTranslation.net_music_list").getString() +
                        Component.translatable("text.cloth-config.config").getString(),
                () -> Minecraft.getInstance().setScreen(
                        ConfigScreenGetter.getConfigScreen(Minecraft.getInstance().screen))
        ));
        if(ModList.get().isLoaded("net_music_login_need")){
            buttons.add(new Entries.Button(
                    ModList.get().getModContainerById("net_music_login_need").orElseThrow()
                            .getModInfo().getDisplayName() +
                            Component.translatable("text.cloth-config.config").getString(),
                    () -> Minecraft.getInstance().setScreen(
                            LoginNeedUtil.getConfigScreen())
            ));
        }
        EntriesRegistry.registryButtonGroup("all_config", buttons);
    }

    public static String getSongTime(int songTime) {
        int min = songTime / 60;
        int sec = songTime % 60;
        String minStr = min <= 9 ? "0" + min : "" + min;
        String secStr = sec <= 9 ? "0" + sec : "" + sec;
        return Component.translatable("tooltips.netmusic.cd.time.format", minStr, secStr).getString();
    }

    public static boolean isCreator(){
        // TODO:目前不知道IMG的uuid，知道了补上
        return isGLY() || isN44() || isWangRenZe9788();
    }

    public static boolean hasAdvancedPlayer(){
        return ModList.get().isLoaded("netmusicadvancedplayer");
    }

    public static void testMengSamaNetMusic(){
        final String MODID = "mengsamanetmusic";
        if(ModList.get().isLoaded(MODID))
            ModLoadingIssue.warning("text.net_music_list.meng_sa_ma_net_music.warning")
                    .withAffectedMod(ModList.get().getModContainerById(MODID).orElseThrow().getModInfo())
                    .withSeverity(ModLoadingIssue.Severity.WARNING);
    }
}
