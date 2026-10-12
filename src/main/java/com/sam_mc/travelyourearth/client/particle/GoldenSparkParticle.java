package com.sam_mc.travelyourearth.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * Golden sparkle of Cowardice: EXACTLY the same behaviour as vanilla's CritParticle
 * (critical hits and Sharpness), only with your golden textures:
 * - picks one of the 4 textures at random (big star, small star, X, twinkle),
 * - same speed, friction (0.7), gravity (0.5), size (x0.75) and life (about 6-10 ticks) as the crit,
 * - pops in quickly and, like the crit, gets a bit more orange while it lives.
 */
public class GoldenSparkParticle extends SingleQuadParticle {

    protected GoldenSparkParticle(ClientLevel level, double x, double y, double z,
                                  double xa, double ya, double za, TextureAtlasSprite sprite) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D, sprite);
        // Same numbers as vanilla's CritParticle
        this.friction = 0.7F;
        this.gravity = 0.5F;
        this.xd *= 0.1F;
        this.yd *= 0.1F;
        this.zd *= 0.1F;
        this.xd += xa * 0.4D;
        this.yd += ya * 0.4D;
        this.zd += za * 0.4D;
        // Crit: a random brightness (0.6 - 0.9). Ours is a bit brighter so the gold shines (0.85 - 1.0)
        float col = this.random.nextFloat() * 0.15F + 0.85F;
        this.rCol = col;
        this.gCol = col;
        this.bCol = col;
        this.quadSize *= 0.75F;
        this.lifetime = Math.max((int) (6.0D / (this.random.nextFloat() * 0.8D + 0.6D)), 1);
        this.hasPhysics = false;
        this.tick();
    }

    /** Pops in very quickly (same as the crit). */
    @Override
    public float getQuadSize(float a) {
        return this.quadSize * Mth.clamp((this.age + a) / this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    /** Same as the crit: it slowly turns a bit more orange (on gold it looks like it's cooling down). */
    @Override
    public void tick() {
        super.tick();
        this.gCol *= 0.96F;
        this.bCol *= 0.9F;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    /** Creates the sparkles: each one gets a RANDOM texture of the set (sprite.get(random)). */
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z,
                                       double xAux, double yAux, double zAux, RandomSource random) {
            return new GoldenSparkParticle(level, x, y, z, xAux, yAux, zAux, this.sprites.get(random));
        }
    }
}