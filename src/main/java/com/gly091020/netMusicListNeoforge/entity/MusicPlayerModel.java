package com.gly091020.netMusicListNeoforge.entity;

import com.gly091020.netMusicListNeoforge.NetMusicList;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public class MusicPlayerModel<T extends Entity> extends EntityModel<MusicPlayerRenderer.MusicPlayerRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(NetMusicList.ModID,
            "music_player"), "main");
	private final ModelPart group;
	private final ModelPart bone;

	public MusicPlayerModel(ModelPart root) {
        super(root);
        this.group = root.getChild("group");
		this.bone = root.getChild("bone");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition group = partdefinition.addOrReplaceChild("group", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -9.0F, 6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 13).addBox(-12.0F, -9.0F, 6.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-13.0F, -9.0F, 6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, 23.0F, -7.0F));

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-14.0F, -6.0F, 4.0F, 12.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(8.0F, 24.0F, -8.0F));

		return LayerDefinition.create(meshdefinition, 64, 16);
	}
}