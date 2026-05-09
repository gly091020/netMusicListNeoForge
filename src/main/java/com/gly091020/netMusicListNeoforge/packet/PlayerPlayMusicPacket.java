package com.gly091020.netMusicListNeoforge.packet;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.mojang.datafixers.util.Function6;
import com.mojang.datafixers.util.Function7;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record PlayerPlayMusicPacket(int playerID, String url, String rawUrl, int timeSecond, String songName, int slot, ItemMusicCD.SongInfo info) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayerPlayMusicPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "player_play_music_packet"));

    public static final StreamCodec<FriendlyByteBuf, PlayerPlayMusicPacket> STREAM_CODEC =
            composite7(
                    ByteBufCodecs.INT,
                    PlayerPlayMusicPacket::playerID,
                    ByteBufCodecs.STRING_UTF8,
                    PlayerPlayMusicPacket::url,
                    ByteBufCodecs.STRING_UTF8,
                    PlayerPlayMusicPacket::rawUrl,
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

    // codec都写不下了……
    static <B, C, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, C> composite7(final StreamCodec<? super B, T1> codec1, final Function<C, T1> getter1, final StreamCodec<? super B, T2> codec2, final Function<C, T2> getter2, final StreamCodec<? super B, T3> codec3, final Function<C, T3> getter3, final StreamCodec<? super B, T4> codec4, final Function<C, T4> getter4, final StreamCodec<? super B, T5> codec5, final Function<C, T5> getter5, final StreamCodec<? super B, T6> codec6, final Function<C, T6> getter6, final StreamCodec<? super B, T7> codec7, final Function<C, T7> getter7, final Function7<T1, T2, T3, T4, T5, T6, T7, C> factory) {
        return new StreamCodec<>() {
            public @NotNull C decode(@NotNull B p_330310_) {
                T1 t1 = codec1.decode(p_330310_);
                T2 t2 = codec2.decode(p_330310_);
                T3 t3 = codec3.decode(p_330310_);
                T4 t4 = codec4.decode(p_330310_);
                T5 t5 = codec5.decode(p_330310_);
                T6 t6 = codec6.decode(p_330310_);
                T7 t7 = codec7.decode(p_330310_);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7);
            }

            public void encode(@NotNull B p_332052_, @NotNull C p_331912_) {
                codec1.encode(p_332052_, getter1.apply(p_331912_));
                codec2.encode(p_332052_, getter2.apply(p_331912_));
                codec3.encode(p_332052_, getter3.apply(p_331912_));
                codec4.encode(p_332052_, getter4.apply(p_331912_));
                codec5.encode(p_332052_, getter5.apply(p_331912_));
                codec6.encode(p_332052_, getter6.apply(p_331912_));
                codec7.encode(p_332052_, getter7.apply(p_331912_));
            }
        };
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
