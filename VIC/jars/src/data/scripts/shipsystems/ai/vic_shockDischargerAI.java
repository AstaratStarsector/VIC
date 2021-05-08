package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;


public class vic_shockDischargerAI implements ShipSystemAIScript {

    private final IntervalUtil
            timer = new IntervalUtil(0.75f, 1f);
    private ShipAPI ship;


    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        timer.advance(amount);
        if (!timer.intervalElapsed()) return;

        if (!ship.getSystem().getState().equals(ShipSystemAPI.SystemState.IDLE)) return;

        boolean useSystem = false;
        if (ship.getFluxLevel() >= 0.25f){
            ShipAPI enemy = AIUtils.getNearestEnemy(ship);
            if (enemy != null && MathUtils.isWithinRange(enemy.getLocation(), ship.getLocation(), 1500)) useSystem = true;
        }
        if (ship.getFluxLevel() >= 0.65f) useSystem = true;

        if (useSystem) ship.useSystem();
    }

}
