package com.gly091020.netMusicListNeoforge.create;

import com.github.tartaricacid.netmusic.NetMusic;
import com.gly091020.netMusicListNeoforge.create.musicPlayerSource.LyricSource;
import com.gly091020.netMusicListNeoforge.create.musicPlayerSource.MusicIndexSource;
import com.gly091020.netMusicListNeoforge.create.musicPlayerSource.MusicInfoSource;
import com.gly091020.netMusicListNeoforge.create.musicPlayerSource.PlayerProgressSource;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

import static com.gly091020.netMusicListNeoforge.NetMusicList.ModID;

public class CreateRegistry {
    public static final CreateRegistrate REGISTRIES = CreateRegistrate.create(ModID);
    public static void registry(IEventBus modEventBus){
        REGISTRIES.displaySource("music_info", MusicInfoSource::new)
                .onRegisterAfter(Registries.BLOCK_ENTITY_TYPE, CreateRegistry::afterRegistry)
                .register();
        REGISTRIES.displaySource("player_progress", PlayerProgressSource::new)
                .onRegisterAfter(Registries.BLOCK_ENTITY_TYPE, CreateRegistry::afterRegistry)
                .register();
        REGISTRIES.displaySource("music_index", MusicIndexSource::new)
                .onRegisterAfter(Registries.BLOCK_ENTITY_TYPE, CreateRegistry::afterRegistry)
                .register();
        REGISTRIES.displaySource("lyric", LyricSource::new)
                .onRegisterAfter(Registries.BLOCK_ENTITY_TYPE, CreateRegistry::afterRegistry)
                .register();
        REGISTRIES.registerEventListeners(modEventBus);
        modEventBus.addListener(CreateRegistry::onRegistry);
    }

    private static void afterRegistry(DisplaySource source){
        BlockEntityType<?> type = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "music_player"));
        DisplaySource.BY_BLOCK_ENTITY.add(type, source);
    }

    public static void onRegistry(RegisterEvent event){
        MusicPlayerPointType.init();
    }
}
