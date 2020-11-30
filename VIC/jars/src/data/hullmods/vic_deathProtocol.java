package data.hullmods;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import java.awt.*;
import java.util.EnumSet;

public class vic_deathProtocol extends BaseHullMod {

    public final float
            dmgIncreas = 1.3f,
            dmgTaken = 1.25f,
            cloakCost = 1.25f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponDamageMult().modifyMult(id, dmgIncreas);
        stats.getEnergyWeaponDamageMult().modifyMult(id, dmgIncreas);
        stats.getBeamWeaponDamageMult().modifyMult(id, dmgIncreas);
        stats.getMissileWeaponDamageMult().modifyMult(id, dmgIncreas);

        stats.getShieldDamageTakenMult().modifyMult(id, dmgTaken);
        stats.getArmorDamageTakenMult().modifyMult(id, dmgTaken);
        stats.getHullDamageTakenMult().modifyMult(id, dmgTaken);

        stats.getPhaseCloakActivationCostBonus().modifyMult(id, cloakCost);
        stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, cloakCost);
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
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
        ship.getShield().setInnerColor(new Color(255,255,255,125));
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return (ship.getHullSpec().getHullId().startsWith("vic_"));
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return Math.round((dmgIncreas - 1) * 100) + "%";
        if (index == 1) return Math.round((dmgTaken - 1) * 100) + "%";
        if (index == 2) return Math.round((cloakCost - 1) * 100) + "%";
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




