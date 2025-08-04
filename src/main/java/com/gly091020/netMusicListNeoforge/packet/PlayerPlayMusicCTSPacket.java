package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record PlayerPlayMusicCTSPacket(int playerID, String url, int timeSecond, String songName, int slot) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
            "player_play_music_cts_packet");
    public static Type<PlayerPlayMusicCTSPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<ByteBuf, PlayerPlayMusicCTSPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            PlayerPlayMusicCTSPacket::playerID,
            ByteBufCodecs.STRING_UTF8,
            PlayerPlayMusicCTSPacket::url,
            ByteBufCodecs.INT,
            PlayerPlayMusicCTSPacket::timeSecond,
            ByteBufCodecs.STRING_UTF8,
            PlayerPlayMusicCTSPacket::songName,
            ByteBufCodecs.INT,
            PlayerPlayMusicCTSPacket::slot,
            PlayerPlayMusicCTSPacket::new
    );
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(playerID);
        buf.writeUtf(url);
        buf.writeInt(timeSecond);
        buf.writeUtf(songName);
        buf.writeInt(slot);
    }

    public static PlayerPlayMusicCTSPacket decode(FriendlyByteBuf buf) {
        return new PlayerPlayMusicCTSPacket(
                buf.readInt(), buf.readUtf(), buf.readInt(), buf.readUtf(), buf.readInt()
        );
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
