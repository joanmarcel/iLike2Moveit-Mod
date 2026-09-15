package mac.ilike2moveit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mac.ilike2moveit.config.MobModelConfig;
import mac.ilike2moveit.ocelot.OcelotTinyModelAccess;
import mac.ilike2moveit.render.RendererModelSwapStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Ocelot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Temporarily swaps the renderer model while a Tiny Takeover baby ocelot is rendered. */
@Mixin(LivingEntityRenderer.class)
public abstract class OcelotRendererModelVariantMixin {
    @Shadow
    protected EntityModel<?> model;

    @Unique
    private static final RendererModelSwapStack<EntityModel<?>> ILIKE2MOVEIT$OCELOT_MODEL_SWAP =
            new RendererModelSwapStack<>();

    @Inject(method = "render", at = @At("HEAD"))
    private void ilike2moveit$selectTinyOcelotModel(LivingEntity entity, float entityYaw,
            float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            CallbackInfo ci) {
        if (!(entity instanceof Ocelot ocelot)
                || !((Object) this instanceof OcelotTinyModelAccess access)) {
            return;
        }
        EntityModel<?> babyModel = null;
        if (ocelot.isBaby()) {
            babyModel =
                    MobModelConfig.ocelotBabyModel() == MobModelConfig.OcelotBabyModel.TINY_TAKEOVER
                            ? access.ilike2moveit$tinyTakeoverOcelotModel()
                            : access.ilike2moveit$classicBabyOcelotModel();
        }
        model = ILIKE2MOVEIT$OCELOT_MODEL_SWAP.begin(model, babyModel);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void ilike2moveit$restoreOcelotModel(LivingEntity entity, float entityYaw,
            float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            CallbackInfo ci) {
        if (!(entity instanceof Ocelot)
                || !((Object) this instanceof OcelotTinyModelAccess)) {
            return;
        }
        model = ILIKE2MOVEIT$OCELOT_MODEL_SWAP.end(model);
    }
}
