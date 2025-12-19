package com.gly091020.netMusicListNeoforge.util;

import com.gly091020.netMusicListNeoforge.client.manual.DirectoryFragment;
import icyllis.modernui.mc.neoforge.MuiForgeApi;
import net.minecraft.client.Minecraft;

public class MUIUtil {
    public static void openDirectoryScreen(){
        NetMusicListUtil.initMDButtons();
        Minecraft.getInstance().setScreen(MuiForgeApi.get().createScreen(new DirectoryFragment(), null, Minecraft.getInstance().screen));
    }
}
