package mac.ilike2moveit.mixin;

import mac.ilike2moveit.emf.EmfAsmMathsGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_model_features.EMFManager;

/**
 * Reapplies {@code asmMaths=false} after EMF re-reads its config from disk.
 *
 * <p>{@code EMFManager.resetInstance()} calls {@code config().loadFromFile()}, and
 * {@code MixinResourceReloadStart} invokes it on every resource reload: the initial one at startup and
 * every F3+T. Without this inject, the value the mod sets on construction is overwritten there, and the
 * {@code .jem} files get parsed with {@code asmMaths=true} — the villager stops animating and nothing
 * in the log points at the cause.
 *
 * <p>Hooked on RETURN: after the re-read, before any model is parsed.
 *
 * <p>{@code remap = false} because {@code EMFManager} belongs to a mod, not to Minecraft: there are no
 * mappings to apply and remapping it would make target resolution fail.
 *
 * @see EmfAsmMathsGuard
 */
@Mixin(value = EMFManager.class, remap = false)
public class EmfManagerResetMixin {

    @Inject(method = "resetInstance", at = @At("RETURN"), remap = false)
    private static void ilike2moveit$keepAsmMathsOff(CallbackInfo callbackInfo) {
        EmfAsmMathsGuard.enforce();
    }
}
