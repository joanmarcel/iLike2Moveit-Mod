package mac.ilike2moveit.sheep;

import mac.ilike2moveit.MoveItCore;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

/** Decides whether iLike2MoveIt's split skin/wool sheep rig replaces VanillaBackport's undercoat. */
public final class SheepUndercoatCompat {
    private static final ResourceLocation BASE_JEM = minecraft("optifine/cem/sheep.jem");
    private static final ResourceLocation WOOL_JEM = minecraft("optifine/cem/sheep_wool.jem");
    private static final ResourceLocation BASE_TEXTURE = minecraft("optifine/cem/sheep.png");
    private static final ResourceLocation WOOL_TEXTURE = minecraft("optifine/cem/sheep_wool.png");

    private static volatile Boolean completePortResources;

    private SheepUndercoatCompat() {
    }

    /**
     * Returns true only when the active resource stack contains the complete iLike2MoveIt sheep
     * pair. Failing open keeps VanillaBackport's feature intact when the RP is absent or incomplete.
     */
    public static boolean shouldSuppressVanillaBackportUndercoat() {
        Boolean cached = completePortResources;
        if (cached != null) {
            return cached;
        }

        Minecraft minecraft = Minecraft.getInstance();
        boolean complete = minecraft != null
                && minecraft.getResourceManager().getResource(BASE_JEM).isPresent()
                && minecraft.getResourceManager().getResource(WOOL_JEM).isPresent()
                && minecraft.getResourceManager().getResource(BASE_TEXTURE).isPresent()
                && minecraft.getResourceManager().getResource(WOOL_TEXTURE).isPresent();
        completePortResources = complete;
        MoveItCore.LOGGER.info(
                "[Sheep Compat] VanillaBackport dyed undercoat {}: iLike2MoveIt sheep resources {}.",
                complete ? "suppressed" : "preserved",
                complete ? "complete" : "absent/incomplete");
        return complete;
    }

    /** Invalidates the resource-stack decision after startup and every F3+T reload. */
    public static void resetResourceDecision() {
        completePortResources = null;
    }

    private static ResourceLocation minecraft(String path) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", path);
    }
}
