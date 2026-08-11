package mac.ilike2moveit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mac.ilike2moveit.cat.CatRestingBodyLock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Deque;

/** Locks the rendered body yaw at the last standing heading without touching head yaw. */
@Mixin(LivingEntityRenderer.class)
public abstract class CatRendererYawLockMixin {
    @Unique
    private static final ThreadLocal<Deque<float[]>> ILIKE2MOVEIT$CAT_YAW_STACK =
            ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method = "render", at = @At("HEAD"))
    private void ilike2moveit$lockRestingCatYaw(LivingEntity entity, float entityYaw,
            float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            CallbackInfo ci) {
        Deque<float[]> stack = ILIKE2MOVEIT$CAT_YAW_STACK.get();
        if (entity instanceof Cat cat) {
            Float anchorYaw = CatRestingBodyLock.restingAnchorYaw(cat);
            if (anchorYaw != null) {
                stack.push(new float[] {cat.yBodyRotO, cat.yBodyRot});
                cat.yBodyRotO = anchorYaw;
                cat.setYBodyRot(anchorYaw);
                return;
            }
        }
        stack.push(new float[0]);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void ilike2moveit$restoreRestingCatYaw(LivingEntity entity, float entityYaw,
            float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            CallbackInfo ci) {
        Deque<float[]> stack = ILIKE2MOVEIT$CAT_YAW_STACK.get();
        float[] saved = stack.pop();
        if (saved.length == 2 && entity instanceof Cat cat) {
            cat.yBodyRotO = saved[0];
            cat.setYBodyRot(saved[1]);
        }
        if (stack.isEmpty()) {
            ILIKE2MOVEIT$CAT_YAW_STACK.remove();
        }
    }
}
