package mac.ilike2moveit.cat;

import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cat;

/** Client-only model layers for the optional Tiny Takeover kitten presentation. */
public final class CatVariantCompat {
    public static final ModelLayerLocation CLASSIC_BABY_CAT_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "classic_baby_cat"), "main"
    );
    public static final ModelLayerLocation CLASSIC_BABY_CAT_COLLAR_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "classic_baby_cat_collar"), "main"
    );
    public static final ModelLayerLocation TINY_TAKEOVER_CAT_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "tiny_takeover_cat"), "main"
    );
    public static final ModelLayerLocation TINY_TAKEOVER_CAT_COLLAR_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "tiny_takeover_cat_collar"), "main"
    );

    private CatVariantCompat() {
    }

    /**
     * {@link CatModel} requires the eight vanilla anchors. They stay empty because the JEM owns
     * the complete authored kitten; baking vanilla cubes here would duplicate the animal.
     */
    public static LayerDefinition createTinyTakeoverCatLayer() {
        return createCatLayer(32, 32);
    }

    public static LayerDefinition createClassicBabyCatLayer() {
        return createCatLayer(44, 22);
    }

    private static LayerDefinition createCatLayer(int textureWidth, int textureHeight) {
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

    public static ResourceLocation tinyTakeoverTexture(Cat cat) {
        String sourcePath = cat.getTextureId().getPath();
        String fileName = sourcePath.substring(sourcePath.lastIndexOf('/') + 1);
        String publicName = switch (fileName) {
            case "white.png" -> "tiny_variant_00.png";
            case "black.png" -> "tiny_variant_01.png";
            case "red.png" -> "tiny_variant_02.png";
            case "siamese.png" -> "tiny_variant_03.png";
            case "british_shorthair.png" -> "tiny_variant_04.png";
            case "calico.png" -> "tiny_variant_05.png";
            case "persian.png" -> "tiny_variant_06.png";
            case "ragdoll.png" -> "tiny_variant_07.png";
            case "tabby.png" -> "tiny_variant_08.png";
            case "all_black.png" -> "tiny_variant_09.png";
            case "jellie.png" -> "tiny_variant_10.png";
            default -> throw new IllegalStateException("Unknown vanilla cat texture: " + sourcePath);
        };
        return ResourceLocation.fromNamespaceAndPath(
                "minecraft", "textures/entity/cat/tiny_takeover/" + publicName
        );
    }
}
