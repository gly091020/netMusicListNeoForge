package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * S2C：命令客户端停止某个 Ringer 的播放。
 */
public record MusicStopCommandPacket(UUID ringerId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MusicStopCommandPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "music_stop_command"));

    public static final StreamCodec<FriendlyByteBuf, MusicStopCommandPacket> STREAM_CODEC =
            StreamCodec.of(MusicStopCommandPacket::encode, MusicStopCommandPacket::decode);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(FriendlyByteBuf buf, MusicStopCommandPacket packet) {
        buf.writeUUID(packet.ringerId);
    }

    public static MusicStopCommandPacket decode(FriendlyByteBuf buf) {
        return new MusicStopCommandPacket(buf.readUUID());
    }
}
