package com.gly091020.netMusicListNeoforge.client.cdpreview;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

/**
 * CD 预览功能的事件注册：按键、物品渲染器、模型替换、模型层。
 */
public final class CDPreviewClient {
    private static final ModelResourceLocation MUSIC_CD_MODEL = new ModelResourceLocation(
            ResourceLocation.fromNamespaceAndPath("netmusic", "music_cd"), "inventory");

    private CDPreviewClient() {
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        CDPreviewKeyMapping.init();
        CDPreviewKeyMapping.register(event);
    }

    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return CDPreviewRenderer.getInstance();
            }
        }, InitItems.MUSIC_CD.get());
    }

    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        var models = event.getModels();
        var original = models.get(MUSIC_CD_MODEL);
        if (original != null) {
            CDPreviewRenderer.setOriginalModel(original);
            models.put(MUSIC_CD_MODEL, new CDPreviewBakedModel(original));
        } else {
            NetMusicList.LOGGER.warn("未找到唱片模型，CD 预览功能不可用：{}", MUSIC_CD_MODEL);
        }
    }

}
