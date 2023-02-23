package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.combat.entities.Ship;

public class vic_shieldsChanger extends BaseHullMod {

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        super.applyEffectsAfterShipCreation(ship, id);
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




