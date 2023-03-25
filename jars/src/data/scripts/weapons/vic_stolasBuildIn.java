package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.ProjectileSpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.MagicAnim;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.fs.starfarer.api.util.Misc.ZERO;

public class vic_stolasBuildIn implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {


    final float Threshold = 7500f;
    ShipAPI ship;
    boolean
            doOnce = true,
            devMode = false,
            changeDoOnce = false;

    animateState
            firstMark = new animateState(),
            secondMark = new animateState(),
            thirdMark = new animateState();

    float
            animTimeIn = 0.4f,
            animStay = 0.4f,
            animTimeOut = 0.8f;

    final float MUZZLE_OFFSET_HARDPOINT = 65f;
    final float MUZZLE_OFFSET_TURRET = 65f;

    WeaponAPI
            bar1,
            bar2,
            bar3,
            mark00,
            mark01,
            mark10,
            mark11,
            mark20,
            mark21,
            systemGlow;

    WeaponSlotAPI
            slot;

    vernierEngine
            FL,
            FR,
            ML,
            MR,
            BL,
            BR;

    ArrayList<vernierEngine> allEngines = new ArrayList<>();

    float
            ammoOnShot = 0;

    private final Map<ShipAPI.HullSize, Float> strafeMulti = new HashMap<>();

    {
        strafeMulti.put(ShipAPI.HullSize.FIGHTER, 1f);
        strafeMulti.put(ShipAPI.HullSize.FRIGATE, 1f);
        strafeMulti.put(ShipAPI.HullSize.DESTROYER, 0.75f);
        strafeMulti.put(ShipAPI.HullSize.CRUISER, 0.5f);
        strafeMulti.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.25f);
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (doOnce) {
            ship = weapon.getShip();
            engine.getListenerManager().addListener(new vic_stolasDamageListener(weapon.getShip(), weapon));
            weapon.setMaxAmmo(3);
            weapon.setAmmo(0);
            for (WeaponAPI tmp : ship.getAllWeapons()) {
                if (tmp.getId().equals("vic_stolasBuildIn")) slot = tmp.getSlot();
                switch (tmp.getSlot().getId()) {
                    case "CH1":
                        bar1 = tmp;
                        tmp.getAnimation().setFrame(1);
                        break;
                    case "CH2":
                        bar2 = tmp;
                        tmp.getAnimation().setFrame(1);
                        break;
                    case "CH3":
                        bar3 = tmp;
                        tmp.getAnimation().setFrame(1);
                        break;
                    case "INDF0":
                        mark00 = tmp;
                        tmp.getAnimation().setFrame(1);
                        break;
                    case "INDF1":
                        mark01 = tmp;
                        tmp.getAnimation().setFrame(1);
                        break;
                    case "INDS0":
                        mark10 = tmp;
                        tmp.getAnimation().setFrame(1);
                        break;
                    case "INDS1":
                        mark11 = tmp;
                        tmp.getAnimation().setFrame(1);
                        break;
                    case "INDT0":
                        mark20 = tmp;
                        tmp.getAnimation().setFrame(1);
                        break;
                    case "INDT1":
                        mark21 = tmp;
                        tmp.getAnimation().setFrame(1);
                        break;
                    case "GL001":
                        systemGlow = tmp;
                        tmp.getAnimation().setFrame(1);
                        break;
                    case "THF0":
                        BR = new vernierEngine(tmp);
                        tmp.getAnimation().setFrame(1);
                        break;
                    case "THF1":
                        BL = new vernierEngine(tmp);
                        tmp.getAnimation().setFrame(1);
                        break;
                    case "THS0":
                        MR = new vernierEngine(tmp);
                        tmp.getAnimation().setFrame(1);
                        break;
                    case "THS1":
                        ML = new vernierEngine(tmp);
                        tmp.getAnimation().setFrame(1);
                        break;
                    case "THT0":
                        FR = new vernierEngine(tmp);
                        tmp.getAnimation().setFrame(1);
                        break;
                    case "THT1":
                        FL = new vernierEngine(tmp);
                        tmp.getAnimation().setFrame(1);
                        break;
                }
            }
            allEngines.add(FL);
            allEngines.add(FR);
            allEngines.add(ML);
            allEngines.add(MR);
            allEngines.add(BL);
            allEngines.add(BR);
            devMode = Global.getSettings().isDevMode();
            doOnce = false;
        }
        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();

        float currDamage = 0f;

        if (customCombatData.get("vic_stolasBuildIn" + ship.getId()) instanceof Float)
            currDamage = (float) customCombatData.get("vic_stolasBuildIn" + ship.getId());

        while (currDamage >= Threshold && weapon.getAmmo() < 3) {
            currDamage -= Threshold;
            if (weapon.getAmmo() == 0) {
                firstMark.animate = true;
            } else if (weapon.getAmmo() == 1) {
                secondMark.animate = true;
            } else if (weapon.getAmmo() == 2) {
                thirdMark.animate = true;
            }
            weapon.getAmmoTracker().addOneAmmo();
        }

        if (weapon.getAmmo() == 3) {
            currDamage = 0;
        }

        //charge bar
        float ratio = currDamage / Threshold;
        Color color = new Color(255, 255, 255, MathUtils.clamp(Math.round((currDamage / Threshold) * 255), 0 ,255));
        Color fullWhite = new Color(255, 255, 255, 255);
        Color fullBlack = new Color(255, 255, 255, 0);
        if (weapon.getAmmo() == 0) {
            bar1.getSprite().setColor(color);
            bar2.getSprite().setColor(fullBlack);
            bar3.getSprite().setColor(fullBlack);
        } else if (weapon.getAmmo() == 1) {
            bar1.getSprite().setColor(fullWhite);
            bar2.getSprite().setColor(color);
            bar3.getSprite().setColor(fullBlack);
        } else if (weapon.getAmmo() == 2) {
            bar1.getSprite().setColor(fullWhite);
            bar2.getSprite().setColor(fullWhite);
            bar3.getSprite().setColor(color);
        } else if (weapon.getAmmo() == 3) {
            bar1.getSprite().setColor(fullWhite);
            bar2.getSprite().setColor(fullWhite);
            bar3.getSprite().setColor(fullWhite);
        }

        //side marks
        color = new Color(255, 255, 255, (Math.round(MagicAnim.smooth(weapon.getChargeLevel()) * 255)));
        if (weapon.getChargeLevel() > 0) {

            firstMark.animate = false;
            secondMark.animate = false;
            thirdMark.animate = false;
            firstMark.animationTime = 0;
            secondMark.animationTime = 0;
            thirdMark.animationTime = 0;

            String sound = "vic_astronomicon_charge1";
            if (ammoOnShot >= 1) {
                mark00.getSprite().setColor(color);
                mark01.getSprite().setColor(color);
            }
            if (ammoOnShot >= 2) {
                mark10.getSprite().setColor(color);
                mark11.getSprite().setColor(color);
                sound = "vic_astronomicon_charge2";
            }
            if (ammoOnShot >= 3) {
                mark20.getSprite().setColor(color);
                mark21.getSprite().setColor(color);
                sound = "vic_astronomicon_charge3";
            }
            if (changeDoOnce) {
                Global.getSoundPlayer().playSound(sound, 1, 1, weapon.getLocation(), weapon.getShip().getVelocity());
                changeDoOnce = false;
            }
        } else {
            mark00.getSprite().setColor(fullBlack);
            mark01.getSprite().setColor(fullBlack);
            mark10.getSprite().setColor(fullBlack);
            mark11.getSprite().setColor(fullBlack);
            mark20.getSprite().setColor(fullBlack);
            mark21.getSprite().setColor(fullBlack);
            ammoOnShot = weapon.getAmmo();
            changeDoOnce = true;
        }

        if (weapon.getChargeLevel() <= 0) {
            animateMarker(firstMark, amount, mark00, mark01);
            animateMarker(secondMark, amount, mark10, mark11);
            animateMarker(thirdMark, amount, mark20, mark21);
        }

        //status
        //TODO: add actual icons
        if (ship == engine.getPlayerShip()) {
            if (weapon.getAmmo() == 3) {
                engine.maintainStatusForPlayerShip("vic_stolasBuildIn", "graphics/icons/hullsys/ammo_feeder.png", "Charge", "Fully charged", false);
            } else {
                if (devMode) {
                    engine.maintainStatusForPlayerShip("vic_stolasBuildIn", "graphics/icons/hullsys/ammo_feeder.png", "Charge", weapon.getAmmo() + "/3 " + Math.round(currDamage) + "/" + Math.round(ratio * 100) + "%", false);
                } else {
                    engine.maintainStatusForPlayerShip("vic_stolasBuildIn", "graphics/icons/hullsys/ammo_feeder.png", "Charge", weapon.getAmmo() + "/3 " + (Math.round(ratio * 1000) / 10f) + "%", false);
                }
            }
        }

        customCombatData.put("vic_stolasBuildIn" + ship.getId(), currDamage);

        color = new Color(255, 255, 255, (Math.round(MagicAnim.smooth(ship.getSystem().getEffectLevel()) * 255)));
        systemGlow.getSprite().setColor(color);

        //vernier engines

        Vector2f newVector = new Vector2f();
        float turn = 0;
        if (ship.getEngineController().isAccelerating()) {
            newVector.y += 1;
        }
        if (ship.getEngineController().isAcceleratingBackwards() || ship.getEngineController().isDecelerating()) {
            newVector.y -= 1;
        }
        if (ship.getEngineController().isStrafingLeft()) {
            newVector.x -= 1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
        }
        if (ship.getEngineController().isStrafingRight()) {
            newVector.x += 1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
        }
        if (ship.getEngineController().isTurningLeft()) {
            turn -= 1;
        }
        if (ship.getEngineController().isTurningRight()) {
            turn += 1;
        }


        float globalThrustMulti = 1 + ship.getEngineController().getExtendLengthFraction().getCurr();
        if (ship.isEngineBoostActive()) globalThrustMulti *= 1.25f;
        if (newVector.y == 1 || newVector.x < 0 || turn == -1) {
            float thrustMulti = 0.75f * globalThrustMulti;
            if (newVector.x > 0) thrustMulti *= 0.5;
            if (turn == 1) thrustMulti *= 0.5;
            thrust(BR, thrustMulti);
            thrust(MR, thrustMulti);
            thrust(FR, thrustMulti);
        } else {
            thrust(BR, 0);
            thrust(MR, 0);
            thrust(FR, 0);
        }

        if (newVector.y == 1 || newVector.x > 0 || turn == 1) {
            float thrustMulti = 0.75f * globalThrustMulti;
            if (newVector.x < 0) thrustMulti *= 0.5;
            if (turn == -1) thrustMulti *= 0.5;
            thrust(BL, thrustMulti);
            thrust(ML, thrustMulti);
            thrust(FL, thrustMulti);
        } else {
            thrust(BL, 0);
            thrust(ML, 0);
            thrust(FL, 0);
        }
        //Super engines after shoot
        for (vernierEngine data : allEngines) {
            if (data.superThrust) {
                if (data.timePassed < data.superThrustTotalTime) {
                    thrust(data, 2.5f);
                    //Colour shift
                    //data.weapon.getSprite().setColor(new Color(0, 255, 255));
                    Vector2f vel = (Vector2f) Misc.getUnitVectorAtDegreeAngle(data.weapon.getCurrAngle()).scale(180f);
                    Vector2f loc = new Vector2f(data.weapon.getLocation().x + vel.x * 0.15f, data.weapon.getLocation().y + vel.y * 0.15f);
                    engine.addSmokeParticle(loc, vel, MathUtils.getRandomNumberInRange(10f, 30f), MathUtils.getRandomNumberInRange(0.5f, 0.9f), 0.5f, new Color(50, 50, 50, 50));
                    data.timePassed += amount;
                } else {
                    data.timePassed = 0;
                    data.superThrust = false;
                }
            }
            //data.weapon.getSprite().setColor(ship.getEngineController().getFlameColorShifter().getCurrForBase(ship.getEngineController().getFlameColorShifter().getBase()));
        }
        //ship.getEngineController().fadeToOtherColor("shift", new Color(0, 255, 0, 255), null, 1, 1);
        //ship.getEngineController().fadeToOtherColor("shift2", new Color(255, 0, 0, 255), null, 1, 1);
        Color shift = ship.getEngineController().getFlameColorShifter().getCurr();
        String text = shift.getRed() + "/" + shift.getGreen() + "/" + shift.getBlue() + "/" + shift.getAlpha();
        //engine.maintainStatusForPlayerShip("enginecolour", null, "shift", text, false);
    }

    private void animateMarker(animateState animation, float amount, WeaponAPI marker1, WeaponAPI marker2) {

        Color color;
        if (animation.animate) {
            if (animation.animationTime <= animTimeIn) {
                color = new Color(255, 255, 255, (Math.round(MagicAnim.smooth((animation.animationTime / animTimeIn)) * 255)));
            } else if (animation.animationTime - animTimeIn <= animStay) {
                color = new Color(255, 255, 255, 255);
            } else {
                color = new Color(255, 255, 255, (Math.round(MagicAnim.smooth(1 - ((animation.animationTime - animTimeIn - animStay) / animTimeOut)) * 255)));
            }

            marker1.getSprite().setColor(color);
            marker2.getSprite().setColor(color);
            animation.animationTime += amount;
            if (animation.animationTime >= animTimeIn + animTimeIn + animTimeOut) {
                animation.animationTime = 0;
                animation.animate = false;
            }
        }
    }

    final Color PARTICLE_COLOR = new Color(113, 255, 237);
    final Color GLOW_COLOR = new Color(0, 203, 186, 225);
    final Color FLASH_COLOR = new Color(255, 255, 255);

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

        ShipAPI ship = weapon.getShip();
        Vector2f weaponLocation = weapon.getLocation();
        float shipFacing = weapon.getCurrAngle();
        Vector2f shipVelocity = ship.getVelocity();
        Vector2f muzzleLocation = MathUtils.getPointOnCircumference(weaponLocation,
                weapon.getSlot().isHardpoint() ? MUZZLE_OFFSET_HARDPOINT : MUZZLE_OFFSET_TURRET, shipFacing);
        String sound = "vic_astronomicon_shot1";

        int particleCount = 15;

        if (ammoOnShot == 1) {
            for (int i = 0; i < 15; i++) {
                CombatEntityAPI newProj = engine.spawnProjectile(weapon.getShip(), weapon, "vic_stolasBuildIn1", projectile.getLocation(), projectile.getFacing() + MathUtils.getRandomNumberInRange(-5f,5f), weapon.getShip().getVelocity());
                newProj.getVelocity().scale(MathUtils.getRandomNumberInRange(0.85f,1.15f));
            }
            engine.removeEntity(projectile);
            BL.superThrust = true;
            BR.superThrust = true;
        } else if (ammoOnShot == 2) {
            sound = "vic_astronomicon_shot2";
            engine.spawnProjectile(weapon.getShip(), weapon, "vic_stolasBuildIn2", projectile.getLocation(), projectile.getFacing(), weapon.getShip().getVelocity());
            engine.removeEntity(projectile);
            weapon.setAmmo(0);

            BL.superThrust = true;
            BR.superThrust = true;
            ML.superThrust = true;
            MR.superThrust = true;
            particleCount = 30;
        } else if (ammoOnShot == 3) {
            sound = "vic_astronomicon_shot3";
            CombatEntityAPI proj = engine.spawnProjectile(weapon.getShip(), weapon, "vic_stolasBuildIn3", projectile.getLocation(), projectile.getFacing(), weapon.getShip().getVelocity());
            engine.removeEntity(projectile);
            weapon.setAmmo(0);

            BL.superThrust = true;
            BR.superThrust = true;
            ML.superThrust = true;
            MR.superThrust = true;
            FL.superThrust = true;
            FR.superThrust = true;

            particleCount = 50;

            Vector2f offset = (Vector2f) Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle() + 90).scale(10);
            Vector2f from = bar1.getLocation();
            for (int i = 0; i <= 4; i++) {
                float random = MathUtils.getRandomNumberInRange(-1, 1);
                engine.spawnEmpArcVisual(new Vector2f(from.x + offset.x * random, from.y + offset.y * random),
                        weapon.getShip(),
                        proj.getLocation(),
                        proj,
                        15,
                        new Color(66, 255, 225, 255),
                        new Color(255, 255, 255, 255));

            }
        }

        Vector2f point = weapon.getFirePoint(0);

        Global.getCombatEngine().spawnExplosion(point, new Vector2f(0f, 0f), PARTICLE_COLOR, 160f, 0.2f);
        Global.getCombatEngine().spawnExplosion(point, new Vector2f(0f, 0f), FLASH_COLOR, 80f, 0.2f);
        engine.addSmoothParticle(point, ZERO, 200f, 0.7f, 0.1f, PARTICLE_COLOR);
        engine.addSmoothParticle(point, ZERO, 300f, 0.7f, 1f, GLOW_COLOR);
        engine.addHitParticle(point, ZERO, 500f, 1f, 0.1f, FLASH_COLOR);

        for (int x = 0; x < particleCount; x++) {
            engine.addHitParticle(point,
                    MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(10f, 150f), (float) Math.random() * 360f),
                    MathUtils.getRandomNumberInRange(5f, 15f),
                    1f,
                    MathUtils.getRandomNumberInRange(0.5f, 1f),
                    PARTICLE_COLOR);
        }


        WaveDistortion wave = new WaveDistortion(muzzleLocation, shipVelocity);
        wave.setIntensity(10f);
        wave.setSize(300f);
        wave.flip(true);
        wave.setLifetime(0f);
        wave.fadeOutIntensity(0.75f);
        wave.setLocation(muzzleLocation);
        DistortionShader.addDistortion(wave);

        Global.getSoundPlayer().playSound(sound, 1, 1, weaponLocation, weapon.getShip().getVelocity());

    }

    private void thrust(vernierEngine data, float thrust) {
        Vector2f size = new Vector2f(15, 80);
        float smooth = 0.15f;
        WeaponAPI weapon = data.weapon;
        if (data.engine.isDisabled()) thrust = 0f;

        //random sprite

        int frame = MathUtils.getRandomNumberInRange(1, data.frames - 1);
        if (frame == weapon.getAnimation().getNumFrames()) {
            frame = 1;
        }
        weapon.getAnimation().setFrame(frame);
        SpriteAPI sprite = weapon.getSprite();


        //target angle
        float length = thrust;


        //thrust is reduced while the engine isn't facing the target angle, then smoothed
        length -= data.previousThrust;
        length *= smooth;
        length += data.previousThrust;
        data.previousThrust = length;


        //finally the actual sprite manipulation
        float width = length * size.x / 2 + size.x / 2;
        float height = length * size.y + (float) Math.random() * 3 + 3;
        sprite.setSize(width, height);
        sprite.setCenter(width / 2, height / 2);

        //clamp the thrust then color stuff
        length = Math.max(0, Math.min(1, length));

        Color engineColor = data.engine.getEngineColor();
        Color shift = ship.getEngineController().getFlameColorShifter().getCurr();
        float ratio = shift.getAlpha() / 255f;
        int Red = Math.min(255, Math.round(engineColor.getRed() * (1f - ratio) + shift.getRed() * ratio));
        int Green = Math.min(255, Math.round((engineColor.getGreen() * (1f - ratio) + shift.getGreen() * ratio) * (0.5f + length / 2)));
        int Blue = Math.min(255, Math.round((engineColor.getBlue() * (1f - ratio) + shift.getBlue() * ratio) * (0.75f + length / 4)));

        sprite.setColor(new Color(Red, Green, Blue));
    }


    public static class vic_stolasDamageListener implements DamageListener {
        ShipAPI ship;
        WeaponAPI weapon;

        vic_stolasDamageListener(ShipAPI ship, WeaponAPI weapon) {
            this.ship = ship;
            this.weapon = weapon;
        }

        @Override
        public void reportDamageApplied(Object source, CombatEntityAPI target, ApplyDamageResultAPI result) {
            if (source instanceof ShipAPI) {
                if (!source.equals(ship)) {
                    return;
                }
                if (!ship.isAlive()) {
                    Global.getCombatEngine().getListenerManager().removeListener(this);
                }
            }
            if (target instanceof ShipAPI) {
                if (!((ShipAPI) target).isAlive()) return;
            }
            if (weapon.getChargeLevel() != 0) {
                return;
            }
            float totalDamage = 0;
            totalDamage += result.getDamageToHull();
            totalDamage += result.getDamageToShields();
            totalDamage += result.getTotalDamageToArmor();
            Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();

            float currDamage = 0f;

            if (customCombatData.get("vic_stolasBuildIn" + ship.getId()) instanceof Float)
                currDamage = (float) customCombatData.get("vic_stolasBuildIn" + ship.getId());

            currDamage += totalDamage;

            customCombatData.put("vic_stolasBuildIn" + ship.getId(), currDamage);
        }
    }

    private class vernierEngine {

        vernierEngine(WeaponAPI weapon) {
            this.weapon = weapon;
            this.frames = weapon.getAnimation().getNumFrames();
            for (ShipEngineControllerAPI.ShipEngineAPI e : ship.getEngineController().getShipEngines()) {
                if (MathUtils.isWithinRange(e.getLocation(), weapon.getLocation(), 2)) {
                    this.engine = e;
                }
            }
        }

        WeaponAPI weapon;
        ShipEngineControllerAPI.ShipEngineAPI engine;
        int frames;
        float previousThrust;
        boolean superThrust = false;
        float superThrustTotalTime = 1.25f;
        float timePassed = 0;

    }

    private static class animateState {
        boolean animate = false;
        float animationTime = 0;
    }
}
