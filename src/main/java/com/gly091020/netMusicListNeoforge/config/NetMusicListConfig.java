package com.gly091020.netMusicListNeoforge.config;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = NetMusicList.ModID)
public class NetMusicListConfig implements ConfigData {
    public boolean debug = false;

    public boolean musicHUD = true;
    public boolean notPauseSoundOnGamePause = false;
    public int x = 10;
    public int y = 10;
    public int maxImportList = 300;
    public boolean enableCache = true;
    public boolean globalCache = false;

    public float selectHudSize = 0.7f;
    public int selectHudCount = 5;
    public boolean selectHudShowArtist = true;
    public boolean glowingText = false;

    public boolean oldGUI = false;
    public boolean noVIP = false;

    public boolean only5Second = false;

    public boolean allowLyricToServer = false;

    // gly特有的突然扭曲
    public boolean glyMusicEntity = false;

    public boolean showedIAM = false;
}
