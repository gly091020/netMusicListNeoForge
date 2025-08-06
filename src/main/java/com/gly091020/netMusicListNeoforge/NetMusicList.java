package com.gly091020.netMusicListNeoforge;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.gly091020.netMusicListNeoforge.item.NetMusicListItem;
import com.gly091020.netMusicListNeoforge.item.NetMusicPlayerItem;
import com.gly091020.netMusicListNeoforge.item.component.MusicListComponent;
import com.gly091020.netMusicListNeoforge.packet.PacketRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

@Mod(NetMusicList.ModID)
public class NetMusicList {
    public static final String ModID = "net_music_list";

    public static final Logger LOGGER = LoggerFactory.getLogger(ModID);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, ModID);
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModID);

    public static final DeferredHolder<Item, NetMusicListItem> MUSIC_LIST_ITEM = ITEMS.register("music_list",
            NetMusicListItem::new);
    public static final DeferredHolder<Item, NetMusicPlayerItem> MUSIC_PLAYER_ITEM = ITEMS.register("music_player",
            NetMusicPlayerItem::new);
    public static final Supplier<DataComponentType<MusicListComponent>> MUSIC_LIST_COMPONENT = COMPONENTS.registerComponentType("music_list_component", builder ->
        builder.persistent(MusicListComponent.CODEC).networkSynchronized(MusicListComponent.STREAM_CODEC)
    );
    public static final Supplier<DataComponentType<Integer>> MUSIC_PLAYER_TICK = COMPONENTS.registerComponentType("music_player_tick", builder ->
            builder.persistent(Codec.INT)
    );
    public static final Supplier<DataComponentType<String>> MUSIC_PLAYER_UUID = COMPONENTS.registerComponentType("music_player_uuid", builder ->
            builder.persistent(Codec.STRING)
    );

    public NetMusicList(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        COMPONENTS.register(modEventBus);
        modEventBus.addListener(this::addItemsToCreativeTab);
        modEventBus.addListener(PacketRegistry::registry);
        modEventBus.addListener(this::addPack);
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
        }
    }

    private void addPack(AddPackFindersEvent event) {
        event.addRepositorySource(new MP3Pack());
    }
}