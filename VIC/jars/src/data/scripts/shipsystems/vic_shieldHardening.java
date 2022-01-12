package data.scripts.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class vic_shieldHardening extends BaseShipSystemScript {

    public static float
            shieldDamageTakenReduction = 0.5f,
            weaponRoFReduction = 0.25f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        stats.getShieldDamageTakenMult().modifyMult(id, 1f - shieldDamageTakenReduction * effectLevel);

        stats.getBallisticRoFMult().modifyMult(id, 1f - weaponRoFReduction * effectLevel);

        stats.getEnergyRoFMult().modifyMult(id, 1f - weaponRoFReduction * effectLevel);

    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getShieldDamageTakenMult().unmodify(id);
        stats.getBallisticRoFMult().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Shield damage taken -" + Math.round(shieldDamageTakenReduction * 100f) + "%", false);
        } else if (index == 1){
            return new StatusData("Weapon rate of fire -" + Math.round(weaponRoFReduction * 100f) + "%", true);
        }
        return null;
    }
}
