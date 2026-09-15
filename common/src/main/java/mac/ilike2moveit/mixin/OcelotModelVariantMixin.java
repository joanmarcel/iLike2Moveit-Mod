package mac.ilike2moveit.mixin;

import mac.ilike2moveit.config.MobModelConfig;
import mac.ilike2moveit.ocelot.OcelotTinyModelAccess;
import mac.ilike2moveit.ocelot.OcelotVariantCompat;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.OcelotRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Ocelot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Bakes and selects the dedicated Tiny Takeover presentation only for baby ocelots. */
@Mixin(OcelotRenderer.class)
public abstract class OcelotModelVariantMixin implements OcelotTinyModelAccess {
    @Unique
    private OcelotModel<Ocelot> ilike2moveit$classicBabyOcelotModel;

    @Unique
    private OcelotModel<Ocelot> ilike2moveit$tinyOcelotModel;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ilike2moveit$bakeTinyOcelotModel(
            EntityRendererProvider.Context context, CallbackInfo ci
    ) {
        ilike2moveit$classicBabyOcelotModel = new OcelotModel<>(
                context.bakeLayer(OcelotVariantCompat.CLASSIC_BABY_OCELOT_LAYER));
        ilike2moveit$tinyOcelotModel = new OcelotModel<>(
                context.bakeLayer(OcelotVariantCompat.TINY_TAKEOVER_OCELOT_LAYER));
    }

    @Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
    private void ilike2moveit$selectTinyOcelotTexture(
            Ocelot ocelot, CallbackInfoReturnable<ResourceLocation> cir
    ) {
        if (ocelot.isBaby()
                && MobModelConfig.ocelotBabyModel() == MobModelConfig.OcelotBabyModel.TINY_TAKEOVER) {
            cir.setReturnValue(OcelotVariantCompat.TINY_TAKEOVER_TEXTURE);
        }
    }

    @Override
    public OcelotModel<Ocelot> ilike2moveit$classicBabyOcelotModel() {
        return ilike2moveit$classicBabyOcelotModel;
    }

    @Override
    public OcelotModel<Ocelot> ilike2moveit$tinyTakeoverOcelotModel() {
        return ilike2moveit$tinyOcelotModel;
    }
}
