package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.CombatUtils;

import java.util.*;

public class vic_tugarinScript implements EveryFrameWeaponEffectPlugin {

    private final List<DamagingProjectileAPI> alreadyRegisteredProjectiles = new ArrayList<>();

    private final IntervalUtil
            checkTime = new IntervalUtil(0.5f, 0.5f);

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (engine.isPaused()) {
            return;
        }

        checkTime.advance(amount);
        //And clean up our registered projectile list
        if (checkTime.intervalElapsed()){
            List<DamagingProjectileAPI> cloneList = new ArrayList<>(alreadyRegisteredProjectiles);
            for (DamagingProjectileAPI proj : cloneList) {
                if (!engine.isEntityInPlay(proj) || proj.didDamage()) {
                    alreadyRegisteredProjectiles.remove(proj);
                }
            }
        }

        for (DamagingProjectileAPI proj : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 200f)) {
            if (proj.getWeapon() == weapon && !alreadyRegisteredProjectiles.contains(proj) && engine.isEntityInPlay(proj) && !proj.didDamage()) {
                engine.addPlugin(new vic_tugarinProjectileScript(proj));
                alreadyRegisteredProjectiles.add(proj);
            }
        }
    }
}
