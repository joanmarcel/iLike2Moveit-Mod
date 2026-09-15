package mac.ilike2moveit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mac.ilike2moveit.config.MobModelConfig;
import mac.ilike2moveit.render.RendererModelSwapStack;
import mac.ilike2moveit.wolf.WolfRestingBodyLock;
import mac.ilike2moveit.wolf.WolfTinyModelAccess;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Deque;

/** Locks only a resting wolf's rendered body yaw; the head remains free to track within its FOV. */
@Mixin(LivingEntityRenderer.class)
public abstract class WolfRendererYawLockMixin {
    @Shadow
    protected EntityModel<?> model;

    @Unique
    private static final ThreadLocal<Deque<float[]>> ILIKE2MOVEIT$WOLF_YAW_STACK =
            ThreadLocal.withInitial(ArrayDeque::new);

    @Unique
    private static final RendererModelSwapStack<EntityModel<?>> ILIKE2MOVEIT$WOLF_MODEL_SWAP =
            new RendererModelSwapStack<>();

    @Inject(method = "render", at = @At("HEAD"))
    private void ilike2moveit$selectTinyWolfModel(LivingEntity entity, float entityYaw,
            float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            CallbackInfo ci) {
        if (!(entity instanceof Wolf wolf)
                || !((Object) this instanceof WolfTinyModelAccess access)) {
            return;
        }
        EntityModel<?> babyModel = null;
        if (wolf.isBaby()
                && MobModelConfig.wolfBabyModel() == MobModelConfig.WolfBabyModel.TINY_TAKEOVER) {
            babyModel = access.ilike2moveit$tinyTakeoverWolfModel();
        }
        model = ILIKE2MOVEIT$WOLF_MODEL_SWAP.begin(model, babyModel);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void ilike2moveit$restoreWolfModel(LivingEntity entity, float entityYaw,
            float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            CallbackInfo ci) {
        if (!(entity instanceof Wolf)
                || !((Object) this instanceof WolfTinyModelAccess)) {
            return;
        }
        model = ILIKE2MOVEIT$WOLF_MODEL_SWAP.end(model);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void ilike2moveit$lockRestingWolfYaw(LivingEntity entity, float entityYaw,
            float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            CallbackInfo ci) {
        Deque<float[]> stack = ILIKE2MOVEIT$WOLF_YAW_STACK.get();
        if (entity instanceof Wolf wolf) {
            Float anchorYaw = WolfRestingBodyLock.restingAnchorYaw(wolf);
            if (anchorYaw != null) {
                stack.push(new float[] {wolf.yBodyRotO, wolf.yBodyRot});
                wolf.yBodyRotO = anchorYaw;
                wolf.setYBodyRot(anchorYaw);
                return;
            }
        }
        stack.push(new float[0]);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void ilike2moveit$restoreRestingWolfYaw(LivingEntity entity, float entityYaw,
            float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            CallbackInfo ci) {
        Deque<float[]> stack = ILIKE2MOVEIT$WOLF_YAW_STACK.get();
        float[] saved = stack.pop();
        if (saved.length == 2 && entity instanceof Wolf wolf) {
            wolf.yBodyRotO = saved[0];
            wolf.setYBodyRot(saved[1]);
        }
        if (stack.isEmpty()) {
            ILIKE2MOVEIT$WOLF_YAW_STACK.remove();
        }
    }
}
