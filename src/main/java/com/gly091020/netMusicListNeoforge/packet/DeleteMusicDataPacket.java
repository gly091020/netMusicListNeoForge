package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record DeleteMusicDataPacket(int index) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DeleteMusicDataPacket> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(NetMusicList.ModID, "delete_music_data_packet"));

    public static final StreamCodec<FriendlyByteBuf, DeleteMusicDataPacket> STREAM_CODEC = StreamCodec.of(
            DeleteMusicDataPacket::encode,
            DeleteMusicDataPacket::decode
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(FriendlyByteBuf buf, DeleteMusicDataPacket packet) {
        buf.writeInt(packet.index);
    }

    public static DeleteMusicDataPacket decode(FriendlyByteBuf buf) {
        return new DeleteMusicDataPacket(
                buf.readInt()
        );
    }
}
