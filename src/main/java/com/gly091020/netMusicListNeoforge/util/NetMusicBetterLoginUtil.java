package com.gly091020.netMusicListNeoforge.util;

import com.github.img.netmusicbetterlogin.config.GeneralConfig;

public class NetMusicBetterLoginUtil {
    public static String getCookie(){
        return GeneralConfig.COOKIE.get();
    }
}
