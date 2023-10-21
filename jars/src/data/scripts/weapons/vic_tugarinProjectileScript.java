package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import data.scripts.plugins.MagicTrailPlugin;
import data.scripts.util.MagicAnim;
import org.magiclib.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

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

    private boolean spawnParticle = true;


    public vic_tugarinProjectileScript(DamagingProjectileAPI proj) {
        this.proj = proj;
        this.ship = proj.getSource();
        this.flightTime = proj.getWeapon().getRange() / proj.getMoveSpeed();
        this.flightTimeFraction = 1 / flightTime;
        this.engine = Global.getCombatEngine();
        proj.getVelocity().scale(2);
        Vector2f speed = new Vector2f(proj.getVelocity().x - proj.getSource().getVelocity().x, proj.getVelocity().y - proj.getSource().getVelocity().y);
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
                new Color(MathUtils.getRandomNumberInRange(215, 255), MathUtils.getRandomNumberInRange(130, 170), MathUtils.getRandomNumberInRange(15, 55), 255)
        );
        explosion.setDamageType(DamageType.HIGH_EXPLOSIVE);

        rotationSpeed = MathUtils.getRandomNumberInRange(120, 240);
        rotationDirection = (Math.random() >= 0.5 ? 1 : -1);
        ringAngle1 = MathUtils.getRandomNumberInRange(-40, 40);
        ringAngle2 = MathUtils.getRandomNumberInRange(-40, 40);
        ringRotationDirection = (Math.random() >= 0.5 ? 1 : -1);
        ringRotationSpeed1 = MathUtils.getRandomNumberInRange(40, 80);
        ringRotationSpeed2 = MathUtils.getRandomNumberInRange(40, 80);
    }

    //Main phase color
    private static final Color PHASE_COLOR = new Color(235, 135, 5, 255);


    //For our "drill" effects
    private final float drillSpeed = 100f;
    private float[] drillTrailIDs = {0f, 0f, 0f};

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine.isPaused()) return;
        //if (proj.isFading()) return;
        //Moves the "phantom" to its appropriate location
        Vector2f phantomPos = MathUtils.getRandomPointInCircle(null, 55f);
        phantomPos.x += proj.getLocation().x;
        phantomPos.y += proj.getLocation().y;

        //And finally spawn our "drill trails"
        //If we have not gotten any IDs for them yet, get some IDs
        if (drillTrailIDs[0] == 0f) {
            for (int i = 0; i < drillTrailIDs.length; i++) {
                drillTrailIDs[i] = MagicTrailPlugin.getUniqueID();
            }
        }

        //Then, spawn six trails, in two different positions, and offset them by angle
        SpriteAPI spriteToUse = Global.getSettings().getSprite("fx", "SRD_trail_helix");
        for (int i = 0; i < 1; i++) {
            Vector2f positionToSpawn = new Vector2f(proj.getLocation().x, proj.getLocation().y);
            positionToSpawn = VectorUtils.rotateAroundPivot(positionToSpawn, proj.getLocation(), proj.getFacing(), new Vector2f(0f, 0f));
            MagicTrailPlugin.AddTrailMemberAdvanced(proj, drillTrailIDs[i], spriteToUse, positionToSpawn, 0, 0 * 0.5f,
                    proj.getFacing() + 180f, 0f, 0f, 24f,
                    128f, PHASE_COLOR, Color.RED, 1f, 0f, 0.1f, 0.3f, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA,
                    210f, 200f, new Vector2f(0f, 0f), null, CombatEngineLayers.CONTRAILS_LAYER);
        }


        if (proj.isFading() && spawnParticle) {
            engine.addHitParticle(proj.getLocation(), proj.getVelocity(), 400, 0.35f, 0.2f, 1.5f, PHASE_COLOR);
            spawnParticle = false;
        }

        float newSpeedMult = speedMult - ((flightTimeFraction) * amount * (float) Math.sqrt(speedMult) * 1.8f);
        proj.getVelocity().scale(newSpeedMult / speedMult);
        speedMult = speedMult - ((flightTimeFraction) * amount * (float) Math.sqrt(speedMult) * 1.8f);

        rotation += rotationSpeed * amount * rotationDirection * speedMult;

        ringRotation1 += ringRotationSpeed1 * amount * ringRotationDirection;
        ringRotation2 += ringRotationSpeed2 * amount * -ringRotationDirection;

        if (!engine.isEntityInPlay(proj) || proj.didDamage()) {
            //lens flare
            MagicRender.battlespace(
                    Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow"),
                    new Vector2f(proj.getLocation()),
                    new Vector2f(),
                    new Vector2f(140 * MathUtils.getRandomNumberInRange(0.8f, 1.2f), 1400 * MathUtils.getRandomNumberInRange(0.8f, 1.2f)),
                    new Vector2f(),
                    rotation,
                    0,
                    new Color(MathUtils.getRandomNumberInRange(200, 255), MathUtils.getRandomNumberInRange(50, 86), MathUtils.getRandomNumberInRange(0, 35), 255),
                    true,
                    0,
                    0,
                    0.5f,
                    0.15f,
                    MathUtils.getRandomNumberInRange(0.05f, 0.2f),
                    0,
                    MathUtils.getRandomNumberInRange(0.4f, 0.6f),
                    MathUtils.getRandomNumberInRange(0.1f, 0.3f),
                    CombatEngineLayers.CONTRAILS_LAYER
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


            engine.addHitParticle(
                    new Vector2f(proj.getLocation()),
                    new Vector2f(),
                    700,
                    0.4f,
                    //0,
                    0.15f,
                    Color.WHITE);

            engine.removePlugin(this);
        }
    }

    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {
        super.renderInWorldCoords(viewport);
        if (!MagicRender.screenCheck(0.5f, proj.getLocation())) return;
        float sizeMult = 1;
        if (proj.isFading()) {
            float timeLeft = ((proj.getElapsed() - flightTime) / proj.getProjectileSpec().getFadeTime());
            if (timeLeft <= 0.5f) {
                sizeMult = 1 + (0.25f * MagicAnim.smooth(timeLeft * 2));
            } else {
                timeLeft -= 0.5f;
                timeLeft *= 2;
                sizeMult = 1 * (1 - MagicAnim.smooth(timeLeft)) + 0.25f;
            }
        }


        sprite.setAngle(rotation);
        sprite.setSize(32 * sizeMult, 32 * sizeMult);
        sprite.setAdditiveBlend();
        sprite.renderAtCenter(proj.getLocation().x, proj.getLocation().y);

        float ringSize = 58;

        spriteRing.setAdditiveBlend();

        spriteRing.setAngle(ringAngle1 + ringRotation1);
        spriteRing.setSize(ringSize * sizeMult, ringSize * sizeMult);
        spriteRing.renderAtCenter(proj.getLocation().x, proj.getLocation().y);

        spriteRing.setAngle(ringAngle2 + ringRotation2);
        spriteRing.setSize(-ringSize * sizeMult * 1.1f, ringSize * sizeMult * 1.1f);
        spriteRing.renderAtCenter(proj.getLocation().x, proj.getLocation().y);
    }
}