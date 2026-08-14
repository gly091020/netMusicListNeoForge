package com.gly091020.netMusicListNeoforge.util;

import com.gly091020.netMusicListNeoforge.sounds.RingerSound;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class MusicManager {
    private static final Map<UUID, RingerSound> SOUNDS = new HashMap<>();
    private static final Map<UUID, Integer> SERVER_POSITIONS = new HashMap<>();

    public static void clearSound(){
        SOUNDS.values().forEach(RingerSound::stopMusic);
        SOUNDS.clear();
        SERVER_POSITIONS.clear();
    }

    public static void addSound(RingerSound sound){
        var old = SOUNDS.put(sound.getRingerId(), sound);
        if (old != null && old != sound) {
            // 同 Ringer 的新声音接管前，确保旧声音被停止，避免残留播放
            old.stopMusic();
        }
    }

    public static void stop(UUID ringerId){
        var sound = SOUNDS.remove(ringerId);
        if(sound != null){
            sound.stopMusic();
        }
        SERVER_POSITIONS.remove(ringerId);
    }

    public static void tick(){
        SOUNDS.entrySet().removeIf(entry -> entry.getValue().isStopped());
    }

    public static List<RingerSound> getSelfSounds(){
        return SOUNDS.values().stream().filter(RingerSound::isSelf).toList();
    }

    public static void setServerPosition(UUID ringerId, int positionTick){
        SERVER_POSITIONS.put(ringerId, positionTick);
    }

    @Nullable
    public static Integer getServerPosition(UUID ringerId){
        return SERVER_POSITIONS.get(ringerId);
    }
}
