package data.scripts.weapons.gateBreaker;

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
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

import static com.fs.starfarer.api.util.Misc.ZERO;

public class vic_gatebreakerAutocannonOnHit implements OnHitEffectPlugin {

    private static final Color PARTICLE_COLOR = new Color(125, 175, 255, 150);
    private static final Color FLASH_COLOR = new Color(255, 209, 173);

    private boolean light = false;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {


        engine.addSmoothParticle(point, ZERO, 150f, 0.5f, 0.1f, PARTICLE_COLOR);
        engine.addHitParticle(point, ZERO, 100f, 0.5f, 0.25f, FLASH_COLOR);

        MagicRender.battlespace(
                Global.getSettings().getSprite("fx","vic_stolas_emp_secondary"),
                point,
                ZERO,
                new Vector2f(15,15),
                new Vector2f(500,500),
                //angle,
                360*(float)Math.random(),
                0,
                new Color(200, 200, 255, 255),
                true,
                0,
                0f,
                0.2f
        );

        WaveDistortion wave = new WaveDistortion(point, ZERO);
        wave.setIntensity(1.5f);
        wave.setSize(45f);
        wave.flip(true);
        wave.setLifetime(0f);
        wave.fadeOutIntensity(0.2f);
        wave.setLocation(projectile.getLocation());
        DistortionShader.addDistortion(wave);

        if (Global.getSettings().getModManager().isModEnabled("shaderLib")) {
            light = true;
        }

        if (light) {
            vic_graphicLibEffects.CustomRippleDistortion(
                    point,
                    ZERO,
                    50,
                    3,
                    false,
                    0,
                    360,
                    1f,
                    0f,
                    0f,
                    0.5f,
                    0.2f,
                    0f
            );
        }

    }
}