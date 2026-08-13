package com.gly091020.netMusicListNeoforge.packet;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.util.PlayMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * C2S：玩家对随身播放器发出的操作意图，由服务端 Ringer 执行。
 * PLAY 时携带歌曲信息，避免依赖容器点击包的处理时序（客户端预测阶段容器已写入）。
 */
public record MusicPlayerActionPacket(Action action, int slot, int index, PlayMode mode,
                                      @Nullable ItemMusicCD.SongInfo info) implements CustomPacketPayload {
    public enum Action {
        PLAY, STOP, NEXT, SELECT_INDEX, SET_MODE
    }

    public static final CustomPacketPayload.Type<MusicPlayerActionPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "music_player_action"));

    public static final StreamCodec<FriendlyByteBuf, MusicPlayerActionPacket> STREAM_CODEC =
            StreamCodec.of(MusicPlayerActionPacket::encode, MusicPlayerActionPacket::decode);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(FriendlyByteBuf buf, MusicPlayerActionPacket packet) {
        buf.writeUtf(packet.action.name());
        buf.writeInt(packet.slot);
        buf.writeInt(packet.index);
        buf.writeUtf(packet.mode.name());
        buf.writeBoolean(packet.info != null);
        if (packet.info != null) {
            ItemMusicCD.SongInfo.STREAM_CODEC.encode(buf, packet.info);
        }
    }

    public static MusicPlayerActionPacket decode(FriendlyByteBuf buf) {
        var action = Action.valueOf(buf.readUtf());
        var slot = buf.readInt();
        var index = buf.readInt();
        var mode = PlayMode.valueOf(buf.readUtf());
        ItemMusicCD.SongInfo info = buf.readBoolean() ? ItemMusicCD.SongInfo.STREAM_CODEC.decode(buf) : null;
        return new MusicPlayerActionPacket(action, slot, index, mode, info);
    }
}
