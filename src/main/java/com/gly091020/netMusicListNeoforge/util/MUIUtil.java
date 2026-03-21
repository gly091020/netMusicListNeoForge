package com.gly091020.netMusicListNeoforge.util;

import com.gly091020.netMusicListNeoforge.client.manual.DirectoryFragment;
import com.gly091020.netMusicListNeoforge.client.manual.Entries;
import com.gly091020.netMusicListNeoforge.client.manual.EntriesFragment;
import icyllis.modernui.mc.neoforge.MuiForgeApi;
import net.minecraft.client.Minecraft;

public class MUIUtil {
    public static void openDirectoryScreen(){
        if(NetMusicListUtil.isNeedReload())
            NetMusicListUtil.loadAllMD();
        Minecraft.getInstance().setScreen(MuiForgeApi.get().createScreen(new DirectoryFragment(), null, Minecraft.getInstance().screen));
    }

    public static void openIAMScreen(){
        if(NetMusicListUtil.isNeedReload())
            NetMusicListUtil.loadAllMD();
        Minecraft.getInstance().setScreen(MuiForgeApi.get().createScreen(new EntriesFragment(
                Entries.createFromMD("iam.md")
        ), null, Minecraft.getInstance().screen));
    }
}
