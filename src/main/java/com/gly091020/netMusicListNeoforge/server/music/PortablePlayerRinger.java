package com.gly091020.netMusicListNeoforge.server.music;

import com.github.tartaricacid.netmusic.api.resolver.MusicPlayResolverManager;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.api.IMusicRinger;
import com.gly091020.netMusicListNeoforge.item.NetMusicListItem;
import com.gly091020.netMusicListNeoforge.item.NetMusicPlayerItem;
import com.gly091020.netMusicListNeoforge.util.LoginNeedUtil;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import com.gly091020.netMusicListNeoforge.util.PlayMode;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 背包中的随身播放器对应的服务端 Ringer。
 * 服务端负责计算播放进度、按模式切歌并下发命令包；客户端只负责出声。
 */
public class PortablePlayerRinger implements IMusicRinger {
    private final MusicRingManager manager;
    private final ServerLevel level;
    private final UUID ringerId;
    private final UUID ownerId;
    private boolean playing;
    private long position;
    private int slot = -1;
    private int scanCooldown = 0;
    private int skipGuard = 0;
    private boolean resolving = false;
    private long playSeq = 0;

    public PortablePlayerRinger(MusicRingManager manager, ServerLevel level, UUID ringerId, UUID ownerId) {
        this.manager = manager;
        this.level = level;
        this.ringerId = ringerId;
        this.ownerId = ownerId;
        var state = MusicRingSavedData.get(level).getState(ringerId);
        if (state != null) {
            this.playing = state.playing();
            this.position = state.positionTicks();
        }
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    private @Nullable ServerPlayer getOwner() {
        return level.getServer().getPlayerList().getPlayer(ownerId);
    }

    private @Nullable ItemStack findPlayerItem() {
        var owner = getOwner();
        if (owner == null) {
            slot = -1;
            return null;
        }
        if (++scanCooldown % 10 != 0 && slot >= 0) {
            var cached = owner.getInventory().getItem(slot);
            if (cached.is(NetMusicList.MUSIC_PLAYER_ITEM.get())
                    && ringerId.equals(NetMusicPlayerItem.getRingerId(cached))) {
                return cached;
            }
        }
        scanCooldown = 0;
        var inventory = owner.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var stack = inventory.getItem(i);
            if (stack.is(NetMusicList.MUSIC_PLAYER_ITEM.get())
                    && ringerId.equals(NetMusicPlayerItem.getRingerId(stack))) {
                slot = i;
                return stack;
            }
        }
        slot = -1;
        return null;
    }

    private static ItemStack getInner(ItemStack playerItem) {
        return NetMusicPlayerItem.getContainer(playerItem).getItem(0);
    }

    private @Nullable ItemMusicCD.SongInfo currentInfo(ItemStack playerItem) {
        var inner = getInner(playerItem);
        if (inner.is(InitItems.MUSIC_CD.get())) {
            return ItemMusicCD.getSongInfo(inner);
        }
        if (inner.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            return NetMusicListItem.getSongInfo(inner);
        }
        return null;
    }

    private PlayMode currentMode(ItemStack playerItem) {
        var inner = getInner(playerItem);
        if (inner.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            return NetMusicListItem.getPlayMode(inner);
        }
        return PlayMode.LOOP;
    }

    private void advance(ItemStack playerItem) {
        var inner = getInner(playerItem);
        if (inner.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            NetMusicListItem.nextMusic(inner);
            var container = NetMusicPlayerItem.getContainer(playerItem);
            container.setItem(0, inner);
            container.setChanged();
        }
    }

    private void persist() {
        MusicRingSavedData.get(level).setState(ringerId, playing, (int) position);
    }

    public void play() {
        play(null);
    }

    /**
     * 开始播放。providedInfo 来自客户端意图包（插入后立即发出，不依赖服务端容器时序）；
     * 为空时回退到读取物品容器。
     */
    public void play(@Nullable ItemMusicCD.SongInfo providedInfo) {
        var item = findPlayerItem();
        if (item == null) {
            NetMusicList.LOGGER.info("[Ringer] play() 跳过：找不到物品 ringer={}", ringerId);
            return;
        }
        var info = providedInfo != null ? providedInfo : currentInfo(item);
        if (info == null) {
            NetMusicList.LOGGER.info("[Ringer] play() 跳过：无歌曲信息 ringer={}", ringerId);
            return;
        }
        NetMusicList.LOGGER.info("[Ringer] play() 开始 ringer={} owner={} song={}", ringerId, ownerId, info.songName);
        position = 0;
        playing = true;
        persist();
        requestPlay(info);
    }

    public void stop() {
        playing = false;
        playSeq++;
        persist();
        manager.broadcastStop(this);
    }

    public void next() {
        var item = findPlayerItem();
        if (item == null) {
            return;
        }
        advance(item);
        play();
    }

    public void selectIndex(int index) {
        var item = findPlayerItem();
        if (item == null) {
            return;
        }
        var inner = getInner(item);
        if (inner.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            NetMusicListItem.setSongIndex(inner, index);
            var container = NetMusicPlayerItem.getContainer(item);
            container.setItem(0, inner);
            container.setChanged();
        }
        play();
    }

    public void setMode(PlayMode mode) {
        var item = findPlayerItem();
        if (item == null) {
            return;
        }
        var inner = getInner(item);
        if (inner.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            NetMusicListItem.setPlayMode(inner, mode);
            var container = NetMusicPlayerItem.getContainer(item);
            container.setItem(0, inner);
            container.setChanged();
        }
    }

    /** 重新登录/扫描恢复时，若 SavedData 标记为播放中则继续播放。 */
    public void resume() {
        if (playing) {
            requestPlay(null);
        }
    }

    /** 使在途的 URL 解析失效并重新开始播放当前歌曲。 */
    private void requestPlay(@Nullable ItemMusicCD.SongInfo providedInfo) {
        playSeq++;
        resolving = false;
        playCurrent(providedInfo);
    }

    private void playCurrent(@Nullable ItemMusicCD.SongInfo providedInfo) {
        if (resolving) {
            return;
        }
        var item = findPlayerItem();
        if (item == null || !playing) {
            return;
        }
        var info = providedInfo != null ? providedInfo : currentInfo(item);
        if (info == null) {
            stopInternal();
            return;
        }
        if (NetMusicListUtil.isVipBlocked(info)) {
            if (++skipGuard > 16) {
                stopInternal();
                return;
            }
            advance(item);
            requestPlay(null);
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
                NetMusicList.LOGGER.info("[Ringer] 广播播放命令 ringer={} url={} pos={}", ringerId, resolved.songUrl, position);
                manager.broadcastPlay(this, info, resolved.songUrl, info.songUrl, (int) position, slot);
                persist();
            });
        });
    }

    private void stopInternal() {
        playing = false;
        playSeq++;
        persist();
        manager.broadcastStop(this);
    }

    @Override
    public void ringerTick() {
        if (!playing) {
            return;
        }
        var item = findPlayerItem();
        if (item == null || getInner(item).isEmpty()) {
            stopInternal();
            return;
        }
        var info = currentInfo(item);
        if (info == null) {
            stopInternal();
            return;
        }
        position++;
        if (position >= (long) info.songTime * 20L) {
            position = 0;
            advance(item);
            persist();
            requestPlay(null);
        } else if (position % 100L == 0L) {
            manager.broadcastSync(this);
            persist();
        }
    }

    @Override
    public void onRemove() {
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
    }

    @Override
    public @Nullable ItemMusicCD.SongInfo getRingerMusic() {
        var item = findPlayerItem();
        return item == null ? null : currentInfo(item);
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
        var item = findPlayerItem();
        return item != null && currentMode(item) == PlayMode.LOOP;
    }

    @Override
    public int getRingerEntityId() {
        var owner = getOwner();
        return owner == null ? -1 : owner.getId();
    }

    @Override
    public @NotNull Vec3 getRingerSpatialPosition() {
        var owner = getOwner();
        return owner == null ? Vec3.ZERO : owner.position();
    }

    @Override
    public float getRingerVolume() {
        return 4.0f;
    }

    @Override
    public boolean exists() {
        var item = findPlayerItem();
        return item != null && !getInner(item).isEmpty();
    }
}
