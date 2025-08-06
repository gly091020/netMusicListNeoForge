package com.gly091020.netMusicListNeoforge.sounds;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    public static Map<String, PlayerNetMusicSound> sounds = new HashMap<>();
    public static void update(){
        for (String key: sounds.keySet()){
            if(sounds.get(key).isStopped()){
                sounds.remove(key);
            }
        }
    }

    public static void stopMusic(String uuid){
        if(!sounds.containsKey(uuid)){return;}
        sounds.get(uuid).stopMusic();
    }
}
