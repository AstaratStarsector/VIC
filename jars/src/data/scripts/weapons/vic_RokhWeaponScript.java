package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

import java.awt.*;

import static data.scripts.plugins.vic_combatPlugin.AddRokh;

public class vic_RokhWeaponScript extends vic_missileFluxGen {

    boolean doOnce = true;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        super.advance(amount, engine, weapon);
        if (doOnce && weapon.getShip().getOriginalOwner() != -1){
            AddRokh(weapon);
            int maxAmmo = weapon.getMaxAmmo();
            if (maxAmmo > 1){
                weapon.getAmmoTracker().setAmmoPerSecond(weapon.getAmmoTracker().getAmmoPerSecond() * (1 + (0.2f * (maxAmmo - 1))));
            }
            weapon.setMaxAmmo(1);
            weapon.getBarrelSpriteAPI().setColor(new Color(0,0,0,0));
            doOnce = false;
        }
    }
}
