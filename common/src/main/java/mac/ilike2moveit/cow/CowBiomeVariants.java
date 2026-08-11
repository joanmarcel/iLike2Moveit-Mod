package mac.ilike2moveit.cow;

import com.blackgear.vanillabackport.common.api.variant.ModelAndTexture;
import com.blackgear.vanillabackport.common.api.variant.VariantDataHolder;
import com.blackgear.vanillabackport.common.api.variant.spawn.SpawnPrioritySelectors;
import com.blackgear.vanillabackport.common.level.entities.animal.CowVariant;
import com.blackgear.vanillabackport.common.level.entities.animal.CowVariants;
import mac.ilike2moveit.ILike2MoveIt;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/** Persistent cow breeds registered in VanillaBackport's own variant registry. */
public final class CowBiomeVariants {
    public static final CowVariant BIRCH_FOREST = register(
            "birch_forest", "optifine/cem/birch_forest_cow.png");

    private CowBiomeVariants() {
    }

    /** Forces class initialization on both logical sides before entities can spawn. */
    public static void bootstrap() {
        ILike2MoveIt.LOGGER.info(
                "[Cow Compat] persistent VanillaBackport breed registered: birch_forest."
        );
    }

    public static boolean isBirchForest(CowVariant variant) {
        return variant == BIRCH_FOREST;
    }

    /** Birch identity is selected exactly once and remains stable outside the spawn biome. */
    public static CowVariant forSpawnBiome(Holder<Biome> biome) {
        if (biome.is(Biomes.BIRCH_FOREST) || biome.is(Biomes.OLD_GROWTH_BIRCH_FOREST)) {
            return BIRCH_FOREST;
        }
        return null;
    }

    /** Resolves a genuinely new cow once, using the captured spawn biome or its join position. */
    public static boolean resolveNewCowBreed(Cow cow) {
        if (!(cow instanceof CowBiomeVariantSpawnAccess access)
                || access.il2m$isCowBiomeBreedResolved()) {
            return false;
        }

        CowVariant breed = access.il2m$getPendingCowBiomeBreed();
        if (breed == null) {
            breed = forSpawnBiome(cow.level().getBiome(cow.blockPosition()));
        }
        if (breed != null) {
            VariantDataHolder.<CowVariant>getHolder(cow).setVariantData(breed);
        }
        access.il2m$setPendingCowBiomeBreed(null);
        access.il2m$setCowBiomeBreedResolved(true);
        return breed != null;
    }

    private static CowVariant register(String name, String texturePath) {
        CowVariant variant = new CowVariant(
                new ModelAndTexture<>(
                        CowVariant.ModelType.NORMAL,
                        ResourceLocation.fromNamespaceAndPath("minecraft", texturePath)
                ),
                SpawnPrioritySelectors.EMPTY
        );
        CowVariants.REGISTRY.register(
                ResourceLocation.fromNamespaceAndPath(ILike2MoveIt.MODID, name),
                variant
        );
        return variant;
    }
}
