package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class vic_devostatorFire implements EveryFrameWeaponEffectPlugin {
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(engine.isPaused() || weapon.getShip().getOriginalOwner()==-1){return;}
        
        if(MagicRender.screenCheck(0.25f, weapon.getLocation())){
            if(weapon.getChargeLevel()==1){
                
                for(DamagingProjectileAPI p : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 200)){
                    
                    if(p.getWeapon()!=weapon)continue;
                    
                    p.getVelocity().scale(MathUtils.getRandomNumberInRange(0.9f, 1.1f));

                }
            }
        }
    }
}