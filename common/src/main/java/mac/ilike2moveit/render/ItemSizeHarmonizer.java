package mac.ilike2moveit.render;

import mac.ilike2moveit.compat.ModefiteBridge;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Measures where and how much an item occupies ONCE DRAWN, so it can be hung off an animated bone
 * (the fox's mouth, the villager's hands) with homogeneous seating and apparent size.
 *
 * <h2>The space it measures in</h2>
 * Vanilla does not draw the item mesh where you leave it: {@code ItemRenderer.render} first applies
 * the display context's {@link ItemTransform} and then a {@code translate(-0.5)}. That is why
 * EVERYTHING here is computed in <b>post-transform space</b>, replicating {@code ItemTransform.apply}'s
 * chain exactly:
 * <pre>
 *   F(q) = translation + R(rotation) · S(scale) · R(rightRotation) · (q − ½)
 * </pre>
 * (with {@code leftHand} negating {@code translation.x}, {@code rotation.y/z} and
 * {@code rightRotation.y/z}, just like vanilla). {@code rightRotation} is supplied by the loader
 * through {@link ItemTransformCompat}: NeoForge patches the field into vanilla's
 * {@code ItemTransform} and the chain really is {@code T·R·S·RR}; where no loader supplies it —
 * vanilla, and therefore Fabric — the accessor yields the identity and the chain collapses to
 * {@code T·R·S}, which is exactly what that loader draws. The model box's <b>eight corners</b> are transformed
 * and the AABB of the result is taken. Transforming the centre and transforming the corners only
 * agree when the display rotation is the identity; as soon as it rotates — and it does:
 * {@code iron_ingot_hand} carries {@code [0,-90,0]}, tools {@code [0,-90,55]} — the final silhouette
 * shifts away from the image of the centre. What the player wants ("that it sticks out equally on
 * both sides of the grip point") is the centre of the final silhouette, so that AABB is what rules.
 *
 * <p>Two quantities come out of it:
 * <ul>
 *   <li>{@link #visualCenter} — that box's centre. Subtracting it before drawing keeps the item
 *       centred ALWAYS on the same point, whatever its shape, and makes rotating it spin in place
 *       instead of sending it off on a trip.</li>
 *   <li>{@link #harmonyScale} — of that box's three dimensions the TWO LARGEST are taken
 *       {@code a >= b}, along with their geometric mean {@code m = sqrt(a*b)}: the side of the square
 *       with the same silhouette area. Unlike the maximum dimension, it does not inflate thin,
 *       elongated objects (an ingot still looks like an ingot). The final scale is
 *       {@code s = (m_ref/m)^k}, clamped to [{@value #MIN_SCALE}, {@value #MAX_SCALE}]; {@code k}
 *       grades the harmonisation: 1 = all with the same apparent silhouette, 0 = untouched.
 *       {@code m_ref} is measured LIVE from the iron ingot in the same context, so the result
 *       self-calibrates to whatever pack the player has installed.</li>
 * </ul>
 *
 * <h2>Two traps to dodge in order to measure the right geometry</h2>
 * <ol>
 *   <li><b>The model being drawn is not the one {@code getModel} returns.</b> With Modefite installed
 *       it has to be requested from it; {@link ModefiteBridge} takes care of that. Composite models are
 *       measured <b>part by part</b>, because each one carries its own {@code ItemTransform} and the
 *       composite's {@code getTransforms()} only returns the first one's.</li>
 *   <li><b>A "generated" item's mesh always occupies the whole 16×16 canvas</b> even when the drawing
 *       only paints one corner, so measuring the raw mesh gave exactly the same number for the ingot
 *       as for the diamond. In those cases — and only those, see {@link #narrowToOpaquePixels} — it is
 *       cropped to the opaque pixel footprint, which is what the eye actually sees.</li>
 * </ol>
 *
 * <p>Everything is cached per {@link BakedModel} instance (reloading resources bakes new models, so
 * the cache renews itself) and wrapped in {@code try/catch}: any exotic model degrades to "touch
 * nothing" instead of breaking the render.
 */
public final class ItemSizeHarmonizer {

    /** Maximum thickness (in blocks) for an item to count as a 2D sheet. 2 pixels. */
    private static final float FLAT_MAX_DEPTH = 0.125F;

    /** Safety bounds: no item shrinks or inflates beyond this. */
    private static final float MIN_SCALE = 0.45F;
    private static final float MAX_SCALE = 1.80F;

    /** The "could not measure" value. */
    private static final float NO_MEASURE = -1.0F;

    private static final int CACHE_LIMIT = 4096;
    private static final RandomSource RANDOM = RandomSource.create(42L);

    /** A model's measured shape, in model space. A null {@code box} = unmeasurable (BEWLR). */
    private record Shape(float[] box) {
        static final Shape UNMEASURABLE = new Shape(null);
    }

    /** Only touched from the render thread, hence an unsynchronised IdentityHashMap is enough. */
    private static final Map<BakedModel, Shape> SHAPE_CACHE = new IdentityHashMap<>();

    private static ItemDisplayContext refContext;
    private static float refMeasure = NO_MEASURE;
    private static long refStamp;

    private ItemSizeHarmonizer() {
    }

    /**
     * Scale to apply to the item so that its apparent size matches the iron ingot's.
     *
     * @param exponent harmonisation degree {@code k}: 1 = full, 0 = disabled
     * @return multiplicative factor; {@code 1.0} if it cannot be measured (never throws)
     */
    public static float harmonyScale(BakedModel model, ItemDisplayContext context, boolean leftHand,
                                     float exponent) {
        if (model == null || exponent <= 0.0F) {
            return 1.0F;
        }
        try {
            float m = measure(model, context, leftHand);
            float ref = referenceMeasure(context, leftHand);
            if (m <= 1.0E-4F || ref <= 1.0E-4F) {
                return 1.0F;
            }
            return Mth.clamp((float) Math.pow(ref / m, exponent), MIN_SCALE, MAX_SCALE);
        } catch (Throwable t) {
            lastError = t.toString();
            return 1.0F;
        }
    }

    /**
     * Point where the item's VISUAL CENTRE ends up relative to the origin of the {@code PoseStack}
     * that {@code renderItem} will be called with — that is, the offset vanilla introduces on its own
     * ({@code ItemTransform.apply} plus the {@code translate(-0.5)} that centres the mesh).
     *
     * <p>Subtracting it before drawing seats the item at the origin: a single set of calibration
     * coordinates works for every item, and rotating them spins them in place.
     *
     * @return the visual centre's offset; {@code (0,0,0)} if it cannot be measured (never throws)
     */
    public static Vector3f visualCenter(BakedModel model, ItemDisplayContext context, boolean leftHand) {
        try {
            float[] box = transformedBox(model, context, leftHand);
            if (box == null) {
                // With no measurable geometry (BEWLR: shield, trident, chest...) the best possible
                // seating is the image of the canvas centre, which is exactly the display translation.
                ItemTransform t = model.getTransforms().getTransform(context);
                return new Vector3f((leftHand ? -1.0F : 1.0F) * t.translation.x(),
                                    t.translation.y(), t.translation.z());
            }
            return new Vector3f((box[0] + box[3]) * 0.5F,
                                (box[1] + box[4]) * 0.5F,
                                (box[2] + box[5]) * 0.5F);
        } catch (Throwable t) {
            lastError = t.toString();
            return new Vector3f();
        }
    }

    /** Temporary DEBUG: the model's perceptual measure ({@code -1} if it could not be measured). */
    public static float measureOf(BakedModel model, ItemDisplayContext context, boolean leftHand) {
        try {
            return measure(model, context, leftHand);
        } catch (Throwable t) {
            lastError = t.toString();
            return NO_MEASURE;
        }
    }

    /** Temporary DEBUG: measure of the iron ingot used as the reference. */
    public static float referenceOf(ItemDisplayContext context, boolean leftHand) {
        try {
            return referenceMeasure(context, leftHand);
        } catch (Throwable t) {
            lastError = t.toString();
            return NO_MEASURE;
        }
    }

    /** Temporary DEBUG: last swallowed exception, or {@code "-"} if none. */
    public static String lastError() {
        return lastError == null ? "-" : lastError;
    }

    private static volatile String lastError;

    /**
     * Is the drawn model a flat SHEET (an extruded sprite, with no real volume) as opposed to a mesh
     * with body (the {@code *_hand.json} of a 3D pack)?
     *
     * <p>It is measured in MODEL space (without the context's {@link ItemTransform}), which is where
     * the distinction is stable: a generated item extrudes its texture to 1px of thickness; a 3D mesh
     * has real depth. With Modefite installed the model arriving here is already the one actually
     * drawn, so the classification is reliable (it was not, back when the flat sprite was measured for
     * everything). Composite models are flat only if ALL their parts are.
     *
     * @return {@code true} if flat; {@code false} if it has volume or could not be measured
     */
    public static boolean isFlat(BakedModel model) {
        try {
            if (model == null) {
                return false;
            }
            List<BakedModel> parts = ModefiteBridge.compositeParts(model);
            if (parts != null && !parts.isEmpty()) {
                for (BakedModel part : parts) {
                    if (!partIsFlat(part)) {
                        return false;
                    }
                }
                return true;
            }
            return partIsFlat(model);
        } catch (Throwable t) {
            lastError = t.toString();
            return false;
        }
    }

    private static boolean partIsFlat(BakedModel model) {
        float[] box = shape(model).box();
        if (box == null) {
            return false;
        }
        float depth = Math.min(box[3] - box[0], Math.min(box[4] - box[1], box[5] - box[2]));
        return depth <= FLAT_MAX_DEPTH;
    }

    /**
     * Measure of the iron ingot. Refreshed once per second rather than per frame: the reference
     * {@code ItemStack} has to be instantiated in order to resolve the model, and it is not worth
     * doing that sixty times a second.
     */
    private static float referenceMeasure(ItemDisplayContext context, boolean leftHand) {
        long now = System.currentTimeMillis();
        if (context != refContext || refMeasure == NO_MEASURE || now - refStamp > 1000L) {
            refContext = context;
            refStamp = now;
            refMeasure = measure(ModefiteBridge.drawnModel(new ItemStack(Items.IRON_INGOT), null, context),
                                 context, leftHand);
        }
        return refMeasure;
    }

    /** {@code sqrt(a*b)}, with {@code a >= b} the two largest dimensions of the post-transform box. */
    private static float measure(BakedModel model, ItemDisplayContext context, boolean leftHand) {
        float[] box = transformedBox(model, context, leftHand);
        if (box == null) {
            return NO_MEASURE;
        }
        float x = box[3] - box[0];
        float y = box[4] - box[1];
        float z = box[5] - box[2];
        // The SMALLEST of the three is discarded: the silhouette is defined by the other two. Counting
        // the near-zero thickness of a flat item would collapse the geometric mean to zero.
        float min = Math.min(x, Math.min(y, z));
        float a;
        float b;
        if (min == x) {
            a = y;
            b = z;
        } else if (min == y) {
            a = x;
            b = z;
        } else {
            a = x;
            b = y;
        }
        return (float) Math.sqrt(a * b);
    }

    /**
     * AABB of the item ALREADY transformed by its {@link ItemTransform}, that is, in the same space it
     * will end up drawn in.
     *
     * <p>A composite model is walked part by part because each one is drawn with its own transform;
     * the resulting box is their union.
     *
     * @return {@code {minX,minY,minZ,maxX,maxY,maxZ}}, or {@code null} if nothing was measurable
     */
    private static float[] transformedBox(BakedModel model, ItemDisplayContext context, boolean leftHand) {
        if (model == null) {
            return null;
        }
        float[] out = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE,
                -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        List<BakedModel> parts = ModefiteBridge.compositeParts(model);
        boolean any = false;
        if (parts != null && !parts.isEmpty()) {
            for (BakedModel part : parts) {
                any |= accumulateTransformed(part, context, leftHand, out);
            }
        } else {
            any = accumulateTransformed(model, context, leftHand, out);
        }
        return any ? out : null;
    }

    private static boolean accumulateTransformed(BakedModel model, ItemDisplayContext context,
                                                 boolean leftHand, float[] out) {
        if (model == null) {
            return false;
        }
        float[] box = shape(model).box();
        if (box == null) {
            return false;
        }
        ItemTransform transform = model.getTransforms().getTransform(context);
        Vector3f corner = new Vector3f();
        for (int i = 0; i < 8; i++) {
            corner.set((i & 1) == 0 ? box[0] : box[3],
                       (i & 2) == 0 ? box[1] : box[4],
                       (i & 4) == 0 ? box[2] : box[5]);
            applyItemTransform(corner, transform, leftHand);
            for (int axis = 0; axis < 3; axis++) {
                float value = corner.get(axis);
                if (value < out[axis]) {
                    out[axis] = value;
                }
                if (value > out[axis + 3]) {
                    out[axis + 3] = value;
                }
            }
        }
        return true;
    }

    /**
     * Exact replica of {@code ItemTransform.apply} on a point, including the {@code translate(-0.5)}
     * that {@code ItemRenderer} applies right afterwards.
     *
     * <p>The matrix chain is {@code T · R · S · RR}, so they are applied to the point in reverse
     * order: {@code rightRotation} first, then the scale, then the rotation and finally the
     * translation. With {@code leftHand}, vanilla negates {@code translation.x} and the Y/Z
     * components of both rotations.
     *
     * <p>{@code RR} comes from {@link ItemTransformCompat} because it is not a vanilla field —
     * NeoForge patches it in. Under a loader that does not have it the accessor returns the
     * identity, the first quaternion is a no-op and the chain is the vanilla {@code T · R · S}.
     */
    private static void applyItemTransform(Vector3f point, ItemTransform transform, boolean leftHand) {
        float mirror = leftHand ? -1.0F : 1.0F;
        point.sub(0.5F, 0.5F, 0.5F);
        degreesToQuaternion(ItemTransformCompat.rightRotation(transform), mirror).transform(point);
        point.mul(transform.scale.x(), transform.scale.y(), transform.scale.z());
        degreesToQuaternion(transform.rotation, mirror).transform(point);
        point.add(mirror * transform.translation.x(), transform.translation.y(), transform.translation.z());
    }

    private static Quaternionf degreesToQuaternion(Vector3f degrees, float mirror) {
        return new Quaternionf().rotationXYZ(degrees.x() * Mth.DEG_TO_RAD,
                                             mirror * degrees.y() * Mth.DEG_TO_RAD,
                                             mirror * degrees.z() * Mth.DEG_TO_RAD);
    }

    private static Shape shape(BakedModel model) {
        Shape cached = SHAPE_CACHE.get(model);
        if (cached != null) {
            return cached;
        }
        float[] box = rawBounds(model);
        Shape shape = box == null ? Shape.UNMEASURABLE : new Shape(narrowToOpaquePixels(model, box));
        if (SHAPE_CACHE.size() > CACHE_LIMIT) {
            SHAPE_CACHE.clear();
        }
        SHAPE_CACHE.put(model, shape);
        return shape;
    }

    /**
     * Crops a generated item's box down to the footprint of its texture's NON-transparent pixels.
     *
     * <p>It only applies to a thin sheet spanning the WHOLE canvas, which is the signature of a
     * "generated" model: there the texture maps 1:1 onto the face and the opaque fraction in U/V
     * translates directly into a fraction of the box. A hand-made flat JSON model may have a partial
     * box, and then that linear mapping would be false and would crop twice; hence the full-canvas
     * condition. V runs inverted with respect to the model's Y (texture row 0 is the top).
     */
    private static float[] narrowToOpaquePixels(BakedModel model, float[] box) {
        float spanX = box[3] - box[0];
        float spanY = box[4] - box[1];
        float depth = Math.min(spanX, Math.min(spanY, box[5] - box[2]));
        if (depth > FLAT_MAX_DEPTH || spanX < 0.999F || spanY < 0.999F) {
            return box;
        }
        TextureAtlasSprite sprite = model.getParticleIcon();
        if (sprite == null) {
            return box;
        }
        SpriteContents contents = sprite.contents();
        int width = contents.width();
        int height = contents.height();
        if (width <= 0 || height <= 0) {
            return box;
        }
        int minX = width;
        int minY = height;
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (contents.isTransparent(0, x, y)) {
                    continue;
                }
                if (x < minX) {
                    minX = x;
                }
                if (x > maxX) {
                    maxX = x;
                }
                if (y < minY) {
                    minY = y;
                }
                if (y > maxY) {
                    maxY = y;
                }
            }
        }
        if (maxX < 0) {
            return box;   // fully transparent texture: keep the raw box
        }
        float u0 = minX / (float) width;
        float u1 = (maxX + 1) / (float) width;
        float v0 = minY / (float) height;
        float v1 = (maxY + 1) / (float) height;
        return new float[] {
                box[0] + spanX * u0, box[1] + spanY * (1.0F - v1), box[2],
                box[0] + spanX * u1, box[1] + spanY * (1.0F - v0), box[5],
        };
    }

    /** Model AABB, walking all its quads (the null face plus the six directions). */
    private static float[] rawBounds(BakedModel model) {
        if (model.isCustomRenderer()) {
            return null;
        }
        float[] box = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE,
                -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        boolean any = accumulate(model, null, box);
        for (Direction dir : Direction.values()) {
            any |= accumulate(model, dir, box);
        }
        return any ? box : null;
    }

    private static boolean accumulate(BakedModel model, Direction face, float[] box) {
        List<BakedQuad> quads = model.getQuads(null, face, RANDOM);
        if (quads == null || quads.isEmpty()) {
            return false;
        }
        for (BakedQuad quad : quads) {
            int[] data = quad.getVertices();
            // The vertex format may come extended by NeoForge: we derive the stride instead of
            // assuming the 8 ints of the BLOCK format. The first three ints are x,y,z (float bits).
            int stride = data.length / 4;
            if (stride < 3) {
                continue;
            }
            for (int v = 0; v < 4; v++) {
                int offset = v * stride;
                for (int axis = 0; axis < 3; axis++) {
                    float value = Float.intBitsToFloat(data[offset + axis]);
                    if (value < box[axis]) {
                        box[axis] = value;
                    }
                    if (value > box[axis + 3]) {
                        box[axis + 3] = value;
                    }
                }
            }
        }
        return true;
    }
}
