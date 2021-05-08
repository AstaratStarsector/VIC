package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.awt.*;

public class vic_phaseDive extends BaseShipSystemScript {

    public static final float SHIP_ALPHA_MULT = 0.25f;

    public static final float MAX_TIME_MULT = 5f;


    protected Object STATUSKEY1 = new Object();
    protected Object STATUSKEY2 = new Object();


    public static float getMaxTimeMult(MutableShipStatsAPI stats) {
        return 1f + (MAX_TIME_MULT - 1f) * stats.getDynamic().getValue(Stats.PHASE_TIME_BONUS_MULT);
    }

    protected void maintainStatus(ShipAPI playerShip, State state, float effectLevel) {

        Color PHASE_COLOR = new Color(0, 234, 255, 255);


        ShipSystemAPI cloak = playerShip.getPhaseCloak();
        if (cloak == null) cloak = playerShip.getSystem();
        if (cloak == null) return;

        Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY2,
                cloak.getSpecAPI().getIconSpriteName(), cloak.getDisplayName(), "time flow altered", false);


    }


    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship;
        boolean player;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            id = id + "_" + ship.getId();
        } else {
            return;
        }

        /*
        if (player) {
            maintainStatus(ship, state, effectLevel);
        }

         */

        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        if (state == State.COOLDOWN || state == State.IDLE) {
            unapply(stats, id);
            return;
        }

        float speedPercentMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).computeEffective(0f);
        speedPercentMod += 100f;
        stats.getMaxSpeed().modifyPercent(id, speedPercentMod * effectLevel);


        if (state == State.IN || state == State.ACTIVE) {
            ship.setPhased(true);
        } else if (state == State.OUT) {
            ship.setPhased(effectLevel > 0.5f);
        }

        ship.setExtraAlphaMult(1f - (1f - SHIP_ALPHA_MULT) * effectLevel);
        ship.setApplyExtraAlphaToEngines(true);

        float shipTimeMult = 1f + (getMaxTimeMult(stats) - 1f) * effectLevel;
        stats.getTimeMult().modifyMult(id, shipTimeMult);
        if (player) {
            Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
        } else {
            Global.getCombatEngine().getTimeMult().unmodify(id);
        }
    }


    public void unapply(MutableShipStatsAPI stats, String id) {

        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        Global.getCombatEngine().getTimeMult().unmodify(id);
        stats.getTimeMult().unmodify(id);

        stats.getMaxSpeed().unmodifyPercent(id);

        ship.setPhased(false);
        ship.setExtraAlphaMult(1f);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("time flow altered", false);
        }
        return null;
    }
}
