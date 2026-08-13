package com.gly091020.netMusicListNeoforge.item;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.entity.MusicPlayerEntity;
import com.gly091020.netMusicListNeoforge.hud.MusicListLayer;
import com.gly091020.netMusicListNeoforge.packet.MusicPlayerActionPacket;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import com.gly091020.netMusicListNeoforge.util.PlayMode;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class NetMusicPlayerItem extends Item {
    public NetMusicPlayerItem() {
        super(new Properties().stacksTo(1));
    }

    @Nullable
    public static UUID getRingerId(ItemStack stack) {
        return stack.get(NetMusicList.RINGER_COMPONENT.get());
    }

    public static UUID getOrCreateRingerId(ItemStack stack) {
        var id = getRingerId(stack);
        if (id == null) {
            id = UUID.randomUUID();
            stack.set(NetMusicList.RINGER_COMPONENT.get(), id);
        }
        return id;
    }

    /** CD 或播放列表统一取当前歌曲信息（播放列表由 CanPlayListMixin 兼容）。 */
    @Nullable
    private static ItemMusicCD.SongInfo getSongInfo(ItemStack stack) {
        if (stack.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            return NetMusicListItem.getSongInfo(stack);
        }
        return ItemMusicCD.getSongInfo(stack);
    }

    /**
     * 客户端：插入唱片后延迟一 tick 再向服务端发送播放意图。
     * 延迟是为了保证意图包在容器点击包之后到达服务端（此时容器内已写入唱片）。
     */
    private static void requestServerPlay(int slotIndex) {
        if (slotIndex < 0) {
            return;
        }
        Minecraft.getInstance().execute(() -> {
            var player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            var stack = player.getInventory().getItem(slotIndex);
            if (!stack.is(NetMusicList.MUSIC_PLAYER_ITEM.get())) {
                return;
            }
            var info = getSongInfo(getContainer(stack).getItem(0));
            PacketDistributor.sendToServer(new MusicPlayerActionPacket(
                    MusicPlayerActionPacket.Action.PLAY, slotIndex, 0, PlayMode.LOOP, info));
        });
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack stack, @NotNull ItemStack stack1,
                                            @NotNull Slot slot, @NotNull ClickAction action,
                                            @NotNull Player player, @NotNull SlotAccess access) {
        if (action == ClickAction.SECONDARY && !getContainer(stack).isEmpty() && access.get().isEmpty()) {
            access.set(getContainer(stack).removeItem(0, 1));
            return true;
        }
        if (action == ClickAction.PRIMARY && getContainer(stack).isEmpty()) {
            if (stack1.is(NetMusicList.MUSIC_LIST_ITEM) && getSongInfo(stack1) == null) {
                NetMusicListItem.setSongIndex(stack1, 0);
                NetMusicListItem.nextMusic(stack1);
            }
            if (getSongInfo(stack1) != null) {
                getContainer(stack).setItem(0, stack1);
                access.set(ItemStack.EMPTY);
                if (slot.container == player.getInventory() && player.level().isClientSide) {
                    requestServerPlay(slot.getSlotIndex());
                }
                return true;
            }
        }
        return super.overrideOtherStackedOnMe(stack, stack1, slot, action, player, access);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Component t;
        var c = getContainer(stack);
        if (c.isEmpty()) {
            t = Component.translatable("item.net_music_player.empty").withStyle(net.minecraft.ChatFormatting.RED);
        } else {
            t = c.getItem(0).getHoverName();
        }
        tooltipComponents.add(Component.translatable("item.net_music_player.tip", t));
        var i = getContainer(stack).getItem(0);
        i.getItem().appendHoverText(i, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return Component.translatable("item.net_music_list.music_player");
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (player.isShiftKeyDown()) {
            return InteractionResultHolder.success(player.getItemInHand(usedHand));
        }
        if (!level.isClientSide) {
            return super.use(level, player, usedHand);
        }
        if (MusicListLayer.isRender) {
            NetMusicListUtil.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
            var musicPlayer = player.getInventory().getItem(MusicListLayer.slot);
            if (musicPlayer.is(NetMusicList.MUSIC_PLAYER_ITEM.get())) {
                var disc = getContainer(musicPlayer).getItem(0);
                if (MusicListLayer.index >= 0 && MusicListLayer.index != NetMusicListItem.getSongIndex(disc)) {
                    PacketDistributor.sendToServer(new MusicPlayerActionPacket(
                            MusicPlayerActionPacket.Action.SELECT_INDEX, MusicListLayer.slot, MusicListLayer.index, PlayMode.LOOP, null));
                }
            }
            MusicListLayer.isRender = false;
            return InteractionResultHolder.success(player.getMainHandItem());
        }
        MusicListLayer.isRender = true;
        MusicListLayer.slot = player.getInventory().findSlotMatchingItem(player.getMainHandItem());
        return InteractionResultHolder.success(player.getMainHandItem());
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        if (player == null) {
            return super.useOn(context);
        }
        if (player.isShiftKeyDown()) {
            var usedHand = context.getHand();
            var level = context.getLevel();
            var item = player.getItemInHand(usedHand);
            var entity = MusicPlayerEntity.fromItem(item, level);
            if (entity == null) {
                return InteractionResult.SUCCESS;
            }
            entity.setPos(context.getClickLocation());
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, player.position());
            if (!level.isClientSide) {
                entity.setRingerId(getOrCreateRingerId(item));
                level.addFreshEntity(entity);
                if (!player.isCreative()) {
                    item.shrink(1);
                }
                // 播放由实体 tick 自动接管
            } else {
                level.addFreshEntity(entity);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public boolean hasCustomEntity(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public @Nullable Entity createEntity(@NotNull Level level, @NotNull Entity location, @NotNull ItemStack stack) {
        var entity = MusicPlayerEntity.fromItem(stack, level);
        if (entity == null) {
            return null;
        }
        entity.setPos(location.position());
        entity.setXRot(location.getXRot() + 90);
        entity.setYRot(location.getYRot());
        entity.setDeltaMovement(location.getDeltaMovement());
        if (!level.isClientSide) {
            entity.setRingerId(getOrCreateRingerId(stack));
        }
        return entity;
    }

    public static PlayerContainer getContainer(ItemStack stack) {
        return new PlayerContainer(stack);
    }

    public static class PlayerContainer extends SimpleContainer {
        private final ItemStack stack;

        public PlayerContainer(ItemStack stack) {
            super(1);
            this.stack = stack;
            ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            contents.copyInto(getItems());
        }

        @Override
        public void setChanged() {
            this.stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(getItems()));
        }
    }
}
