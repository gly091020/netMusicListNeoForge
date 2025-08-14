package com.gly091020.netMusicListNeoforge.packet;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record PlayerPlayMusicSTCPacket(int playerID, String playUrl, ItemMusicCD.SongInfo info, int slot, String uuid) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
            "player_play_music_stc_packet");
    public static Type<PlayerPlayMusicSTCPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<ByteBuf, PlayerPlayMusicSTCPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            PlayerPlayMusicSTCPacket::playerID,
            ByteBufCodecs.STRING_UTF8,
            PlayerPlayMusicSTCPacket::playUrl,
            ItemMusicCD.SongInfo.STREAM_CODEC,
            PlayerPlayMusicSTCPacket::info,
            ByteBufCodecs.INT,
            PlayerPlayMusicSTCPacket::slot,
            ByteBufCodecs.STRING_UTF8,
            PlayerPlayMusicSTCPacket::uuid,
            PlayerPlayMusicSTCPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
