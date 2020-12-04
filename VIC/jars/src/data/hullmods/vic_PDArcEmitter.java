package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
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
            ZapDamage = 150f,
            ZapFlux = 150f;

    {
        chargeCD.put(ShipAPI.HullSize.FRIGATE, 10f);
        chargeCD.put(ShipAPI.HullSize.DESTROYER, 7f);
        chargeCD.put(ShipAPI.HullSize.CRUISER, 5f);
        chargeCD.put(ShipAPI.HullSize.CAPITAL_SHIP, 4f);

        maxCharges.put(ShipAPI.HullSize.FRIGATE, 2f);
        maxCharges.put(ShipAPI.HullSize.DESTROYER, 3f);
        maxCharges.put(ShipAPI.HullSize.CRUISER, 4f);
        maxCharges.put(ShipAPI.HullSize.CAPITAL_SHIP, 6f);

        range.put(ShipAPI.HullSize.FRIGATE, 200f);
        range.put(ShipAPI.HullSize.DESTROYER, 250f);
        range.put(ShipAPI.HullSize.CRUISER, 300f);
        range.put(ShipAPI.HullSize.CAPITAL_SHIP, 350f);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        super.advanceInCombat(ship, amount);
        if (!ship.isAlive()) return;
        ShipAPI.HullSize hullSize = ship.getHullSize();

        float charges = maxCharges.get(hullSize);
        float empCD = 0f;

        String id = ship.getId();
        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
        if (customCombatData.get("vic_PDArcEmitterCharges" + id) instanceof Float)
            charges = (float) customCombatData.get("vic_PDArcEmitterCharges" + id);
        if (customCombatData.get("vic_PDArcEmitterCD" + id) instanceof Float)
            empCD = (float) customCombatData.get("vic_PDArcEmitterCD" + id);

        if (charges < maxCharges.get(hullSize)) charges += (1 / chargeCD.get(ship.getHullSize())) * amount;
        if (empCD > 0) empCD -= amount;

        if (charges >= 1 && empCD <= 0 && (ship.getFluxTracker().getCurrFlux() - 100 < ship.getMaxFlux())) {
            for (MissileAPI missile : CombatUtils.getMissilesWithinRange(ship.getLocation(), ship.getCollisionRadius() + range.get(ship.getHullSize()))) {
                if (missile.getOwner() == ship.getOwner()) continue;
                if (missile.getCollisionClass() == CollisionClass.NONE) continue;

                Global.getCombatEngine().spawnEmpArc(ship,
                        ship.getLocation(),
                        null,
                        missile,
                        DamageType.FRAGMENTATION,
                        ZapDamage,
                        0,
                        3000,
                        null,
                        1,
                        new Color(0, 217, 255, 183),
                        new Color(21, 208, 255, 255));
                ship.getFluxTracker().setCurrFlux(ship.getCurrFlux() + ZapFlux);
                charges--;
                empCD = 0.2f;
                break;
            }
        }
        customCombatData.put("vic_PDArcEmitterCharges" + id, charges);
        customCombatData.put("vic_PDArcEmitterCD" + id, empCD);

        if (ship == Global.getCombatEngine().getPlayerShip()) {
            if (charges >= maxCharges.get(hullSize)) {
                Global.getCombatEngine().maintainStatusForPlayerShip("vic_PDArcEmitterCharges", "graphics/icons/hullsys/vic_empEmitter.png", "EMP charges", Math.round(Math.floor(charges)) + "", false);
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
            return (chargeCD.get(ShipAPI.HullSize.FRIGATE)).intValue() + "/" + (chargeCD.get(ShipAPI.HullSize.DESTROYER)).intValue() + "/" + (chargeCD.get(ShipAPI.HullSize.CRUISER)).intValue() + "/" + (chargeCD.get(ShipAPI.HullSize.CAPITAL_SHIP)).intValue();
        if (index == 4)
            return (maxCharges.get(ShipAPI.HullSize.FRIGATE)).intValue() + "/" + (maxCharges.get(ShipAPI.HullSize.DESTROYER)).intValue() + "/" + (maxCharges.get(ShipAPI.HullSize.CRUISER)).intValue() + "/" + (maxCharges.get(ShipAPI.HullSize.CAPITAL_SHIP)).intValue();
        return null;
    }
}
