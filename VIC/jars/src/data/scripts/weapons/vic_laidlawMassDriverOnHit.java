package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_laidlawMassDriverOnHit implements OnHitEffectPlugin {

    private static final Vector2f ZERO = new Vector2f();

    private final DamagingExplosionSpec explosion = new DamagingExplosionSpec(0.05f,
            20,
            10,
            75,
            37.5f,
            CollisionClass.PROJECTILE_FF,
            CollisionClass.PROJECTILE_FIGHTER,
            2,
            2,
            0.5f,
            10,
            new Color(33, 255, 122, 255),
            new Color(255, 150, 35, 255)
    );

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
                      Vector2f point, boolean shieldHit, CombatEngineAPI engine) {
        if (!shieldHit && !projectile.isFading() && target instanceof ShipAPI) {
            explosion.setDamageType(DamageType.FRAGMENTATION);
            explosion.setShowGraphic(false);
            engine.spawnDamagingExplosion(explosion, projectile.getSource(), point);
        }
        WaveDistortion wave = new WaveDistortion(point, ZERO);
        wave.setIntensity(15f);
        wave.setSize(50f);
        wave.flip(false);
        wave.fadeOutIntensity(0.3f);
        wave.setLifetime(0.2f);
        wave.fadeOutIntensity(0.3f);
        wave.setLocation(projectile.getLocation());
        DistortionShader.addDistortion(wave);

    }
}
