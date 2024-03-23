package data.scripts.weapons.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_hungrufBombAI implements MissileAIPlugin {

    final CombatEngineAPI engine;

    boolean exploding = false;
    float
            timeExploding = 0,
            maxTimeExploding = 1f,
            proxRange = 50;

    MissileAPI missile;
    final float explosionDur = 2f;
    final float NEBULA_RAMPUP = 0.1f;
    // -- Color information --------------------------------------------------
    final Color explosionColor = new Color(255, 125, 60, 200);
    final Color smokeColor = new Color(120, 60, 30, 100);
    // -- Smoke particle information -----------------------------------------
    final float smokeEndMult = 15f;
    final float explosionRange = 50f;
    DamagingExplosionSpec miniExplosion;
    DamagingExplosionSpec explosion;

    IntervalUtil timer = new IntervalUtil(0.05f,0.1f);

    public vic_hungrufBombAI(MissileAPI missile){
        this.missile = missile;
        this.engine = Global.getCombatEngine();
        int damage = MathUtils.getRandomNumberInRange(10,20);
        miniExplosion = new DamagingExplosionSpec(0.07f,
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
        proxRange = 150;
        miniExplosion.setShowGraphic(false);
        maxTimeExploding = proxRange/missile.getMaxSpeed() * 2f;
    }



    @Override
    public void advance(float amount) {
        if (!exploding && !AIUtils.getNearbyEnemies(missile,proxRange).isEmpty()){
            exploding = true;
        }
        if (timeExploding >= maxTimeExploding){
            missile.explode();

            //lens flare
            MagicRender.battlespace(
                    Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow"),
                    new Vector2f(missile.getLocation()),
                    new Vector2f(),
                    new Vector2f(70 * MathUtils.getRandomNumberInRange(0.8f, 1.2f), 700 * MathUtils.getRandomNumberInRange(0.8f, 1.2f)),
                    new Vector2f(),
                    MathUtils.getRandomNumberInRange(0,360),
                    0,
                    new Color(MathUtils.getRandomNumberInRange(200, 255), MathUtils.getRandomNumberInRange(50, 86), MathUtils.getRandomNumberInRange(0, 35), 255),
                    true,
                    0,
                    0,
                    0.5f,
                    0.15f,
                    MathUtils.getRandomNumberInRange(0.05f, 0.2f),
                    0,
                    MathUtils.getRandomNumberInRange(0.4f, 0.6f),
                    MathUtils.getRandomNumberInRange(0.1f, 0.3f),
                    CombatEngineLayers.CONTRAILS_LAYER
            );

            Global.getSoundPlayer().playSound(
                    "vic_hungruf_explosion",
                    1,
                    1,
                    new Vector2f(missile.getLocation()),
                    new Vector2f()
            );


            engine.removeEntity(missile);
        }
        if (exploding){
            timeExploding += amount;
            timer.advance(amount);
            if (timer.intervalElapsed()){
                int explosionCount = MathUtils.getRandomNumberInRange(2, 4);
                for (int i = 0; i <= (explosionCount - 1); i++)
                {
                    int damage = MathUtils.getRandomNumberInRange(10,20);
                    miniExplosion.setMaxDamage(damage);
                    miniExplosion.setMinDamage(damage);
                    float effectSize = 20f * (0.75f + (float) Math.random() * 0.5f);
                    Vector2f random_point = new Vector2f(MathUtils.getRandomPointInCircle(missile.getLocation(), explosionRange));
                    engine.spawnExplosion(random_point, new Vector2f(), explosionColor, effectSize * 2, 0.07f);
                    miniExplosion.setCoreRadius(effectSize);
                    miniExplosion.setRadius(effectSize * 2);
                    engine.spawnDamagingExplosion(miniExplosion, missile.getSource(), random_point);
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
        if (missile.isFizzling()){
            missile.explode();
            engine.removeEntity(missile);
        }
    }
}
