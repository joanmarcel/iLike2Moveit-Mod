package mac.ilike2moveit.wolf;

/**
 * Pure reunion thresholds. The 8..11 band keeps the proximity hysteresis, while an absence only
 * counts as significant once the wolf has really gone past 100 blocks.
 */
final class WolfReunionDistance {
    static final double NEAR_BLOCKS = 8.0;
    static final double LEAVE_NEAR_BLOCKS = 11.0;
    static final double REUNION_FAR_BLOCKS = 100.0;

    private static final double NEAR_SQUARED = NEAR_BLOCKS * NEAR_BLOCKS;
    private static final double LEAVE_NEAR_SQUARED = LEAVE_NEAR_BLOCKS * LEAVE_NEAR_BLOCKS;
    private static final double REUNION_FAR_SQUARED = REUNION_FAR_BLOCKS * REUNION_FAR_BLOCKS;

    private WolfReunionDistance() {
    }

    static boolean isNear(double distanceSquared) {
        return distanceSquared <= NEAR_SQUARED;
    }

    static boolean hasLeftNearZone(double distanceSquared) {
        return distanceSquared >= LEAVE_NEAR_SQUARED;
    }

    static boolean isSignificantAbsence(double distanceSquared) {
        return distanceSquared > REUNION_FAR_SQUARED;
    }
}
