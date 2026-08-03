package mac.ilike2moveit.villager;

import mac.ilike2moveit.MoveItCore;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.vehicle.Boat;
import traben.entity_model_features.EMFAnimationApi;
import traben.entity_model_features.utils.EMFEntity;

import java.util.Map;
import java.util.WeakHashMap;

/** Exposes the villager passenger's real vehicle kind to CEM. */
public final class VillagerVehicleBridge {
    public static final String EMF_IN_BOAT = "alt_villager_in_boat";
    private static final Map<AbstractVillager, Boolean> LAST_IN_BOAT = new WeakHashMap<>();

    private VillagerVehicleBridge() {
    }

    /**
     * Returns 1 only for villagers and wandering traders riding a boat (including boat variants).
     * Minecraft's generic EMF {@code is_riding} also includes minecarts, where these mobs must keep
     * their standing pose, so the resource pack cannot derive this distinction on its own.
     */
    public static Float currentInBoat() {
        EMFEntity emfEntity = EMFAnimationApi.getCurrentEntity();
        if (!(emfEntity instanceof Entity entity) || !(entity instanceof AbstractVillager villager)) {
            return 0.0F;
        }
        boolean inBoat = villager.getVehicle() instanceof Boat;
        Boolean previous = LAST_IN_BOAT.put(villager, inBoat);
        if (previous == null || previous != inBoat) {
            MoveItCore.LOGGER.info("[Villager Vehicle] boat {} uuid={}",
                    inBoat ? "IN" : "OUT", villager.getUUID());
        }
        return inBoat ? 1.0F : 0.0F;
    }
}
