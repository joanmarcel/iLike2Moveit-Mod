package mac.ilike2moveit.mixin;

import com.blackgear.vanillabackport.client.api.renderer.AbstractVariantRenderer;
import com.blackgear.vanillabackport.client.api.renderer.CowVariantRenderer;
import com.blackgear.vanillabackport.common.api.variant.VariantDataHolder;
import com.blackgear.vanillabackport.common.api.variant.VariantUtils;
import com.blackgear.vanillabackport.common.level.entities.animal.CowVariant;
import com.blackgear.vanillabackport.common.level.entities.animal.CowVariants;
import mac.ilike2moveit.MoveItCore;
import mac.ilike2moveit.cow.CowBiomeVariants;
import mac.ilike2moveit.cow.CowVariantCompat;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/** Routes named, climate and biome-breed cows through explicit EMF layers and matching atlases. */
@Mixin(AbstractVariantRenderer.class)
public abstract class CowVariantRendererMixin {
    @Unique
    private CowModel<Cow> ilike2moveit$warmCowModel;

    @Unique
    private CowModel<Cow> ilike2moveit$coldCowModel;

    @Unique
    private CowModel<Cow> ilike2moveit$birchForestCowModel;

    @Unique
    private CowModel<Cow> ilike2moveit$birchForestCowCalfModel;

    @Unique
    private CowModel<Cow> ilike2moveit$bullCowModel;

    @Unique
    private CowModel<Cow> ilike2moveit$bullCowCalfModel;

    @Unique
    private boolean ilike2moveit$loggedCowModels;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ilike2moveit$bakeCowVariantModels(EntityRendererProvider.Context context, CallbackInfo ci) {
        if ((Object) this instanceof CowVariantRenderer) {
            ilike2moveit$warmCowModel = new CowModel<>(context.bakeLayer(CowVariantCompat.WARM_COW_LAYER));
            ilike2moveit$coldCowModel = new CowModel<>(context.bakeLayer(CowVariantCompat.COLD_COW_LAYER));
            ilike2moveit$birchForestCowModel = new CowModel<>(context.bakeLayer(
                    CowVariantCompat.BIRCH_FOREST_COW_LAYER));
            ilike2moveit$birchForestCowCalfModel = new CowModel<>(context.bakeLayer(
                    CowVariantCompat.BIRCH_FOREST_COW_CALF_LAYER));
            ilike2moveit$bullCowModel = new CowModel<>(context.bakeLayer(
                    CowVariantCompat.BULL_COW_LAYER));
            ilike2moveit$bullCowCalfModel = new CowModel<>(context.bakeLayer(
                    CowVariantCompat.BULL_COW_CALF_LAYER));
        }
    }

    @Inject(
            method = "getModel(Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ilike2moveit$selectCowVariantModel(
            LivingEntity entity, CallbackInfoReturnable<Optional<?>> cir
    ) {
        if (!((Object) this instanceof CowVariantRenderer) || !(entity instanceof Cow cow)
                || ilike2moveit$warmCowModel == null || ilike2moveit$coldCowModel == null
                || ilike2moveit$birchForestCowModel == null
                || ilike2moveit$birchForestCowCalfModel == null
                || ilike2moveit$bullCowModel == null || ilike2moveit$bullCowCalfModel == null) {
            return;
        }
        if (CowVariantCompat.isBullEasterEgg(cow)) {
            if (cow.isBaby() && CowVariantCompat.hasBullCowCalfPortResources()) {
                cir.setReturnValue(Optional.of(ilike2moveit$bullCowCalfModel));
            } else if (!cow.isBaby() && CowVariantCompat.hasBullCowPortResources()) {
                cir.setReturnValue(Optional.of(ilike2moveit$bullCowModel));
            }
            return;
        }
        Object variantData = VariantDataHolder.getHolder(cow).getVariantData().orElse(null);
        if (!(variantData instanceof CowVariant variant)) {
            return;
        }
        if (CowBiomeVariants.isBirchForest(variant)) {
            if (cow.isBaby() && CowVariantCompat.hasBirchForestCowCalfPortResources()) {
                cir.setReturnValue(Optional.of(ilike2moveit$birchForestCowCalfModel));
            } else if (!cow.isBaby() && CowVariantCompat.hasBirchForestCowPortResources()) {
                cir.setReturnValue(Optional.of(ilike2moveit$birchForestCowModel));
            }
        } else if (VariantUtils.matches(CowVariants.REGISTRY, variant, CowVariants.WARM)) {
            if (!CowVariantCompat.hasWarmCowPortResources()) {
                return;
            }
            cir.setReturnValue(Optional.of(ilike2moveit$warmCowModel));
        } else if (VariantUtils.matches(CowVariants.REGISTRY, variant, CowVariants.COLD)) {
            if (!CowVariantCompat.hasColdCowPortResources()) {
                return;
            }
            cir.setReturnValue(Optional.of(ilike2moveit$coldCowModel));
        } else {
            return;
        }
        if (!ilike2moveit$loggedCowModels) {
            ilike2moveit$loggedCowModels = true;
            MoveItCore.LOGGER.info(
                    "[Cow Compat] named, climate and persistent birch cows use dedicated EMF layers."
            );
        }
    }

    @Inject(
            method = "getTexture(Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ilike2moveit$selectCowVariantTexture(
            LivingEntity entity, CallbackInfoReturnable<Optional<ResourceLocation>> cir
    ) {
        if (!((Object) this instanceof CowVariantRenderer) || !(entity instanceof Cow cow)) {
            return;
        }
        if (CowVariantCompat.isBullEasterEgg(cow)) {
            if (cow.isBaby() && CowVariantCompat.hasBullCowCalfPortResources()) {
                cir.setReturnValue(Optional.of(CowVariantCompat.BULL_COW_CALF_TEXTURE));
            } else if (!cow.isBaby() && CowVariantCompat.hasBullCowPortResources()) {
                cir.setReturnValue(Optional.of(CowVariantCompat.BULL_COW_TEXTURE));
            }
            return;
        }
        Object variantData = VariantDataHolder.getHolder(cow).getVariantData().orElse(null);
        if (!(variantData instanceof CowVariant variant)) {
            return;
        }
        if (CowBiomeVariants.isBirchForest(variant)) {
            if (cow.isBaby() && CowVariantCompat.hasBirchForestCowCalfPortResources()) {
                cir.setReturnValue(Optional.of(CowVariantCompat.BIRCH_FOREST_COW_CALF_TEXTURE));
            } else if (!cow.isBaby() && CowVariantCompat.hasBirchForestCowPortResources()) {
                cir.setReturnValue(Optional.of(CowVariantCompat.BIRCH_FOREST_COW_TEXTURE));
            } else {
                // The registry entry points into the optional pack; avoid a missing-texture cow.
                cir.setReturnValue(Optional.of(CowVariantCompat.VANILLA_COW_TEXTURE));
            }
        } else if (VariantUtils.matches(CowVariants.REGISTRY, variant, CowVariants.WARM)) {
            if (CowVariantCompat.hasWarmCowPortResources()) {
                cir.setReturnValue(Optional.of(CowVariantCompat.WARM_COW_TEXTURE));
            }
        } else if (VariantUtils.matches(CowVariants.REGISTRY, variant, CowVariants.COLD)) {
            if (CowVariantCompat.hasColdCowPortResources()) {
                cir.setReturnValue(Optional.of(CowVariantCompat.COLD_COW_TEXTURE));
            }
        }
    }
}
