package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AsteroidAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.Misc;
import data.scripts.utilities.vic_graphicLibEffects;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.json.JSONException;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static data.scripts.utilities.vic_getSettings.getBoolean;

public class vic_alkonostExplosion {

    static final Color CORE_EXPLOSION_COLOR = new Color(156, 255, 161, 255);
    static final Color CORE_GLOW_COLOR = new Color(214, 241, 213, 150);
    static final Color EXPLOSION_COLOR = new Color(184, 255, 176, 10);
    static final Color FLASH_GLOW_COLOR = new Color(215, 241, 218, 200);
    static final Color GLOW_COLOR = new Color(175, 255, 172, 50);
    static final String SOUND_ID = "vic_alkonost_explosion";
    static final Vector2f ZERO = new Vector2f();

    // Blast visuals
    static float CoreExplosionRadius = 200f;
    static float CoreExplosionDuration = 1f;
    static float ExplosionRadius = 500f;
    static float ExplosionDuration = 1f;
    static float CoreGlowRadius = 650f;
    static float CoreGlowDuration = 1f;
    static float GlowRadius = 750f;
    static float GlowDuration = 1f;
    static float FlashGlowRadius = 2500f;
    static float FlashGlowDuration = 0.65f;

    static final int NUM_PARTICLES_1 = 15;
    static final int NUM_PARTICLES_2 = 15;
    static final int NUM_PARTICLES_3 = 15;
    static final int NUM_PARTICLES_4 = 15;
    static final int NUM_PARTICLES_5 = 15;
    static final boolean light = Global.getSettings().getModManager().isModEnabled("shaderLib");


    public static void explosion(DamagingProjectileAPI projectile, CombatEngineAPI engine){
        //Global.getLogger(vic_alkonostExplosion.class).info("exploded");
        Vector2f point = projectile.getLocation();

        engine.spawnExplosion(point, ZERO, CORE_EXPLOSION_COLOR, CoreExplosionRadius, CoreExplosionDuration);
        engine.spawnExplosion(point, ZERO, EXPLOSION_COLOR, ExplosionRadius, ExplosionDuration);
        engine.addHitParticle(point, ZERO, CoreGlowRadius, 1f, CoreGlowDuration, CORE_GLOW_COLOR);
        engine.addSmoothParticle(point, ZERO, GlowRadius, 1f, GlowDuration, GLOW_COLOR);
        engine.addHitParticle(point, ZERO, FlashGlowRadius, 1f, FlashGlowDuration, FLASH_GLOW_COLOR);

        for (int x = 0; x < NUM_PARTICLES_1; x++) {
            Vector2f particelVelocity = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(50f, 100f), (float) Math.random() * 360f);
            Vector2f offset = (Vector2f) new Vector2f(particelVelocity).scale(MathUtils.getRandomNumberInRange(0.05f, 1f));
            Global.getCombatEngine().addHitParticle(Vector2f.add(projectile.getLocation(), offset, null),
                    particelVelocity,
                    MathUtils.getRandomNumberInRange(4, 12),
                    1f,
                    MathUtils.getRandomNumberInRange(1.5f, 5.5f),
                    CORE_EXPLOSION_COLOR);
        }

        for (int x = 0; x < NUM_PARTICLES_2; x++) {
            Vector2f particelVelocity = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(75f, 150f), (float) Math.random() * 360f);
            Vector2f offset = (Vector2f) new Vector2f(particelVelocity).scale(MathUtils.getRandomNumberInRange(0.05f, 1f));
            Global.getCombatEngine().addHitParticle(Vector2f.add(projectile.getLocation(), offset, null),
                    particelVelocity,
                    MathUtils.getRandomNumberInRange(4, 12),
                    1f,
                    MathUtils.getRandomNumberInRange(1.5f, 5.5f),
                    CORE_EXPLOSION_COLOR);
        }

        for (int x = 0; x < NUM_PARTICLES_3; x++) {
            Vector2f particelVelocity = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(100f, 200f), (float) Math.random() * 360f);
            Vector2f offset = (Vector2f) new Vector2f(particelVelocity).scale(MathUtils.getRandomNumberInRange(0.05f, 1f));
            Global.getCombatEngine().addHitParticle(Vector2f.add(projectile.getLocation(), offset, null),
                    particelVelocity,
                    MathUtils.getRandomNumberInRange(4, 12),
                    1f,
                    MathUtils.getRandomNumberInRange(1.5f, 5.5f),
                    CORE_EXPLOSION_COLOR);
        }

        for (int x = 0; x < NUM_PARTICLES_4; x++) {
            Vector2f particelVelocity = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(25f, 75f), (float) Math.random() * 360f);
            Vector2f offset = (Vector2f) new Vector2f(particelVelocity).scale(MathUtils.getRandomNumberInRange(0.05f, 1f));
            Global.getCombatEngine().addHitParticle(Vector2f.add(projectile.getLocation(), offset, null),
                    particelVelocity,
                    MathUtils.getRandomNumberInRange(4, 12),
                    1f,
                    MathUtils.getRandomNumberInRange(1.5f, 5.5f),
                    CORE_EXPLOSION_COLOR);
        }

        for (int x = 0; x < NUM_PARTICLES_5; x++) {
            Vector2f particelVelocity = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(150f, 250f), (float) Math.random() * 360f);
            Vector2f offset = (Vector2f) new Vector2f(particelVelocity).scale(MathUtils.getRandomNumberInRange(0.05f, 1f));
            Global.getCombatEngine().addHitParticle(Vector2f.add(projectile.getLocation(), offset, null),
                    particelVelocity,
                    MathUtils.getRandomNumberInRange(4, 12),
                    1f,
                    MathUtils.getRandomNumberInRange(1.5f, 5.5f),
                    CORE_EXPLOSION_COLOR);
        }



        try {
            if (getBoolean("BFGfart")){
                Global.getSoundPlayer().playSound("vic_surprise", 1f, 1f, point, ZERO);
            } else {
                Global.getSoundPlayer().playSound(SOUND_ID, 1f, 1f, point, ZERO);
            }
        } catch (JSONException | IOException ignore) {
        }


        MagicRender.battlespace(
                Global.getSettings().getSprite("fx", "vic_stolas_emp_secondary"),
                point,
                new Vector2f(),
                new Vector2f(200, 200),
                new Vector2f(1500, 1500),
                //angle,
                360 * (float) Math.random(),
                0,
                new Color(207, 255, 95, 50),
                true,
                0,
                0,
                0.75f
        );

        MagicRender.battlespace(
                Global.getSettings().getSprite("fx", "vic_stolas_emp_secondary"),
                point,
                new Vector2f(),
                new Vector2f(192, 192),
                new Vector2f(960, 960),
                //angle,
                360 * (float) Math.random(),
                0,
                new Color(100, 255, 95, 255),
                true,
                0,
                0.1f,
                0.2f
        );
        MagicRender.battlespace(
                Global.getSettings().getSprite("fx", "vic_stolas_emp_secondary"),
                point,
                new Vector2f(),
                new Vector2f(256, 256),
                new Vector2f(550, 550),
                //angle,
                360 * (float) Math.random(),
                0,
                new Color(54, 255, 66, 225),
                true,
                0.2f,
                0.0f,
                0.35f
        );
        MagicRender.battlespace(
                Global.getSettings().getSprite("fx", "vic_stolas_emp_secondary"),
                point,
                new Vector2f(),
                new Vector2f(392, 392),
                new Vector2f(240, 240),
                //angle,
                360 * (float) Math.random(),
                0,
                new Color(159, 210, 132, 100),
                true,
                0.4f,
                0.0f,
                1.6f
        );

        MagicRender.battlespace(
                Global.getSettings().getSprite("fx", "vic_stolas_emp_secondary"),
                point,
                new Vector2f(),
                new Vector2f(392, 392),
                new Vector2f(240, 240),
                //angle,
                360 * (float) Math.random(),
                0,
                new Color(159, 210, 132, 100),
                true,
                0.4f,
                0.0f,
                2.5f
        );

        MagicRender.battlespace(
                Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow"),
                point,
                new Vector2f(),
                new Vector2f(140 * MathUtils.getRandomNumberInRange(0.8f, 1.2f), 1400 * MathUtils.getRandomNumberInRange(0.8f, 1.2f)),
                new Vector2f(),
                360 * (float) Math.random(),
                0,
                new Color(146, 255, 159, 255),
                true,
                0,
                0,
                0.25f,
                0.15f,
                MathUtils.getRandomNumberInRange(0.05f, 0.2f),
                0,
                MathUtils.getRandomNumberInRange(0.8f, 1.2f),
                MathUtils.getRandomNumberInRange(0.2f, 0.6f),
                CombatEngineLayers.CONTRAILS_LAYER
        );

        WaveDistortion wave = new WaveDistortion(point, ZERO);
        wave.setIntensity(1.5f);
        wave.setSize(750f);
        wave.flip(true);
        wave.setLifetime(0f);
        wave.fadeOutIntensity(1f);
        wave.setLocation(projectile.getLocation());
        DistortionShader.addDistortion(wave);

        if (light) {
            vic_graphicLibEffects.CustomRippleDistortion(
                    point,
                    ZERO,
                    530,
                    5,
                    false,
                    0,
                    360,
                    1f,
                    0.1f,
                    0.25f,
                    0.5f,
                    0.70f,
                    0f
            );
        }


        Vector2f nebulaSpeed1 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(projectile.getAngularVelocity() + MathUtils.getRandomNumberInRange(0f, 90f)).scale(MathUtils.getRandomNumberInRange(25f, 50f));
        Vector2f nebulaSpeed2 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(projectile.getAngularVelocity() + MathUtils.getRandomNumberInRange(90f, 180f)).scale(MathUtils.getRandomNumberInRange(25f, 50f));
        Vector2f nebulaSpeed3 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(projectile.getAngularVelocity() + MathUtils.getRandomNumberInRange(180f, 270f)).scale(MathUtils.getRandomNumberInRange(25f, 50f));
        Vector2f nebulaSpeed4 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(projectile.getAngularVelocity() + MathUtils.getRandomNumberInRange(270f, 360f)).scale(MathUtils.getRandomNumberInRange(25f, 50f));

        Global.getCombatEngine().addNebulaSmokeParticle(point, nebulaSpeed1, 200f, 2.5f, 0.2f, 0.2f, (3.5f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(186, 255, 235, 100));
        Global.getCombatEngine().addNebulaSmokeParticle(point, nebulaSpeed2, 200f, 2.5f, 0.2f, 0.2f, (3.5f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(135, 255, 213, 100));
        Global.getCombatEngine().addNebulaSmokeParticle(point, nebulaSpeed3, 200f, 2.5f, 0.2f, 0.2f, (3.5f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(78, 212, 170, 100));
        Global.getCombatEngine().addNebulaSmokeParticle(point, nebulaSpeed4, 200f, 2.5f, 0.2f, 0.2f, (3.5f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(157, 255, 174, 100));



    }

}
