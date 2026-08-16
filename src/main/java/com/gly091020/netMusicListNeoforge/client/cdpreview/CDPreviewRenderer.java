package com.gly091020.netMusicListNeoforge.client.cdpreview;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.gly091020.netMusicListNeoforge.api.musicSource.MusicSource;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 唱片的自定义物品渲染器：按住预览键且有封面时渲染封面薄片，
 * 否则委托原模型渲染，保证默认显示不变。
 */
public class CDPreviewRenderer extends BlockEntityWithoutLevelRenderer {
    /** 加载动画贴图：32x256，竖排 8 帧，每帧 50ms（与 mcmeta 的 frametime=1 一致） */
    private static final int LOADING_FRAME_COUNT = 8;
    private static final long FRAME_TICK_MS = 50L;
    private static final ResourceLocation DEFAULT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "textures/gui/loading_icon.png");
    private static CDPreviewRenderer instance;
    private static BakedModel originalModel;

    private CDPreviewRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static CDPreviewRenderer getInstance() {
        if (instance == null) {
            instance = new CDPreviewRenderer();
        }
        return instance;
    }

    /** 烘焙阶段把原模型存下来，供默认渲染使用 */
    public static void setOriginalModel(BakedModel model) {
        originalModel = model;
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext context,
                             @NotNull PoseStack pose, @NotNull MultiBufferSource buffer,
                             int light, int overlay) {
        boolean preview = CDPreviewKeyMapping.isKeyDown();
        ItemMusicCD.SongInfo info = ItemMusicCD.getSongInfo(stack);
        if (preview && info != null) {
            MusicSource source = resolveSource(info);
            if (source != null) {
                String key = source.loaderType() + ":" + source.identifier();
                CDPreviewIconManager.requestIcon(key, source);
                CDPreviewIconManager.processPending();
                ResourceLocation icon = CDPreviewIconManager.getIcon(key);
                // 封面未就绪时用占位纹理，保证按住键时有预览反馈
                renderCover(pose, buffer, icon != null ? icon : DEFAULT_TEXTURE, light, overlay);
                return;
            }
        }
        renderDefault(stack, pose, buffer, light, overlay);
    }

    private void renderDefault(ItemStack stack, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        if (originalModel == null) return;
        // 与原版 ItemRenderer 的默认渲染路径保持一致：按 render pass / render type 逐个渲染
        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        for (BakedModel pass : originalModel.getRenderPasses(stack, stack.hasFoil())) {
            for (RenderType renderType : pass.getRenderTypes(stack, stack.hasFoil())) {
                VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(
                        buffer, renderType, true, stack.hasFoil());
                itemRenderer.renderModelLists(pass, stack, light, overlay, pose, consumer);
            }
        }
    }

    /**
     * 手写四边形渲染封面。entityTranslucent 是 QUADS 模式（4 顶点一 quad），
     * 不能写成 6 顶点，否则第二个三角形会丢。
     */
    private void renderCover(PoseStack pose, MultiBufferSource buffer, ResourceLocation texture,
                             int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.scale(-1, -1, 1);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        float v0;
        float v1;
        if (DEFAULT_TEXTURE.equals(texture)) {
            // 长条动画：按时间偏移 V 坐标逐帧播放
            int frame = (int) ((Util.getMillis() / FRAME_TICK_MS) % LOADING_FRAME_COUNT);
            float step = 1.0F / LOADING_FRAME_COUNT;
            v0 = frame * step;
            v1 = v0 + step;
        } else {
            v0 = 0.0F;
            v1 = 1.0F;
        }
        // 物品图标区域：居中 -0.5..0.5，z=0
        quad(consumer, pose.last(), -0.5F, -0.5F, 0.5F, 0.5F, 0.0F, 0.0F, v0, 1.0F, v1, light, overlay);
        pose.popPose();
    }

    /** 4 顶点一个完整四边形（QUADS 模式） */
    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x0, float y0, float x1, float y1, float z,
                             float u0, float v0, float u1, float v1, int light, int overlay) {
        consumer.addVertex(pose, x0, y0, z).setColor(255, 255, 255, 255).setUv(u0, v0)
                .setOverlay(overlay).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, x1, y0, z).setColor(255, 255, 255, 255).setUv(u1, v0)
                .setOverlay(overlay).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, x1, y1, z).setColor(255, 255, 255, 255).setUv(u1, v1)
                .setOverlay(overlay).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, x0, y1, z).setColor(255, 255, 255, 255).setUv(u0, v1)
                .setOverlay(overlay).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    private static MusicSource resolveSource(ItemMusicCD.SongInfo info) {
        if (info == null || info.songUrl == null) return null;
        MusicSource source = MusicSource.tryPasteFromSongInfo(info);
        if (source != null && !source.isEmpty()) return source;
        // 旧格式直链（网易云 mp3），解析出 id 后按网易云处理
        try {
            long id = NetMusicListUtil.getIdFromUrl(info.songUrl);
            return new MusicSource("netease", String.valueOf(id), info.songTime);
        } catch (Exception e) {
            return null;
        }
    }
}
