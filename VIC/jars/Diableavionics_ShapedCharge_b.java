/*Armour Penetration / Shield Ricochet script by Xangle13, updated by Debido
 * This script allows a missile/projectile to simulate certain behaviors
 * 1. Successfully hitting projectiles will pass through and exit the opposite side of a ship if the armour is low enough
 * 2. The projectile will bounce off the enemies shield if it hits at an oblique enough angle
 */

/*
 This is the instant pass through variant, projectile(s) will only 'pass through' if the exit side has armour that is compromised.
 Dev version, commented out pop up text.
 */
package data.scripts.weapons;

import data.scripts.plugins.DiableAvionics_delayedProjectilePlugin;
import data.scripts.plugins.DiableAvionics_debrisPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.DefenseUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_ShapedCharge_b implements OnHitEffectPlugin {

    public float pitch = 1.0f; //sound pitch. Default seems to be 1
    public float volume = 1.0f; //volume, scale from 0-1
    public String soundName = "diableavionics_explosionA";
    private final boolean dealsSoftFlux = false;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        float ENTER_FACTOR = 0.1f;
        float EXIT_FACTOR = 0.2f;
        //if the target is a ship, that is to say it will not work on asteroids
        if (target instanceof ShipAPI) {
            ShipAPI sourceShip = projectile.getSource();

            //check the source ship is not a null entity
            if (sourceShip != null) {

                //get the weapon that fired the projectile
                WeaponAPI weaponFrom = projectile.getWeapon();

                //if the weapon that fired the projectile is not null
                if (weaponFrom != null) {

                    //get the projectile location and velocity. Location currently not used
                    Vector2f projectileVelocity = projectile.getVelocity();
                    //get the angle at which the projectile is hitting the ship
                    float fromAngle = projectile.getFacing();
                    //get the damage amount
                    float damageAmount = projectile.getDamageAmount()/2;

                    //get the emp amount
                    float empAmount = projectile.getEmpAmount();

                    //get the damage type
                    DamageType damageType = projectile.getDamageType();

                    if (shieldHit) {

                        engine.applyDamage(target, point, damageAmount * (float) (Math.random() * 0.25 + 0.25), damageType, empAmount, false, false, null);

                    } else {
                        // get the targets armor grid
                        ArmorGridAPI targetArmorGrid = ((ShipAPI) target).getArmorGrid();

                        //get the armor rating
                        float targetArmorRating = targetArmorGrid.getArmorRating();

                        //get the armor level of the cell where the projectile is hitting
                        float targetArmorLevel = DefenseUtils.getArmorLevel((ShipAPI) target, point) * (targetArmorRating / 15);

                        //calculate the penetration value against the current armor level at the hit point using 0.1f as a fudge factor.
                        float penetrateValue = (damageAmount * ENTER_FACTOR) - targetArmorLevel;

                        if ((penetrateValue > 0) && (targetArmorLevel >= 0) && ((float) Math.random() <= (penetrateValue / targetArmorLevel))) {
                            Vector2f penetrateVelocity = new Vector2f(projectileVelocity.getX(), projectileVelocity.getY());
                            penetrateVelocity = penetrateVelocity.normalise(penetrateVelocity);
                            Vector2f penetrateLocation = new Vector2f(point.getX() + penetrateVelocity.getX() * 50f, point.getY() + penetrateVelocity.getY() * 50f);

                            //project the pentration vector through the ship to an imaginary point beyond the ship on the opposite side
                            float projectedLocationX = (float) (1200f * FastTrig.cos(Math.toRadians(projectile.getFacing())) + penetrateLocation.x);
                            float projectedLocationY = (float) (1200f * FastTrig.sin(Math.toRadians(projectile.getFacing())) + penetrateLocation.y);

                            //location as vector2f
                            Vector2f projectedLocation = new Vector2f(projectedLocationX, projectedLocationY);

                            //derive exit location with Lazylib collision utils. This basically uses the imaginary point and the hit point, then checks every segment of the hit boundary segments for an intersect then return the Vector2f value of the closest point where the the two line and the boundary segment meet
                            //using this vector direction it will use the farthest point on the ship for the exit
                            //this method will consistently get the outside boundary
                            //Vector2f exitLocation = CollisionUtils.getCollisionPoint(projectedLocation, penetrateLocation, target);
                            
                            //use this vector direction to determine if it should exit at the nearest point
                            //this method does not consistently get the nearest point due to issues probably with other authors ship boundaries.
                            Vector2f exitLocation = CollisionUtils.getCollisionPoint(penetrateLocation, projectedLocation, target);

                            if (null != exitLocation) {
                                float passThroughShipDistance = MathUtils.getDistance(point, exitLocation);

                                //So let's check IF the imaginary projectile can get 'through' the full distance of internal hull, let's use a value of 1000px, a very very big ship! ie. Zorg
                                if (passThroughShipDistance > 1000f) {
                                    //apply bonus RNG damage to entry point.
                                    damageAmount = (float)(damageAmount + 20f * Math.random());
                                    engine.applyDamage(target, point, damageAmount, DamageType.FRAGMENTATION, empAmount, false, dealsSoftFlux, projectile.getSource());
                                    Global.getSoundPlayer().playSound(soundName, 1f, 1f,target.getLocation(), target.getVelocity());
                                    //we're done, no exit
                                    return;
                                }
                                //Now let's check how strong the armor is on the exit location, and can we penetrate the otherside
                                float targetArmorExitLevel = DefenseUtils.getArmorLevel((ShipAPI) target, exitLocation) * (targetArmorRating / 15);

                                float penetrateExitValue = (damageAmount * EXIT_FACTOR) - targetArmorExitLevel;

                                if ((penetrateExitValue > 0) && (targetArmorExitLevel >= 0) && ((float) Math.random() <= (penetrateExitValue / targetArmorExitLevel))) {

                                    //let's do a simple test if the distance through the ship, we have two versions
                                    // if (passThroughShipDistance > projectile.getVelocity().length() || (passThroughShipDistance - projectile.getVelocity().length()) < 100f){
//                                        float newHull = ((ShipAPI)target).getHitpoints() - projectile.getDamageAmount() * 0.5f;
//                                        ((ShipAPI)target).setHitpoints(newHull);
//                                        return;
//                                    }
                                    float passThroughShipTime = passThroughShipDistance / projectile.getVelocity().length();

                                    DiableAvionics_debrisPlugin.startFire(
                                            target,
                                            exitLocation,
                                            projectile.getDamageAmount() * 0.1f,
                                            5f,
                                            projectile.getSource(),
                                            fromAngle,
                                            passThroughShipTime,
                                            projectile
                                    );

                                    DiableAvionics_delayedProjectilePlugin.startFire(
                                            target,
                                            exitLocation,
                                            projectile.getDamageAmount(),
                                            projectile.getSource(),
                                            fromAngle, passThroughShipTime,
                                            projectile.getWeapon().getId(),
                                            projectile.getVelocity().length(),
                                            projectile
                                    );

                                    //engine.applyDamage(target, point, damageAmount * (float) (Math.random() * 1 + 1), damageType, empAmount, true, true, null);
                                    
                                    //Pop Up Text
                                    //engine.addFloatingText(penetrateLocation, "Penetrated", 30, new Color(255, 0, 0, 255), target, 1f, 2f);
                                    Global.getSoundPlayer().playSound(soundName, 1f, 1f,target.getLocation(), target.getVelocity());
                                    engine.spawnExplosion(penetrateLocation, new Vector2f(penetrateVelocity.getX() * 250f, penetrateVelocity.getY() * 250f), new Color(225, 200, 50, 200), damageAmount / 70f, 0.5f);
                                    damageAmount += (20f * Math.random());
                                    engine.applyDamage(target, point, damageAmount, DamageType.HIGH_EXPLOSIVE, empAmount, false, dealsSoftFlux, projectile.getSource());

                                } else {
                                    // So if the shaped charge cannot penetrate all the way through, it will start causing damage on the opposite side of the ship.
								
                                    damageAmount += (20f * Math.random());
                                    engine.applyDamage(target, exitLocation, damageAmount, DamageType.HIGH_EXPLOSIVE, empAmount, true, dealsSoftFlux, projectile.getSource());
                                }
                                //There are some strange cases where where the algorithm to find the exit location will not return one, this else statement captures that
                            } else {
                                Global.getSoundPlayer().playSound(soundName, 1f, 1f,target.getLocation(), target.getVelocity());
                                engine.spawnExplosion(penetrateLocation, new Vector2f(penetrateVelocity.getX() * 250f, penetrateVelocity.getY() * 250f), new Color(225, 200, 50, 200), damageAmount / 70f, 0.5f);

                                //spawn exit projectile(s) this will spawn 10 projectiles, there is not need for a loop if you just want to spawn one projectile
                                for (int i = 0; i < 10; i++) {
                                    String projID = "debris_launcher" + Integer.toString((int)MathUtils.getRandomNumberInRange(0f, 8f));
                                    engine.spawnProjectile(null, null, projID, penetrateLocation, fromAngle + MathUtils.getRandomNumberInRange(-45f, 45f), null);
                                }

                                //engine.applyDamage(target, point, damageAmount * (float) (Math.random() * 1 + 1), damageType, empAmount, true, true, null);
                                
                                //Pop Up Text
                                //engine.addFloatingText(penetrateLocation, "Bypass", 30, new Color(255, 0, 0, 255), target, 1f, 2f);
                            }
                        }
                    }
                }
            }
        }
    }
}
