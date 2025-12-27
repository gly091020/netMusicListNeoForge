package com.gly091020.netMusicListNeoforge.jade;

import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.block.EnderMusicPlayerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import snownee.jade.JadeClient;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.impl.ui.ItemStackElement;

import java.util.ArrayList;

public class PlayerBlockComponentProvider implements IBlockComponentProvider {
    public static final PlayerBlockComponentProvider INSTANCE = new PlayerBlockComponentProvider();
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "player_block");
    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if(blockAccessor.getBlockEntity() instanceof TileEntityMusicPlayer musicPlayer){
            getTooltip(iTooltip, musicPlayer);
        }else if(blockAccessor.getBlockEntity() instanceof EnderMusicPlayerEntity enderMusicPlayer){
            getTooltip(iTooltip, enderMusicPlayer.getOriginalPlayer());
        }
    }

    private void getTooltip(ITooltip iTooltip, TileEntityMusicPlayer musicPlayer){
        var item = musicPlayer.getPlayerInv().getStackInSlot(0);
        if(!item.isEmpty()){
            var texts = new ArrayList<Component>();
            item.getItem().appendHoverText(item, Item.TooltipContext.EMPTY, texts, TooltipFlag.NORMAL);
            iTooltip.addAll(texts);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    private PlayerBlockComponentProvider(){

    }
}
