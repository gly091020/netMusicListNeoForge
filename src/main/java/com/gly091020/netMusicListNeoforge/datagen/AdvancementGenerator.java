package com.gly091020.netMusicListNeoforge.datagen;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@SuppressWarnings("all")
public class AdvancementGenerator extends AdvancementProvider {
    public AdvancementGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, List.of(new NetMusicListAdvancements()));
    }

    private static class NetMusicListAdvancements implements AdvancementSubProvider {
        @Override
        public void generate(HolderLookup.Provider provider, Consumer<AdvancementHolder> consumer) {
            Advancement.Builder.advancement()
                    .addCriterion("get_item",
                            InventoryChangeTrigger.TriggerInstance.hasItems(NetMusicList.MUSIC_LIST_ITEM.get()))
                    .rewards(AdvancementRewards.Builder.loot(ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(NetMusicList.ModID, "book"))))
                    .save(consumer, Identifier.fromNamespaceAndPath(NetMusicList.ModID, "give_book"));
        }
    }
}
