package com.gly091020.netMusicListNeoforge.datagen;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.NonNull;

import java.util.stream.Stream;

public class ItemModelGenerator extends ModelProvider {

    public ItemModelGenerator(PackOutput output) {
        super(output, NetMusicList.ModID);
    }

    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(NetMusicList.MUSIC_LIST_ITEM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(NetMusicList.MUSIC_PLAYER_ITEM.get(), ModelTemplates.FLAT_ITEM);
    }

    @Override
    protected @NonNull Stream<? extends Holder<Item>> getKnownItems() {
        return Stream.of(
                NetMusicList.MUSIC_LIST_ITEM,
                NetMusicList.MUSIC_PLAYER_ITEM
        );
    }
}
