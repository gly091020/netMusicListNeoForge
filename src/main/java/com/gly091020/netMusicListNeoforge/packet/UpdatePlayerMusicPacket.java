package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UpdatePlayerMusicPacket(int index, int slot, String uuid) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
            "update_player_music_packet");
    public static Type<UpdatePlayerMusicPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<ByteBuf, UpdatePlayerMusicPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            UpdatePlayerMusicPacket::index,
            ByteBufCodecs.INT,
            UpdatePlayerMusicPacket::slot,
            ByteBufCodecs.STRING_UTF8,
            UpdatePlayerMusicPacket::uuid,
            UpdatePlayerMusicPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
