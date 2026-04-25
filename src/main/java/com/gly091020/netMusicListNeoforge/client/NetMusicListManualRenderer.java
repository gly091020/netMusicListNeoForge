package com.gly091020.netMusicListNeoforge.client;

import com.github.tartaricacid.netmusic.client.renderer.MusicPlayerItemRenderer;
import com.gly091020.netMusicListNeoforge.NetMusicList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.fml.ModList;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public class NetMusicListManualRenderer implements NoDataSpecialModelRenderer {
    // 用了很多来自IAM Music Player的渲染代码
    private static final Component DEV_TEXT = Component.literal("5112151111121");
    private static final Component MANUAL_TEXT = Component.translatable("manual.net_music_list.manual");
    private static final Component COVER_INFO1_TEXT = Component.translatable("manual.net_music_list.cover1");
    private static final Component COVER_INFO2_TEXT = Component.translatable("manual.net_music_list.cover2");
    private static final Component MOD_NAME_TEXT = Component.translatable("modmenu.nameTranslation.net_music_list");
    private final Component MOD_VERSION_TEXT;

    public static ItemModel model;
    private static final Identifier MODEL_LOCATION = Identifier.fromNamespaceAndPath(NetMusicList.ModID, "manual_model");
    public NetMusicListManualRenderer() {
        var info = ModList.get().getModFileById(NetMusicList.ModID).getMods().getFirst();
        MOD_VERSION_TEXT = Component.literal("v").append(Component.literal(info.getVersion().toString()));
    }

    private static final NetMusicListManualRenderer INSTANCE = new NetMusicListManualRenderer();

    public static NetMusicListManualRenderer getInstance() {
        return INSTANCE;
    }

    public void renderText(PoseStack poseStack,
                           Component text,
                           int light,
                           float x,
                           float y,
                           float scale,
                           boolean center,
                           int color,
                           SubmitNodeCollector collector) {

        poseStack.pushPose();
        poseStack.translate(1 / 16f * x, 0.5f / 16f, 1 / 16f * y);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
        poseStack.scale(
                0.010416667F * scale,
                -0.010416667F * scale,
                0.010416667F * scale
        );
        float offsetX = 0f;
        if (center) {
            offsetX = -Minecraft.getInstance().font.width(text) / 2f;
        }
        FormattedCharSequence seq = text.getVisualOrderText();
        collector.submitText(
                poseStack,
                offsetX,
                0,
                seq,
                false,
                Font.DisplayMode.NORMAL,
                color,
                light,
                0,
                0
        );

        poseStack.popPose();
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int i1, boolean b, int i2) {
        model = Minecraft.getInstance().getModelManager().getItemModel(MODEL_LOCATION);
        renderText(poseStack, DEV_TEXT, i, 9f, 15.75f, 0.4f, false, 0, submitNodeCollector);
        renderText(poseStack, MANUAL_TEXT, i, 4.225f / 2f, 15.75f, 0.54f, true, 0xFFFFFFFF, submitNodeCollector);
        renderText(poseStack, MOD_NAME_TEXT, i, 9.85f, 14.5f, 0.6f, false, 0, submitNodeCollector);
        renderText(poseStack, MOD_VERSION_TEXT, i, 9.85f, 13.5f, 0.4f, false, 0, submitNodeCollector);
        renderText(poseStack, COVER_INFO1_TEXT, i, 10f / 2f, 12, 0.4f, true, 0xFFFFFFFF, submitNodeCollector);
        renderText(poseStack, COVER_INFO2_TEXT, i, 10f / 2f, 11.4514f, 0.4f, true, 0XFFFFFFFF, submitNodeCollector);
        //                                                       意义明确的数值
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {

    }

    public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<MusicPlayerItemRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new MusicPlayerItemRenderer.Unbaked());

        public MapCodec<MusicPlayerItemRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        public NetMusicListManualRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new NetMusicListManualRenderer();
        }
    }
}
