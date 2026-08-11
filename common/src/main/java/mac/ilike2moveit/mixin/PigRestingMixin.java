package mac.ilike2moveit.mixin;

import com.blackgear.vanillabackport.common.level.entities.animal.PigVariant;
import mac.ilike2moveit.pig.PigBiomeVariantSpawnAccess;
import mac.ilike2moveit.pig.PigRestGoal;
import mac.ilike2moveit.pig.PigRestingAccess;
import mac.ilike2moveit.pig.PigRestingController;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds synchronized resting state without replacing any vanilla pig data. */
@Mixin(Pig.class)
public abstract class PigRestingMixin extends Animal
        implements PigRestingAccess, PigBiomeVariantSpawnAccess {
    @Unique private static final EntityDataAccessor<Byte> IL2M_REST_STATE =
            SynchedEntityData.defineId(Pig.class, EntityDataSerializers.BYTE);
    @Unique private static final EntityDataAccessor<Long> IL2M_REST_START =
            SynchedEntityData.defineId(Pig.class, EntityDataSerializers.LONG);
    @Unique private static final EntityDataAccessor<Float> IL2M_REST_YAW =
            SynchedEntityData.defineId(Pig.class, EntityDataSerializers.FLOAT);
    @Unique private long il2m$nextRestOffer;
    @Unique private long il2m$wakeAt;
    @Unique private boolean il2m$wakeRequested;
    @Unique private PigVariant il2m$pendingBiomeBreed;
    @Unique private boolean il2m$biomeBreedResolved;

    protected PigRestingMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void il2m$defineRestingData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(IL2M_REST_STATE, PigRestingController.AWAKE);
        builder.define(IL2M_REST_START, 0L);
        builder.define(IL2M_REST_YAW, 0.0F);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void il2m$registerRestGoal(CallbackInfo ci) {
        goalSelector.addGoal(0, new PigRestGoal((Pig) (Object) this));
    }

    @Inject(method = "mobInteract", at = @At("HEAD"))
    private void il2m$wakeForInteraction(Player player, InteractionHand hand,
            CallbackInfoReturnable<?> cir) {
        if (!level().isClientSide() && il2m$getRestState() != PigRestingController.AWAKE) {
            il2m$setWakeRequested(true);
        }
    }

    /** VanillaBackport has already assigned a parent variant to this offspring at RETURN. */
    @Inject(method = "getBreedOffspring", at = @At("RETURN"), order = 1100)
    private void il2m$preserveInheritedBreed(ServerLevel level, net.minecraft.world.entity.AgeableMob mate,
            CallbackInfoReturnable<Pig> cir) {
        Pig offspring = cir.getReturnValue();
        if (offspring instanceof PigBiomeVariantSpawnAccess access) {
            access.il2m$setBiomeBreedResolved(true);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void il2m$saveResting(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("IL2MBiomeBreedResolved", il2m$biomeBreedResolved);
        tag.putByte("IL2MRestState", il2m$getRestState());
        tag.putLong("IL2MRestStart", il2m$getRestStateStart());
        tag.putFloat("IL2MRestYaw", il2m$getRestYaw());
        tag.putLong("IL2MNextRestOffer", il2m$nextRestOffer);
        tag.putLong("IL2MWakeAt", il2m$wakeAt);
        tag.putBoolean("IL2MWakeRequested", il2m$wakeRequested);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void il2m$loadResting(CompoundTag tag, CallbackInfo ci) {
        // Packs predating this marker are migrated once at entity join, then remain stable.
        il2m$biomeBreedResolved = tag.getBoolean("IL2MBiomeBreedResolved");
        if (tag.contains("IL2MRestState")) {
            il2m$setRestState(tag.getByte("IL2MRestState"));
            il2m$setRestStateStart(tag.getLong("IL2MRestStart"));
            il2m$setRestYaw(tag.getFloat("IL2MRestYaw"));
            il2m$nextRestOffer = tag.getLong("IL2MNextRestOffer");
            il2m$wakeAt = tag.getLong("IL2MWakeAt");
            il2m$wakeRequested = tag.getBoolean("IL2MWakeRequested");
        }
    }

    @Override public byte il2m$getRestState() { return entityData.get(IL2M_REST_STATE); }
    @Override public void il2m$setRestState(byte state) { entityData.set(IL2M_REST_STATE, state); }
    @Override public long il2m$getRestStateStart() { return entityData.get(IL2M_REST_START); }
    @Override public void il2m$setRestStateStart(long value) { entityData.set(IL2M_REST_START, value); }
    @Override public float il2m$getRestYaw() { return entityData.get(IL2M_REST_YAW); }
    @Override public void il2m$setRestYaw(float value) { entityData.set(IL2M_REST_YAW, value); }
    @Override public long il2m$getNextRestOffer() { return il2m$nextRestOffer; }
    @Override public void il2m$setNextRestOffer(long value) { il2m$nextRestOffer = value; }
    @Override public long il2m$getWakeAt() { return il2m$wakeAt; }
    @Override public void il2m$setWakeAt(long value) { il2m$wakeAt = value; }
    @Override public boolean il2m$isWakeRequested() { return il2m$wakeRequested; }
    @Override public void il2m$setWakeRequested(boolean value) { il2m$wakeRequested = value; }
    @Override public PigVariant il2m$getPendingBiomeBreed() { return il2m$pendingBiomeBreed; }
    @Override public void il2m$setPendingBiomeBreed(PigVariant variant) {
        il2m$pendingBiomeBreed = variant;
    }
    @Override public boolean il2m$isBiomeBreedResolved() { return il2m$biomeBreedResolved; }
    @Override public void il2m$setBiomeBreedResolved(boolean resolved) {
        il2m$biomeBreedResolved = resolved;
    }
}
