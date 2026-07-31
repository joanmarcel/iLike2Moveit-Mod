package mac.ilike2moveit.wolf;

import java.util.UUID;

/** Runnable without Minecraft: checks significant distance, return and scheduler. */
public final class WolfReunionStateLab {
    public static void main(String[] args) {
        expectFalse(WolfReunionDistance.isSignificantAbsence(100.0 * 100.0),
                "100 bloques exactos aun no son mas de 100");
        expectTrue(WolfReunionDistance.isSignificantAbsence(100.01 * 100.01),
                "mas de 100 bloques debe validar la ausencia");

        WolfReunionState state = new WolfReunionState();
        expect(state.observe(0, true, false, false, true), WolfReunionState.Event.ARMED_NEAR, 0);
        expectNear(state, true, "muestra inicial cercana");

        expect(state.observe(10, false, true, false, true), WolfReunionState.Event.NONE, 0);
        expectNear(state, false, "salir de 11 cancela near sin armar reencuentro");
        expect(state.observeMissing(20, true, false), WolfReunionState.Event.NONE, 0);
        expect(state.observeMissing(30, true, true), WolfReunionState.Event.AWAY_STARTED, 0);
        expect(state.observeMissing(31, true, true), WolfReunionState.Event.AWAY_QUALIFIED, 0);
        expect(state.observe(40, true, false, false, true), WolfReunionState.Event.RETURN_QUEUED, 1);
        expectNear(state, true, "regreso encolado");
        state.schedulePendingSequence(1, 55);
        expectPending(state, 54, 0, "antes del turno coordinado");
        expectPending(state, 55, 1, "en el turno coordinado");
        expect(state.acknowledge(1), WolfReunionState.Event.EMF_ACKNOWLEDGED, 1);

        expect(state.observe(60, false, true, true, true), WolfReunionState.Event.AWAY_STARTED, 0);
        expect(state.observe(61, false, true, true, true), WolfReunionState.Event.AWAY_QUALIFIED, 0);
        expect(state.observe(70, true, false, false, true), WolfReunionState.Event.RETURN_QUEUED, 2);
        expect(state.acknowledge(2), WolfReunionState.Event.EMF_ACKNOWLEDGED, 2);

        WolfReunionState diagnostic = new WolfReunionState();
        expect(diagnostic.queueDiagnosticReturn(), WolfReunionState.Event.RETURN_QUEUED, 1);
        expectNear(diagnostic, true, "comando diagnostico");

        WolfGreetingScheduler scheduler = new WolfGreetingScheduler();
        long previous = 10_000;
        for (int i = 0; i < 12; i++) {
            WolfGreetingScheduler.Plan plan = scheduler.schedule(new UUID(17L, i + 1L), 1, 10_000);
            long actualGap = plan.startTick() - previous;
            if (actualGap < WolfGreetingScheduler.MIN_SEPARATION_TICKS
                    || actualGap > WolfGreetingScheduler.MAX_SEPARATION_TICKS) {
                throw new AssertionError("Separacion fuera de 0.75..3.00 s: " + actualGap + " ticks");
            }
            if (i < 3 && plan.startTick() > 10_000 + WolfGreetingScheduler.MAX_SEPARATION_TICKS) {
                throw new AssertionError("La cohorte de tres perros salio de la ventana maxima de 3 s");
            }
            previous = plan.startTick();
        }

        System.out.println("WolfReunionStateLab OK: regresos consumidos y separacion 0.75..3.00 s garantizada");
    }

    private static void expect(WolfReunionState.Result result, WolfReunionState.Event event, int sequence) {
        if (result.event() != event || (sequence != 0 && result.sequence() != sequence)) {
            throw new AssertionError("Esperado " + event + "/" + sequence + ", recibido " + result);
        }
    }

    private static void expectNear(WolfReunionState state, boolean expected, String context) {
        if (state.isNearForGreeting() != expected) {
            throw new AssertionError("Gate near incorrecto en " + context);
        }
    }

    private static void expectPending(WolfReunionState state, long now, int expected, String context) {
        int actual = state.pendingSequence(now);
        if (actual != expected) {
            throw new AssertionError("Secuencia pendiente incorrecta " + context + ": " + actual);
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
