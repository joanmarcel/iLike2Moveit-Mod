package mac.ilike2moveit.fabric;

import mac.ilike2moveit.MoveItCore;
import mac.ilike2moveit.chicken.ChickenVariantCompat;
import mac.ilike2moveit.cat.CatVariantCompat;
import mac.ilike2moveit.cow.CowVariantCompat;
import mac.ilike2moveit.fox.FoxSleepParticleEmitter;
import mac.ilike2moveit.fox.FoxZzzParticle;
import mac.ilike2moveit.fox.MoveItParticles;
import mac.ilike2moveit.pig.PigVariantCompat;
import mac.ilike2moveit.wolf.WolfReunionTracker;
import mac.ilike2moveit.wolf.WolfVariantCompat;
import mac.ilike2moveit.network.ServerBridgePayload;
import mac.ilike2moveit.network.ServerBridgeState;
import mac.ilike2moveit.ocelot.OcelotVariantCompat;
import mac.ilike2moveit.rabbit.RabbitVariantCompat;
import mac.ilike2moveit.sheep.SheepVariantCompat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.model.geom.ModelLayerLocation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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

        registerSharedVanillaBackportLayer(ChickenVariantCompat.WARM_CHICKEN_LAYER,
                ChickenVariantCompat::createWarmChickenLayer);
        EntityModelLayerRegistry.registerModelLayer(ChickenVariantCompat.TINY_TAKEOVER_CHICKEN_LAYER,
                ChickenVariantCompat::createTinyTakeoverChickenLayer);
        EntityModelLayerRegistry.registerModelLayer(CatVariantCompat.CLASSIC_BABY_CAT_LAYER,
                CatVariantCompat::createClassicBabyCatLayer);
        EntityModelLayerRegistry.registerModelLayer(CatVariantCompat.CLASSIC_BABY_CAT_COLLAR_LAYER,
                CatVariantCompat::createClassicBabyCatLayer);
        EntityModelLayerRegistry.registerModelLayer(CatVariantCompat.TINY_TAKEOVER_CAT_LAYER,
                CatVariantCompat::createTinyTakeoverCatLayer);
        EntityModelLayerRegistry.registerModelLayer(CatVariantCompat.TINY_TAKEOVER_CAT_COLLAR_LAYER,
                CatVariantCompat::createTinyTakeoverCatLayer);
        EntityModelLayerRegistry.registerModelLayer(WolfVariantCompat.TINY_TAKEOVER_WOLF_LAYER,
                WolfVariantCompat::createTinyTakeoverWolfLayer);
        EntityModelLayerRegistry.registerModelLayer(WolfVariantCompat.TINY_TAKEOVER_WOLF_COLLAR_LAYER,
                WolfVariantCompat::createTinyTakeoverWolfLayer);
        EntityModelLayerRegistry.registerModelLayer(WolfVariantCompat.TINY_TAKEOVER_WOLF_ARMOR_LAYER,
                WolfVariantCompat::createTinyTakeoverWolfLayer);
        EntityModelLayerRegistry.registerModelLayer(OcelotVariantCompat.CLASSIC_BABY_OCELOT_LAYER,
                OcelotVariantCompat::createClassicBabyOcelotLayer);
        EntityModelLayerRegistry.registerModelLayer(OcelotVariantCompat.TINY_TAKEOVER_OCELOT_LAYER,
                OcelotVariantCompat::createTinyTakeoverOcelotLayer);
        EntityModelLayerRegistry.registerModelLayer(RabbitVariantCompat.TINY_TAKEOVER_RABBIT_LAYER,
                RabbitVariantCompat::createTinyTakeoverRabbitLayer);
        EntityModelLayerRegistry.registerModelLayer(SheepVariantCompat.ALTERNATE_SHEEP_LAYER,
                SheepVariantCompat::createAlternateSheepLayer);
        EntityModelLayerRegistry.registerModelLayer(SheepVariantCompat.ALTERNATE_SHEEP_WOOL_LAYER,
                SheepVariantCompat::createAlternateSheepWoolLayer);
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
        registerSharedVanillaBackportLayer(PigVariantCompat.WARM_PIG_LAYER,
                PigVariantCompat::createAdultPigLayer);
        registerSharedVanillaBackportLayer(PigVariantCompat.COLD_PIG_LAYER,
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
        EntityModelLayerRegistry.registerModelLayer(PigVariantCompat.TINY_TAKEOVER_PIGLET_LAYER,
                PigVariantCompat::createPigletLayer);
        MoveItCore.LOGGER.info("[Chicken Compat] warm_chicken layer registered.");
        MoveItCore.LOGGER.info("[Cat Compat] Classic and Tiny Takeover baby body/collar layers registered.");
        MoveItCore.LOGGER.info("[Sheep Compat] Vanilla and Alternate adult base/wool layers registered.");
        MoveItCore.LOGGER.info("[Cow Compat] named, climate, biome-breed and calf layers registered.");
        MoveItCore.LOGGER.info("[Pig Compat] named, climate, biome-breed and piglet layers registered.");

        // Wolf before fox, the same order the NeoForge tick listener calls them in. Fabric invokes
        // listeners of one phase in registration order, so this ordering is the one that runs.
        ClientTickEvents.END_CLIENT_TICK.register(WolfReunionTracker::clientTick);
        ClientTickEvents.END_CLIENT_TICK.register(FoxSleepParticleEmitter::clientTick);

        FabricWolfCommand.register();
    }

    /**
     * Registers a compatibility layer only when VanillaBackport does not already own the exact
     * {@link ModelLayerLocation}. Version checks are insufficient here: 1.1.5.2 declares cold_pig
     * under its old namespace, while 1.1.5.3+ declares minecraft:cold_pig and Fabric rejects a
     * second registration. Inspecting the exported locations also protects warm layers if a future
     * VanillaBackport release starts providing them.
     */
    private static void registerSharedVanillaBackportLayer(
            ModelLayerLocation layer,
            EntityModelLayerRegistry.TexturedModelDataProvider provider
    ) {
        if (vanillaBackportOwns(layer)) {
            MoveItCore.LOGGER.info("[VanillaBackport Compat] reusing model layer {}.", layer);
            return;
        }
        EntityModelLayerRegistry.registerModelLayer(layer, provider);
    }

    private static boolean vanillaBackportOwns(ModelLayerLocation layer) {
        if (!FabricLoader.getInstance().isModLoaded("vanillabackport")) {
            return false;
        }
        try {
            Class<?> modelLayers = Class.forName(
                    "com.blackgear.vanillabackport.client.registries.ModModelLayers",
                    true,
                    ILike2MoveItFabric.class.getClassLoader()
            );
            for (Field field : modelLayers.getFields()) {
                if (Modifier.isStatic(field.getModifiers())
                        && ModelLayerLocation.class.isAssignableFrom(field.getType())
                        && layer.equals(field.get(null))) {
                    return true;
                }
            }
        } catch (ReflectiveOperationException | LinkageError error) {
            MoveItCore.LOGGER.warn(
                    "[VanillaBackport Compat] could not inspect exported model layers; registering {}.",
                    layer,
                    error
            );
        }
        return false;
    }
}
