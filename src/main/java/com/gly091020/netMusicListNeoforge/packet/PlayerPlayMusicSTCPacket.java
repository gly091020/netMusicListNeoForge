package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record PlayerPlayMusicSTCPacket(int playerID, String url, int timeSecond, String songName, int slot) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
            "player_play_music_stc_packet");
    public static Type<PlayerPlayMusicSTCPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<ByteBuf, PlayerPlayMusicSTCPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            PlayerPlayMusicSTCPacket::playerID,
            ByteBufCodecs.STRING_UTF8,
            PlayerPlayMusicSTCPacket::url,
            ByteBufCodecs.INT,
            PlayerPlayMusicSTCPacket::timeSecond,
            ByteBufCodecs.STRING_UTF8,
            PlayerPlayMusicSTCPacket::songName,
            ByteBufCodecs.INT,
            PlayerPlayMusicSTCPacket::slot,
            PlayerPlayMusicSTCPacket::new
    );
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(playerID);
        buf.writeUtf(url);
        buf.writeInt(timeSecond);
        buf.writeUtf(songName);
        buf.writeInt(slot);
    }

    public static PlayerPlayMusicSTCPacket decode(FriendlyByteBuf buf) {
        return new PlayerPlayMusicSTCPacket(
                buf.readInt(), buf.readUtf(), buf.readInt(), buf.readUtf(), buf.readInt()
        );
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
