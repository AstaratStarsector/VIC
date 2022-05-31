//By Tartiflette modified by PureTilt
package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_azhdaya_script implements EveryFrameWeaponEffectPlugin, OnHitEffectPlugin, OnFireEffectPlugin {

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
        if (engine.isPaused() || weapon.getShip().getOriginalOwner() == -1) return;

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

        float shootPitch = 1f;

        if (weapon.getShip().getSystem().isActive()){
            shootPitch = 1.2f;
        }


        if (weapon.getChargeLevel()>0){

            Global.getSoundPlayer().playLoop(
                    "vic_azhdaya_shot",
                    weapon,
                    shootPitch,
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

    private final DamagingExplosionSpec explosion = new DamagingExplosionSpec(0.05f,
            20f,
            10f,
            250,
            125f,
            CollisionClass.PROJECTILE_FF,
            CollisionClass.PROJECTILE_FIGHTER,
            1,
            2,
            0.5f,
            10,
            new Color(255, 211, 33, 255),
            new Color(255, 185, 35, 255)
    );

    private final DamagingExplosionSpec explosionHE = new DamagingExplosionSpec(0.05f,
            60f,
            30f,
            250,
            125f,
            CollisionClass.PROJECTILE_FF,
            CollisionClass.PROJECTILE_FIGHTER,
            1,
            2,
            0.5f,
            10,
            new Color(255, 144, 33, 255),
            new Color(255, 101, 35, 255)
    );


    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        float random = (float) Math.random();
        if (random >= 0.95){
            explosion.setDamageType(DamageType.HIGH_EXPLOSIVE);
            float damage = projectile.getDamageAmount() * 8.3333333333333333333333333333333f;
            explosion.setMinDamage(damage * 0.5f);
            explosion.setMaxDamage(damage);
            explosion.setShowGraphic(false);
            engine.spawnDamagingExplosion(explosion, projectile.getSource(), point);
            engine.spawnExplosion(point, new Vector2f(), new Color(255, 118, 33, 255), 60, 0.5f);
        } else {
            explosionHE.setDamageType(DamageType.FRAGMENTATION);
            float damage = projectile.getDamageAmount() * 0.33333333333333333333333333333333f;
            explosionHE.setMinDamage(damage * 0.5f);
            explosionHE.setMaxDamage(damage);
            explosionHE.setShowGraphic(false);
            engine.spawnDamagingExplosion(explosionHE, projectile.getSource(), point);
            engine.spawnExplosion(point, new Vector2f(), new Color(255, 225, 33, 255), 5, 0.1f);
        }
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if (weapon.getShip().getSystem().getEffectLevel() == 1){
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
            engine.addPlugin(new vic_azhdayaHoming(projectile, target));
        }
    }

}