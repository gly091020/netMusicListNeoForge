package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record DeleteMusicDataPacket(int index) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
            "delete_music_packet");
    public static final Type<DeleteMusicDataPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, DeleteMusicDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            DeleteMusicDataPacket::index,
            DeleteMusicDataPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
