package com.gly091020.netMusicListNeoforge.sounds;

import com.github.tartaricacid.netmusic.client.audio.NetMusicAudioStream;
import com.github.tartaricacid.netmusic.init.InitSounds;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.entity.MusicPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PlayerEntityNetMusicSound extends AbstractTickableSoundInstance {
    private final Entity entity;
    private final String rawUrl;
    private final URL url;
    private int tick = 0;
    private final int countTick;
    public PlayerEntityNetMusicSound(int entityID, String rawUrl, URL url, int second) {
        super(InitSounds.NET_MUSIC.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.rawUrl = rawUrl;
        this.url = url;
        this.countTick = second * 20;
        if (Minecraft.getInstance().level != null) {
            entity = Minecraft.getInstance().level.getEntity(entityID);
        }else{
            entity = null;
            stop();
        }
        if(entity != null){
            this.x = entity.getX();
            this.y = entity.getY();
            this.z = entity.getZ();
        }
        this.volume = 4;
    }

    @Override
    public void tick() {
        Level world = Minecraft.getInstance().level;
        if(world == null){stop();return;}

        if(entity == null){stop();return;}
        if(entity.isRemoved()){stop();return;}
        if(!(entity instanceof MusicPlayerEntity musicPlayer)){stop();return;}

        tick++;
        if (this.tick > this.countTick + 50) {
            this.stop();
        } else if (!isStopped() && world.getGameTime() % 8L == 0L) {
            for(int i = 0; i < 2; ++i) {
                world.addParticle(ParticleTypes.NOTE, this.x - (double)0.5F + world.getRandom().nextDouble(), this.y + world.getRandom().nextDouble() + (double)1.0F, this.z - (double)0.5F + world.getRandom().nextDouble(), world.getRandom().nextGaussian(), world.getRandom().nextGaussian(), world.getRandom().nextInt(3));
            }
        }
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
        if(this.tick > 40) {
            if(!musicPlayer.isPlaying()){stop();return;}
            var songUrl = musicPlayer.getEntityData().get(MusicPlayerEntity.DATA_RAW_URL);
            if(!Objects.equals(songUrl, rawUrl)){stop();}
        }
    }

    @Override
    public @NotNull CompletableFuture<AudioStream> getStream(@NotNull SoundBufferLibrary soundBuffers, @NotNull Sound sound, boolean looping) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new NetMusicAudioStream(url);
            } catch (UnsupportedAudioFileException | IOException e) {
                NetMusicList.LOGGER.error("出现错误：", e);
                return null;
            }
        }, Util.backgroundExecutor());
    }
}
