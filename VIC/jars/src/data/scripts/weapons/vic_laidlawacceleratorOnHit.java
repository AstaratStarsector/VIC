package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_laidlawacceleratorOnHit implements OnHitEffectPlugin {

    private static final float FORCE_MULT = 0.666f; // force applied = base damage amount * this

    private static final Vector2f ZERO = new Vector2f();

    private final DamagingExplosionSpec explosion = new DamagingExplosionSpec(0.05f,
            25,
            12.5f,
            150,
            75,
            CollisionClass.PROJECTILE_FF,
            CollisionClass.PROJECTILE_FIGHTER,
            3,
            3,
            0.5f,
            10,
            new Color(33, 255, 122, 255),
            new Color(255, 150, 35, 255)
    );

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (!shieldHit && !projectile.isFading() && target instanceof ShipAPI) {
            explosion.setDamageType(DamageType.FRAGMENTATION);
            explosion.setShowGraphic(false);
            engine.spawnDamagingExplosion(explosion, projectile.getSource(), point);

            float force = projectile.getBaseDamageAmount() * FORCE_MULT;
            CombatUtils.applyForce(target, projectile.getVelocity(), force);

        }
        WaveDistortion wave = new WaveDistortion(point, ZERO);
        wave.setIntensity(15f);
        wave.setSize(100f);
        wave.flip(false);
        wave.fadeOutIntensity(0.3f);
        wave.setLifetime(0.2f);
        wave.fadeOutIntensity(0.3f);
        wave.setLocation(projectile.getLocation());
        DistortionShader.addDistortion(wave);



    }
}
