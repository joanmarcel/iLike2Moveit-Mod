package mac.ilike2moveit.mixin;

import com.blackgear.vanillabackport.client.api.renderer.AbstractVariantRenderer;
import com.blackgear.vanillabackport.client.api.renderer.PigVariantRenderer;
import com.blackgear.vanillabackport.common.api.variant.VariantDataHolder;
import com.blackgear.vanillabackport.common.api.variant.VariantUtils;
import com.blackgear.vanillabackport.common.level.entities.animal.PigVariant;
import com.blackgear.vanillabackport.common.level.entities.animal.PigVariants;
import mac.ilike2moveit.ILike2MoveItMod;
import mac.ilike2moveit.pig.PigVariantCompat;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Broken WARM pig (FA + VanillaBackport, same case as the warm chickens, see {@link ChickenVariantRendererMixin}).
 * VanillaBackport assigns NORMAL to warm -> FA Extensions' default model (textureSize [64,64]), but the
 * warm_pig.png that wins is [64,32] -> mismatch -> deformed. We give the warm pig the DEDICATED [64,32]
 * model (warm_pig.jem, baked by EMF into the layer registered by {@link PigVariantCompat}), which matches
 * the 64x32 texture. Applies to adults AND piglets. Temperate (NORMAL default) and cold (ColdPigModel)
 * are left untouched.
 *
 * Mixins the base class AbstractVariantRenderer (shared with the chicken) and filters by instanceof.
 */
@Mixin(AbstractVariantRenderer.class)
public abstract class PigVariantRendererMixin {

    @Unique
    private PigModel<Pig> ilike2moveit$warmPigModel;

    @Unique
    private boolean ilike2moveit$loggedWarmPig;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ilike2moveit$bakeWarmPigModel(EntityRendererProvider.Context context, CallbackInfo ci) {
        if ((Object) this instanceof PigVariantRenderer) {
            ilike2moveit$warmPigModel = new PigModel<>(context.bakeLayer(PigVariantCompat.WARM_PIG_LAYER));
        }
    }

    @Inject(
            method = "getModel(Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ilike2moveit$selectWarmPigModel(LivingEntity entity, CallbackInfoReturnable<Optional<?>> cir) {
        if (ilike2moveit$warmPigModel == null || !(entity instanceof Pig pig)) {
            return;
        }
        Object variantData = VariantDataHolder.getHolder(pig).getVariantData().orElse(null);
        if (variantData instanceof PigVariant variant
                && VariantUtils.matches(PigVariants.REGISTRY, variant, PigVariants.WARM)) {
            cir.setReturnValue(Optional.of(ilike2moveit$warmPigModel));
            if (!ilike2moveit$loggedWarmPig) {
                ilike2moveit$loggedWarmPig = true;
                ILike2MoveItMod.LOGGER.info(
                        "[Pig Compat] warm usa el modelo dedicado warm_pig [64,32]; temperate/cold intactos."
                );
            }
        }
    }

    @Inject(
            method = "getTexture(Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ilike2moveit$warmPigTexture(LivingEntity entity, CallbackInfoReturnable<Optional<ResourceLocation>> cir) {
        if (!((Object) this instanceof PigVariantRenderer) || !(entity instanceof Pig pig)) {
            return;
        }
        Object variantData = VariantDataHolder.getHolder(pig).getVariantData().orElse(null);
        if (variantData instanceof PigVariant variant
                && VariantUtils.matches(PigVariants.REGISTRY, variant, PigVariants.WARM)) {
            // Our own texture with the FA eye; the base warm_pig.png (VBP) does not have it.
            cir.setReturnValue(Optional.of(PigVariantCompat.WARM_PIG_TEXTURE));
        }
    }
}
