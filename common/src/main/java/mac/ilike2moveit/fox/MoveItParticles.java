package mac.ilike2moveit.fox;

import mac.ilike2moveit.MoveItCore;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;

/**
 * Identity of the pack's own particles, and holder for the type each loader registers.
 *
 * <p>For now only the sleeping fox's "Zzz" emote ({@code ilike2moveit:fox_zzz}), ported from a Bedrock
 * particle effect. The registration itself is loader-specific (DeferredRegister on NeoForge,
 * Registry.register on Fabric); this class only keeps the resulting type reachable from common code.
 */
public final class MoveItParticles {
    public static final ResourceLocation FOX_ZZZ_ID =
            ResourceLocation.fromNamespaceAndPath(MoveItCore.MODID, "fox_zzz");

    private static SimpleParticleType foxZzz;

    private MoveItParticles() {
    }

    /** Called by the loader entrypoint right after registering the type. */
    public static void setFoxZzz(SimpleParticleType type) {
        foxZzz = type;
    }

    /** Null until the loader has registered it; emitters must tolerate that. */
    public static SimpleParticleType foxZzz() {
        return foxZzz;
    }
}
