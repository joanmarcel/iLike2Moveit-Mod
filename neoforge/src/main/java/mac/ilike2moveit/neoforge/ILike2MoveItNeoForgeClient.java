package mac.ilike2moveit.neoforge;

import mac.ilike2moveit.MoveItCore;
import mac.ilike2moveit.chicken.ChickenVariantCompat;
import mac.ilike2moveit.cat.CatVariantCompat;
import mac.ilike2moveit.cow.CowVariantCompat;
import mac.ilike2moveit.fox.FoxSleepParticleEmitter;
import mac.ilike2moveit.fox.FoxZzzParticle;
import mac.ilike2moveit.fox.MoveItParticles;
import mac.ilike2moveit.network.ServerBridgeState;
import mac.ilike2moveit.ocelot.OcelotVariantCompat;
import mac.ilike2moveit.rabbit.RabbitVariantCompat;
import mac.ilike2moveit.pig.PigVariantCompat;
import mac.ilike2moveit.render.ItemTransformCompat;
import mac.ilike2moveit.sheep.SheepVariantCompat;
import mac.ilike2moveit.wolf.WolfReunionTracker;
import mac.ilike2moveit.wolf.WolfVariantCompat;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Client-only NeoForge wiring, isolated so dedicated servers never resolve graphical classes. */
final class ILike2MoveItNeoForgeClient {
    private static long clientTicks;
    private static boolean windowTitleLogged;

    private ILike2MoveItNeoForgeClient() {
    }

    static void bootstrap(IEventBus modEventBus) {
        MoveItCore.LOGGER.info("[iLike2MoveIt] client bridge loaded (neoforge).");
        MoveItCore.bootstrap();
        ServerBridgeState.reset();
        ItemTransformCompat.setRightRotationAccessor(transform -> transform.rightRotation);
        modEventBus.addListener(ILike2MoveItNeoForgeClient::registerLayerDefinitions);
        modEventBus.addListener(ILike2MoveItNeoForgeClient::registerParticleProviders);
        NeoForge.EVENT_BUS.addListener(ILike2MoveItNeoForgeClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(ILike2MoveItNeoForgeClient::onLoggingIn);
        NeoForge.EVENT_BUS.addListener(ILike2MoveItNeoForgeClient::onLoggingOut);
        NeoForge.EVENT_BUS.addListener(NeoForgeWolfCommand::register);
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ChickenVariantCompat.WARM_CHICKEN_LAYER,
                ChickenVariantCompat::createWarmChickenLayer);
        event.registerLayerDefinition(ChickenVariantCompat.TINY_TAKEOVER_CHICKEN_LAYER,
                ChickenVariantCompat::createTinyTakeoverChickenLayer);
        event.registerLayerDefinition(CatVariantCompat.CLASSIC_BABY_CAT_LAYER,
                CatVariantCompat::createClassicBabyCatLayer);
        event.registerLayerDefinition(CatVariantCompat.CLASSIC_BABY_CAT_COLLAR_LAYER,
                CatVariantCompat::createClassicBabyCatLayer);
        event.registerLayerDefinition(CatVariantCompat.TINY_TAKEOVER_CAT_LAYER,
                CatVariantCompat::createTinyTakeoverCatLayer);
        event.registerLayerDefinition(CatVariantCompat.TINY_TAKEOVER_CAT_COLLAR_LAYER,
                CatVariantCompat::createTinyTakeoverCatLayer);
        event.registerLayerDefinition(WolfVariantCompat.TINY_TAKEOVER_WOLF_LAYER,
                WolfVariantCompat::createTinyTakeoverWolfLayer);
        event.registerLayerDefinition(WolfVariantCompat.TINY_TAKEOVER_WOLF_COLLAR_LAYER,
                WolfVariantCompat::createTinyTakeoverWolfLayer);
        event.registerLayerDefinition(WolfVariantCompat.TINY_TAKEOVER_WOLF_ARMOR_LAYER,
                WolfVariantCompat::createTinyTakeoverWolfLayer);
        event.registerLayerDefinition(OcelotVariantCompat.CLASSIC_BABY_OCELOT_LAYER,
                OcelotVariantCompat::createClassicBabyOcelotLayer);
        event.registerLayerDefinition(OcelotVariantCompat.TINY_TAKEOVER_OCELOT_LAYER,
                OcelotVariantCompat::createTinyTakeoverOcelotLayer);
        event.registerLayerDefinition(RabbitVariantCompat.TINY_TAKEOVER_RABBIT_LAYER,
                RabbitVariantCompat::createTinyTakeoverRabbitLayer);
        event.registerLayerDefinition(SheepVariantCompat.ALTERNATE_SHEEP_LAYER,
                SheepVariantCompat::createAlternateSheepLayer);
        event.registerLayerDefinition(SheepVariantCompat.ALTERNATE_SHEEP_WOOL_LAYER,
                SheepVariantCompat::createAlternateSheepWoolLayer);
        event.registerLayerDefinition(CowVariantCompat.WARM_COW_LAYER,
                CowVariantCompat::createCowLayer);
        event.registerLayerDefinition(CowVariantCompat.COLD_COW_LAYER,
                CowVariantCompat::createCowLayer);
        event.registerLayerDefinition(CowVariantCompat.BIRCH_FOREST_COW_LAYER,
                CowVariantCompat::createCowLayer);
        event.registerLayerDefinition(CowVariantCompat.BIRCH_FOREST_COW_CALF_LAYER,
                CowVariantCompat::createCowLayer);
        event.registerLayerDefinition(CowVariantCompat.BULL_COW_LAYER,
                CowVariantCompat::createCowLayer);
        event.registerLayerDefinition(CowVariantCompat.BULL_COW_CALF_LAYER,
                CowVariantCompat::createCowLayer);
        event.registerLayerDefinition(PigVariantCompat.WARM_PIG_LAYER,
                PigVariantCompat::createAdultPigLayer);
        event.registerLayerDefinition(PigVariantCompat.COLD_PIG_LAYER,
                PigVariantCompat::createAdultPigLayer);
        event.registerLayerDefinition(PigVariantCompat.PIGLET_LAYER,
                PigVariantCompat::createPigletLayer);
        event.registerLayerDefinition(PigVariantCompat.LEGEND_PIG_LAYER,
                PigVariantCompat::createAdultPigLayer);
        event.registerLayerDefinition(PigVariantCompat.MR_PIGGY_LAYER,
                PigVariantCompat::createAdultPigLayer);
        event.registerLayerDefinition(PigVariantCompat.REDCOAT_PIG_LAYER,
                PigVariantCompat::createAdultPigLayer);
        event.registerLayerDefinition(PigVariantCompat.BIRCH_FOREST_PIG_LAYER,
                PigVariantCompat::createAdultPigLayer);
        event.registerLayerDefinition(PigVariantCompat.SAVANNA_PIG_LAYER,
                PigVariantCompat::createAdultPigLayer);
        event.registerLayerDefinition(PigVariantCompat.SAVANNA_SPOTTED_PIG_LAYER,
                PigVariantCompat::createAdultPigLayer);
        event.registerLayerDefinition(PigVariantCompat.TAIGA_PIG_LAYER,
                PigVariantCompat::createAdultPigLayer);
        event.registerLayerDefinition(PigVariantCompat.WARM_PIGLET_LAYER,
                PigVariantCompat::createPigletLayer);
        event.registerLayerDefinition(PigVariantCompat.COLD_PIGLET_LAYER,
                PigVariantCompat::createPigletLayer);
        event.registerLayerDefinition(PigVariantCompat.TINY_TAKEOVER_PIGLET_LAYER,
                PigVariantCompat::createPigletLayer);
        MoveItCore.LOGGER.info("[Chicken Compat] warm_chicken layer registered.");
        MoveItCore.LOGGER.info("[Cat Compat] Classic and Tiny Takeover baby body/collar layers registered.");
        MoveItCore.LOGGER.info("[Sheep Compat] Vanilla and Alternate adult base/wool layers registered.");
        MoveItCore.LOGGER.info("[Cow Compat] named, climate, biome-breed and calf layers registered.");
        MoveItCore.LOGGER.info("[Pig Compat] named, climate, biome-breed and piglet layers registered.");
    }

    private static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        MoveItParticles.setFoxZzz(ILike2MoveItMod.foxZzz());
        event.registerSpriteSet(ILike2MoveItMod.foxZzz(), FoxZzzParticle.Provider::new);
        MoveItCore.LOGGER.info("[Fox] particle provider '{}' registered.", MoveItParticles.FOX_ZZZ_ID);
    }

    private static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        ServerBridgeState.reset();
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ServerBridgeState.reset();
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        applyWindowTitle(minecraft);
        WolfReunionTracker.clientTick(minecraft);
        FoxSleepParticleEmitter.clientTick(minecraft);
    }

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
