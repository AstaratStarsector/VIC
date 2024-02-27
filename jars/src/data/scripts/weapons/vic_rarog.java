package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import org.magiclib.util.MagicAnim;

public class vic_rarog implements BeamEffectPlugin {

    float changeAmount = 0;
    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        changeAmount += amount * 5;
        if (changeAmount >= 1) changeAmount = 1;
        beam.setWidth(40 * MagicAnim.smooth(changeAmount));
    }
}
