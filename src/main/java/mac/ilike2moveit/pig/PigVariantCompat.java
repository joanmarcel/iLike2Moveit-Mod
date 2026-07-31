package mac.ilike2moveit.pig;

import mac.ilike2moveit.ILike2MoveItMod;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * FA + VanillaBackport compat for the WARM pig (same case as the warm chickens).
 *
 * VanillaBackport assigns NORMAL to both temperate AND warm (default model = FA Extensions' pig.jem,
 * textureSize [64,64]), but the warm_pig.png that wins the stack is [64,32] (VB injects it from its
 * own pack at the top) -> a 64x64 model samples a 64x32 texture -> UVs halved -> deformed.
 *
 * Fix (warm_chicken pattern): register a dedicated layer whose model has textureSize [64,32], which
 * MATCHES the texture. EMF bakes the source JEM warm_pig.jem ([64,32], FA style) into this layer.
 */
public final class PigVariantCompat {

    public static final ModelLayerLocation WARM_PIG_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "warm_pig"), "main"
    );

    // Our own 64x32 warm texture (VBP's warm colour + FA's painted eye). VBP wins the base path
    // textures/entity/pig/warm_pig.png (without the FA eye); this one sits next to the jem and is
    // forced through getTexture (rooster pattern) for the warm pig, recovering the white of the eye.
    public static final ResourceLocation WARM_PIG_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "optifine/cem/warm_pig.png"
    );

    private PigVariantCompat() {
    }

    public static void registerLayerDefinition(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WARM_PIG_LAYER, () -> PigModel.createBodyLayer(CubeDeformation.NONE));
        ILike2MoveItMod.LOGGER.info("[Pig Compat] warm_pig layer registered.");
    }
}
