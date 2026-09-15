package mac.ilike2moveit.mixin;

import mac.ilike2moveit.cat.CatTinyModelAccess;
import mac.ilike2moveit.cat.CatVariantCompat;
import mac.ilike2moveit.config.MobModelConfig;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Bakes the dedicated Tiny Takeover layer and selects its breed-matched atlas for kittens. */
@Mixin(CatRenderer.class)
public abstract class CatModelVariantMixin implements CatTinyModelAccess {
    @Unique
    private CatModel<Cat> ilike2moveit$classicBabyModel;

    @Unique
    private CatModel<Cat> ilike2moveit$tinyTakeoverModel;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ilike2moveit$bakeTinyTakeoverModel(
            EntityRendererProvider.Context context, CallbackInfo ci
    ) {
        ilike2moveit$classicBabyModel = new CatModel<>(
                context.bakeLayer(CatVariantCompat.CLASSIC_BABY_CAT_LAYER)
        );
        ilike2moveit$tinyTakeoverModel = new CatModel<>(
                context.bakeLayer(CatVariantCompat.TINY_TAKEOVER_CAT_LAYER)
        );
    }

    @Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
    private void ilike2moveit$selectTinyTakeoverTexture(
            Cat cat, CallbackInfoReturnable<ResourceLocation> cir
    ) {
        if (cat.isBaby()
                && MobModelConfig.catBabyModel() == MobModelConfig.CatBabyModel.TINY_TAKEOVER) {
            cir.setReturnValue(CatVariantCompat.tinyTakeoverTexture(cat));
        }
    }

    @Override
    public CatModel<Cat> ilike2moveit$classicBabyModel() {
        return ilike2moveit$classicBabyModel;
    }

    @Override
    public CatModel<Cat> ilike2moveit$tinyTakeoverModel() {
        return ilike2moveit$tinyTakeoverModel;
    }
}
