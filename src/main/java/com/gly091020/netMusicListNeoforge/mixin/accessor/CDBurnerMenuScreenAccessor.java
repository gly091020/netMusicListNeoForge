package com.gly091020.netMusicListNeoforge.mixin.accessor;

import com.github.tartaricacid.netmusic.client.gui.CDBurnerMenuScreen;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CDBurnerMenuScreen.class, remap = false)
public interface CDBurnerMenuScreenAccessor {
    @Accessor("textField")
    EditBox getTextField();
}
