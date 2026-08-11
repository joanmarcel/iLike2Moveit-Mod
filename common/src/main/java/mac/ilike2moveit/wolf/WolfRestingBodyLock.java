package mac.ilike2moveit.wolf;

import net.minecraft.world.entity.animal.Wolf;
import traben.entity_model_features.EMFAnimationApi;

import java.util.Map;

/** Keeps a resting wolf's body heading fixed while leaving its head rotation untouched. */
public final class WolfRestingBodyLock {
    private static final String BODY_LOCK_WEIGHT = "var.wolf_restpose_w";
    private static final String REST_ANCHOR_YAW = "var.wolf_restpose_anchor_yaw";

    private WolfRestingBodyLock() {
    }

    public static Float restingAnchorYaw(Wolf wolf) {
        Map<String, Float> variables =
                EMFAnimationApi.emfEntityOf(wolf).emf$getVariableMap();
        if (variables.getOrDefault(BODY_LOCK_WEIGHT, 0.0F) <= 0.001F) {
            return null;
        }
        Float anchorYaw = variables.get(REST_ANCHOR_YAW);
        return anchorYaw != null && Float.isFinite(anchorYaw) ? anchorYaw : null;
    }
}
