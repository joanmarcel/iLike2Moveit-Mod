package mac.ilike2moveit.mixin;

import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Exposes ModelPart's private `children` map so the item layer can walk the bone tree
 * looking for "alt_arms"/"arms".
 */
@Mixin(ModelPart.class)
public interface ModelPartAccessor {
    @Accessor("children")
    Map<String, ModelPart> ilike2moveit$getChildren();
}
