package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AsteroidAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.MagicRender;
import data.scripts.utilities.vic_graphicLibEffects;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.fs.starfarer.api.util.Misc.ZERO;

public class vic_alkonostOnHit implements OnHitEffectPlugin {
    private static final Color CORE_EXPLOSION_COLOR = new Color(156, 255, 161, 255);
    private static final Color CORE_GLOW_COLOR = new Color(214, 241, 213, 150);
    private static final Color EXPLOSION_COLOR = new Color(184, 255, 176, 10);
    private static final Color FLASH_GLOW_COLOR = new Color(215, 241, 218, 200);
    private static final Color GLOW_COLOR = new Color(175, 255, 172, 50);
    private static final Color ARC_FRINGE_COLOR = new Color(52, 255, 62);
    private static final Color ARC_CORE_COLOR = new Color(213, 255, 212);
    private static final String SOUND_ID = "vic_alkonost_explosion";
    private static final Vector2f ZERO = new Vector2f();

    private static final int NUM_PARTICLES_1 = 25;
    private static final int NUM_PARTICLES_2 = 25;
    private static final int NUM_ARCS = 10;
    private boolean light = false;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (projectile.didDamage() && !(target instanceof MissileAPI)) {

            // Blast visuals
            float CoreExplosionRadius = 200f;
            float CoreExplosionDuration = 1f;
            float ExplosionRadius = 500f;
            float ExplosionDuration = 1f;
            float CoreGlowRadius = 650f;
            float CoreGlowDuration = 1f;
            float GlowRadius = 750f;
            float GlowDuration = 1f;
            float FlashGlowRadius = 1250f;
            float FlashGlowDuration = 0.05f;

            engine.spawnExplosion(point, ZERO, CORE_EXPLOSION_COLOR, CoreExplosionRadius, CoreExplosionDuration);
            engine.spawnExplosion(point, ZERO, EXPLOSION_COLOR, ExplosionRadius, ExplosionDuration);
            engine.addHitParticle(point, ZERO, CoreGlowRadius, 1f, CoreGlowDuration, CORE_GLOW_COLOR);
            engine.addSmoothParticle(point, ZERO, GlowRadius, 1f, GlowDuration, GLOW_COLOR);
            engine.addHitParticle(point, ZERO, FlashGlowRadius, 1f, FlashGlowDuration, FLASH_GLOW_COLOR);

            for (int x = 0; x < NUM_PARTICLES_1; x++) {
                engine.addHitParticle(point,
                        MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(50f, 150f), (float) Math.random() * 360f),
                        MathUtils.getRandomNumberInRange(4, 12), 1f, MathUtils.getRandomNumberInRange(0.5f, 2f), CORE_EXPLOSION_COLOR);
            }

            for (int x = 0; x < NUM_PARTICLES_2; x++) {
                engine.addHitParticle(point,
                        MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(25f, 75f), (float) Math.random() * 360f),
                        MathUtils.getRandomNumberInRange(4, 12), 1f, MathUtils.getRandomNumberInRange(0.5f, 2f), CORE_EXPLOSION_COLOR);
            }


            Global.getSoundPlayer().playSound(SOUND_ID, 1f, 1f, point, ZERO);


            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","vic_stolas_emp_secondary"),
                    point,
                    new Vector2f(),
                    new Vector2f(200,200),
                    new Vector2f(1500,1500),
                    //angle,
                    360*(float)Math.random(),
                    0,
                    new Color(207, 255, 95,50),
                    true,
                    0,
                    0,
                    0.75f
            );

            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","vic_stolas_emp_secondary"),
                    point,
                    new Vector2f(),
                    new Vector2f(192,192),
                    new Vector2f(960,960),
                    //angle,
                    360*(float)Math.random(),
                    0,
                    new Color(100, 255, 95,255),
                    true,
                    0,
                    0.1f,
                    0.2f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","vic_stolas_emp_secondary"),
                    point,
                    new Vector2f(),
                    new Vector2f(256,256),
                    new Vector2f(550,550),
                    //angle,
                    360*(float)Math.random(),
                    0,
                    new Color(54, 255, 66,225),
                    true,
                    0.2f,
                    0.0f,
                    0.35f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","vic_stolas_emp_secondary"),
                    point,
                    new Vector2f(),
                    new Vector2f(392,392),
                    new Vector2f(240,240),
                    //angle,
                    360*(float)Math.random(),
                    0,
                    new Color(159, 210, 132,200),
                    true,
                    0.4f,
                    0.0f,
                    1.6f
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

            if (Global.getSettings().getModManager().isModEnabled("shaderLib")) {
                light = true;
            }

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

            Global.getCombatEngine().addNebulaSmokeParticle(point, nebulaSpeed1, 200f, 2f, 0.2f, 0.2f, (2.5f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(186, 255, 235, 100));
            Global.getCombatEngine().addNebulaSmokeParticle(point, nebulaSpeed2, 200f, 2f, 0.2f, 0.2f, (2.5f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(135, 255, 213, 100));
            Global.getCombatEngine().addNebulaSmokeParticle(point, nebulaSpeed3, 200f, 2f, 0.2f, 0.2f, (2.5f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(78, 212, 170, 100));
            Global.getCombatEngine().addNebulaSmokeParticle(point, nebulaSpeed4, 200f, 2f, 0.2f, 0.2f, (2.5f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(157, 255, 174, 100));



            // Arcing stuff
            List<CombatEntityAPI> validTargets = new ArrayList<CombatEntityAPI>();
            for (CombatEntityAPI entityToTest : CombatUtils.getEntitiesWithinRange(point, 500)) {
                if (entityToTest instanceof ShipAPI || entityToTest instanceof AsteroidAPI || entityToTest instanceof MissileAPI) {
                    //Phased targets, and targets with no collision, are ignored
                    if (entityToTest instanceof ShipAPI) {
                        if (((ShipAPI) entityToTest).isPhased()) {
                            continue;
                        }
                    }
                    if (entityToTest.getCollisionClass().equals(CollisionClass.NONE)) {
                        continue;
                    }

                    validTargets.add(entityToTest);
                }
            }

            for (int x = 0; x < NUM_ARCS; x++) {
                //If we have no valid targets, zap a random point near us
                if (validTargets.isEmpty()) {
                    validTargets.add(new SimpleEntity(MathUtils.getRandomPointInCircle(point, 500)));
                }

                float bonusDamage = projectile.getDamageAmount()*0.025f;

                //And finally, fire at a random valid target
                CombatEntityAPI arcTarget = validTargets.get(MathUtils.getRandomNumberInRange(0, validTargets.size() - 1));

                if (arcTarget == target && shieldHit) {

                    Global.getCombatEngine().applyDamage(target,point,MathUtils.getRandomNumberInRange(0.8f, 1.2f) * bonusDamage,DamageType.ENERGY,MathUtils.getRandomNumberInRange(0.8f, 1.2f) * bonusDamage,
                            false,false,projectile.getSource());

                } else {

                    Global.getCombatEngine().spawnEmpArc(projectile.getSource(), point, projectile.getSource(), arcTarget,
                            DamageType.ENERGY, //Damage type
                            MathUtils.getRandomNumberInRange(0.8f, 1.2f) * bonusDamage, //Damage
                            MathUtils.getRandomNumberInRange(0.8f, 1.2f) * bonusDamage, //Emp
                            100000f, //Max range
                            "vic_alkonost_emp_arc", //Impact sound
                            10f, // thickness of the lightning bolt
                            ARC_CORE_COLOR, //Central color
                            ARC_FRINGE_COLOR //Fringe Color
                    );

                }
            }
        }
    }
}
