package mac.ilike2moveit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mac.ilike2moveit.emf.EmfLocatorBridge;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.IronGolemFlowerLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Anchors the offered poppy to the resource pack's visible animated arm.
 *
 * <p>The pack keeps vanilla {@code right_arm} as an empty animation passthrough, while the mesh the
 * player sees is {@code rightArm}. Vanilla's flower layer follows the former, so the flower floats
 * away from the hand. The golem JEM exposes a {@code right_handheld_item} attachment inside the
 * visible arm; EMF captures its accumulated pose and this layer renders the poppy on that pose.
 * Without the attachment (vanilla model, EMF absent or another pack), rendering falls through to
 * the untouched vanilla implementation.
 */
@Mixin(IronGolemFlowerLayer.class)
public abstract class IronGolemFlowerLayerMixin
        extends RenderLayer<IronGolem, IronGolemModel<IronGolem>> {

    @Shadow @Final private BlockRenderDispatcher blockRenderer;

    private IronGolemFlowerLayerMixin(
            RenderLayerParent<IronGolem, IronGolemModel<IronGolem>> parent) {
        super(parent);
    }

    @Inject(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/IronGolem;FFFFFF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void ilike2moveit$followVisibleArm(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            IronGolem golem,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci) {
        if (golem.getOfferFlowerTick() == 0) {
            return;
        }

        PoseStack.Pose locator = EmfLocatorBridge.currentRightItemPose(golem.getUUID());
        if (locator == null) {
            return;
        }

        ci.cancel();
        poseStack.pushPose();
        PoseStack.Pose top = poseStack.last();
        top.pose().set(locator.pose());
        top.normal().set(locator.normal());

        // Centre the half-size block model on the authored hand locator. The -90 degree rotation is
        // the same orientation used by the vanilla flower layer.
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        this.blockRenderer.renderSingleBlock(
                Blocks.POPPY.defaultBlockState(), poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
