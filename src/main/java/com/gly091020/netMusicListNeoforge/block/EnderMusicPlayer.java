package com.gly091020.netMusicListNeoforge.block;

import com.github.tartaricacid.netmusic.block.BlockMusicPlayer;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnderMusicPlayer extends BlockMusicPlayer {
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnderMusicPlayerEntity(pos, state);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        var entity = level.getBlockEntity(pos);
        if(!(entity instanceof EnderMusicPlayerEntity enderMusicPlayer))return InteractionResult.PASS;
        if(player.isShiftKeyDown() && !enderMusicPlayer.isPlay()){
            if(level.isClientSide){
                player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
                return InteractionResult.SUCCESS;
            }
            var uuid = player.getUUID();
            if(enderMusicPlayer.hasPlayer(uuid)){
                enderMusicPlayer.removePlayer(uuid);
                player.sendSystemMessage(Component.translatable("block.net_music_list.ender_music_player.remove_player", player.getDisplayName()));
            }else{
                player.sendSystemMessage(Component.translatable("block.net_music_list.ender_music_player.add_player", player.getDisplayName()));
                enderMusicPlayer.addPlayer(uuid);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
        var entity = worldIn.getBlockEntity(pos);
        if(!(entity instanceof EnderMusicPlayerEntity enderMusicPlayer))return ItemInteractionResult.CONSUME_PARTIAL;
        ItemStackHandler handler = enderMusicPlayer.getPlayerInv();
        if (!handler.getStackInSlot(0).isEmpty()) {
            ItemStack extract = handler.extractItem(0, 1, false);
            popResource(worldIn, pos, extract);
            enderMusicPlayer.markDirty();
            return ItemInteractionResult.SUCCESS;
        } else {
            ItemMusicCD.SongInfo info = ItemMusicCD.getSongInfo(stack);
            if (info == null) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            } else if (!NetMusicListUtil.hasLoginNeed() && info.vip) {
                if (worldIn.isClientSide) {
                    playerIn.sendSystemMessage(Component.translatable("message.netmusic.music_player.need_vip").withStyle(ChatFormatting.RED));
                }

                return ItemInteractionResult.FAIL;
            } else {
                handler.insertItem(0, stack.copy(), false);
                if (!playerIn.isCreative()) {
                    stack.shrink(1);
                }

                enderMusicPlayer.setPlayToClient(info);
                enderMusicPlayer.markDirty();
                return ItemInteractionResult.SUCCESS;
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof EnderMusicPlayerEntity musicPlayer) {
            ItemStack stack = musicPlayer.getPlayerInv().getStackInSlot(0);
            if (!stack.isEmpty()) {
                Block.popResource(worldIn, pos, stack);
            }
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }
}
