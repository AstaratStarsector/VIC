//By Tartiflette modified by PureTilt
package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;

public class vic_azhdaya_script implements EveryFrameWeaponEffectPlugin {

    private float delay = 0.1f;
    private float timer = 0;
    private float SPINUP = 0.02f;
    private float SPINDOWN = 10f;

    private boolean runOnce=false;
    private boolean hidden=false;
    private AnimationAPI theAnim;
    private int maxFrame;
    private int frame;

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

        //play the spinning sound
        if (weapon.getChargeLevel()>0){

            Global.getSoundPlayer().playLoop(
                    "vic_azhdaya_shot",
                    weapon,
                    1,
                    Math.max(0,10*weapon.getChargeLevel()-9),
                    weapon.getLocation(),
                    weapon.getShip().getVelocity()
            );

            Global.getSoundPlayer().playLoop(
                    "vic_azhdaya_spin",
                    weapon,
                    0.25f+1f*weapon.getChargeLevel(),
                    3f,
                    weapon.getLocation(),
                    weapon.getShip().getVelocity()
            );
        }

        if (!hidden){
            theAnim.setFrame(frame);
        }
    }
}