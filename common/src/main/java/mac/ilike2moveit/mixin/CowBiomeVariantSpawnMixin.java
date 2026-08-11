package mac.ilike2moveit.mixin;

import com.blackgear.vanillabackport.common.level.entities.animal.CowVariant;
import mac.ilike2moveit.cow.CowBiomeVariantSpawnAccess;
import mac.ilike2moveit.cow.CowBiomeVariants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Assigns persistent custom cow breeds after VanillaBackport's climate fallback. */
@Mixin(Mob.class)
public abstract class CowBiomeVariantSpawnMixin {
    @Inject(method = "finalizeSpawn", at = @At("RETURN"), order = 1100)
    private void il2m$selectPersistentCowBiomeBreed(ServerLevelAccessor level,
            DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData spawnData,
            CallbackInfoReturnable<SpawnGroupData> cir) {
        if (!((Object) this instanceof Cow cow) || spawnType == MobSpawnType.BREEDING) {
            return;
        }
        CowVariant breed = CowBiomeVariants.forSpawnBiome(level.getBiome(cow.blockPosition()));
        if (breed != null && cow instanceof CowBiomeVariantSpawnAccess access) {
            access.il2m$setPendingCowBiomeBreed(breed);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void il2m$applyPendingCowBiomeBreed(CallbackInfo ci) {
        if ((Object) this instanceof Cow cow && !cow.level().isClientSide()) {
            CowBiomeVariants.resolveNewCowBreed(cow);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void il2m$saveCowBiomeBreedMarker(CompoundTag tag, CallbackInfo ci) {
        if ((Object) this instanceof CowBiomeVariantSpawnAccess access) {
            tag.putBoolean("IL2MCowBiomeBreedResolved", access.il2m$isCowBiomeBreedResolved());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void il2m$loadCowBiomeBreedMarker(CompoundTag tag, CallbackInfo ci) {
        if ((Object) this instanceof CowBiomeVariantSpawnAccess access) {
            // Cows created before this marker are migrated once at entity join.
            access.il2m$setCowBiomeBreedResolved(
                    tag.getBoolean("IL2MCowBiomeBreedResolved"));
        }
    }
}
