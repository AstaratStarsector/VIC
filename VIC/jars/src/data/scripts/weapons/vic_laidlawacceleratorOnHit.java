package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import data.scripts.util.MagicRender;
import data.scripts.utilities.vic_graphicLibEffects;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

import static data.scripts.utilities.vic_graphicLibEffects.CustomRippleDistortion;

public class vic_laidlawacceleratorOnHit implements OnHitEffectPlugin {

    private boolean light=false;

    private static final float FORCE_MULT = 0f; // force applied = base damage amount * this

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
        wave.setIntensity(1.5f);
        wave.setSize(75f);
        wave.flip(true);
        wave.setLifetime(0f);
        wave.fadeOutIntensity(0.66f);
        wave.setLocation(projectile.getLocation());
        DistortionShader.addDistortion(wave);



        if(Global.getSettings().getModManager().isModEnabled("shaderLib")){
            light=true;
        }

        if(light) {
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



        engine.spawnExplosion(point,
                new Vector2f(0,0),
                new Color(255, 255, 255,255),
                25f,
                0.3f);

        engine.spawnExplosion(point,
                new Vector2f(0,0),
                new Color(0, 255, 225,75),
                50f,
                0.75f);


        float angle = 360*(float)Math.random();

        MagicRender.battlespace(
                Global.getSettings().getSprite("fx","vic_laidlawExplosion"),
                point,
                new Vector2f(),
                new Vector2f(48,48),
                new Vector2f(200,200),
                //angle,
                360*(float)Math.random(),
                0,
                new Color(255,200,200,255),
                true,
                0,
                0.1f,
                0.15f
        );
        MagicRender.battlespace(
                Global.getSettings().getSprite("fx","vic_laidlawExplosion"),
                point,
                new Vector2f(),
                new Vector2f(64,64),
                new Vector2f(100,100),
                //angle,
                360*(float)Math.random(),
                0,
                new Color(255,225,225,225),
                true,
                0.2f,
                0.0f,
                0.3f
        );
        MagicRender.battlespace(
                Global.getSettings().getSprite("fx","vic_laidlawExplosion"),
                point,
                new Vector2f(),
                new Vector2f(98,98),
                new Vector2f(50,50),
                //angle,
                360*(float)Math.random(),
                0,
                new Color(255,255,255,200),
                true,
                0.4f,
                0.0f,
                0.6f
        );



    }
}
