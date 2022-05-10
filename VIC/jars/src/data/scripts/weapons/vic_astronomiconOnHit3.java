package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

import static data.scripts.weapons.vic_astronomiconOnHit2.explosion;


public class vic_astronomiconOnHit3 implements OnHitEffectPlugin {
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target instanceof MissileAPI) return;

        explosion(point, engine, projectile);

        float damage = projectile.getDamageAmount() * 0.1f;

        for (int x = 0; x < 5; x++) {
            engine.spawnEmpArc(projectile.getSource(),
                    point,
                    projectile.getSource(),
                    target,
                    DamageType.ENERGY, //Damage type
                    damage, //Damage
                    damage, //Emp
                    100000f, //Max range
                    "tachyon_lance_emp_impact", //Impact sound
                    10f, // thickness of the lightning bolt
                    new Color(MathUtils.getRandomNumberInRange(160, 200), MathUtils.getRandomNumberInRange(200, 255), MathUtils.getRandomNumberInRange(200, 255), 255), //Central color
                    new Color(MathUtils.getRandomNumberInRange(0, 30), MathUtils.getRandomNumberInRange(180, 220), MathUtils.getRandomNumberInRange(220, 255), 255) //Fringe Color
            );

        }
    }
}
