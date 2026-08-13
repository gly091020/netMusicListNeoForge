package com.gly091020.netMusicListNeoforge.api;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Ringer 的只读状态视图，供其他模组查询某个音乐来源的播放状态。
 */
public interface MusicRingerAccess {
    UUID getRingerUUID();

    ServerLevel getRingerLevel();

    Component getRingerName();

    boolean isRingerPlaying();

    @Nullable
    ItemMusicCD.SongInfo getRingerMusic();

    int getRingerSongTime();

    long getRingerPosition();

    boolean isRingerLoop();

    int getRingerEntityId();

    @NotNull
    Vec3 getRingerSpatialPosition();

    float getRingerVolume();

    boolean exists();

    default boolean canListen(ServerPlayer player) {
        return true;
    }
}
