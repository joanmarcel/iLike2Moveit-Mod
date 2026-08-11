package mac.ilike2moveit.mixin;

import com.blackgear.vanillabackport.client.api.renderer.AbstractVariantRenderer;
import com.blackgear.vanillabackport.client.api.renderer.PigVariantRenderer;
import com.blackgear.vanillabackport.common.api.variant.VariantDataHolder;
import com.blackgear.vanillabackport.common.api.variant.VariantUtils;
import com.blackgear.vanillabackport.common.level.entities.animal.PigVariant;
import com.blackgear.vanillabackport.common.level.entities.animal.PigVariants;
import mac.ilike2moveit.MoveItCore;
import mac.ilike2moveit.pig.PigBiomeVariants;
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
 * Gives named and VanillaBackport pigs explicit EMF model layers and matching textures. Name-tag
 * overrides win before age/climate/breed; unnamed temperate pigs remain on pig.jem/pig.properties.
 */
@Mixin(AbstractVariantRenderer.class)
public abstract class PigVariantRendererMixin {

    @Unique
    private PigModel<Pig> ilike2moveit$warmPigModel;

    @Unique
    private PigModel<Pig> ilike2moveit$coldPigModel;

    @Unique
    private PigModel<Pig> ilike2moveit$pigletModel;

    @Unique
    private PigModel<Pig> ilike2moveit$legendPigModel;

    @Unique
    private PigModel<Pig> ilike2moveit$mrPiggyModel;

    @Unique
    private PigModel<Pig> ilike2moveit$redcoatPigModel;

    @Unique
    private PigModel<Pig> ilike2moveit$birchForestPigModel;

    @Unique
    private PigModel<Pig> ilike2moveit$savannaPigModel;

    @Unique
    private PigModel<Pig> ilike2moveit$savannaSpottedPigModel;

    @Unique
    private PigModel<Pig> ilike2moveit$taigaPigModel;

    @Unique
    private PigModel<Pig> ilike2moveit$warmPigletModel;

    @Unique
    private PigModel<Pig> ilike2moveit$coldPigletModel;

    @Unique
    private boolean ilike2moveit$loggedPigModels;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ilike2moveit$bakePigVariantModels(EntityRendererProvider.Context context, CallbackInfo ci) {
        if ((Object) this instanceof PigVariantRenderer) {
            ilike2moveit$warmPigModel = new PigModel<>(context.bakeLayer(PigVariantCompat.WARM_PIG_LAYER));
            ilike2moveit$coldPigModel = new PigModel<>(context.bakeLayer(PigVariantCompat.COLD_PIG_LAYER));
            ilike2moveit$pigletModel = new PigModel<>(context.bakeLayer(PigVariantCompat.PIGLET_LAYER));
            ilike2moveit$legendPigModel = new PigModel<>(context.bakeLayer(PigVariantCompat.LEGEND_PIG_LAYER));
            ilike2moveit$mrPiggyModel = new PigModel<>(context.bakeLayer(PigVariantCompat.MR_PIGGY_LAYER));
            ilike2moveit$redcoatPigModel = new PigModel<>(context.bakeLayer(PigVariantCompat.REDCOAT_PIG_LAYER));
            ilike2moveit$birchForestPigModel = new PigModel<>(context.bakeLayer(PigVariantCompat.BIRCH_FOREST_PIG_LAYER));
            ilike2moveit$savannaPigModel = new PigModel<>(context.bakeLayer(PigVariantCompat.SAVANNA_PIG_LAYER));
            ilike2moveit$savannaSpottedPigModel = new PigModel<>(context.bakeLayer(PigVariantCompat.SAVANNA_SPOTTED_PIG_LAYER));
            ilike2moveit$taigaPigModel = new PigModel<>(context.bakeLayer(PigVariantCompat.TAIGA_PIG_LAYER));
            ilike2moveit$warmPigletModel = new PigModel<>(context.bakeLayer(PigVariantCompat.WARM_PIGLET_LAYER));
            ilike2moveit$coldPigletModel = new PigModel<>(context.bakeLayer(PigVariantCompat.COLD_PIGLET_LAYER));
        }
    }

    @Inject(
            method = "getModel(Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ilike2moveit$selectPigVariantModel(LivingEntity entity, CallbackInfoReturnable<Optional<?>> cir) {
        if (ilike2moveit$warmPigModel == null || ilike2moveit$coldPigModel == null
                || ilike2moveit$pigletModel == null
                || ilike2moveit$legendPigModel == null || ilike2moveit$mrPiggyModel == null
                || ilike2moveit$redcoatPigModel == null
                || ilike2moveit$birchForestPigModel == null || ilike2moveit$savannaPigModel == null
                || ilike2moveit$savannaSpottedPigModel == null || ilike2moveit$taigaPigModel == null
                || !(entity instanceof Pig pig)) {
            return;
        }
        PigVariantCompat.NamedEasterEgg named = PigVariantCompat.namedEasterEgg(pig);
        if (named != null) {
            cir.setReturnValue(Optional.of(switch (named) {
                case LEGEND -> ilike2moveit$legendPigModel;
                case MR_PIGGY -> ilike2moveit$mrPiggyModel;
                case REDCOAT -> ilike2moveit$redcoatPigModel;
            }));
            return;
        }
        Object variantData = VariantDataHolder.getHolder(pig).getVariantData().orElse(null);
        if (!(variantData instanceof PigVariant variant)) {
            return;
        }
        boolean biomeBreed = PigBiomeVariants.isBirchForest(variant)
                || PigBiomeVariants.isSavanna(variant)
                || PigBiomeVariants.isTaiga(variant);
        if (pig.isBaby() && biomeBreed) {
            cir.setReturnValue(Optional.of(ilike2moveit$pigletModel));
            return;
        }
        if (PigBiomeVariants.isBirchForest(variant)) {
            cir.setReturnValue(Optional.of(ilike2moveit$birchForestPigModel));
            return;
        }
        if (PigBiomeVariants.isSavanna(variant)) {
            cir.setReturnValue(Optional.of(PigVariantCompat.isSavannaSpotted(pig)
                    ? ilike2moveit$savannaSpottedPigModel : ilike2moveit$savannaPigModel));
            return;
        }
        if (PigBiomeVariants.isTaiga(variant)) {
            cir.setReturnValue(Optional.of(ilike2moveit$taigaPigModel));
            return;
        }
        boolean warm = VariantUtils.matches(PigVariants.REGISTRY, variant, PigVariants.WARM);
        boolean cold = VariantUtils.matches(PigVariants.REGISTRY, variant, PigVariants.COLD);
        if (pig.isBaby() && warm) {
            cir.setReturnValue(Optional.of(ilike2moveit$warmPigletModel));
        } else if (pig.isBaby() && cold) {
            cir.setReturnValue(Optional.of(ilike2moveit$coldPigletModel));
        } else if (warm) {
            cir.setReturnValue(Optional.of(ilike2moveit$warmPigModel));
        } else if (cold) {
            cir.setReturnValue(Optional.of(ilike2moveit$coldPigModel));
        } else {
            return;
        }
        if (!ilike2moveit$loggedPigModels) {
            ilike2moveit$loggedPigModels = true;
            MoveItCore.LOGGER.info(
                    "[Pig Compat] warm/cold adults and piglets use dedicated models; "
                            + "temperate piglets remain routed through pig.properties."
            );
        }
    }

    @Inject(
            method = "getTexture(Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ilike2moveit$pigVariantTexture(LivingEntity entity, CallbackInfoReturnable<Optional<ResourceLocation>> cir) {
        if (!((Object) this instanceof PigVariantRenderer) || !(entity instanceof Pig pig)) {
            return;
        }
        ResourceLocation namedTexture = PigVariantCompat.namedTexture(pig);
        if (namedTexture != null) {
            cir.setReturnValue(Optional.of(namedTexture));
            return;
        }
        Object variantData = VariantDataHolder.getHolder(pig).getVariantData().orElse(null);
        if (!(variantData instanceof PigVariant variant)) {
            return;
        }
        ResourceLocation biomeTexture = PigVariantCompat.biomeTexture(pig, variant);
        if (biomeTexture != null) {
            cir.setReturnValue(Optional.of(biomeTexture));
            return;
        }
        if (VariantUtils.matches(PigVariants.REGISTRY, variant, PigVariants.WARM)) {
            cir.setReturnValue(Optional.of(
                    pig.isBaby() ? PigVariantCompat.WARM_PIGLET_TEXTURE : PigVariantCompat.WARM_PIG_TEXTURE
            ));
        } else if (VariantUtils.matches(PigVariants.REGISTRY, variant, PigVariants.COLD)) {
            cir.setReturnValue(Optional.of(
                    pig.isBaby() ? PigVariantCompat.COLD_PIGLET_TEXTURE : PigVariantCompat.COLD_PIG_TEXTURE
            ));
        }
    }
}
