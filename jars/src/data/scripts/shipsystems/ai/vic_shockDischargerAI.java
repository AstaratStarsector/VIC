package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import data.scripts.shipsystems.vic_shockDischarger;

import java.awt.*;
import java.util.List;


public class vic_shockDischargerAI implements ShipSystemAIScript {

    private final IntervalUtil
            timer = new IntervalUtil(0.75f, 1f);
    private ShipAPI ship;

    vic_shockDischarger system;



    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = (vic_shockDischarger) system.getSpecAPI().getStatsScript();
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        float powerCollected = system.getPower(ship);
        //Global.getCombatEngine().maintainStatusForPlayerShip("vic_shockDischargerAI", "graphics/icons/hullsys/emp_emitter.png", "powerSucked", powerCollected + "", false);

        if (!ship.getSystem().getState().equals(ShipSystemAPI.SystemState.IDLE)) return;
        timer.advance(amount);
        if (!timer.intervalElapsed()) return;
        float weight = 0;
        boolean maxPower = powerCollected >= system.hardCap;


        if (target != null){
            if (target.getFleetMember() != null){
                float targetsWorth = target.getFleetMember().getFleetPointCost() * (target.getHardFluxLevel() + ((target.getFluxLevel() - target.getHardFluxLevel()) * 0.5f));
                weight += targetsWorth;
            }
        } else {
            List<ShipAPI> enemyShips = AIUtils.getNearbyEnemies(ship, vic_shockDischarger.shockRange);
            for (ShipAPI enemy : enemyShips){
                if (enemy != null && enemy.getFleetMember() != null)
                    weight += (enemy.getFleetMember().getFleetPointCost() * (enemy.getHardFluxLevel() + ((enemy.getFluxLevel() - enemy.getHardFluxLevel()) * 0.5f))) / enemyShips.size();
            }
        }



        if (!maxPower){
            if (powerCollected >= system.Threshold){
                weight *=  powerCollected / system.Threshold;
            }
            weight -= ship.getMutableStats().getFluxDissipation().getModifiedValue() * 0.01;
            for (ShipAPI ally : AIUtils.getNearbyAllies(ship, vic_shockDischarger.suckRange)){
                if (ally.isHulk() ||
                        ally.getFluxTracker().isVenting() ||
                        ally.isDrone() ||
                        ally.isFighter() ||
                        ally.getOwner() != ship.getOwner()) continue;
                weight -= ally.getMutableStats().getFluxDissipation().getModifiedValue() * 0.005;
            }
        }

        float neededWeight = 10 * (1 - (powerCollected / system.hardCap));
        if (weight > neededWeight){
            ship.useSystem();
        }
        //Global.getCombatEngine().addFloatingText(ship.getLocation(),weight + "/" + neededWeight + "", 40, Color.WHITE,null, 0,0);

        /*
        boolean useSystem = false;
        if (ship.getFluxLevel() >= 0.25f){
            ShipAPI enemy = AIUtils.getNearestEnemy(ship);
            if (enemy != null && MathUtils.isWithinRange(enemy.getLocation(), ship.getLocation(), 1500)) useSystem = true;
        }
        if (ship.getFluxLevel() >= 0.65f) useSystem = true;

        if (useSystem) ship.useSystem();

         */
    }

}
