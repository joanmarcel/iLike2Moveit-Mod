package mac.ilike2moveit.emf;

import com.mojang.blaze3d.vertex.PoseStack;
import mac.ilike2moveit.MoveItCore;
import mac.ilike2moveit.render.RenderedAttachmentCaptureScope;
import traben.entity_model_features.models.animation.EMFAnimationEntityContext;
import traben.entity_model_features.models.animation.EMFAttachments;
import traben.entity_model_features.models.animation.state.EMFEntityRenderState;
import traben.entity_model_features.models.parts.EMFModelPartCustom;
import traben.entity_model_features.models.parts.EMFModelPartRoot;

import java.util.UUID;

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

    private static final boolean LOCATOR_PROBE = Boolean.getBoolean("ilike2moveit.locatorProbe");
    private static final RenderedAttachmentCaptureScope<UUID, EMFModelPartRoot, Integer, PoseStack.Pose>
            RIGHT_ITEM_POSES = new RenderedAttachmentCaptureScope<>();

    private EmfLocatorBridge() {
    }

    public static void beginEntityRender(UUID entityId) {
        RIGHT_ITEM_POSES.beginEntity(entityId);
    }

    public static void endEntityRender(UUID entityId) {
        RIGHT_ITEM_POSES.endEntity(entityId);
    }

    public static void enterRenderedModelPart(EMFModelPartCustom part) {
        try {
            EMFModelPartRoot root = part.getRoot();
            RIGHT_ITEM_POSES.enterRenderedPart(root, root.currentModelVariant);
        } catch (Throwable ignored) {
            // Optional EMF boundary; the item layer will fall back to vanilla.
        }
    }

    public static void exitRenderedModelPart() {
        RIGHT_ITEM_POSES.exitRenderedPart();
    }

    /** Captures a locator only while EMF is traversing the actual main model. */
    public static void captureRenderedRightItemPose(EMFAttachments attachment) {
        try {
            EMFEntityRenderState state = EMFAnimationEntityContext.getEmfState();
            if (state == null || attachment == null || !attachment.right) {
                return;
            }
            RIGHT_ITEM_POSES.capture(
                    state.uuid(), attachment.pose, EMFAnimationEntityContext.isLayerPhase());
        } catch (Throwable ignored) {
            // Optional EMF boundary; the item layer will fall back to vanilla.
        }
    }

    /**
     * Returns the pose captured during the current entity's real model traversal.
     *
     * <p>EMF 3.2.4 writes {@code rightArmOverride} twice: first while {@code AgeableListModel}
     * renders the selected model variant, then again from {@code checkArmOverrides} after
     * {@code renderToBuffer}. The second traversal starts from the bare entity pose and therefore
     * omits the baby head/body scale and translation. Reading the global override makes a baby item
     * inherit adult-sized movement, especially visibly during sleep.
     *
     * <p>The scoped capture retains UUID, root identity, selected model variant and pass identity;
     * post-render recomputes and feature-layer models cannot overwrite it.
     */
    public static PoseStack.Pose currentRightItemPose(UUID entityId) {
        try {
            EMFEntityRenderState state = EMFAnimationEntityContext.getEmfState();
            if (state == null || !entityId.equals(state.uuid())) {
                return null;
            }
            RenderedAttachmentCaptureScope.Capture<EMFModelPartRoot, Integer, PoseStack.Pose> capture =
                    RIGHT_ITEM_POSES.current(entityId);
            if (capture == null) {
                return null;
            }
            if (LOCATOR_PROBE) {
                EMFModelPartRoot root = capture.root();
                MoveItCore.LOGGER.info(
                        "[LocatorProbe] uuid={} root={} rootIdentity={} variant={} pass=main-model",
                        entityId, root.modelName.getfileName(), System.identityHashCode(root),
                        capture.variant());
            }
            return capture.pose();
        } catch (Throwable ignored) {
            return null;
        }
    }
}
