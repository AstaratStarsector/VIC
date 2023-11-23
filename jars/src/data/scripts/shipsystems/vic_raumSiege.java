package data.scripts.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class vic_raumSiege extends BaseShipSystemScript {

    float
            siegeDamageActiveUltra = 0.25f,
            siegeDamageActive = 0.25f,
            siegeRoF = -0.15f,
            siegeSpeed = -0.25f,
            siegeManeuver = 0.5f,
            siegeManeuverActive = 1f,
            assaultRoF = 0.5f,
            assaultFluxCost = -0.25f,
            assaultSpeed = 0.25f;

    public static float siegeRange = 0.25f;


    boolean siege = false;
    boolean swithced = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (state == State.IDLE || state == State.COOLDOWN) {
            effectLevel = 1;
        }
        if (siege) {
            stats.getBallisticWeaponRangeBonus().modifyMult(id, 1 + siegeRange * effectLevel);
            stats.getEnergyWeaponRangeBonus().modifyMult(id, 1 + siegeRange * effectLevel);
            stats.getMissileWeaponRangeBonus().modifyMult(id, 1 + siegeRange * effectLevel);

            /*
            stats.getBallisticWeaponDamageMult().modifyMult(id, 1 + siegeDamage);
            stats.getEnergyWeaponDamageMult().modifyMult(id, 1 + siegeDamage);
            stats.getMissileWeaponDamageMult().modifyMult(id, 1 + siegeDamage);
             */

            stats.getBallisticRoFMult().modifyMult(id, 1 + siegeRoF * effectLevel);
            stats.getBallisticRoFMult().modifyMult(id, 1 + siegeRoF * effectLevel);
            stats.getMissileRoFMult().modifyMult(id, 1 + siegeRoF * effectLevel);

            stats.getMaxSpeed().modifyMult(id, 1 + siegeSpeed * effectLevel);
            stats.getAcceleration().modifyMult(id, 1 + siegeManeuver * effectLevel);
            stats.getDeceleration().modifyMult(id, 1 + siegeManeuver * effectLevel);
            stats.getMaxTurnRate().modifyMult(id, 1 + siegeManeuver * effectLevel);
            stats.getTurnAcceleration().modifyMult(id, 1 + (siegeManeuver * 2) * effectLevel);
        } else {
            stats.getBallisticWeaponRangeBonus().unmodify(id);
            stats.getEnergyWeaponRangeBonus().unmodify(id);
            stats.getMissileWeaponRangeBonus().unmodify(id);

            stats.getBallisticWeaponDamageMult().unmodify(id);
            stats.getEnergyWeaponDamageMult().unmodify(id);
            stats.getMissileWeaponDamageMult().unmodify(id);

            stats.getBallisticRoFMult().unmodify(id);
            stats.getBallisticRoFMult().unmodify(id);
            stats.getMissileRoFMult().unmodify(id);

            stats.getMaxSpeed().unmodify(id);
            stats.getAcceleration().unmodify(id);
            stats.getDeceleration().unmodify(id);
            stats.getMaxTurnRate().unmodify(id);
            stats.getTurnAcceleration().unmodify(id);
        }

        if (state == State.ACTIVE || state == State.IN || state == State.OUT) {
            if (!swithced) {
                siege = !siege;
                swithced = true;
            }

            if (siege) {

                stats.getAcceleration().modifyMult(id, 1 + siegeManeuverActive * effectLevel);
                stats.getDeceleration().modifyMult(id, 1 + siegeManeuverActive * effectLevel);
                stats.getMaxTurnRate().modifyMult(id, 1 + siegeManeuverActive * effectLevel);
                stats.getTurnAcceleration().modifyMult(id, 1 + (siegeManeuverActive * 2) * effectLevel);

                if (state == State.IN) {
                    stats.getBallisticWeaponDamageMult().modifyMult(id, 1 + (siegeDamageActive + siegeDamageActiveUltra));
                    stats.getEnergyWeaponDamageMult().modifyMult(id, 1 + (siegeDamageActive + siegeDamageActiveUltra));
                    stats.getMissileWeaponDamageMult().modifyMult(id, 1 + (siegeDamageActive + siegeDamageActiveUltra));
                } else {
                    stats.getBallisticWeaponDamageMult().modifyMult(id, 1 + siegeDamageActive * effectLevel);
                    stats.getEnergyWeaponDamageMult().modifyMult(id, 1 + siegeDamageActive * effectLevel);
                    stats.getMissileWeaponDamageMult().modifyMult(id, 1 + siegeDamageActive * effectLevel);
                }





            } else {
                stats.getBallisticRoFMult().modifyMult(id, 1 + assaultRoF * effectLevel);
                stats.getEnergyRoFMult().modifyMult(id, 1 + assaultRoF * effectLevel);
                stats.getMissileRoFMult().modifyMult(id, 1 + assaultRoF * effectLevel);

                stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1 + assaultFluxCost * effectLevel);
                stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1 + assaultFluxCost * effectLevel);
                stats.getMissileWeaponFluxCostMod().modifyMult(id, 1 + assaultFluxCost * effectLevel);

                stats.getMaxSpeed().modifyMult(id, 1 + assaultSpeed);
                stats.getAcceleration().modifyMult(id, 1 + assaultSpeed * 2 * effectLevel);
                stats.getDeceleration().modifyMult(id, 1 + assaultSpeed * 2 * effectLevel);
            }
        } else {
            swithced = false;

        }


        super.apply(stats, id, state, effectLevel);
        stats.getEntity().getCustomData().put("vic_raumSiedge", siege);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().unmodify(id);
        stats.getEnergyWeaponRangeBonus().unmodify(id);
        stats.getMissileWeaponRangeBonus().unmodify(id);

        stats.getBallisticWeaponDamageMult().unmodify(id);
        stats.getEnergyWeaponDamageMult().unmodify(id);
        stats.getMissileWeaponDamageMult().unmodify(id);

        stats.getBallisticRoFMult().unmodify(id);
        stats.getBallisticRoFMult().unmodify(id);
        stats.getMissileRoFMult().unmodify(id);

        stats.getBallisticWeaponFluxCostMod().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
        stats.getMissileWeaponFluxCostMod().unmodify(id);

        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
    }

    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        String text = null;
        if (siege) {
            text = "Siege";
        }
        return text;
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (siege && (effectLevel == 0 || swithced)) {
            if (index == 0) return new StatusData("Range and Maneuverability increased", false);
            if (index == 1) return new StatusData("Max speed and RoF decreased", true);
            if (state == State.ACTIVE || state == State.IN)
                if (index == 2) return new StatusData("Damage and Maneuverability increased", false);
            if (state == State.IN) if (index == 3) return new StatusData("Damage Super Increased!!!", false);
        } else {
            if (state == State.ACTIVE || state == State.IN)
                if (index == 0) return new StatusData("RoF, Max speed and WPN Flux Eff. increased", false);
            if (state == State.IN) if (index == 3) return null;
        }
        return null;
    }
}