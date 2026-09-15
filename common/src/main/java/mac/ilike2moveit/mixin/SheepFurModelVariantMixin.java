package mac.ilike2moveit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mac.ilike2moveit.config.MobModelConfig;
import mac.ilike2moveit.render.RendererModelSwapStack;
import mac.ilike2moveit.sheep.SheepVariantCompat;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.SheepFurLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.animal.Sheep;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps the dyed wool pass aligned with the selected adult sheep base model. */
@Mixin(SheepFurLayer.class)
public abstract class SheepFurModelVariantMixin {
    @Shadow
    @Final
    @Mutable
    private SheepFurModel<Sheep> model;

    @Unique
    private SheepFurModel<Sheep> ilike2moveit$alternateSheepWoolModel;

    @Unique
    private static final RendererModelSwapStack<SheepFurModel<Sheep>> ILIKE2MOVEIT$SHEEP_WOOL_SWAP =
            new RendererModelSwapStack<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ilike2moveit$bakeAlternateSheepWoolModel(
            RenderLayerParent<Sheep, SheepModel<Sheep>> renderer,
            EntityModelSet modelSet,
            CallbackInfo ci
    ) {
        ilike2moveit$alternateSheepWoolModel = new SheepFurModel<>(
                modelSet.bakeLayer(SheepVariantCompat.ALTERNATE_SHEEP_WOOL_LAYER));
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void ilike2moveit$selectAlternateSheepWool(PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, Sheep sheep, float limbSwing,
            float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
            float headPitch, CallbackInfo ci) {
        SheepFurModel<Sheep> replacement = null;
        if (!sheep.isBaby()) {
            replacement = switch (MobModelConfig.sheepAdultModel()) {
                case CLASSIC -> null;
                case ALTERNATE -> ilike2moveit$alternateSheepWoolModel;
            };
        }
        model = ILIKE2MOVEIT$SHEEP_WOOL_SWAP.begin(model, replacement);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void ilike2moveit$restoreSheepWool(PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, Sheep sheep, float limbSwing,
            float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
            float headPitch, CallbackInfo ci) {
        model = ILIKE2MOVEIT$SHEEP_WOOL_SWAP.end(model);
    }
}
