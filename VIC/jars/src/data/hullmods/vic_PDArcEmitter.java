package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class vic_PDArcEmitter extends BaseHullMod {

    private final Map<ShipAPI.HullSize, Float>
            chargeCD = new HashMap<>(),
            maxCharges = new HashMap<>(),
            range = new HashMap<>();
    private final float
            ZapDamage = 50f,
            ZapFlux = ZapDamage * 0.7f;

    {
        chargeCD.put(ShipAPI.HullSize.FIGHTER, 4.5f);
        chargeCD.put(ShipAPI.HullSize.FRIGATE, 3f);
        chargeCD.put(ShipAPI.HullSize.DESTROYER, 1.5f);
        chargeCD.put(ShipAPI.HullSize.CRUISER, 1f);
        chargeCD.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.6f);

        maxCharges.put(ShipAPI.HullSize.FIGHTER, 4f);
        maxCharges.put(ShipAPI.HullSize.FRIGATE, 6f);
        maxCharges.put(ShipAPI.HullSize.DESTROYER, 9f);
        maxCharges.put(ShipAPI.HullSize.CRUISER, 12f);
        maxCharges.put(ShipAPI.HullSize.CAPITAL_SHIP, 21f);

        range.put(ShipAPI.HullSize.FIGHTER, 250f);
        range.put(ShipAPI.HullSize.FRIGATE, 250f);
        range.put(ShipAPI.HullSize.DESTROYER, 300f);
        range.put(ShipAPI.HullSize.CRUISER, 350f);
        range.put(ShipAPI.HullSize.CAPITAL_SHIP, 450f);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

        super.advanceInCombat(ship, amount);
        if (!ship.isAlive()) return;
        ShipAPI.HullSize hullSize = ship.getHullSize();

        float charges = Math.round(maxCharges.get(hullSize) * ship.getMutableStats().getEnergyAmmoBonus().computeEffective(1));
        float empCD = 0f;

        String id = ship.getId();
        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
        if (customCombatData.get("vic_PDArcEmitterCharges" + id) instanceof Float)
            charges = (float) customCombatData.get("vic_PDArcEmitterCharges" + id);
        if (customCombatData.get("vic_PDArcEmitterCD" + id) instanceof Float)
            empCD = (float) customCombatData.get("vic_PDArcEmitterCD" + id);

        if (charges < maxCharges.get(hullSize)) charges += (1 / chargeCD.get(ship.getHullSize())) * amount;
        if (empCD > 0) empCD -= amount;

        if (!ship.isPhased() && !ship.getFluxTracker().isOverloadedOrVenting() && charges >= 1 && empCD <= 0 && !ship.isHoldFire() && (ship.getFluxTracker().getCurrFlux() + ZapFlux < ship.getMaxFlux())) {
            MissileAPI missile = NearestEnemyMissilesInRange(ship, ship.getCollisionRadius() + range.get(ship.getHullSize()));
            if (missile != null) {

                Global.getCombatEngine().spawnEmpArcVisual(ship.getLocation(),
                        ship,
                        missile.getLocation(),
                        missile,
                        2,
                        new Color(255, 162, 0, 29),
                        new Color(255, 191, 21, 255));
                Global.getCombatEngine().applyDamage(missile,
                        missile.getLocation(),
                        ZapDamage * ship.getMutableStats().getDamageToMissiles().getModifiedValue() * ship.getMutableStats().getEnergyWeaponDamageMult().getModifiedValue(),
                        DamageType.ENERGY,
                        0,
                        false,
                        false,
                        ship);
                /*
                Global.getCombatEngine().spawnEmpArc(ship,
                        ship.getLocation(),
                        null,
                        missile,
                        DamageType.FRAGMENTATION,
                        ZapDamage * ship.getMutableStats().getDamageToMissiles().getModifiedValue() * ship.getMutableStats().getEnergyWeaponDamageMult().getModifiedValue(),
                        0,
                        3000,
                        null,
                        1,
                        new Color(0, 217, 255, 183),
                        new Color(21, 208, 255, 255));
                 */
                ship.getFluxTracker().increaseFlux(ZapFlux * (ship.getMutableStats().getEnergyWeaponFluxCostMod().computeEffective(1)), false);
                charges--;
                empCD = 0.1f;
            }
        }
        customCombatData.put("vic_PDArcEmitterCharges" + id, charges);
        customCombatData.put("vic_PDArcEmitterCD" + id, empCD);

        if (ship == Global.getCombatEngine().getPlayerShip()) {
            if (charges >= maxCharges.get(hullSize)) {
                Global.getCombatEngine().maintainStatusForPlayerShip("vic_PDArcEmitterCharges", "graphics/icons/hullsys/vic_empEmitter.png", "EMP charges", Math.round(Math.floor(charges)) + " ", false);
            } else {
                float timeLeft = (float) (1 - (charges - Math.floor(charges))) * chargeCD.get(ship.getHullSize());
                Global.getCombatEngine().maintainStatusForPlayerShip("vic_PDArcEmitterCharges", "graphics/icons/hullsys/vic_empEmitter.png", "EMP charges", Math.round(Math.floor(charges)) + " / " + (float) Math.round(timeLeft * 10) / 10, false);
            }
        }
    }


    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0)
            return (range.get(ShipAPI.HullSize.FRIGATE)).intValue() + "/" + (range.get(ShipAPI.HullSize.DESTROYER)).intValue() + "/" + (range.get(ShipAPI.HullSize.CRUISER)).intValue() + "/" + (range.get(ShipAPI.HullSize.CAPITAL_SHIP)).intValue();
        if (index == 1) return "" + Math.round(ZapDamage);
        if (index == 2) return "" + Math.round(ZapFlux);
        if (index == 3)
            return (chargeCD.get(ShipAPI.HullSize.FRIGATE)).intValue() + "/" + (chargeCD.get(ShipAPI.HullSize.DESTROYER)).toString() + "/" + (chargeCD.get(ShipAPI.HullSize.CRUISER)).intValue() + "/" + (chargeCD.get(ShipAPI.HullSize.CAPITAL_SHIP)).toString();
        if (index == 4)
            return (maxCharges.get(ShipAPI.HullSize.FRIGATE)).intValue() + "/" + (maxCharges.get(ShipAPI.HullSize.DESTROYER)).intValue() + "/" + (maxCharges.get(ShipAPI.HullSize.CRUISER)).intValue() + "/" + (maxCharges.get(ShipAPI.HullSize.CAPITAL_SHIP)).intValue();
        return null;
    }

    public MissileAPI NearestEnemyMissilesInRange(CombatEntityAPI entity, float MaxRange) {

        MissileAPI closest = null;
        float distanceSquared, closestDistanceSquared = Float.MAX_VALUE;

        for (MissileAPI tmp : Global.getCombatEngine().getMissiles()) {

            if (tmp.getOwner() == entity.getOwner()) continue;
            if (tmp.getCollisionClass() == CollisionClass.NONE) continue;
            if (!CombatUtils.isVisibleToSide(tmp, entity.getOwner())) continue;

            distanceSquared = MathUtils.getDistanceSquared(tmp.getLocation(), entity.getLocation());

            if (distanceSquared < closestDistanceSquared) {
                closest = tmp;
                closestDistanceSquared = distanceSquared;
            }
        }
        if (closestDistanceSquared > MaxRange * MaxRange) {
            return null;
        }
        return closest;
    }
}
