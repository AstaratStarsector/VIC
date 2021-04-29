package data.scripts.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class vic_OmniLunge extends BaseShipSystemScript {

    public static float SPEED_BONUS = 250f;
    public static float TURN_BONUS = 100f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();

        stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);

        stats.getTurnAcceleration().modifyFlat(id, TURN_BONUS);
        stats.getTurnAcceleration().modifyPercent(id, TURN_BONUS * 5f);
        stats.getMaxTurnRate().modifyFlat(id, 60f);
        stats.getMaxTurnRate().modifyPercent(id, 100f);

        if (state == State.IN) {
            stats.getAcceleration().modifyMult(id, 50f);
            stats.getDeceleration().modifyMult(id, 50f);
        }
        if (state == State.ACTIVE) {
            stats.getAcceleration().modifyMult(id, 0f);
            stats.getDeceleration().modifyMult(id, 0f);
        }
        if (state == State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
			/*
			if (MathUtils.getDistance(stats.getEntity().getVelocity(), new Vector2f()) > ship.getEngineController().getMaxSpeedWithoutBoost())
				stats.getEntity().getVelocity().scale(0.95f);

			 */
            stats.getAcceleration().modifyMult(id, 10f);
            stats.getDeceleration().modifyMult(id, 20f);
            stats.getMaxTurnRate().unmodify(id);
        }

        if (stats.getEntity() instanceof ShipAPI) {
            ship.getEngineController().extendFlame(this, 0.5f * effectLevel, 0.5f * effectLevel, 0.25f * effectLevel);
        }

    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            if (state == State.IN) {
                return new StatusData("IN", false);
            }
            if (state == State.ACTIVE) {
                return new StatusData("ACTIVE", false);
            }
            if (state == State.OUT) {
                return new StatusData("OUT", false);
            }
        } else if (index == 1) {
            return new StatusData("+" + (int) SPEED_BONUS + " top speed", false);
        }
        return null;
    }

    @Override
    public float getRegenOverride(ShipAPI ship) {
        if (ship.getVariant().hasHullMod("vic_ShturmSolution")) {
            return 0.15f;
        } else {
            return -1;
        }
    }
}








