package com.gly091020.netMusicListNeoforge.util;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.flag.FeatureFlagSet;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

public class MP3Pack implements RepositorySource {
    private final Pack pack;
    public MP3Pack(){
        Pack.ResourcesSupplier supplier = getLegacyPack();
        var id = "net_music_list_mp3_pack";
        var info = new PackLocationInfo(id, Component.translatable("title.net_music_list.mp3"),
                PackSource.BUILT_IN, Optional.empty());
        var metadata = new Pack.Metadata(Component.translatable("description.net_music_list.mp3"), PackCompatibility.COMPATIBLE, FeatureFlagSet.of(), Collections.emptyList(), false);
        var config = new PackSelectionConfig(false, Pack.Position.TOP, false);
        pack = new Pack(info, supplier, metadata, config);
    }

    private PathPackResources.PathResourcesSupplier getLegacyPack() {
        net.neoforged.neoforgespi.locating.IModFile file = net.neoforged.fml.ModList.get().getModFileById(NetMusicList.ModID).getFile();
        return new PathPackResources.PathResourcesSupplier(file.getFilePath().resolve("mp3"));
    }

    @Override
    public void loadPacks(Consumer<Pack> consumer) {
        consumer.accept(pack);
    }
}
