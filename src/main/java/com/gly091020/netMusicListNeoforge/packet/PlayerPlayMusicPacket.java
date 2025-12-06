package com.gly091020.netMusicListNeoforge.packet;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record PlayerPlayMusicPacket(int playerID, String url, int timeSecond, String songName, int slot, ItemMusicCD.SongInfo info) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayerPlayMusicPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "player_play_music_packet"));

    public static final StreamCodec<FriendlyByteBuf, PlayerPlayMusicPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    PlayerPlayMusicPacket::playerID,
                    ByteBufCodecs.STRING_UTF8,
                    PlayerPlayMusicPacket::url,
                    ByteBufCodecs.INT,
                    PlayerPlayMusicPacket::timeSecond,
                    ByteBufCodecs.STRING_UTF8,
                    PlayerPlayMusicPacket::songName,
                    ByteBufCodecs.INT,
                    PlayerPlayMusicPacket::slot,
                    ItemMusicCD.SongInfo.STREAM_CODEC,
                    PlayerPlayMusicPacket::info,
                    PlayerPlayMusicPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
