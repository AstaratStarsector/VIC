package data.scripts.weapons.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;

import static com.fs.starfarer.api.util.Misc.normalizeAngle;

public class vic_swervingDumbfire implements MissileAIPlugin {


    private final MissileAPI missile;
    private final ShipAPI launchingShip;
    IntervalUtil timer;
    swervingState state;

    enum swervingState {
        left,
        right
    }

    float initialAngle;

    public vic_swervingDumbfire(MissileAPI missile, ShipAPI launchingShip)
    {
        this.missile = missile;
        this.launchingShip = launchingShip;
        this.initialAngle = missile.getFacing();
        state = (Math.random() > 0.5 ? swervingState.left : swervingState.right);
        timer = new IntervalUtil(0.05f,0.25f);
        missile.getVelocity().scale(MathUtils.getRandomNumberInRange(0.5f,1.5f));
    }

    @Override
    public void advance(float amount) {
        missile.giveCommand(ShipCommand.ACCELERATE);
        timer.advance(amount);
        boolean changeDirection = false;
        if (initialAngle - missile.getFacing() > 6f && state.equals(swervingState.right)){
            timer.forceIntervalElapsed();
            //Global.getCombatEngine().addFloatingText(missile.getLocation(),Misc.getAngleDiff(initialAngle, missile.getFacing()) + "",10, Color.WHITE,missile,0,0 );
        }

        if (initialAngle - missile.getFacing() < -6f && state.equals(swervingState.left)){
            timer.forceIntervalElapsed();
            //Global.getCombatEngine().addFloatingText(missile.getLocation(),Misc.getAngleDiff(initialAngle, missile.getFacing()) + "",10, Color.WHITE,missile,0,0 );
        }

        if (timer.intervalElapsed()) changeDirection = true;

        if (changeDirection){
            if (state.equals(swervingState.left)){
                state = swervingState.right;
            } else {
                state = swervingState.left;
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
