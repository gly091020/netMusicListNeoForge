package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record StopMusicPacket(int playerID, String url) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<StopMusicPacket> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(NetMusicList.ModID, "stop_music_packet"));

    public static final StreamCodec<FriendlyByteBuf, StopMusicPacket> STREAM_CODEC = StreamCodec.of(
            StopMusicPacket::encode,
            StopMusicPacket::decode
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void encode(FriendlyByteBuf buf, StopMusicPacket packet) {
        buf.writeInt(packet.playerID);
        buf.writeUtf(packet.url);
    }
    public static StopMusicPacket decode(FriendlyByteBuf buf) {
        return new StopMusicPacket(buf.readInt(), buf.readUtf());
    }
}
