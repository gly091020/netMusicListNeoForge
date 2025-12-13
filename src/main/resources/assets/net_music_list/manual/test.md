# 等一下，
<!-- entry.title:测试5 -->
<!-- entry.ico:gui/gly091020.png -->
<!-- entry.img:gui/bg.png,图片1 -->
<!-- entry.img:gui/old_bg.png,图片2 -->
<!-- entry.img:gui/server.png,图片3 -->
<!-- entry.img:gui/default.png,图片4 -->

你TM拿Markdown写模组手册啊？

```java
package com.gly091020.netMusicListNeoforge.util;

import java.util.regex.Pattern;

public enum URLType {
    SONG("https://.*163.*song.*?(?!user)id=([0-9]+)"),
    SONG_LIST("https://.*?163.*?playlist.*?(?!user)id=([0-9]+)"),
    DJ("https://.*?163.*?program.*?[(?!user)(?!dj)]id=([0-9]+)"),
    OTHER("(.*)");
    final Pattern pattern;
    URLType(String pattern){
        this.pattern = Pattern.compile(pattern);
    }

    public boolean isMatch(String url){
        return this.pattern.matcher(url).find();
    }

    public String getMatch(String url){
        var m = this.pattern.matcher(url);
        if(m.find())
            return m.group(1);
        return null;
    }
}

```