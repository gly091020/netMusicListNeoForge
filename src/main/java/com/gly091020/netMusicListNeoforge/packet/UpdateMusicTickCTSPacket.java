package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record UpdateMusicTickCTSPacket(int slot, int tick) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateMusicTickCTSPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "update_music_tick_cts_packet"));

    public static final StreamCodec<FriendlyByteBuf, UpdateMusicTickCTSPacket> STREAM_CODEC = StreamCodec.of(
            UpdateMusicTickCTSPacket::encode,
            UpdateMusicTickCTSPacket::decode
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void encode(FriendlyByteBuf buf, UpdateMusicTickCTSPacket packet) {
        buf.writeInt(packet.slot);
        buf.writeInt(packet.tick);
    }

    public static UpdateMusicTickCTSPacket decode(FriendlyByteBuf buf) {
        return new UpdateMusicTickCTSPacket(
                buf.readInt(), buf.readInt()
        );
    }
}
