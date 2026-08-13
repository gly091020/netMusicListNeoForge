package com.gly091020.netMusicListNeoforge.server.music;

import com.gly091020.netMusicListNeoforge.api.IMusicRinger;
import com.gly091020.netMusicListNeoforge.packet.MusicPlayCommandPacket;
import com.gly091020.netMusicListNeoforge.packet.MusicStopCommandPacket;
import com.gly091020.netMusicListNeoforge.packet.MusicSyncCommandPacket;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 每服务器一个的 Ringer 管理器：持有全部活跃 Ringer，每 tick 推进播放，
 * 并向监听玩家派发 S2C 命令包。Ringer 的持久化状态落在各自维度的 SavedData 中。
 */
public class MusicRingManager {
    private static final Map<MinecraftServer, MusicRingManager> MANAGERS = new ConcurrentHashMap<>();
    private final MinecraftServer server;
    private final Map<UUID, IMusicRinger> ringers = new HashMap<>();

    private MusicRingManager(MinecraftServer server) {
        this.server = server;
    }

    public static MusicRingManager get(ServerLevel level) {
        return get(level.getServer());
    }

    public static MusicRingManager get(MinecraftServer server) {
        return MANAGERS.computeIfAbsent(server, MusicRingManager::new);
    }

    public static void clear(MinecraftServer server) {
        MANAGERS.remove(server);
    }

    public void addRinger(IMusicRinger ringer) {
        ringers.put(ringer.getRingerUUID(), ringer);
    }

    public void removeRinger(UUID ringerId) {
        var ringer = ringers.remove(ringerId);
        if (ringer != null) {
            ringer.onRemove();
        }
    }

    @Nullable
    public IMusicRinger getRinger(UUID ringerId) {
        return ringers.get(ringerId);
    }

    public Collection<IMusicRinger> getRingers() {
        return ringers.values();
    }

    /** 玩家下线/换维度时，将其名下所有物品 Ringer 移出活跃集合（状态保留在 SavedData 以支持续播）。 */
    public void removeOwner(UUID ownerId) {
        Iterator<IMusicRinger> it = ringers.values().iterator();
        while (it.hasNext()) {
            var ringer = it.next();
            if (ringer instanceof PortablePlayerRinger ppr && ppr.getOwnerId().equals(ownerId)) {
                it.remove();
                ringer.onRemove();
            }
        }
    }

    public void tick() {
        Iterator<Map.Entry<UUID, IMusicRinger>> it = ringers.entrySet().iterator();
        while (it.hasNext()) {
            var ringer = it.next().getValue();
            if (!ringer.exists()) {
                ringer.setRingerPlaying(false);
                ringer.onRemove();
                it.remove();
                continue;
            }
            ringer.ringerTick();
        }
    }

    public void persistAll() {
        for (var ringer : ringers.values()) {
            ringer.ringerPersist();
        }
    }

    public void broadcastPlay(IMusicRinger ringer, ItemMusicCD.SongInfo info, String url, String rawUrl,
                              int startTick, int slot) {
        PacketDistributor.sendToPlayersInDimension(ringer.getRingerLevel(),
                new MusicPlayCommandPacket(ringer.getRingerUUID(), ringer.getRingerEntityId(), url, rawUrl,
                        startTick, ringer.getRingerSongTime(), info.songName, slot, info));
    }

    public void broadcastStop(IMusicRinger ringer) {
        PacketDistributor.sendToPlayersInDimension(ringer.getRingerLevel(),
                new MusicStopCommandPacket(ringer.getRingerUUID()));
    }

    public void broadcastSync(IMusicRinger ringer) {
        PacketDistributor.sendToPlayersInDimension(ringer.getRingerLevel(),
                new MusicSyncCommandPacket(ringer.getRingerUUID(), (int) ringer.getRingerPosition()));
    }

    public MinecraftServer getServer() {
        return server;
    }
}
