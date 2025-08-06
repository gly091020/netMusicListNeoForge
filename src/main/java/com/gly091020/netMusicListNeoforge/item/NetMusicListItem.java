package com.gly091020.netMusicListNeoforge.item;

import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.PlayMode;
import com.gly091020.netMusicListNeoforge.client.MusicSelectionScreen;
import com.gly091020.netMusicListNeoforge.item.component.MusicListComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NetMusicListItem extends ItemMusicCD {
    private static final String listKey = "NetMusicSongInfoList";
    public NetMusicListItem() {
        super();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    public static MusicListComponent getComponent(ItemStack stack){
        if (stack.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            return stack.getOrDefault(NetMusicList.MUSIC_LIST_COMPONENT.get(), MusicListComponent.getDefault());
        }
        return MusicListComponent.getDefault();
    }

    public static List<SongInfo> getSongInfoList(ItemStack stack) {
        if (stack.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            MusicListComponent component = getComponent(stack);
            return component.songList();
        }

        return new ArrayList<>();
    }

    public static void nextMusic(ItemStack stack){
        if(stack.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            switch (getPlayMode(stack)){
                case RANDOM -> {
                    var i = RandomSource.create().nextInt(0, getSongInfoList(stack).size() - 1);
                    setSongIndex(stack, i);
                }
                case SEQUENTIAL -> {
                    var i = getSongIndex(stack) + 1;
                    if(i >= getSongInfoList(stack).size()){
                        i = 0;
                    }
                    setSongIndex(stack, i);
                }
            }
        }
    }

    public static SongInfo getSongInfo(ItemStack stack) {
        var l = getSongInfoList(stack);
        if(l.isEmpty()){
            return null;
        }
        var i = getSongIndex(stack);
        if(i >= l.size()){return null;}
        return l.get(i);
    }

    public static Integer getSongIndex(ItemStack stack){
        if (stack.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            return getComponent(stack).index();
        }
        return -1;
    }

    public static void deleteSong(ItemStack stack, int index){
        if (stack.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
             var c = getComponent(stack);
             var c1 = new ArrayList<>(List.copyOf(c.songList()));
             c1.remove(index);
             stack.set(NetMusicList.MUSIC_LIST_COMPONENT.get(), new MusicListComponent(c1, c.playMode(), c.index()));
        }
    }

    public static void moveSong(ItemStack stack, int from, int to){
        if (stack.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            var c = getComponent(stack);
            var c1 = new ArrayList<>(List.copyOf(c.songList()));
            Collections.swap(c1, from, to);
            stack.set(NetMusicList.MUSIC_LIST_COMPONENT.get(), new MusicListComponent(c1, c.playMode(), c.index()));
        }
    }

    public static void setSongIndex(ItemStack stack, Integer index){
        if (stack.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            var c = getComponent(stack);
            var c1 = new MusicListComponent(c.songList(), c.playMode(), index);
            stack.set(NetMusicList.MUSIC_LIST_COMPONENT.get(), c1);
        }
    }

    public static ItemStack setSongInfo(SongInfo info, ItemStack stack) {
        if (stack.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            var c = getComponent(stack);
            var c1 = new ArrayList<>(List.copyOf(c.songList()));
            if(c1.size() <= c.index()){
                c1.add(info);
                stack.set(NetMusicList.MUSIC_LIST_COMPONENT.get(), new MusicListComponent(c1, c.playMode(), c.index() + 1));
            }else{
                c1.set(c.index(), info);
                stack.set(NetMusicList.MUSIC_LIST_COMPONENT.get(), new MusicListComponent(c1, c.playMode(), c.index()));
            }
        }

        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        String name;
        String text;
        name = Component.translatable("tooltip.net_music_list.play_mode").getString();
        text = "§a▍ §7" + name + ": §6" + getPlayMode(stack).getName().getString();
        if(getSongInfoList(stack).isEmpty()){
            tooltip.add(Component.translatable("tooltips.netmusic.cd.empty").withStyle(ChatFormatting.RED));
        }

        tooltip.add(Component.literal(text));
        SongInfo info = getSongInfo(stack);
        Language language = Language.getInstance();
        if (info != null) {
            if(info.transName != null && !info.transName.isEmpty()){
                name = language.getOrDefault("tooltips.netmusic.cd.trans_name");
                text = "§a▍ §7" + name + ": §6" + info.transName;
                tooltip.add(Component.literal(text));
            }

            if (info.artists != null && !info.artists.isEmpty()) {
                text = StringUtils.join(info.artists, " | ");
                name = language.getOrDefault("tooltips.netmusic.cd.artists");
                text = "§a▍ §7" + name + ": §3" + text;
                tooltip.add(Component.literal(text));
            }

            name = language.getOrDefault("tooltips.netmusic.cd.time");
            text = "§a▍ §7" + name + ": §5" + this.getSongTime(info.songTime);
            tooltip.add(Component.literal(text));
        }
    }

    private String getSongTime(int songTime) {
        int min = songTime / 60;
        int sec = songTime % 60;
        String minStr = min <= 9 ? "0" + min : "" + min;
        String secStr = sec <= 9 ? "0" + sec : "" + sec;
        String format = Language.getInstance().getOrDefault("tooltips.netmusic.cd.time.format");
        return String.format(format, minStr, secStr);
    }

    public static PlayMode getPlayMode(ItemStack stack){
        return getComponent(stack).playMode();
    }

    public static void setPlayMode(ItemStack stack, PlayMode mode){
        var c = getComponent(stack);
        var c1 = new MusicListComponent(c.songList(), mode, c.index());
        stack.set(NetMusicList.MUSIC_LIST_COMPONENT.get(), c1);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        var stack = context.getItemInHand();
        if(context.getLevel().getBlockState(context.getClickedPos()).is(InitBlocks.MUSIC_PLAYER.get())){
            if(getSongInfoList(stack).isEmpty()){return InteractionResult.PASS;}
            if(getSongIndex(stack) >= getSongInfoList(stack).size()){
                setSongIndex(stack, getSongInfoList(stack).size() - 1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        var stack = player.getItemInHand(usedHand);
        if(!stack.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            return InteractionResultHolder.pass(stack);
        }
        if(level.isClientSide){
            var l = getSongInfoList(stack);
            MusicSelectionScreen.open(l, getPlayMode(stack), getSongIndex(stack));
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        if(Objects.equals(super.getName(stack), Component.translatable(getDescriptionId(stack)))){
            return Component.translatable("item.net_music_list.name", getSongInfoList(stack).size());
        }
        return Component.translatable("item.net_music_list.info", super.getName(stack));
    }
}
