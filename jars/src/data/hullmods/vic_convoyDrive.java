package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.loading.WeaponGroupSpec;

import java.util.ArrayList;
import java.util.List;

public class vic_convoyDrive extends BaseHullMod {

    final int zFluxBonus = 35;
    final float rangeBonus = 0.25f;
    final float altModRangeReduction = 0.5f;

    final List<String> allowedShips = new ArrayList<>();
    {
        allowedShips.add("vic_kobal");
        allowedShips.add("vic_samael");
        allowedShips.add("vic_thamuz");
    }


    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        boolean hasFortress = false;
        if (stats.getEntity() instanceof ShipAPI) hasFortress = ((ShipAPI) stats.getEntity()).getVariant().hasHullMod("vic_allRoundShieldUpgrade");
        stats.getZeroFluxSpeedBoost().modifyFlat(id, zFluxBonus);
        float rangeBonus = 1f + this.rangeBonus * (hasFortress ? 1f - altModRangeReduction : 1f);
        stats.getBallisticWeaponRangeBonus().modifyMult(id, rangeBonus);
        stats.getEnergyWeaponRangeBonus().modifyMult(id, rangeBonus);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        boolean allowed = false;
        for (String name : allowedShips){
            if (ship.getHullSpec().getHullId().startsWith(name)) allowed = true;
        }
        if (!allowed) return;
        if (ship.getVariant().hasHullMod("vic_allRoundShieldUpgrade")) {
            if (!ship.getVariant().getHullSpec().getHullId().endsWith("_alt")) {
                ShipHullSpecAPI hullSpec = Global.getSettings().getHullSpec(ship.getHullSpec().getBaseHullId() + "_alt");
                List<WeaponGroupSpec> weaponGroups = new ArrayList<>(ship.getVariant().getWeaponGroups());
                ship.getVariant().setHullSpecAPI(hullSpec);
                ship.getVariant().getWeaponGroups().clear();
                ship.getVariant().getWeaponGroups().addAll(weaponGroups);
            }
        } else {
            if (ship.getVariant().getHullSpec().getHullId().endsWith("_alt")) {
                ShipHullSpecAPI hullSpec = Global.getSettings().getHullSpec(ship.getHullSpec().getBaseHullId().replace("_alt", ""));
                List<WeaponGroupSpec> weaponGroups = new ArrayList<>(ship.getVariant().getWeaponGroups());
                ship.getVariant().setHullSpecAPI(hullSpec);
                ship.getVariant().getWeaponGroups().clear();
                ship.getVariant().getWeaponGroups().addAll(weaponGroups);
            }
        }
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) {
        if (index == 0) return "" + zFluxBonus;
        if (index == 1) return "x" + (1f + rangeBonus) + "";
        if (index == 2) return (altModRangeReduction * 100f) + "%";
        return null;
    }
}