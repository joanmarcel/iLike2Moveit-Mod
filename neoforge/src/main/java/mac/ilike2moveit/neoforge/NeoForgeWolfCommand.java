package mac.ilike2moveit.neoforge;

import mac.ilike2moveit.MoveItCore;
import mac.ilike2moveit.wolf.WolfReunionTracker;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/**
 * Brigadier tree of the diagnostic command, on NeoForge's CommandSourceStack.
 *
 * <p>Duplicated per loader on purpose: Fabric registers client commands on
 * FabricClientCommandSource, an incompatible type. The behaviour behind it lives in common.
 */
public final class NeoForgeWolfCommand {
    private NeoForgeWolfCommand() {
    }

    public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("answolf_reunion_test")
                .executes(context -> WolfReunionTracker.queueDiagnosticReturn()));
        MoveItCore.LOGGER.info("[Wolf Reunion] client command /answolf_reunion_test registered.");
    }
}
