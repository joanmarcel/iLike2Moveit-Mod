package mac.ilike2moveit.wolf;

import java.util.UUID;

/** Serialises a cohort of greetings inside the overall 0.75..3.00 s window. */
final class WolfGreetingScheduler {
    static final long MIN_SEPARATION_TICKS = 15L;
    static final long MAX_SEPARATION_TICKS = 60L;
    private static final long COHORT_JOIN_TICKS = 15L;
    private static final long[] COHORT_OFFSETS = {15L, 35L, 60L};

    record Plan(long startTick, long separationTicks) {
    }

    private long lastStartTick = Long.MIN_VALUE;
    private long cohortOriginTick = Long.MIN_VALUE;
    private long lastQueuedTick = Long.MIN_VALUE;
    private int cohortSlot;

    Plan schedule(UUID uuid, int sequence, long now) {
        boolean newCohort = cohortOriginTick == Long.MIN_VALUE
                || (now - lastQueuedTick > COHORT_JOIN_TICKS && now >= lastStartTick);
        if (newCohort) {
            cohortOriginTick = now;
            cohortSlot = 0;
        }

        long target;
        if (cohortSlot < COHORT_OFFSETS.length) {
            target = cohortOriginTick + COHORT_OFFSETS[cohortSlot];
        } else {
            // More than three dogs do not fit into 2.25 s while keeping the minimum separation. We
            // keep handing out 0.75 s slots rather than letting them sync up again.
            target = lastStartTick + MIN_SEPARATION_TICKS;
        }
        long floor = lastStartTick == Long.MIN_VALUE ? now : lastStartTick + MIN_SEPARATION_TICKS;
        lastStartTick = Math.max(target, Math.max(now, floor));
        long separation = cohortSlot == 0
                ? lastStartTick - cohortOriginTick
                : lastStartTick - floor + MIN_SEPARATION_TICKS;
        cohortSlot++;
        lastQueuedTick = now;
        return new Plan(lastStartTick, separation);
    }

    void reset() {
        lastStartTick = Long.MIN_VALUE;
        cohortOriginTick = Long.MIN_VALUE;
        lastQueuedTick = Long.MIN_VALUE;
        cohortSlot = 0;
    }
}
