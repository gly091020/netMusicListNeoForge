// MusicPlayerRenderer.java
package com.gly091020.netMusicListNeoforge.entity;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class MusicPlayerRenderer<T extends Entity> extends EntityRenderer<T> {
    private final MusicPlayerModel<T> model;
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "textures/entity/music_player.png");

    public MusicPlayerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new MusicPlayerModel<>(context.bakeLayer(MusicPlayerModel.LAYER_LOCATION));
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(@NotNull T entity, float entityYaw, float partialTicks,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer,
                       int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        var color = 0xFFFFFFFF;

        if(NetMusicList.CONFIG.glyMusicEntity && entity instanceof MusicPlayerEntity entity1 && entity1.isPlaying()){
            var time = System.currentTimeMillis();
            var v1 = (Math.sin(time / 300d) * 0.5 + 0.5) + 0.5;
            var v2 = (Math.cos(time / 300d) * 0.5 + 0.5) + 0.5;
            poseStack.scale((float) (1 * v1), (float) (1 * v2), 1);
            color = hsvToArgb((float) (Math.cos(time / 500d) * 0.5 + 0.5) * 360f, 1, 1, 1);
        }

        poseStack.scale(-1, -1, -1);
        poseStack.translate(0, -1.5, 0.5 / 16);

        VertexConsumer vertexConsumer = buffer.getBuffer(
                this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight,
                OverlayTexture.NO_OVERLAY, color);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return TEXTURE;
    }

    public static int hsvToArgb(float h, float s, float v, int alpha) {
        Color color = Color.getHSBColor(h / 360.0f, s, v);
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        // 组合成ARGB整数
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}