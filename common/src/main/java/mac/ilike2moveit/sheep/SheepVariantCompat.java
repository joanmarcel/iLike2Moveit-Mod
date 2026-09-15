package mac.ilike2moveit.sheep;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;

/** Client-only layers for the selectable adult sheep base and dyed wool passes. */
public final class SheepVariantCompat {
    public static final ModelLayerLocation ALTERNATE_SHEEP_LAYER = layer("alternate_sheep");
    public static final ModelLayerLocation ALTERNATE_SHEEP_WOOL_LAYER = layer("alternate_sheep_wool");

    private SheepVariantCompat() {
    }

    private static ModelLayerLocation layer(String path) {
        return new ModelLayerLocation(
                ResourceLocation.fromNamespaceAndPath("minecraft", path), "main");
    }

    public static LayerDefinition createAlternateSheepLayer() {
        return createLayer();
    }

    public static LayerDefinition createAlternateSheepWoolLayer() {
        return createLayer();
    }

    /** Java sheep anchors for the Alternate JEM; EMF owns every visible cube in these layers. */
    private static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        CubeListBuilder empty = CubeListBuilder.create();
        root.addOrReplaceChild("head", empty, PartPose.offset(0.0F, 6.0F, -8.0F));
        root.addOrReplaceChild("body", empty,
                PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F,
                        (float) (Math.PI / 2.0), 0.0F, 0.0F));
        root.addOrReplaceChild("right_hind_leg", empty, PartPose.offset(-3.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("left_hind_leg", empty, PartPose.offset(3.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("right_front_leg", empty, PartPose.offset(-3.0F, 12.0F, -5.0F));
        root.addOrReplaceChild("left_front_leg", empty, PartPose.offset(3.0F, 12.0F, -5.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
