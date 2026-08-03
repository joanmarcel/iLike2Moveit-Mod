package mac.ilike2moveit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mac.ilike2moveit.emf.EmfLocatorBridge;
import mac.ilike2moveit.render.ItemSizeHarmonizer;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.FoxHeldItemLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes the item held in the fox's mouth (FoxHeldItemLayer, anchored in vanilla to the vanilla head
 * with the ENTITY's rotations) FOLLOW the animated CEM bone the pack provides.
 *
 * <p>In {@code fox*.jem} the pack declares a locator {@code alt_held_item} (under the snout bone)
 * carrying the EMF attachment {@code right_handheld_item}. EMF captures that bone's accumulated
 * matrix while rendering the mesh; we read it here ({@link EmfLocatorBridge}) and draw the item on
 * top of it -> a REAL child of the animated bone, with no frame or phase lag.
 *
 * <p>If EMF is not active, or the fox does not expose the pose (because the model falls back to
 * vanilla/Fresh Animations, say), {@link EmfLocatorBridge#currentRightItemPose()} returns
 * {@code null} and we leave the vanilla render untouched (zero regression).
 *
 * <p>On 1.21.1 the render pipeline is the classic one (10-arg render(...)); EntityRenderState arrived
 * in 1.21.2, hence this signature. Client-side.
 */
@Mixin(FoxHeldItemLayer.class)
public abstract class FoxHeldItemLayerMixin extends RenderLayer<Fox, FoxModel<Fox>> {

    @Shadow @Final private ItemInHandRenderer itemInHandRenderer;

    // --- Local locator->mouth offset (fine on-screen calibration). The EMF locator already places the
    // frame on the animated bone; these values only tune the item's seating/orientation/size.
    // Hot-reloadable through /tmp/ilike2moveit_offset.txt with fox_* keys (they coexist with the
    // villager's). BLOCK space (small, vanilla-like values). ---
    // Two independent sets depending on the display CONTEXT (the why is next to their use below):
    // index 0 = GROUND context, fox_* keys; index 1 = hand context, fox3d_* keys.
    private static final int ILIKE2MOVEIT$FLAT = 0;
    private static final int ILIKE2MOVEIT$SOLID = 1;
    // Values calibrated on screen and baked in (index 0 = flat fox_ group, 1 = 3D fox3d_ group).
    // Live-recalibrable from /tmp/ilike2moveit_offset.txt; without that file these are used.
    private static final float[] ilike2moveit$ox = {0.0F, 0.0F};
    private static final float[] ilike2moveit$oy = {0.02F, 0.10F};
    private static final float[] ilike2moveit$oz = {-0.25F, -0.10F};
    private static final float[] ilike2moveit$scale = {0.95F, 0.90F};
    private static final float[] ilike2moveit$rotX = {90.0F, 0.0F};
    private static final float[] ilike2moveit$rotY = {0.0F, 0.0F};
    private static final float[] ilike2moveit$rotZ = {180.0F, 180.0F};
    // Item display context (same scheme as the villager). ItemDisplayContext ordinal:
    // 0 NONE, 1/2 THIRD_PERSON_LEFT/RIGHT_HAND (3D hand models), 3/4 FIRST_PERSON, 5 HEAD,
    // 6 GUI, 7 GROUND (flat ground sprite/model), 8 FIXED. Default 2 = 3D in the right hand.
    private static volatile int ilike2moveit$ctx = 2;
    // How strongly item sizes are harmonised (see ItemSizeHarmonizer): 1 = all with the same apparent
    // silhouette as the iron ingot, 0 = the 3D pack's raw sizes.
    private static volatile float ilike2moveit$harmony = 1.0F;
    private static volatile long ilike2moveit$lastLoad = 0L;
    private static volatile long ilike2moveit$lastLog = 0L;   // temporary DEBUG
    private static final java.io.File ILIKE2MOVEIT$CFG = new java.io.File("/tmp/ilike2moveit_offset.txt");

    private static void ilike2moveit$reloadIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - ilike2moveit$lastLoad < 400L) return;
        ilike2moveit$lastLoad = now;
        java.util.List<String> lines;
        try {
            if (!ILIKE2MOVEIT$CFG.exists()) return;
            lines = java.nio.file.Files.readAllLines(ILIKE2MOVEIT$CFG.toPath());
        } catch (Exception e) {
            return;
        }
        for (String line : lines) {
            // A malformed line only skips itself: the rest of the calibration stays alive.
            // (With a single try wrapping the loop, one typo while hot-editing left ALL the values
            // half-loaded.)
            try {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.charAt(0) == '#') continue;
                int eq = trimmed.indexOf('=');
                if (eq <= 0) continue;
                String key = trimmed.substring(0, eq).trim();
                float v = Float.parseFloat(trimmed.substring(eq + 1).trim());
                // fox3d_<axis> targets the set for models with volume; fox_<axis> the flat ones.
                int g = key.startsWith("fox3d_") ? ILIKE2MOVEIT$SOLID : ILIKE2MOVEIT$FLAT;
                String axis = key.startsWith("fox3d_") ? key.substring(6)
                        : key.startsWith("fox_") ? key.substring(4) : "";
                switch (axis) {
                    case "x": ilike2moveit$ox[g] = v; break;
                    case "y": ilike2moveit$oy[g] = v; break;
                    case "z": ilike2moveit$oz[g] = v; break;
                    case "scale": ilike2moveit$scale[g] = v; break;
                    case "rotx": ilike2moveit$rotX[g] = v; break;
                    case "roty": ilike2moveit$rotY[g] = v; break;
                    case "rotz": ilike2moveit$rotZ[g] = v; break;
                    // These two are global: read from the fox_ group only and applied to both.
                    case "ctx": if (g == ILIKE2MOVEIT$FLAT) ilike2moveit$ctx = (int) v; break;
                    case "harmony": if (g == ILIKE2MOVEIT$FLAT) ilike2moveit$harmony = v; break;
                    default: break;
                }
            } catch (Exception ignored) {}
        }
    }

    private FoxHeldItemLayerMixin(RenderLayerParent<Fox, FoxModel<Fox>> parent) {
        super(parent);
    }

    @Inject(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/Fox;FFFFFF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void ilike2moveit$followMouth(PoseStack ps, MultiBufferSource buf, int light, Fox fox,
                                          float limbSwing, float limbSwingAmount, float partialTick,
                                          float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        ItemStack stack = fox.getItemBySlot(EquipmentSlot.MAINHAND);
        if (stack.isEmpty()) return;

        PoseStack.Pose locator = EmfLocatorBridge.currentRightItemPose();
        // Temporary DEBUG: does the mixin draw (locator OK) or yield to vanilla (NULL)? What scale?
        ilike2moveit$reloadIfNeeded();
        long ilike2moveit$now = System.currentTimeMillis();
        boolean ilike2moveit$debug = ilike2moveit$now - ilike2moveit$lastLog > 1000L;
        if (ilike2moveit$debug) ilike2moveit$lastLog = ilike2moveit$now;
        if (locator == null) {
            if (ilike2moveit$debug) mac.ilike2moveit.MoveItCore.LOGGER.info(
                "[FoxItem] locator=NULL cfgExists={}", ILIKE2MOVEIT$CFG.exists());
            return;   // EMF inactive / no attachment -> leave the vanilla render
        }

        // We replace the vanilla render (anchored to the vanilla head + the entity's rotations).
        ci.cancel();
        ps.pushPose();
        // Replace the current transform with the locator bone's accumulated matrix (already animated
        // by EMF this frame): the item becomes a real child of the snout. Do NOT reapply baby/sleep:
        // the captured pose already includes them (baby scale and curled-up pose live in the
        // stack/bones).
        PoseStack.Pose top = ps.last();
        top.pose().set(locator.pose());
        top.normal().set(locator.normal());
        // Display context (same scheme as the villager): weapons/tools -> GROUND (flat 2D sprite,
        // avoids the oversized/odd 3D model); everything else -> hand context (3D model from packs
        // like Weskerson's). Vanilla fox used a fixed GROUND; here we default to a 3D item.
        // The mace has no ItemTag of its own (new 1.21 weapon) and must be forced by hand; leather DID
        // fall into the 3D group by default despite having no `_hand` mesh in the pack -> it was drawn
        // as a flat sprite but calibrated with the hand offsets, hence misplaced.
        boolean force2d = stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES)
                || stack.is(Items.MACE) || stack.is(Items.LEATHER);
        ItemDisplayContext dc = force2d
                ? ItemDisplayContext.GROUND
                : ItemDisplayContext.values()[Math.max(0, Math.min(8, ilike2moveit$ctx))];
        boolean leftHand = (dc == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || dc == ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        // The model must be requested from ModefiteBridge, NOT from getItemRenderer().getModel(): with
        // Modefite installed that getModel always returns the GUI-context resolution (the flat
        // sprite), while renderStatic resolves again with the real context and draws the pack's 3D
        // *_hand.json. Measuring one and drawing the other is what misplaced every item differently
        // (ingot fine, diamond at the tip of the snout, berries floating in front).
        net.minecraft.client.resources.model.BakedModel bm =
                mac.ilike2moveit.compat.ModefiteBridge.drawnModel(stack, fox, dc);
        // If the item's 3D model is broken/absent (missing model), fall back to GROUND (2D) instead of
        // showing the magenta/black cube. It must be RE-RESOLVED: the GROUND model is a different one.
        if (dc != ItemDisplayContext.GROUND && bm == mc.getModelManager().getMissingModel()) {
            dc = ItemDisplayContext.GROUND;
            leftHand = false;
            bm = mac.ilike2moveit.compat.ModefiteBridge.drawnModel(stack, fox, dc);
        }
        // Harmonised size: every model has a different box/pixel footprint (the ingot is thin and
        // elongated, the diamond chunky), so we match their apparent SILHOUETTE to the iron ingot's
        // instead of drawing them all at the same raw scale. See ItemSizeHarmonizer.
        //
        // Alignment group: grouped by display CONTEXT, not by the mesh's geometry. It is the context
        // that decides which ItemTransform vanilla applies, and therefore the orientation the player
        // perceives: GROUND leaves the sprite lying face up, the hand context tilts it into the
        // holding perspective. Each group carries its own calibration because those two seatings
        // cannot be reconciled with a single set of angles.
        int g = dc == ItemDisplayContext.GROUND ? ILIKE2MOVEIT$FLAT : ILIKE2MOVEIT$SOLID;
        float harmony = ItemSizeHarmonizer.harmonyScale(bm, dc, leftHand, ilike2moveit$harmony);
        float s = ilike2moveit$scale[g] * harmony;

        // Local hot-reload tuning (seating in the mouth). Baked into constants once calibrated.
        ps.translate(ilike2moveit$ox[g], ilike2moveit$oy[g], ilike2moveit$oz[g]);
        Quaternionf rot = new Quaternionf()
                .rotateX(ilike2moveit$rotX[g] * Mth.DEG_TO_RAD)
                .rotateY(ilike2moveit$rotY[g] * Mth.DEG_TO_RAD)
                .rotateZ(ilike2moveit$rotZ[g] * Mth.DEG_TO_RAD);
        ps.mulPose(rot);
        ps.scale(s, s, s);
        // SHARED SEATING: vanilla does not draw the item at the PoseStack origin, but offset by its
        // ItemTransform (translation + rotation + scale of the display context) plus the translate(-0.5)
        // that centres the mesh; and on top of that each item occupies a different area of its canvas
        // (the ingot is thin and low, the diamond chunky and centred). That own offset differs per
        // item, so with the SAME offsets the ingot sat in the mouth and the diamond at the tip of the
        // snout. Subtracting the visual centre cancels that own offset: the item is drawn at
        // `ox+R*s*(p-pivot)`, so its centre (p == pivot) ALWAYS lands on (ox,oy,oz) whatever its shape,
        // its harmonised scale or its rotation angles. A single set of coordinates works for every
        // item, and rotating does not shift them.
        Vector3f pivot = ItemSizeHarmonizer.visualCenter(bm, dc, leftHand);
        ps.translate(-pivot.x, -pivot.y, -pivot.z);
        if (ilike2moveit$debug) mac.ilike2moveit.MoveItCore.LOGGER.info(
            "[FoxItem] item={} grupo={} modelo={} viaModefite={} pivot=({}, {}, {}) harmony={} scale={} ctx={} m={} ref={} err={}",
            stack.getItem(), g == ILIKE2MOVEIT$FLAT ? "GROUND(fox_)" : "HAND(fox3d_)",
            bm.getClass().getSimpleName(), mac.ilike2moveit.compat.ModefiteBridge.isActive(),
            pivot.x, pivot.y, pivot.z, harmony, s, dc,
            ItemSizeHarmonizer.measureOf(bm, dc, leftHand), ItemSizeHarmonizer.referenceOf(dc, leftHand),
            ItemSizeHarmonizer.lastError());
        this.itemInHandRenderer.renderItem(fox, stack, dc, leftHand, ps, buf, light);
        ps.popPose();
    }
}
