package mac.ilike2moveit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mac.ilike2moveit.wolf.WolfRestingBodyLock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Deque;

/** Locks only a resting wolf's rendered body yaw; the head remains free to track within its FOV. */
@Mixin(LivingEntityRenderer.class)
public abstract class WolfRendererYawLockMixin {
    @Unique
    private static final ThreadLocal<Deque<float[]>> ILIKE2MOVEIT$WOLF_YAW_STACK =
            ThreadLocal.withInitial(ArrayDeque::new);

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
