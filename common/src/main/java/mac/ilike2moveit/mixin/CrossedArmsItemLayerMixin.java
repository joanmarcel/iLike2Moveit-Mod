package mac.ilike2moveit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mac.ilike2moveit.emf.EmfLocatorBridge;
import mac.ilike2moveit.render.ItemSizeHarmonizer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemDisplayContext;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Makes villager trade items and wandering-trader drink items (CrossedArmsItemLayer, pinned to the
 * chest in vanilla) FOLLOW the animated arm bone that the pack's CEM model provides, with the same
 * machinery that tunes the item in the fox's mouth (see {@code FoxHeldItemLayerMixin}).
 *
 * <p>Two ways of obtaining the base matrix the item is drawn on:
 * <ol>
 *   <li><b>Native EMF locator (preferred).</b> The {@code .jem} declares the
 *       {@code right_handheld_item} attachment at its {@code held_item} seat (directly or on a
 *       zero-offset locator child).
 *       EMF captures that bone's accumulated matrix while rendering the mesh — hierarchy and the
 *       frame's animation already applied — and we read it here ({@link EmfLocatorBridge}). The item
 *       becomes a REAL child of the animated arm: it moves in real time with the offering gesture,
 *       with no frame or phase lag. That is what makes it fluid.</li>
 *   <li><b>DFS over the bone tree (fallback).</b> If EMF is not active, or the model falls back to
 *       vanilla/Fresh Animations with no locator, the {@code root -> ... -> bone} chain is replicated
 *       by hand ({@code alt_arms}, the custom one with sway, or vanilla {@code arms}). Coarser, but it
 *       avoids leaving the item stuck to the chest.</li>
 * </ol>
 *
 * <p>From there on everything is shared with the fox: the model is requested from
 * {@code ModefiteBridge} (the one actually drawn, not the flat sprite {@code getModel} returns under
 * Modefite), its size is harmonised and it is seated by subtracting its visual centre, so that a
 * single set of coordinates works for every item and rotating them does not shift them. Live
 * calibration with {@code vil_*} keys (GROUND context, flat) and {@code vil3d_*} keys (hand context,
 * 3D) in a file of ITS OWN, {@code /tmp/ilike2moveit_villager_offset.txt}, kept apart from the fox's
 * so each mob can be tuned without touching the other.
 *
 * <p>On 1.21.1 the render pipeline is the classic one (10-arg render(...)); EntityRenderState arrived
 * in 1.21.2, hence this signature. Client-side.
 */
@Mixin(CrossedArmsItemLayer.class)
public abstract class CrossedArmsItemLayerMixin<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    @Shadow @Final private ItemInHandRenderer itemInHandRenderer;

    // Bones IN PRIORITY ORDER for the DFS fallback: the pack's custom one first (under waist -> it
    // inherits the idle sway plus the gesture); vanilla "arms" (a direct child of root, WITHOUT sway)
    // only as a last resort.
    private static final String[] ILIKE2MOVEIT$BONES = {"alt_arms", "arms"};

    // Two independent sets depending on the display CONTEXT, same as the fox:
    // index 0 = GROUND context (vil_* keys, flat); index 1 = hand context (vil3d_* keys).
    private static final int ILIKE2MOVEIT$FLAT = 0;
    private static final int ILIKE2MOVEIT$SOLID = 1;
    // Values calibrated on screen and baked in (index 0 = flat vil_ group, 1 = 3D vil3d_ group).
    // Live-recalibrable from /tmp/ilike2moveit_villager_offset.txt; without that file these are used.
    private static final float[] ilike2moveit$ox = {0.0F, 0.0F};
    private static final float[] ilike2moveit$oy = {-1.0F, -1.03F};
    private static final float[] ilike2moveit$oz = {-0.3F, -0.20F};
    private static final float[] ilike2moveit$scale = {0.90F, 1.0F};
    private static final float[] ilike2moveit$rotX = {90.0F, -90.0F};
    private static final float[] ilike2moveit$rotY = {0.0F, 180.0F};
    private static final float[] ilike2moveit$rotZ = {180.0F, 0.0F};
    // Calibracion visual propia de los recipientes del wandering trader. No se mezcla con los
    // objetos que un villager ofrece: cada indice conserva su ajuste independiente 2D/3D.
    private static final float[] ILIKE2MOVEIT$TRADER_DRINK_OX = {0.0F, 0.0F};
    private static final float[] ILIKE2MOVEIT$TRADER_DRINK_OY = {-1.10F, -1.09F};
    private static final float[] ILIKE2MOVEIT$TRADER_DRINK_OZ = {-0.15F, -0.20F};
    private static final float[] ILIKE2MOVEIT$TRADER_DRINK_SCALE = {0.90F, 0.90F};
    // Item display context. ItemDisplayContext ordinal: 2 = 3D right hand, 7 = GROUND.
    private static volatile int ilike2moveit$ctx = 2;
    private static volatile float ilike2moveit$harmony = 1.0F;
    private static volatile long ilike2moveit$lastLoad = 0L;
    private static volatile long ilike2moveit$lastLog = 0L;   // temporary DEBUG
    private static final java.io.File ILIKE2MOVEIT$CFG = new java.io.File("/tmp/ilike2moveit_villager_offset.txt");

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
            try {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.charAt(0) == '#') continue;
                int eq = trimmed.indexOf('=');
                if (eq <= 0) continue;
                String key = trimmed.substring(0, eq).trim();
                float v = Float.parseFloat(trimmed.substring(eq + 1).trim());
                // vil3d_<axis> targets the set for models with volume; vil_<axis> the flat ones.
                int g = key.startsWith("vil3d_") ? ILIKE2MOVEIT$SOLID : ILIKE2MOVEIT$FLAT;
                String axis = key.startsWith("vil3d_") ? key.substring(6)
                        : key.startsWith("vil_") ? key.substring(4) : "";
                switch (axis) {
                    case "x": ilike2moveit$ox[g] = v; break;
                    case "y": ilike2moveit$oy[g] = v; break;
                    case "z": ilike2moveit$oz[g] = v; break;
                    case "scale": ilike2moveit$scale[g] = v; break;
                    case "rotx": ilike2moveit$rotX[g] = v; break;
                    case "roty": ilike2moveit$rotY[g] = v; break;
                    case "rotz": ilike2moveit$rotZ[g] = v; break;
                    // These two are global: read from the vil_ group only and applied to both.
                    case "ctx": if (g == ILIKE2MOVEIT$FLAT) ilike2moveit$ctx = (int) v; break;
                    case "harmony": if (g == ILIKE2MOVEIT$FLAT) ilike2moveit$harmony = v; break;
                    default: break;
                }
            } catch (Exception ignored) {}
        }
    }

    private CrossedArmsItemLayerMixin(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Inject(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void ilike2moveit$followArms(PoseStack ps, MultiBufferSource buf, int light, T entity,
                                         float limbSwing, float limbSwingAmount, float partialTick,
                                         float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        boolean wanderingTrader = entity instanceof WanderingTrader;
        if (!(entity instanceof Villager) && !wanderingTrader) return;
        ItemStack stack = entity.getItemBySlot(EquipmentSlot.MAINHAND);
        if (stack.isEmpty()) return;

        // UseItemGoal equipa pocion/leche y limpia el slot en pasos distintos. Solo se muestran
        // durante el uso sincronizado: un slot cliente rezagado no deja la botella flotando.
        boolean traderPotion = wanderingTrader && stack.is(Items.POTION);
        boolean traderMilk = wanderingTrader && stack.is(Items.MILK_BUCKET);
        boolean traderDrink = traderPotion || traderMilk;
        if (traderDrink) {
            int useDuration = stack.getUseDuration(entity);
            int ticksUsing = entity.getTicksUsingItem();
            boolean withinUseWindow = entity.isUsingItem()
                    && ticksUsing >= 0 && ticksUsing < useDuration;
            // La aplicacion del efecto es un segundo cierre autoritativo: la pocion termina al
            // hacerse invisible y la leche al volver visible, incluso si el flag de uso llega tarde.
            boolean effectPending = traderPotion ? !entity.isInvisible() : entity.isInvisible();
            if (!withinUseWindow || !effectPending) {
                ci.cancel();
                return;
            }
        }

        ilike2moveit$reloadIfNeeded();
        long ilike2moveit$now = System.currentTimeMillis();
        boolean ilike2moveit$debug = ilike2moveit$now - ilike2moveit$lastLog > 1000L;
        if (ilike2moveit$debug) ilike2moveit$lastLog = ilike2moveit$now;

        // --- Path 1: native EMF locator (item as a real child of the animated arm, fluid). ---
        PoseStack.Pose locator = EmfLocatorBridge.currentRightItemPose();

        // El trader solo se reemplaza cuando su pack aporta el held_item locator. Sin él se deja
        // intacto el render vanilla; el DFS de brazos es una reserva exclusiva del trade villager.
        if (wanderingTrader && locator == null) {
            if (ilike2moveit$debug) mac.ilike2moveit.MoveItCore.LOGGER.info(
                "[WanderingTraderItem] locator=NULL cfgExists={}", ILIKE2MOVEIT$CFG.exists());
            return;
        }

        ci.cancel();
        ps.pushPose();
        boolean viaLocator;
        if (locator != null) {
            // Replace the current transform with the locator bone's accumulated matrix (already
            // animated by EMF this frame). Do NOT reapply any of the hierarchy: the captured pose
            // already includes it.
            PoseStack.Pose top = ps.last();
            top.pose().set(locator.pose());
            top.normal().set(locator.normal());
            viaLocator = true;
        } else {
            // --- Path 2: DFS fallback. Replays this frame's root -> ... -> bone chain by hand.
            M model = this.getParentModel();
            if (!(model instanceof HierarchicalModel<?> hm)) { ps.popPose(); return; }
            ModelPart root = hm.root();
            List<ModelPart> path = null;
            for (String bone : ILIKE2MOVEIT$BONES) {
                List<ModelPart> p = new ArrayList<>();
                if (ilike2moveit$findPath(root, bone, p)) { path = p; break; }
            }
            if (path == null) { ps.popPose(); return; }   // no bone at all -> leave the vanilla render
            root.translateAndRotate(ps);
            for (ModelPart part : path) {
                part.translateAndRotate(ps);
            }
            viaLocator = false;
        }

        // --- Shared with the fox, BUT grouping by REAL GEOMETRY instead of by context. ---
        // The group (2D vs 3D) is decided by measuring the model that is actually drawn: an item with
        // no 3D mesh in the pack (leather_leggings, say) comes out flat even when the context is "in
        // hand", and belongs in the vil_ group (flat); the ones with volume (diamond, ingot, 3D brick)
        // fall into vil3d_. Grouping used to be by display context, and with vil_ctx=2 nothing ever
        // reached the 2D group.
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        boolean force2d = stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES)
                || stack.is(Items.MACE) || stack.is(Items.LEATHER);
        // Model in the configured hand context, so it can be classified by its real volume.
        ItemDisplayContext handCtx = ItemDisplayContext.values()[Math.max(0, Math.min(8, ilike2moveit$ctx))];
        net.minecraft.client.resources.model.BakedModel handModel =
                mac.ilike2moveit.compat.ModefiteBridge.drawnModel(stack, entity, handCtx);
        boolean missing = handModel == mc.getModelManager().getMissingModel();
        boolean flat = force2d || missing || ItemSizeHarmonizer.isFlat(handModel);

        // Each group has its own context: flat ones are drawn lying down (GROUND), 3D ones with the
        // holding perspective (hand context). That way they share a seat within the group.
        ItemDisplayContext dc;
        net.minecraft.client.resources.model.BakedModel bm;
        if (flat) {
            dc = ItemDisplayContext.GROUND;
            bm = mac.ilike2moveit.compat.ModefiteBridge.drawnModel(stack, entity, dc);
        } else {
            dc = handCtx;
            bm = handModel;
        }
        boolean leftHand = (dc == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || dc == ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
        int g = flat ? ILIKE2MOVEIT$FLAT : ILIKE2MOVEIT$SOLID;
        // La silueta fina del fallback 2D satura el armonizador y lo infla hasta 1.8x.
        // Mantener esos recipientes en la escala plana autorada; los modelos 3D conservan
        // la armonizacion normal que ya iguala correctamente su volumen visual.
        float harmonyExponent = traderDrink && flat ? 0.0F : ilike2moveit$harmony;
        float harmony = ItemSizeHarmonizer.harmonyScale(bm, dc, leftHand, harmonyExponent);
        float ox = traderDrink ? ILIKE2MOVEIT$TRADER_DRINK_OX[g] : ilike2moveit$ox[g];
        float oy = traderDrink ? ILIKE2MOVEIT$TRADER_DRINK_OY[g] : ilike2moveit$oy[g];
        float oz = traderDrink ? ILIKE2MOVEIT$TRADER_DRINK_OZ[g] : ilike2moveit$oz[g];
        float authoredScale = traderDrink ? ILIKE2MOVEIT$TRADER_DRINK_SCALE[g] : ilike2moveit$scale[g];
        float s = authoredScale * harmony;

        ps.translate(ox, oy, oz);
        Quaternionf rot = new Quaternionf()
                .rotateX(ilike2moveit$rotX[g] * Mth.DEG_TO_RAD)
                .rotateY(ilike2moveit$rotY[g] * Mth.DEG_TO_RAD)
                .rotateZ(ilike2moveit$rotZ[g] * Mth.DEG_TO_RAD);
        ps.mulPose(rot);
        ps.scale(s, s, s);
        // Shared seating: subtracting the real model's visual centre cancels its own offset, so its
        // centre always lands on (ox,oy,oz) whatever its shape, scale or rotation.
        Vector3f pivot = ItemSizeHarmonizer.visualCenter(bm, dc, leftHand);
        ps.translate(-pivot.x, -pivot.y, -pivot.z);
        if (ilike2moveit$debug) mac.ilike2moveit.MoveItCore.LOGGER.info(
            "[{}] item={} via={} group={} model={} viaModefite={} seat=({}, {}, {}) pivot=({}, {}, {}) harmony={} scale={} ctx={}",
            wanderingTrader ? "WanderingTraderItem" : "VillagerItem",
            stack.getItem(), viaLocator ? "LOCATOR" : "DFS",
            g == ILIKE2MOVEIT$FLAT ? "GROUND(vil_)" : "HAND(vil3d_)",
            bm.getClass().getSimpleName(), mac.ilike2moveit.compat.ModefiteBridge.isActive(),
            ox, oy, oz, pivot.x, pivot.y, pivot.z, harmony, s, dc);
        this.itemInHandRenderer.renderItem(entity, stack, dc, leftHand, ps, buf, light);
        ps.popPose();
    }

    /**
     * DFS over the bone tree. Fills `path` with the descendants from root's first child down to the
     * target bone (inclusive, WITHOUT including root). Returns true if it finds it.
     */
    private static boolean ilike2moveit$findPath(ModelPart node, String target, List<ModelPart> path) {
        Map<String, ModelPart> children = ((ModelPartAccessor) (Object) node).ilike2moveit$getChildren();
        for (Map.Entry<String, ModelPart> e : children.entrySet()) {
            path.add(e.getValue());
            if (e.getKey().equals(target)) return true;
            if (ilike2moveit$findPath(e.getValue(), target, path)) return true;
            path.remove(path.size() - 1);
        }
        return false;
    }
}
