package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record PlayEnderMusicPlayerPacket(BlockPos pos, String url, int timeSecond, String songName) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayEnderMusicPlayerPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "play_ender_music_player_packet"));

    public static final StreamCodec<FriendlyByteBuf, PlayEnderMusicPlayerPacket> STREAM_CODEC = StreamCodec.of(
            PlayEnderMusicPlayerPacket::encode,
            PlayEnderMusicPlayerPacket::decode
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static PlayEnderMusicPlayerPacket decode(FriendlyByteBuf buf) {
        return new PlayEnderMusicPlayerPacket(BlockPos.of(buf.readLong()), buf.readUtf(), buf.readInt(), buf.readUtf());
    }

    public static void encode(FriendlyByteBuf buf, PlayEnderMusicPlayerPacket message) {
        buf.writeLong(message.pos.asLong());
        buf.writeUtf(message.url);
        buf.writeInt(message.timeSecond);
        buf.writeUtf(message.songName);
    }
}
