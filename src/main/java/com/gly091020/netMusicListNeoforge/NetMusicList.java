package com.gly091020.netMusicListNeoforge;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.gly091020.netMusicListNeoforge.config.ConfigScreenGetter;
import com.gly091020.netMusicListNeoforge.config.NetMusicListConfig;
import com.gly091020.netMusicListNeoforge.datagen.AdvancementGenerator;
import com.gly091020.netMusicListNeoforge.datagen.ItemModelGenerator;
import com.gly091020.netMusicListNeoforge.datagen.LootTableGenerator;
import com.gly091020.netMusicListNeoforge.datagen.RecipeGenerator;
import com.gly091020.netMusicListNeoforge.entity.MusicPlayerEntity;
import com.gly091020.netMusicListNeoforge.item.NetMusicListItem;
import com.gly091020.netMusicListNeoforge.item.NetMusicListManual;
import com.gly091020.netMusicListNeoforge.item.NetMusicPlayerItem;
import com.gly091020.netMusicListNeoforge.item.components.MusicListComponent;
import com.gly091020.netMusicListNeoforge.util.CacheManager;
import com.gly091020.netMusicListNeoforge.util.MP3Pack;
import com.gly091020.netMusicListNeoforge.util.NetMusicListKeyMapping;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Mod(NetMusicList.ModID)
public class NetMusicList {
    public static final String ModID = "net_music_list";

    public static final Logger LOGGER = LoggerFactory.getLogger(ModID);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ModID);
    public static final DeferredHolder<Item, NetMusicListItem> MUSIC_LIST_ITEM = ITEMS.register("music_list",
            NetMusicListItem::new);
    public static final DeferredHolder<Item, NetMusicPlayerItem> MUSIC_PLAYER_ITEM = ITEMS.register("music_player",
            NetMusicPlayerItem::new);
    public static final DeferredHolder<Item, Item> MANUAL_MODEL = ITEMS.register("manual_model",
            id -> new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));  // 俺寻思之力
    public static final DeferredHolder<Item, NetMusicListManual> MANUAL = ITEMS.register("manual",
            NetMusicListManual::new);

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, ModID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModID);

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ModID);
    public static final Supplier<DataComponentType<MusicListComponent>> MUSIC_LIST_COMPONENT = DATA_COMPONENTS.register("music_list", () ->
            DataComponentType.<MusicListComponent>builder()
                    .persistent(MusicListComponent.CODEC)
                    .networkSynchronized(MusicListComponent.STREAM_CODEC)
                    .build());

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPE = DeferredRegister.create(Registries.ENTITY_TYPE, ModID);
    public static final Supplier<EntityType<MusicPlayerEntity>> MUSIC_PLAYER_ENTITY = ENTITY_TYPE.register("music_player", identifier ->
        EntityType.Builder.of(MusicPlayerEntity::new, MobCategory.MISC)
                .sized(12 / 16f, 6 / 16f)
                .build(ResourceKey.create(Registries.ENTITY_TYPE, identifier))
    );
    public static NetMusicListConfig CONFIG;

    public NetMusicList(IEventBus modEventBus) {
        AutoConfig.register(NetMusicListConfig.class, Toml4jConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(NetMusicListConfig.class).get();
        NetMusicListUtil.reloadConfig();

        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
        ENTITY_TYPE.register(modEventBus);
        modEventBus.addListener(this::addItemsToCreativeTab);
        modEventBus.addListener(this::addPack);

        ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> (mc, screen) ->
                        ConfigScreenGetter.getConfigScreen(screen)
        );

        if(FMLEnvironment.getDist().isClient()){
            NetMusicListKeyMapping.init();
            modEventBus.addListener(NetMusicListKeyMapping::registerKeyBindings);
        }
        modEventBus.addListener(NetMusicList::gatherDataClient);
        modEventBus.addListener(NetMusicList::gatherDataServer);
        modEventBus.addListener(NetMusicList::registerEntityAttributes);
        CacheManager.load();
    }

    private static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(MUSIC_PLAYER_ENTITY.get(),
                MusicPlayerEntity.createAttributes().build());
    }

    public static void gatherDataServer(GatherDataEvent.Server event){
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(true, new LootTableGenerator(output, lookupProvider));
        generator.addProvider(true, new AdvancementGenerator(output, lookupProvider));
        generator.addProvider(true, new RecipeGenerator.Runner(output, lookupProvider));
    }

    public static void gatherDataClient(GatherDataEvent.Client event){
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(true, new ItemModelGenerator(output));
    }

    private void addItemsToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == InitItems.NET_MUSIC_TAB.get()) {
            event.insertBefore(
                    new ItemStack(InitItems.MUSIC_CD.get()),
                    new ItemStack(MUSIC_LIST_ITEM.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.insertBefore(
                    new ItemStack(MUSIC_LIST_ITEM.get()),
                    new ItemStack(MUSIC_PLAYER_ITEM.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
//            event.insertBefore(
//                    new ItemStack(MUSIC_PLAYER_ITEM.get()),
//                    new ItemStack(MANUAL.get()),
//                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
//            );
        }
    }

    private void addPack(AddPackFindersEvent event) {
        event.addRepositorySource(new MP3Pack());
    }
}