package com.gly091020.netMusicListNeoforge.api;

import com.gly091020.netMusicListNeoforge.server.music.MusicRingManager;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 供其他模组接入 Ringer 体系的门面：注册/移除/查询 Ringer，或获取每服务器的管理器。
 */
public final class MusicRingerAPI {
    private MusicRingerAPI() {
    }

    public static MusicRingManager getManager(ServerLevel level) {
        return MusicRingManager.get(level);
    }

    public static void addRinger(IMusicRinger ringer) {
        MusicRingManager.get(ringer.getRingerLevel()).addRinger(ringer);
    }

    public static void removeRinger(ServerLevel level, UUID ringerId) {
        MusicRingManager.get(level).removeRinger(ringerId);
    }

    @Nullable
    public static IMusicRinger getRinger(ServerLevel level, UUID ringerId) {
        return MusicRingManager.get(level).getRinger(ringerId);
    }
}
