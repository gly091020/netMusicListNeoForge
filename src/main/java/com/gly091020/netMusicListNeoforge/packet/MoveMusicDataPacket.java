package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record MoveMusicDataPacket(int fromIndex, int toIndex) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MoveMusicDataPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "move_music_data_packet"));

    public static final StreamCodec<FriendlyByteBuf, MoveMusicDataPacket> STREAM_CODEC = StreamCodec.of(
            MoveMusicDataPacket::encode,
            MoveMusicDataPacket::decode
    );

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void encode(FriendlyByteBuf buf, MoveMusicDataPacket packet) {
        buf.writeInt(packet.fromIndex);
        buf.writeInt(packet.toIndex);
    }

    public static MoveMusicDataPacket decode(FriendlyByteBuf buf) {
        return new MoveMusicDataPacket(buf.readInt(), buf.readInt());
    }
}
