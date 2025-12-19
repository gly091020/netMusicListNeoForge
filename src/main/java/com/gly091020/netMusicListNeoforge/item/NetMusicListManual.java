package com.gly091020.netMusicListNeoforge.item;

import com.gly091020.netMusicListNeoforge.util.MUIUtil;
import com.gly091020.netMusicListNeoforge.util.PatchouliOpener;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

public class NetMusicListManual extends Item {
    public NetMusicListManual() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if(ModList.get().isLoaded("modernui")) {
            if (player.level().isClientSide) {
                MUIUtil.openDirectoryScreen();
            }
        } else if(ModList.get().isLoaded("patchouli")) {
            if (!player.level().isClientSide) {
                PatchouliOpener.open((ServerPlayer) player);
            }
        } else {
            player.sendSystemMessage(Component.translatable("text.net_music_list.no_manual_mod").withStyle(ChatFormatting.RED));
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
