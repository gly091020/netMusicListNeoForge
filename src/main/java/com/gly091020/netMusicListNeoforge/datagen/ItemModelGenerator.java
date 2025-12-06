package com.gly091020.netMusicListNeoforge.datagen;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ItemModelGenerator extends ItemModelProvider {
    public ItemModelGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, NetMusicList.ModID, existingFileHelper);
    }
    @Override
    protected void registerModels() {
        basicItem(NetMusicList.MUSIC_LIST_ITEM.get());
        basicItem(NetMusicList.MUSIC_PLAYER_ITEM.get());
        blockItem(NetMusicList.ENDER_MUSIC_PLAYER);
    }

    public void blockItem(DeferredHolder<?, ?> block) {
        withExistingParent(block.getId().getPath(),
                modLoc("block/" + block.getId().getPath()));
    }
}
