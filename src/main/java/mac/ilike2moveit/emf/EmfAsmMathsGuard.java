package mac.ilike2moveit.emf;

import mac.ilike2moveit.ILike2MoveItMod;
import traben.entity_model_features.EMF;
import traben.entity_model_features.config.EMFConfig;

/**
 * Forces {@code asmMaths=false} in EMF's config so the user does not have to edit it by hand.
 *
 * <p><b>Why it is needed.</b> EMF 3.x with {@code asmMaths=true} compiles each animation into ONE
 * bytecode method. The pack's villager animation block (~152k characters) exceeds the <b>JVM limit of
 * 64 KB per method</b> and throws {@code MethodTooLargeException}: the villager stops animating and
 * stays static, with no symptom pointing at the cause. With {@code false}, EMF uses its expression
 * tree interpreter, which has no such limit. It costs a little more CPU and it works.
 *
 * <p><b>Why it is called from a mixin and not just on mod construction.</b> Applying it once at
 * startup is NOT enough, and the failure is silent. {@code EMFManager.resetInstance()} calls
 * {@code config().loadFromFile()}, and {@code MixinResourceReloadStart} invokes it on <b>every
 * resource reload</b> — including the initial one at startup and every F3+T. That is, EMF re-reads the
 * flag from disk right before rebuilding the models, overwriting whatever we had set in memory. That
 * is why {@link mac.ilike2moveit.mixin.EmfManagerResetMixin} reapplies it on the RETURN of
 * {@code resetInstance}, which is after the re-read and before any {@code .jem} gets parsed.
 *
 * <p>EMF reads the flag in a single place, {@code EMFManager.setupAnimationsFromJemToModel(...)},
 * invoked when parsing each {@code .jem}. Reapplying it at the end of every reset guarantees that spot
 * always sees it as {@code false}.
 *
 * <p><b>What it does NOT do.</b> It does not rewrite {@code config/entity_model_features.json}. The
 * change lives only in memory: it is idempotent, and anyone uninstalling this mod gets their
 * configuration back untouched. It also leaves the value alone if it was already {@code false}.
 *
 * <p>Should a future EMF version change these internals, the failure degrades to a log warning with
 * the manual instruction, never to a crash: this tweak is a convenience, not a startup requirement.
 */
public final class EmfAsmMathsGuard {

    /** The warning is useful once; repeating it on every F3+T only clutters the log. */
    private static boolean announced;

    private EmfAsmMathsGuard() {
    }

    public static void enforce() {
        try {
            EMFConfig config = EMF.config().getConfig();
            if (!config.asmMaths) {
                announceOnce(() -> ILike2MoveItMod.LOGGER.info(
                        "[EMF] asmMaths was already false; nothing to adjust."));
                return;
            }
            config.asmMaths = false;
            // Without a debug level there would be no way to check that the reapply after each reload
            // actually happens: the warning below is emitted only once on purpose.
            ILike2MoveItMod.LOGGER.debug("[EMF] asmMaths reasserted to false.");
            announceOnce(() -> ILike2MoveItMod.LOGGER.warn(
                    "[EMF] asmMaths forced to false in memory only (your entity_model_features.json is "
                    + "left untouched). With true, the villager animation block exceeds the JVM limit of "
                    + "64 KB per method and the mob stays static. Reapplied on every resource reload."));
        } catch (Throwable failure) {
            announceOnce(() -> ILike2MoveItMod.LOGGER.warn(
                    "[EMF] could not adjust asmMaths automatically ({}). If a mob stays static, set "
                    + "\"asmMaths\": false by hand in config/entity_model_features.json.",
                    failure.toString()));
        }
    }

    private static void announceOnce(Runnable message) {
        if (!announced) {
            announced = true;
            message.run();
        }
    }
}
