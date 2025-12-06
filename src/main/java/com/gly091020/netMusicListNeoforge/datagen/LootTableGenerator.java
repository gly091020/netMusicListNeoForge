package com.gly091020.netMusicListNeoforge.datagen;

import com.github.tartaricacid.netmusic.init.InitDataComponent;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetCustomDataFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class LootTableGenerator extends LootTableProvider {
    public LootTableGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(BookLootTable::new, LootContextParamSets.ADVANCEMENT_REWARD),
                new SubProviderEntry(BlockLootTable::new, LootContextParamSets.BLOCK)
        ), registries);
    }

    @SuppressWarnings("all")
    public static class BookLootTable implements LootTableSubProvider{
        public BookLootTable(HolderLookup.Provider registries){
            super();
        }

        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
            consumer.accept(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "book")),
                    new LootTable.Builder().withPool(
                            new LootPool.Builder()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(NetMusicList.MANUAL.get()))
                    )
            );

            consumer.accept(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "cat_dj")),
                    new LootTable.Builder().withPool(
                            new LootPool.Builder()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(InitItems.MUSIC_CD.get()).apply(SetComponentsFunction.setComponent(InitDataComponent.SONG_INFO.get(), getCatDJTag())))
                    )
            );
        }

        private static ItemMusicCD.SongInfo getCatDJTag(){
            var songInfo = new ItemMusicCD.SongInfo();
            songInfo.songName = "自学编曲作品集";
            songInfo.songTime = 201;
            songInfo.readOnly = true;
            songInfo.artists = List.of("Frafrale");
            songInfo.songUrl = "https://music.163.com/song/media/outer/url?id=2752399163.mp3";
            songInfo.vip = false;
            return songInfo;
        }
    }

    public static class BlockLootTable extends BlockLootSubProvider{
        protected BlockLootTable(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            dropSelf(NetMusicList.ENDER_MUSIC_PLAYER.get());
        }

        @Override
        protected @NotNull Iterable<Block> getKnownBlocks() {
            return List.of(NetMusicList.ENDER_MUSIC_PLAYER.get());
        }
    }
}
