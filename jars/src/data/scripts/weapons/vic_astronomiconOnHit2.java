package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_astronomiconOnHit2 implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target instanceof ShipAPI) {
            float damage = projectile.getDamageAmount() * 0.1f;
            for (int i = 0; i < 10; i++) {
                Global.getLogger(vic_astronomiconOnHit2.class).info(damage + "/" + i);
                if (shieldHit) {
                    engine.spawnEmpArcPierceShields(projectile.getSource(), point, target,
                            target,
                            DamageType.ENERGY,
                            damage*2.5f,
                            0f,
                            1000,
                            null,
                            10,
                            new Color(MathUtils.getRandomNumberInRange(160, 200), MathUtils.getRandomNumberInRange(200, 255), MathUtils.getRandomNumberInRange(200, 255), 255), //Central color
                            new Color(MathUtils.getRandomNumberInRange(0, 30), MathUtils.getRandomNumberInRange(180, 220), MathUtils.getRandomNumberInRange(220, 255), 255)
                    );
                } else {
                    engine.spawnEmpArc(projectile.getSource(), point, target,
                            target,
                            DamageType.ENERGY,
                            damage,
                            0f,
                            1000,
                            null,
                            10,
                            new Color(MathUtils.getRandomNumberInRange(160, 200), MathUtils.getRandomNumberInRange(200, 255), MathUtils.getRandomNumberInRange(200, 255), 255), //Central color
                            new Color(MathUtils.getRandomNumberInRange(0, 30), MathUtils.getRandomNumberInRange(180, 220), MathUtils.getRandomNumberInRange(220, 255), 255)
                    );

                }
            }

            Global.getSoundPlayer().playSound("vic_astronomicon_hit_small", 1f + MathUtils.getRandomNumberInRange(-0.1f, 0.1f), 1f, point, new Vector2f());

            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "vic_stolas_emp_secondary"),
                    point,
                    new Vector2f(),
                    new Vector2f(0, 0),
                    new Vector2f(750, 750),
                    //angle,
                    360 * (float) Math.random(),
                    0,
                    new Color(25, 255, 225, 150),
                    true,
                    0,
                    0f,
                    0.5f
            );

            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "vic_laidlawExplosion"),
                    point,
                    new Vector2f(),
                    new Vector2f(48, 48),
                    new Vector2f(240, 240),
                    //angle,
                    360 * (float) Math.random(),
                    0,
                    new Color(255, 255, 255, 255),
                    true,
                    0,
                    0.1f,
                    0.3f
            );

            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "vic_laidlawExplosion"),
                    point,
                    new Vector2f(),
                    new Vector2f(64, 64),
                    new Vector2f(250, 250),
                    //angle,
                    360 * (float) Math.random(),
                    0,
                    new Color(0, 255, 156, 255),
                    true,
                    0.2f,
                    0.0f,
                    0.4f
            );

            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "vic_laidlawExplosion"),
                    point,
                    new Vector2f(),
                    new Vector2f(125, 125),
                    new Vector2f(75, 75),
                    //angle,
                    360 * (float) Math.random(),
                    0,
                    new Color(149, 35, 35, 200),
                    true,
                    0.35f,
                    0.0f,
                    1f
            );

            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "vic_laidlawExplosion"),
                    point,
                    new Vector2f(),
                    new Vector2f(100, 100),
                    new Vector2f(60, 60),
                    //angle,
                    360 * (float) Math.random(),
                    0,
                    new Color(0, 255, 194, 100),
                    true,
                    0.35f,
                    0.0f,
                    1.5f
            );

        }
    }

}

