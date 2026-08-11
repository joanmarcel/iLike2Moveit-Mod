package mac.ilike2moveit.pig;

import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;
import com.blackgear.vanillabackport.common.level.entities.animal.PigVariant;

import java.util.Locale;

/**
 * EMF + VanillaBackport compatibility for named, climate, biome-breed and piglet models.
 * Dedicated layer names let EMF bake warm_pig.jem/cold_pig.jem instead of routing both climates
 * through pig.jem; the renderer then forces the matching CEM atlas for each layer.
 */
public final class PigVariantCompat {

    public static final int SAVANNA_SPOTTED_CHANCE_PERCENT = 12;

    public static final ModelLayerLocation WARM_PIG_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "warm_pig"), "main"
    );
    public static final ModelLayerLocation COLD_PIG_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "cold_pig"), "main"
    );
    public static final ModelLayerLocation PIGLET_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "pig2"), "main"
    );
    public static final ModelLayerLocation LEGEND_PIG_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "pig4"), "main"
    );
    public static final ModelLayerLocation MR_PIGGY_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "pig5"), "main"
    );
    public static final ModelLayerLocation REDCOAT_PIG_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "pig6"), "main"
    );
    public static final ModelLayerLocation BIRCH_FOREST_PIG_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "birch_forest_pig"), "main"
    );
    public static final ModelLayerLocation SAVANNA_PIG_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "savanna_pig"), "main"
    );
    public static final ModelLayerLocation SAVANNA_SPOTTED_PIG_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "savanna_spotted_pig"), "main"
    );
    public static final ModelLayerLocation TAIGA_PIG_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "taiga_pig"), "main"
    );
    public static final ModelLayerLocation WARM_PIGLET_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "warm_piglet"), "main"
    );
    public static final ModelLayerLocation COLD_PIGLET_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "cold_piglet"), "main"
    );

    // Adult and named atlases are authored for the port's 64x64 rig and live next to their JEMs.
    public static final ResourceLocation WARM_PIG_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/warm_pig.png"
    );
    public static final ResourceLocation COLD_PIG_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/cold_pig.png"
    );
    public static final ResourceLocation PIGLET_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/pig_baby.png"
    );
    public static final ResourceLocation LEGEND_PIG_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/legend.png"
    );
    public static final ResourceLocation MR_PIGGY_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/mr_piggy.png"
    );
    public static final ResourceLocation REDCOAT_PIG_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/redcoats.png"
    );
    public static final ResourceLocation BIRCH_FOREST_PIG_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/birch_forest_pig.png"
    );
    public static final ResourceLocation SAVANNA_PIG_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/savanna_pig.png"
    );
    public static final ResourceLocation SAVANNA_SPOTTED_PIG_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/savanna_spotted_pig.png"
    );
    public static final ResourceLocation TAIGA_PIG_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/taiga_pig.png"
    );
    public static final ResourceLocation WARM_PIGLET_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/warm_pig_baby.png"
    );
    public static final ResourceLocation COLD_PIGLET_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/cold_pig_baby.png"
    );

    private PigVariantCompat() {
    }

    public enum NamedEasterEgg {
        LEGEND,
        MR_PIGGY,
        REDCOAT
    }

    /** Name-tag overrides are visual and win over age, climate and persistent biome breed. */
    public static NamedEasterEgg namedEasterEgg(Pig pig) {
        if (!pig.hasCustomName() || pig.getCustomName() == null) {
            return null;
        }
        return switch (pig.getCustomName().getString().strip().toLowerCase(Locale.ROOT)) {
            case "legend" -> NamedEasterEgg.LEGEND;
            case "mr. piggy" -> NamedEasterEgg.MR_PIGGY;
            case "redcoat" -> NamedEasterEgg.REDCOAT;
            default -> null;
        };
    }

    public static ResourceLocation namedTexture(Pig pig) {
        NamedEasterEgg easterEgg = namedEasterEgg(pig);
        if (easterEgg == null) {
            return null;
        }
        return switch (easterEgg) {
            case LEGEND -> LEGEND_PIG_TEXTURE;
            case MR_PIGGY -> MR_PIGGY_TEXTURE;
            case REDCOAT -> REDCOAT_PIG_TEXTURE;
        };
    }

    public static LayerDefinition createAdultPigLayer() {
        return createEmptyPigLayer();
    }

    public static LayerDefinition createPigletLayer() {
        return createEmptyPigLayer();
    }

    /**
     * Supplies the six anchors required by {@link PigModel} without baking vanilla cubes.
     *
     * <p>These are dedicated EMF layers: their JEM already owns the complete authored body and
     * legs. Baking {@code PigModel.createBodyLayer()} here leaves the four vanilla leg cubes under
     * the custom JEM and produces the eight-legged breed variants seen in game.</p>
     */
    private static LayerDefinition createEmptyPigLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        CubeListBuilder empty = CubeListBuilder.create();
        root.addOrReplaceChild("head", empty, PartPose.offset(0.0F, 12.0F, -6.0F));
        root.addOrReplaceChild("body", empty,
                PartPose.offsetAndRotation(0.0F, 11.0F, 2.0F, (float) (Math.PI / 2.0), 0.0F, 0.0F));
        root.addOrReplaceChild("right_hind_leg", empty, PartPose.offset(-3.0F, 18.0F, 7.0F));
        root.addOrReplaceChild("left_hind_leg", empty, PartPose.offset(3.0F, 18.0F, 7.0F));
        root.addOrReplaceChild("right_front_leg", empty, PartPose.offset(-3.0F, 18.0F, -5.0F));
        root.addOrReplaceChild("left_front_leg", empty, PartPose.offset(3.0F, 18.0F, -5.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static boolean isSavannaSpotted(Pig pig) {
        int optifineId = (int) (pig.getUUID().getLeastSignificantBits() & 0x7fffffffL);
        return optifineId % 100 >= 100 - SAVANNA_SPOTTED_CHANCE_PERCENT;
    }

    public static ResourceLocation biomeTexture(Pig pig, PigVariant variant) {
        if (pig.isBaby()) {
            return PIGLET_TEXTURE;
        }
        if (PigBiomeVariants.isBirchForest(variant)) {
            return BIRCH_FOREST_PIG_TEXTURE;
        }
        if (PigBiomeVariants.isSavanna(variant)) {
            return isSavannaSpotted(pig) ? SAVANNA_SPOTTED_PIG_TEXTURE : SAVANNA_PIG_TEXTURE;
        }
        if (PigBiomeVariants.isTaiga(variant)) {
            return TAIGA_PIG_TEXTURE;
        }
        return null;
    }
}
