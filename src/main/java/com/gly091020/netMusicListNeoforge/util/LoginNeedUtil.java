package com.gly091020.netMusicListNeoforge.util;

import com.gly091020.NetMusicLoginNeed.NetMusicLoginNeedUtil;
import com.gly091020.NetMusicLoginNeed.config.NetMusicLoginNeedConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class LoginNeedUtil {
    public static String getUrl(String url){
        return NetMusicLoginNeedUtil.pasteVIPUrl(url);
    }

    public static Screen getConfigScreen(){
        return AutoConfig.getConfigScreen(NetMusicLoginNeedConfig.class, Minecraft.getInstance().screen).get();
    }
}
