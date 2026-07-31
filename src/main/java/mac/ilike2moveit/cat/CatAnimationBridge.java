package mac.ilike2moveit.cat;

import mac.ilike2moveit.ILike2MoveItMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cat;
import traben.entity_model_features.EMFAnimationApi;
import traben.entity_model_features.utils.EMFEntity;

import java.util.Map;
import java.util.WeakHashMap;

/** Exposes vanilla cat pose progress to CEM without inventing a second scheduler. */
public final class CatAnimationBridge {
    public static final String EMF_LIE_DOWN_AMOUNT = "alt_cat_liedown_amount";
    private static final Map<Cat, Boolean> LAST_LYING = new WeakHashMap<>();

    private CatAnimationBridge() {
    }

    /**
     * Mirrors {@link Cat#getLieDownAmount(float)} for the entity currently evaluated by EMF.
     * Vanilla already interpolates this value toward one while the cat lies on its owner and
     * toward zero while it gets back up, so the resource pack can retain the source controller's
     * non-zero/one transition semantics.
     */
    public static Float currentEmfLieDownAmount() {
        EMFEntity emfEntity = EMFAnimationApi.getCurrentEntity();
        if (!(emfEntity instanceof Entity entity) || !(entity instanceof Cat cat)) {
            return 0.0F;
        }
        float amount = cat.getLieDownAmount(1.0F);
        boolean firstSample = !LAST_LYING.containsKey(cat);
        boolean lying = amount > 0.001F;
        Boolean previous = LAST_LYING.put(cat, lying);
        if (firstSample) {
            ILike2MoveItMod.LOGGER.info(
                    "[Cat Resting] EMF bridge active for uuid={} amount={}", cat.getUUID(), amount);
        }
        if (previous != null && previous != lying) {
            ILike2MoveItMod.LOGGER.info(
                    "[Cat Resting] lying {} for uuid={} amount={}",
                    lying ? "IN" : "OUT", cat.getUUID(), amount);
        }
        return amount;
    }
}
