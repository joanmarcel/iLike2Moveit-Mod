package mac.ilike2moveit.neoforge;

import mac.ilike2moveit.ILike2MoveIt;
import mac.ilike2moveit.network.ServerBridgePayload;
import mac.ilike2moveit.network.ServerBridgeState;
import mac.ilike2moveit.cow.CowBiomeVariants;
import mac.ilike2moveit.pig.PigBiomeVariants;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Cow;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Common NeoForge entrypoint; client wiring is loaded only on the physical client. */
@Mod(ILike2MoveIt.MODID)
public final class ILike2MoveItMod {
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, ILike2MoveIt.MODID);
    private static final DeferredHolder<ParticleType<?>, SimpleParticleType> FOX_ZZZ =
            PARTICLE_TYPES.register("fox_zzz", () -> new SimpleParticleType(false));

    public ILike2MoveItMod(IEventBus modEventBus) {
        ILike2MoveIt.LOGGER.info("[iLike2MoveIt] common bridge loaded (neoforge).");
        CowBiomeVariants.bootstrap();
        PigBiomeVariants.bootstrap();
        modEventBus.addListener(ILike2MoveItMod::registerPayloads);
        NeoForge.EVENT_BUS.addListener(ILike2MoveItMod::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(ILike2MoveItMod::onEntityJoinLevel);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            PARTICLE_TYPES.register(modEventBus);
            ILike2MoveItNeoForgeClient.bootstrap(modEventBus);
        }
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(Integer.toString(ServerBridgePayload.PROTOCOL)).optional().playToClient(
                ServerBridgePayload.TYPE,
                ServerBridgePayload.STREAM_CODEC,
                (payload, context) -> ServerBridgeState.accept(payload));
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && NetworkRegistry.hasChannel(player.connection,
                        ServerBridgePayload.TYPE.id())) {
            PacketDistributor.sendToPlayer(player, ServerBridgePayload.CURRENT);
        }
    }

    private static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            if (event.getEntity() instanceof Cow cow) {
                CowBiomeVariants.resolveNewCowBreed(cow);
            } else if (event.getEntity() instanceof Pig pig) {
                PigBiomeVariants.resolveNewPigBreed(pig);
            }
        }
    }

    static SimpleParticleType foxZzz() {
        return FOX_ZZZ.get();
    }
}
