package data.scripts.weapons.decos;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.util.Random;

public class vic_decalSelector implements EveryFrameWeaponEffectPlugin {

    private boolean runOnce;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine == null) {
            return;
        }
        
        ShipAPI ship = weapon.getShip();
        
        if (!runOnce) {
            if (ship != null && ship.getFleetMember() != null){
                int maxFrames = weapon.getAnimation().getNumFrames() - 1;
                weapon.getAnimation().setFrame(new Random(ship.getFleetMember().getShipName().hashCode()).nextInt(maxFrames));
            }
            runOnce = true;
        }
    }
}
