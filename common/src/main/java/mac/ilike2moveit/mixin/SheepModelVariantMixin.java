package mac.ilike2moveit.mixin;

import mac.ilike2moveit.sheep.SheepAlternateModelAccess;
import mac.ilike2moveit.sheep.SheepVariantCompat;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.world.entity.animal.Sheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Bakes the selectable adult sheep base models once with the renderer. */
@Mixin(SheepRenderer.class)
public abstract class SheepModelVariantMixin implements SheepAlternateModelAccess {
    @Unique
    private SheepModel<Sheep> ilike2moveit$alternateSheepModel;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ilike2moveit$bakeAlternateSheepModel(
            EntityRendererProvider.Context context, CallbackInfo ci
    ) {
        ilike2moveit$alternateSheepModel = new SheepModel<>(
                context.bakeLayer(SheepVariantCompat.ALTERNATE_SHEEP_LAYER));
    }

    @Override
    public SheepModel<Sheep> ilike2moveit$alternateSheepModel() {
        return ilike2moveit$alternateSheepModel;
    }
}
