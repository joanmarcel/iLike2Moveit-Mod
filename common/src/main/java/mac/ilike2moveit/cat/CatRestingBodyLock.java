package mac.ilike2moveit.cat;

import net.minecraft.world.entity.animal.Cat;
import traben.entity_model_features.EMFAnimationApi;

import java.util.Map;

/** Keeps a resting cat's body heading fixed while leaving its head rotation untouched. */
public final class CatRestingBodyLock {
    private static final String BODY_LOCK_WEIGHT = "var.cat_bodylock_w";
    private static final String REST_ANCHOR_YAW = "var.cat_restpose_anchor_yaw";

    private CatRestingBodyLock() {
    }

    public static Float restingAnchorYaw(Cat cat) {
        Map<String, Float> variables =
                EMFAnimationApi.emfEntityOf(cat).emf$getVariableMap();
        if (variables.getOrDefault(BODY_LOCK_WEIGHT, 0.0F) <= 0.001F) {
            return null;
        }
        Float anchorYaw = variables.get(REST_ANCHOR_YAW);
        return anchorYaw != null && Float.isFinite(anchorYaw) ? anchorYaw : null;
    }
}
