// 客户端哑播放器：只负责出声，播放/停止/进度全部由服务端 Ringer 通过命令包控制
package com.gly091020.netMusicListNeoforge.sounds;

import com.github.tartaricacid.netmusic.client.audio.NetMusicAudioStream;
import com.github.tartaricacid.netmusic.init.InitSounds;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.util.MusicManager;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RingerSound extends AbstractTickableSoundInstance {
    private final UUID ringerId;
    private final Entity entity;
    private final boolean self;
    private final URL url;
    private final String rawUrl;
    private final int startTick;
    private final int songTimeTicks;
    private final int slot;
    private final ItemMusicCD.SongInfo info;
    private int tick = 0;

    public RingerSound(UUID ringerId, Entity entity, URL url, String rawUrl, int startTick,
                       int songTimeTicks, int slot, ItemMusicCD.SongInfo info) {
        super(InitSounds.NET_MUSIC.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.ringerId = ringerId;
        this.entity = entity;
        this.url = url;
        this.rawUrl = rawUrl;
        this.startTick = startTick;
        this.songTimeTicks = songTimeTicks;
        this.slot = slot;
        this.info = info;
        this.volume = 4.0f;
        this.self = Minecraft.getInstance().player != null
                && entity.getUUID().equals(Minecraft.getInstance().player.getUUID());
        if (self) {
            this.attenuation = Attenuation.NONE;
            this.relative = true;
            this.x = 0;
            this.y = 0;
            this.z = 0;
        } else {
            this.attenuation = Attenuation.LINEAR;
            this.relative = false;
            this.x = (float) entity.getX();
            this.y = (float) entity.getY();
            this.z = (float) entity.getZ();
        }
    }

    @Override
    public void tick() {
        var level = Minecraft.getInstance().level;
        if (level == null || entity == null || entity.isRemoved()) {
            stop();
            return;
        }
        tick++;
        if (!self) {
            this.x = (float) entity.getX();
            this.y = (float) entity.getY();
            this.z = (float) entity.getZ();
            if (level.getGameTime() % 8L == 0L) {
                for (int i = 0; i < 2; ++i) {
                    level.addParticle(ParticleTypes.NOTE,
                            this.x - 0.5f + level.random.nextDouble(),
                            this.y + 2.0f + level.random.nextDouble(),
                            this.z - 0.5f + level.random.nextDouble(),
                            level.random.nextGaussian(), level.random.nextGaussian(), level.random.nextInt(3));
                }
            }
        }
        this.volume = NetMusicListUtil.globalStopMusic ? 0.0f : 4.0f;
    }

    /** 当前播放位置（tick）：服务端同步值加本地平滑推进，否则纯本地估算。 */
    public int getPositionTick() {
        var serverPosition = MusicManager.getServerPosition(ringerId);
        return serverPosition != null ? serverPosition : getLocalPositionTick();
    }

    /** 纯本地估算位置（tick），不包含服务端校准。 */
    public int getLocalPositionTick() {
        return startTick + tick;
    }

    /** 剩余 tick，供 HUD 进度显示（与旧接口保持一致）。 */
    public int getTick() {
        return Math.max(0, songTimeTicks - getPositionTick());
    }

    @Override
    public @NotNull CompletableFuture<AudioStream> getStream(@NotNull SoundBufferLibrary soundBuffers,
                                                             @NotNull Sound sound, boolean looping) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (NetMusicList.CONFIG.forceFFmpeg) {
                    FFmpegAudioStream.applyConfig();
                    return new FFmpegAudioStream(url);
                }
                return new NetMusicAudioStream(url);
            } catch (Exception e) {
                NetMusicList.LOGGER.error("出现错误：", e);
                return null;
            }
        }, Util.backgroundExecutor());
    }

    public UUID getRingerId() {
        return ringerId;
    }

    public boolean isSelf() {
        return self;
    }

    public int getSlot() {
        return slot;
    }

    public String getRawUrl() {
        return rawUrl;
    }

    public ItemMusicCD.SongInfo getInfo() {
        return info;
    }

    /** 公开的停止入口，供 MusicManager 调用。 */
    public void stopMusic() {
        stop();
    }
}
