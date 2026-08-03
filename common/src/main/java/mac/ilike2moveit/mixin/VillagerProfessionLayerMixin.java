package mac.ilike2moveit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mac.ilike2moveit.gender.VillagerGender;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerHeadModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Villager GENDER layer (woman pilot). Injects a texture pass RIGHT AFTER the biome pass
 * (type = renderColoredCutoutModel ordinal 0, which ALWAYS runs) and BEFORE the profession one
 * (ordinal 1, conditional on having a job) -> exact hierarchy:
 *
 *     skin (base)  <  biome clothing (type)  <  GENDER  <  job (profession)  <  level
 *
 * The decision (stable 33% + exact skin&lt;-&gt;woman association through ETFApi + adults only) lives in
 * {@link VillagerGender}. Being a pass after the biome one, the woman's hair is drawn ON TOP of the
 * biome clothing (shoulders, for instance); being before the profession one, the job (hats) covers it.
 *
 * On 1.21.1 the pipeline is the classic one (10-arg render, no EntityRenderState). Client-side;
 * coexists with {@code CrossedArmsItemLayerMixin} (they mixin different classes).
 */
@Mixin(VillagerProfessionLayer.class)
public abstract class VillagerProfessionLayerMixin<T extends LivingEntity & VillagerDataHolder, M extends EntityModel<T> & VillagerHeadModel>
        extends RenderLayer<T, M> {

    private VillagerProfessionLayerMixin(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Inject(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
        at = @At(
            value = "INVOKE",
            // No owner: the invokestatic in the bytecode resolves the inherited static method with owner
            // VillagerProfessionLayer (the subclass), not RenderLayer -> pinning the owner matches 0
            // times. The full descriptor disambiguates just as well.
            target = "renderColoredCutoutModel(Lnet/minecraft/client/model/EntityModel;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;I)V",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void ilike2moveit$genderLayer(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                                          float limbSwing, float limbSwingAmount, float partialTicks,
                                          float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof Villager villager)) {
            return;   // WanderingTrader does not use this layer; guard anyway.
        }
        ResourceLocation womanTex = VillagerGender.womanTextureFor(villager);
        if (womanTex == null) {
            return;
        }
        M model = this.getParentModel();
        model.hatVisible(true);   // make sure the headwear (where the hair sits) is drawn in this pass.
        renderColoredCutoutModel(model, womanTex, poseStack, buffer, packedLight, entity, -1);
    }
}
