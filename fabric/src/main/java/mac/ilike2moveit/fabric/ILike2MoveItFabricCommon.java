package mac.ilike2moveit.fabric;

import mac.ilike2moveit.ILike2MoveIt;
import mac.ilike2moveit.network.ServerBridgePayload;
import mac.ilike2moveit.cow.CowBiomeVariants;
import mac.ilike2moveit.pig.PigBiomeVariants;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Cow;

/** Physical-server entrypoint. Contains no EMF or rendering references. */
public final class ILike2MoveItFabricCommon implements ModInitializer {
    @Override
    public void onInitialize() {
        boolean vanillaBackportLoaded = FabricLoader.getInstance().isModLoaded("vanillabackport");
        if (vanillaBackportLoaded) {
            CowBiomeVariants.bootstrap();
            PigBiomeVariants.bootstrap();
        }
        PayloadTypeRegistry.playS2C().register(
                ServerBridgePayload.TYPE, ServerBridgePayload.STREAM_CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (ServerPlayNetworking.canSend(handler, ServerBridgePayload.TYPE)) {
                sender.sendPacket(ServerBridgePayload.CURRENT);
            }
        });
        if (vanillaBackportLoaded) {
            ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
                if (entity instanceof Cow cow) {
                    CowBiomeVariants.resolveNewCowBreed(cow);
                } else if (entity instanceof Pig pig) {
                    PigBiomeVariants.resolveNewPigBreed(pig);
                }
            });
        } else {
            ILike2MoveIt.LOGGER.info(
                    "[VanillaBackport Compat] mod absent; biome breed registration disabled."
            );
        }
        ILike2MoveIt.LOGGER.info("[Server Bridge] Fabric capability channel registered.");
    }
}
