package mac.ilike2moveit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mac.ilike2moveit.emf.EmfLocatorBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_model_features.models.parts.EMFModelPartCustom;

/** Marks only calls reached through EMF's real model-part render traversal. */
@Mixin(value = EMFModelPartCustom.class, remap = false)
public abstract class EmfModelPartRenderPassMixin {

    // EMF overrides a Minecraft method: named on NeoForge/dev, intermediary on Fabric 1.21.1.
    // The class is mod-owned, so select both runtime names without disabling required injection.
    @Inject(method = {"render", "method_22699"}, at = @At("HEAD"), remap = false, require = 1)
    private void ilike2moveit$enterRenderedPart(
            PoseStack poseStack, VertexConsumer vertices, int light, int overlay, int color,
            CallbackInfo callbackInfo) {
        EmfLocatorBridge.enterRenderedModelPart((EMFModelPartCustom) (Object) this);
    }

    @Inject(method = {"render", "method_22699"}, at = @At("RETURN"), remap = false, require = 1)
    private void ilike2moveit$exitRenderedPart(
            PoseStack poseStack, VertexConsumer vertices, int light, int overlay, int color,
            CallbackInfo callbackInfo) {
        EmfLocatorBridge.exitRenderedModelPart();
    }
}
