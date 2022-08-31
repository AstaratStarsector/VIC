package data.scripts.weapons.decos;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.util.MagicAnim;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_falcorRadar implements EveryFrameWeaponEffectPlugin {

    WeaponAPI
            leftSection,
            rightSection,
            bridgeShield,
            tailGlow,
            rightGlow,
            leftGlow,
            bridgeGlow;

    Vector2f
            shieldPos,
            leftPos,
            rightPos;

    boolean DoOnce = true;

    AnimationAPI animation;

    int
            maxFrame,
            frame;


    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (DoOnce) {
            for (WeaponAPI weapon1 : weapon.getShip().getAllWeapons()) {
                if (weapon1.getSlot().getId().startsWith("WSLS")) {
                    leftSection = weapon1;
                    leftPos = new Vector2f(leftSection.getSprite().getCenterX(), leftSection.getSprite().getCenterY());
                }
                if (weapon1.getSlot().getId().startsWith("WSRS")) {
                    rightSection = weapon1;
                    rightPos = new Vector2f(rightSection.getSprite().getCenterX(), rightSection.getSprite().getCenterY());
                }
                if (weapon1.getSlot().getId().startsWith("WSMS")) {
                    bridgeShield = weapon1;
                    shieldPos = new Vector2f(bridgeShield.getSprite().getCenterX(), bridgeShield.getSprite().getCenterY());
                }
                if (weapon1.getSlot().getId().startsWith("WSGS01")) {
                    tailGlow = weapon1;
                }
                if (weapon1.getSlot().getId().startsWith("WSGS02")) {
                    rightGlow = weapon1;
                }
                if (weapon1.getSlot().getId().startsWith("WSGS03")) {
                    leftGlow = weapon1;
                }
                if (weapon1.getSlot().getId().startsWith("WSGS04")) {
                    bridgeGlow = weapon1;
                }
                animation = weapon.getAnimation();
                maxFrame = animation.getNumFrames();
            }
            DoOnce = false;
        }
        float effectLevel = MagicAnim.smooth(weapon.getShip().getSystem().getEffectLevel());
        float platesEffect = Math.min(effectLevel * 2, 1);
        bridgeShield.getSprite().setCenter(shieldPos.x, shieldPos.y + 9 * platesEffect);
        leftSection.getSprite().setCenter(leftPos.x + 6 * platesEffect, leftPos.y - 6 * platesEffect);
        rightSection.getSprite().setCenter(rightPos.x - 6 * platesEffect, rightPos.y - 6 * platesEffect);
        if (platesEffect != 1) {
            frame = Math.round((maxFrame - 1) * platesEffect);
            animation.setFrame(frame);
        }
        float glowEffect = Math.max(0, Math.min(effectLevel * 2 - 1, 1));
        if (glowEffect > 0){
            Color alpha = new Color(255, 255, 255, Math.round(255 * glowEffect));
            tailGlow.getAnimation().setFrame(1);
            tailGlow.getSprite().setColor(alpha);
            rightGlow.getAnimation().setFrame(1);
            rightGlow.getSprite().setColor(alpha);
            leftGlow.getAnimation().setFrame(1);
            leftGlow.getSprite().setColor(alpha);
            bridgeGlow.getAnimation().setFrame(1);
            bridgeGlow.getSprite().setColor(alpha);
        } else {
            tailGlow.getAnimation().setFrame(0);
            rightGlow.getAnimation().setFrame(0);
            leftGlow.getAnimation().setFrame(0);
            bridgeGlow.getAnimation().setFrame(0);
        }
        //Global.getCombatEngine().maintainStatusForPlayerShip("falcorrader", null, "tail", glowEffect + "", false);
    }
}
