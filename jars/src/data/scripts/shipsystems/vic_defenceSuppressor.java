package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import data.scripts.plugins.vic_combatPlugin;

import java.awt.*;

public class vic_defenceSuppressor extends BaseShipSystemScript {

    protected static float RANGE = 1500f;

    public static Color TEXT_COLOR = new Color(255, 55, 55, 255);

    public static Color JITTER_COLOR = new Color(111, 23, 248, 166);

    public ShipAPI ship;
    public ShipAPI target;

    private boolean doOnce = true;


    public void apply(MutableShipStatsAPI stats, final String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }


        if (state == State.IN) {
            if (target == null) {
                target = findTarget(ship);
            }
        } else if (state == State.ACTIVE) {
            if (doOnce) {
                if (target != null) {
                    if (target.getFluxTracker().showFloaty() ||
                            ship == Global.getCombatEngine().getPlayerShip() ||
                            target == Global.getCombatEngine().getPlayerShip()) {
                        target.getFluxTracker().showOverloadFloatyIfNeeded("Defences Suppressed!", TEXT_COLOR, 4f, true);
                    }
                }
                vic_combatPlugin.AddDefenceSuppressorTarget(target, 7);
                //target.getFluxTracker().showOverloadFloatyIfNeeded("Defences Suppressed!", TEXT_COLOR, 4f, true);
                doOnce = false;
            }
        }


        if (effectLevel > 0) {

            float maxRangeBonus = 50f;
            float jitterRangeBonus = effectLevel * maxRangeBonus;
            ship.setJitter(ship, JITTER_COLOR, effectLevel, 4, 0f, 0 + jitterRangeBonus);

            if (target != null)
                target.setJitter(ship, JITTER_COLOR, effectLevel, 3, 0f, 5f);
        }
    }


    public void unapply(MutableShipStatsAPI stats, String id) {
        target = null;
        doOnce = true;
    }

    protected ShipAPI findTarget(ShipAPI ship) {
        float range = getMaxRange(ship);
        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        ShipAPI target = ship.getShipTarget();
        if (target != null) {
            if (target.getOwner() == ship.getOwner()) {
                if (player) {
                    target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), HullSize.FRIGATE, range, true);
                } else {
                    Object test = ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET);
                    if (test instanceof ShipAPI) {
                        target = (ShipAPI) test;
                        float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
                        float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
                        if (dist > range + radSum) target = null;
                    }
                }
            } else {
                float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
                float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
                if (dist > range + radSum) target = null;
            }
        } else {
            target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), HullSize.FRIGATE, range, true);
        }

        return target;
    }


    public static float getMaxRange(ShipAPI ship) {
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(RANGE);
        //return RANGE;
    }


    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (effectLevel > 0) {
            if (index == 0) {
                return new StatusData("Charging up", false);
            }
        }
        return null;
    }


    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) return null;
        if (system.getState() != SystemState.IDLE) return null;

        ShipAPI target = findTarget(ship);
        if (target != null && target != ship) {
            return "READY";
        }
        if ((target == null) && ship.getShipTarget() != null) {
            return "OUT OF RANGE";
        }
        return "NO TARGET";
    }


    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        //if (true) return true;
        ShipAPI target = findTarget(ship);
        return target != null && target != ship;
    }

}








