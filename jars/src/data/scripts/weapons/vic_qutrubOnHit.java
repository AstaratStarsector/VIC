package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import data.scripts.weapons.ai.vic_qutrubStuckAI;
import org.lwjgl.util.vector.Vector2f;

public class vic_qutrubOnHit implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (!shieldHit && target instanceof ShipAPI) {
            String ID = "vic_qutrub_sub";
            MissileAPI missile = (MissileAPI) engine.spawnProjectile(
                    projectile.getSource(),
                    projectile.getWeapon(),
                    ID,
                    point,
                    projectile.getFacing(),
                    target.getVelocity()
            );
            //((vic_qutrubStuckAI) missile.getMissileAI()).setTarget(target);
        }
    }
}
