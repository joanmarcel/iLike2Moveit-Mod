package mac.ilike2moveit.pig;

import mac.ilike2moveit.network.ServerBridgeState;

/** Deterministic policy contract; run with assertions enabled. */
public final class PigRestingPolicyLab {
    private PigRestingPolicyLab() {
    }

    public static void main(String[] args) {
        assert PigRestingPolicy.targetResting(0) == 0;
        assert PigRestingPolicy.targetResting(3) == 0;
        assert PigRestingPolicy.targetResting(4) == 2;
        assert PigRestingPolicy.targetResting(5) == 2;
        assert PigRestingPolicy.targetResting(10) == 5;
        assert PigRestingPolicy.targetResting(101) == 50;
        assert !PigRestingPolicy.shouldFillRestSlot(0, 0);
        assert !PigRestingPolicy.shouldFillRestSlot(6, 6);
        assert PigRestingPolicy.shouldFillRestSlot(6, 5);
        assert PigRestingPolicy.shouldFillRestSlot(6, 0);
        int sequentialResting = 0;
        while (PigRestingPolicy.shouldFillRestSlot(
                PigRestingPolicy.targetResting(10), sequentialResting)) {
            sequentialResting++;
        }
        assert sequentialResting == 5;
        assert PigRestingPolicy.LIE_DOWN_TICKS == 51;
        assert PigRestingPolicy.STAND_UP_TICKS == 27;
        assert PigRestingPolicy.nextState(PigRestingPolicy.LIE_DOWN, 50,
                true, false, false) == PigRestingPolicy.LIE_DOWN;
        assert PigRestingPolicy.nextState(PigRestingPolicy.LIE_DOWN, 51,
                false, false, false) == PigRestingPolicy.LYING;
        assert PigRestingPolicy.nextState(PigRestingPolicy.LIE_DOWN, 51,
                true, false, false) == PigRestingPolicy.STAND_UP;
        assert PigRestingPolicy.nextState(PigRestingPolicy.LYING, 900,
                false, true, false) == PigRestingPolicy.STAND_UP;
        assert PigRestingPolicy.nextState(PigRestingPolicy.STAND_UP, 26,
                false, false, false) == PigRestingPolicy.STAND_UP;
        assert PigRestingPolicy.nextState(PigRestingPolicy.STAND_UP, 27,
                false, false, false) == PigRestingPolicy.AWAKE;
        assert PigRestingPolicy.nextState(PigRestingPolicy.LYING, 1,
                false, false, true) == PigRestingPolicy.AWAKE;
        ServerBridgeState.reset();
        assert !ServerBridgeState.isPresent();
        assert ServerBridgeState.currentEmfPresent() == 0.0F;
        ServerBridgeState.acceptProtocol(ServerBridgeState.PROTOCOL);
        assert ServerBridgeState.isPresent();
        assert ServerBridgeState.currentEmfPresent() == 1.0F;
        ServerBridgeState.acceptProtocol(ServerBridgeState.PROTOCOL + 1);
        assert !ServerBridgeState.isPresent();
        assert ServerBridgeState.currentEmfPresent() == 0.0F;
        System.out.println("OK PigRestingPolicyLab");
    }
}
