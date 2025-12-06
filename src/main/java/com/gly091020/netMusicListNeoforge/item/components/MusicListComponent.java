package com.gly091020.netMusicListNeoforge.item.components;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.util.PlayMode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.List;

public record MusicListComponent(List<ItemMusicCD.SongInfo> songInfos, PlayMode playMode, Integer index) {
    public static final Codec<MusicListComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(ItemMusicCD.SongInfo.CODEC).fieldOf("song_list").forGetter(MusicListComponent::songInfos),
                    StringRepresentable.fromEnum(PlayMode::values).fieldOf("play_mode").forGetter(MusicListComponent::playMode),
                    Codec.INT.fieldOf("index").forGetter(MusicListComponent::index)
            ).apply(instance, MusicListComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MusicListComponent> STREAM_CODEC = StreamCodec.composite(
            ItemMusicCD.SongInfo.STREAM_CODEC.apply(ByteBufCodecs.list()),
            MusicListComponent::songInfos,
            ByteBufCodecs.INT.map(
                    PlayMode::getMode,
                    PlayMode::ordinal
            ),
            MusicListComponent::playMode,
            ByteBufCodecs.INT,
            MusicListComponent::index,
            MusicListComponent::new
    );

    public static MusicListComponent getInstance(){
        return new MusicListComponent(List.of(), PlayMode.LOOP, 0);
    }
}
