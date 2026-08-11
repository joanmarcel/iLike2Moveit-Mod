package mac.ilike2moveit.pig;

/** State added to every pig by the common mixin and synchronized by vanilla entity data. */
public interface PigRestingAccess {
    byte il2m$getRestState();

    void il2m$setRestState(byte state);

    long il2m$getRestStateStart();

    void il2m$setRestStateStart(long gameTime);

    float il2m$getRestYaw();

    void il2m$setRestYaw(float yaw);

    long il2m$getNextRestOffer();

    void il2m$setNextRestOffer(long gameTime);

    long il2m$getWakeAt();

    void il2m$setWakeAt(long gameTime);

    boolean il2m$isWakeRequested();

    void il2m$setWakeRequested(boolean requested);
}
