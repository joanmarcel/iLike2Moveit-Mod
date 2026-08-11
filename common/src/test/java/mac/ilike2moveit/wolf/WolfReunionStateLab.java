package mac.ilike2moveit.wolf;

import java.util.UUID;

/** Runnable without Minecraft: checks significant distance, return and scheduler. */
public final class WolfReunionStateLab {
    public static void main(String[] args) {
        expectFalse(WolfReunionDistance.isSignificantAbsence(100.0 * 100.0),
                "exactly 100 blocks is not more than 100");
        expectTrue(WolfReunionDistance.isSignificantAbsence(100.01 * 100.01),
                "more than 100 blocks must qualify the absence");

        WolfReunionState state = new WolfReunionState();
        expect(state.observe(0, true, false, false, true), WolfReunionState.Event.ARMED_NEAR, 0);
        expectNear(state, true, "initial nearby sample");

        expect(state.observe(10, false, true, false, true), WolfReunionState.Event.NONE, 0);
        expectNear(state, false, "leaving 11 blocks clears near without arming a reunion");
        expect(state.observeMissing(20, true, false), WolfReunionState.Event.NONE, 0);
        expect(state.observeMissing(30, true, true), WolfReunionState.Event.AWAY_STARTED, 0);
        expect(state.observeMissing(31, true, true), WolfReunionState.Event.AWAY_QUALIFIED, 0);
        expect(state.observe(40, true, false, false, true), WolfReunionState.Event.RETURN_QUEUED, 1);
        expectNear(state, true, "queued return");
        state.schedulePendingSequence(1, 55);
        expectPending(state, 54, 0, "before the coordinated turn");
        expectPending(state, 55, 1, "at the coordinated turn");
        expect(state.acknowledge(1), WolfReunionState.Event.EMF_ACKNOWLEDGED, 1);

        expect(state.observe(60, false, true, true, true), WolfReunionState.Event.AWAY_STARTED, 0);
        expect(state.observe(61, false, true, true, true), WolfReunionState.Event.AWAY_QUALIFIED, 0);
        expect(state.observe(70, true, false, false, true), WolfReunionState.Event.RETURN_QUEUED, 2);
        expect(state.acknowledge(2), WolfReunionState.Event.EMF_ACKNOWLEDGED, 2);

        WolfReunionState diagnostic = new WolfReunionState();
        expect(diagnostic.queueDiagnosticReturn(), WolfReunionState.Event.RETURN_QUEUED, 1);
        expectNear(diagnostic, true, "diagnostic command");

        WolfGreetingScheduler scheduler = new WolfGreetingScheduler();
        long previous = 10_000;
        for (int i = 0; i < 12; i++) {
            WolfGreetingScheduler.Plan plan = scheduler.schedule(new UUID(17L, i + 1L), 1, 10_000);
            long actualGap = plan.startTick() - previous;
            if (actualGap < WolfGreetingScheduler.MIN_SEPARATION_TICKS
                    || actualGap > WolfGreetingScheduler.MAX_SEPARATION_TICKS) {
                throw new AssertionError("Spacing outside 0.75..3.00 s: " + actualGap + " ticks");
            }
            if (i < 3 && plan.startTick() > 10_000 + WolfGreetingScheduler.MAX_SEPARATION_TICKS) {
                throw new AssertionError("The three-wolf cohort exceeded the maximum 3 s window");
            }
            previous = plan.startTick();
        }

        System.out.println("WolfReunionStateLab OK: returns consumed and 0.75..3.00 s spacing guaranteed");
    }

    private static void expect(WolfReunionState.Result result, WolfReunionState.Event event, int sequence) {
        if (result.event() != event || (sequence != 0 && result.sequence() != sequence)) {
            throw new AssertionError("Expected " + event + "/" + sequence + ", received " + result);
        }
    }

    private static void expectNear(WolfReunionState state, boolean expected, String context) {
        if (state.isNearForGreeting() != expected) {
            throw new AssertionError("Incorrect near gate in " + context);
        }
    }

    private static void expectPending(WolfReunionState state, long now, int expected, String context) {
        int actual = state.pendingSequence(now);
        if (actual != expected) {
            throw new AssertionError("Incorrect pending sequence " + context + ": " + actual);
        }
    }

    private static void expectTrue(boolean actual, String context) {
        if (!actual) {
            throw new AssertionError(context);
        }
    }

    private static void expectFalse(boolean actual, String context) {
        expectTrue(!actual, context);
    }
}
