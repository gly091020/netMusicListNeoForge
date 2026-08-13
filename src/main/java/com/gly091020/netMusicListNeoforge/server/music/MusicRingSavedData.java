package com.gly091020.netMusicListNeoforge.server.music;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 每维度一份的 Ringer 播放状态持久化。
 * 只保存"播放中"标志与进度 tick，曲目索引仍以播放列表物品自身为准。
 */
public class MusicRingSavedData extends SavedData {
    public static final String NAME = "net_music_list_ringers";
    private final Map<UUID, RingerState> states = new HashMap<>();

    public record RingerState(boolean playing, int positionTicks) {
    }

    public static MusicRingSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(MusicRingSavedData::new, MusicRingSavedData::load), NAME);
    }

    public void setState(UUID ringerId, boolean playing, int positionTicks) {
        states.put(ringerId, new RingerState(playing, positionTicks));
        setDirty();
    }

    public void removeState(UUID ringerId) {
        states.remove(ringerId);
        setDirty();
    }

    @Nullable
    public RingerState getState(UUID ringerId) {
        return states.get(ringerId);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        var list = new ListTag();
        for (var entry : states.entrySet()) {
            var t = new CompoundTag();
            t.putUUID("id", entry.getKey());
            t.putBoolean("playing", entry.getValue().playing());
            t.putInt("position", entry.getValue().positionTicks());
            list.add(t);
        }
        tag.put("ringers", list);
        return tag;
    }

    public static MusicRingSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        var data = new MusicRingSavedData();
        var list = tag.getList("ringers", Tag.TAG_COMPOUND);
        for (Tag t : list) {
            var c = (CompoundTag) t;
            data.states.put(c.getUUID("id"),
                    new RingerState(c.getBoolean("playing"), c.getInt("position")));
        }
        return data;
    }
}
