package data.scripts.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class VIC_AdaptiveAssault extends BaseShipSystemScript {

    final float
            shieldDamageIncrease = 15f,
            weaponDamageIncrease = 50f,

            shieldDamageReduction = 50f,
            weaponDamageReduction = 25f;

    boolean
            doOnce = true,
            altSystem = false;

    String
            altSystemHmodID = "vic_allRoundShieldUpgrade";



    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (doOnce){
            altSystem = ship.getVariant().hasHullMod(altSystemHmodID);
        }

        if (!altSystem){
            stats.getBallisticWeaponDamageMult().modifyMult(id, 1 + weaponDamageIncrease * 0.01f * effectLevel);
            stats.getEnergyWeaponDamageMult().modifyMult(id, 1 + weaponDamageIncrease * 0.01f * effectLevel);
            stats.getShieldDamageTakenMult().modifyMult(id, 1 + shieldDamageIncrease * 0.01f * effectLevel);
        } else {

            if (ship.getShield() == null) return;
            ShieldAPI shipShield = ship.getShield();
            float radius = shipShield.getRadius();
            String innersprite;
            String outersprite;
            if (radius >= 256.0F) {
                innersprite = "graphics/fx/shield/vic_shieldsHex256.png";
                outersprite = "graphics/fx/shields256ring.png";
            } else if (radius >= 128.0F) {
                innersprite = "graphics/fx/shield/vic_shieldsHex128.png";
                outersprite = "graphics/fx/shields128ringc.png";
            } else {
                innersprite = "graphics/fx/shield/vic_shieldsHex64.png";
                outersprite = "graphics/fx/shields64ringd.png";
            }
            shipShield.setRadius(radius, innersprite, outersprite);
        }

    }

    public void unapply(MutableShipStatsAPI stats, String id) {

        stats.getEnergyWeaponDamageMult().unmodify(id);
        stats.getBallisticWeaponDamageMult().unmodify(id);
        stats.getShieldDamageTakenMult().unmodify(id);

        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship.getShield() == null) return;
        ShieldAPI shipShield = ship.getShield();
        float radius = shipShield.getRadius();
        String innerSprite;
        String outerSprite;
        if (radius >= 256.0F) {
            innerSprite = "graphics/fx/shield/vic_shields256.png";
            outerSprite = "graphics/fx/shields256ring.png";
        } else if (radius >= 128.0F) {
            innerSprite = "graphics/fx/shield/vic_shields128.png";
            outerSprite = "graphics/fx/shields128ringc.png";
        } else {
            innerSprite = "graphics/fx/shield/vic_shields64.png";
            outerSprite = "graphics/fx/shields64ringd.png";
        }
        shipShield.setRadius(radius, innerSprite, outerSprite);


    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (!altSystem){
            if (index == 0) return new StatusData("+" + (int) weaponDamageIncrease + "% weapon damage", false);
            if (index == 1) return new StatusData("+" + (int) shieldDamageIncrease + "% shield damage taken", false);
        } else {
            if (index == 0) return new StatusData("-" + (int) weaponDamageReduction + "% weapon damage", false);
            if (index == 1) return new StatusData("-" + (int) shieldDamageReduction + "% shield damage taken", false);
        }
        return null;
    }

    //gona keep it there mb i gona need it later
    /*
    private final Map<WeaponAPI.WeaponSize, Integer> pointsPerSize = new HashMap<>();

    {
        pointsPerSize.put(WeaponAPI.WeaponSize.SMALL, 1);
        pointsPerSize.put(WeaponAPI.WeaponSize.MEDIUM, 2);
        pointsPerSize.put(WeaponAPI.WeaponSize.LARGE, 4);
    }

    public Vector2f WeaponsSplit(ShipAPI ship) {
        float balScore = 0f;
        float energyScore = 0f;
        float totalScore = 0f;
        WeaponAPI.WeaponType type;
        WeaponSpecAPI weapon;
        for (String weaponSlot : ship.getVariant().getFittedWeaponSlots()) {
            weapon = ship.getVariant().getWeaponSpec(weaponSlot);
            type = weapon.getType();
            totalScore += pointsPerSize.get(weapon.getSize());
            if (type == WeaponAPI.WeaponType.BALLISTIC)
                energyScore += pointsPerSize.get(weapon.getSize());
            else if (type == WeaponAPI.WeaponType.ENERGY)
                balScore += pointsPerSize.get(weapon.getSize());
        }
        if (totalScore == 0){
            return new Vector2f(0.5f, 0.5f);
        }
        float balPrec = balScore / totalScore;
        float energyPrec = energyScore / totalScore;
        return new Vector2f(balPrec, energyPrec);
    }
     */
}
