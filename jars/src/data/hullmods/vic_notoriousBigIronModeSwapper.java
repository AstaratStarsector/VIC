package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import java.util.HashMap;
import java.util.Map;

// controls ammo swap
public class vic_notoriousBigIronModeSwapper extends BaseHullMod
{
    public static final String WEAPON_SLOT = "WS0046";
    public static final String WEAPON_PREFIX = "vic_notorious_big_iron_";

    // points to the next weapon/hullmod suffix
    public static final Map<String, String> LOADOUT_CYCLE = new HashMap<>();

    static
    {
        LOADOUT_CYCLE.put("buckshot", "slug");
        LOADOUT_CYCLE.put("slug", "dragon_breath");
        LOADOUT_CYCLE.put("dragon_breath", "buckshot");
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id)
    {

        if (stats.getEntity() == null)
            return;

        //WEAPONS

        // trigger a weapon switch if none of the selector hullmods are present (because one was removed, or because the ship was just spawned without one)
        boolean switchLoadout = true;
        for (String hullmod : LOADOUT_CYCLE.values())
        {
            if (stats.getVariant().getHullMods().contains("vic_notorious_big_iron_mode_" + hullmod))
            {
                switchLoadout = false;
                break;
            }
        }

        if (switchLoadout)
        {

            // default to buckshot if there's no weapons
            String newWeawpon = "buckshot";
            for (String key : LOADOUT_CYCLE.keySet())
            {
                // cycle to whatever the next weapon is, based on the weapon currently in the slot
                if (stats.getVariant().getWeaponId(WEAPON_SLOT) != null && stats.getVariant().getWeaponId(WEAPON_SLOT).contains(key))
                {
                    newWeawpon = LOADOUT_CYCLE.get(key);
                }

            }

            // add hullmod to match new weapons
            stats.getVariant().addMod("vic_notorious_big_iron_mode_" + newWeawpon);

            // clear slot
            stats.getVariant().clearSlot(WEAPON_SLOT);
            // add gun
            stats.getVariant().addWeapon(WEAPON_SLOT, WEAPON_PREFIX + newWeawpon);
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id)
    {
        if(ship.getOriginalOwner()<0){
            //undo fix for weapons put in cargo
            if(
                    Global.getSector()!=null &&
                            Global.getSector().getPlayerFleet()!=null &&
                            Global.getSector().getPlayerFleet().getCargo()!=null &&
                            Global.getSector().getPlayerFleet().getCargo().getStacksCopy()!=null &&
                            !Global.getSector().getPlayerFleet().getCargo().getStacksCopy().isEmpty()
            ){
                for (CargoStackAPI s : Global.getSector().getPlayerFleet().getCargo().getStacksCopy()){
                    if(
                            s.isWeaponStack()
                                    && s.getWeaponSpecIfWeapon().getWeaponId().startsWith("vic_notorious_big_iron")
                    ){
                        Global.getSector().getPlayerFleet().getCargo().removeStack(s);
                    }
                }
            }
        }
    }

    @Override
    public int getDisplayCategoryIndex()
    {
        return 2;
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize)
    {
        return "without the normal CR penalty";
    }
}
