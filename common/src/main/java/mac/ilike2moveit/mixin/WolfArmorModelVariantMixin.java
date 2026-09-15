package mac.ilike2moveit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mac.ilike2moveit.config.MobModelConfig;
import mac.ilike2moveit.wolf.WolfVariantCompat;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.WolfArmorLayer;
import net.minecraft.world.entity.animal.Wolf;
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

/** Uses the intentionally hidden Tiny armor JEM while preserving adult and Classic behavior. */
@Mixin(WolfArmorLayer.class)
public abstract class WolfArmorModelVariantMixin {
    @Shadow @Final @Mutable private WolfModel<Wolf> model;
    @Unique private WolfModel<Wolf> ilike2moveit$tinyArmorModel;
    @Unique private static final ThreadLocal<Deque<WolfModel<Wolf>>> ILIKE2MOVEIT$ARMOR_STACK =
            ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ilike2moveit$bakeTinyArmor(RenderLayerParent<Wolf, WolfModel<Wolf>> renderer,
            EntityModelSet models, CallbackInfo ci) {
        ilike2moveit$tinyArmorModel = new WolfModel<>(
                models.bakeLayer(WolfVariantCompat.TINY_TAKEOVER_WOLF_ARMOR_LAYER));
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void ilike2moveit$selectTinyArmor(PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, Wolf wolf, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch,
            CallbackInfo ci) {
        ILIKE2MOVEIT$ARMOR_STACK.get().push(model);
        if (wolf.isBaby()
                && MobModelConfig.wolfBabyModel() == MobModelConfig.WolfBabyModel.TINY_TAKEOVER) {
            model = ilike2moveit$tinyArmorModel;
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void ilike2moveit$restoreArmor(PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, Wolf wolf, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch,
            CallbackInfo ci) {
        Deque<WolfModel<Wolf>> stack = ILIKE2MOVEIT$ARMOR_STACK.get();
        model = stack.pop();
        if (stack.isEmpty()) {
            ILIKE2MOVEIT$ARMOR_STACK.remove();
        }
    }
}
