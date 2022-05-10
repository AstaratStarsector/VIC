package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

public class vic_overloadProtection extends BaseHullMod {

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship.getFluxTracker().isOverloaded()){
            ship.getFluxTracker().stopOverload();
            ship.setDefenseDisabled(true);
        }
        if (ship.getFluxLevel() == 0) ship.setDefenseDisabled(false);
    }
}
