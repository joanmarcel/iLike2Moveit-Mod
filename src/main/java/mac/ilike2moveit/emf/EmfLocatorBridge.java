package mac.ilike2moveit.emf;

import com.mojang.blaze3d.vertex.PoseStack;
import traben.entity_model_features.models.animation.EMFAnimationEntityContext;
import traben.entity_model_features.models.animation.EMFAttachments;
import traben.entity_model_features.models.animation.state.EMFEntityRenderState;

/**
 * Bridge to Bedrock's <em>locator</em> concept, which EMF reimplements natively.
 *
 * <p>When a {@code .jem} submodel declares {@code "attachments": {"right_handheld_item":
 * [x,y,z]}}, EMF captures — INSIDE the mesh render, after applying the frame's animation — that
 * bone's accumulated matrix (camera -> entity -> root -> ... -> bone, with the whole hierarchy and
 * EMF animation already applied) and stores it per-entity as {@code rightArmOverride().pose}. EMF's
 * only consumer is {@code ItemInHandLayer}; neither {@code FoxHeldItemLayer} nor
 * {@code CrossedArmsItemLayer} are, so we expose it here for those layers to consume.
 *
 * <p>The returned pose belongs to the SAME frame and partial tick EMF used to pose the model: an
 * item drawn on top of it inherits the bone's movement in real time, with no lag.
 * Everything is wrapped in {@code try/catch}: should a future EMF version change these internals,
 * it returns {@code null} and the layer falls back to its vanilla render instead of crashing.
 */
public final class EmfLocatorBridge {

    private EmfLocatorBridge() {
    }

    /**
     * Accumulated pose of the {@code right_handheld_item} locator captured by EMF this frame, or
     * {@code null} if EMF is not active, the entity has no such attachment, or the bone was not
     * rendered this frame.
     */
    public static PoseStack.Pose currentRightItemPose() {
        try {
            EMFEntityRenderState state = EMFAnimationEntityContext.getEmfState();
            if (state == null) {
                return null;
            }
            EMFAttachments attachment = state.rightArmOverride();
            return attachment == null ? null : attachment.pose;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
