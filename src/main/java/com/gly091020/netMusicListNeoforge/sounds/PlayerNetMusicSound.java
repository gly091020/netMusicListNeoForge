// 部分参考MaidNetMusicSound，以保持与女仆行为一致
package com.gly091020.netMusicListNeoforge.sounds;

import com.github.tartaricacid.netmusic.client.audio.NetMusicAudioStream;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.init.InitSounds;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.hud.MusicInfoHud;
import com.gly091020.netMusicListNeoforge.item.NetMusicListItem;
import com.gly091020.netMusicListNeoforge.item.NetMusicPlayerItem;
import com.gly091020.netMusicListNeoforge.packet.StopMusicPacketServer;
import com.gly091020.netMusicListNeoforge.packet.UpdateMusicIndexCTSPacket;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PlayerNetMusicSound extends AbstractTickableSoundInstance {
    final Player player;
    final URL url;
    final int countTick;
    int tick = 0;
    final int slot;

    @Nullable
    String clientUrl;

    public PlayerNetMusicSound(Player player, URL songUrl, int second, int slot) {
        super(InitSounds.NET_MUSIC.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.url = songUrl;
        this.countTick = second * 20;
        this.volume = 4f;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        this.slot = slot;

        // 我终于搞清楚relative怎么用了
        // 不用relative会导致声音不稳定
        if(isClientPlayer()){
            attenuation = Attenuation.NONE;
            relative = true;
            x = 0;
            y = 0;
            z = 0;
        }else{
            relative = false;
            attenuation = Attenuation.LINEAR;
        }
    }

    @Override
    public void tick() {
        if(player.isRemoved()){
            stopMusic();
        }

        if(tick > countTick){
            var musicPlayer = player.getInventory().getItem(slot);
            if(!isStopped() && isClientPlayer() && musicPlayer.is(NetMusicList.MUSIC_PLAYER_ITEM) &&
                    NetMusicPlayerItem.getContainer(musicPlayer).getItem(0).is(NetMusicList.MUSIC_LIST_ITEM)) {
                var c = NetMusicPlayerItem.getContainer(musicPlayer);
                var i = NetMusicListItem.nextMusic(c.getItem(0));
                c.setChanged();
                NetworkHandler.sendToServer(new UpdateMusicIndexCTSPacket(slot, i));
                NetMusicPlayerItem.playSound(musicPlayer, player, slot);
                MusicInfoHud.setInfo(NetMusicListItem.getSongInfo(c.getItem(0)), c.getItem(0), slot);
            }
            stopMusic();
            return;
        }

        if(isClientPlayer()){
            var itemStack = player.getInventory().getItem(slot);
            if (!itemStack.is(NetMusicList.MUSIC_PLAYER_ITEM.get())) {
                stopMusic();
            }
            itemStack = NetMusicPlayerItem.getContainer(itemStack).getItem(0);
            if (itemStack.is(InitItems.MUSIC_CD.get())) {
                if (ItemMusicCD.getSongInfo(itemStack) == null) {
                    stopMusic();
                }
            } else if (itemStack.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
                var info = NetMusicListItem.getSongInfo(itemStack);
                if (info == null) {
                    stopMusic();
                } else {
                    if (clientUrl == null) clientUrl = info.songUrl;
                    if (!Objects.equals(clientUrl, info.songUrl)) {
                        stopMusic();
                    }
                }
            } else {
                stopMusic();
            }
        }

        ClientLevel level = Minecraft.getInstance().level;
        if(level == null){
            stopMusic();
        }else{
            ++this.tick;
            if (this.tick >= this.countTick+200){ //这个给你留着做纪念意义吧，虽然可能永远都执行不了了
                stopMusic();
            }else{
                if(!isClientPlayer()){
                    // 之前声音偏左的问题是两个bug的结合，不愧是我
                    this.x = player.getX();
                    this.y = player.getY();
                    this.z = player.getZ();
                }
                if (level.getGameTime() % 8L == 0L) {
                    for(int i = 0; i < 2; ++i) {
                        level.addParticle(ParticleTypes.NOTE, player.getX() - (double)0.5F + level.random.nextDouble(), player.getY() + (double)2F + level.random.nextDouble(), player.getZ() - (double)0.5F + level.random.nextDouble(), level.random.nextGaussian(), level.random.nextGaussian(), level.random.nextInt(3));
                    }
                }
            }
        }

        if(NetMusicListUtil.globalStopMusic){
            this.volume = 0;
        }else{
            this.volume = 4f;
        }
    }

    public void stopMusic(){
        if(!isStopped() && isClientPlayer()){
            NetworkHandler.sendToServer(new StopMusicPacketServer(player.getId(), url.toString()));
        }
        stop();
    }

    public void onlyTickUpdate(){
        tick++;
    }

    public int getTick(){
        return Math.clamp(countTick - tick, 0, countTick);
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

    public boolean isClientPlayer(){
        if (Minecraft.getInstance().player != null) {
            return player.getUUID() == Minecraft.getInstance().player.getUUID();
        }
        return false;
    }

    public Player getPlayer() {
        return player;
    }
}
