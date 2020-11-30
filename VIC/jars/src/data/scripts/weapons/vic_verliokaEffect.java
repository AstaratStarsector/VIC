package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import data.scripts.plugins.vic_combatPlugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class vic_verliokaEffect implements EveryFrameWeaponEffectPlugin {

    public final float degrees = 15;

    private final Map<ShipAPI, Float> debuffed_fighters = new HashMap<>();

    private final IntervalUtil timer = new IntervalUtil(0.25f, 0.25f);

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (engine.isPaused() || weapon.getShip().getOriginalOwner() == -1) return;



        ShipAPI ship = weapon.getShip();
        float dmgMult = ship.getMutableStats().getDamageToMissiles().getModifiedValue();

        ArrayList<BeamAPI> weaponBeams = new ArrayList<>(2);
        for (BeamAPI beam : engine.getBeams()){
            if (beam.getWeapon() == weapon) weaponBeams.add(beam);
        }
        float facing = weapon.getCurrAngle();

        for (Iterator<Map.Entry<ShipAPI, Float>> iter = debuffed_fighters.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<ShipAPI, Float> entry = iter.next();
            if (!entry.getKey().isAlive() || entry.getValue() - amount < 0) {
                iter.remove();
                entry.getKey().getMutableStats().getTimeMult().unmodify("vic_virlioka" + weapon.hashCode());
            } else {
                entry.setValue(entry.getValue() - amount);
                entry.getKey().setJitterUnder(entry.getKey(), new Color(44, 255, 255), 4, 8, 2);
                entry.getKey().getMutableStats().getTimeMult().modifyMult("vic_virlioka" + weapon.hashCode(), 0.80f);
            }
        }
        timer.advance(amount);
        if (timer.intervalElapsed()) {
            for (Map.Entry<ShipAPI, Float> entry : debuffed_fighters.entrySet()) {
                //if (!engine.isEntityInPlay(entry.getKey())) continue;
                ShipAPI fighter = entry.getKey();
                //engine.addFloatingText(entry.getKey().getLocation(), weapon.getDamage().getDamage() * 0.25 * dmgMult + "", 60, Color.WHITE, ship, 0.25f, 0.25f);
                //Global.getCombatEngine().applyDamage(entry.getKey(), entry.getKey().getLocation(), weapon.getDamage().getDamage() * 0.25f * dmgMult, weapon.getDamageType(), 0f, false, false, null);
                Vector2f zapFrom = fighter.getLocation();
                if (!weaponBeams.isEmpty()) {
                    BeamAPI beam = weaponBeams.get(0);
                    float angleToTarget = VectorUtils.getAngle(weapon.getLocation(), fighter.getLocation());
                    if (MathUtils.getShortestRotation(angleToTarget, facing) <= 0) beam = weaponBeams.get(1);
                    zapFrom = MathUtils.getNearestPointOnLine(fighter.getLocation(), beam.getFrom(), beam.getTo());
                }
                engine.spawnEmpArc(ship,
                        zapFrom,
                        null,
                        fighter,
                        weapon.getDamageType(),
                        weapon.getDamage().getDamage() * 0.25f,
                        0,
                        3000,
                        null,
                        2,
                        new Color(255, 162, 0, 29),
                        new Color(255, 191, 21, 255));
            }
        }

        if (!ship.isAlive()){
            for (Iterator<Map.Entry<ShipAPI, Float>> iter = debuffed_fighters.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry<ShipAPI, Float> entry = iter.next();
                iter.remove();
                entry.getKey().getMutableStats().getTimeMult().unmodify("vic_virlioka" + weapon.hashCode());
            }
        }

        if (weapon.getChargeLevel() < 1) return;



        /*
        float range = weapon.getRange() * 1.05f;

        boolean needToDraw = true;
        while (needToDraw){
            SpriteAPI trail = Global.getSettings().getSprite("fx", "vic_verlioka_field");
            Vector2f dir = Misc.getUnitVectorAtDegreeAngle(facing);
            Vector2f trailLoc = new Vector2f(weapon.getLocation().x + (dir.x * range), weapon.getLocation().y + (dir.y * range));
            //engine.addFloatingText(trailLoc, "o" + "", 60, Color.WHITE, ship, 0.25f, 0.25f);
            float size = range * 0.53f;
            Vector2f trailSize = new Vector2f(size, size / 2f);
            trail.setCenter(size / 2f, size / 2f);
            MagicRender.singleframe(trail, trailLoc, trailSize, facing - 90, new Color(255, 255, 255, 255), false);
            range -= size / 3.08f;
            if (range < 100) needToDraw = false;
        }

         */

        //Global.getCombatEngine().maintainStatusForPlayerShip("vic_dmgMult", "graphics/icons/hullsys/vic_adaptiveWarfareSystem.png", "dmgMult", dmgMult + "", false);

        for (MissileAPI missile : CombatUtils.getMissilesWithinRange(weapon.getLocation(), weapon.getRange())){
            if (missile.getCollisionClass() == CollisionClass.NONE) continue;
            if (missile.getOwner() == ship.getOwner()) continue;
            float angleToTarget = VectorUtils.getAngle(weapon.getLocation(), missile.getLocation());
            //engine.addFloatingText(missile.getLocation(), angleToTarget + "", 60, Color.WHITE, ship, 0.25f, 0.25f);
            if (Math.abs(MathUtils.getShortestRotation(angleToTarget, facing)) <= degrees){
                missile.setJitter(missile, new Color(44, 255, 255), 4, 6, 2);
                missile.getVelocity().scale(0.85f);
                Global.getCombatEngine().applyDamage(missile, missile.getLocation(), weapon.getDamage().getDamage() * amount * dmgMult, weapon.getDamageType(), 0f, false, false, null);
                BeamAPI beam = weaponBeams.get(0);
                if (Math.random() >= amount) continue;
                if (MathUtils.getShortestRotation(angleToTarget, facing) <= 0) beam = weaponBeams.get(1);

                Vector2f zapFrom = MathUtils.getNearestPointOnLine(missile.getLocation(),beam.getFrom(),beam.getTo());
                missile.setEmpResistance(100);

                engine.spawnEmpArc(ship,
                            zapFrom,
                            null,
                            missile,
                            weapon.getDamageType(),
                            0,
                            -1000,
                            3000,
                            null,
                            1,
                            new Color(255, 162, 0, 29),
                            new Color(255, 191, 21, 255));

            }
        }
        for (ShipAPI shipToCheck : CombatUtils.getShipsWithinRange(weapon.getLocation(), weapon.getRange())) {
            if (shipToCheck.getHullSize() != ShipAPI.HullSize.FIGHTER) continue;
            if (shipToCheck.getOwner() == ship.getOwner()) continue;
            if (shipToCheck.getCollisionClass() == CollisionClass.NONE) continue;
            float angleToTarget = VectorUtils.getAngle(weapon.getLocation(), shipToCheck.getLocation());
            if (Math.abs(MathUtils.getShortestRotation(angleToTarget, facing)) <= degrees) {
                debuffed_fighters.put(shipToCheck, 0.1f);
            }
        }
    }
}