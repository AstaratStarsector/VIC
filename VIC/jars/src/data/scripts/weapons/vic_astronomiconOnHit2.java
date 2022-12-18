package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_astronomiconOnHit2 implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target instanceof ShipAPI) {
            float fluxLevel = ((ShipAPI) target).getFluxLevel();
            float damage = projectile.getDamageAmount() * 0.1f;
            for (int i = 0; i < 10; i++) {
                Global.getLogger(vic_astronomiconOnHit2.class).info(damage + "/" + i);
                if (fluxLevel >= MathUtils.getRandomNumberInRange(0f, 0.5f)) {
                    engine.spawnEmpArcPierceShields(projectile.getSource(), point, target,
                            target,
                            DamageType.ENERGY,
                            damage,
                            damage,
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
                            damage,
                            1000,
                            null,
                            10,
                            new Color(MathUtils.getRandomNumberInRange(160, 200), MathUtils.getRandomNumberInRange(200, 255), MathUtils.getRandomNumberInRange(200, 255), 255), //Central color
                            new Color(MathUtils.getRandomNumberInRange(0, 30), MathUtils.getRandomNumberInRange(180, 220), MathUtils.getRandomNumberInRange(220, 255), 255)
                    );

                }
            }

        }
    }

}

