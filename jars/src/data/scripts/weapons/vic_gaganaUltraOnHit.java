package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicRender;
import data.scripts.utilities.vic_graphicLibEffects;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

import static com.fs.starfarer.api.util.Misc.ZERO;

public class vic_gaganaUltraOnHit implements OnHitEffectPlugin {

    private static final Color FLASH_FRINGE = new Color(255, 68, 68, 150);
    private static final Color FLASH_CORE = new Color(246, 164, 94);

    private boolean light = false;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {


        engine.addSmoothParticle(point, ZERO, 500, 1f, 0.6f, FLASH_CORE);
        engine.addSmoothParticle(point, ZERO, 750, 1f, 0.4f, FLASH_FRINGE);

        MagicRender.battlespace(
                Global.getSettings().getSprite("fx","vic_stolas_emp_secondary"),
                point,
                ZERO,
                new Vector2f(150,150),
                new Vector2f(250,250),
                //angle,
                360*(float)Math.random(),
                0,
                new Color(255, 0, 30, 109),
                true,
                0,
                0f,
                0.6f
        );

        for (int I = 0; I < 7; I++){
            float shrapnelDir = projectile.getAngularVelocity() + MathUtils.getRandomNumberInRange(-240f, -60f);
            float shrapnelDir2 = VectorUtils.getAngle(projectile.getDamageTarget().getLocation(), projectile.getLocation());
            DamagingProjectileAPI gaganaUltraShrapnel = (DamagingProjectileAPI) Global.getCombatEngine().spawnProjectile(projectile.getWeapon().getShip(), projectile.getWeapon(), "vic_raum_weapon_gagana_ultra_shrapnel", projectile.getLocation(),
                    shrapnelDir2 + MathUtils.getRandomNumberInRange(-90, 90), null);
            gaganaUltraShrapnel.getVelocity().scale(MathUtils.getRandomNumberInRange(0.25f, 0.75f));
            gaganaUltraShrapnel.setCollisionClass(CollisionClass.valueOf("NONE"));
        }

        WaveDistortion wave = new WaveDistortion(point, ZERO);
        wave.setIntensity(1f);
        wave.setSize(225f);
        wave.flip(true);
        wave.setLifetime(0f);
        wave.fadeOutIntensity(0.2f);
        wave.setLocation(projectile.getLocation());
        DistortionShader.addDistortion(wave);

        Global.getSoundPlayer().playSound("vic_gagana_ultra_hit", 1f + MathUtils.getRandomNumberInRange(-0.1f, 0.1f), 1f, point, new Vector2f());

        if (Global.getSettings().getModManager().isModEnabled("shaderLib")) {
            light = true;
        }

        if (light) {
            vic_graphicLibEffects.CustomRippleDistortion(
                    point,
                    ZERO,
                    200,
                    1.5f,
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