package mac.ilike2moveit.wolf;

/**
 * Pure per-wolf state. The clock runs on world ticks, not render frames.
 */
final class WolfReunionState {
    // Significant distance is decided outside this state; no additional time-based wait is required.
    static final long MIN_AWAY_TICKS = 0L;
    static final long RETURN_ELIGIBILITY_GRACE_TICKS = 5L * 20L;

    enum Event {
        NONE,
        ARMED_NEAR,
        AWAY_STARTED,
        AWAY_QUALIFIED,
        RETURN_WAITING_ELIGIBILITY,
        RETURN_QUEUED,
        EMF_ACKNOWLEDGED,
        SHORT_AWAY_CANCELLED
    }

    record Result(Event event, long awayTicks, int sequence) {
        static Result none() {
            return new Result(Event.NONE, 0, 0);
        }
    }

    private boolean sampled;
    private boolean near;
    private boolean armed;
    private boolean awayQualifiedReported;
    private long awaySince = -1;
    private long returnWindowUntil = -1;
    private int nextSequence = 1;
    private int pendingSequence;
    private long pendingReleaseTick = Long.MAX_VALUE;

    Result acknowledge(int emfSeenSequence) {
        if (pendingSequence > 0 && emfSeenSequence >= pendingSequence) {
            int acknowledged = pendingSequence;
            pendingSequence = 0;
            pendingReleaseTick = Long.MAX_VALUE;
            return new Result(Event.EMF_ACKNOWLEDGED, 0, acknowledged);
        }
        return Result.none();
    }

    Result observe(long now, boolean observedNear, boolean observedOutsideNear,
                   boolean observedSignificantlyFar, boolean eligible) {
        if (!sampled) {
            sampled = true;
            near = observedNear;
            if (near && eligible) {
                armed = true;
                return new Result(Event.ARMED_NEAR, 0, 0);
            }
            return Result.none();
        }

        if (observedSignificantlyFar) {
            near = false;
            return startAway(now);
        }

        // Leaving the 11-block range clears the near gate, but does NOT arm a reunion. For that the
        // player must separately go past the 100 blocks of significant distance.
        if (observedOutsideNear) {
            near = false;
            return qualifyIfNeeded(now);
        }

        if (!observedNear) {
            return qualifyIfNeeded(now);
        }

        boolean enteredNear = !near;
        near = true;

        if (awaySince >= 0) {
            long awayTicks = Math.max(0, now - awaySince);
            if (awayTicks < MIN_AWAY_TICKS) {
                awaySince = -1;
                returnWindowUntil = -1;
                awayQualifiedReported = false;
                if (eligible) {
                    armed = true;
                }
                return enteredNear
                        ? new Result(Event.SHORT_AWAY_CANCELLED, awayTicks, 0)
                        : Result.none();
            }

            if (returnWindowUntil < 0) {
                returnWindowUntil = now + RETURN_ELIGIBILITY_GRACE_TICKS;
            }
            if (eligible) {
                return queueReturn(awayTicks);
            }
            if (now <= returnWindowUntil) {
                return enteredNear
                        ? new Result(Event.RETURN_WAITING_ELIGIBILITY, awayTicks, 0)
                        : Result.none();
            }

            awaySince = -1;
            returnWindowUntil = -1;
            awayQualifiedReported = false;
            armed = false;
            return Result.none();
        }

        if (eligible && !armed) {
            armed = true;
            return new Result(Event.ARMED_NEAR, 0, 0);
        }
        return Result.none();
    }

    Result observeMissing(long now, boolean observedOutsideNear, boolean observedSignificantlyFar) {
        return observe(now, false, observedOutsideNear, observedSignificantlyFar, false);
    }

    void schedulePendingSequence(int sequence, long releaseTick) {
        if (pendingSequence != sequence || sequence <= 0) {
            throw new IllegalStateException("No se puede planificar una secuencia que no esta pendiente");
        }
        pendingReleaseTick = releaseTick;
    }

    int pendingSequence(long now) {
        return pendingSequence > 0 && now >= pendingReleaseTick ? pendingSequence : 0;
    }

    boolean isNearForGreeting() {
        return near;
    }

    Result queueDiagnosticReturn() {
        near = true;
        return queueReturn(0);
    }

    private Result startAway(long now) {
        if (armed && awaySince < 0) {
            awaySince = now;
            returnWindowUntil = -1;
            awayQualifiedReported = false;
            return new Result(Event.AWAY_STARTED, 0, 0);
        }
        return qualifyIfNeeded(now);
    }

    private Result qualifyIfNeeded(long now) {
        if (awaySince >= 0 && !awayQualifiedReported && now - awaySince >= MIN_AWAY_TICKS) {
            awayQualifiedReported = true;
            return new Result(Event.AWAY_QUALIFIED, now - awaySince, 0);
        }
        return Result.none();
    }

    private Result queueReturn(long awayTicks) {
        awaySince = -1;
        returnWindowUntil = -1;
        awayQualifiedReported = false;
        armed = true;
        if (pendingSequence == 0) {
            pendingSequence = nextSequence++;
            if (nextSequence > 16_000_000) {
                nextSequence = 1;
            }
            return new Result(Event.RETURN_QUEUED, awayTicks, pendingSequence);
        }
        return Result.none();
    }
}
