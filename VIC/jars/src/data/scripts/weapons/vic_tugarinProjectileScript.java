package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import data.scripts.util.MagicAnim;
import data.scripts.util.MagicLensFlare;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

public class vic_tugarinProjectileScript extends BaseEveryFrameCombatPlugin {

    private final DamagingProjectileAPI proj;

    private final float
            flightTime,
            rotationSpeed = MathUtils.getRandomNumberInRange(120, 240),
            rotationDirection = (Math.random() >= 0.5 ? 1 : -1),
            ringAngle1 = MathUtils.getRandomNumberInRange(-40, 40),
            ringAngle2 = MathUtils.getRandomNumberInRange(-40, 40),
            ringRotationDirection = (Math.random() >= 0.5 ? 1 : -1),
            ringRotationSpeed1 = MathUtils.getRandomNumberInRange(20, 60),
            ringRotationSpeed2 = MathUtils.getRandomNumberInRange(20, 60);

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
        this.engine = Global.getCombatEngine();
        proj.getVelocity().scale(2);
        float DMG = proj.getBaseDamageAmount() * 0.3f * ship.getMutableStats().getBallisticWeaponDamageMult().getModifiedValue();
        //engine.addFloatingText(proj.getLocation(), DMG + "", 60, Color.WHITE, ship, 0.25f, 0.25f);
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
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine.isPaused()) return;
        //if (proj.isFading()) return;
		if (ringRotation1 == 0){
			float DMG = proj.getWeapon().getDamage().getDamage();
			engine.addFloatingText(proj.getLocation(), DMG + "", 60, Color.WHITE, ship, 0.25f, 0.25f);
			ringRotation1 = 1;
		}



        float newSpeedMult = speedMult - ((1 / flightTime) * amount * (float) Math.sqrt(speedMult) * 1.8f);
        proj.getVelocity().scale(newSpeedMult / speedMult);
        speedMult = speedMult - ((1 / flightTime) * amount * (float) Math.sqrt(speedMult) * 1.8f);

        rotation += rotationSpeed * amount * rotationDirection * speedMult;

        //ringRotation1 += ringRotationSpeed1 * amount * ringRotationDirection;
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
        float timLeft = 1;
        if (proj.isFading()) {
            timLeft = 1 - ((proj.getElapsed() - flightTime) / 0.3f);
            timLeft = MagicAnim.smooth(timLeft);
        }

        //engine.maintainStatusForPlayerShip("vic_adaptiveWarfare3", "graphics/icons/hullsys/vic_adaptiveWarfareSystem.png", "Speed power", timLeft + "%", false);
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