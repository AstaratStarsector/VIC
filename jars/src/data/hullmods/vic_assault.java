package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
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

    private final Map<HullSize, Float> boostRateMulti = new HashMap<>();

    {
        boostRateMulti.put(HullSize.FIGHTER, 2f);
        boostRateMulti.put(HullSize.FRIGATE, 2f);
        boostRateMulti.put(HullSize.DESTROYER, 1f);
        boostRateMulti.put(HullSize.CRUISER, 0.75f);
        boostRateMulti.put(HullSize.CAPITAL_SHIP, 0.5f);
    }

    float accelBonus = 200f;
    float minAngle = 60f;
    float boostRate = 0.5f;
    float boostLossMulti = 2f;

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
        if (ship.getEngineController().isAcceleratingBackwards()) {
            newVector.y -= 1 * ship.getDeceleration();
        }
        if (ship.getEngineController().isStrafingLeft()) {
            newVector.x -= 1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
        }
        if (ship.getEngineController().isStrafingRight()) {
            newVector.x += 1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
        }
        if (ship.getEngineController().isDecelerating()) {
            if (ship.getVelocity().lengthSquared() > 0) {
                Vector2f normalizedVel = new Vector2f(ship.getVelocity());
                normalizedVel = Misc.normalise(normalizedVel);
                normalizedVel = VectorUtils.rotate(normalizedVel, -ship.getFacing() - 90);
                Vector2f.add(newVector, normalizedVel, newVector);
            }
        }

        float angleDifferance = 0;
        if (!VectorUtils.isZeroVector(newVector)) {
            float angle = Misc.getAngleInDegrees(new Vector2f(ship.getVelocity()));
            float accelAngle = Misc.getAngleInDegrees(new Vector2f(newVector)) + ship.getFacing() - 90f;
            angleDifferance = Math.abs(MathUtils.getShortestRotation(angle, accelAngle));
        }

        float boostLevel = (float) Math.pow((minAngle - angleDifferance) / minAngle, 0.4);
        float effectLevel = (float) Math.pow((angleDifferance - minAngle) / (180f - minAngle), 0.4);
        if (angleDifferance <= minAngle || newVector.equals(new Vector2f())) {
            effectLevel = 0f;
            angleDifferance = 0f;
        }

        float shipsBoostRate = boostRate * strafeMulti.get(ship.getHullSize());
        float powerIncrease = shipsBoostRate * effectLevel * amount;
        if (powerIncrease > 0) {
            boostPower += powerIncrease;
            if (boostPower > 1f) boostPower = 1f;
        } else {
            if (boostPower > 0f && !newVector.equals(new Vector2f())) {
                boostPower -= shipsBoostRate* boostLossMulti * boostLevel * amount;
                if (boostPower < 0f) boostPower = 0f;
            }
        }

        /*
        ship.getMutableStats().getAcceleration().modifyMult("vic_assault", 1 + (boostPower / 50f));
        ship.getMutableStats().getDeceleration().modifyMult("vic_assault", 1 + (boostPower / 50f));
         */


        ship.getMutableStats().getAcceleration().modifyPercent("vic_assaultDamper", accelBonus * effectLevel);
        ship.getMutableStats().getDeceleration().modifyPercent("vic_assaultDamper", accelBonus * effectLevel);

        if (effectLevel == 0 && boostPower > 0) {
            ship.getMutableStats().getAcceleration().modifyPercent("vic_assaultBoost", accelBonus * boostLevel);
            ship.getMutableStats().getDeceleration().modifyPercent("vic_assaultBoost", accelBonus * boostLevel);
        } else {
            ship.getMutableStats().getAcceleration().unmodifyPercent("vic_assaultBoost");
            ship.getMutableStats().getDeceleration().unmodifyPercent("vic_assaultBoost");
        }

        if (ship == Global.getCombatEngine().getPlayerShip()) {
            Global.getCombatEngine().maintainStatusForPlayerShip("vic_direction1", "graphics/icons/hullsys/vic_auxThrusters.png", "Booster charge level", Math.round(100 * boostPower) + "%", false);
            Global.getCombatEngine().maintainStatusForPlayerShip("vic_direction2", "graphics/icons/hullsys/vic_auxThrusters.png", "Deceleration boost", Math.round(100 * effectLevel) + "%", false);
        }
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
        if (index == 0) return "0%";
        if (index == 1) return String.valueOf(Math.round(minAngle));
        if (index == 2) return Math.round(accelBonus) + "%";
        if (index == 3) return "180";
        return null;
    }
}




