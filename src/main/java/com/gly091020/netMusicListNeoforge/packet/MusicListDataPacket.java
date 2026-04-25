package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.util.PlayMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record MusicListDataPacket(int index, PlayMode playMode) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MusicListDataPacket> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(NetMusicList.ModID, "music_list_data_packet"));

    public static final StreamCodec<FriendlyByteBuf, MusicListDataPacket> STREAM_CODEC = StreamCodec.of(
            MusicListDataPacket::encode,
            MusicListDataPacket::decode
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(FriendlyByteBuf buf, MusicListDataPacket packet) {
        buf.writeInt(packet.index);
        buf.writeInt(packet.playMode.ordinal());
    }

    public static MusicListDataPacket decode(FriendlyByteBuf buf) {
        return new MusicListDataPacket(
                buf.readInt(),
                PlayMode.values()[buf.readInt()]
        );
    }
}
