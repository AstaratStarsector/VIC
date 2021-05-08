package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import data.scripts.plugins.vic_combatPlugin;

import java.awt.*;
import java.util.*;
import java.util.List;

public class vic_shockDischarger extends BaseShipSystemScript {

    public static float suckRange = 2000f;
    public float suckDurationPowerMult = 1;
    public float Threshold = 6000f;
    public float Factor = 3000f;
    public Color ghostColour = new Color(0, 144, 255, 193);

    public float arcFrequency = 1f;
    public float powerCollected = 0f;
    public float arcAmount = 0f;

    public float suckedHard = 0;
    public float suckedSoft = 0;

    public float scalingThreshold = 5000;
    public float maxArcFrequency = 10;
    public float basePowerPerArc = 50;
    public float powerPerArc = 0;
    public static float shockRange = 1500f;
    public float empFraction = 0.5f;
    public float fluxFraction = 0.5f;
    public float hardFluxFraction = 0f;
    public Color arcColor = new Color(0, 123, 255,255);

    public IntervalUtil particleCD = new IntervalUtil(0.2f, 0.2f);

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

        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        if (Global.getCombatEngine().isPaused()) {
            amount = 0;
        }

        switch (state) {
            case IN:
                if (doOnce2) {
                    for (WeaponAPI weapon : ship.getAllWeapons()) {
                        if (weapon.getSlot().getId().startsWith("SYS")) {
                            SystemWeapons.add(weapon);
                        }
                    }
                    suckDurationPowerMult = 1 / ship.getSystem().getChargeUpDur();
                    vic_combatPlugin.AddFluxRaptureShip(ship);
                    suckedHard = 0;
                    suckedSoft = 0;
                    doOnce2 = false;
                }
                List<ShipAPI> listOfSuckTargets = CombatUtils.getShipsWithinRange(ship.getLocation(), suckRange);
                Collections.shuffle(listOfSuckTargets);
                float PowerMult = 1f * suckDurationPowerMult;
                if (powerCollected >= Threshold) PowerMult = Factor / (powerCollected - (Threshold - Factor));
                for (ShipAPI target : listOfSuckTargets) {
                    if (target.isHulk() ||
                            target.getFluxTracker().isVenting() ||
                            target.isDrone() ||
                            target.isFighter()) continue;
                    float fluxToCollect = target.getMutableStats().getFluxDissipation().getModifiedValue() * amount * PowerMult;
                    if (target == ship) fluxToCollect *= 2;
                    float softFlux = target.getFluxTracker().getCurrFlux() - target.getFluxTracker().getHardFlux();
                    if (fluxToCollect > target.getFluxTracker().getCurrFlux())
                        fluxToCollect = target.getFluxTracker().getCurrFlux();
                    float hardFlux = 0f;
                    suckedSoft = fluxToCollect;
                    if (fluxToCollect > softFlux) {
                        hardFlux = ((fluxToCollect - softFlux) * 0.5f * (1 + target.getMutableStats().getHardFluxDissipationFraction().getModifiedValue()));
                        fluxToCollect = softFlux + hardFlux;
                        suckedSoft = softFlux;
                        suckedHard = hardFlux;
                    }
                    target.getFluxTracker().decreaseFlux(fluxToCollect);
                    if (target.getShield() != null){
                        hardFlux = hardFlux / target.getShield().getFluxPerPointOfDamage() - hardFlux;
                        fluxToCollect += hardFlux;
                        suckedHard +=hardFlux;
                    }
                    target.getFluxTracker().decreaseFlux(fluxToCollect);
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
                        Vector2f moveDir = VectorUtils.getDirectionalVector(target.getLocation(), ship.getLocation());

                        SpriteAPI shipSprite = Global.getSettings().getSprite(target.getHullSpec().getSpriteName());
                        MagicRender.battlespace(
                                shipSprite,
                                new Vector2f(target.getLocation()),
                                new Vector2f(moveDir.x * 500, moveDir.y * 500),
                                new Vector2f(target.getSpriteAPI().getWidth(), target.getSpriteAPI().getHeight()),
                                new Vector2f(),
                                target.getFacing() - 90,
                                0,
                                ghostColour,
                                true,
                                0.0F,
                                0.0F,
                                0.0F,
                                0.0F,
                                0.0F,
                                0.1f,
                                0f,
                                0.3f,
                                CombatEngineLayers.UNDER_SHIPS_LAYER);
                    }
                }
                break;
            case ACTIVE:
                if (doOnce) {
                    if (powerCollected <= scalingThreshold){
                        powerPerArc = powerCollected * 0.035f + basePowerPerArc;
                        arcFrequency = (powerCollected / powerPerArc) / ship.getSystem().getChargeActiveDur();
                    } else {
                        arcFrequency = maxArcFrequency;
                        powerPerArc = powerCollected / (ship.getSystem().getChargeActiveDur() * arcFrequency);
                    }
                    //arcFrequency = (powerCollected / maxDmgPerArc) / ship.getSystem().getChargeActiveDur();
                    arcAmount = 1;
                    hardFluxFraction = suckedHard / (suckedHard + suckedSoft);
                    doOnce = false;
                }
                arcAmount += arcFrequency * amount;

                while (arcAmount >= 1 /*&& powerCollected > powerPerArc*/) {
                    arcAmount--;
                    ShipAPI target;
                    ShipAPI shipTarget = ship.getShipTarget();
                    if (shipTarget != null && !shipTarget.isHulk() && MathUtils.isWithinRange(shipTarget, ship.getLocation(), shockRange)){
                        target = ship.getShipTarget();
                    } else {
                        List<ShipAPI> enemies = AIUtils.getNearbyEnemies(ship, shockRange - ship.getCollisionRadius());
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
                    float power = powerPerArc;
                    if (powerCollected < powerPerArc){
                        power = powerCollected;
                    }
                    float ArcDamage;
                    float addEMP = 0;
                    if (target.isFighter()){
                        ArcDamage = power;
                    } else {
                        ArcDamage = power * (1 - fluxFraction);
                        float FluxDamage = power * fluxFraction;
                        float freeFlux = target.getFluxTracker().getMaxFlux() - target.getFluxTracker().getCurrFlux();
                        if (freeFlux >= FluxDamage){
                            target.getFluxTracker().increaseFlux(FluxDamage * (1 -hardFluxFraction), false);
                            target.getFluxTracker().increaseFlux(FluxDamage * hardFluxFraction, true);
                        } else {
                            addEMP = FluxDamage - freeFlux;
                            float softFluxToAdd = FluxDamage * (1 -hardFluxFraction) - addEMP;
                            float hardFluxToAdd = FluxDamage * hardFluxFraction;
                            if (softFluxToAdd < 0){
                                hardFluxToAdd -= softFluxToAdd;
                                softFluxToAdd = 0;
                            }
                            target.getFluxTracker().increaseFlux(softFluxToAdd, false);
                            target.getFluxTracker().increaseFlux(hardFluxToAdd, true);
                        }

                    }
                    Global.getCombatEngine().spawnEmpArc(ship,
                            from,
                            null,
                            target,
                            DamageType.ENERGY,
                            ArcDamage,
                            ArcDamage * empFraction + addEMP,
                            3000,
                            "tachyon_lance_emp_impact",
                            2 + (power * 0.025f),
                            arcColor,
                            Color.WHITE);
                    powerCollected -= power;
                }
                break;
            case OUT:
                powerCollected = 0f;
                arcAmount = 0f;
                doOnce = true;
                doOnce2 = true;
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
