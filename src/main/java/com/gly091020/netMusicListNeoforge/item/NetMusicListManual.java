package com.gly091020.netMusicListNeoforge.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class NetMusicListManual extends Item {
    public NetMusicListManual(Identifier identifier) {
        super(new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, identifier)));
    }
}
