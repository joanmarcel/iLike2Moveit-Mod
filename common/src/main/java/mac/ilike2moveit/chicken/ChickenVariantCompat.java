package mac.ilike2moveit.chicken;

import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Chicken;

/** Dedicated warm layer that VanillaBackport 1.21.1 does not bake. */
public final class ChickenVariantCompat {
    public static final int ROOSTER_CHANCE_PERCENT = 10;

    public static final ModelLayerLocation WARM_CHICKEN_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "warm_chicken"), "main"
    );
    public static final ResourceLocation ROOSTER_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/rooster.png"
    );

    private ChickenVariantCompat() {
    }

    public static LayerDefinition createWarmChickenLayer() {
        return ChickenModel.createBodyLayer();
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
