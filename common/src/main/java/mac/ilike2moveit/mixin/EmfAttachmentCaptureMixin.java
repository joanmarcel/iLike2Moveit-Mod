package mac.ilike2moveit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mac.ilike2moveit.emf.EmfLocatorBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_model_features.models.animation.EMFAttachments;

/** Captures the attachment only when {@link EmfModelPartRenderPassMixin} marks a real render pass. */
@Mixin(value = EMFAttachments.class, remap = false)
public abstract class EmfAttachmentCaptureMixin {

    @Inject(method = "setAttachment", at = @At("RETURN"), remap = false)
    private void ilike2moveit$captureRenderedPose(PoseStack poseStack, CallbackInfo callbackInfo) {
        EmfLocatorBridge.captureRenderedRightItemPose((EMFAttachments) (Object) this);
    }
}
