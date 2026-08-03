package mac.ilike2moveit.render;

import net.minecraft.client.renderer.block.model.ItemTransform;
import org.joml.Vector3f;

import java.util.function.Function;

/**
 * Loader-neutral access to {@code ItemTransform.rightRotation}.
 *
 * <p>That field is not vanilla: NeoForge patches it into Minecraft's own {@code ItemTransform},
 * turning the transform chain from {@code T·R·S} into {@code T·R·S·RR}. Under Fabric the field
 * does not exist, and the correct behaviour is the vanilla one.
 *
 * <p>Hence the default: identity. A loader that has the field replaces the accessor at startup;
 * one that does not, leaves it alone and gets vanilla semantics for free.
 *
 * <p>The returned vector is read-only by contract — callers must not mutate it.
 */
public final class ItemTransformCompat {
    private static final Vector3f IDENTITY = new Vector3f();

    private static Function<ItemTransform, Vector3f> rightRotation = transform -> IDENTITY;

    private ItemTransformCompat() {
    }

    /** Called by the loader entrypoint when the loader patches {@code rightRotation} in. */
    public static void setRightRotationAccessor(Function<ItemTransform, Vector3f> accessor) {
        rightRotation = accessor;
    }

    /** Degrees, never null. Identity unless a loader supplied its own accessor. */
    public static Vector3f rightRotation(ItemTransform transform) {
        return rightRotation.apply(transform);
    }
}
