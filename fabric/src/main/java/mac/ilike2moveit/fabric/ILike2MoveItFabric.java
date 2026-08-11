package mac.ilike2moveit.fabric;

import mac.ilike2moveit.MoveItCore;
import mac.ilike2moveit.chicken.ChickenVariantCompat;
import mac.ilike2moveit.cow.CowVariantCompat;
import mac.ilike2moveit.fox.FoxSleepParticleEmitter;
import mac.ilike2moveit.fox.FoxZzzParticle;
import mac.ilike2moveit.fox.MoveItParticles;
import mac.ilike2moveit.pig.PigVariantCompat;
import mac.ilike2moveit.wolf.WolfReunionTracker;
import mac.ilike2moveit.network.ServerBridgePayload;
import mac.ilike2moveit.network.ServerBridgeState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Fabric entrypoint. Registers what the loader owns and delegates every behaviour to common code.
 * Mirror of {@code mac.ilike2moveit.neoforge.ILike2MoveItMod}: same order, same log lines.
 *
 * <p>No {@code ItemTransformCompat} accessor here on purpose: vanilla has no {@code rightRotation},
 * so common's identity default is already the correct behaviour under Fabric.
 */
public final class ILike2MoveItFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MoveItCore.LOGGER.info("[iLike2MoveIt] client bridge loaded (fabric).");
        // Before any registration, same as on NeoForge: enforce() has to run first or the villager
        // does not animate and the symptom hides the cause.
        MoveItCore.bootstrap();
        ServerBridgeState.reset();
        ClientPlayNetworking.registerGlobalReceiver(ServerBridgePayload.TYPE,
                (payload, context) -> ServerBridgeState.accept(payload));
        ClientPlayConnectionEvents.INIT.register((handler, client) ->
                ServerBridgeState.reset());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                ServerBridgeState.reset());

        // overrideLimiter=false: honours the distance-based particle count, like the NeoForge side.
        SimpleParticleType foxZzz = Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                MoveItParticles.FOX_ZZZ_ID, FabricParticleTypes.simple(false));
        MoveItParticles.setFoxZzz(foxZzz);
        ParticleFactoryRegistry.getInstance().register(foxZzz, FoxZzzParticle.Provider::new);
        MoveItCore.LOGGER.info("[Fox] particle provider '{}' registered.", MoveItParticles.FOX_ZZZ_ID);

        EntityModelLayerRegistry.registerModelLayer(ChickenVariantCompat.WARM_CHICKEN_LAYER,
                ChickenVariantCompat::createWarmChickenLayer);
        EntityModelLayerRegistry.registerModelLayer(CowVariantCompat.WARM_COW_LAYER,
                CowVariantCompat::createCowLayer);
        EntityModelLayerRegistry.registerModelLayer(CowVariantCompat.COLD_COW_LAYER,
                CowVariantCompat::createCowLayer);
        EntityModelLayerRegistry.registerModelLayer(CowVariantCompat.BIRCH_FOREST_COW_LAYER,
                CowVariantCompat::createCowLayer);
        EntityModelLayerRegistry.registerModelLayer(CowVariantCompat.BIRCH_FOREST_COW_CALF_LAYER,
                CowVariantCompat::createCowLayer);
        EntityModelLayerRegistry.registerModelLayer(CowVariantCompat.BULL_COW_LAYER,
                CowVariantCompat::createCowLayer);
        EntityModelLayerRegistry.registerModelLayer(CowVariantCompat.BULL_COW_CALF_LAYER,
                CowVariantCompat::createCowLayer);
        EntityModelLayerRegistry.registerModelLayer(PigVariantCompat.WARM_PIG_LAYER,
                PigVariantCompat::createAdultPigLayer);
        EntityModelLayerRegistry.registerModelLayer(PigVariantCompat.COLD_PIG_LAYER,
                PigVariantCompat::createAdultPigLayer);
        EntityModelLayerRegistry.registerModelLayer(PigVariantCompat.PIGLET_LAYER,
                PigVariantCompat::createPigletLayer);
        EntityModelLayerRegistry.registerModelLayer(PigVariantCompat.LEGEND_PIG_LAYER,
                PigVariantCompat::createAdultPigLayer);
        EntityModelLayerRegistry.registerModelLayer(PigVariantCompat.MR_PIGGY_LAYER,
                PigVariantCompat::createAdultPigLayer);
        EntityModelLayerRegistry.registerModelLayer(PigVariantCompat.REDCOAT_PIG_LAYER,
                PigVariantCompat::createAdultPigLayer);
        EntityModelLayerRegistry.registerModelLayer(PigVariantCompat.BIRCH_FOREST_PIG_LAYER,
                PigVariantCompat::createAdultPigLayer);
        EntityModelLayerRegistry.registerModelLayer(PigVariantCompat.SAVANNA_PIG_LAYER,
                PigVariantCompat::createAdultPigLayer);
        EntityModelLayerRegistry.registerModelLayer(PigVariantCompat.SAVANNA_SPOTTED_PIG_LAYER,
                PigVariantCompat::createAdultPigLayer);
        EntityModelLayerRegistry.registerModelLayer(PigVariantCompat.TAIGA_PIG_LAYER,
                PigVariantCompat::createAdultPigLayer);
        EntityModelLayerRegistry.registerModelLayer(PigVariantCompat.WARM_PIGLET_LAYER,
                PigVariantCompat::createPigletLayer);
        EntityModelLayerRegistry.registerModelLayer(PigVariantCompat.COLD_PIGLET_LAYER,
                PigVariantCompat::createPigletLayer);
        MoveItCore.LOGGER.info("[Chicken Compat] warm_chicken layer registered.");
        MoveItCore.LOGGER.info("[Cow Compat] named, climate, biome-breed and calf layers registered.");
        MoveItCore.LOGGER.info("[Pig Compat] named, climate, biome-breed and piglet layers registered.");

        // Wolf before fox, the same order the NeoForge tick listener calls them in. Fabric invokes
        // listeners of one phase in registration order, so this ordering is the one that runs.
        ClientTickEvents.END_CLIENT_TICK.register(WolfReunionTracker::clientTick);
        ClientTickEvents.END_CLIENT_TICK.register(FoxSleepParticleEmitter::clientTick);

        FabricWolfCommand.register();
    }
}
