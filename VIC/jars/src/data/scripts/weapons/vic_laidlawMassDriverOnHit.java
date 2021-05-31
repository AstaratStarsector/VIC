package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import data.scripts.util.MagicRender;
import data.scripts.utilities.vic_graphicLibEffects;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_laidlawMassDriverOnHit implements OnHitEffectPlugin {

    private boolean light=false;

    private static final Vector2f ZERO = new Vector2f();

    private final DamagingExplosionSpec explosion = new DamagingExplosionSpec(0.05f,
            35,
            12.5f,
            200,
            75f,
            CollisionClass.PROJECTILE_FF,
            CollisionClass.PROJECTILE_FIGHTER,
            2,
            2,
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
        }
        WaveDistortion wave = new WaveDistortion(point, ZERO);
        wave.setIntensity(0.75f);
        wave.setSize(35f);
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
                    35,
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
                12.5f,
                0.3f);

        engine.spawnExplosion(point,
                new Vector2f(0,0),
                new Color(0, 255, 225,75),
                25f,
                0.75f);


        float angle = 360*(float)Math.random();

        MagicRender.battlespace(
                Global.getSettings().getSprite("fx","vic_laidlawExplosion"),
                point,
                new Vector2f(),
                new Vector2f(24,24),
                new Vector2f(100,100),
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
                new Vector2f(32,32),
                new Vector2f(50,50),
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
                new Vector2f(49,49),
                new Vector2f(25,25),
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
