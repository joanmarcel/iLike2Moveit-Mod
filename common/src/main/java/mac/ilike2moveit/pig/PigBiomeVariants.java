package mac.ilike2moveit.pig;

import com.blackgear.vanillabackport.common.api.variant.VariantDataHolder;
import com.blackgear.vanillabackport.common.api.variant.ModelAndTexture;
import com.blackgear.vanillabackport.common.api.variant.spawn.SpawnPrioritySelectors;
import com.blackgear.vanillabackport.common.level.entities.animal.PigVariant;
import com.blackgear.vanillabackport.common.level.entities.animal.PigVariants;
import mac.ilike2moveit.ILike2MoveIt;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.entity.animal.Pig;

/**
 * Persistent pig breeds registered in VanillaBackport's own variant registry.
 *
 * <p>The spawn hook assigns these exact-biome breeds only when the pig is first created.
 * VanillaBackport then synchronizes, saves and inherits the selected variant exactly like
 * {@code warm}, {@code cold} and {@code temperate}.
 */
public final class PigBiomeVariants {
    public static final PigVariant BIRCH_FOREST = register(
            "birch_forest", "optifine/cem/birch_forest_pig.png");
    public static final PigVariant SAVANNA = register(
            "savanna", "optifine/cem/savanna_pig.png");
    public static final PigVariant TAIGA = register(
            "taiga", "optifine/cem/taiga_pig.png");

    private PigBiomeVariants() {
    }

    /** Forces class initialization on both logical sides before entities can spawn. */
    public static void bootstrap() {
        ILike2MoveIt.LOGGER.info(
                "[Pig Compat] persistent VanillaBackport breeds registered: birch_forest, savanna, taiga."
        );
    }

    public static boolean isBirchForest(PigVariant variant) {
        return variant == BIRCH_FOREST;
    }

    public static boolean isSavanna(PigVariant variant) {
        return variant == SAVANNA;
    }

    public static boolean isTaiga(PigVariant variant) {
        return variant == TAIGA;
    }

    /**
     * Resolves only the biome present at initial spawn. This deliberately uses exact biome keys
     * rather than a live position-based render check, so the breed remains stable after the pig
     * walks elsewhere and VanillaBackport can persist/inherit it normally.
     */
    public static PigVariant forSpawnBiome(Holder<Biome> biome) {
        if (biome.is(Biomes.BIRCH_FOREST) || biome.is(Biomes.OLD_GROWTH_BIRCH_FOREST)) {
            return BIRCH_FOREST;
        }
        if (biome.is(Biomes.SAVANNA)
                || biome.is(Biomes.SAVANNA_PLATEAU)
                || biome.is(Biomes.WINDSWEPT_SAVANNA)) {
            return SAVANNA;
        }
        if (biome.is(Biomes.TAIGA)
                || biome.is(Biomes.OLD_GROWTH_PINE_TAIGA)
                || biome.is(Biomes.OLD_GROWTH_SPRUCE_TAIGA)) {
            return TAIGA;
        }
        return null;
    }

    /** Resolves a genuinely new pig once, using the captured spawn biome or its join position. */
    public static boolean resolveNewPigBreed(Pig pig) {
        if (!(pig instanceof PigBiomeVariantSpawnAccess access)) {
            return false;
        }
        if (access.il2m$isBiomeBreedResolved()) {
            return false;
        }

        PigVariant breed = access.il2m$getPendingBiomeBreed();
        if (breed == null) {
            breed = forSpawnBiome(pig.level().getBiome(pig.blockPosition()));
        }

        if (breed != null) {
            VariantDataHolder.<PigVariant>getHolder(pig).setVariantData(breed);
        }
        access.il2m$setPendingBiomeBreed(null);
        access.il2m$setBiomeBreedResolved(true);
        return breed != null;
    }

    private static PigVariant register(String name, String texturePath) {
        PigVariant variant = new PigVariant(
                new ModelAndTexture<>(
                        PigVariant.ModelType.NORMAL,
                        ResourceLocation.fromNamespaceAndPath("minecraft", texturePath)
                ),
                SpawnPrioritySelectors.EMPTY
        );
        PigVariants.REGISTRY.register(
                ResourceLocation.fromNamespaceAndPath(ILike2MoveIt.MODID, name),
                variant
        );
        return variant;
    }
}
