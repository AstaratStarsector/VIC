package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.plugins.vic_combatPlugin;

import java.util.HashMap;
import java.util.Map;

// controls weapon swap
public class vic_raumWeaponSwapper extends BaseHullMod {

    public static final String WEAPON_SLOT = "WS0046";
    public static final String WEAPON_PREFIX = "vic_raum_weapon_";

    // points to the next weapon/hullmod suffix
    public static final Map<String, String> LOADOUT_CYCLE = new HashMap<>();

    static {
        LOADOUT_CYCLE.put("vodyanoy_ultra", "strzyga");
        LOADOUT_CYCLE.put("strzyga", "alkonost");
        LOADOUT_CYCLE.put("alkonost", "xl_laidlaw");
        LOADOUT_CYCLE.put("xl_laidlaw", "gagana_ultra");
        LOADOUT_CYCLE.put("gagana_ultra", "rokh");
        LOADOUT_CYCLE.put("rokh", "rakh");
        LOADOUT_CYCLE.put("rakh", "vodyanoy_ultra");
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

        if (stats.getEntity() == null)
            return;

        //WEAPONS

        // trigger a weapon switch if none of the selector hullmods are present (because one was removed, or because the ship was just spawned without one)
        boolean switchLoadout = true;
        for (String hullmod : LOADOUT_CYCLE.values()) {
            if (stats.getVariant().getHullMods().contains("vic_raum_weapon_swapper_" + hullmod)) {
                switchLoadout = false;
                break;
            }
        }

        if (switchLoadout) {

            // default to Gagana Ultra if there's no weapons
            String newWeawpon = "gagana_ultra";
            for (String key : LOADOUT_CYCLE.keySet()) {
                // cycle to whatever the next weapon is, based on the weapon currently in the slot
                if (stats.getVariant().getWeaponId(WEAPON_SLOT) != null && stats.getVariant().getWeaponId(WEAPON_SLOT).contains(key)) {
                    newWeawpon = LOADOUT_CYCLE.get(key);
                }

            }

            // add hullmod to match new weapons
            stats.getVariant().addMod("vic_raum_weapon_swapper_" + newWeawpon);

            // clear slot
            stats.getVariant().clearSlot(WEAPON_SLOT);
            // add gun
            stats.getVariant().addWeapon(WEAPON_SLOT, WEAPON_PREFIX + newWeawpon);
        } else if (stats.getVariant().getWeaponId(WEAPON_SLOT) == null){
            String newWeawpon = "gagana_ultra";
            for (String hullmod : LOADOUT_CYCLE.keySet()) {
                if (stats.getVariant().getHullMods().contains("vic_raum_weapon_swapper_" + hullmod)) {
                    newWeawpon = hullmod;
                    break;
                }
            }
            stats.getVariant().addWeapon(WEAPON_SLOT, WEAPON_PREFIX + newWeawpon);
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship.getOriginalOwner() < 0) {
            //undo fix for weapons put in cargo
            if (
                    Global.getSector() != null &&
                            Global.getSector().getPlayerFleet() != null &&
                            Global.getSector().getPlayerFleet().getCargo() != null &&
                            Global.getSector().getPlayerFleet().getCargo().getStacksCopy() != null &&
                            !Global.getSector().getPlayerFleet().getCargo().getStacksCopy().isEmpty()
            ) {
                for (CargoStackAPI s : Global.getSector().getPlayerFleet().getCargo().getStacksCopy()) {
                    if (
                            s.isWeaponStack()
                                    && s.getWeaponSpecIfWeapon().getWeaponId().startsWith("vic_raum_weapon")
                    ) {
                        Global.getSector().getPlayerFleet().getCargo().removeStack(s);
                    }
                }
            }
        }
    }

    @Override
    public int getDisplayCategoryIndex() {
        return 2;
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return "without the normal CR penalty";
    }
}
