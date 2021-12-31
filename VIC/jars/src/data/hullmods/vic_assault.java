package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;
import java.util.Map;

public class vic_assault extends BaseHullMod {

    private final Map<HullSize, Float> strafeMulti = new HashMap<>();
    {
        strafeMulti.put(HullSize.FIGHTER, 1f);
        strafeMulti.put(HullSize.FRIGATE, 1f);
        strafeMulti.put(HullSize.DESTROYER, 0.75f);
        strafeMulti.put(HullSize.CRUISER, 0.5f);
        strafeMulti.put(HullSize.CAPITAL_SHIP, 0.25f);
    }

    private final Map<HullSize, Float> baseDecay = new HashMap<>();
    {
        baseDecay.put(HullSize.FIGHTER, 35f);
        baseDecay.put(HullSize.FRIGATE, 35f);
        baseDecay.put(HullSize.DESTROYER, 25f);
        baseDecay.put(HullSize.CRUISER, 20f);
        baseDecay.put(HullSize.CAPITAL_SHIP, 15f);
    }

    float accelBonus = 200f;
    float minAngle = 60f;

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
	}

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
        String id = ship.getId();

        float boostPower = 0f;

        if (customCombatData.get("vic_assaultBoostPower" + id) instanceof Float)
            boostPower = (float) customCombatData.get("vic_assaultBoostPower" + id);

        /*
        Vector2f lastVelocity = null;
        if (customCombatData.get("vic_assaultlastVelocity" + id) instanceof Vector2f)
            lastVelocity = (Vector2f) customCombatData.get("vic_assaultlastVelocity" + id);

        if (lastVelocity == null) lastVelocity = new Vector2f(ship.getVelocity());
        Vector2f currentVelocity = new Vector2f(ship.getVelocity().x - lastVelocity.x, ship.getVelocity().y - lastVelocity.y);
        lastVelocity = new Vector2f(ship.getVelocity());
        if (VectorUtils.isZeroVector(currentVelocity)) currentVelocity = new Vector2f(ship.getVelocity());

        customCombatData.put("vic_assaultlastVelocity" + id, lastVelocity);

        Global.getCombatEngine().maintainStatusForPlayerShip("vic_direction", "graphics/icons/hullsys/vic_adaptiveWarfareSystem.png", "Acceleration",  currentVelocity.length() / amount + "", false);
*/

        Vector2f newVector = new Vector2f();
        if (ship.getEngineController().isAccelerating()) {
            newVector.y += 1 * ship.getAcceleration();
        }
        if(ship.getEngineController().isAcceleratingBackwards() || ship.getEngineController().isDecelerating()){
            newVector.y -= 1 * ship.getDeceleration();
        }
        if (ship.getEngineController().isStrafingLeft()) {
            newVector.x -=  1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
        }
        if (ship.getEngineController().isStrafingRight()) {
            newVector.x += 1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
        }

        float angleDifferance = 0;
        if (!VectorUtils.isZeroVector(newVector)){
            float angle = Misc.getAngleInDegrees(new Vector2f(ship.getVelocity()));
            float accelAngle = Misc.getAngleInDegrees(new Vector2f(newVector)) + ship.getFacing() - 90f;
            angleDifferance = Math.abs(MathUtils.getShortestRotation(angle, accelAngle));
        }
        if (angleDifferance <= minAngle) angleDifferance = 0f;

        float powerIncrease = 75f * angleDifferance/180f * amount;
        if (powerIncrease > 0){
            boostPower += powerIncrease;
            if (boostPower > 100f) boostPower = 100f;
        } else {
            if (boostPower > 0f){
                float base = baseDecay.get(ship.getHullSize());
                boostPower -= (base + (boostPower * 0.10f)) * amount;
                if (boostPower < 0f) boostPower = 0f;
            }
        }

        /*
        ship.getMutableStats().getAcceleration().modifyMult("vic_assault", 1 + (boostPower / 50f));
        ship.getMutableStats().getDeceleration().modifyMult("vic_assault", 1 + (boostPower / 50f));
         */

        ship.getMutableStats().getAcceleration().modifyPercent("vic_assault", accelBonus * boostPower * 0.01f);
        ship.getMutableStats().getDeceleration().modifyPercent("vic_assault", accelBonus * boostPower * 0.01f);

        Global.getCombatEngine().maintainStatusForPlayerShip("vic_direction1", "graphics/icons/hullsys/vic_auxThrusters.png", "Acceleration increase",  Math.round(accelBonus * boostPower * 0.01f) + "%", false);

        customCombatData.put("vic_assaultBoostPower" + id, boostPower);
    }
/*
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship.getHullSpec().getHullId().startsWith("vic_");
    }

	@Override
	public String getUnapplicableReason(ShipAPI ship) {
        if (!ship.getHullSpec().getHullId().startsWith("vic_"))
            return "Not compatible with non VIC ships";
		return null;
	}

 */

    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) return Math.round(accelBonus) + "%";
        if (index == 1) return Math.round(minAngle) + "";
        return null;
    }
}




