package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicRender;
import data.scripts.utilities.vic_graphicLibEffects;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

import static com.fs.starfarer.api.util.Misc.ZERO;
import static com.fs.starfarer.api.util.Misc.getAngleInDegrees;

public class vic_apocryphaOnHit implements OnHitEffectPlugin {

    private static final Color PARTICLE_COLOR = new Color(255, 68, 21, 150);
    private static final Color CORE_COLOR = new Color(255, 34, 67);
    private static final Color AFTERMATH_COLOR = new Color(201, 123, 68);
    private static final Color FLASH_COLOR = new Color(255, 209, 173);
    private static final int NUM_PARTICLES = 50;

    private boolean light = false;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        float force = (projectile.getDamageAmount() * 0.15f);
        CombatUtils.applyForce(target, projectile.getVelocity(), force);

        engine.spawnExplosion(point, ZERO, PARTICLE_COLOR, 300f, 1.3f);
        engine.spawnExplosion(point, ZERO, CORE_COLOR, 150f, 1f);
        engine.spawnExplosion(point, ZERO, AFTERMATH_COLOR, 250f, 2.5f);
        engine.addSmoothParticle(point, ZERO, 1000, 1f, 0.1f, FLASH_COLOR);
        engine.addSmoothParticle(point, ZERO, 1300, 1f, 0.2f, FLASH_COLOR);

        engine.addSmoothParticle(point, ZERO, 400f, 0.5f, 0.1f, PARTICLE_COLOR);
        engine.addHitParticle(point, ZERO, 200f, 0.5f, 0.25f, FLASH_COLOR);
        for (int x = 0; x < NUM_PARTICLES; x++) {
            engine.addHitParticle(point,
                    MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(100f, 500f), (float) Math.random() * 360f),
                    10f, 1f, MathUtils.getRandomNumberInRange(0.3f, 0.6f), PARTICLE_COLOR);
        }
        MagicRender.battlespace(
                Global.getSettings().getSprite("fx","vic_stolas_emp_secondary"),
                point,
                ZERO,
                new Vector2f(100,100),
                new Vector2f(750,750),
                //angle,
                360*(float)Math.random(),
                0,
                new Color(255, 0, 30, 109),
                true,
                0,
                0.2f,
                1f
        );

        MagicRender.battlespace(
                Global.getSettings().getSprite("fx","vic_laidlawExplosion"),
                point,
                ZERO,
                new Vector2f(250,250),
                new Vector2f(100,100),
                //angle,
                360*(float)Math.random(),
                0,
                new Color(255, 153, 0, 100),
                true,
                0.3f,
                0f,
                3f
        );

        MagicRender.battlespace(
                Global.getSettings().getSprite("fx","vic_stolas_emp_secondary"),
                point,
                ZERO,
                new Vector2f(200,200),
                new Vector2f(150,150),
                //angle,
                360*(float)Math.random(),
                0,
                new Color(255, 51, 0, 150),
                true,
                0.3f,
                0f,
                2f
        );

        WaveDistortion wave = new WaveDistortion(point, ZERO);
        wave.setIntensity(1.5f);
        wave.setSize(300f);
        wave.flip(true);
        wave.setLifetime(0f);
        wave.fadeOutIntensity(1f);
        wave.setLocation(projectile.getLocation());
        DistortionShader.addDistortion(wave);

        if (Global.getSettings().getModManager().isModEnabled("shaderLib")) {
            light = true;
        }

        if (light) {
            vic_graphicLibEffects.CustomRippleDistortion(
                    point,
                    ZERO,
                    300,
                    3,
                    false,
                    0,
                    360,
                    1f,
                    0.1f,
                    0.25f,
                    0.5f,
                    0.5f,
                    0f
            );
        }
    }
}