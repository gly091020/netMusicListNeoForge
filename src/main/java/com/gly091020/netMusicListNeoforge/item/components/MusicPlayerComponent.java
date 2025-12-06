package com.gly091020.netMusicListNeoforge.item.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MusicPlayerComponent(int tick, int shadowTick, int waitSong) {
    public static final Codec<MusicPlayerComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("tick").forGetter(MusicPlayerComponent::tick),
                    Codec.INT.fieldOf("shadow_tick").forGetter(MusicPlayerComponent::shadowTick),
                    Codec.INT.fieldOf("wait_song").forGetter(MusicPlayerComponent::waitSong)
            ).apply(instance, MusicPlayerComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MusicPlayerComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            MusicPlayerComponent::tick,
            ByteBufCodecs.INT,
            MusicPlayerComponent::shadowTick,
            ByteBufCodecs.INT,
            MusicPlayerComponent::waitSong,
            MusicPlayerComponent::new
    );

    public static MusicPlayerComponent getInstance(){
        return new MusicPlayerComponent(-1, 0, 0);
    }
}
