package mac.ilike2moveit.cat;

import net.minecraft.client.model.CatModel;
import net.minecraft.world.entity.animal.Cat;

/** Internal bridge from the CatRenderer mixin to the generic LivingEntityRenderer hook. */
public interface CatTinyModelAccess {
    CatModel<Cat> ilike2moveit$classicBabyModel();

    CatModel<Cat> ilike2moveit$tinyTakeoverModel();
}
