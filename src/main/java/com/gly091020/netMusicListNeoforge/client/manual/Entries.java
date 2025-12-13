package com.gly091020.netMusicListNeoforge.client.manual;

import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public record Entries(String title,
                      String markdown,
                      @Nullable String imgID,
                      List<Button> buttons,
                      List<Image> images) {
    public record Image(String id, String tip){ }
    public record Button(String name, Runnable runnable){}

    private static final Pattern ENTRY_PATTERN = Pattern.compile("<!-- entry\\.(.+):(.*) -->");
    public static Entries createFromMD(String path, List<Button> buttons){
        String md;
        try{
            md = NetMusicListUtil.loadStringFromFile("manual/" + path);
        } catch (Exception e) {
            return createError(e);
        }
        var title = "";
        String img = null;
        var images = new ArrayList<Image>();

        var matcher = ENTRY_PATTERN.matcher(md);
        while (matcher.find()) {
            if(matcher.groupCount() != 2)continue;
            var type = matcher.group(1);
            var data = matcher.group(2);
            switch (type){
                case "title": title = data;
                case "ico": img = data;
                case "img":{
                    var p = data.split(",");
                    if(p.length != 2)continue;
                    images.add(new Image(p[0], p[1]));
                }
            }
        }

        return new Entries(title, md, img, buttons, images);
    }

    public static Entries createFromMD(String path){
        return createFromMD(path, List.of());
    }

    private static Entries createError(Exception e){
        return new Entries("出现问题", e.getMessage() + "\n" +
                Arrays.toString(e.getStackTrace()), null, List.of(), List.of());
    }
}
