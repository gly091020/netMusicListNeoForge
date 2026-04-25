package com.gly091020.netMusicListNeoforge.mixin;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import com.github.tartaricacid.netmusic.inventory.io.CDInput;
import com.github.tartaricacid.netmusic.inventory.io.CDOutput;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.item.NetMusicListItem;
import com.gly091020.netMusicListNeoforge.util.PlayMode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.gly091020.netMusicListNeoforge.NetMusicList.CONFIG;

@Mixin(value = CDBurnerMenu.class, remap = false)
public class CanBurnerCDListMixin {
    @Shadow
    @Final
    private CDInput input;

    @Shadow
    @Final
    private CDOutput output;

    @Inject(method = "setSongInfo", at = @At("HEAD"), cancellable = true)
    public void onSetInfo(ItemMusicCD.SongInfo setSongInfo, CallbackInfo ci){
        // 列表刻录暴力适配
        if (input.getResource(0).isEmpty() && output.getResource(0).is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            input.set(0, output.getResource(0), output.getAmountAsInt(0));
            output.set(0, ItemResource.of(Items.AIR.getDefaultInstance()), 0);
        }
        var itemStack = input.getResource(0).toStack();
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
