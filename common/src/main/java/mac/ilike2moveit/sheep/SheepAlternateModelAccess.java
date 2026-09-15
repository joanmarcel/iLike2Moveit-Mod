package mac.ilike2moveit.sheep;

import net.minecraft.client.model.SheepModel;
import net.minecraft.world.entity.animal.Sheep;

/** Exposes the selectable adult models baked by the sheep renderer. */
public interface SheepAlternateModelAccess {
    SheepModel<Sheep> ilike2moveit$alternateSheepModel();
}
