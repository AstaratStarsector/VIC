package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import org.magiclib.util.MagicRender;
import data.scripts.utilities.vic_graphicLibEffects;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_laidlawacceleratorOnHit implements OnHitEffectPlugin {

    private boolean light = false;

    final Vector2f ZERO = new Vector2f();
    final float damagePercent = 0.25f;

    private final DamagingExplosionSpec explosion = new DamagingExplosionSpec(0.05f,
            125,
            67f,
            500,
            250,
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
        if (!projectile.isFading() && target instanceof ShipAPI && !((ShipAPI) target).isFighter() && !(target instanceof MissileAPI)) {
            explosion.setDamageType(DamageType.FRAGMENTATION);
            explosion.setShowGraphic(false);
            explosion.setMinDamage(projectile.getDamageAmount() * damagePercent * 0.5f);
            explosion.setMaxDamage(projectile.getDamageAmount() * damagePercent);
            engine.spawnDamagingExplosion(explosion, projectile.getSource(), point);





        if (Global.getSettings().getModManager().isModEnabled("shaderLib")) {
            light = true;
        }

            WaveDistortion wave = new WaveDistortion(point, ZERO);
            wave.setIntensity(1.5f);
            wave.setSize(75f);
            wave.flip(true);
            wave.setLifetime(0f);
            wave.fadeOutIntensity(0.66f);
            wave.setLocation(projectile.getLocation());
            DistortionShader.addDistortion(wave);

            if (light) {
                vic_graphicLibEffects.CustomRippleDistortion(
                        point,
                        ZERO,
                        75,
                        3,
                        false,
                        0,
                        360,
                        1f,
                        0.1f,
                        0.25f,
                        0.2f,
                        0.3f,
                        0f
                );
            }


            engine.spawnExplosion(
                    point,
                    new Vector2f(0, 0),
                    new Color(255, 255, 255, 255),
                    25f,
                    0.3f);

            engine.spawnExplosion(
                    point,
                    new Vector2f(0, 0),
                    new Color(0, 255, 225, 75),
                    50f,
                    0.75f);

            engine.addSmoothParticle(
                    point,
                    new Vector2f(),
                    200,
                    2f,
                    0.15f,
                    new Color(100, 255, 255, 255));


            float angle = 360 * (float) Math.random();

            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "vic_laidlawExplosion"),
                    point,
                    new Vector2f(),
                    new Vector2f(72, 72),
                    new Vector2f(300, 300),
                    //angle,
                    360 * (float) Math.random(),
                    0,
                    new Color(255, 200, 200, 255),
                    true,
                    0,
                    0.1f,
                    0.15f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "vic_laidlawExplosion"),
                    point,
                    new Vector2f(),
                    new Vector2f(96, 96),
                    new Vector2f(150, 150),
                    //angle,
                    360 * (float) Math.random(),
                    0,
                    new Color(255, 225, 225, 225),
                    true,
                    0.2f,
                    0.0f,
                    0.3f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "vic_laidlawExplosion"),
                    point,
                    new Vector2f(),
                    new Vector2f(150, 150),
                    new Vector2f(75, 75),
                    //angle,
                    360 * (float) Math.random(),
                    0,
                    new Color(255, 255, 255, 200),
                    true,
                    0.4f,
                    0.0f,
                    0.9f
            );
        }

    }
}
