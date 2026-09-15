package mac.ilike2moveit.mixin;

import mac.ilike2moveit.config.MobModelConfig;
import mac.ilike2moveit.wolf.WolfTinyModelAccess;
import mac.ilike2moveit.wolf.WolfVariantCompat;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Bakes the dedicated puppy layers and chooses the matching breed/state atlas. */
@Mixin(WolfRenderer.class)
public abstract class WolfModelVariantMixin implements WolfTinyModelAccess {
    @Unique private WolfModel<Wolf> ilike2moveit$tinyWolfModel;
    @Unique private WolfModel<Wolf> ilike2moveit$tinyWolfCollarModel;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ilike2moveit$bakeTinyWolfModels(EntityRendererProvider.Context context, CallbackInfo ci) {
        ilike2moveit$tinyWolfModel = new WolfModel<>(
                context.bakeLayer(WolfVariantCompat.TINY_TAKEOVER_WOLF_LAYER));
        ilike2moveit$tinyWolfCollarModel = new WolfModel<>(
                context.bakeLayer(WolfVariantCompat.TINY_TAKEOVER_WOLF_COLLAR_LAYER));
    }

    @Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
    private void ilike2moveit$selectTinyWolfTexture(Wolf wolf, CallbackInfoReturnable<ResourceLocation> cir) {
        if (wolf.isBaby() && MobModelConfig.wolfBabyModel() == MobModelConfig.WolfBabyModel.TINY_TAKEOVER) {
            cir.setReturnValue(WolfVariantCompat.tinyTakeoverTexture(wolf));
        }
    }

    @Override
    public WolfModel<Wolf> ilike2moveit$tinyTakeoverWolfModel() {
        return ilike2moveit$tinyWolfModel;
    }

    @Override
    public WolfModel<Wolf> ilike2moveit$tinyTakeoverWolfCollarModel() {
        return ilike2moveit$tinyWolfCollarModel;
    }
}
