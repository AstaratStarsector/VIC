package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import data.scripts.plugins.vic_weaponDamageListener;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_falakScript extends vic_missileFluxGen{

    boolean doOnce = true;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        super.advance(amount, engine, weapon);

        if (doOnce) {
            if (!weapon.getShip().hasListenerOfClass(vic_falakListner.class)){
                weapon.getShip().addListener(new vic_falakListner());
            }
            doOnce = false;
        }
        /*
        int alpha = 0;
        if (weapon.getShip().getOwner() == -1 || (weapon.getCooldownRemaining() == 0 && weapon.getAmmo() >= 1)) alpha = 255;
        if (weapon.getBarrelSpriteAPI() != null) weapon.getBarrelSpriteAPI().setColor(new Color(255,255,255,alpha));

         */

    }

    public static class vic_falakListner implements DamageDealtModifier {

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (param instanceof MissileAPI) {
                if (((DamagingProjectileAPI) param).getProjectileSpecId().equals("vic_falak_main")) {
                    float fluxFaction = 0;
                    if (target instanceof ShipAPI) {
                        if (((ShipAPI) target).getFluxTracker().isOverloaded()) {
                            fluxFaction = 1;
                        } else {
                            fluxFaction = ((ShipAPI) target).getFluxLevel();
                        }
                    }
                    float EMP = damage.getDamage() * 0.075f * (1f - fluxFaction);
                    float damAmount = damage.getDamage() * 0.1f - EMP;
                    if (((ShipAPI) target).getFluxTracker().isOverloaded()) {
                        EMP = damage.getDamage() * 0.0375f ;
                        damAmount = damage.getDamage() * 0.1f;
                    }
                    //Global.getCombatEngine().addFloatingText(point, damAmount + "", 20, Color.WHITE, null, 0,0);
                    for (int i =0; i < 10; i++){
                        Global.getCombatEngine().spawnEmpArc(((MissileAPI) param).getSource(),
                                point,
                                target,
                                target,
                                DamageType.ENERGY,
                                damAmount,
                                EMP,
                                10000,
                                null,
                                5 + 10 * fluxFaction,
                                Color.cyan,
                                Color.white);
                    }

                    if (shieldHit) {
                        for (int i = 0; i < 10; i++) {
                            Global.getCombatEngine().spawnEmpArcPierceShields(((MissileAPI) param).getSource(),
                                    point,
                                    target,
                                    target,
                                    DamageType.ENERGY,
                                    0,
                                    EMP,
                                    10000,
                                    null,
                                    5 + 10 * fluxFaction,
                                    Color.cyan,
                                    Color.white);
                        }
                    }

                    damage.getModifier().modifyMult("vic_damage", 0);
                    return "vic_damage";


                }
            }
            return null;
        }
    }
}
