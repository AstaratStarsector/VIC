package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import data.scripts.plugins.vic_weaponDamageListener;
import org.magiclib.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
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
                    float EMP = damage.getDamage() * 0.075f * (1f - fluxFaction);
                    float damAmount = damage.getDamage() * 0.1f - EMP;
                    if (target instanceof ShipAPI) {
                        if (((ShipAPI) target).getFluxTracker().isOverloaded()) {
                            fluxFaction = 1;
                        } else {
                            fluxFaction = ((ShipAPI) target).getFluxLevel();
                        }
                        EMP = damage.getDamage() * 0.075f * (1f - fluxFaction);
                        damAmount = damage.getDamage() * 0.1f - EMP;
                        if (((ShipAPI) target).getFluxTracker().isOverloaded()) {
                            EMP = damage.getDamage() * 0.0375f ;
                            damAmount = damage.getDamage() * 0.1f;
                        }
                        EMP *= 2;
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

                    MagicRender.battlespace(
                            Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow"),
                            point,
                            new Vector2f(),
                            new Vector2f(80 * MathUtils.getRandomNumberInRange(0.8f, 1.2f), 800 * MathUtils.getRandomNumberInRange(0.8f, 1.2f)),
                            new Vector2f(),
                            360 * (float) Math.random(),
                            0,
                            new Color(136, 255, 209, 255),
                            true,
                            0,
                            0,
                            0.5f,
                            0.15f,
                            MathUtils.getRandomNumberInRange(0.05f, 0.2f),
                            0,
                            MathUtils.getRandomNumberInRange(0.6f, 0.8f),
                            MathUtils.getRandomNumberInRange(0.2f, 0.5f),
                            CombatEngineLayers.CONTRAILS_LAYER
                    );

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

                        MagicRender.battlespace(
                                Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow"),
                                point,
                                new Vector2f(),
                                new Vector2f(80 * MathUtils.getRandomNumberInRange(0.8f, 1.2f), 800 * MathUtils.getRandomNumberInRange(0.8f, 1.2f)),
                                new Vector2f(),
                                360 * (float) Math.random(),
                                0,
                                new Color(136, 255, 209, 255),
                                true,
                                0,
                                0,
                                0.5f,
                                0.15f,
                                MathUtils.getRandomNumberInRange(0.05f, 0.2f),
                                0,
                                MathUtils.getRandomNumberInRange(0.6f, 0.8f),
                                MathUtils.getRandomNumberInRange(0.2f, 0.5f),
                                CombatEngineLayers.CONTRAILS_LAYER
                        );

                    }

                    damage.getModifier().modifyMult("vic_damage", 0);
                    return "vic_damage";


                }
            }
            return null;
        }
    }
}
