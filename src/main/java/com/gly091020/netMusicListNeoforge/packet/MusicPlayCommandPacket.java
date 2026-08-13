package com.gly091020.netMusicListNeoforge.packet;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * S2C：命令客户端开始/替换某个 Ringer 的播放。URL 已由服务端解析完成，客户端不得再解析。
 */
public record MusicPlayCommandPacket(UUID ringerId, int entityId, String url, String rawUrl,
                                     int startTick, int songTime, String songName, int slot,
                                     ItemMusicCD.SongInfo info) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MusicPlayCommandPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "music_play_command"));

    public static final StreamCodec<FriendlyByteBuf, MusicPlayCommandPacket> STREAM_CODEC =
            StreamCodec.of(MusicPlayCommandPacket::encode, MusicPlayCommandPacket::decode);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(FriendlyByteBuf buf, MusicPlayCommandPacket packet) {
        buf.writeUUID(packet.ringerId);
        buf.writeInt(packet.entityId);
        buf.writeUtf(packet.url);
        buf.writeUtf(packet.rawUrl);
        buf.writeInt(packet.startTick);
        buf.writeInt(packet.songTime);
        buf.writeUtf(packet.songName);
        buf.writeInt(packet.slot);
        ItemMusicCD.SongInfo.STREAM_CODEC.encode(buf, packet.info);
    }

    public static MusicPlayCommandPacket decode(FriendlyByteBuf buf) {
        return new MusicPlayCommandPacket(buf.readUUID(), buf.readInt(), buf.readUtf(), buf.readUtf(),
                buf.readInt(), buf.readInt(), buf.readUtf(), buf.readInt(),
                ItemMusicCD.SongInfo.STREAM_CODEC.decode(buf));
    }
}
