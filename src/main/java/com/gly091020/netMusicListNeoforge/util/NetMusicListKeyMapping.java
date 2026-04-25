package com.gly091020.netMusicListNeoforge.util;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public class NetMusicListKeyMapping {
    // 不要使用@OnlyIn(Dist.CLIENT)！会导致正式服务器环境加载失败
    public static KeyMapping TOGGLE_MUSIC_SPEED_UP;
    public static KeyMapping TOGGLE_MUSIC_TRANSFORM;
    public static KeyMapping FAST_STOP;
    public static KeyMapping SWITCH_MUSIC;

    public static Identifier CATEGORY = Identifier.fromNamespaceAndPath(NetMusicList.ModID, "net_music_list");

    public static void init(){
        var c = KeyMapping.Category.register(CATEGORY);
        TOGGLE_MUSIC_TRANSFORM = new KeyMapping(
                "key.net_music_list.toggle_music_transform",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_LALT,
                c
        );
        TOGGLE_MUSIC_SPEED_UP = new KeyMapping(
                "key.net_music_list.toggle_music_speed_up",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_LSHIFT,
                c
        );
        FAST_STOP = new KeyMapping(
                "key.net_music_list.toggle_music_fast_stop",
                KeyConflictContext.UNIVERSAL,
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                c
        );
        SWITCH_MUSIC = new KeyMapping(
                "key.net_music_list.switch_music",
                KeyConflictContext.UNIVERSAL,
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                c
        );
    }

    public static void registerKeyBindings(final RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_MUSIC_SPEED_UP);
        event.register(TOGGLE_MUSIC_TRANSFORM);
        event.register(FAST_STOP);
        event.register(SWITCH_MUSIC);
    }
}
