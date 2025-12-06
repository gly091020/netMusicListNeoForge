package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record UpdatePlayerMusicPacket(int index, int slot) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdatePlayerMusicPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "update_player_music_packet"));

    public static final StreamCodec<FriendlyByteBuf, UpdatePlayerMusicPacket> STREAM_CODEC = StreamCodec.of(
            UpdatePlayerMusicPacket::encode,
            UpdatePlayerMusicPacket::decode
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void encode(FriendlyByteBuf buf, UpdatePlayerMusicPacket packet) {
        buf.writeInt(packet.index);
        buf.writeInt(packet.slot);
    }

    public static UpdatePlayerMusicPacket decode(FriendlyByteBuf buf) {
        return new UpdatePlayerMusicPacket(buf.readInt(), buf.readInt());
    }
}
