package mac.ilike2moveit.fox;

import mac.ilike2moveit.ILike2MoveItMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry for the pack's own particles.
 *
 * <p>For now only the sleeping fox's "Zzz" emote ({@code ilike2moveit:fox_zzz}), ported from a
 * Bedrock particle effect. It is declared with {@link DeferredRegister} over
 * {@link BuiltInRegistries#PARTICLE_TYPE} and its client provider hooks into
 * {@link RegisterParticleProvidersEvent}. Client-side only; touches neither the server nor cat/wolf.
 */
public final class MoveItParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, ILike2MoveItMod.MODID);

    /** Sleeping fox "Zzz" emote. {@code overrideLimiter=false}: honours the distance-based count. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FOX_ZZZ =
            PARTICLE_TYPES.register("fox_zzz", () -> new SimpleParticleType(false));

    private MoveItParticles() {
    }

    /** Hooks the DeferredRegister into the mod bus (call from the @Mod constructor). */
    public static void register(IEventBus modEventBus) {
        PARTICLE_TYPES.register(modEventBus);
    }

    /** Binds the client provider to the particle type (mod bus event). */
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(FOX_ZZZ.get(), FoxZzzParticle.Provider::new);
        ILike2MoveItMod.LOGGER.info("[Fox] particle provider '{}' registered.", "fox_zzz");
    }
}
