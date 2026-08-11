package mac.ilike2moveit.cow;

import com.blackgear.vanillabackport.common.level.entities.animal.CowVariant;

/** One-shot handoff from the generic spawn method to the cow's first server tick. */
public interface CowBiomeVariantSpawnAccess {
    CowVariant il2m$getPendingCowBiomeBreed();

    void il2m$setPendingCowBiomeBreed(CowVariant variant);

    boolean il2m$isCowBiomeBreedResolved();

    void il2m$setCowBiomeBreedResolved(boolean resolved);
}
