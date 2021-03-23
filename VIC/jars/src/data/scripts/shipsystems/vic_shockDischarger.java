package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.*;
import java.util.List;

public class vic_shockDischarger extends BaseShipSystemScript {

    public float suckRange = 2000f;
    public float shockRange = 2000f;
    public float dmgPerArc = 200;
    public float empFraction = 0.5f;
    public float powerCollected = 0f;
    public float arcFrequency = 1f;
    public float arcAmount = 0f;
    public float Threshold = 6000f;
    public float Factor = 3000f;

    public IntervalUtil particleCD = new IntervalUtil(0.1f, 0.1f);

    public List<WeaponAPI> SystemWeapons = new ArrayList<>();
    public HashMap<ShipAPI.HullSize, Integer> particleAmount = new HashMap<>();
    public boolean doOnce = true;
    public boolean doOnce2 = true;

    {
        particleAmount.put(ShipAPI.HullSize.DEFAULT, 0);
        particleAmount.put(ShipAPI.HullSize.FIGHTER, 0);
        particleAmount.put(ShipAPI.HullSize.FRIGATE, 1);
        particleAmount.put(ShipAPI.HullSize.DESTROYER, 2);
        particleAmount.put(ShipAPI.HullSize.CRUISER, 3);
        particleAmount.put(ShipAPI.HullSize.CAPITAL_SHIP, 4);
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship = (ShipAPI) stats.getEntity();

        if (doOnce2) {
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (weapon.getSlot().getId().startsWith("SYS")) {
                    SystemWeapons.add(weapon);
                }
            }
            doOnce2 = false;
        }

        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        if (Global.getCombatEngine().isPaused()) {
            amount = 0;
        }

        switch (state) {
            case IN:
                List<ShipAPI> listOfSuckTargets = CombatUtils.getShipsWithinRange(ship.getLocation(), suckRange);
                Collections.shuffle(listOfSuckTargets);
                float PowerMult = 1f;
                if (powerCollected >= Threshold) PowerMult = Factor / (powerCollected - (Threshold - Factor));
                for (ShipAPI target : listOfSuckTargets) {
                    if (target.isHulk()) continue;
                    float fluxToCollect = target.getMutableStats().getFluxDissipation().getModifiedValue() * amount * PowerMult;
                    if (target == ship) fluxToCollect *= 2;
                    float softFlux = target.getFluxTracker().getCurrFlux() - target.getFluxTracker().getHardFlux();
                    if (fluxToCollect > target.getFluxTracker().getCurrFlux())
                        fluxToCollect = target.getFluxTracker().getCurrFlux();
                    float hardFlux = 0f;
                    if (fluxToCollect > softFlux) {
                        hardFlux = ((fluxToCollect - softFlux) * 0.5f);
                        fluxToCollect = softFlux + hardFlux;
                    }
                    target.getFluxTracker().decreaseFlux(fluxToCollect);
                    if (target.getShield() != null)
                        fluxToCollect += hardFlux / target.getShield().getFluxPerPointOfDamage() - hardFlux;
                    if (target.getOwner() == ship.getOwner()) fluxToCollect *= 0.5f;
                    powerCollected += fluxToCollect;
                }
                particleCD.advance(amount);
                if (particleCD.intervalElapsed()) {
                    for (ShipAPI target : listOfSuckTargets) {

                        if (target == ship) continue;
                        if (target.isStation()) continue;
                        if (target.isFighter()) continue;
                        if (target.isHulk()) continue;
                        if (!MagicRender.screenCheck(0.5f, target.getLocation())) continue;
                        float angle = VectorUtils.getAngle(target.getLocation(), ship.getLocation());

                        for (int i = 0; i < particleAmount.get(target.getHullSize()); i++) {
                            Vector2f spawnPoint = MathUtils.getPointOnCircumference(target.getLocation(), target.getCollisionRadius(), angle + MathUtils.getRandomNumberInRange(-30, 30));
                            Vector2f moveDir = VectorUtils.getDirectionalVector(spawnPoint, ship.getLocation());
                            Global.getCombatEngine().addHitParticle(
                                    spawnPoint,
                                    new Vector2f(moveDir.x * MathUtils.getRandomNumberInRange(300, 500), moveDir.y * MathUtils.getRandomNumberInRange(300, 500)),
                                    MathUtils.getRandomNumberInRange(8, 25),
                                    MathUtils.getRandomNumberInRange(0.7f, 1f),
                                    MathUtils.getRandomNumberInRange(0.5f, 0.7f),
                                    new Color(0, MathUtils.getRandomNumberInRange(120, 220), MathUtils.getRandomNumberInRange(200, 255), 255)
                            );
                        }
                    }
                }
                break;
            case ACTIVE:
                if (doOnce) {
                    arcFrequency = (powerCollected / dmgPerArc) / ship.getSystem().getChargeActiveDur();
                    arcAmount = 1;
                    doOnce = false;
                }
                arcAmount += arcFrequency * amount;

                while (arcAmount >= 1 && powerCollected > dmgPerArc) {
                    arcAmount--;
                    ShipAPI target;
                    if (ship.getShipTarget() != null && MathUtils.isWithinRange(ship.getShipTarget().getLocation(), ship.getLocation(), shockRange))
                        target = ship.getShipTarget();
                    else {
                        List<ShipAPI> enemies = AIUtils.getNearbyEnemies(ship, shockRange);
                        if (enemies.isEmpty()) break;
                        target = enemies.get(MathUtils.getRandomNumberInRange(0, enemies.size() - 1));
                    }
                    Vector2f from = new Vector2f(ship.getLocation());
                    float angle = VectorUtils.getAngle(ship.getLocation(), target.getLocation());
                    //Global.getLogger(vic_shockDischarger.class).info(angle);
                    Collections.shuffle(SystemWeapons);
                    for (WeaponAPI weapon : SystemWeapons) {
                        float rotation = Math.abs(MathUtils.getShortestRotation(weapon.getCurrAngle(), angle));
                        //Global.getLogger(vic_shockDischarger.class).info(weapon.getSlot().getId() + " " + rotation + " " + weapon.getArc() * 0.5f);
                        if (rotation <= weapon.getArc() * 0.5f) {
                            from = weapon.getLocation();
                            break;
                        }
                    }
                    Global.getCombatEngine().spawnEmpArc(ship,
                            from,
                            null,
                            target,
                            DamageType.ENERGY,
                            dmgPerArc,
                            dmgPerArc * empFraction,
                            3000,
                            "tachyon_lance_emp_impact",
                            4,
                            Color.WHITE,
                            Color.CYAN);
                    powerCollected -= dmgPerArc;
                }
                break;
            case OUT:
                powerCollected = 0f;
                arcAmount = 0f;
                doOnce = true;
                break;
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            if (state == State.IN || state == State.ACTIVE)
                return new StatusData("power " + Math.round(powerCollected), false);
        }
        return null;
    }
}
