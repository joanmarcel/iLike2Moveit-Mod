package mac.ilike2moveit.cow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;

import java.util.Locale;

/** EMF-compatible model layers and atlases for named, climate and biome-breed cows. */
public final class CowVariantCompat {
    public static final ModelLayerLocation WARM_COW_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "il2m_warm_cow"), "main"
    );
    public static final ModelLayerLocation COLD_COW_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "il2m_cold_cow"), "main"
    );
    public static final ModelLayerLocation BIRCH_FOREST_COW_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "birch_forest_cow"), "main"
    );
    public static final ModelLayerLocation BIRCH_FOREST_COW_CALF_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "birch_forest_cow2"), "main"
    );
    public static final ModelLayerLocation BULL_COW_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "cow4"), "main"
    );
    public static final ModelLayerLocation BULL_COW_CALF_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "cow5"), "main"
    );

    public static final ResourceLocation WARM_COW_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/warm_cow.png"
    );
    public static final ResourceLocation COLD_COW_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/cold_cow.png"
    );
    public static final ResourceLocation BIRCH_FOREST_COW_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/birch_forest_cow.png"
    );
    public static final ResourceLocation BIRCH_FOREST_COW_CALF_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/birch_forest_cow_baby.png"
    );
    public static final ResourceLocation BULL_COW_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/bull.png"
    );
    public static final ResourceLocation BULL_COW_CALF_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/bull_baby.png"
    );
    public static final ResourceLocation VANILLA_COW_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "textures/entity/cow/cow.png"
    );
    private static final ResourceLocation WARM_COW_JEM = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/il2m_warm_cow.jem"
    );
    private static final ResourceLocation COLD_COW_JEM = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/il2m_cold_cow.jem"
    );
    private static final ResourceLocation BIRCH_FOREST_COW_JEM = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/birch_forest_cow.jem"
    );
    private static final ResourceLocation BIRCH_FOREST_COW_CALF_JEM = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/birch_forest_cow2.jem"
    );
    private static final ResourceLocation BULL_COW_JEM = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/cow4.jem"
    );
    private static final ResourceLocation BULL_COW_CALF_JEM = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/cow5.jem"
    );

    private CowVariantCompat() {
    }

    public static boolean hasWarmCowPortResources() {
        return hasResources(WARM_COW_JEM, WARM_COW_TEXTURE);
    }

    public static boolean hasColdCowPortResources() {
        return hasResources(COLD_COW_JEM, COLD_COW_TEXTURE);
    }

    public static boolean hasBirchForestCowPortResources() {
        return hasResources(BIRCH_FOREST_COW_JEM, BIRCH_FOREST_COW_TEXTURE);
    }

    public static boolean hasBirchForestCowCalfPortResources() {
        return hasResources(BIRCH_FOREST_COW_CALF_JEM, BIRCH_FOREST_COW_CALF_TEXTURE);
    }

    public static boolean hasBullCowPortResources() {
        return hasResources(BULL_COW_JEM, BULL_COW_TEXTURE);
    }

    public static boolean hasBullCowCalfPortResources() {
        return hasResources(BULL_COW_CALF_JEM, BULL_COW_CALF_TEXTURE);
    }

    /** Bull/Lidia is a presentation override and never rewrites persistent breed identity. */
    public static boolean isBullEasterEgg(Cow cow) {
        if (!cow.hasCustomName() || cow.getCustomName() == null) {
            return false;
        }
        String name = cow.getCustomName().getString().strip().toLowerCase(Locale.ROOT);
        return name.equals("bull") || name.equals("lidia");
    }

    /** Fail open to VanillaBackport whenever the resource pack is absent or incomplete. */
    private static boolean hasResources(ResourceLocation jem, ResourceLocation texture) {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft != null
                && minecraft.getResourceManager().getResource(jem).isPresent()
                && minecraft.getResourceManager().getResource(texture).isPresent();
    }

    /**
     * Supplies the six anchors required by CowModel without leaving VanillaBackport cubes under
     * the complete CEM model.
     */
    public static LayerDefinition createCowLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        CubeListBuilder empty = CubeListBuilder.create();
        root.addOrReplaceChild("head", empty, PartPose.offset(0.0F, 4.0F, -8.0F));
        root.addOrReplaceChild("body", empty,
                PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, (float) (Math.PI / 2.0), 0.0F, 0.0F));
        root.addOrReplaceChild("right_hind_leg", empty, PartPose.offset(-4.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("left_hind_leg", empty, PartPose.offset(4.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("right_front_leg", empty, PartPose.offset(-4.0F, 12.0F, -5.0F));
        root.addOrReplaceChild("left_front_leg", empty, PartPose.offset(4.0F, 12.0F, -5.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
