package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

public class vic_qutrubOnHit implements OnHitEffectPlugin {

    private final String ID = "vic_qutrub_sub";

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (!shieldHit && target instanceof ShipAPI) {
            ((vic_qutrubScript) projectile.getWeapon().getEffectPlugin()).putHIT(target);
            engine.spawnProjectile(
                    projectile.getSource(),
                    projectile.getWeapon(),
                    ID,
                    point,
                    projectile.getFacing(),
                    target.getVelocity()
            );
        }
    }
}
