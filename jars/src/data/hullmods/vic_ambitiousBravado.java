package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;
import java.util.Map;

public class vic_ambitiousBravado extends BaseHullMod {

    static final float damageTakenIncrease = 5f;
    final float DPCheckRange = 1500f,
            capDamageReduction = 10f;
    final String HmodId = "vic_ambitiousBravado";

    final Map<HullSize, Float> damageBonusToDestroyers = new HashMap<>();
    final Map<HullSize, Float> damageBonusToCruisers = new HashMap<>();
    final Map<HullSize, Float> damageBonusToCapitals = new HashMap<>();

    {
        damageBonusToDestroyers.put(HullSize.FRIGATE, 5f);
        damageBonusToDestroyers.put(HullSize.DESTROYER, 0f);
        damageBonusToDestroyers.put(HullSize.CRUISER, 0f);
        damageBonusToDestroyers.put(HullSize.CAPITAL_SHIP, 0f);

        damageBonusToCruisers.put(HullSize.FRIGATE, 10f);
        damageBonusToCruisers.put(HullSize.DESTROYER, 5f);
        damageBonusToCruisers.put(HullSize.CRUISER, 0f);
        damageBonusToCruisers.put(HullSize.CAPITAL_SHIP, 0f);

        damageBonusToCapitals.put(HullSize.FRIGATE, 15f);
        damageBonusToCapitals.put(HullSize.DESTROYER, 10f);
        damageBonusToCapitals.put(HullSize.CRUISER, 10f);
        damageBonusToCapitals.put(HullSize.CAPITAL_SHIP, 0f);
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDamageToDestroyers().modifyPercent(id, damageBonusToDestroyers.get(hullSize));
        stats.getDamageToCruisers().modifyPercent(id, damageBonusToCruisers.get(hullSize));
        stats.getDamageToCapital().modifyPercent(id, damageBonusToCapitals.get(hullSize));
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship.getFullTimeDeployed() > 1 && !ship.hasListener(vic_ambitiousBravadoDamageTaken.class)) {
            ship.addListener(new vic_ambitiousBravadoDamageTaken());
        }

        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
        String id = ship.getId();

        float timer = 0.5f;
        float DPInRange = 0;

        if (customCombatData.get(HmodId + id) instanceof Float)
            timer = (float) customCombatData.get(HmodId + id);

        if (customCombatData.get(HmodId + id + "DPInRange") instanceof Float)
            DPInRange = (float) customCombatData.get(HmodId + id + "DPInRange");

        timer += amount;
        if (timer >= 0.5f) {
            timer -= 0.5f;
            MutableShipStatsAPI stats = ship.getMutableStats();
            float enemyDP = 0;
            for (ShipAPI enemy : AIUtils.getNearbyEnemies(ship, DPCheckRange)) {
                enemyDP += enemy.getDeployCost();
            }
            DPInRange = enemyDP;
            if (enemyDP >= ship.getDeployCost()) {
                stats.getShieldDamageTakenMult().modifyMult(HmodId, 1 - capDamageReduction * 0.01f);
                stats.getHullDamageTakenMult().modifyMult(HmodId, 1 - capDamageReduction * 0.01f);
                stats.getArmorDamageTakenMult().modifyMult(HmodId, 1 - capDamageReduction * 0.01f);
            } else {
                stats.getShieldDamageTakenMult().unmodify(HmodId);
                stats.getHullDamageTakenMult().unmodify(HmodId);
                stats.getArmorDamageTakenMult().unmodify(HmodId);
            }
        }

        if (Global.getCombatEngine().getPlayerShip().equals(ship)) {
            Global.getCombatEngine().maintainStatusForPlayerShip(HmodId, null, "Ambitious Bravado", "enemy DP in range:" + Math.round(DPInRange), false);
        }

        customCombatData.put(HmodId + id, timer);
        customCombatData.put(HmodId + id + "DPInRange", DPInRange);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return Math.round(5f) + "%";
        if (index == 1) return Math.round(10f) + "%";
        if (index == 2) return Math.round(15f) + "%";
        if (index == 3) return Math.round(10f) + "%";
        if (index == 4) return Math.round(10f) + "%";
        if (index == 5) return "Deployment Points";
        if (index == 6) return Math.round(DPCheckRange) + "";
        if (index == 7) return "Deployment Points";
        if (index == 8) return Math.round(damageTakenIncrease) + "%";
        return null;
    }

    public static class vic_ambitiousBravadoDamageTaken implements DamageTakenModifier {

        final Map<HullSize, Float> size = new HashMap<>();

        {
            size.put(HullSize.FIGHTER, 0f);
            size.put(HullSize.FRIGATE, 1f);
            size.put(HullSize.DESTROYER, 2f);
            size.put(HullSize.CRUISER, 3f);
            size.put(HullSize.CAPITAL_SHIP, 4f);
        }

        @Override
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            ShipAPI enemy = null;
            ShipAPI ship = (ShipAPI) target;
            if (param instanceof DamagingProjectileAPI) enemy = ((DamagingProjectileAPI) param).getSource();
            if (param instanceof BeamAPI) enemy = ((BeamAPI) param).getSource();
            if (param instanceof MissileAPI) enemy = ((MissileAPI) param).getSource();
            if (enemy != null) {
                /*
                if (enemy.isFighter()) {
                    ShipAPI carrier = enemy.getWing().getSourceShip();
                    if (carrier != null) enemy = carrier;
                }
                 */
                if (size.get(ship.getHullSize()) < size.get(enemy.getHullSize())) {
                    damage.getModifier().modifyMult("vic_damage", 1 + damageTakenIncrease * 0.01f);
                    return "vic_damage";
                }
            }
            return null;
        }
    }
}