package mac.ilike2moveit.rabbit;

import mac.ilike2moveit.MoveItCore;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Rabbit;
import traben.entity_model_features.EMFAnimationApi;
import traben.entity_model_features.models.animation.EMFAnimationEntityContext;
import traben.entity_model_features.utils.EMFEntity;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/** Exposes the vanilla client rabbit jump clock that CEM cannot otherwise observe. */
public final class RabbitAnimationBridge {
    public static final String EMF_JUMP_COMPLETION = "alt_rabbit_jump_completion";
    public static final String EMF_VERTICAL_SPEED = "alt_rabbit_vertical_speed";
    public static final String EMF_HORIZONTAL_SPEED = "alt_rabbit_horizontal_speed";
    private static final Set<Rabbit> ANNOUNCED_ACTIVE = Collections.newSetFromMap(new WeakHashMap<>());

    private RabbitAnimationBridge() {
    }

    /**
     * Mirrors {@link Rabbit#getJumpCompletion(float)} for the rabbit currently evaluated by EMF.
     *
     * <p>The server broadcasts entity event {@code 1}; the client handles it by resetting its own
     * {@code jumpTicks}/{@code jumpDuration}. Vanilla {@code RabbitModel} reads those fields with the
     * render partial tick. Using EMF's own tick delta here therefore reproduces the same clock without
     * inventing a timer from ground state, distance or velocity.
     */
    public static Float currentJumpCompletion() {
        EMFEntity emfEntity = EMFAnimationApi.getCurrentEntity();
        if (!(emfEntity instanceof Entity entity) || !(entity instanceof Rabbit rabbit)) {
            return 0.0F;
        }

        float partialTick = EMFAnimationEntityContext.getTickDelta();
        float completion = rabbit.getJumpCompletion(partialTick);
        if (completion > 0.0F && ANNOUNCED_ACTIVE.add(rabbit)) {
            MoveItCore.LOGGER.info(
                    "[Rabbit] EMF jump bridge active uuid={} completion={} deltaY={} horizontalSpeed={} limbSpeed={} moveControlSpeed={}",
                    rabbit.getUUID(), completion, rabbit.getDeltaMovement().y,
                    rabbit.getDeltaMovement().horizontalDistance(), rabbit.walkAnimation.speed(partialTick),
                    rabbit.getMoveControl().getSpeedModifier());
        }
        return completion;
    }

    /** Returns the rabbit's client vertical velocity in Bedrock query units (blocks per second). */
    public static Float currentVerticalSpeed() {
        EMFEntity emfEntity = EMFAnimationApi.getCurrentEntity();
        if (!(emfEntity instanceof Entity entity) || !(entity instanceof Rabbit rabbit)) {
            return 0.0F;
        }
        return (float) (rabbit.getDeltaMovement().y * 20.0D);
    }

    /** Returns actual horizontal travel, not the AI's requested speed, in blocks per second. */
    public static Float currentHorizontalSpeed() {
        EMFEntity emfEntity = EMFAnimationApi.getCurrentEntity();
        if (!(emfEntity instanceof Entity entity) || !(entity instanceof Rabbit rabbit)) {
            return 0.0F;
        }
        return (float) (rabbit.getDeltaMovement().horizontalDistance() * 20.0D);
    }
}
