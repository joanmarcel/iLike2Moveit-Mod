package mac.ilike2moveit.mixin;

import com.blackgear.vanillabackport.common.level.entities.animal.PigVariant;
import mac.ilike2moveit.pig.PigBiomeVariants;
import mac.ilike2moveit.pig.PigBiomeVariantSpawnAccess;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Assigns persistent custom pig breeds after VanillaBackport has applied its climate fallback. */
@Mixin(Mob.class)
public abstract class PigBiomeVariantSpawnMixin {
    @Inject(method = "finalizeSpawn", at = @At("RETURN"), order = 1100)
    private void il2m$selectPersistentPigBiomeBreed(ServerLevelAccessor level,
            DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData spawnData,
            CallbackInfoReturnable<SpawnGroupData> cir) {
        if (!((Object) this instanceof Pig pig)) {
            return;
        }

        // VanillaBackport applies parent inheritance after finalizeSpawn. Never replace it.
        if (spawnType == MobSpawnType.BREEDING) {
            return;
        }

        PigVariant breed = PigBiomeVariants.forSpawnBiome(level.getBiome(pig.blockPosition()));
        if (breed != null && pig instanceof PigBiomeVariantSpawnAccess access) {
            // VanillaBackport's Pig-specific RETURN hook runs after this superclass method. Defer
            // the final write until Pig.tick, when every spawn hook has completed.
            access.il2m$setPendingBiomeBreed(breed);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void il2m$applyPendingPigBiomeBreed(CallbackInfo ci) {
        if (!((Object) this instanceof Pig pig)
                || pig.level().isClientSide()
                || !(pig instanceof PigBiomeVariantSpawnAccess access)) {
            return;
        }

        PigBiomeVariants.resolveNewPigBreed(pig);
    }
}
