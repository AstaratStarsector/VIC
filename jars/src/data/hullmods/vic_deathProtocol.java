package data.hullmods;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.EnumSet;

public class vic_deathProtocol extends BaseHullMod {

    public static float
            dmgIncrease = 0.3f,
            dmgTaken = 1.25f,
            cloakCost = 1.25f,
            rangePenalty = 0.85f,
            minRange = 500,
            maxRange = 700;


    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        /*
        stats.getBallisticWeaponDamageMult().modifyMult(id, dmgIncrease);
        stats.getEnergyWeaponDamageMult().modifyMult(id, dmgIncrease);
        stats.getMissileWeaponDamageMult().modifyMult(id, dmgIncrease);

        ShipVariantAPI variant = stats.getVariant();
        float damageAndCloakMult = 0f;
        if (variant != null && variant.hasHullMod("vic_allRoundShieldUpgrade")) {
            damageAndCloakMult = dmgTakenAndCloakCostPenaltyMult;
        }
        if (variant != null && variant.hasHullMod("vic_assault")) {
            damageAndCloakMult = dmgTakenAndCloakCostPenaltyMult;
        }
        if (variant != null && !variant.getHullSpec().getHullId().startsWith("vic_")) {
            damageAndCloakMult = dmgTakenAndCloakCostPenaltyMult;
        }
        */
        stats.getShieldDamageTakenMult().modifyMult(id, dmgTaken);
        stats.getArmorDamageTakenMult().modifyMult(id, dmgTaken);
        stats.getHullDamageTakenMult().modifyMult(id, dmgTaken);

        stats.getPhaseCloakActivationCostBonus().modifyMult(id, cloakCost);
        stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, cloakCost);
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship.getShield() == null) return;
        ShieldAPI shipShield = ship.getShield();
        float radius = shipShield.getRadius();
        String innersprite;
        String outersprite;
        if (radius >= 256.0F) {
            innersprite = "graphics/fx/shield/vic_shields256_fervor.png";
            outersprite = "graphics/fx/shields256ring.png";
        } else if (radius >= 128.0F) {
            innersprite = "graphics/fx/shield/vic_shields128_fervor.png";
            outersprite = "graphics/fx/shields128ringc.png";
        } else {
            innersprite = "graphics/fx/shield/vic_shields64_fervor.png";
            outersprite = "graphics/fx/shields64ringd.png";
        }
        shipShield.setRadius(radius, innersprite, outersprite);
        ship.getShield().setInnerColor(new Color(255, 255, 255, 125));
    }

    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) return Math.round((dmgIncrease) * 100) + "%";
        if (index == 1) return Math.round(minRange) + "";
        if (index == 2) return Math.round(maxRange) + "";
        if (index == 3) return Math.round(((dmgTaken) - 1) * 100) + "%";
        if (index == 4) return Math.round(((cloakCost) - 1) * 100) + "%";
        return null;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship.getOriginalOwner() == -1) return;
        if (Global.getCurrentState().equals(GameState.TITLE)) return;
        if (!ship.hasListenerOfClass(vic_FervorProtocolListener.class)) {
            vic_FervorProtocolListener listner = new vic_FervorProtocolListener();
            listner.ship = ship;
            ship.addListener(listner);
        }
        if (ship == Global.getCombatEngine().getPlayerShip() && ship.getShipTarget() != null){
            float dist = MathUtils.getDistance(ship.getShipTarget(), ship);
            float f = 1f;
            if (dist > maxRange) {
                f = 0f;
            } else if (dist > minRange) {
                f = 1f - (dist - minRange) / (maxRange - minRange);
            }
            if (f < 0) f = 0;
            if (f > 1) f = 1;
            Global.getCombatEngine().maintainStatusForPlayerShip("vic_deathProtocol_status", "graphics/icons/hullsys/vic_deathProtocolIcon.png", "Fervor damage bonus", Math.round(dmgIncrease * f * 100f) + "%", false);
        }
        if (ship.getShield() == null) {
            ship.setWeaponGlow(1.5f, new Color(255, 0, 21, 255), EnumSet.allOf(com.fs.starfarer.api.combat.WeaponAPI.WeaponType.class));
        } else {
            if (ship.getSystem() == null || !ship.getSystem().isActive())
                ship.getShield().setInnerColor(new Color(255, 255, 255, 125));
        }
    }

    static class vic_FervorProtocolListener implements DamageDealtModifier {

        public CombatEntityAPI ship = null;

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            //if (!(param instanceof DamagingProjectileAPI) && !(param instanceof BeamAPI) && !(param instanceof EmpArcEntityAPI)) return null;
            /*
            Global.getLogger(vic_FervorProtocolListener.class).info("procked");
            if (param != null){
                Global.getLogger(vic_FervorProtocolListener.class).info(param.toString());
            } else {
                Global.getLogger(vic_FervorProtocolListener.class).info("class is null");
            }
             */
            if (target == null || ship == null) return null;
            float dist = MathUtils.getDistance(target, ship);

            float f = 1f;
            if (dist > maxRange) {
                f = 0f;
            } else if (dist > minRange) {
                f = 1f - (dist - minRange) / (maxRange - minRange);
            }
            f = MathUtils.clamp(f, 0, 1);

            String id = "vic_FervorProtocolDamageMod";
            damage.getModifier().modifyMult(id, 1 + f * dmgIncrease);
            return id;
        }
    }

}




