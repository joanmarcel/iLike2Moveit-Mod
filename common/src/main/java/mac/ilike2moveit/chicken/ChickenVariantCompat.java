package mac.ilike2moveit.chicken;

import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Chicken;

/** Dedicated warm layer that VanillaBackport 1.21.1 does not bake. */
public final class ChickenVariantCompat {
    public static final int ROOSTER_CHANCE_PERCENT = 10;

    public static final ModelLayerLocation WARM_CHICKEN_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "warm_chicken"), "main"
    );
    public static final ModelLayerLocation TINY_TAKEOVER_CHICKEN_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "tiny_takeover_chicken"), "main"
    );
    public static final ResourceLocation ROOSTER_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/rooster.png"
    );
    public static final ResourceLocation TINY_TAKEOVER_CHICKEN_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "textures/entity/chicken/tiny_takeover_chicken.png"
    );

    private ChickenVariantCompat() {
    }

    public static LayerDefinition createWarmChickenLayer() {
        return ChickenModel.createBodyLayer();
    }

    public static LayerDefinition createTinyTakeoverChickenLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        CubeListBuilder empty = CubeListBuilder.create();

        // ChickenModel requires these eight anchors. They deliberately contain no vanilla cubes:
        // the dedicated JEM owns the complete chick, so retaining createBodyLayer() here rendered
        // a second 64x32 chicken with the Tiny Takeover 16x16 atlas as floating yellow blocks.
        root.addOrReplaceChild("head", empty, PartPose.offset(0.0F, 15.0F, -4.0F));
        root.addOrReplaceChild("beak", empty, PartPose.offset(0.0F, 15.0F, -4.0F));
        root.addOrReplaceChild("red_thing", empty, PartPose.offset(0.0F, 15.0F, -4.0F));
        root.addOrReplaceChild("body", empty,
                PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, (float) (Math.PI / 2.0), 0.0F, 0.0F));
        root.addOrReplaceChild("right_leg", empty, PartPose.offset(-2.0F, 19.0F, 1.0F));
        root.addOrReplaceChild("left_leg", empty, PartPose.offset(1.0F, 19.0F, 1.0F));
        root.addOrReplaceChild("right_wing", empty, PartPose.offset(-4.0F, 13.0F, 0.0F));
        root.addOrReplaceChild("left_wing", empty, PartPose.offset(4.0F, 13.0F, 0.0F));
        return LayerDefinition.create(mesh, 16, 16);
    }

    /**
     * Reproduces ETF's seed: optifineId = UUID.lsb & 0x7fffffff. chicken.properties uses
     * weights 90/10, so the second suffix (rooster) covers values 90..99.
     */
    public static boolean isRooster(Chicken chicken) {
        if (chicken.isBaby()) {
            return false;
        }
        if (chicken.hasCustomName()) {
            String name = chicken.getCustomName().getString();
            if ("cooked".equalsIgnoreCase(name) || "nugget".equalsIgnoreCase(name)) {
                return false;
            }
        }
        int optifineId = (int) (chicken.getUUID().getLeastSignificantBits() & 0x7fffffffL);
        return optifineId % 100 >= 100 - ROOSTER_CHANCE_PERCENT;
    }
}
