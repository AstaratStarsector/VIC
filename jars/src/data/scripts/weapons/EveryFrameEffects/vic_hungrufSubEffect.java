package data.scripts.weapons.EveryFrameEffects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

public class vic_hungrufSubEffect extends BaseEveryFrameCombatPlugin {

    float time = 0;

    final MissileAPI missile;
    final CombatEngineAPI engine;

    // -- Time information ---------------------------------------------------
    final float explosionDur = 2f;
    final float NEBULA_RAMPUP = 0.1f;
    // -- Color information --------------------------------------------------
    final Color explosionColor = new Color(255, 125, 60, 200);
    final Color smokeColor = new Color(120, 60, 30, 100);
    // -- Smoke particle information -----------------------------------------
    final float smokeEndMult = 15f;
    final float explosionRange = 50f;
    IntervalUtil timer = new IntervalUtil(0.1f,0.25f);
    DamagingExplosionSpec explosion;

    public vic_hungrufSubEffect(MissileAPI missile) {
        this.missile = missile;
        this.engine = Global.getCombatEngine();
        int damage = MathUtils.getRandomNumberInRange(10,20);
        explosion = new DamagingExplosionSpec(0.07f,
                20,
                10,
                damage,
                damage * 0.5f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                3,
                3,
                0.5f,
                0,
                explosionColor,
                explosionColor
        );
        explosion.setShowGraphic(false);
    }

    public void advance(float amount, List<InputEventAPI> events) {
        if (engine.isPaused()) return;
        time += amount;
        if (time >= 0.25f) {
            timer.advance(amount);
            if (timer.intervalElapsed()){
                // do the base VFX
                //engine.addNebulaSmokeParticle(missile.getLocation(), new Vector2f(), NEBULA_SIZE, NEBULA_SIZE_MULT, NEBULA_RAMPUP, 0.3f, explosionDur, smokeColor);
                //engine.addNebulaSmokeParticle(missile.getLocation(), new Vector2f(), NEBULA_SIZE, NEBULA_SIZE_MULT, NEBULA_RAMPUP, 0.6f, explosionDur, smokeColor);
                //smaller smoke
                int explosionCount = MathUtils.getRandomNumberInRange(2, 4);
                for (int i = 0; i <= (explosionCount - 1); i++)
                {
                    float effectSize = 20f * (0.75f + (float) Math.random() * 0.5f);
                    Vector2f random_point = new Vector2f(MathUtils.getRandomPointInCircle(missile.getLocation(), explosionRange));
                    engine.spawnExplosion(random_point, new Vector2f(), explosionColor, effectSize * 2, 0.07f);
                    explosion.setCoreRadius(effectSize);
                    explosion.setRadius(effectSize * 2);
                    engine.spawnDamagingExplosion(explosion, missile.getSource(), random_point);
                    engine.addNebulaSmokeParticle(random_point, new Vector2f(), effectSize / 2, smokeEndMult, NEBULA_RAMPUP, 0.3f, explosionDur, smokeColor);
                    Global.getSoundPlayer().playSound(
                            "vic_hungruf_pop",
                            1,
                            1,
                            random_point,
                            new Vector2f()
                    );
                }
            }
        }

        if (!engine.isEntityInPlay(missile) || missile.didDamage()) {
            engine.removePlugin(this);
        }
    }

}
