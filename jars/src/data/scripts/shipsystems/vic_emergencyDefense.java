package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class vic_emergencyDefense extends BaseShipSystemScript {

    public static final Object KEY_JITTER = new Object();
    public static final Color JITTER_UNDER_COLOR = new Color(255, 50, 0, 125);
    public static final Color JITTER_COLOR = new Color(255, 50, 0, 75);
    public final float
            damageMult = 1.3f,
            speedMult = 2f,
            damageTakenMult = 0.7f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        stats.getFighterWingRange().modifyMult(id, 0.2f);


        if (!ship.isAlive()) return;
        ship.blockCommandForOneFrame(ShipCommand.PULL_BACK_FIGHTERS);
        if (!ship.isPullBackFighters()) {
            ship.setPullBackFighters(true);
        }


        if (effectLevel > 0) {
            float maxRangeBonus = 5f;
            float jitterRangeBonus = effectLevel * maxRangeBonus;
            for (ShipAPI fighter : getFighters(ship)) {
                if (fighter.isHulk()) continue;
                MutableShipStatsAPI fStats = fighter.getMutableStats();

                fStats.getBallisticWeaponDamageMult().modifyMult(id, damageMult);
                fStats.getEnergyWeaponDamageMult().modifyMult(id, damageMult);
                fStats.getMissileWeaponDamageMult().modifyMult(id, damageMult);

                fStats.getArmorDamageTakenMult().modifyMult(id, damageTakenMult);
                fStats.getShieldDamageTakenMult().modifyMult(id, damageTakenMult);
                fStats.getHullDamageTakenMult().modifyMult(id, damageTakenMult);

                fStats.getMaxSpeed().modifyMult(id, speedMult);
                fStats.getAcceleration().modifyMult(id, speedMult);
                fStats.getDeceleration().modifyMult(id, speedMult);
                fStats.getMaxTurnRate().modifyMult(id, speedMult);
                fStats.getTurnAcceleration().modifyMult(id, speedMult);

                if (effectLevel > 0) {
                    //fighter.setWeaponGlow(effectLevel, Misc.setAlpha(JITTER_UNDER_COLOR, 255), EnumSet.allOf(WeaponAPI.WeaponType.class));

                    fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, effectLevel, 5, 0f, jitterRangeBonus);
                    fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, effectLevel, 2, 0f, 0 + jitterRangeBonus * 1f);
                    Global.getSoundPlayer().playLoop("system_targeting_feed_loop", ship, 1f, 1f, fighter.getLocation(), fighter.getVelocity());
                }
            }
        }

    }


    private List<ShipAPI> getFighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<>();

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (!ship.isFighter()) continue;
            if (ship.getWing() == null) continue;
            if (ship.getWing().getSourceShip() == carrier) {
                result.add(ship);
            }
        }

        return result;
    }


    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getFighterWingRange().unmodify(id);
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        for (ShipAPI fighter : getFighters(ship)) {
            if (fighter.isHulk()) continue;
            MutableShipStatsAPI fStats = fighter.getMutableStats();
            fStats.getBallisticWeaponDamageMult().unmodify(id);
            fStats.getEnergyWeaponDamageMult().unmodify(id);
            fStats.getMissileWeaponDamageMult().unmodify(id);

            fStats.getArmorDamageTakenMult().unmodify(id);
            fStats.getShieldDamageTakenMult().unmodify(id);
            fStats.getHullDamageTakenMult().unmodify(id);

            fStats.getMaxSpeed().unmodify(id);
            fStats.getAcceleration().unmodify(id);
            fStats.getDeceleration().unmodify(id);
            fStats.getMaxTurnRate().unmodify(id);
            fStats.getTurnAcceleration().unmodify(id);
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {

        return null;
    }

}
