package com.gly091020.netMusicListNeoforge.server.music;

import com.github.tartaricacid.netmusic.api.resolver.MusicPlayResolverManager;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.api.IMusicRinger;
import com.gly091020.netMusicListNeoforge.entity.MusicPlayerEntity;
import com.gly091020.netMusicListNeoforge.item.NetMusicListItem;
import com.gly091020.netMusicListNeoforge.util.LoginNeedUtil;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 放置/掉落形态的随身播放器实体对应的服务端 Ringer，与物品形态共用同一 Ringer UUID。
 */
public class EntityRinger implements IMusicRinger {
    private final MusicRingManager manager;
    private final ServerLevel level;
    private final UUID ringerId;
    private final int entityId;
    private boolean playing;
    private long position;
    private int skipGuard = 0;
    private boolean resolving = false;
    private long playSeq = 0;

    public EntityRinger(MusicRingManager manager, ServerLevel level, UUID ringerId, int entityId) {
        this.manager = manager;
        this.level = level;
        this.ringerId = ringerId;
        this.entityId = entityId;
        var state = MusicRingSavedData.get(level).getState(ringerId);
        if (state != null) {
            this.playing = state.playing();
            this.position = state.positionTicks();
        }
    }

    private @Nullable MusicPlayerEntity getEntity() {
        var entity = level.getEntity(entityId);
        return entity instanceof MusicPlayerEntity musicPlayer ? musicPlayer : null;
    }

    public void play() {
        var entity = getEntity();
        if (entity == null || !entity.isRightItem(entity.getMusicCD())) {
            return;
        }
        position = 0;
        playing = true;
        entity.setPlaying(true);
        persist();
        requestPlay();
    }

    public void stop() {
        playing = false;
        playSeq++;
        var entity = getEntity();
        if (entity != null) {
            entity.setPlaying(false);
        }
        persist();
        manager.broadcastStop(this);
    }

    /** 实体重新加载/恢复时继续播放。 */
    public void resume() {
        if (playing) {
            requestPlay();
        }
    }

    private void requestPlay() {
        playSeq++;
        resolving = false;
        playCurrent();
    }

    private void persist() {
        MusicRingSavedData.get(level).setState(ringerId, playing, (int) position);
    }

    private void playCurrent() {
        if (resolving) {
            return;
        }
        var entity = getEntity();
        if (entity == null || !playing) {
            return;
        }
        var info = ItemMusicCD.getSongInfo(entity.getMusicCD());
        if (info == null) {
            stopInternal();
            return;
        }
        if (NetMusicListUtil.isVipBlocked(info)) {
            if (++skipGuard > 16) {
                stopInternal();
                return;
            }
            var cd = entity.getMusicCD();
            NetMusicListItem.nextMusic(cd);
            entity.setMusicCD(cd);
            requestPlay();
            return;
        }
        skipGuard = 0;
        resolving = true;
        long seq = playSeq;
        var clone = info.clone();
        MusicPlayResolverManager.resolve(clone).thenApplyAsync(resolved -> {
            if (NetMusicListUtil.hasLoginNeed()) {
                try {
                    resolved.songUrl = LoginNeedUtil.getUrl(resolved.songUrl);
                } catch (Exception e) {
                    NetMusicList.LOGGER.error("登录解析出现错误：", e);
                }
            }
            return resolved;
        }).thenAcceptAsync(resolved -> {
            var server = level.getServer();
            if (server == null) {
                return;
            }
            server.execute(() -> {
                resolving = false;
                if (!playing || seq != playSeq) {
                    return;
                }
                manager.broadcastPlay(this, info, resolved.songUrl, info.songUrl, (int) position, -1);
                persist();
            });
        });
    }

    private void stopInternal() {
        playing = false;
        playSeq++;
        var entity = getEntity();
        if (entity != null) {
            entity.setPlaying(false);
        }
        persist();
        manager.broadcastStop(this);
    }

    @Override
    public void ringerTick() {
        var entity = getEntity();
        if (entity == null || entity.isRemoved()) {
            playing = false;
            return;
        }
        if (!playing) {
            return;
        }
        var info = ItemMusicCD.getSongInfo(entity.getMusicCD());
        if (info == null) {
            stopInternal();
            return;
        }
        position++;
        if (position >= (long) info.songTime * 20L) {
            position = 0;
            var cd = entity.getMusicCD();
            NetMusicListItem.nextMusic(cd);
            entity.setMusicCD(cd);
            persist();
            requestPlay();
        } else if (position % 100L == 0L) {
            manager.broadcastSync(this);
            persist();
        }
    }

    @Override
    public void onRemove() {
        var entity = getEntity();
        if (entity != null) {
            entity.setPlaying(false);
        }
        persist();
        manager.broadcastStop(this);
    }

    @Override
    public void ringerPersist() {
        persist();
    }

    @Override
    public UUID getRingerUUID() {
        return ringerId;
    }

    @Override
    public ServerLevel getRingerLevel() {
        return level;
    }

    @Override
    public Component getRingerName() {
        return Component.translatable("item.net_music_list.music_player");
    }

    @Override
    public boolean isRingerPlaying() {
        return playing;
    }

    @Override
    public void setRingerPlaying(boolean playing) {
        this.playing = playing;
        var entity = getEntity();
        if (entity != null) {
            entity.setPlaying(playing);
        }
    }

    @Override
    public @Nullable ItemMusicCD.SongInfo getRingerMusic() {
        var entity = getEntity();
        return entity == null ? null : ItemMusicCD.getSongInfo(entity.getMusicCD());
    }

    @Override
    public int getRingerSongTime() {
        var info = getRingerMusic();
        return info == null ? 0 : info.songTime;
    }

    @Override
    public long getRingerPosition() {
        return position;
    }

    @Override
    public void setRingerPosition(long position) {
        this.position = position;
    }

    @Override
    public boolean isRingerLoop() {
        var entity = getEntity();
        return entity != null && entity.getMusicCD().is(com.gly091020.netMusicListNeoforge.NetMusicList.MUSIC_LIST_ITEM.get())
                && com.gly091020.netMusicListNeoforge.item.NetMusicListItem.getPlayMode(entity.getMusicCD())
                == com.gly091020.netMusicListNeoforge.util.PlayMode.LOOP;
    }

    @Override
    public int getRingerEntityId() {
        return entityId;
    }

    @Override
    public @NotNull Vec3 getRingerSpatialPosition() {
        var entity = getEntity();
        return entity == null ? Vec3.ZERO : entity.position();
    }

    @Override
    public float getRingerVolume() {
        return 4.0f;
    }

    @Override
    public boolean exists() {
        var entity = getEntity();
        return entity != null && !entity.isRemoved() && !entity.getMusicCD().isEmpty();
    }
}
