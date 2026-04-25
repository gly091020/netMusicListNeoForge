package com.gly091020.netMusicListNeoforge.entity;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.awt.*;

public class MusicPlayerRenderer<T extends MusicPlayerEntity> extends EntityRenderer<MusicPlayerEntity, MusicPlayerRenderer.MusicPlayerRenderState> {
    private final MusicPlayerModel<T> model;
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(NetMusicList.ModID, "textures/entity/music_player.png");

    public MusicPlayerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new MusicPlayerModel<>(context.bakeLayer(MusicPlayerModel.LAYER_LOCATION));
        this.shadowRadius = 0.5F;
    }

    public static class MusicPlayerRenderState extends EntityRenderState {

        public float time;

        public float scaleX = 1.0F;
        public float scaleY = 1.0F;

        public int color = 0xFFFFFFFF;

        public float yRot = 0;
    }

    @Override
    public MusicPlayerRenderState createRenderState() {
        return new MusicPlayerRenderState();
    }

    @Override
    public void extractRenderState(MusicPlayerEntity entity, MusicPlayerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        float time = entity.tickCount + partialTicks;
        state.time = time;

        if (NetMusicList.CONFIG.glyMusicEntity && entity.isPlaying()) {

            state.scaleX = (float)(1.0 + Math.sin(time * 0.1) * 0.2);
            state.scaleY = (float)(1.0 + Math.cos(time * 0.1) * 0.2);

            state.color = hsvToArgb(
                    (float)(Math.cos(time * 0.05) * 0.5 + 0.5) * 360f,
                    1, 1, 1
            );
        } else {
            state.scaleX = 1.0F;
            state.scaleY = 1.0F;
            state.color = 0xFFFFFFFF;
        }
        state.yRot = entity.getYRot();
    }

    @Override
    public void submit(MusicPlayerRenderState state,
                       PoseStack poseStack,
                       SubmitNodeCollector collector,
                       @NonNull CameraRenderState cameraState) {

        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));

        poseStack.scale(state.scaleX, state.scaleY, 1.0F);

        poseStack.scale(-1, -1, -1);
        poseStack.translate(0, -1.5, 0.5 / 16.0);

        collector.submitModel(model, state, poseStack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, 0, null);

        poseStack.popPose();

        super.submit(state, poseStack, collector, cameraState);
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