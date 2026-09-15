package mac.ilike2moveit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mac.ilike2moveit.emf.EmfLocatorBridge;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Bounds attachment captures to one complete living-entity render, including its feature layers. */
@Mixin(LivingEntityRenderer.class)
public abstract class EmfLivingEntityRenderScopeMixin {

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    private void ilike2moveit$beginLocatorScope(
            LivingEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int light, CallbackInfo callbackInfo) {
        EmfLocatorBridge.beginEntityRender(entity.getUUID());
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("RETURN"))
    private void ilike2moveit$endLocatorScope(
            LivingEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int light, CallbackInfo callbackInfo) {
        EmfLocatorBridge.endEntityRender(entity.getUUID());
    }
}
