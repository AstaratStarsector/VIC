//By Tartiflette
package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;

public class vic_vodyanoy_script implements EveryFrameWeaponEffectPlugin{
        
    private float delay = 0.1f;
    private float timer = 0;
    private float SPINUP = 0.02f;
    private float SPINDOWN = 10f;
    
    private boolean runOnce=false;
    private boolean hidden=false;
    private AnimationAPI theAnim;
    private int maxFrame;
    private int frame;
    private float shot = 0;
    
    @Override
    public void advance (float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(engine.isPaused()){return;}
        
        if(!runOnce){
            runOnce=true;
            if(weapon.getSlot().isHidden()){
                hidden=true;
            } else {
                theAnim=weapon.getAnimation();
                maxFrame=theAnim.getNumFrames();
                frame=MathUtils.getRandomNumberInRange(0, maxFrame-1);
            }               
            SPINUP=0.03f;
            SPINDOWN=7.5f;
        }
        
        timer+=amount;
        if (timer >= delay){
            timer-=delay;
            if (weapon.getChargeLevel()>0){
                delay = Math.max(
                            delay - SPINUP,
                            0.02f
                        );
            } else {
                delay = Math.min(
                            delay + delay/SPINDOWN,
                            0.1f
                        );
            }
            if (!hidden && delay!=0.1f){
                frame++;
                if (frame==maxFrame){
                    frame=0;
                }
            }
        }


        if (MagicRender.screenCheck(0.25f, weapon.getLocation())) {
            if (weapon.getChargeLevel() > 0) shot = 1;
            if (shot == 1) shot = 2;
            if (shot == 2) {
                for (DamagingProjectileAPI p : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 200)) {

                    if (p.getWeapon() != weapon) continue;

                    p.getVelocity().scale(MathUtils.getRandomNumberInRange(0.9f, 1.1f));

                }
                shot = 0;
            }
        }


        //play the spinning sound
        if (weapon.getChargeLevel()>0){       
            
            Global.getSoundPlayer().playLoop(
                    "diableavionics_opfer_fire",
                    weapon,
                    1,
                    Math.max(0,10*weapon.getChargeLevel()-9),
                    weapon.getLocation(),
                    weapon.getShip().getVelocity()
            );
            
            Global.getSoundPlayer().playLoop(
                    "diableavionics_opfer_spin",
                    weapon,
                    0.25f+1f*weapon.getChargeLevel(),
                    0.25f,
//                    0.5f+0.5f*weapon.getChargeLevel(),
                    weapon.getLocation(),
                    weapon.getShip().getVelocity()
            );            
        }
        
        if (!hidden){            
            theAnim.setFrame(frame);
        }
    }
}
