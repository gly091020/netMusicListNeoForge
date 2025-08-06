package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.PlayMode;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record MusicListDataPacket(int index, PlayMode playMode) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
            "music_list_data_packet");
    public static final Type<MusicListDataPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<ByteBuf, MusicListDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            MusicListDataPacket::index,
            ByteBufCodecs.idMapper(PlayMode::getMode, Enum::ordinal),
            MusicListDataPacket::playMode,
            MusicListDataPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
