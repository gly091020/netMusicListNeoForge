package com.gly091020.netMusicListNeoforge.config;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = NetMusicList.ModID)
public class NetMusicListConfig implements ConfigData {
    public boolean debug = false;

    public boolean musicHUD = true;

    @Deprecated
    // todo:由于底层重写，此功能失效
    public boolean notPauseSoundOnGamePause = false;

    public int x = 10;
    public int y = 10;
    public int maxImportList = 300;

    public String neteaseCookie = "";
    public String qqCookie = "";
    public String bilibiliCookie = "";

    public boolean enableCache = true;
    public boolean globalCache = false;

    /** 强制使用 ffmpeg 解码所有音乐（需安装 ffmpeg）；关闭时使用 Java Sound 默认解码。 */
    public boolean forceFFmpeg = false;

    /** ffmpeg 可执行文件所在目录；留空则从系统 PATH 查找。 */
    public String ffmpegPath = "";

    /** ffprobe 可执行文件所在目录；预留，留空则从系统 PATH 查找。 */
    public String ffprobePath = "";

    public float selectHudSize = 0.7f;
    public int selectHudCount = 5;
    public boolean selectHudShowArtist = true;
    public boolean glowingText = false;

    public boolean oldGUI = false;
    public boolean noVIP = false;

    public boolean only5Second = false;

    public boolean allowLyricToServer = true;

    // gly特有的突然扭曲
    public boolean glyMusicEntity = false;

    public boolean showedIAM = false;
}
