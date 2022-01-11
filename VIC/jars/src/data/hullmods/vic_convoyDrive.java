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

    final int ZERO_FLUX_BONUS = 35;
    final float RANGE_BONUS = 1.25f;

    final List<String> allowedShips = new ArrayList<>();
    {
        allowedShips.add("vic_kobal");
        allowedShips.add("vic_samael");
        allowedShips.add("vic_thamuz");
    }


    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getZeroFluxSpeedBoost().modifyFlat(id, ZERO_FLUX_BONUS);
        stats.getBallisticWeaponRangeBonus().modifyMult(id, RANGE_BONUS);
        stats.getEnergyWeaponRangeBonus().modifyMult(id, RANGE_BONUS);
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
        if (index == 0) return "" + ZERO_FLUX_BONUS;
        if (index == 1) return "x" + RANGE_BONUS + "";
        return null;
    }
}