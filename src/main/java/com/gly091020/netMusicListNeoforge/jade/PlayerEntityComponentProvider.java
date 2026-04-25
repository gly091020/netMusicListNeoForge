package com.gly091020.netMusicListNeoforge.jade;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.entity.MusicPlayerEntity;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.impl.ui.ItemStackElement;

import java.util.Objects;

public class PlayerEntityComponentProvider implements IEntityComponentProvider {
    public static final PlayerEntityComponentProvider INSTANCE = new PlayerEntityComponentProvider();
    private static final Identifier UID = Identifier.fromNamespaceAndPath(NetMusicList.ModID, "player_entity");
    @Override
    public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
        if(entityAccessor.getEntity() instanceof MusicPlayerEntity){
            var data = entityAccessor.getServerData();
            if(!data.contains("item"))return;
            var item = NetMusicListUtil.tagToItem(Objects.requireNonNull(data.get("item")));
            item.getItem().appendHoverText(item, Item.TooltipContext.EMPTY, TooltipDisplay.DEFAULT, iTooltip::add, TooltipFlag.NORMAL);
        }
    }

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public Element getIcon(EntityAccessor accessor, IPluginConfig config, Element currentIcon) {
        return ItemStackElement.of(NetMusicList.MUSIC_PLAYER_ITEM.get().getDefaultInstance());
    }

    private PlayerEntityComponentProvider(){}
}
