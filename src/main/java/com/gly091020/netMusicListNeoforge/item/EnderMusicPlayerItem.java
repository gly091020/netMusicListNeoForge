package com.gly091020.netMusicListNeoforge.item;

import com.github.tartaricacid.netmusic.init.InitBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.gly091020.netMusicListNeoforge.NetMusicList.ENDER_MUSIC_PLAYER;

public class EnderMusicPlayerItem extends BlockItem {
    public EnderMusicPlayerItem() {
        super(ENDER_MUSIC_PLAYER.get(), new Item.Properties());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.net_music_list.ender_music_player.tip1"));
        tooltipComponents.add(Component.translatable("item.net_music_list.ender_music_player.tip2"));
    }

    @Override
    protected boolean canPlace(@NotNull BlockPlaceContext context, @NotNull BlockState state) {
        return false;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if(player.isShiftKeyDown()){
            var stack = player.getItemInHand(usedHand);
            stack.shrink(1);
            player.addItem(new ItemStack(InitBlocks.MUSIC_PLAYER.asItem(), 1));
            player.addItem(new ItemStack(Items.ENDER_PEARL, 1));
            return InteractionResultHolder.success(stack);
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public @NotNull InteractionResult place(BlockPlaceContext context) {
        if(context.getLevel().isClientSide) {
            if (context.getPlayer() != null) {
                context.getPlayer().sendSystemMessage(Component.translatable("item.net_music_list.ender_music_player.tip1"));
                context.getPlayer().sendSystemMessage(Component.translatable("item.net_music_list.ender_music_player.tip2"));
            }
        }
        return super.place(context);
    }
}
