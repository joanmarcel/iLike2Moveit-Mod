package mac.ilike2moveit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mac.ilike2moveit.config.MobModelConfig;
import mac.ilike2moveit.wolf.WolfTinyModelAccess;
import mac.ilike2moveit.wolf.WolfVariantCompat;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Renders the dyed collar with its dedicated Tiny geometry. */
@Mixin(WolfCollarLayer.class)
public abstract class WolfCollarModelVariantMixin {
    @Unique private RenderLayerParent<Wolf, WolfModel<Wolf>> ilike2moveit$renderer;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ilike2moveit$captureTinyCollarModel(
            RenderLayerParent<Wolf, WolfModel<Wolf>> renderer, CallbackInfo ci) {
        ilike2moveit$renderer = renderer;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void ilike2moveit$renderTinyCollar(PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, Wolf wolf, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch,
            CallbackInfo ci) {
        if (!wolf.isBaby()
                || MobModelConfig.wolfBabyModel() != MobModelConfig.WolfBabyModel.TINY_TAKEOVER) {
            return;
        }
        ci.cancel();
        WolfModel<Wolf> tinyCollarModel = ilike2moveit$renderer instanceof WolfTinyModelAccess access
                ? access.ilike2moveit$tinyTakeoverWolfCollarModel() : null;
        if (!wolf.isTame() || wolf.isInvisible() || tinyCollarModel == null) {
            return;
        }
        tinyCollarModel.prepareMobModel(wolf, limbSwing, limbSwingAmount, partialTicks);
        tinyCollarModel.setupAnim(
                wolf, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        int color = wolf.getCollarColor().getTextureDiffuseColor();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(
                WolfVariantCompat.TINY_TAKEOVER_COLLAR_TEXTURE));
        tinyCollarModel.renderToBuffer(
                poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, color);
    }
}
