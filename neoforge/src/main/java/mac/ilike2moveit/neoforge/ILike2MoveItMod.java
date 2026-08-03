package mac.ilike2moveit.neoforge;

import mac.ilike2moveit.MoveItCore;
import mac.ilike2moveit.chicken.ChickenVariantCompat;
import mac.ilike2moveit.fox.FoxSleepParticleEmitter;
import mac.ilike2moveit.fox.FoxZzzParticle;
import mac.ilike2moveit.fox.MoveItParticles;
import mac.ilike2moveit.pig.PigVariantCompat;
import mac.ilike2moveit.render.ItemTransformCompat;
import mac.ilike2moveit.wolf.WolfReunionTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * NeoForge entrypoint. Registers what the loader owns and delegates every behaviour to common code.
 */
@Mod(value = MoveItCore.MODID, dist = Dist.CLIENT)
public class ILike2MoveItMod {
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, MoveItCore.MODID);

    /** Sleeping fox "Zzz" emote. {@code overrideLimiter=false}: honours the distance-based count. */
    private static final DeferredHolder<ParticleType<?>, SimpleParticleType> FOX_ZZZ =
            PARTICLE_TYPES.register(MoveItParticles.FOX_ZZZ_ID.getPath(),
                    () -> new SimpleParticleType(false));

    public ILike2MoveItMod(IEventBus modEventBus) {
        MoveItCore.LOGGER.info("[iLike2MoveIt] client bridge loaded (neoforge).");
        // Before any registration: enforce() must run as early as it did in the single-loader
        // version, or the villager does not animate and the symptom hides the cause.
        MoveItCore.bootstrap();
        // NeoForge patches rightRotation into vanilla's ItemTransform; common defaults to identity.
        ItemTransformCompat.setRightRotationAccessor(transform -> transform.rightRotation);
        PARTICLE_TYPES.register(modEventBus);
        modEventBus.addListener(ILike2MoveItMod::registerLayerDefinitions);
        modEventBus.addListener(ILike2MoveItMod::registerParticleProviders);
        NeoForge.EVENT_BUS.addListener(ILike2MoveItMod::onClientTick);
        NeoForge.EVENT_BUS.addListener(NeoForgeWolfCommand::register);
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ChickenVariantCompat.WARM_CHICKEN_LAYER,
                ChickenVariantCompat::createWarmChickenLayer);
        event.registerLayerDefinition(PigVariantCompat.WARM_PIG_LAYER,
                PigVariantCompat::createWarmPigLayer);
        MoveItCore.LOGGER.info("[Chicken Compat] warm_chicken layer registered.");
        MoveItCore.LOGGER.info("[Pig Compat] warm_pig layer registered.");
    }

    private static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        MoveItParticles.setFoxZzz(FOX_ZZZ.get());
        event.registerSpriteSet(FOX_ZZZ.get(), FoxZzzParticle.Provider::new);
        MoveItCore.LOGGER.info("[Fox] particle provider '{}' registered.", MoveItParticles.FOX_ZZZ_ID);
    }

    private static long clientTicks = 0L;
    private static boolean windowTitleLogged = false;

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        applyWindowTitle(minecraft);
        WolfReunionTracker.clientTick(minecraft);
        FoxSleepParticleEmitter.clientTick(minecraft);
    }

    /**
     * Dev-only: label the per-worktree verification client window (see neoforge/build.gradle,
     * {@code -PmcTitle="Minecraft (dolphin)" -> -Dil2m.windowTitle}). {@code -Xdock:name} only renames
     * the macOS menu; the GLFW window title still reads "NeoForge 1.21.1", so we set it here. Reasserted
     * every ~2 s because NeoForge/Minecraft may rewrite the title (e.g. on level load).
     */
    private static void applyWindowTitle(Minecraft minecraft) {
        String title = System.getProperty("il2m.windowTitle");
        if (title == null || title.isBlank() || minecraft == null || minecraft.getWindow() == null) {
            return;
        }
        if (clientTicks++ % 40L == 0L) {
            minecraft.getWindow().setTitle(title);
            if (!windowTitleLogged) {
                MoveItCore.LOGGER.info("[iLike2MoveIt] window title set to '{}'.", title);
                windowTitleLogged = true;
            }
        }
    }
}
