package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_psotnikOnHit implements OnHitEffectPlugin, OnFireEffectPlugin {


    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (!shieldHit && !projectile.isFading() && target instanceof ShipAPI) {
            if (Math.random() > 0.80f) {
                engine.spawnEmpArc(projectile.getSource(),
                        point,
                        target,
                        target,
                        DamageType.FRAGMENTATION,
                        0,
                        50,
                        3000,
                        "tachyon_lance_emp_impact",
                        4,
                        Color.WHITE,
                        Color.CYAN);
            }
        }
    }

    int count = 0;
    DamagingProjectileAPI lastPorj = null;
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

        if (count == 0){
            lastPorj = projectile;
            count += 1;
        } else if (count == 1){
            projectile.setAngularVelocity(lastPorj.getAngularVelocity());
            projectile.getVelocity().set(lastPorj.getVelocity());
            projectile.setFacing(lastPorj.getFacing());
            count = 0;
        }
    }
}
