package mac.ilike2moveit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mac.ilike2moveit.config.MobModelConfig;
import mac.ilike2moveit.rabbit.RabbitTinyModelAccess;
import mac.ilike2moveit.render.RendererModelSwapStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Rabbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Temporarily swaps the renderer model while a Tiny Takeover baby rabbit is rendered. */
@Mixin(LivingEntityRenderer.class)
public abstract class RabbitRendererModelVariantMixin {
    @Shadow
    protected EntityModel<?> model;

    @Unique
    private static final RendererModelSwapStack<EntityModel<?>> ILIKE2MOVEIT$RABBIT_MODEL_SWAP =
            new RendererModelSwapStack<>();

    @Inject(method = "render", at = @At("HEAD"))
    private void ilike2moveit$selectTinyRabbitModel(LivingEntity entity, float entityYaw,
            float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            CallbackInfo ci) {
        if (!(entity instanceof Rabbit rabbit)
                || !((Object) this instanceof RabbitTinyModelAccess access)) {
            return;
        }
        EntityModel<?> babyModel = null;
        if (rabbit.isBaby()
                && MobModelConfig.rabbitBabyModel() == MobModelConfig.RabbitBabyModel.TINY_TAKEOVER) {
            babyModel = access.ilike2moveit$tinyTakeoverRabbitModel();
        }
        model = ILIKE2MOVEIT$RABBIT_MODEL_SWAP.begin(model, babyModel);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void ilike2moveit$restoreRabbitModel(LivingEntity entity, float entityYaw,
            float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            CallbackInfo ci) {
        if (!(entity instanceof Rabbit)
                || !((Object) this instanceof RabbitTinyModelAccess)) {
            return;
        }
        model = ILIKE2MOVEIT$RABBIT_MODEL_SWAP.end(model);
    }
}
