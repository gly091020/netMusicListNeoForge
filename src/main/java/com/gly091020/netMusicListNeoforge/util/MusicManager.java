package com.gly091020.netMusicListNeoforge.util;

import com.gly091020.netMusicListNeoforge.sounds.PlayerNetMusicSound;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class MusicManager {
    private static final List<PlayerNetMusicSound> SOUNDS = new ArrayList<>();
    public static void clearSound(){
        SOUNDS.clear();
    }

    public static void addSound(PlayerNetMusicSound sound){
        SOUNDS.add(sound);
    }

    public static void tick(){
        SOUNDS.removeIf(AbstractTickableSoundInstance::isStopped);
    }

    public static List<PlayerNetMusicSound> getSelfSounds(){
        return SOUNDS.stream().filter(PlayerNetMusicSound::isClientPlayer).toList();
    }
}
