package mac.ilike2moveit.pig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Server-authoritative density scheduler and four-state resting machine. */
public final class PigRestingController {
    public static final byte AWAKE = PigRestingPolicy.AWAKE;
    public static final byte LIE_DOWN = PigRestingPolicy.LIE_DOWN;
    public static final byte LYING = PigRestingPolicy.LYING;
    public static final byte STAND_UP = PigRestingPolicy.STAND_UP;
    private static final double GROUP_RADIUS = 8.0;
    private static final double GROUP_RADIUS_SQR = GROUP_RADIUS * GROUP_RADIUS;
    private static final double TEMPT_RADIUS = 6.0;

    private PigRestingController() {
    }

    public static void serverTick(Pig pig) {
        if (!(pig.level() instanceof ServerLevel level) || !(pig instanceof PigRestingAccess access)) {
            return;
        }
        long now = level.getGameTime();
        byte state = access.il2m$getRestState();
        if (state < AWAKE || state > STAND_UP) {
            enter(pig, access, AWAKE, now);
            state = AWAKE;
        }

        boolean emergency = state != AWAKE && mustAbortImmediately(pig);
        if (state != AWAKE) {
            holdStill(pig, access);
            if (!emergency && shouldWakeNormally(pig, now)) {
                access.il2m$setWakeRequested(true);
            }
        }

        byte next = PigRestingPolicy.nextState(state, elapsed(access, now),
                access.il2m$isWakeRequested(),
                state == LYING && now >= access.il2m$getWakeAt(), emergency);
        if (next != state) {
            enter(pig, access, next, now);
            if (next == LYING) {
                access.il2m$setWakeAt(now + PigRestingPolicy.REST_MIN_TICKS
                        + pig.getRandom().nextInt(PigRestingPolicy.REST_JITTER_TICKS + 1));
            } else if (next == AWAKE) {
                scheduleAwakeCooldown(pig, access, now);
            }
            return;
        }
        if (state == AWAKE) {
            tickAwake(level, pig, access, now);
        }
    }

    private static void tickAwake(ServerLevel level, Pig pig, PigRestingAccess access, long now) {
        if (access.il2m$getNextRestOffer() <= 0) {
            access.il2m$setNextRestOffer(now + initialOfferDelay(pig));
            return;
        }
        if (now < access.il2m$getNextRestOffer()) {
            return;
        }
        access.il2m$setNextRestOffer(now + PigRestingPolicy.OFFER_MIN_TICKS
                + pig.getRandom().nextInt(PigRestingPolicy.OFFER_JITTER_TICKS + 1));
        if (!canStartResting(pig)) {
            return;
        }

        List<Pig> group = level.getEntitiesOfClass(Pig.class,
                pig.getBoundingBox().inflate(GROUP_RADIUS, 4.0, GROUP_RADIUS),
                other -> other.isAlive() && !other.isBaby()
                        && other.distanceToSqr(pig) <= GROUP_RADIUS_SQR);
        int target = PigRestingPolicy.targetResting(group.size());
        if (target == 0) {
            return;
        }
        int resting = 0;
        for (Pig other : group) {
            if (other instanceof PigRestingAccess otherAccess
                    && otherAccess.il2m$getRestState() != AWAKE) {
                resting++;
            }
        }
        if (PigRestingPolicy.shouldFillRestSlot(target, resting)) {
            access.il2m$setRestYaw(pig.getYRot());
            enter(pig, access, LIE_DOWN, now);
        }
    }

    private static int initialOfferDelay(Pig pig) {
        return 20 + Math.floorMod(pig.getUUID().hashCode(), 181);
    }

    private static boolean canStartResting(Pig pig) {
        // Random strolling is not a reason to leave the quota empty: enter() gives ownership to the
        // priority-0 rest goal, stops navigation and removes horizontal velocity before lie-down.
        return !pig.isBaby() && !pig.isNoAi() && pig.onGround() && !pig.isInWaterOrBubble()
                && !pig.isOnFire() && !pig.isPassenger() && !pig.isVehicle() && !pig.isLeashed()
                && !pig.isSaddled() && !pig.isInLove();
    }

    private static boolean mustAbortImmediately(Pig pig) {
        return !pig.isAlive() || pig.hurtTime > 0 || pig.isOnFire() || pig.isInWaterOrBubble()
                || !pig.onGround() || pig.isPassenger() || pig.isVehicle();
    }

    private static boolean shouldWakeNormally(Pig pig, long now) {
        if (pig.isLeashed() || pig.isSaddled() || pig.isInLove()) {
            return true;
        }
        if (Math.floorMod(pig.getId() + (int) now, 10) != 0) {
            return false;
        }
        AABB area = pig.getBoundingBox().inflate(TEMPT_RADIUS, 3.0, TEMPT_RADIUS);
        return !pig.level().getEntitiesOfClass(Player.class, area,
                player -> pig.isFood(player.getMainHandItem()) || pig.isFood(player.getOffhandItem()))
                .isEmpty();
    }

    private static long elapsed(PigRestingAccess access, long now) {
        return Math.max(0L, now - access.il2m$getRestStateStart());
    }

    private static void enter(Pig pig, PigRestingAccess access, byte state, long now) {
        access.il2m$setRestState(state);
        access.il2m$setRestStateStart(now);
        access.il2m$setWakeRequested(false);
        if (state == AWAKE) {
            access.il2m$setWakeAt(0L);
        }
        holdStill(pig, access);
    }

    private static void scheduleAwakeCooldown(Pig pig, PigRestingAccess access, long now) {
        access.il2m$setNextRestOffer(now + PigRestingPolicy.AWAKE_COOLDOWN_MIN_TICKS
                + pig.getRandom().nextInt(PigRestingPolicy.AWAKE_COOLDOWN_JITTER_TICKS + 1));
    }

    public static boolean shouldRunFullAi(Pig pig) {
        return !(pig instanceof PigRestingAccess access)
                || access.il2m$getRestState() != LYING
                || Math.floorMod(pig.tickCount + pig.getUUID().hashCode(),
                        PigRestingPolicy.FULL_AI_INTERVAL_TICKS) == 0;
    }

    public static void holdStill(Pig pig, PigRestingAccess access) {
        if (access.il2m$getRestState() == AWAKE) {
            return;
        }
        pig.getNavigation().stop();
        Vec3 movement = pig.getDeltaMovement();
        pig.setDeltaMovement(0.0, movement.y, 0.0);
        pig.stopInPlace();
        float yaw = access.il2m$getRestYaw();
        pig.setYRot(yaw);
        pig.yRotO = yaw;
        pig.setYBodyRot(yaw);
    }
}
