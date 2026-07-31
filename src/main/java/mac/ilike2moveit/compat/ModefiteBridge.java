package mac.ilike2moveit.compat;

import mac.ilike2moveit.ILike2MoveItMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Bridge to <b>Modefite — Item Definition Backport</b> so we can MEASURE the same model the game is
 * going to DRAW.
 *
 * <p><b>The problem it solves.</b> Modefite backports the 1.21.4/1.21.5 item definition system
 * ({@code assets/minecraft/items/*.json}) to 1.21.1, which is why packs like Weskersons3DItems — which
 * declare {@code pack_format 46} — DO load their 3D meshes {@code models/item/*_hand.json}. Its mixin
 * on {@code ItemRenderer} does two asymmetric things:
 * <ul>
 *   <li>in {@code getModel(stack, level, entity, seed)} it always returns the resolution with a
 *       <b>hardcoded GUI context</b> (the flat sprite);</li>
 *   <li>in {@code renderStatic(...)} it resolves again with the <b>real context</b>
 *       (THIRD_PERSON_RIGHT_HAND) and draws the {@code *_hand.json}.</li>
 * </ul>
 * That is: asking for the model the normal way returns a different geometry from the one being
 * painted, and the difference is <b>per item</b> (each {@code *_hand.json} carries its own
 * {@code display} block and its own mesh). Any computation done on the wrong model — AABB,
 * centre/pivot, harmonised scale — ends up misplaced differently for each item: with the same
 * calibration the ingot landed in the mouth, the diamond at the tip of the snout and the berries
 * halfway in front of the fox.
 *
 * <p><b>How it solves it.</b> We locate by <b>signature</b> (not by name: Mixin may rename the members
 * it merges) the static method Modefite injects into {@code ItemRenderer} with descriptor
 * {@code (ItemStack, LivingEntity, ItemDisplayContext) -> BakedModel} and ask it for the model with
 * the real context. If Modefite is absent — or changes its signature in an update — it degrades to
 * vanilla {@code getModel}, which without the mod already returns the right thing.
 *
 * <p>Everything is cached in statics and wrapped in {@code try/catch}: this bridge can never break the
 * render.
 */
public final class ModefiteBridge {

    private static final String MIXIN_MARKER =
            "timmychips.modefiteitemdefinitions.property.resolver.ItemModelResolver";
    private static final String COMPOSITE_CLASS =
            "timmychips.modefiteitemdefinitions.bakedmodels.CompositeItemModel";

    private static boolean initialised;
    private static Method resolveDrawn;      // (ItemStack, LivingEntity, ItemDisplayContext) -> BakedModel
    private static Class<?> compositeClass;
    private static Method compositeGetModels;

    private ModefiteBridge() {
    }

    /** {@code true} if Modefite's resolver was located (for the diagnostic log only). */
    public static boolean isActive() {
        ensureInitialised();
        return resolveDrawn != null;
    }

    /**
     * The model the game will actually draw for this item in this context.
     *
     * @param entity holder (may be {@code null}); affects selectors such as {@code using_item}
     * @return never {@code null}: if nothing resolves, the vanilla model
     */
    public static BakedModel drawnModel(ItemStack stack, LivingEntity entity, ItemDisplayContext context) {
        ensureInitialised();
        if (resolveDrawn != null) {
            try {
                Object model = resolveDrawn.invoke(null, stack, entity, context);
                if (model instanceof BakedModel baked) {
                    return baked;
                }
            } catch (Throwable t) {
                // An item with no definition returns null by design; any other failure falls to vanilla.
            }
        }
        Minecraft mc = Minecraft.getInstance();
        // The seed is the same one ItemInHandRenderer.renderItem uses, so overrides that depend on it
        // pick the same variant that will be drawn.
        int seed = (entity == null ? 0 : entity.getId()) + context.ordinal();
        return mc.getItemRenderer().getModel(stack, entity == null ? null : entity.level(), entity, seed);
    }

    /**
     * Sub-models of a Modefite composite model, or {@code null} if it is not one.
     *
     * <p>It matters for measuring: Modefite draws each part with ITS own {@code ItemTransform}, while
     * {@code CompositeItemModel.getTransforms()} only returns the first one's. Measuring the union of
     * quads and applying a single transform to it would give a false box as soon as the parts differ.
     */
    public static List<BakedModel> compositeParts(BakedModel model) {
        ensureInitialised();
        if (compositeClass == null || compositeGetModels == null || !compositeClass.isInstance(model)) {
            return null;
        }
        try {
            Object parts = compositeGetModels.invoke(model);
            return parts instanceof List<?> list ? castParts(list) : null;
        } catch (Throwable t) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<BakedModel> castParts(List<?> list) {
        return (List<BakedModel>) list;
    }

    private static void ensureInitialised() {
        if (initialised) {
            return;
        }
        initialised = true;
        boolean modefitePresent = classExists(MIXIN_MARKER);

        // Lookup BY DESCRIPTOR, not by name: Mixin renames the private members it merges when there is
        // a collision, so "getCustomModel" is not guaranteed. That name is preferred if it shows up.
        for (Method method : ItemRenderer.class.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers())
                    || method.getReturnType() != BakedModel.class) {
                continue;
            }
            Class<?>[] p = method.getParameterTypes();
            if (p.length != 3
                    || p[0] != ItemStack.class
                    || p[1] != LivingEntity.class
                    || p[2] != ItemDisplayContext.class) {
                continue;
            }
            try {
                method.setAccessible(true);
                resolveDrawn = method;
                if (method.getName().contains("getCustomModel")) {
                    break;   // exact match: stop looking
                }
            } catch (Throwable ignored) {
                // no reflective access: keep looking for another candidate
            }
        }

        compositeClass = forName(COMPOSITE_CLASS);
        if (compositeClass != null) {
            try {
                compositeGetModels = compositeClass.getMethod("getModels");
                compositeGetModels.setAccessible(true);
            } catch (Throwable ignored) {
                compositeGetModels = null;
            }
        }

        if (modefitePresent && resolveDrawn == null) {
            // Dangerous and silent situation: the mod is there, it replaces the model when drawing, and
            // we failed to ask it -> we would go back to measuring the flat sprite. Make it visible.
            ILike2MoveItMod.LOGGER.warn(
                    "[ModefiteBridge] Modefite is installed but its model resolver was not found in"
                    + " ItemRenderer (did the signature change?). The held item will be measured against"
                    + " the vanilla model and may end up misplaced.");
        } else if (resolveDrawn != null) {
            ILike2MoveItMod.LOGGER.info("[ModefiteBridge] resolver located: {}", resolveDrawn.getName());
        }
    }

    private static boolean classExists(String name) {
        return forName(name) != null;
    }

    private static Class<?> forName(String name) {
        try {
            return Class.forName(name, false, ModefiteBridge.class.getClassLoader());
        } catch (Throwable t) {
            return null;
        }
    }
}
