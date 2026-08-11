package mac.ilike2moveit.mixin;

import com.blackgear.vanillabackport.common.level.entities.animal.CowVariant;
import mac.ilike2moveit.cow.CowBiomeVariantSpawnAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Stores the one-shot migration marker and protects VanillaBackport parent inheritance. */
@Mixin(Cow.class)
public abstract class CowBiomeVariantMixin extends Animal implements CowBiomeVariantSpawnAccess {
    @Unique private CowVariant il2m$pendingCowBiomeBreed;
    @Unique private boolean il2m$cowBiomeBreedResolved;

    protected CowBiomeVariantMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Inject(method = "getBreedOffspring", at = @At("RETURN"), order = 1100)
    private void il2m$preserveInheritedCowBreed(ServerLevel level, AgeableMob mate,
            CallbackInfoReturnable<Cow> cir) {
        Cow offspring = cir.getReturnValue();
        if (offspring instanceof CowBiomeVariantSpawnAccess access) {
            access.il2m$setCowBiomeBreedResolved(true);
        }
    }

    @Override public CowVariant il2m$getPendingCowBiomeBreed() { return il2m$pendingCowBiomeBreed; }
    @Override public void il2m$setPendingCowBiomeBreed(CowVariant variant) {
        il2m$pendingCowBiomeBreed = variant;
    }
    @Override public boolean il2m$isCowBiomeBreedResolved() { return il2m$cowBiomeBreedResolved; }
    @Override public void il2m$setCowBiomeBreedResolved(boolean resolved) {
        il2m$cowBiomeBreedResolved = resolved;
    }
}
