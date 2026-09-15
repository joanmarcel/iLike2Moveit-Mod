package mac.ilike2moveit.wolf;

import net.minecraft.client.model.WolfModel;
import net.minecraft.world.entity.animal.Wolf;

/** Exposes the dedicated Tiny Takeover models baked by the wolf renderer. */
public interface WolfTinyModelAccess {
    WolfModel<Wolf> ilike2moveit$tinyTakeoverWolfModel();

    WolfModel<Wolf> ilike2moveit$tinyTakeoverWolfCollarModel();
}
