package mac.ilike2moveit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mac.ilike2moveit.sheep.SheepUndercoatCompat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.animal.Sheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prevents VanillaBackport from tinting a second complete sheep mesh over iLike2MoveIt's split rig. */
@Pseudo
@Mixin(targets = "com.blackgear.vanillabackport.client.level.entities.layer.SheepWoolUndercoatLayer")
public abstract class SheepWoolUndercoatLayerMixin {

    @Inject(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/Sheep;FFFFFF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void ilike2moveit$suppressDuplicateTintedMesh(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            Sheep sheep,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci) {
        if (SheepUndercoatCompat.shouldSuppressVanillaBackportUndercoat()) {
            ci.cancel();
        }
    }
}
