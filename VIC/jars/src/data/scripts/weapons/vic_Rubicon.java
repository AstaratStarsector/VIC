package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class vic_Rubicon implements EveryFrameWeaponEffectPlugin {


    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (engine.isPaused() || weapon.getShip().getOriginalOwner() == -1) return;

        weapon.getSpec().setMinSpread(weapon.getSpec().getMinSpread() + amount);
        


        if (weapon.getCurrSpread() >= weapon.getSpec().getMaxSpread()){
            weapon.ensureClonedSpec();
            Global.getCombatEngine().maintainStatusForPlayerShip("vic_spread" + weapon.getSlot(), "graphics/icons/hullsys/vic_adaptiveWarfareSystem.png", "spread", "no bread", false);
            weapon.getSpec().setSpreadDecayRate(0);
        } else {
            weapon.ensureClonedSpec();
            Global.getCombatEngine().maintainStatusForPlayerShip("vic_spread" + weapon.getSlot(), "graphics/icons/hullsys/vic_adaptiveWarfareSystem.png", "spread", "bread", false);
            weapon.getSpec().setSpreadDecayRate(-10);
        }
        Global.getCombatEngine().maintainStatusForPlayerShip("vic_spread" + weapon.getSlot(), "graphics/icons/hullsys/vic_adaptiveWarfareSystem.png", "spread dec", weapon.getSpec().getMinSpread() + "", false);
        //weapon.getSpec().setSpreadDecayRate(1);


    }
}