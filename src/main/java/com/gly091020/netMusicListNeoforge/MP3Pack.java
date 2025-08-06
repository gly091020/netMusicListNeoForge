package com.gly091020.netMusicListNeoforge;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.fml.ModList;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

public class MP3Pack implements RepositorySource {
    private final Pack pack;
    public MP3Pack(){
        Pack.ResourcesSupplier supplier = new PathPackResources.PathResourcesSupplier(ModList.get().getModFileById(NetMusicList.ModID).getFile().getSecureJar().getRootPath().resolve("packs/mp3"));
        pack = new Pack(
                new PackLocationInfo("net_music_list_mp3_pack",
                        Component.translatable("title.net_music_list.mp3"), PackSource.BUILT_IN, Optional.empty()),
                supplier,
                new Pack.Metadata(
                        Component.translatable("description.net_music_list.mp3"), PackCompatibility.COMPATIBLE, FeatureFlagSet.of(), Collections.emptyList(), false
                ),
                new PackSelectionConfig(false, Pack.Position.TOP, false)
        );
    }
    @Override
    public void loadPacks(Consumer<Pack> consumer) {
        consumer.accept(pack);
    }
}
