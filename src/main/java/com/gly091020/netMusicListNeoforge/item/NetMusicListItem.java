package com.gly091020.netMusicListNeoforge.item;

import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.client.MusicSelectionScreen;
import com.gly091020.netMusicListNeoforge.item.components.MusicListComponent;
import com.gly091020.netMusicListNeoforge.util.PlayMode;
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
import java.util.List;
import java.util.Objects;

public class NetMusicListItem extends ItemMusicCD {
    public NetMusicListItem() {
        super();
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return 1;
    }

    public static MusicListComponent getComponent(ItemStack stack){
        return stack.getOrDefault(NetMusicList.MUSIC_LIST_COMPONENT, MusicListComponent.getInstance());
    }

    public static List<SongInfo> getSongInfoList(ItemStack stack) {
        return getComponent(stack).songInfos();
    }

    public static int nextMusic(ItemStack stack){
        if(stack.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            switch (getPlayMode(stack)){
                case RANDOM -> {
                    var i = RandomSource.create().nextInt(0, getSongInfoList(stack).size() - 1);
                    setSongIndex(stack, i);
                    return i;
                }
                case SEQUENTIAL -> {
                    var i = getSongIndex(stack) + 1;
                    if(i >= getSongInfoList(stack).size()){
                        i = 0;
                    }
                    setSongIndex(stack, i);
                    return i;
                }
            }
            return getSongIndex(stack);
        }
        return 0;
    }

    public static SongInfo getSongInfo(ItemStack stack) {
        var l = getSongInfoList(stack);
        if(l.isEmpty()){
            return null;
        }
        var i = getSongIndex(stack);
        if(i >= 0 && i >= l.size()){return null;}
        if(i < 0){return null;}
        return l.get(i);
    }

    public static Integer getSongIndex(ItemStack stack){
        return getComponent(stack).index();
    }

    public static void deleteSong(ItemStack stack, int index){
        var c = getComponent(stack);
        var l = new ArrayList<>(c.songInfos());
        l.remove(index);
        stack.set(NetMusicList.MUSIC_LIST_COMPONENT,
                new MusicListComponent(l, c.playMode(), 0));
    }

    public static void moveSong(ItemStack stack, int from, int to){
        var c = getComponent(stack);
        var l = new ArrayList<>(c.songInfos());
        var j = l.get(from);
        l.set(from, l.get(to));
        l.set(to, j);
        stack.set(NetMusicList.MUSIC_LIST_COMPONENT,
                new MusicListComponent(l, c.playMode(), c.index()));
    }

    public static void setSongIndex(ItemStack stack, Integer index){
        var c = getComponent(stack);
        stack.set(NetMusicList.MUSIC_LIST_COMPONENT,
                new MusicListComponent(c.songInfos(), c.playMode(), index));
    }

    public static ItemStack setSongInfo(SongInfo info, ItemStack stack) {
        var c = getComponent(stack);
        var l = new ArrayList<>(c.songInfos());
        var index = c.index();

        if(c.index() < 0 || c.index() >= l.size()) {
            l.add(info);
            index++;
        } else
            l.set(c.index(), info);

        stack.set(NetMusicList.MUSIC_LIST_COMPONENT,
                new MusicListComponent(l, c.playMode(), index));
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
        stack.set(NetMusicList.MUSIC_LIST_COMPONENT,
                new MusicListComponent(c.songInfos(), mode, c.index()));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if(context.getPlayer() == null){return InteractionResult.PASS;}
        var stack = context.getPlayer().getMainHandItem();
        if(!stack.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            return InteractionResult.PASS;
        }
        if(context.getLevel().getBlockState(context.getClickedPos()).is(InitBlocks.MUSIC_PLAYER.get())){
            if(getSongInfoList(stack).isEmpty()){return InteractionResult.PASS;}
            if(getSongIndex(stack) >= getSongInfoList(stack).size()){
                setSongIndex(stack, getSongInfoList(stack).size() - 1);
            }
            return InteractionResult.PASS;
        }
        if(context.getLevel().isClientSide){
            var l = getSongInfoList(stack);
            // 只要我直接移到新版本就行了（简单粗暴）
            MusicSelectionScreen.open(l, getPlayMode(stack), getSongIndex(stack));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        var stack = player.getItemInHand(hand);
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
