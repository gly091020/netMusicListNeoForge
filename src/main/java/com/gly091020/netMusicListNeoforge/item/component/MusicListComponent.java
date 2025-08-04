package com.gly091020.netMusicListNeoforge.item.component;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.PlayMode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.NonNullList;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.List;

public record MusicListComponent(List<ItemMusicCD.SongInfo> songList, PlayMode playMode, int index) {
    public static final StreamCodec<ByteBuf, MusicListComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(NonNullList::createWithCapacity, ItemMusicCD.SongInfo.STREAM_CODEC),
            MusicListComponent::songList,
            ByteBufCodecs.idMapper(PlayMode::getMode, PlayMode::ordinal),
            MusicListComponent::playMode,
            ByteBufCodecs.INT,
            MusicListComponent::index,
            MusicListComponent::new
    );
    public static final Codec<MusicListComponent> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.list(ItemMusicCD.SongInfo.CODEC).fieldOf("song_list").forGetter(MusicListComponent::songList),
                StringRepresentable.fromEnum(PlayMode::values).fieldOf("play_mode").forGetter(MusicListComponent::playMode),
                Codec.INT.fieldOf("index").forGetter(MusicListComponent::index)
        ).apply(instance, MusicListComponent::new)
    );

    public static MusicListComponent getDefault(){
        return new MusicListComponent(List.of(), PlayMode.LOOP, 0);
    }
}
