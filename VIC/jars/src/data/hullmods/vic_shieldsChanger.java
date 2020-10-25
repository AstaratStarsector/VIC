package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;

public class vic_shieldsChanger extends BaseHullMod {

    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship.getFullTimeDeployed() > 0.5f) return;
        if (Global.getCombatEngine() != null) {
            CombatEngineAPI engine = Global.getCombatEngine();
            if (!engine.isPaused() && ship.getShield() != null) {
                ShieldAPI shipShield = ship.getShield();
                float radius = shipShield.getRadius();
                String innersprite;
                String outersprite;
                if (radius >= 256.0F) {
                    innersprite = "graphics/fx/shield/vic_shields256.png";
                    outersprite = "graphics/fx/shield/vic_shields256ring.png";
                } else if (radius >= 128.0F) {
                    innersprite = "graphics/fx/shield/vic_shields128.png";
                    outersprite = "graphics/fx/shield/vic_shields128ring.png";
                } else {
                    innersprite = "graphics/fx/shield/vic_shields64.png";
                    outersprite = "graphics/fx/shield/vic_shields64ring.png";
                }
                shipShield.setRadius(radius, innersprite, outersprite);
            }

        }
    }
}




