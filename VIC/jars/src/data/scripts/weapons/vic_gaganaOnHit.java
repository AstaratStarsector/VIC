package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import org.lwjgl.util.vector.Vector2f;

public class vic_gaganaOnHit implements OnHitEffectPlugin {
    
    private final String ID="vic_gagana_sub";
    
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, CombatEngineAPI engine) {
          
        if(!projectile.isFading()){
            if(!shieldHit){
                ((vic_gaganaScript) projectile.getWeapon().getEffectPlugin()).putHIT(target);
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
}
