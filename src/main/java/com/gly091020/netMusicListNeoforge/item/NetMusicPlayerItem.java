// 回来吧我的IAM MUSIC PLAYER
package com.gly091020.netMusicListNeoforge.item;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.hud.MusicListLayer;
import com.gly091020.netMusicListNeoforge.item.components.MusicPlayerComponent;
import com.gly091020.netMusicListNeoforge.packet.PlayerPlayMusicPacket;
import com.gly091020.netMusicListNeoforge.packet.UpdatePlayerMusicPacket;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
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
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NetMusicPlayerItem extends Item{
    public NetMusicPlayerItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack stack, @NotNull ItemStack stack1,
                                            @NotNull Slot slot, @NotNull ClickAction action,
                                            @NotNull Player player, @NotNull SlotAccess access) {
        if(action == ClickAction.SECONDARY && !getContainer(stack).isEmpty() && access.get().isEmpty()){
            access.set(getContainer(stack).removeItem(0, 1));
            var c = stack.getOrDefault(NetMusicList.MUSIC_PLAYER_COMPONENT, MusicPlayerComponent.getInstance());
            stack.set(NetMusicList.MUSIC_PLAYER_COMPONENT, new MusicPlayerComponent(-1, c.shadowTick(), c.waitSong()));
            return true;
        }
        if(action == ClickAction.PRIMARY && ItemMusicCD.getSongInfo(stack1) != null & getContainer(stack).isEmpty()){
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
        if(info == null)return;
        if(!NetMusicListUtil.hasLoginNeed() && info.vip && player.level().isClientSide){
            player.sendSystemMessage(Component.translatable("message.netmusic.music_player.need_vip")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        var c = stack.getOrDefault(NetMusicList.MUSIC_PLAYER_COMPONENT, MusicPlayerComponent.getInstance());
        var shadowTick = c.shadowTick();
        if(shadowTick < 0){shadowTick = info.songTime * 20;} //避免出现什么奇怪的BUG
        stack.set(NetMusicList.MUSIC_PLAYER_COMPONENT, new MusicPlayerComponent(info.songTime * 20, shadowTick, 0)); //哦！在此重置！(Dj音）

        if(!player.level().isClientSide){return;}
        NetworkHandler.sendToServer(new PlayerPlayMusicPacket(player.getId(), info.songUrl, info.songTime, info.songName, slot, info));
    }

    public static void sendPacket(ItemStack stack, Player player, int slot){
        var i = getContainer(stack).getItem(0);
        if(!i.is(InitItems.MUSIC_CD.get()) && !i.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            return;
        }
        var info = ItemMusicCD.getSongInfo(i);
        if(info == null)return;
        if(!NetMusicListUtil.hasLoginNeed() && info.vip){
            return;
        }
        if(!player.level().isClientSide){
            PacketDistributor.sendToAllPlayers(new PlayerPlayMusicPacket(player.getId(), info.songUrl, info.songTime, info.songName, slot, info));
        }else {
            NetworkHandler.sendToServer(new PlayerPlayMusicPacket(player.getId(), info.songUrl, info.songTime, info.songName, slot, info));
        }
    }

    public static PlayerContainer getContainer(ItemStack stack){
        return new PlayerContainer(stack);
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
        return Component.translatable("item.net_music_list.music_player");
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
        var i = getContainer(stack).getItem(0);
        if(i.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            NetMusicListItem.nextMusic(i);
        }
        playSound(stack, player, slot);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean b) {
        super.inventoryTick(stack, level, entity, slot, b);
        //给你稍微重构了下，现在就是正常的结束后重新播放了
        var c = stack.getOrDefault(NetMusicList.MUSIC_PLAYER_COMPONENT, MusicPlayerComponent.getInstance());
        var t = c.tick();
        var shadowTick = c.shadowTick(); //这个是记录上一个tick的值，（影子是光的上一步）
        var waitSong = c.waitSong(); //这个是保证在声音结束后才会下一首
        if(entity instanceof Player player && waitSong==2 && t<1){ //复杂度比你的稍微高了一点点，但是稳定性应该是高了不少
            nextMusic(stack, player, slot);
            return;
        }
        if(t==-1&&shadowTick>-1&&shadowTick<4){
            waitSong++;
        }
        if(t>0){
            shadowTick = t;
            t--;
        }
        stack.set(NetMusicList.MUSIC_PLAYER_COMPONENT, new MusicPlayerComponent(t, shadowTick, waitSong));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if(!level.isClientSide){
            return super.use(level, player, usedHand);
        }
        if(MusicListLayer.isRender){
            NetMusicListUtil.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
            var container = getContainer(player.getItemInHand(usedHand));
            var item = container.getItem(0);
            var index = NetMusicListItem.getSongIndex(item);
            if(MusicListLayer.index != index){
                NetMusicListItem.setSongIndex(item, MusicListLayer.index);
                container.setItem(0, item);
                var slot = player.getInventory().findSlotMatchingItem(player.getItemInHand(usedHand));
                playSound(player.getItemInHand(usedHand), player, slot);
                NetworkHandler.sendToServer(new UpdatePlayerMusicPacket(MusicListLayer.index,
                        slot));
            }
            MusicListLayer.isRender = false;
            return InteractionResultHolder.success(player.getMainHandItem());
        }
        MusicListLayer.isRender = true;
        return InteractionResultHolder.success(player.getMainHandItem());
    }

    public static class PlayerContainer extends SimpleContainer{
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
