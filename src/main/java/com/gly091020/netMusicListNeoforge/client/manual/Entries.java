package com.gly091020.netMusicListNeoforge.client.manual;

import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public record Entries(String title,
                      String markdown,
                      @Nullable String imgID,
                      List<Button> buttons,
                      List<Image> images, boolean enable) {
    public record Image(String id, String tip){ }
    public record Button(String name, Runnable runnable){}

    private static final Pattern ENTRY_PATTERN = Pattern.compile("<!-- entry\\.(.+):(.*) -->");
    public static Entries createFromMD(String path){
        var language = Minecraft.getInstance().getLanguageManager().getSelected();
        String md;
        try{
            try{
                md = NetMusicListUtil.loadStringFromFile("manual/" + language + "/" + path);
            } catch (RuntimeException e) {
                md = NetMusicListUtil.loadStringFromFile("manual/en_us/" + path);
            }
        } catch (Exception e) {
            return createError(e);
        }
        var title = "";
        String img = null;
        var images = new ArrayList<Image>();
        var enable = true;
        List<Button> buttons = new ArrayList<>();

        var matcher = ENTRY_PATTERN.matcher(md);
        while (matcher.find()) {
            if(matcher.groupCount() != 2)continue;
            var type = matcher.group(1);
            var data = matcher.group(2);
            switch (type){
                case "title": {
                    title = data;
                    break;
                }
                case "ico": {
                    img = data;
                    break;
                }
                case "img":{
                    var p = data.split(",");
                    if(p.length != 2)continue;
                    images.add(new Image(p[0], p[1]));
                    break;
                }
                case "enable":{
                    if(Objects.equals(data, "false"))enable = false;
                    break;
                }
                case "button":{
                    buttons = EntriesRegistry.getButtons(data);
                    break;
                }
            }
        }

        return new Entries(title, md, img, buttons, images, enable);
    }

    private static Entries createError(Exception e){
        return new Entries("出现问题", e.getMessage() + "\n\n```" +
                Arrays.toString(e.getStackTrace()) + "```", null, List.of(), List.of(), true);
    }
}
