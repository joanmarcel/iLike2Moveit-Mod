package mac.ilike2moveit.fox;

import mac.ilike2moveit.ILike2MoveItMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Fox;
import traben.entity_model_features.EMFAnimationApi;
import traben.entity_model_features.utils.EMFEntity;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Exposes the REAL vanilla fox state to the pack's CEM.
 *
 * <p>The vanilla EMF variables are useless for the fox: {@code is_sitting} uses
 * {@code isInSittingPose()} (from {@code TamableAnimal}), which {@link Fox} does not implement, so it
 * always returns {@code false}. The pack needs the fox's own flag ({@link Fox#isSitting()}, set by
 * {@code PerchAndSearchGoal}/{@code StalkPreyGoal}), its nap ({@link Fox#isSleeping()}, set by
 * {@code SleepGoal}) and its crouched stalk ({@link Fox#isCrouching()}, flag {@code FLAG_CROUCHING}
 * set by {@code StalkPreyGoal}). They are registered as
 * {@code alt_fox_sitting}/{@code alt_fox_sleeping}/{@code alt_fox_stalking} (0/1) and build_fox.py
 * reads them as the TARGET of the sit/sleep/stalk integrators, replacing the debug cycles.
 */
public final class FoxAnimationBridge {
    public static final String EMF_SITTING = "alt_fox_sitting";
    public static final String EMF_SLEEPING = "alt_fox_sleeping";
    public static final String EMF_STALKING = "alt_fox_stalking";
    private static final Map<Fox, Boolean> LAST_SIT = new WeakHashMap<>();
    private static final Map<Fox, Boolean> LAST_SLEEP = new WeakHashMap<>();
    private static final Map<Fox, Boolean> LAST_STALK = new WeakHashMap<>();

    private FoxAnimationBridge() {
    }

    /** 1.0 while the current fox has the SITTING flag (perch/stalk), 0.0 otherwise. */
    public static Float currentSitting() {
        Fox fox = currentFox();
        if (fox == null) {
            return 0.0F;
        }
        boolean sitting = fox.isSitting();
        Boolean previous = LAST_SIT.put(fox, sitting);
        if (previous == null || previous != sitting) {
            ILike2MoveItMod.LOGGER.info("[Fox] sitting {} uuid={}", sitting ? "IN" : "OUT", fox.getUUID());
        }
        return sitting ? 1.0F : 0.0F;
    }

    /** 1.0 while the current fox is napping (SleepGoal), 0.0 otherwise. */
    public static Float currentSleeping() {
        Fox fox = currentFox();
        if (fox == null) {
            return 0.0F;
        }
        boolean sleeping = fox.isSleeping();
        Boolean previous = LAST_SLEEP.put(fox, sleeping);
        if (previous == null || previous != sleeping) {
            ILike2MoveItMod.LOGGER.info("[Fox] sleeping {} uuid={}", sleeping ? "IN" : "OUT", fox.getUUID());
        }
        return sleeping ? 1.0F : 0.0F;
    }

    /** 1.0 while the current fox is crouch-stalking (FLAG_CROUCHING, StalkPreyGoal), 0.0 otherwise. */
    public static Float currentStalking() {
        Fox fox = currentFox();
        if (fox == null) {
            return 0.0F;
        }
        boolean stalking = fox.isCrouching();
        Boolean previous = LAST_STALK.put(fox, stalking);
        if (previous == null || previous != stalking) {
            ILike2MoveItMod.LOGGER.info("[Fox] stalking {} uuid={}", stalking ? "IN" : "OUT", fox.getUUID());
        }
        return stalking ? 1.0F : 0.0F;
    }

    private static Fox currentFox() {
        EMFEntity emfEntity = EMFAnimationApi.getCurrentEntity();
        if (emfEntity instanceof Entity entity && entity instanceof Fox fox) {
            return fox;
        }
        return null;
    }
}
