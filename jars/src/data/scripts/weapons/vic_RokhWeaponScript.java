package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

import static data.scripts.plugins.vic_combatPlugin.AddRokh;

public class vic_RokhWeaponScript extends vic_missileFluxGen {

    boolean doOnce = true;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        super.advance(amount, engine, weapon);
        if (doOnce && weapon.getShip().getOriginalOwner() != -1){
            AddRokh(weapon);
            doOnce = false;
        }
    }
}
