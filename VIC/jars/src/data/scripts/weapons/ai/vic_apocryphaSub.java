package data.scripts.weapons.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;

public class vic_apocryphaSub implements MissileAIPlugin {

    MissileAPI
            missile,
            leadMissile;

    IntervalUtil timer,
    rangeCheck = new IntervalUtil(0.25f,0.25f);
    vic_swervingDumbfire.swervingState state;

    public vic_apocryphaSub (MissileAPI missile, MissileAPI leadMissile){
        this.missile = missile;
        this.leadMissile = leadMissile;

        state = (Math.random() > 0.5 ? vic_swervingDumbfire.swervingState.left : vic_swervingDumbfire.swervingState.right);
        timer = new IntervalUtil(0.15f,0.25f);
    }

    @Override
    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused() || missile.isFading() || missile.isFizzling()) {
            return;
        }
        missile.giveCommand(ShipCommand.ACCELERATE);
        timer.advance(amount);
        rangeCheck.advance(amount);
        float leadAngle = leadMissile.getFacing();
        boolean changeDirection = false;
        if (leadAngle - missile.getFacing() > 6f && state.equals(vic_swervingDumbfire.swervingState.right)){
            timer.forceIntervalElapsed();
            //Global.getCombatEngine().addFloatingText(missile.getLocation(),Misc.getAngleDiff(initialAngle, missile.getFacing()) + "",10, Color.WHITE,missile,0,0 );
        }

        if (leadAngle - missile.getFacing() < -6f && state.equals(vic_swervingDumbfire.swervingState.left)){
            timer.forceIntervalElapsed();
            //Global.getCombatEngine().addFloatingText(missile.getLocation(),Misc.getAngleDiff(initialAngle, missile.getFacing()) + "",10, Color.WHITE,missile,0,0 );
        }
        if (rangeCheck.intervalElapsed()){
            if (!MathUtils.isWithinRange(missile.getLocation(), leadMissile.getLocation(), 250f)){
                changeDirection = true;
                timer.setElapsed(0);
            }
        }

        if (timer.intervalElapsed()) changeDirection = true;

        if (changeDirection){
            if (state.equals(vic_swervingDumbfire.swervingState.left)){
                state = vic_swervingDumbfire.swervingState.right;
            } else {
                state = vic_swervingDumbfire.swervingState.left;
            }
        }

        switch (state){
            case left:
                missile.giveCommand(ShipCommand.TURN_LEFT);
                break;
            case right:
                missile.giveCommand(ShipCommand.TURN_RIGHT);
                break;
        }

    }
}
