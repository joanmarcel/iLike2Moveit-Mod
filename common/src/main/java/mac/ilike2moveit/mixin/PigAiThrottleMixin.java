package mac.ilike2moveit.mixin;

import mac.ilike2moveit.pig.PigRestingController;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Pig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Runs the expensive vanilla AI pass at 1 Hz only while a pig is fully lying down. */
@Mixin(Mob.class)
public abstract class PigAiThrottleMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void il2m$tickPigResting(CallbackInfo ci) {
        if ((Object) this instanceof Pig pig && !pig.level().isClientSide()) {
            PigRestingController.serverTick(pig);
        }
    }

    @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
    private void il2m$throttleLyingPigAi(CallbackInfo ci) {
        if ((Object) this instanceof Pig pig && !PigRestingController.shouldRunFullAi(pig)) {
            ci.cancel();
        }
    }
}
