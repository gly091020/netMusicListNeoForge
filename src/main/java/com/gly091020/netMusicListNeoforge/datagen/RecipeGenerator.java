package com.gly091020.netMusicListNeoforge.datagen;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class RecipeGenerator extends RecipeProvider{
    public RecipeGenerator(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        var itemRegistryLookup = registries.lookupOrThrow(Registries.ITEM);

        ShapedRecipeBuilder.shaped(itemRegistryLookup, RecipeCategory.MISC, NetMusicList.MUSIC_PLAYER_ITEM.get())
                .pattern("SSS")
                .pattern("IPI")
                .pattern("   ")
                .define('I', Items.IRON_INGOT)
                .define('S', Items.STRING)
                .define('P', InitItems.MUSIC_PLAYER.get())
                .unlockedBy("has_cd", has(InitItems.MUSIC_CD.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(itemRegistryLookup, RecipeCategory.MISC, NetMusicList.MUSIC_PLAYER_ITEM.get())
                .pattern("SSS")
                .pattern("IPI")
                .pattern("   ")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('S', Items.STRING)
                .define('P', InitItems.MUSIC_PLAYER.get())
                .unlockedBy("has_cd", has(InitItems.MUSIC_CD.get()))
                .save(output, "music_player1");

        ShapelessRecipeBuilder.shapeless(itemRegistryLookup, RecipeCategory.MISC, NetMusicList.MUSIC_LIST_ITEM.get())
                .requires(InitItems.MUSIC_CD.get())
                .requires(Items.LEATHER)
                .requires(Items.STRING)
                .unlockedBy("has_list", has(NetMusicList.MUSIC_LIST_ITEM.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(itemRegistryLookup, RecipeCategory.MISC, NetMusicList.MANUAL.get())
                .requires(Items.BOOK)
                .requires(InitItems.MUSIC_CD.get())
                .unlockedBy("has_cd", has(InitItems.MUSIC_CD.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(itemRegistryLookup, RecipeCategory.MISC, NetMusicList.MUSIC_LIST_ITEM.get())
                .requires(NetMusicList.MUSIC_LIST_ITEM.get())
                .unlockedBy("has_list", has(NetMusicList.MUSIC_LIST_ITEM.get()))
                .save(output, "clear_music_list");
    }

    public static final class Runner extends RecipeProvider.Runner {

        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
            return new RecipeGenerator(provider, recipeOutput);
        }

        @Override
        public String getName() {
            return "Net Music List";
        }
    }
}
