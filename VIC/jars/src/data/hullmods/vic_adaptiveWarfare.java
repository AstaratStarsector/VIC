package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.Map;

public class vic_adaptiveWarfare extends BaseHullMod {

    //Triggers
    public float
            PerHull = 0.4f,
            PerHardFlux = 0.01f,
            PerSU = 0.5f;

    //Bonuses at 100% power
    public float
            Speed = 20f,
            Damage = 10f,
            ShieldEff = 10f;

    //Decay and shit
    public float
            StopTime = 8f,
            FloatDecay = 3f,
            PercentDecay = 0.1f;

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return Math.round(ShieldEff) + "%";
        if (index == 1) return Math.round(Damage) + "%";
        if (index == 2) return Math.round(Speed) + "%";
        return null;
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        float SpeedPower = 0f;
        float DamagePower = 0f;
        float ShieldPower = 0f;
        float SpeedPowerTime = 0f;
        float DamagePowerTime = 0f;
        float ShieldPowerTime = 0f;

        if (!ship.isAlive()) return;

        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
        String id = ship.getId();

        Vector2f LocLastFrame = new Vector2f(ship.getLocation());
        float HardFluxLastFrame = 0f;
        float HullLastFrame = ship.getHitpoints();

        MutableShipStatsAPI stats = ship.getMutableStats();

        float timer = Global.getCombatEngine().getTotalElapsedTime(false);

        //Boosts power
        if (customCombatData.get("vic_adaptiveWarfareSpeed" + id) instanceof Float)
            SpeedPower = (float) customCombatData.get("vic_adaptiveWarfareSpeed" + id);

        if (customCombatData.get("vic_adaptiveWarfareDamage" + id) instanceof Float)
            DamagePower = (float) customCombatData.get("vic_adaptiveWarfareDamage" + id);

        if (customCombatData.get("vic_adaptiveWarfareShield" + id) instanceof Float)
            ShieldPower = (float) customCombatData.get("vic_adaptiveWarfareShield" + id);

        //Last triggered  timer
        if (customCombatData.get("vic_adaptiveWarfareSpeedTime" + id) instanceof Float)
            SpeedPowerTime = (float) customCombatData.get("vic_adaptiveWarfareSpeedTime" + id);

        if (customCombatData.get("vic_adaptiveWarfareDamageTime" + id) instanceof Float)
            DamagePowerTime = (float) customCombatData.get("vic_adaptiveWarfareDamageTime" + id);

        if (customCombatData.get("vic_adaptiveWarfareShieldTime" + id) instanceof Float)
            ShieldPowerTime = (float) customCombatData.get("vic_adaptiveWarfareShieldTime" + id);

        //Trigger values
        if (customCombatData.get("vic_adaptiveWarfareLocLastFrame" + id) instanceof Vector2f)
            LocLastFrame = (Vector2f) customCombatData.get("vic_adaptiveWarfareLocLastFrame" + id);

        if (customCombatData.get("vic_adaptiveWarfareHardFluxLastFrame" + id) instanceof Float)
            HardFluxLastFrame = (float) customCombatData.get("vic_adaptiveWarfareHardFluxLastFrame" + id);

        if (customCombatData.get("vic_adaptiveWarfareHullLastFrame" + id) instanceof Float)
            HullLastFrame = (float) customCombatData.get("vic_adaptiveWarfareHullLastFrame" + id);

        //speed for hull
        if (ship.getHitpoints() < HullLastFrame) {
            SpeedPower += (HullLastFrame - ship.getHitpoints()) * PerHull;
            if (SpeedPower > 100) SpeedPower = 100;
            SpeedPowerTime = timer;
        } else if (timer - SpeedPowerTime >= StopTime) {
            SpeedPower = ((SpeedPower - FloatDecay * amount) - (SpeedPower * PercentDecay * amount));
            if (SpeedPower < 0) SpeedPower = 0;
        }
        HullLastFrame = ship.getHitpoints();

        //Dmg for hard flux
        if (ship.getFluxTracker().getHardFlux() > HardFluxLastFrame) {
            DamagePower += (ship.getFluxTracker().getHardFlux() - HardFluxLastFrame) * PerHardFlux;
            if (DamagePower > 100) DamagePower = 100;
            DamagePowerTime = timer;
        } else if (timer - DamagePowerTime >= StopTime) {
            DamagePower = ((DamagePower - FloatDecay * amount) - (DamagePower * PercentDecay * amount));
            if (DamagePower < 0) DamagePower = 0;
        }
        HardFluxLastFrame = ship.getFluxTracker().getHardFlux();

        //Shield Eff for changing position
        float distanceTravel = MathUtils.getDistance(ship.getLocation(), LocLastFrame);
        ShieldPower = ((ShieldPower - FloatDecay * amount * 2f) - (ShieldPower * PercentDecay * amount * 2f));
        if (ShieldPower < 0) ShieldPower = 0;
        if (distanceTravel != 0) {
            ShieldPower += distanceTravel * PerSU;
            if (ShieldPower > 100) ShieldPower = 100;
            ShieldPowerTime = timer;
        }
        LocLastFrame = new Vector2f(ship.getLocation());

        stats.getMaxSpeed().modifyPercent("vic_adaptiveWarfare", SpeedPower * 0.01f * Speed);
        stats.getAcceleration().modifyPercent("vic_adaptiveWarfare", SpeedPower * 0.01f * Speed);
        stats.getDeceleration().modifyPercent("vic_adaptiveWarfare", SpeedPower * 0.01f * Speed);
        stats.getTurnAcceleration().modifyPercent("vic_adaptiveWarfare", SpeedPower * 0.01f * Speed);
        stats.getMaxTurnRate().modifyPercent("vic_adaptiveWarfare", SpeedPower * 0.01f * Speed);

        stats.getShieldDamageTakenMult().modifyPercent("vic_adaptiveWarfare", ShieldPower * 0.01f * ShieldEff);

        stats.getEnergyWeaponDamageMult().modifyPercent("vic_adaptiveWarfare", DamagePower * 0.01f * Damage);
        float currRoFBonus = DamagePower * 0.01f * Damage;
        stats.getBallisticRoFMult().modifyPercent("vic_adaptiveWarfare", currRoFBonus);
        stats.getBallisticWeaponFluxCostMod().modifyPercent("vic_adaptiveWarfare", (1 / (1 + currRoFBonus * 0.01f) - 1) * 100f);

        if (ship == Global.getCombatEngine().getPlayerShip()) {
            Global.getCombatEngine().maintainStatusForPlayerShip("vic_adaptiveWarfare3", "graphics/icons/hullsys/vic_adaptiveWarfareSystem.png", "Speed power", Math.round(SpeedPower) + "%", false);
            Global.getCombatEngine().maintainStatusForPlayerShip("vic_adaptiveWarfare2", "graphics/icons/hullsys/vic_adaptiveWarfareSystem.png", "Damage Power", Math.round(DamagePower) + "%", false);
            Global.getCombatEngine().maintainStatusForPlayerShip("vic_adaptiveWarfare1", "graphics/icons/hullsys/vic_adaptiveWarfareSystem.png", "Shield Power", Math.round(ShieldPower) + "%", false);
        }

        //Boosts power
        customCombatData.put("vic_adaptiveWarfareSpeed" + id, SpeedPower);
        customCombatData.put("vic_adaptiveWarfareDamage" + id, DamagePower);
        customCombatData.put("vic_adaptiveWarfareShield" + id, ShieldPower);
        //Last triggered timer
        customCombatData.put("vic_adaptiveWarfareSpeedTime" + id, SpeedPowerTime);
        customCombatData.put("vic_adaptiveWarfareDamageTime" + id, DamagePowerTime);
        customCombatData.put("vic_adaptiveWarfareShieldTime" + id, ShieldPowerTime);
        //Trigger values
        customCombatData.put("vic_adaptiveWarfareLocLastFrame" + id, LocLastFrame);
        customCombatData.put("vic_adaptiveWarfareHardFluxLastFrame" + id, HardFluxLastFrame);
        customCombatData.put("vic_adaptiveWarfareHullLastFrame" + id, HullLastFrame);

    }
}