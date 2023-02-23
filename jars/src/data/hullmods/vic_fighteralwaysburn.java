package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import org.lazywizard.lazylib.MathUtils;

import java.awt.Color;

public class vic_fighteralwaysburn extends BaseHullMod {
    public vic_fighteralwaysburn() {
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        return null;
    }


    public boolean isApplicableToShip(ShipAPI ship) {
        return true;
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        if (!ship.isLiftingOff() && !ship.isLanding() && !ship.getWing().getSourceShip().isPullBackFighters()) {
            ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
            ship.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT);
            ship.blockCommandForOneFrame(ShipCommand.STRAFE_RIGHT);
            ship.blockCommandForOneFrame(ShipCommand.DECELERATE);
            ship.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS);
        }
    }
}