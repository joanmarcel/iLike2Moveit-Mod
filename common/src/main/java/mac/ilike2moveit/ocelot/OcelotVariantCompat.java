package mac.ilike2moveit.ocelot;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;

/** Client-only model layer for the optional Tiny Takeover baby ocelot. */
public final class OcelotVariantCompat {
    public static final ModelLayerLocation CLASSIC_BABY_OCELOT_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "classic_baby_ocelot"), "main"
    );
    public static final ModelLayerLocation TINY_TAKEOVER_OCELOT_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "tiny_takeover_ocelot"), "main"
    );
    public static final ResourceLocation TINY_TAKEOVER_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "textures/entity/ocelot/tiny_takeover/tiny_variant_00.png"
    );

    private OcelotVariantCompat() {
    }

    /** Empty vanilla anchors: the matching JEM owns the complete authored geometry. */
    public static LayerDefinition createTinyTakeoverOcelotLayer() {
        return createOcelotLayer(32, 32);
    }

    public static LayerDefinition createClassicBabyOcelotLayer() {
        return createOcelotLayer(44, 22);
    }

    private static LayerDefinition createOcelotLayer(int textureWidth, int textureHeight) {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        CubeListBuilder empty = CubeListBuilder.create();
        root.addOrReplaceChild("head", empty, PartPose.offset(0.0F, 15.0F, -9.0F));
        root.addOrReplaceChild("body", empty,
                PartPose.offsetAndRotation(0.0F, 12.0F, -10.0F,
                        (float) (Math.PI / 2.0), 0.0F, 0.0F));
        root.addOrReplaceChild("tail1", empty,
                PartPose.offsetAndRotation(0.0F, 15.0F, 8.0F, 0.9F, 0.0F, 0.0F));
        root.addOrReplaceChild("tail2", empty, PartPose.offset(0.0F, 20.0F, 14.0F));
        root.addOrReplaceChild("left_hind_leg", empty, PartPose.offset(1.1F, 18.0F, 5.0F));
        root.addOrReplaceChild("right_hind_leg", empty, PartPose.offset(-1.1F, 18.0F, 5.0F));
        root.addOrReplaceChild("left_front_leg", empty, PartPose.offset(1.2F, 14.1F, -5.0F));
        root.addOrReplaceChild("right_front_leg", empty, PartPose.offset(-1.2F, 14.1F, -5.0F));
        return LayerDefinition.create(mesh, textureWidth, textureHeight);
    }
}
