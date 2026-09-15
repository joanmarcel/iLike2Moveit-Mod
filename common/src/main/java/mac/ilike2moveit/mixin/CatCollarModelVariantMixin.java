package mac.ilike2moveit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mac.ilike2moveit.cat.CatVariantCompat;
import mac.ilike2moveit.config.MobModelConfig;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CatCollarLayer;
import net.minecraft.world.entity.animal.Cat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Deque;

/** Keeps the dyed vanilla collar layer aligned with the selected Tiny Takeover kitten model. */
@Mixin(CatCollarLayer.class)
public abstract class CatCollarModelVariantMixin {
    @Shadow
    @Final
    @Mutable
    private CatModel<Cat> catModel;

    @Unique
    private CatModel<Cat> ilike2moveit$classicBabyCollarModel;

    @Unique
    private CatModel<Cat> ilike2moveit$tinyTakeoverCollarModel;

    @Unique
    private static final ThreadLocal<Deque<CatModel<Cat>>> ILIKE2MOVEIT$COLLAR_STACK =
            ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ilike2moveit$bakeTinyTakeoverCollar(
            RenderLayerParent<Cat, CatModel<Cat>> renderer, EntityModelSet modelSet, CallbackInfo ci
    ) {
        ilike2moveit$classicBabyCollarModel = new CatModel<>(
                modelSet.bakeLayer(CatVariantCompat.CLASSIC_BABY_CAT_COLLAR_LAYER)
        );
        ilike2moveit$tinyTakeoverCollarModel = new CatModel<>(
                modelSet.bakeLayer(CatVariantCompat.TINY_TAKEOVER_CAT_COLLAR_LAYER)
        );
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void ilike2moveit$selectTinyTakeoverCollar(
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, Cat cat,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch, CallbackInfo ci
    ) {
        ILIKE2MOVEIT$COLLAR_STACK.get().push(catModel);
        if (cat.isBaby()) {
            CatModel<Cat> babyModel =
                    MobModelConfig.catBabyModel() == MobModelConfig.CatBabyModel.TINY_TAKEOVER
                            ? ilike2moveit$tinyTakeoverCollarModel
                            : ilike2moveit$classicBabyCollarModel;
            if (babyModel != null) {
                catModel = babyModel;
            }
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void ilike2moveit$restoreCollarModel(
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, Cat cat,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch, CallbackInfo ci
    ) {
        Deque<CatModel<Cat>> stack = ILIKE2MOVEIT$COLLAR_STACK.get();
        catModel = stack.pop();
        if (stack.isEmpty()) {
            ILIKE2MOVEIT$COLLAR_STACK.remove();
        }
    }
}
