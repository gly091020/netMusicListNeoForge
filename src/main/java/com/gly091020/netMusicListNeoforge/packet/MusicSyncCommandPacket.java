package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * S2C：周期性同步服务端权威的播放进度（tick），客户端 HUD 据此显示。
 */
public record MusicSyncCommandPacket(UUID ringerId, int positionTick) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MusicSyncCommandPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "music_sync_command"));

    public static final StreamCodec<FriendlyByteBuf, MusicSyncCommandPacket> STREAM_CODEC =
            StreamCodec.of(MusicSyncCommandPacket::encode, MusicSyncCommandPacket::decode);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(FriendlyByteBuf buf, MusicSyncCommandPacket packet) {
        buf.writeUUID(packet.ringerId);
        buf.writeInt(packet.positionTick);
    }

    public static MusicSyncCommandPacket decode(FriendlyByteBuf buf) {
        return new MusicSyncCommandPacket(buf.readUUID(), buf.readInt());
    }
}
