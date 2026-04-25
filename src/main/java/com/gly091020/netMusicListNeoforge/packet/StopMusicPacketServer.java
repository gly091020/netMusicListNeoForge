package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record StopMusicPacketServer(int playerID, String url) implements CustomPacketPayload {
    // 玩家间的背包是不完全同步的！
    public static final CustomPacketPayload.Type<StopMusicPacketServer> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(NetMusicList.ModID, "stop_music_packet_server"));

    public static final StreamCodec<FriendlyByteBuf, StopMusicPacketServer> STREAM_CODEC = StreamCodec.of(
            StopMusicPacketServer::encode,
            StopMusicPacketServer::decode
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(FriendlyByteBuf buf, StopMusicPacketServer packet) {
        buf.writeInt(packet.playerID);
        buf.writeUtf(packet.url);
    }
    public static StopMusicPacketServer decode(FriendlyByteBuf buf) {
        return new StopMusicPacketServer(buf.readInt(), buf.readUtf());
    }
}
