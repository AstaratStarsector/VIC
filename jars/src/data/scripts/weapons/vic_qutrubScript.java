package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.combat.CombatUtils;

import java.util.*;

public class vic_qutrubScript implements EveryFrameWeaponEffectPlugin {

    private List<DamagingProjectileAPI> alreadyRegisteredProjectiles = new ArrayList<>();

    private boolean runOnce=false, empty=false;

    private Map<CombatEntityAPI, Boolean> detonation= new HashMap<>(); //to check for synch detonations
    private List<CombatEntityAPI> hit = new ArrayList<>(); //to transmit the target from the projectile to the pike missile

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if(!runOnce){
            runOnce=true;
            detonation.clear();
            hit.clear();
        }


        if (engine.isPaused()) {
            return;
        }

        if(!detonation.isEmpty()){
            for(Iterator<CombatEntityAPI> iter = detonation.keySet().iterator(); iter.hasNext(); ){
                CombatEntityAPI entry = iter.next();
                if(detonation.get(entry)){
                    iter.remove();
                }
            }
        }


        //Guidence
        ShipAPI source = weapon.getShip();
        ShipAPI target = null;

        if(source.getWeaponGroupFor(weapon)!=null ){
            //WEAPON IN AUTOFIRE
            if(source.getWeaponGroupFor(weapon).isAutofiring()  //weapon group is autofiring
                    && source.getSelectedGroupAPI()!=source.getWeaponGroupFor(weapon)){ //weapon group is not the selected group
                target = source.getWeaponGroupFor(weapon).getAutofirePlugin(weapon).getTargetShip();
            }
            else {
                target = source.getShipTarget();
            }
        }


        //And clean up our registered projectile list
        List<DamagingProjectileAPI> cloneList = new ArrayList<>(alreadyRegisteredProjectiles);
        for (DamagingProjectileAPI proj : cloneList) {
            if (!engine.isEntityInPlay(proj) || proj.didDamage()) {
                alreadyRegisteredProjectiles.remove(proj);
            }
        }

        for (DamagingProjectileAPI proj : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 200f)) {
            if (proj.getWeapon() == weapon && !alreadyRegisteredProjectiles.contains(proj) && engine.isEntityInPlay(proj) && !proj.didDamage()) {
                engine.addPlugin(new vic_qutrubProjectileScript(proj, target));
                alreadyRegisteredProjectiles.add(proj);
            }
        }
    }

    public void putHIT(CombatEntityAPI target) {
        hit.add(target);
    }

    public List<CombatEntityAPI> getHITS(){
        return hit;
    }

    public void setDetonation(CombatEntityAPI target){
        hit.remove(target);
        if(!detonation.containsKey(target)){
            detonation.put(target, false);
        }
    }

    public boolean getDetonation(CombatEntityAPI target){
        if(detonation.containsKey(target)){
            return detonation.get(target);
        } else {
            return true;
        }
    }

    public void applyDetonation(CombatEntityAPI target){
        if(detonation.containsKey(target)){
            detonation.put(target, true);
        }
    }
}
