package mac.ilike2moveit.pig;

import mac.ilike2moveit.network.ServerBridgeState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pig;
import traben.entity_model_features.EMFAnimationApi;
import traben.entity_model_features.models.animation.EMFAnimationEntityContext;
import traben.entity_model_features.utils.EMFEntity;

/** Exposes only server-authoritative pig resting state to EMF. */
public final class PigAnimationBridge {
    public static final String EMF_REST_STATE = "alt_pig_rest_state";
    public static final String EMF_REST_AGE = "alt_pig_rest_age";

    private PigAnimationBridge() {
    }

    public static Float currentRestState() {
        Pig pig = currentPig();
        if (!ServerBridgeState.isPresent() || !(pig instanceof PigRestingAccess access)) {
            return 0.0F;
        }
        return (float) access.il2m$getRestState();
    }

    public static Float currentRestAge() {
        Pig pig = currentPig();
        if (!ServerBridgeState.isPresent() || !(pig instanceof PigRestingAccess access)) {
            return 0.0F;
        }
        long elapsedTicks = Math.max(0L,
                pig.level().getGameTime() - access.il2m$getRestStateStart());
        float partialTick = EMFAnimationEntityContext.getTickDelta();
        return (elapsedTicks + partialTick) / 20.0F;
    }

    private static Pig currentPig() {
        EMFEntity emfEntity = EMFAnimationApi.getCurrentEntity();
        if (emfEntity instanceof Entity entity && entity instanceof Pig pig) {
            return pig;
        }
        return null;
    }
}
