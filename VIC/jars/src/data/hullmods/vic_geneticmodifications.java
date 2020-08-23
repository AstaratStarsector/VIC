package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

import java.util.HashMap;
import java.util.Map;

public class vic_geneticmodifications extends BaseHullMod {


    private final float TIME_ACCELERATION_BONUS = 5f;
    private final float ACCELERATION_BONUS = 10f;
    private final float DAMAGE_PENALTY = 10f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {


        stats.getTimeMult().modifyPercent(id, TIME_ACCELERATION_BONUS);
        stats.getAcceleration().modifyPercent(id, ACCELERATION_BONUS);
        stats.getTurnAcceleration().modifyPercent(id, ACCELERATION_BONUS);
        stats.getShieldDamageTakenMult().modifyPercent(id, DAMAGE_PENALTY);
        stats.getArmorDamageTakenMult().modifyPercent(id, DAMAGE_PENALTY);
        stats.getHullDamageTakenMult().modifyPercent(id, DAMAGE_PENALTY);
    }

        public String getDescriptionParam ( int index, HullSize hullSize){
            if (index == 0) return Math.round(TIME_ACCELERATION_BONUS) + "%";
            if (index == 1) return Math.round(ACCELERATION_BONUS) + "%";
            if (index == 2) return Math.round(DAMAGE_PENALTY) + "%";
            return null;
        }


    }

