package mac.ilike2moveit;

import mac.ilike2moveit.cat.CatAnimationBridge;
import mac.ilike2moveit.chicken.ChickenVariantCompat;
import mac.ilike2moveit.emf.EmfAsmMathsGuard;
import mac.ilike2moveit.pig.PigVariantCompat;
import mac.ilike2moveit.fox.MoveItParticles;
import mac.ilike2moveit.fox.FoxAnimationBridge;
import mac.ilike2moveit.fox.FoxSleepParticleEmitter;
import mac.ilike2moveit.wolf.WolfReunionTracker;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import traben.entity_model_features.EMFAnimationApi;

/**
 * iLike2MoveIt — NeoForge 1.21.1 client bridge for the resource pack of the same name.
 *
 * Repositions the trade item the villager displays (via HoldTradeOffersTask /
 * ShowTradesToPlayer -> MAINHAND -> CrossedArmsItemLayer) so that it FOLLOWS the animated
 * hands of the pack's CEM model (bone "alt_arms" / "arms") instead of staying pinned to the
 * chest. All the logic lives in the CrossedArmsItemLayerMixin mixin.
 * Client-side only; touches nothing on the server.
 */
@Mod(value = ILike2MoveItMod.MODID, dist = Dist.CLIENT)
public class ILike2MoveItMod {
    public static final String MODID = "ilike2moveit";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ILike2MoveItMod(IEventBus modEventBus) {
        LOGGER.info("[iLike2MoveIt] client bridge loaded.");
        // First of all: without this the villager does not animate and the symptom hides the cause.
        EmfAsmMathsGuard.enforce();
        modEventBus.addListener(ChickenVariantCompat::registerLayerDefinition);
        modEventBus.addListener(PigVariantCompat::registerLayerDefinition);
        MoveItParticles.register(modEventBus);
        modEventBus.addListener(MoveItParticles::registerParticleProviders);
        NeoForge.EVENT_BUS.addListener(WolfReunionTracker::onClientTick);
        NeoForge.EVENT_BUS.addListener(WolfReunionTracker::registerClientCommands);
        NeoForge.EVENT_BUS.addListener(FoxSleepParticleEmitter::onClientTick);
        try {
            EMFAnimationApi.registerSingletonAnimationVariable(
                    MODID,
                    WolfReunionTracker.EMF_RETURN_SEQUENCE,
                    "Secuencia consumible de reencuentro del lobo, persistente por UUID",
                    WolfReunionTracker::currentEmfReturnSequence
            );
            EMFAnimationApi.registerSingletonAnimationVariable(
                    MODID,
                    WolfReunionTracker.EMF_GREETING_NEAR,
                    "Proximidad autoritativa del reencuentro, con histeresis 8/11 por UUID",
                    WolfReunionTracker::currentEmfGreetingNear
            );
            EMFAnimationApi.registerSingletonAnimationVariable(
                    MODID,
                    CatAnimationBridge.EMF_LIE_DOWN_AMOUNT,
                    "Progreso vanilla interpolado del gato al acostarse sobre su owner",
                    CatAnimationBridge::currentEmfLieDownAmount
            );
            EMFAnimationApi.registerSingletonAnimationVariable(
                    MODID,
                    FoxAnimationBridge.EMF_SITTING,
                    "Flag SITTING real del zorro (perch/stalk); is_sitting vanilla no sirve (Fox no es Tamable)",
                    FoxAnimationBridge::currentSitting
            );
            EMFAnimationApi.registerSingletonAnimationVariable(
                    MODID,
                    FoxAnimationBridge.EMF_SLEEPING,
                    "Flag de siesta real del zorro (Fox.isSleeping, SleepGoal)",
                    FoxAnimationBridge::currentSleeping
            );
            EMFAnimationApi.registerSingletonAnimationVariable(
                    MODID,
                    FoxAnimationBridge.EMF_STALKING,
                    "Flag de acecho agachado del zorro (Fox.isCrouching, FLAG_CROUCHING/StalkPreyGoal)",
                    FoxAnimationBridge::currentStalking
            );
            LOGGER.info("[Wolf Reunion] variables EMF '{}' y '{}' registradas.",
                    WolfReunionTracker.EMF_RETURN_SEQUENCE, WolfReunionTracker.EMF_GREETING_NEAR);
            LOGGER.info("[Cat Resting] variable EMF '{}' registrada.",
                    CatAnimationBridge.EMF_LIE_DOWN_AMOUNT);
            LOGGER.info("[Fox] variables EMF '{}', '{}' y '{}' registradas.",
                    FoxAnimationBridge.EMF_SITTING, FoxAnimationBridge.EMF_SLEEPING,
                    FoxAnimationBridge.EMF_STALKING);
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo registrar la señal de reencuentro en EMF", exception);
        }
    }
}
