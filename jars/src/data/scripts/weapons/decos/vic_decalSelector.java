package data.scripts.weapons.decos;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

import java.util.Date;
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
                String shipName = ship.getFleetMember().getShipName();
                if (shipName != null){
                    weapon.getAnimation().setFrame(new Random(shipName.hashCode()).nextInt(maxFrames));
                } else {
                    weapon.getAnimation().setFrame(new Random(new Date().getHours()).nextInt(maxFrames));
                }
            }
            runOnce = true;
        }
    }
}
