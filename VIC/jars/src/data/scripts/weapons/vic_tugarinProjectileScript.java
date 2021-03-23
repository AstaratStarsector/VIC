package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import data.scripts.util.MagicAnim;
import data.scripts.util.MagicLensFlare;
import data.scripts.util.MagicRender;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

public class vic_tugarinProjectileScript extends BaseEveryFrameCombatPlugin {

    private final DamagingProjectileAPI proj;

    private final float
            flightTime,
            flightTimeFraction,
            rotationSpeed,
            rotationDirection,
            ringAngle1,
            ringAngle2,
            ringRotationDirection,
            ringRotationSpeed1,
            ringRotationSpeed2;

    private final SpriteAPI sprite = Global.getSettings().getSprite("fx", "vic_tugarin_proj");
    private final SpriteAPI spriteRing = Global.getSettings().getSprite("fx", "vic_tugarin_proj_ring");

    private final CombatEngineAPI engine;

    private final ShipAPI ship;
    private final DamagingExplosionSpec explosion;
    private float
            rotation = 0,
            ringRotation1 = 0f,
            ringRotation2 = 0f,
            speedMult = 2;


    public vic_tugarinProjectileScript(@NotNull DamagingProjectileAPI proj) {
        this.proj = proj;
        this.ship = proj.getSource();
        this.flightTime = proj.getWeapon().getRange() / proj.getMoveSpeed();
        this.flightTimeFraction = 1 / flightTime;
        this.engine = Global.getCombatEngine();
        proj.getVelocity().scale(2);
        Vector2f speed = new Vector2f(proj.getVelocity().x - proj.getSource().getVelocity().x,proj.getVelocity().y - proj.getSource().getVelocity().y);
        proj.getVelocity().set(speed);
        float DMG = proj.getBaseDamageAmount() * 0.3f * ship.getMutableStats().getBallisticWeaponDamageMult().getModifiedValue();
        explosion = new DamagingExplosionSpec(0.1f,
                250,
                125,
                DMG,
                DMG * 0.5f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                3,
                3,
                0.5f,
                10,
                new Color(33, 255, 122, 255),
                new Color(255, 150, 35, 255)
        );
        explosion.setDamageType(DamageType.HIGH_EXPLOSIVE);

        rotationSpeed = MathUtils.getRandomNumberInRange(120, 240);
        rotationDirection = (Math.random() >= 0.5 ? 1 : -1);
        ringAngle1 = MathUtils.getRandomNumberInRange(-40, 40);
        ringAngle2 = MathUtils.getRandomNumberInRange(-40, 40);
        ringRotationDirection = (Math.random() >= 0.5 ? 1 : -1);
        ringRotationSpeed1 = MathUtils.getRandomNumberInRange(20, 60);
        ringRotationSpeed2 = MathUtils.getRandomNumberInRange(20, 60);
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine.isPaused()) return;
        //if (proj.isFading()) return;

        float newSpeedMult = speedMult - ((flightTimeFraction) * amount * (float) Math.sqrt(speedMult) * 1.8f);
        proj.getVelocity().scale(newSpeedMult / speedMult);
        speedMult = speedMult - ((flightTimeFraction) * amount * (float) Math.sqrt(speedMult) * 1.8f);

        rotation += rotationSpeed * amount * rotationDirection * speedMult;

        ringRotation1 += ringRotationSpeed1 * amount * ringRotationDirection;
        ringRotation2 += ringRotationSpeed2 * amount * -ringRotationDirection;


        if (!engine.isEntityInPlay(proj) || proj.didDamage()) {
            MagicLensFlare.createSmoothFlare(
                    engine,
                    ship,
                    new Vector2f(proj.getLocation()),
                    70,
                    700,
                    rotation,
                    new Color(255, 86, 35, 255),
                    new Color(255, 150, 35, 186)
            );

            engine.spawnDamagingExplosion(
                    explosion,
                    ship,
                    new Vector2f(proj.getLocation()),
                    false
            );

            Global.getSoundPlayer().playSound(
                    "vic_tugarin_explosion",
                    1,
                    1,
                    proj.getLocation(),
                    new Vector2f()
            );


            engine.removePlugin(this);
        }
    }

    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {
        super.renderInWorldCoords(viewport);
        if (!MagicRender.screenCheck (0.5f, proj.getLocation())) return;
        float timLeft = 1;
        if (proj.isFading()) {
            timLeft = 1 - ((proj.getElapsed() - flightTime) / 0.3f);
            timLeft = MagicAnim.smooth(timLeft);
        }

        sprite.setAngle(rotation);
        sprite.setSize(32 * timLeft, 32 * timLeft);
        sprite.setNormalBlend();
        sprite.renderAtCenter(proj.getLocation().x, proj.getLocation().y);

        float ringSize = 58;

        spriteRing.setAngle(ringAngle1 + ringRotation1);
        spriteRing.setSize(ringSize * timLeft, ringSize * timLeft);
        spriteRing.setNormalBlend();
        spriteRing.renderAtCenter(proj.getLocation().x, proj.getLocation().y);

        spriteRing.setAngle(ringAngle2 + ringRotation2);
        spriteRing.setSize(-ringSize * timLeft * 1.1f, ringSize * timLeft * 1.1f);
        spriteRing.renderAtCenter(proj.getLocation().x, proj.getLocation().y);
    }
}