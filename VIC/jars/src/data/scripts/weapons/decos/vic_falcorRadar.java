package data.scripts.weapons.decos;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.util.MagicAnim;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_falcorRadar implements EveryFrameWeaponEffectPlugin {

    WeaponAPI
            leftSection,
            rightSection,
            bridgeShield;

    Vector2f
            shieldPos,
            leftPos,
            rightPos;

    boolean DoOnce = true;


    AnimationAPI animation;
    int maxFrame;
    int frame;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (DoOnce) {
            for (WeaponAPI weapon1 : weapon.getShip().getAllWeapons()) {
                if (weapon1.getSlot().getId().startsWith("WSLS")){
                    leftSection = weapon1;
                    leftPos = new Vector2f(leftSection.getSprite().getCenterX(), leftSection.getSprite().getCenterY());
                }
                if (weapon1.getSlot().getId().startsWith("WSRS")){
                    rightSection = weapon1;
                    rightPos = new Vector2f(rightSection.getSprite().getCenterX(), rightSection.getSprite().getCenterY());
                }
                if (weapon1.getSlot().getId().startsWith("WSMS")){
                    bridgeShield = weapon1;
                    shieldPos = new Vector2f(bridgeShield.getSprite().getCenterX(), bridgeShield.getSprite().getCenterY());
                }
                animation = weapon.getAnimation();
                maxFrame = animation.getNumFrames();
            }
            DoOnce = false;
        }
        float effectLevel = MagicAnim.smooth(weapon.getShip().getSystem().getEffectLevel());
        bridgeShield.getSprite().setCenter(shieldPos.x, shieldPos.y + 9 * effectLevel);
        leftSection.getSprite().setCenter(leftPos.x + 6 * effectLevel, leftPos.y - 6 * effectLevel);
        rightSection.getSprite().setCenter(rightPos.x - 6 * effectLevel, rightPos.y - 6 * effectLevel);
        if (effectLevel != 1){
            frame = Math.round((maxFrame - 1) * effectLevel);
            animation.setFrame(frame);
        }
        Global.getCombatEngine().maintainStatusForPlayerShip("falcorrader", null, "tail", animation.getFrame() + "", false);
    }
}
