package com.gly091020.netMusicListNeoforge.datagen;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class RecipeGenerator extends RecipeProvider{
    public RecipeGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NetMusicList.MUSIC_PLAYER_ITEM.get())
                .pattern("SSS")
                .pattern("IPI")
                .pattern("   ")
                .define('I', Items.IRON_INGOT)
                .define('S', Items.STRING)
                .define('P', InitItems.MUSIC_PLAYER.get())
                .unlockedBy("has_cd", has(InitItems.MUSIC_CD.get()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NetMusicList.MUSIC_PLAYER_ITEM.get())
                .pattern("SSS")
                .pattern("IPI")
                .pattern("   ")
                .define('I', Ingredient.of(TagKey.create(Registries.ITEM,
                        ResourceLocation.parse("forge:ingots/iron"))))
                .define('S', Items.STRING)
                .define('P', InitItems.MUSIC_PLAYER.get())
                .unlockedBy("has_cd", has(InitItems.MUSIC_CD.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "music_player1"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NetMusicList.MUSIC_LIST_ITEM.get())
                .requires(InitItems.MUSIC_CD.get())
                .requires(Items.LEATHER)
                .requires(Items.STRING)
                .unlockedBy("has_list", has(NetMusicList.MUSIC_LIST_ITEM.get()))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NetMusicList.MANUAL.get())
                .requires(Items.BOOK)
                .requires(InitItems.MUSIC_CD.get())
                .unlockedBy("has_cd", has(InitItems.MUSIC_CD.get()));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NetMusicList.MUSIC_LIST_ITEM.get())
                .requires(NetMusicList.MUSIC_LIST_ITEM.get())
                .unlockedBy("has_list", has(NetMusicList.MUSIC_LIST_ITEM.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "clear_music_list"));
    }
}
