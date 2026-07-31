package mac.ilike2moveit.fox;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Sleeping fox "Zzz" particle, ported from a Bedrock particle effect.
 *
 * <p>Replicates the behaviour of the Bedrock JSON (everything relative to the spawn point above the
 * head, because the sleeping fox is still and {@code emitter_local_space.position=true}):
 * <ul>
 *   <li>Parametric movement: rises {@code age*0.4 + 0.5} blocks in Y and traces a horizontal sway
 *       {@code x=-0.2*sin(age*200deg)}, {@code z=0.2*cos(age*200deg)} (a slow spiral).</li>
 *   <li>Billboard rotation {@code cos(age*200deg)*-20deg} (roll).</li>
 *   <li>Camera-facing billboard ({@link TextureSheetParticle} does this by default).</li>
 *   <li>Small size {@code (rand*0.08+0.04)} scaled by the source pop curve (0.5-&gt;1.4-&gt;1).</li>
 *   <li>Semi-transparent white tint {@code #A6FFFFFF} (alpha ~0.65) fading to {@code #00FFFFFF}
 *       over the last 20% of its life.</li>
 *   <li>Lifetime ~2.75 s (55 ticks).</li>
 * </ul>
 */
public class FoxZzzParticle extends TextureSheetParticle {
    private static final int LIFETIME_TICKS = 55;            // 2.75 s * 20
    private static final double SPIN_DEG_PER_SECOND = 200.0; // v.particle_age*200 (Bedrock degrees)
    private static final double RADIUS = 0.2;                // radius of the horizontal sway
    private static final double RISE_PER_SECOND = 0.4;       // v.particle_age*0.4
    private static final double RISE_OFFSET = 0.5;           // +0.5 -> starts above the head
    private static final double ROLL_AMPLITUDE_DEG = -20.0;  // rotation: cos(age*200)*-20
    private static final float ALPHA_MAX = 0.651f;           // #A6 = 166/255
    private static final float FADE_START = 0.8f;            // gradient: 0.8 -> 1.0 fades out

    private final double originX;
    private final double originY;
    private final double originZ;
    private final float baseSize;

    FoxZzzParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.originX = x;
        this.originY = y;
        this.originZ = z;
        this.lifetime = LIFETIME_TICKS;
        this.gravity = 0.0f;
        this.hasPhysics = false;
        this.friction = 1.0f;
        // v.particle_random_2*0.08 + 0.04  -> small base size (0.04..0.12 blocks)
        this.baseSize = this.random.nextFloat() * 0.08f + 0.04f;
        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
        this.alpha = ALPHA_MAX;
        applyState(0);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        applyState(this.age);
    }

    private void applyState(int ageTicks) {
        double seconds = ageTicks / 20.0;
        double angle = Math.toRadians(seconds * SPIN_DEG_PER_SECOND);
        // minecraft:particle_motion_parametric.relative_position (relative to the spawn point)
        this.x = this.originX - RADIUS * Math.sin(angle);
        this.y = this.originY + seconds * RISE_PER_SECOND + RISE_OFFSET;
        this.z = this.originZ + RADIUS * Math.cos(angle);
        // billboard rotation
        this.roll = (float) Math.toRadians(Math.cos(angle) * ROLL_AMPLITUDE_DEG);

        float lifeFraction = this.lifetime <= 0 ? 1.0f : (float) ageTicks / this.lifetime;
        // entry pop curve applied to the size
        this.quadSize = this.baseSize * hieseyo(lifeFraction);
        // minecraft:particle_appearance_tinting: constant alpha and then a fade to transparent
        this.alpha = lifeFraction < FADE_START
                ? ALPHA_MAX
                : ALPHA_MAX * Math.max(0.0f, 1.0f - (lifeFraction - FADE_START) / (1.0f - FADE_START));
    }

    /**
     * Size curve from the source effect: linear with nodes {@code [0.5,1.4,1,1,1,1,1,1,1]} evenly
     * spaced over the lifetime (input = age/lifetime). Gives an entry "pop" of 0.5 -&gt; 1.4 -&gt; 1.
     */
    private static float hieseyo(float lifeFraction) {
        float t = lifeFraction < 0.0f ? 0.0f : (lifeFraction > 1.0f ? 1.0f : lifeFraction);
        if (t <= 0.125f) {
            return lerp(0.5f, 1.4f, t / 0.125f);
        }
        if (t <= 0.25f) {
            return lerp(1.4f, 1.0f, (t - 0.125f) / 0.125f);
        }
        return 1.0f;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    @Override
    public ParticleRenderType getRenderType() {
        // "particles_blend" in Bedrock -> translucent sheet with alpha blending
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    /** Client provider hooked into {@code RegisterParticleProvidersEvent} (single-texture sprite set). */
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            FoxZzzParticle particle = new FoxZzzParticle(level, x, y, z);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
