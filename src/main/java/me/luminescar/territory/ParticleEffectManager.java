package me.luminescar.territory;

import org.bukkit.Particle;

public class ParticleEffectManager {

    private static Particle particleEffect = Particle.SOUL_FIRE_FLAME;
    private static int particlesPerSecond = 10;

    public static Particle getParticleEffect() {
        return particleEffect;
    }

    public static void setParticleEffect(Particle particleEffect) {
        ParticleEffectManager.particleEffect = particleEffect;
    }

    public static int getParticlesPerSecond() {
        return particlesPerSecond;
    }

    public static void setParticlesPerSecond(int particlesPerSecond) {
        ParticleEffectManager.particlesPerSecond = particlesPerSecond;
    }
}
