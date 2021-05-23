package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class vic_targetAnalysis extends BaseHullMod {

    //fighter
    float weaponTurnRate = 25f;
    float projSpeed = 20f;

    //frigate
    float speed = 15f;
    float maneuverability = 20f;

    //destroyer

    //cruiser
    float weaponDamage = 10f;
    float weaponFluxCost = 10f;

    //capital
    float shieldEff = 10f;

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        String id = "vic_targetAnalysis";
        ShipAPI.HullSize target = null;
        if (ship.getShipTarget() != null){
            target = ship.getShipTarget().getHullSize();
        }
        MutableShipStatsAPI stats = ship.getMutableStats();

        //unApply all
        //fighter
        stats.getWeaponTurnRateBonus().unmodify(id);
        stats.getProjectileSpeedMult().unmodify(id);
        //frigate
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        //destroyer
        //cruiser
        stats.getEnergyWeaponDamageMult().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
        stats.getBallisticWeaponDamageMult().unmodify(id);
        stats.getBallisticWeaponFluxCostMod().unmodify(id);
        //capital
        stats.getShieldDamageTakenMult().unmodify(id);

        //apply
        if (target != null) {
            switch (target) {
                case DEFAULT:
                    break;
                case FIGHTER:
                    stats.getWeaponTurnRateBonus().modifyPercent(id, weaponTurnRate);
                    stats.getProjectileSpeedMult().modifyPercent(id, projSpeed);
                    break;
                case FRIGATE:
                    stats.getMaxSpeed().modifyFlat(id, speed);
                    stats.getMaxTurnRate().modifyPercent(id, maneuverability);
                    stats.getTurnAcceleration().modifyPercent(id, maneuverability);
                    stats.getAcceleration().modifyPercent(id, maneuverability);
                    stats.getDeceleration().modifyPercent(id, maneuverability);
                    break;
                case DESTROYER:
                    stats.getMaxTurnRate().modifyPercent(id, maneuverability);
                    stats.getTurnAcceleration().modifyPercent(id, maneuverability);


                    break;
                case CRUISER:


                    stats.getEnergyWeaponDamageMult().modifyMult(id, 1 + weaponDamage * 0.01f);
                    stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1 + weaponFluxCost * 0.01f);
                    stats.getBallisticWeaponDamageMult().modifyMult(id, 1 + weaponDamage * 0.01f);
                    stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1 + weaponFluxCost * 0.01f);
                    break;
                case CAPITAL_SHIP:
                    stats.getEnergyWeaponDamageMult().modifyMult(id, 1 + weaponDamage * 0.01f);
                    stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1 + weaponFluxCost * 0.01f);
                    stats.getBallisticWeaponDamageMult().modifyMult(id, 1 + weaponDamage * 0.01f);
                    stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1 + weaponFluxCost * 0.01f);

                    stats.getShieldDamageTakenMult().modifyMult(id, 1f - shieldEff * 0.01f);
                    break;
            }
        } else {
            //effect if no target

        }
    }
}
