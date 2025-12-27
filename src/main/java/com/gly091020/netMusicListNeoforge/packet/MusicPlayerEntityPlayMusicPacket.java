package com.gly091020.netMusicListNeoforge.packet;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record MusicPlayerEntityPlayMusicPacket(int entityID, String url, int timeSecond, String songName) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MusicPlayerEntityPlayMusicPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "music_player_entity_play_music_packet"));

    public static final StreamCodec<FriendlyByteBuf, MusicPlayerEntityPlayMusicPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            MusicPlayerEntityPlayMusicPacket::entityID,
            ByteBufCodecs.STRING_UTF8,
            MusicPlayerEntityPlayMusicPacket::url,
            ByteBufCodecs.INT,
            MusicPlayerEntityPlayMusicPacket::timeSecond,
            ByteBufCodecs.STRING_UTF8,
            MusicPlayerEntityPlayMusicPacket::songName,
            MusicPlayerEntityPlayMusicPacket::new
    );
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
