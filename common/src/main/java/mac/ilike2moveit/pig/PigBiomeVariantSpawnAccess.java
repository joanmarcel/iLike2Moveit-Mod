package mac.ilike2moveit.pig;

import com.blackgear.vanillabackport.common.level.entities.animal.PigVariant;

/** One-shot handoff from the generic spawn method to the pig's first server tick. */
public interface PigBiomeVariantSpawnAccess {
    PigVariant il2m$getPendingBiomeBreed();

    void il2m$setPendingBiomeBreed(PigVariant variant);

    boolean il2m$isBiomeBreedResolved();

    void il2m$setBiomeBreedResolved(boolean resolved);
}
