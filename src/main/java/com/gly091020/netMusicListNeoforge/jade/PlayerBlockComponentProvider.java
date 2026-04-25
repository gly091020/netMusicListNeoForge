package com.gly091020.netMusicListNeoforge.jade;

import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class PlayerBlockComponentProvider implements IBlockComponentProvider {
    public static final PlayerBlockComponentProvider INSTANCE = new PlayerBlockComponentProvider();
    private static final Identifier UID = Identifier.fromNamespaceAndPath(NetMusicList.ModID, "player_block");
    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if(blockAccessor.getBlockEntity() instanceof TileEntityMusicPlayer musicPlayer){
            getTooltip(iTooltip, musicPlayer);
        }
    }

    private void getTooltip(ITooltip iTooltip, TileEntityMusicPlayer musicPlayer){
        var item = musicPlayer.getPlayerInv().getResource(0).toStack();
        if(!item.isEmpty()){
            item.getItem().appendHoverText(item, Item.TooltipContext.EMPTY, TooltipDisplay.DEFAULT, iTooltip::add, TooltipFlag.NORMAL);
        }
    }

    @Override
    public Identifier getUid() {
        return UID;
    }

    private PlayerBlockComponentProvider(){

    }
}
