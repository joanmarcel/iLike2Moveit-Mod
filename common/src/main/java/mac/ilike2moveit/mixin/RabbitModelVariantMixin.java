package mac.ilike2moveit.mixin;

import mac.ilike2moveit.config.MobModelConfig;
import mac.ilike2moveit.rabbit.RabbitTinyModelAccess;
import mac.ilike2moveit.rabbit.RabbitVariantCompat;
import net.minecraft.client.model.RabbitModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RabbitRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Rabbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Bakes and selects the dedicated Tiny Takeover presentation only for baby rabbits. */
@Mixin(RabbitRenderer.class)
public abstract class RabbitModelVariantMixin implements RabbitTinyModelAccess {
    @Unique
    private RabbitModel<Rabbit> ilike2moveit$tinyRabbitModel;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ilike2moveit$bakeTinyRabbitModel(
            EntityRendererProvider.Context context, CallbackInfo ci
    ) {
        ilike2moveit$tinyRabbitModel = new RabbitModel<>(
                context.bakeLayer(RabbitVariantCompat.TINY_TAKEOVER_RABBIT_LAYER));
    }

    @Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
    private void ilike2moveit$selectTinyRabbitTexture(
            Rabbit rabbit, CallbackInfoReturnable<ResourceLocation> cir
    ) {
        if (rabbit.isBaby()
                && MobModelConfig.rabbitBabyModel() == MobModelConfig.RabbitBabyModel.TINY_TAKEOVER) {
            cir.setReturnValue(RabbitVariantCompat.tinyTexture(rabbit));
        }
    }

    @Override
    public RabbitModel<Rabbit> ilike2moveit$tinyTakeoverRabbitModel() {
        return ilike2moveit$tinyRabbitModel;
    }
}
