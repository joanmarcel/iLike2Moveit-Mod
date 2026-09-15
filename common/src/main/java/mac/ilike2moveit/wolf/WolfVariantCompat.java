package mac.ilike2moveit.wolf;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

/** Client-only layers and breed textures for the optional Tiny Takeover puppy. */
public final class WolfVariantCompat {
    public static final ModelLayerLocation TINY_TAKEOVER_WOLF_LAYER = layer("tiny_takeover_wolf");
    public static final ModelLayerLocation TINY_TAKEOVER_WOLF_COLLAR_LAYER = layer("tiny_takeover_wolf_collar");
    public static final ModelLayerLocation TINY_TAKEOVER_WOLF_ARMOR_LAYER = layer("tiny_takeover_wolf_armor");
    public static final ResourceLocation TINY_TAKEOVER_COLLAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "textures/entity/wolf/tiny_takeover/collar.png"
    );

    private WolfVariantCompat() {
    }

    private static ModelLayerLocation layer(String path) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("minecraft", path), "main");
    }

    /** Empty vanilla anchors let EMF render only the authored JEM geometry. */
    public static LayerDefinition createTinyTakeoverWolfLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        CubeListBuilder empty = CubeListBuilder.create();
        var head = root.addOrReplaceChild("head", empty, PartPose.offset(-1.0F, 13.5F, -7.0F));
        head.addOrReplaceChild("real_head", empty, PartPose.ZERO);
        root.addOrReplaceChild("body", empty,
                PartPose.offsetAndRotation(0.0F, 14.0F, 2.0F, (float) (Math.PI / 2.0), 0.0F, 0.0F));
        root.addOrReplaceChild("upper_body", empty,
                PartPose.offsetAndRotation(-1.0F, 14.0F, -3.0F, (float) (Math.PI / 2.0), 0.0F, 0.0F));
        root.addOrReplaceChild("right_hind_leg", empty, PartPose.offset(-2.5F, 16.0F, 7.0F));
        root.addOrReplaceChild("left_hind_leg", empty, PartPose.offset(0.5F, 16.0F, 7.0F));
        root.addOrReplaceChild("right_front_leg", empty, PartPose.offset(-2.5F, 16.0F, -4.0F));
        root.addOrReplaceChild("left_front_leg", empty, PartPose.offset(0.5F, 16.0F, -4.0F));
        var tail = root.addOrReplaceChild("tail", empty,
                PartPose.offsetAndRotation(-1.0F, 12.0F, 8.0F, (float) (Math.PI / 5.0), 0.0F, 0.0F));
        tail.addOrReplaceChild("real_tail", empty, PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static ResourceLocation tinyTakeoverTexture(Wolf wolf) {
        String sourcePath = wolf.getTexture().getPath();
        String file = sourcePath.substring(sourcePath.lastIndexOf('/') + 1);
        String stem = file.endsWith(".png") ? file.substring(0, file.length() - 4) : file;
        String state = stem.endsWith("_angry") ? "angry" : stem.endsWith("_tame") ? "tame" : "wild";
        stem = stem.replaceFirst("_angry$", "").replaceFirst("_tame$", "");
        int breed = switch (stem) {
            case "wolf" -> 0;
            case "wolf_ashen" -> 1;
            case "wolf_black" -> 2;
            case "wolf_chestnut" -> 3;
            case "wolf_rusty" -> 4;
            case "wolf_snowy" -> 5;
            case "wolf_spotted" -> 6;
            case "wolf_striped" -> 7;
            case "wolf_woods" -> 8;
            default -> throw new IllegalStateException("Unknown vanilla wolf texture: " + sourcePath);
        };
        return ResourceLocation.fromNamespaceAndPath(
                "minecraft", String.format("textures/entity/wolf/tiny_takeover/tiny_variant_%02d_%s.png", breed, state)
        );
    }
}
