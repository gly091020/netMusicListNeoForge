package com.gly091020.netMusicListNeoforge.config;

import com.gly091020.netMusicListNeoforge.NetMusicListUtil;
import com.gly091020.netMusicListNeoforge.client.MoveHudScreen;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Optional;

import static com.gly091020.netMusicListNeoforge.NetMusicList.CONFIG;

@OnlyIn(Dist.CLIENT)
public class ConfigScreenGetter {
    public static Screen getConfigScreen(Screen parent){
        var builder = ConfigBuilder.create();
        builder.setParentScreen(parent);
        builder.setTitle(Component.translatable("config.net_music_list.title"));
        var category = builder.getOrCreateCategory(Component.empty());
        var entryBuilder = builder.entryBuilder();

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.net_music_list.enable_etched"),
                        CONFIG.enableEtched).setErrorSupplier((b) -> {
                    if(NetMusicListUtil.hasEtchedExtension() && b){
                        return Optional.of(Component.translatable("config.net_music_list.enable_etched.error"));
                    }
                    return Optional.empty();
                }).setDefaultValue(!NetMusicListUtil.hasEtchedExtension())
                .setSaveConsumer(b -> CONFIG.enableEtched = b).requireRestart().build());
        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.net_music_list.old_gui"),
                        CONFIG.oldGUI).setSaveConsumer(b -> CONFIG.oldGUI = b).setDefaultValue(false)
                .build());
        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.net_music_list.music_hud"),
                        CONFIG.musicHUD).setSaveConsumer(b -> CONFIG.musicHUD = b).setDefaultValue(true)
                .build());

        var buttonWidget = new ButtonEntry(Component.translatable("config.net_music_list.set_hud_pos"),
                (button) -> MoveHudScreen.open());
        buttonWidget.isEnable(CONFIG.musicHUD);
        category.addEntry(buttonWidget);

        return builder.build();
    }
}
