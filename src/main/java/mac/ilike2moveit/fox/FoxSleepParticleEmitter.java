package mac.ilike2moveit.fox;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Fox;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Emits the "Zzz" particle ({@link MoveItParticles#FOX_ZZZ}) above the head of every sleeping fox,
 * replicating the Bedrock emitter it was ported from (spawn_rate 2, max 5, while
 * {@code q.is_sleeping}).
 *
 * <p>Same pattern as {@code WolfReunionTracker::onClientTick}: it registers on
 * {@code NeoForge.EVENT_BUS} and walks the {@link ClientLevel} entities on every client tick. For
 * each fox with {@link Fox#isSleeping()} it emits in BURSTS, replicating the original
 * {@code emitter_lifetime_looping} (2.74 s active -&gt; 5 s asleep, looping): a batch of 5 "Zzz" at 2/s
 * (the emitter's {@code max_particles}) and then ~5 s of silence while the previous ones fade out,
 * then round again. Client-side only; touches neither the server nor cat/wolf.
 */
public final class FoxSleepParticleEmitter {
    private static final long SPAWN_INTERVAL_TICKS = 10L; // 20 / 10 = 2 particles per second (spawn_rate 2)
    private static final long CYCLE_TICKS = 155L;         // emitter_lifetime_looping: 2.74 s active + 5 s asleep
    private static final long ACTIVE_SPAWN_TICKS = 45L;   // emission window -> 5 spawns (0,10,20,30,40) = max_particles 5
    private static final double HEAD_FORWARD = 0.35;      // offset towards the head
    private static final double HEAD_RISE = 0.25;         // the parametric movement adds another +0.5

    private FoxSleepParticleEmitter() {
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null || minecraft.getConnection() == null
                || minecraft.isPaused()) {
            return;
        }

        long now = level.getGameTime();
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof Fox fox) || !fox.isSleeping()) {
                continue;
            }
            // Desynchronises the burst cycle per fox (each one starts its batch at a different time).
            long phase = Math.floorMod((long) fox.getUUID().hashCode(), CYCLE_TICKS);
            long cyclePos = Math.floorMod(now + phase, CYCLE_TICKS);
            // Only emits during the active window (burst) and at 2/s; silence for the rest of the cycle.
            if (cyclePos >= ACTIVE_SPAWN_TICKS || cyclePos % SPAWN_INTERVAL_TICKS != 0L) {
                continue;
            }
            spawnZzz(level, fox);
        }
    }

    private static void spawnZzz(ClientLevel level, Fox fox) {
        // Entity's forward vector (towards the head): (-sin(yaw), cos(yaw)).
        double yawRadians = Math.toRadians(fox.yBodyRot);
        double headX = fox.getX() - Math.sin(yawRadians) * HEAD_FORWARD;
        double headZ = fox.getZ() + Math.cos(yawRadians) * HEAD_FORWARD;
        double headY = fox.getY() + HEAD_RISE;
        level.addParticle(MoveItParticles.FOX_ZZZ.get(), headX, headY, headZ, 0.0, 0.0, 0.0);
    }
}
