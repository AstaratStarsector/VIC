package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;

public class vic_thermalLance_beam implements BeamEffectPlugin {

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (beam.getWeapon().getChargeLevel() < 0.9f){
            beam.setWidth(20);
        } else {
            beam.setWidth(55 * (float) Math.pow( beam.getWeapon().getChargeLevel(), 4f));
        }
    }
}
