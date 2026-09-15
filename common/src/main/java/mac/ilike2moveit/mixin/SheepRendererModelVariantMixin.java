package mac.ilike2moveit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mac.ilike2moveit.config.MobModelConfig;
import mac.ilike2moveit.render.RendererModelSwapStack;
import mac.ilike2moveit.sheep.SheepAlternateModelAccess;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Selects the configured adult sheep model and restores it after every call. */
@Mixin(LivingEntityRenderer.class)
public abstract class SheepRendererModelVariantMixin {
    @Shadow
    protected EntityModel<?> model;

    @Unique
    private static final RendererModelSwapStack<EntityModel<?>> ILIKE2MOVEIT$SHEEP_MODEL_SWAP =
            new RendererModelSwapStack<>();

    @Inject(method = "render", at = @At("HEAD"))
    private void ilike2moveit$selectAlternateSheepModel(LivingEntity entity, float entityYaw,
            float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            CallbackInfo ci) {
        if (!(entity instanceof Sheep sheep)
                || !((Object) this instanceof SheepAlternateModelAccess access)) {
            return;
        }
        EntityModel<?> replacement = null;
        if (!sheep.isBaby()) {
            replacement = switch (MobModelConfig.sheepAdultModel()) {
                case CLASSIC -> null;
                case ALTERNATE -> access.ilike2moveit$alternateSheepModel();
            };
        }
        model = ILIKE2MOVEIT$SHEEP_MODEL_SWAP.begin(model, replacement);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void ilike2moveit$restoreSheepModel(LivingEntity entity, float entityYaw,
            float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            CallbackInfo ci) {
        if (!(entity instanceof Sheep)
                || !((Object) this instanceof SheepAlternateModelAccess)) {
            return;
        }
        model = ILIKE2MOVEIT$SHEEP_MODEL_SWAP.end(model);
    }
}
