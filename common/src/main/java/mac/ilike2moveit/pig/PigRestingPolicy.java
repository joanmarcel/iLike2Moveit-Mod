package mac.ilike2moveit.pig;

/** Pure policy calculations kept separate so density and timing can be tested without Minecraft. */
public final class PigRestingPolicy {
    public static final byte AWAKE = 0;
    public static final byte LIE_DOWN = 1;
    public static final byte LYING = 2;
    public static final byte STAND_UP = 3;
    public static final int MIN_GROUP_SIZE = 4;
    public static final int MIN_AWAKE = 2;
    public static final double MAX_REST_FRACTION = 0.50;
    public static final int OFFER_MIN_TICKS = 100;
    public static final int OFFER_JITTER_TICKS = 100;
    public static final int AWAKE_COOLDOWN_MIN_TICKS = 400;
    public static final int AWAKE_COOLDOWN_JITTER_TICKS = 800;
    public static final int REST_MIN_TICKS = 600;
    public static final int REST_JITTER_TICKS = 1200;
    /** Visual calibration knob shared with tools/build_pig.py. */
    public static final double TRANSITION_PLAYBACK_SPEED = 1.5;
    public static final int LIE_DOWN_TICKS =
            (int) Math.ceil(3.7917 * 20.0 / TRANSITION_PLAYBACK_SPEED);
    public static final int STAND_UP_TICKS =
            (int) Math.ceil(2.0 * 20.0 / TRANSITION_PLAYBACK_SPEED);
    public static final int FULL_AI_INTERVAL_TICKS = 20;

    private PigRestingPolicy() {
    }

    public static int targetResting(int groupSize) {
        if (groupSize < MIN_GROUP_SIZE) {
            return 0;
        }
        return Math.max(0, Math.min(groupSize - MIN_AWAKE,
                (int) Math.floor(groupSize * MAX_REST_FRACTION)));
    }

    /** The staggered offer schedule chooses the pig; an open quota slot must not be discarded again. */
    public static boolean shouldFillRestSlot(int target, int alreadyResting) {
        return alreadyResting < target;
    }

    public static byte nextState(byte state, long elapsedTicks, boolean wakeRequested,
            boolean wakeDeadlineReached, boolean emergency) {
        if (emergency) {
            return AWAKE;
        }
        return switch (state) {
            case AWAKE -> AWAKE;
            case LIE_DOWN -> elapsedTicks < LIE_DOWN_TICKS
                    ? LIE_DOWN : wakeRequested ? STAND_UP : LYING;
            case LYING -> wakeRequested || wakeDeadlineReached ? STAND_UP : LYING;
            case STAND_UP -> elapsedTicks < STAND_UP_TICKS ? STAND_UP : AWAKE;
            default -> AWAKE;
        };
    }
}
