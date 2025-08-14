package com.gly091020.netMusicListNeoforge.etched;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.google.gson.Gson;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import gg.moonflower.etched.api.record.TrackData;
import gg.moonflower.etched.api.sound.download.SoundSourceManager;
import gg.moonflower.etched.common.component.MusicTrackComponent;
import gg.moonflower.etched.core.registry.EtchedComponents;
import gg.moonflower.etched.core.registry.EtchedItems;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class EtchedRegistry {
    public static void registry(){
        SoundSourceManager.registerSource(new NetDownloadSource());
    }

    public static int idToItem(CommandContext<CommandSourceStack> context){
        try{
            var player = context.getSource().getPlayer();
            if(player == null){return 0;}
            var stack = new ItemStack(EtchedItems.ETCHED_MUSIC_DISC.asItem());
            var info = MusicListManage.get163Song(LongArgumentType.getLong(context, "id"));
            var artist = new StringBuilder();
            for (String a : info.artists) {
                artist.append(a);
                artist.append("、");
            }
            var as = artist.toString();
            stack.set(EtchedComponents.MUSIC.get(), new MusicTrackComponent(List.of(new TrackData(info.songUrl, as.substring(0, as.length() - 1), Component.literal(info.songName)))));
            player.addItem(stack);
            context.getSource().sendSuccess(() -> Component.translatable("command.net_music_list.success", info.songName),
                    false);
            return 1;
        } catch (Exception e) {
            NetMusic.LOGGER.error("出现错误：", e);
            context.getSource().sendFailure(Component.translatable("command.net_music_list.fail"));
        }
        return 0;
    }
}
