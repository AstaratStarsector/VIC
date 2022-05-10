package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.plugins.vic_combatPlugin;
import data.scripts.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

import static data.scripts.plugins.vic_combatPlugin.AddHunterDriveTarget;

public class vic_hunterDrive extends BaseShipSystemScript {

    float
            speedBoost = 350,
            accBoost = 200,
            waveRange = 1000,
            waveDuration = 0.5f,
            currWaveDuration = 0f;

    boolean doOnce = true;

    ArrayList<ShipAPI> affectedShips = new ArrayList<>();

    HashMap<ShipAPI.HullSize, Float> arcMulti = new HashMap<>();
    {
        arcMulti.put(ShipAPI.HullSize.FIGHTER, 2f);
        arcMulti.put(ShipAPI.HullSize.FRIGATE, 4f);
        arcMulti.put(ShipAPI.HullSize.DESTROYER, 5f);
        arcMulti.put(ShipAPI.HullSize.CRUISER, 7f);
        arcMulti.put(ShipAPI.HullSize.CAPITAL_SHIP, 9f);
    }


    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        if (stats.getEntity() == null) return;
        ShipAPI ship = (ShipAPI) stats.getEntity();
        switch (state) {
            case IN:
            case ACTIVE:
                stats.getMaxSpeed().modifyFlat(id, speedBoost);
                stats.getAcceleration().modifyFlat(id, accBoost);
                doOnce = true;
                break;
            case OUT:
                if (doOnce) {
                    //vic_combatPlugin.AddHunterDriveAnimation(ship);
                    doOnce = false;

                    currWaveDuration = 0f;
                    affectedShips.clear();

                    float rotation = (float) Math.random() * 360;

                    Vector2f LocPulse = ship.getLocation();

                    for (int i = 0; i < 2; i++){
                        float spin = MathUtils.getRandomNumberInRange(-120, 120);
                        MagicRender.battlespace(
                                Global.getSettings().getSprite("fx", "vic_stolas_emp_main"),
                                LocPulse,
                                ship.getVelocity(),
                                new Vector2f(200f, 200f),
                                (Vector2f) new Vector2f((waveRange) * 4, (waveRange) * 4),
                                rotation,
                                spin,
                                new Color(MathUtils.getRandomNumberInRange(220,255),MathUtils.getRandomNumberInRange(220,255),MathUtils.getRandomNumberInRange(220,255),MathUtils.getRandomNumberInRange(130,170)),
                                true,
                                0, 0, 0.4f, 0.8f, 0,
                                0f,
                                0.1f,
                                0.2f,
                                CombatEngineLayers.BELOW_SHIPS_LAYER
                        );

                        MagicRender.battlespace(
                                Global.getSettings().getSprite("fx", "vic_stolas_emp_secondary"),
                                LocPulse,
                                ship.getVelocity(),
                                new Vector2f(200f, 200f),
                                (Vector2f) new Vector2f((waveRange) * 4, (waveRange) * 4),
                                rotation,
                                spin * -1,
                                new Color(MathUtils.getRandomNumberInRange(220,255),MathUtils.getRandomNumberInRange(220,255),MathUtils.getRandomNumberInRange(220,255),MathUtils.getRandomNumberInRange(130,170)),
                                true,
                                0, 0, 0.4f, 0.8f, 0,
                                0.3f,
                                0.1f,
                                0.25f,
                                CombatEngineLayers.BELOW_SHIPS_LAYER
                        );
                    }

                    Global.getCombatEngine().addHitParticle(
                            ship.getLocation(),
                            ship.getVelocity(),
                            1000,
                            0.6f,
                            //0,
                            0.2f,
                            Color.WHITE);
                }
                if (currWaveDuration <= waveDuration){
                    currWaveDuration += amount;
                    for (ShipAPI target : AIUtils.getNearbyEnemies(ship, waveRange * currWaveDuration / waveDuration)){
                        if (!affectedShips.contains(target)){
                            affectedShips.add(target);
                            if (target.isPhased()){
                                target.getFluxTracker().beginOverloadWithTotalBaseDuration(1f);
                            }
                            Vector2f empPos = new Vector2f((ship.getLocation().x + target.getLocation().x) * 0.5f, (ship.getLocation().y + target.getLocation().y) * 0.5f);
                            float damage = 0;
                            if (target.isFighter()) damage = 150;
                            for (int i = 0; i < arcMulti.get(target.getHullSize()); i++){
                                Global.getCombatEngine().spawnEmpArcPierceShields(ship,
                                        empPos,
                                        null,
                                        target,
                                        DamageType.ENERGY,
                                        damage,
                                        350,
                                        10000,
                                        null,
                                        20,
                                        new Color(0, 118, 210,255),
                                        Color.white);
                            }
                            if (!target.isFighter()) AddHunterDriveTarget(ship, target);
                        }
                    }
                }
                stats.getMaxSpeed().unmodify(id);
                stats.getAcceleration().unmodify(id);
                if (!MathUtils.isWithinRange(new Vector2f(), ship.getVelocity(), ship.getMaxSpeed())) {
                    ship.getVelocity().scale(1f - 0.5f * amount);
                }
                break;
        }
    }
}
