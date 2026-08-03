package mac.ilike2moveit.fabric;

import mac.ilike2moveit.MoveItCore;
import mac.ilike2moveit.wolf.WolfReunionTracker;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

/**
 * Brigadier tree of the diagnostic command, on Fabric's FabricClientCommandSource.
 *
 * <p>Duplicated per loader on purpose: NeoForge registers client commands on CommandSourceStack, an
 * incompatible type. The behaviour behind it lives in common.
 */
public final class FabricWolfCommand {
    private FabricWolfCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("answolf_reunion_test")
                        .executes(context -> WolfReunionTracker.queueDiagnosticReturn())));
        MoveItCore.LOGGER.info("[Wolf Reunion] client command /answolf_reunion_test registered.");
    }
}
