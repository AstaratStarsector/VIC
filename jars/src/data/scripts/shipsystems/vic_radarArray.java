package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.util.ArrayList;
import java.util.List;

public class vic_radarArray extends BaseShipSystemScript {

    final float
            shieldDamageIncrease = 25f,
            weaponDamageIncrease = 50f,
            shieldDamageReduction = 50f,
            weaponDamageReduction = 25f,
            missileManeuverBonus = 35f,
            fighterManeuverBonus = 35f;

    boolean
            doOnce = true,
            altSystem = false;

    String
            altSystemHmodID = "vic_allRoundShieldUpgrade";


    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();

        stats.getEccmChance().modifyFlat(id, 1);

        stats.getMissileMaxSpeedBonus().modifyPercent(id, missileManeuverBonus * effectLevel);
        stats.getMissileWeaponRangeBonus().modifyMult(id, 100 / (100f + missileManeuverBonus * effectLevel));
        stats.getMissileAccelerationBonus().modifyPercent(id, missileManeuverBonus * 3f * effectLevel);

        stats.getMissileMaxTurnRateBonus().modifyPercent(id, missileManeuverBonus * effectLevel);
        stats.getMissileTurnAccelerationBonus().modifyPercent(id, missileManeuverBonus * 3f * effectLevel);

        if (effectLevel > 0) {
            for (ShipAPI fighter : getFighters(ship)) {
                if (fighter.isHulk()) continue;
                MutableShipStatsAPI fStats = fighter.getMutableStats();
                fStats.getMaxSpeed().modifyMult(id, fighterManeuverBonus * effectLevel);
                fStats.getAcceleration().modifyPercent(id, fighterManeuverBonus * 2f * effectLevel);
                fStats.getDeceleration().modifyPercent(id, fighterManeuverBonus * effectLevel);
                fStats.getTurnAcceleration().modifyPercent(id, fighterManeuverBonus * 2f * effectLevel);
                fStats.getMaxTurnRate().modifyPercent(id, fighterManeuverBonus * effectLevel);
                fStats.getAutofireAimAccuracy().modifyFlat(id, 1);
                fighter.getEngineController().extendFlame(id, 0.5f * effectLevel, 0.5f * effectLevel, 0.5f * effectLevel);
                Global.getSoundPlayer().playLoop("system_targeting_feed_loop", ship, 1f, 1f, fighter.getLocation(), fighter.getVelocity());
            }
        }
    }

    private List<ShipAPI> getFighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<ShipAPI>();

//		this didn't catch fighters returning for refit
//		for (FighterLaunchBayAPI bay : carrier.getLaunchBaysCopy()) {
//			if (bay.getWing() == null) continue;
//			result.addAll(bay.getWing().getWingMembers());
//		}

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (!ship.isFighter()) continue;
            if (ship.getWing() == null) continue;
            if (ship.getWing().getSourceShip() == carrier) {
                result.add(ship);
            }
        }

        return result;
    }


    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();

        stats.getEccmChance().unmodify(id);

        stats.getMissileMaxSpeedBonus().unmodify(id);
        stats.getMissileWeaponRangeBonus().unmodify(id);
        stats.getMissileAccelerationBonus().unmodify(id);

        stats.getMissileMaxTurnRateBonus().unmodify(id);
        stats.getMissileTurnAccelerationBonus().unmodify(id);

        for (ShipAPI fighter : getFighters(ship)) {
            if (fighter.isHulk()) continue;
            MutableShipStatsAPI fStats = fighter.getMutableStats();
            fStats.getMaxSpeed().unmodify(id);
            fStats.getAcceleration().unmodify(id);
            fStats.getDeceleration().unmodify(id);
            fStats.getTurnAcceleration().unmodify(id);
            fStats.getMaxTurnRate().unmodify(id);
            fStats.getAutofireAimAccuracy().unmodify(id);
        }
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) return new StatusData("+" + (int) missileManeuverBonus + "% missile Maneuverability", false);
        if (index == 1) return new StatusData("+" + (int) fighterManeuverBonus + "% fighter Maneuverability", false);
        if (index == 2) return new StatusData("Smart gun guidance active", false);
        return null;
    }
}
