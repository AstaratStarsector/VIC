package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

public class vic_commissionedCrewBundle extends BaseHullMod {

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        super.applyEffectsAfterShipCreation(ship, id);
        if (!(ship.getVariant().hasHullMod("vic_brandengineupgrades") || ship.getVariant().hasHullMod("vic_ambitiousBravado"))) {
            ship.getVariant().addMod("vic_ambitiousBravado");
            ship.getVariant().addMod("vic_brandengineupgrades");
        }
        ship.getVariant().removeMod("vic_commissionedCrewBundle");
    }

}