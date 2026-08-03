package mac.ilike2moveit.mixin;

import com.blackgear.vanillabackport.client.api.renderer.AbstractVariantRenderer;
import com.blackgear.vanillabackport.client.api.renderer.ChickenVariantRenderer;
import com.blackgear.vanillabackport.common.api.variant.VariantDataHolder;
import com.blackgear.vanillabackport.common.api.variant.VariantUtils;
import com.blackgear.vanillabackport.common.level.entities.animal.ChickenVariant;
import com.blackgear.vanillabackport.common.level.entities.animal.ChickenVariants;
import mac.ilike2moveit.MoveItCore;
import mac.ilike2moveit.chicken.ChickenVariantCompat;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * VanillaBackport 1.1.7.10 assigns NORMAL to both temperate and warm, and only bakes a dedicated
 * model for COLD. We add warm to the selector without touching the temperate default. For the
 * rooster we reuse ModelLayers.CHICKEN: EMF recognises that layer and chicken.properties picks
 * chicken4.jem.
 */
@Mixin(AbstractVariantRenderer.class)
public abstract class ChickenVariantRendererMixin {
    @Unique
    private ChickenModel<Chicken> ilike2moveit$warmChickenModel;

    @Unique
    private ChickenModel<Chicken> ilike2moveit$roosterModel;

    @Unique
    private boolean ilike2moveit$loggedWarmSelection;

    @Unique
    private boolean ilike2moveit$loggedRoosterSelection;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ilike2moveit$bakeWarmChickenModel(EntityRendererProvider.Context context, CallbackInfo ci) {
        if ((Object) this instanceof ChickenVariantRenderer) {
            ilike2moveit$warmChickenModel = new ChickenModel<>(
                    context.bakeLayer(ChickenVariantCompat.WARM_CHICKEN_LAYER)
            );
            ilike2moveit$roosterModel = new ChickenModel<>(context.bakeLayer(ModelLayers.CHICKEN));
        }
    }

    @Inject(
            method = "getModel(Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ilike2moveit$selectDedicatedWarmModel(
            LivingEntity entity, CallbackInfoReturnable<Optional<?>> cir
    ) {
        if (!(entity instanceof Chicken chicken) || chicken.isBaby()) {
            return;
        }

        if (ilike2moveit$roosterModel != null && ChickenVariantCompat.isRooster(chicken)) {
            cir.setReturnValue(Optional.of(ilike2moveit$roosterModel));
            if (!ilike2moveit$loggedRoosterSelection) {
                ilike2moveit$loggedRoosterSelection = true;
                MoveItCore.LOGGER.info(
                        "[Chicken Compat] gallo usa ModelLayers.CHICKEN y chicken4.jem."
                );
            }
            return;
        }

        if (ilike2moveit$warmChickenModel == null) {
            return;
        }

        Object variantData = VariantDataHolder.getHolder(chicken).getVariantData().orElse(null);
        if (variantData instanceof ChickenVariant variant
                && VariantUtils.matches(ChickenVariants.REGISTRY, variant, ChickenVariants.WARM)) {
            cir.setReturnValue(Optional.of(ilike2moveit$warmChickenModel));
            if (!ilike2moveit$loggedWarmSelection) {
                ilike2moveit$loggedWarmSelection = true;
                MoveItCore.LOGGER.info(
                        "[Chicken Compat] warm adulto usa el modelo dedicado warm_chicken; cold queda intacto."
                );
            }
        }
    }

    @Inject(
            method = "getTexture(Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ilike2moveit$selectRoosterTexture(
            LivingEntity entity, CallbackInfoReturnable<Optional<ResourceLocation>> cir
    ) {
        if ((Object) this instanceof ChickenVariantRenderer
                && entity instanceof Chicken chicken
                && ChickenVariantCompat.isRooster(chicken)) {
            cir.setReturnValue(Optional.of(ChickenVariantCompat.ROOSTER_TEXTURE));
        }
    }
}
