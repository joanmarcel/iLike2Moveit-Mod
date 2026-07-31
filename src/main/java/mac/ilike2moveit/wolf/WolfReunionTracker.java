package mac.ilike2moveit.wolf;

import com.mojang.brigadier.Command;
import mac.ilike2moveit.ILike2MoveItMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import traben.entity_model_features.EMFAnimationApi;
import traben.entity_model_features.utils.EMFEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Detects the absence even when the model is not rendered. The choreography stays entirely in CEM.
 */
public final class WolfReunionTracker {
    public static final String EMF_RETURN_SEQUENCE = "alt_wolf_return_seq";
    public static final String EMF_GREETING_NEAR = "alt_wolf_greet_near";
    private static final String EMF_ACK_VARIABLE = "var.mod_return_seen";
    private static final long TRACE_DURATION_TICKS = 30L * 20L;
    private static final long TRACE_INTERVAL_TICKS = 5L;
    private static final List<String> TRACE_VARIABLES = List.of(
            "var.mod_return_seq", "var.return_event", "var.mod_return_seen",
            "var.greet_dbg_body_seq", "var.greet_dbg_collar_seq",
            "var.greet_dbg_layer", "var.greet_dbg_near_at_event",
            "var.greet_dbg_local_near_at_event", "var.greet_dbg_mod_near",
            "var.greet_dbg_pending_ever",
            "var.greet_dbg_drop_code", "var.greet_dbg_drop_layer",
            "var.greet_dbg_drop_dist",
            "var.greet_pending", "var.greet_wait", "var.greet_delay",
            "var.greet_release", "var.greet_start", "var.greet_active", "var.greet_done",
            "var.greet_t", "var.greet_session", "var.greet_variant",
            "var.greet_force_stand", "var.greet_root_p",
            "var.greet_idle_active", "var.greet_clip_sel", "var.greet_clip_t",
            "var.greet_look_active", "var.greet_tail_active",
            "var.rest_p", "var.sleep_p", "var.sit_p", "var.player_near",
            "var.greet_dbg_abort", "var.greet_dbg_limb_speed", "var.greet_dbg_sitting",
            "var.greet_dbg_tamed", "var.greet_dbg_aggressive", "var.greet_dbg_water"
    );

    private static final Map<UUID, WolfReunionState> STATES = new HashMap<>();
    private static final Map<UUID, Vec3> LAST_WOLF_POSITIONS = new HashMap<>();
    private static final Map<UUID, Trace> TRACES = new HashMap<>();
    private static final Set<UUID> DIAGNOSTIC_LOGGED = new HashSet<>();
    private static final WolfGreetingScheduler GREETING_SCHEDULER = new WolfGreetingScheduler();
    private static Object connectionIdentity;

    private WolfReunionTracker() {
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null || minecraft.getConnection() == null) {
            if (connectionIdentity != null) {
                STATES.clear();
                LAST_WOLF_POSITIONS.clear();
                TRACES.clear();
                DIAGNOSTIC_LOGGED.clear();
                GREETING_SCHEDULER.reset();
                connectionIdentity = null;
                ILike2MoveItMod.LOGGER.info("[Wolf Reunion] sesion cerrada; estados por UUID limpiados.");
            }
            return;
        }

        Object currentConnection = minecraft.getConnection();
        if (connectionIdentity != currentConnection) {
            STATES.clear();
            LAST_WOLF_POSITIONS.clear();
            TRACES.clear();
            DIAGNOSTIC_LOGGED.clear();
            GREETING_SCHEDULER.reset();
            connectionIdentity = currentConnection;
            ILike2MoveItMod.LOGGER.info(
                    "[Wolf Reunion] nueva sesion cliente; recibimiento 100% listo " +
                            "(ausencia >100 -> regreso <=8 bloques, salida cercana >=11, sin espera minima).");
        }

        long now = level.getGameTime();
        Set<UUID> seenThisTick = new HashSet<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof Wolf wolf)) {
                continue;
            }

            UUID uuid = wolf.getUUID();
            seenThisTick.add(uuid);
            LAST_WOLF_POSITIONS.put(uuid, wolf.position());
            WolfReunionState state = STATES.computeIfAbsent(uuid, ignored -> new WolfReunionState());
            acknowledgeEmf(wolf, state, now);

            double distanceSquared = wolf.distanceToSqr(minecraft.player);
            boolean near = WolfReunionDistance.isNear(distanceSquared);
            boolean outsideNear = WolfReunionDistance.hasLeftNearZone(distanceSquared);
            boolean significantlyFar = WolfReunionDistance.isSignificantAbsence(distanceSquared);
            // EMF's is_sitting variable uses isInSittingPose(), not isOrderedToSit(). Keep the
            // tracker gate identical to the animation expression evaluated on the client.
            boolean eligible = wolf.isTame() && wolf.isInSittingPose();
            logFirstSample(wolf, Math.sqrt(distanceSquared), near, significantlyFar);
            WolfReunionState.Result result = state.observe(
                    now, near, outsideNear, significantlyFar, eligible);
            log(uuid, Math.sqrt(distanceSquared), eligible, result);
            if (result.event() == WolfReunionState.Event.RETURN_QUEUED) {
                scheduleReturn(uuid, state, result.sequence(), now);
                startTrace(uuid, now, result.sequence(), "away-return");
            }
        }

        for (Map.Entry<UUID, WolfReunionState> entry : STATES.entrySet()) {
            if (!seenThisTick.contains(entry.getKey())) {
                Vec3 lastWolfPosition = LAST_WOLF_POSITIONS.get(entry.getKey());
                double distanceSquared = lastWolfPosition == null
                        ? 0.0
                        : minecraft.player.position().distanceToSqr(lastWolfPosition);
                boolean outsideNear = lastWolfPosition != null
                        && WolfReunionDistance.hasLeftNearZone(distanceSquared);
                boolean significantlyFar = lastWolfPosition != null
                        && WolfReunionDistance.isSignificantAbsence(distanceSquared);
                WolfReunionState.Result result = entry.getValue().observeMissing(
                        now, outsideNear, significantlyFar);
                log(entry.getKey(), lastWolfPosition == null ? Double.NaN : Math.sqrt(distanceSquared),
                        false, result);
            }
        }
    }

    /** Called by the EMF singleton variable while the current entity is being evaluated. */
    public static Float currentEmfReturnSequence() {
        EMFEntity emfEntity = EMFAnimationApi.getCurrentEntity();
        if (!(emfEntity instanceof Entity entity) || !(entity instanceof Wolf)) {
            return 0.0F;
        }
        WolfReunionState state = STATES.get(entity.getUUID());
        // If the player walks away again before the reserved slot, we neither deliver nor consume the
        // one-shot. It stays pending and resumes on the next valid approach.
        return state == null || !state.isNearForGreeting()
                ? 0.0F
                : (float) state.pendingSequence(entity.level().getGameTime());
    }

    /** Authoritative greeting gate: shares the tracker's 8/11 hysteresis exactly. */
    public static Float currentEmfGreetingNear() {
        EMFEntity emfEntity = EMFAnimationApi.getCurrentEntity();
        if (!(emfEntity instanceof Entity entity) || !(entity instanceof Wolf)) {
            return 0.0F;
        }
        WolfReunionState state = STATES.get(entity.getUUID());
        return state != null && state.isNearForGreeting() ? 1.0F : 0.0F;
    }

    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("answolf_reunion_test")
                .executes(context -> queueDiagnosticReturn()));
        ILike2MoveItMod.LOGGER.info(
                "[Wolf Reunion] comando cliente /answolf_reunion_test registrado.");
    }

    private static int queueDiagnosticReturn() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return 0;
        }

        Wolf nearest = null;
        double nearestDistanceSquared = WolfReunionDistance.NEAR_BLOCKS * WolfReunionDistance.NEAR_BLOCKS;
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof Wolf wolf && wolf.isTame() && wolf.isInSittingPose()) {
                double distanceSquared = wolf.distanceToSqr(minecraft.player);
                if (distanceSquared <= nearestDistanceSquared) {
                    nearest = wolf;
                    nearestDistanceSquared = distanceSquared;
                }
            }
        }

        if (nearest == null) {
            minecraft.player.displayClientMessage(Component.literal(
                    "[Wolf Reunion] No hay un perro domesticado y sentado a <=8 bloques."), false);
            return 0;
        }

        WolfReunionState state = STATES.computeIfAbsent(nearest.getUUID(), ignored -> new WolfReunionState());
        WolfReunionState.Result result = state.queueDiagnosticReturn();
        if (result.event() != WolfReunionState.Event.RETURN_QUEUED) {
            minecraft.player.displayClientMessage(Component.literal(
                    "[Wolf Reunion] El perro ya tiene una secuencia pendiente."), false);
            return 0;
        }

        long now = minecraft.level.getGameTime();
        scheduleReturn(nearest.getUUID(), state, result.sequence(), now);
        startTrace(nearest.getUUID(), now, result.sequence(), "client-command");
        ILike2MoveItMod.LOGGER.info(
                "[Wolf Reunion] state=RETURN_QUEUED uuid={} distance={} eligible=true awayTicks=TEST sequence={} diagnostic=true",
                nearest.getUUID(), String.format(Locale.ROOT, "%.2f", Math.sqrt(nearestDistanceSquared)),
                result.sequence());
        minecraft.player.displayClientMessage(Component.literal(
                "[Wolf Reunion] Diagnostico encolado para " + nearest.getUUID() +
                        " (sequence=" + result.sequence() + ")."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static void scheduleReturn(UUID uuid, WolfReunionState state, int sequence, long now) {
        WolfGreetingScheduler.Plan plan = GREETING_SCHEDULER.schedule(uuid, sequence, now);
        state.schedulePendingSequence(sequence, plan.startTick());
        ILike2MoveItMod.LOGGER.info(
                "[Wolf Reunion] state=RETURN_SCHEDULED uuid={} sequence={} startTick={} startIn={}s separation={}s",
                uuid, sequence, plan.startTick(), seconds(plan.startTick() - now),
                seconds(plan.separationTicks()));
    }

    private static String seconds(long ticks) {
        return String.format(Locale.ROOT, "%.2f", ticks / 20.0);
    }

    private static void acknowledgeEmf(Wolf wolf, WolfReunionState state, long now) {
        Map<String, Float> variables = EMFAnimationApi.emfEntityOf(wolf).emf$getVariableMap();
        int seenSequence = Math.round(variables.getOrDefault(EMF_ACK_VARIABLE, 0.0F));
        log(wolf.getUUID(), wolf.distanceTo(Minecraft.getInstance().player),
                wolf.isTame() && wolf.isInSittingPose(), state.acknowledge(seenSequence));
        traceEmf(wolf.getUUID(), now, variables);
    }

    private static void startTrace(UUID uuid, long now, int sequence, String reason) {
        TRACES.put(uuid, new Trace(now + TRACE_DURATION_TICKS, now, sequence));
        ILike2MoveItMod.LOGGER.info(
                "[Wolf Reunion Trace] begin uuid={} sequence={} reason={} durationTicks={}",
                uuid, sequence, reason, TRACE_DURATION_TICKS);
    }

    private static void traceEmf(UUID uuid, long now, Map<String, Float> variables) {
        Trace trace = TRACES.get(uuid);
        if (trace == null) {
            return;
        }
        if (now > trace.untilTick) {
            ILike2MoveItMod.LOGGER.info("[Wolf Reunion Trace] end uuid={} sequence={} reason=timeout",
                    uuid, trace.sequence);
            TRACES.remove(uuid);
            return;
        }
        if (now < trace.nextLogTick) {
            return;
        }
        trace.nextLogTick = now + TRACE_INTERVAL_TICKS;

        String snapshot = snapshot(variables);
        if (!snapshot.equals(trace.lastSnapshot)) {
            trace.lastSnapshot = snapshot;
            ILike2MoveItMod.LOGGER.info("[Wolf Reunion Trace] uuid={} sequence={} tick={} {}",
                    uuid, trace.sequence, now, snapshot);
        }
    }

    private static String snapshot(Map<String, Float> variables) {
        StringBuilder result = new StringBuilder();
        for (String variable : TRACE_VARIABLES) {
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(variable.substring(4)).append('=');
            Float value = variables.get(variable);
            result.append(value == null ? "NA" : String.format(Locale.ROOT, "%.3f", value));
        }
        return result.toString();
    }

    private static void logFirstSample(Wolf wolf, double distance, boolean near, boolean far) {
        if (!DIAGNOSTIC_LOGGED.add(wolf.getUUID())) {
            return;
        }
        ILike2MoveItMod.LOGGER.info(
                "[Wolf Reunion] sample uuid={} distance={} tame={} sittingPose={} orderedToSit={} near={} far={}",
                wolf.getUUID(), String.format("%.2f", distance), wolf.isTame(), wolf.isInSittingPose(),
                wolf.isOrderedToSit(), near, far);
    }

    private static void log(UUID uuid, double distance, boolean eligible, WolfReunionState.Result result) {
        if (result.event() == WolfReunionState.Event.NONE) {
            return;
        }
        String distanceText = Double.isNaN(distance) ? "unloaded" : String.format("%.2f", distance);
        ILike2MoveItMod.LOGGER.info(
                "[Wolf Reunion] state={} uuid={} distance={} eligible={} awayTicks={} sequence={}",
                result.event(), uuid, distanceText, eligible, result.awayTicks(), result.sequence());
    }

    private static final class Trace {
        private final long untilTick;
        private final int sequence;
        private long nextLogTick;
        private String lastSnapshot = "";

        private Trace(long untilTick, long nextLogTick, int sequence) {
            this.untilTick = untilTick;
            this.nextLogTick = nextLogTick;
            this.sequence = sequence;
        }
    }
}
