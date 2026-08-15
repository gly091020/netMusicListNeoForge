package com.gly091020.netMusicListNeoforge.mixin;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.inventory.ComputerMenu;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.item.NetMusicListItem;
import com.gly091020.netMusicListNeoforge.util.PlayMode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.gly091020.netMusicListNeoforge.NetMusicList.CONFIG;

@Mixin(ComputerMenu.class)
public class CanBurnerCDListComputerMixin {
    @Shadow(remap = false)
    private final ItemStackHandler input = new ItemStackHandler() {
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.is(InitItems.MUSIC_CD.get()) || stack.is(NetMusicList.MUSIC_LIST_ITEM.get());
        }
    };

    @Shadow
    @Final
    private ItemStackHandler output;

    @Inject(method = "setSongInfo", at = @At("HEAD"), cancellable = true)
    public void onSetInfo(ItemMusicCD.SongInfo setSongInfo, CallbackInfo ci){
        // 列表刻录暴力适配
        if (input.getStackInSlot(0).isEmpty() && output.getStackInSlot(0).is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            input.setStackInSlot(0, output.getStackInSlot(0));
            output.setStackInSlot(0, Items.AIR.getDefaultInstance());
        }
        var itemStack = input.getStackInSlot(0);
        var size = NetMusicListItem.getSongInfoList(itemStack).size();
        if(itemStack.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            var index = NetMusicListItem.getSongIndex(itemStack);
            if(index < 0 || index >= size) {
                NetMusicListItem.setSongIndex(itemStack, size);
            }
            NetMusicListItem.setPlayMode(itemStack, PlayMode.LOOP);
        }
        if(size >= CONFIG.maxImportList){
            ci.cancel();
        }
    }
}
