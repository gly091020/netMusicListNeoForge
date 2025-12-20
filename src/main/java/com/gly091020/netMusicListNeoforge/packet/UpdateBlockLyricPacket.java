package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record UpdateBlockLyricPacket(BlockPos pos, NetMusicListUtil.Lyric lyric) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateBlockLyricPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "update_block_lyric_packet"));
    public static final StreamCodec<FriendlyByteBuf, UpdateBlockLyricPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            UpdateBlockLyricPacket::pos,
            NetMusicListUtil.Lyric.STREAM_CODEC,
            UpdateBlockLyricPacket::lyric,
            UpdateBlockLyricPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
