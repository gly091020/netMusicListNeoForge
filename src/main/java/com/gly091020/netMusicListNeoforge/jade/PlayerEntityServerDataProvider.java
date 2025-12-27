package com.gly091020.netMusicListNeoforge.jade;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.entity.MusicPlayerEntity;
import com.gly091020.netMusicListNeoforge.item.NetMusicListItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

public class PlayerEntityServerDataProvider implements IServerDataProvider<EntityAccessor> {
    public static final PlayerEntityServerDataProvider INSTANCE = new PlayerEntityServerDataProvider();
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "player_entity_server");
    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, EntityAccessor entityAccessor) {
        if(entityAccessor.getEntity() instanceof MusicPlayerEntity entity){
            var cd = entity.getMusicCD();
            var info = ItemMusicCD.getSongInfo(cd);
            if(!cd.isEmpty()){
                var newStack = new ItemStack(cd.getItem());
                if(info != null)
                    ItemMusicCD.setSongInfo(info, newStack);
                NetMusicListItem.setSongIndex(newStack, 0);
                compoundTag.put("item", newStack.save(entityAccessor.getLevel().registryAccess()));
            }
        }
    }
}
