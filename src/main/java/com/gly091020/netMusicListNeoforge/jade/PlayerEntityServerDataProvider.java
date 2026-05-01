package com.gly091020.netMusicListNeoforge.jade;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.entity.MusicPlayerEntity;
import com.gly091020.netMusicListNeoforge.item.NetMusicListItem;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

public class PlayerEntityServerDataProvider implements IServerDataProvider<EntityAccessor> {
    public static final PlayerEntityServerDataProvider INSTANCE = new PlayerEntityServerDataProvider();
    private static final Identifier UID = Identifier.fromNamespaceAndPath(NetMusicList.ModID, "player_entity_server");
    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, EntityAccessor entityAccessor) {
        if(entityAccessor.getEntity() instanceof MusicPlayerEntity entity){
            var cd = entity.getMusicCD();
            var data = cd.get(NetMusicList.MUSIC_LIST_COMPONENT);
            var info = ItemMusicCD.getSongInfo(cd);
            if(!cd.isEmpty()){
                var newStack = new ItemStack(cd.getItem());
                if(info != null)
                    ItemMusicCD.setSongInfo(info, newStack);
                NetMusicListItem.setSongIndex(newStack, 0);
                if(data != null)
                    NetMusicListItem.setPlayMode(newStack, data.playMode());
                compoundTag.put("item", NetMusicListUtil.itemToTag(newStack));
            }
        }
    }
}
