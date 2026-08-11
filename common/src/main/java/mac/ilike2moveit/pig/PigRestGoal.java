package mac.ilike2moveit.pig;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Pig;

import java.util.EnumSet;

/** Owns movement/look controls while the server resting machine is active. */
public final class PigRestGoal extends Goal {
    private final Pig pig;

    public PigRestGoal(Pig pig) {
        this.pig = pig;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return isResting();
    }

    @Override
    public boolean canContinueToUse() {
        return isResting();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        hold();
    }

    @Override
    public void tick() {
        hold();
    }

    private boolean isResting() {
        return pig instanceof PigRestingAccess access
                && access.il2m$getRestState() != PigRestingController.AWAKE;
    }

    private void hold() {
        if (pig instanceof PigRestingAccess access) {
            PigRestingController.holdStill(pig, access);
        }
    }
}
