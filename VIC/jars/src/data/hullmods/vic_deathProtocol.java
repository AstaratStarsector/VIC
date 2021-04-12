package data.hullmods;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import java.awt.*;
import java.util.EnumSet;

public class vic_deathProtocol extends BaseHullMod {

    public float
            dmgIncreas = 1.3f,
            dmgTaken = 1.25f,
            cloakCost = 1.25f,
            rangePenalty = 0.85f,
            dmgTakenAndCloakCostPenaltyMult = 0.25f;


    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponDamageMult().modifyMult(id, dmgIncreas);
        stats.getEnergyWeaponDamageMult().modifyMult(id, dmgIncreas);
        stats.getMissileWeaponDamageMult().modifyMult(id, dmgIncreas);

        ShipVariantAPI variant = stats.getVariant();
        float damageAndCloakMult = 0f;
        if (variant != null && variant.hasHullMod("vic_allRoundShieldUpgrade")){
            damageAndCloakMult = dmgTakenAndCloakCostPenaltyMult;
        }
        if (variant != null && variant.hasHullMod("vic_assault")) {
            damageAndCloakMult = dmgTakenAndCloakCostPenaltyMult;
        }
        if (!variant.getHullSpec().getHullId().startsWith("vic_")){
            damageAndCloakMult = dmgTakenAndCloakCostPenaltyMult;
        }

        stats.getBallisticWeaponRangeBonus().modifyMult(id, rangePenalty);
        stats.getEnergyWeaponRangeBonus().modifyMult(id, rangePenalty);
        stats.getMissileWeaponRangeBonus().modifyMult(id, rangePenalty);
        stats.getShieldDamageTakenMult().modifyMult(id, dmgTaken + damageAndCloakMult);
        stats.getArmorDamageTakenMult().modifyMult(id, dmgTaken + damageAndCloakMult);
        stats.getHullDamageTakenMult().modifyMult(id, dmgTaken + damageAndCloakMult);


        stats.getPhaseCloakActivationCostBonus().modifyMult(id, cloakCost + damageAndCloakMult);
        stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, cloakCost + damageAndCloakMult);

    }

    public void applyEffectsAfterShipCreation(ShipAPI ship,MutableShipStatsAPI stats, String id) {
        super.applyEffectsAfterShipCreation(ship, id);
        if (ship.getShield() == null) return;
        ShieldAPI shipShield = ship.getShield();
        float radius = shipShield.getRadius();
        String innersprite;
        String outersprite;
        if (radius >= 256.0F) {
            innersprite = "graphics/fx/shield/vic_shields256_fervor.png";
            outersprite = "graphics/fx/shield/vic_shields256ring.png";
        } else if (radius >= 128.0F) {
            innersprite = "graphics/fx/shield/vic_shields128_fervor.png";
            outersprite = "graphics/fx/shield/vic_shields128ring.png";
        } else {
            innersprite = "graphics/fx/shield/vic_shields64_fervor.png";
            outersprite = "graphics/fx/shield/vic_shields64ring.png";
        }
        shipShield.setRadius(radius, innersprite, outersprite);
        ship.getShield().setInnerColor(new Color(255, 255, 255, 125));


    }

    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {

        float damageAndCloakMult = 0f;
        if (ship != null){
            if (ship.getVariant().hasHullMod("vic_allRoundShieldUpgrade") ||
                    ship.getVariant().hasHullMod("vic_assault")||
                    !ship.getHullSpec().getHullId().startsWith("vic_")){
                damageAndCloakMult = dmgTakenAndCloakCostPenaltyMult;
            }
        }

        if (index == 0) return Math.round((dmgIncreas - 1) * 100) + "%";
        if (index == 1) return Math.round(((dmgTaken + damageAndCloakMult) - 1) * 100) + "%";
        if (index == 2) return Math.round(((cloakCost + damageAndCloakMult) - 1) * 100) + "%";
        if (index == 3) return Math.round((1f - rangePenalty) * 100f) + "%";
        if (index == 4) return "doubled";
        return null;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        super.advanceInCombat(ship, amount);
        if (ship.getOriginalOwner() == -1) return;
        if (ship.getShield() != null) return;
        ship.setWeaponGlow(1.5f, new Color(255, 0, 21, 255),EnumSet.allOf(com.fs.starfarer.api.combat.WeaponAPI.WeaponType.class));
    }
}




