package com.gly091020.netMusicListNeoforge.config;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = NetMusicList.ModID)
public class NetMusicListConfig implements ConfigData {
    public boolean enableEtched = true;
    public boolean oldGUI = false;
    public boolean musicHUD = true;
    public int x = 10;
    public int y = 10;
}
