package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record MoveMusicDataPacket(int fromIndex, int toIndex) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
            "move_music_packet");
    public static final Type<MoveMusicDataPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<ByteBuf, MoveMusicDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            MoveMusicDataPacket::fromIndex,
            ByteBufCodecs.INT,
            MoveMusicDataPacket::toIndex,
            MoveMusicDataPacket::new
    );
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(fromIndex);
        buf.writeInt(toIndex);
    }

    public static MoveMusicDataPacket decode(FriendlyByteBuf buf) {
        return new MoveMusicDataPacket(buf.readInt(), buf.readInt());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
