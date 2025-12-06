package com.gly091020.netMusicListNeoforge.datagen;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@SuppressWarnings("all")
public class AdvancementGenerator extends AdvancementProvider {
    public AdvancementGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(new NetMusicListAdvancements()));
    }

    private static class NetMusicListAdvancements implements AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider provider, Consumer<AdvancementHolder> consumer, ExistingFileHelper existingFileHelper) {
            Advancement.Builder.advancement()
                    .addCriterion("get_item",
                            InventoryChangeTrigger.TriggerInstance.hasItems(NetMusicList.MUSIC_LIST_ITEM.get()))
                    .rewards(AdvancementRewards.Builder.loot(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "book"))))
                    .save(consumer, ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "give_book"), existingFileHelper);
        }
    }
}
