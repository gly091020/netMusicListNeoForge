package com.gly091020.netMusicListNeoforge.jade;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.entity.MusicPlayerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.impl.ui.ItemStackElement;

import java.util.ArrayList;
import java.util.Objects;

public class PlayerEntityComponentProvider implements IEntityComponentProvider {
    public static final PlayerEntityComponentProvider INSTANCE = new PlayerEntityComponentProvider();
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "player_entity");
    @Override
    public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
        if(entityAccessor.getEntity() instanceof MusicPlayerEntity){
            var data = entityAccessor.getServerData();
            if(!data.contains("item"))return;
            var item = ItemStack.parse(entityAccessor.getLevel().registryAccess(), Objects.requireNonNull(data.get("item")));
            if(item.isPresent()){
                var texts = new ArrayList<Component>();
                item.get().getItem().appendHoverText(item.get(), Item.TooltipContext.EMPTY, texts, TooltipFlag.NORMAL);
                iTooltip.addAll(texts);
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public @Nullable IElement getIcon(EntityAccessor accessor, IPluginConfig config, IElement currentIcon) {
        return ItemStackElement.of(NetMusicList.MUSIC_PLAYER_ITEM.get().getDefaultInstance());
    }

    private PlayerEntityComponentProvider(){}
}
