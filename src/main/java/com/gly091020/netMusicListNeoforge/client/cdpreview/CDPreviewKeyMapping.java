package com.gly091020.netMusicListNeoforge.client.cdpreview;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class CDPreviewKeyMapping {
    private static KeyMapping cdPreview;

    private CDPreviewKeyMapping() {
    }

    public static void init() {
        cdPreview = new KeyMapping(
                "key.net_music_list.cd_preview",
                KeyConflictContext.UNIVERSAL,
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_LSHIFT,
                "modmenu.nameTranslation.net_music_list"
        );
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(cdPreview);
    }

    /** 渲染线程实时查询物理按键，保证按住瞬间生效 */
    public static boolean isKeyDown() {
        if (cdPreview == null) return false;
        if(Minecraft.getInstance().screen == null)return false;
        var window = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetKey(window, cdPreview.getKey().getValue()) == GLFW.GLFW_PRESS;
    }
}
