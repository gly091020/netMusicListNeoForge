package com.gly091020.netMusicListNeoforge.create;

import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MusicPlayerPointType{
    static {
        Registry.register(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE, ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "music_player"),
                new NetMusicPlayerType());
    }
    public static class NetMusicPlayerType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(InitBlocks.MUSIC_PLAYER);
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new NetMusicPlayerArmInteractionPoint(this, level, pos, state);
        }
    }

    public static class NetMusicPlayerArmInteractionPoint extends ArmInteractionPoint{
        public NetMusicPlayerArmInteractionPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            var out = super.insert(armBlockEntity, stack, simulate);
            if(!stack.isEmpty() && !simulate && level.getBlockEntity(pos) instanceof TileEntityMusicPlayer musicPlayer){
                var info = ItemMusicCD.getSongInfo(musicPlayer.getPlayerInv().getStackInSlot(0));
                if(info != null){
                    musicPlayer.setPlayToClient(info);
                    musicPlayer.setPlay(true);
                }
            }
            return out;
        }

        @Override
        public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, boolean simulate) {
            var out = super.extract(armBlockEntity, slot, simulate);
            if(!out.isEmpty() && !simulate && level.getBlockEntity(pos) instanceof TileEntityMusicPlayer musicPlayer &&
                    musicPlayer.getPlayerInv().getStackInSlot(0).isEmpty()){
                musicPlayer.setPlay(false);
            }
            return out;
        }
    }

    public static void init(){}
}
