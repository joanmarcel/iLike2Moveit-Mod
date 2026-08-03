package mac.ilike2moveit;

import com.mojang.logging.LogUtils;
import mac.ilike2moveit.cat.CatAnimationBridge;
import mac.ilike2moveit.emf.EmfAsmMathsGuard;
import mac.ilike2moveit.fox.FoxAnimationBridge;
import mac.ilike2moveit.villager.VillagerVehicleBridge;
import mac.ilike2moveit.wolf.WolfReunionTracker;
import org.slf4j.Logger;
import traben.entity_model_features.EMFAnimationApi;

/**
 * Loader-independent core of the client bridge: identity, logger and the EMF animation variables
 * that Java 1.21.1 does not expose natively.
 *
 * <p>This mod is the client-side half of the iLike2MoveIt resource pack. It feeds the pack's CEM
 * models the animation signals vanilla keeps to itself (wolf reunion, cat resting, fox
 * sit/sleep/stalk), bakes the entity variant layers VanillaBackport leaves out (warm chicken, warm
 * pig), and anchors the villager's trade item to the animated {@code alt_arms}/{@code arms} bones
 * instead of leaving it pinned to the chest. Client-side only; touches nothing on the server.
 *
 * <p>Every loader entrypoint calls {@link #bootstrap()} exactly once during client init. Nothing in
 * here knows which loader is running.
 */
public final class MoveItCore {
    public static final String MODID = "ilike2moveit";
    public static final Logger LOGGER = LogUtils.getLogger();

    private MoveItCore() {
    }

    /** Enforces the EMF config and registers the animation variables. */
    public static void bootstrap() {
        // First of all: without this the villager does not animate and the symptom hides the cause.
        EmfAsmMathsGuard.enforce();
        try {
            EMFAnimationApi.registerSingletonAnimationVariable(
                    MODID,
                    WolfReunionTracker.EMF_RETURN_SEQUENCE,
                    "Consumable wolf reunion sequence, persistent per UUID",
                    WolfReunionTracker::currentEmfReturnSequence
            );
            EMFAnimationApi.registerSingletonAnimationVariable(
                    MODID,
                    WolfReunionTracker.EMF_GREETING_NEAR,
                    "Authoritative reunion proximity, 8/11 hysteresis per UUID",
                    WolfReunionTracker::currentEmfGreetingNear
            );
            EMFAnimationApi.registerSingletonAnimationVariable(
                    MODID,
                    CatAnimationBridge.EMF_LIE_DOWN_AMOUNT,
                    "Interpolated vanilla progress of the cat lying down on its owner",
                    CatAnimationBridge::currentEmfLieDownAmount
            );
            EMFAnimationApi.registerSingletonAnimationVariable(
                    MODID,
                    FoxAnimationBridge.EMF_SITTING,
                    "Real SITTING flag of the fox (perch/stalk); vanilla is_sitting is useless (Fox is not Tamable)",
                    FoxAnimationBridge::currentSitting
            );
            EMFAnimationApi.registerSingletonAnimationVariable(
                    MODID,
                    FoxAnimationBridge.EMF_SLEEPING,
                    "Real nap flag of the fox (Fox.isSleeping, SleepGoal)",
                    FoxAnimationBridge::currentSleeping
            );
            EMFAnimationApi.registerSingletonAnimationVariable(
                    MODID,
                    FoxAnimationBridge.EMF_STALKING,
                    "Crouched stalking flag of the fox (Fox.isCrouching, FLAG_CROUCHING/StalkPreyGoal)",
                    FoxAnimationBridge::currentStalking
            );
            EMFAnimationApi.registerSingletonAnimationVariable(
                    MODID,
                    VillagerVehicleBridge.EMF_IN_BOAT,
                    "Real boat passenger flag for villagers and wandering traders",
                    VillagerVehicleBridge::currentInBoat
            );
            LOGGER.info("[Wolf Reunion] EMF variables '{}' and '{}' registered.",
                    WolfReunionTracker.EMF_RETURN_SEQUENCE, WolfReunionTracker.EMF_GREETING_NEAR);
            LOGGER.info("[Cat Resting] EMF variable '{}' registered.",
                    CatAnimationBridge.EMF_LIE_DOWN_AMOUNT);
            LOGGER.info("[Fox] EMF variables '{}', '{}' and '{}' registered.",
                    FoxAnimationBridge.EMF_SITTING, FoxAnimationBridge.EMF_SLEEPING,
                    FoxAnimationBridge.EMF_STALKING);
            LOGGER.info("[Villager Vehicle] EMF variable '{}' registered.",
                    VillagerVehicleBridge.EMF_IN_BOAT);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not register the reunion signal in EMF", exception);
        }
    }
}
