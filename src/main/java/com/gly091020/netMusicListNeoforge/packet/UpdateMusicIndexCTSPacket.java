package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record UpdateMusicIndexCTSPacket(int slot, int index) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateMusicIndexCTSPacket> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(NetMusicList.ModID, "update_music_index_cts_packet"));

    public static final StreamCodec<FriendlyByteBuf, UpdateMusicIndexCTSPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            UpdateMusicIndexCTSPacket::slot,
            ByteBufCodecs.INT,
            UpdateMusicIndexCTSPacket::index,
            UpdateMusicIndexCTSPacket::new
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
