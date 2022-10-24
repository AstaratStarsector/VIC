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
public class vic_gatebreakerModeSwapper extends BaseHullMod
{
    public static final String GATEBREAKER_SLOT_R = "WS0006";
    public static final String GATEBREAKER_SLOT_L = "WS0007";
    public static final String WEAPON_PREFIX_R = "vic_gatebreaker_r_";
    public static final String WEAPON_PREFIX_L = "vic_gatebreaker_l_";

    // points to the next weapon/hullmod suffix
    public static final Map<String, String> LOADOUT_CYCLE = new HashMap<>();

    static
    {
        LOADOUT_CYCLE.put("autocannon", "shotgun");
        LOADOUT_CYCLE.put("shotgun", "flamer");
        LOADOUT_CYCLE.put("flamer", "autocannon");
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
            if (stats.getVariant().getHullMods().contains("vic_gatebreaker_mode_" + hullmod))
            {
                switchLoadout = false;
                break;
            }
        }

        if (switchLoadout)
        {

            // default to autocannon if there's no weapons
            String newWeawpon = "shotgun";
            for (String key : LOADOUT_CYCLE.keySet())
            {
                // cycle to whatever the next weapon is, based on the weapon currently in the slot
                if (stats.getVariant().getWeaponId(GATEBREAKER_SLOT_R) != null && stats.getVariant().getWeaponId(GATEBREAKER_SLOT_R).contains(key))
                {
                    newWeawpon = LOADOUT_CYCLE.get(key);
                }

                if (stats.getVariant().getWeaponId(GATEBREAKER_SLOT_L) != null && stats.getVariant().getWeaponId(GATEBREAKER_SLOT_L).contains(key))
                {
                    newWeawpon = LOADOUT_CYCLE.get(key);
                }
            }

            // add hullmod to match new weapons
            stats.getVariant().addMod("vic_gatebreaker_mode_" + newWeawpon);

            // clear slot
            stats.getVariant().clearSlot(GATEBREAKER_SLOT_R);
            stats.getVariant().clearSlot(GATEBREAKER_SLOT_L);
            // add gun
            stats.getVariant().addWeapon(GATEBREAKER_SLOT_R, WEAPON_PREFIX_R + newWeawpon);
            stats.getVariant().addWeapon(GATEBREAKER_SLOT_L, WEAPON_PREFIX_L + newWeawpon);
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id)
    {
        // how the fuck can ALL of these be null
        // alex pls
        if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null || Global.getSector().getPlayerFleet().getCargo() == null)
            return;
        if (Global.getSector().getPlayerFleet().getCargo().getStacksCopy() == null || Global.getSector().getPlayerFleet().getCargo().getStacksCopy().isEmpty())
            return;
        for (CargoStackAPI stack : Global.getSector().getPlayerFleet().getCargo().getStacksCopy())
        {
            if (stack.isWeaponStack() && stack.getWeaponSpecIfWeapon().getWeaponId().contains("vic_gatebreaker"))
                Global.getSector().getPlayerFleet().getCargo().removeStack(stack);
        }
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize)
    {
        return "without the normal CR penalty";
    }
}
