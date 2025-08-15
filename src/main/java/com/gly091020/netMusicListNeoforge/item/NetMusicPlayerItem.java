// 回来吧我的IAM MUSIC PLAYER
package com.gly091020.netMusicListNeoforge.item;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.NetMusicListUtil;
import com.gly091020.netMusicListNeoforge.client.MusicInfoHud;
import com.gly091020.netMusicListNeoforge.client.MusicListLayer;
import com.gly091020.netMusicListNeoforge.item.component.MusicListComponent;
import com.gly091020.netMusicListNeoforge.packet.PlayerPlayMusicCTSPacket;
import com.gly091020.netMusicListNeoforge.packet.StopMusicCTSPacket;
import com.gly091020.netMusicListNeoforge.packet.UpdatePlayerMusicPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class NetMusicPlayerItem extends Item{
    public NetMusicPlayerItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack stack, @NotNull ItemStack stack1,
                                            @NotNull Slot slot, @NotNull ClickAction action,
                                            @NotNull Player player, @NotNull SlotAccess access) {
        if(action == ClickAction.SECONDARY && !getContainer(stack).isEmpty()){
            access.set(getContainer(stack).removeItem(0, 1));
            stack.set(NetMusicList.MUSIC_PLAYER_TICK, -1);
            return true;
        }
        if(action == ClickAction.PRIMARY && ItemMusicCD.getSongInfo(stack1) != null && getContainer(stack).isEmpty()){
            getContainer(stack).setItem(0, stack1);
            access.set(ItemStack.EMPTY);
            playSound(stack, player, slot.getSlotIndex());
            return true;
        }
        return super.overrideOtherStackedOnMe(stack, stack1, slot, action, player, access);
    }

    public static void playSound(ItemStack stack, Player player, int slot){
        var i = getContainer(stack).getItem(0);
        if(!i.is(InitItems.MUSIC_CD.get()) && !i.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            return;
        }
        var info = ItemMusicCD.getSongInfo(i);
        if(info == null){return;}
        if(info.vip && player.level().isClientSide){
            player.sendSystemMessage(Component.translatable("message.netmusic.music_player.need_vip")
                    .withStyle(ChatFormatting.RED));
            stack.set(NetMusicList.MUSIC_PLAYER_TICK, 0);
            MusicInfoHud.clearInfo();
            return;
        }
        stack.set(NetMusicList.MUSIC_PLAYER_TICK, info.songTime * 20);
        if(!player.level().isClientSide){return;}
        PacketDistributor.sendToServer(new PlayerPlayMusicCTSPacket(player.getId(), info.songUrl, info, slot, stack.getOrDefault(NetMusicList.MUSIC_PLAYER_UUID, UUID.randomUUID().toString())));
    }

    public static MusicPlayerContainer getContainer(ItemStack stack){
        return new MusicPlayerContainer(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Component t;
        var c = getContainer(stack);
        if(c.isEmpty()){
            t = Component.translatable("item.net_music_player.empty").withStyle(ChatFormatting.RED);
        }else{
            t = c.getItem(0).getHoverName();
        }
        tooltipComponents.add(Component.translatable("item.net_music_player.tip", t));
        var i = getContainer(stack).getItem(0);
        i.getItem().appendHoverText(i, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return Component.translatable("item.net_music_player.name");
    }

    @Override
    public boolean overrideStackedOnOther(@NotNull ItemStack stack, @NotNull Slot slot,
                                          @NotNull ClickAction action, @NotNull Player player) {
        var i = getContainer(stack).getItem(0);
        if(ItemMusicCD.getSongInfo(i) != null){
            playSound(stack, player, slot.getSlotIndex());
        }
        return false;
    }

    public static void nextMusic(ItemStack stack, Player player, int slot){
        // todo:下一首功能只能循环播放
        // 这个bug还是让有缘人来改吧
        // 能跑就别动
        var i = getContainer(stack).getItem(0);
        if(i.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            NetMusicListItem.nextMusic(i);
        }
        playSound(stack, player, slot);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean b) {
        super.inventoryTick(stack, level, entity, slot, b);
        var t = stack.getOrDefault(NetMusicList.MUSIC_PLAYER_TICK, 0) - 1;
        if(t < 0){return;}
        if(entity instanceof Player player && 0 < t && t < 16 && t % 5 == 0){
            stack.set(NetMusicList.MUSIC_PLAYER_TICK, -1);
            nextMusic(stack, player, slot);
            return;
        }
        stack.set(NetMusicList.MUSIC_PLAYER_TICK, t);
        stack.setPopTime(0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if(!level.isClientSide){
            return super.use(level, player, usedHand);
        }
        if(MusicListLayer.isRender){
            NetMusicListUtil.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
            var container = getContainer(player.getItemInHand(usedHand));
            var item = container.getItem(0);
            var c = item.getOrDefault(NetMusicList.MUSIC_LIST_COMPONENT, MusicListComponent.getDefault());
            if(MusicListLayer.index != c.index()){
                item.set(NetMusicList.MUSIC_LIST_COMPONENT, new MusicListComponent(c.songList(), c.playMode(), MusicListLayer.index));
                container.setItem(0, item);
                var uuid = player.getItemInHand(usedHand).get(NetMusicList.MUSIC_PLAYER_UUID);
                if(uuid != null){
                    PacketDistributor.sendToServer(new StopMusicCTSPacket(uuid));
                }
                var uuid1 = UUID.randomUUID().toString();
                player.getItemInHand(usedHand).set(NetMusicList.MUSIC_PLAYER_UUID, uuid1);
                var slot = player.getInventory().findSlotMatchingItem(player.getItemInHand(usedHand));
                playSound(player.getItemInHand(usedHand), player, slot);
                PacketDistributor.sendToServer(new UpdatePlayerMusicPacket(MusicListLayer.index,
                        slot, player.getItemInHand(usedHand).get(NetMusicList.MUSIC_PLAYER_UUID)));
            }
            MusicListLayer.isRender = false;
            return InteractionResultHolder.success(player.getMainHandItem());
        }
        MusicListLayer.isRender = true;
        return InteractionResultHolder.success(player.getMainHandItem());
    }
}
