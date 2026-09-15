package mac.ilike2moveit.ocelot;

import net.minecraft.client.model.OcelotModel;
import net.minecraft.world.entity.animal.Ocelot;

/** Exposes the dedicated Tiny Takeover model baked by the ocelot renderer. */
public interface OcelotTinyModelAccess {
    OcelotModel<Ocelot> ilike2moveit$classicBabyOcelotModel();

    OcelotModel<Ocelot> ilike2moveit$tinyTakeoverOcelotModel();
}
