package com.gly091020.netMusicListNeoforge.client.cdpreview;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 唱片模型的包装：其余行为全部委托原模型，仅把 {@link #isCustomRenderer()} 改为 true，
 * 使 1.21.1 的 ItemRenderer 走 {@code BlockEntityWithoutLevelRenderer.renderByItem}。
 */
public class CDPreviewBakedModel implements BakedModel {
    private final BakedModel delegate;

    public CDPreviewBakedModel(BakedModel delegate) {
        this.delegate = delegate;
    }

    public BakedModel getDelegate() {
        return delegate;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return delegate.getQuads(state, direction, random);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return delegate.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return delegate.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return delegate.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return delegate.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return delegate.getOverrides();
    }

    @Override
    public ItemTransforms getTransforms() {
        return delegate.getTransforms();
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fancy) {
        return delegate.getRenderTypes(itemStack, fancy);
    }
}
