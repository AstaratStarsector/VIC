package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.shipsystems.vic_shockDischarger;
import data.scripts.util.MagicAnim;
import data.scripts.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class vic_combatPlugin extends BaseEveryFrameCombatPlugin {

    private static final String DATA_KEY = "vic_combatPluginData";
    private CombatEngineAPI engine;

    //Verlioka
    private final IntervalUtil timer = new IntervalUtil(0.25f, 0.25f);


    //FluxRapture
    private final SpriteAPI OuterRing = Global.getSettings().getSprite("fx", "vic_fluxRaptureSuck");
    private final SpriteAPI InnerRing = Global.getSettings().getSprite("fx", "vic_fluxRaptureZap");

    {
        float OuterRingRadius = vic_shockDischarger.suckRange;
        float InnerRingRadius = vic_shockDischarger.shockRange;

        OuterRing.setSize(OuterRingRadius, OuterRingRadius);
        InnerRing.setSize(InnerRingRadius, InnerRingRadius);
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData());
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {

        if (engine == null) return;
        if (engine.isPaused()) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        HashMap<ShipAPI, Float> cloneMap;

        //animation advance
        final List<animationRenderData> animationRenderList = localData.animationRenderList;
        for (animationRenderData FX : animationRenderList) {
            FX.time += amount;
        }
        List<animationRenderData> cloneListRender = new ArrayList<>(animationRenderList);
        for (animationRenderData FX : cloneListRender) {
            if (FX.time >= FX.duration) {
                animationRenderList.remove(FX);
            }
        }
        /*
        final List<NawiaFxData> NawiaFxList = localData.NawiaFxList;
        for (NawiaFxData FX : NawiaFxList){
            FX.timePast += amount;
        }
        List<NawiaFxData> cloneList = new ArrayList<>(NawiaFxList);
        for (NawiaFxData FX : cloneList) {
            if (FX.timePast >= FX.animTime) {
                NawiaFxList.remove(FX);
            }
        }

         */

        //Defence Suppressor
        for (Map.Entry<ShipAPI, Float> entry : localData.defenceSuppressor.entrySet()) {
            if (!entry.getKey().isAlive() || entry.getValue() - amount < 0) {
                MutableShipStatsAPI stats = entry.getKey().getMutableStats();

                stats.getShieldDamageTakenMult().unmodify("vic_defenceSuppressor");
                stats.getShieldUpkeepMult().unmodify("vic_defenceSuppressor");

                stats.getPhaseCloakActivationCostBonus().unmodify("vic_defenceSuppressor");
                stats.getPhaseCloakCooldownBonus().unmodify("vic_defenceSuppressor");
                stats.getPhaseCloakUpkeepCostBonus().unmodify("vic_defenceSuppressor");

                stats.getEffectiveArmorBonus().unmodify("vic_defenceSuppressor");
                stats.getMaxArmorDamageReduction().unmodify("vic_defenceSuppressor");
            } else {
                entry.setValue(entry.getValue() - amount);
                entry.getKey().setJitterShields(false);
                entry.getKey().setJitterUnder(entry.getKey(), new Color(106, 0, 255), 4, 8, 2);
                MutableShipStatsAPI stats = entry.getKey().getMutableStats();

                stats.getShieldDamageTakenMult().modifyMult("vic_defenceSuppressor", 1.5f);
                stats.getShieldUpkeepMult().modifyMult("vic_defenceSuppressor", 1.5f);
                stats.getDynamic().getStat(Stats.SHIELD_PIERCED_MULT).modifyMult("vic_defenceSuppressor", 1 + 0.5f);

                stats.getPhaseCloakActivationCostBonus().modifyMult("vic_defenceSuppressor", 1.5f);
                stats.getPhaseCloakCooldownBonus().modifyMult("vic_defenceSuppressor", 1.5f);
                stats.getPhaseCloakUpkeepCostBonus().modifyMult("vic_defenceSuppressor", 1.5f);

                stats.getEffectiveArmorBonus().modifyMult("vic_defenceSuppressor", 0.5f);
                stats.getMaxArmorDamageReduction().modifyFlat("vic_defenceSuppressor", -0.075f);
                if (entry.getKey() == engine.getPlayerShip())
                    engine.maintainStatusForPlayerShip("vic_defenceSuppressor_effect", "graphics/icons/hullsys/vic_defenceSuppressor.png", "Defence Suppressor", "Defenses systems suppressed", true);
            }
        }
        cloneMap = new HashMap<>(localData.defenceSuppressor);
        for (Map.Entry<ShipAPI, Float> entry : cloneMap.entrySet()) {
            if (entry.getValue() <= 0)
                localData.defenceSuppressor.remove(entry.getKey());
        }

        //Flux Rapture
        for (Map.Entry<ShipAPI, Float> entry : localData.FluxRaptureRender.entrySet()) {
            entry.setValue(entry.getValue() + amount);
        }

        cloneMap = new HashMap<>(localData.FluxRaptureRender);
        for (Map.Entry<ShipAPI, Float> entry : cloneMap.entrySet()) {
            if (entry.getKey().getSystem().getState().equals(ShipSystemAPI.SystemState.COOLDOWN) || entry.getKey().getSystem().getState().equals(ShipSystemAPI.SystemState.IDLE))
                localData.FluxRaptureRender.remove(entry.getKey());
        }

        //Zlydzen
        for (Map.Entry<ShipAPI, ZlydzenTargetsData> entry : localData.ZlydzenTargets.entrySet()) {
            if (!entry.getKey().isAlive() || entry.getValue().power <= 0) {
                MutableShipStatsAPI stats = entry.getKey().getMutableStats();

                stats.getShieldDamageTakenMult().unmodify("vic_zlydzen_effect");
                stats.getShieldUpkeepMult().unmodify("vic_zlydzen_effect");

                stats.getPhaseCloakActivationCostBonus().unmodify("vic_zlydzen_effect");
                stats.getPhaseCloakCooldownBonus().unmodify("vic_zlydzen_effect");
                stats.getPhaseCloakUpkeepCostBonus().unmodify("vic_zlydzen_effect");

                stats.getEffectiveArmorBonus().unmodify("vic_zlydzen_effect");
            } else {
                if (entry.getValue().wasAffectedLastCheck) {
                    entry.getValue().wasAffectedLastCheck = false;
                } else {
                    entry.getValue().advance(amount);
                }
//                entry.getKey().setJitterShields(false);
//                entry.getKey().setJitterUnder(entry.getKey(), new Color(106, 0, 255), 4, 8, 2);
                MutableShipStatsAPI stats = entry.getKey().getMutableStats();
                float effectLevel = entry.getValue().power;

                stats.getShieldDamageTakenMult().modifyMult("vic_zlydzen_effect", 1 + (0.1f * effectLevel));
                stats.getShieldUpkeepMult().modifyMult("vic_zlydzen_effect", 1 + (0.1f * effectLevel));

                stats.getPhaseCloakActivationCostBonus().modifyMult("vic_zlydzen_effect", 1 + (0.2f * effectLevel));
                stats.getPhaseCloakCooldownBonus().modifyMult("vic_zlydzen_effect", 1 + (0.2f * effectLevel));
                stats.getPhaseCloakUpkeepCostBonus().modifyMult("vic_zlydzen_effect", 1 + (0.2f * effectLevel));

                stats.getEffectiveArmorBonus().modifyMult("vic_zlydzen_effect", 1 - (0.1f * effectLevel));
                if (entry.getKey() == engine.getPlayerShip())
                    engine.maintainStatusForPlayerShip("vic_zlydzen_effect", "graphics/icons/hullsys/vic_zlydzenEffect.png", "Disruptor Beam", "Ship effectiveness reduced", true);
            }
        }
        HashMap<ShipAPI, ZlydzenTargetsData> cloneMapZ = new HashMap<>(localData.ZlydzenTargets);
        for (Map.Entry<ShipAPI, ZlydzenTargetsData> entry : cloneMapZ.entrySet()) {
            if (entry.getValue().power <= 0)
                localData.ZlydzenTargets.remove(entry.getKey());
        }
        //ArcaneMissiles
        for (Map.Entry<DamagingProjectileAPI, ArcaneMissilesData> entry : localData.ArcaneMissiles.entrySet()) {
            ArcaneMissilesData data = entry.getValue();
            if (data.time < data.timeBeforeCurving) {
                data.time += amount;
            } else if (data.rotate) {
                DamagingProjectileAPI proj = data.proj;
                if (data.rotationSpeed == null) {
                    proj.getVelocity().scale(data.speedUpOnCurving);
                    float flightTime = MathUtils.getDistance(proj.getLocation(), data.finalPoint) / Math.abs(MathUtils.getDistance(new Vector2f(), proj.getVelocity()));
                    float angle = MathUtils.getShortestRotation(proj.getFacing(), VectorUtils.getAngle(proj.getLocation(), data.finalPoint));
                    data.rotationSpeed = (angle / flightTime) * (2f);
                    engine.addHitParticle(proj.getLocation(), new Vector2f(), 30, 1f, 0.10f, new Color(255, 255, 255, 255));
                    MagicRender.battlespace(
                            Global.getSettings().getSprite("fx","vic_laidlawExplosion3"),
                            proj.getLocation(),
                            new Vector2f(),
                            new Vector2f(60,60),
                            new Vector2f(40,80),
                            //angle,
                            180f + proj.getFacing(),
                            0,
                            new Color(255,225,225,255),
                            true,
                            0.0f,
                            0.0f,
                            0.35f
                    );

                    MagicRender.battlespace(
                            Global.getSettings().getSprite("fx","vic_laidlawExplosion3"),
                            proj.getLocation(),
                            new Vector2f(),
                            new Vector2f(60,60),
                            new Vector2f(300,600),
                            //angle,
                            180f + proj.getFacing(),
                            0,
                            new Color(255,225,225,175),
                            true,
                            0.0f,
                            0.0f,
                            0.2f
                    );

                    //turn instantly if rotation speed too high
                    if (Math.abs(data.rotationSpeed) >= 800) {
                        VectorUtils.rotate(proj.getVelocity(), angle, proj.getVelocity());
                        proj.setFacing(angle + proj.getFacing());
                        data.rotate = false;
                        //engine.addFloatingText(proj.getLocation(), Math.round(data.rotationSpeed) + "", 20, Color.WHITE, null, 0, 0);
                    } else {
                        float angle2 = data.rotationSpeed * data.timeBeforeCurving * (1 - data.rangeBeforeCurving) * 0.25f;
                        VectorUtils.rotate(proj.getVelocity(), angle2, proj.getVelocity());
                        proj.setFacing(angle2 + proj.getFacing());

                        angle = MathUtils.getShortestRotation(proj.getFacing(), VectorUtils.getAngle(proj.getLocation(), data.finalPoint));
                        data.rotationSpeed = (angle / flightTime) * (2f);
                        proj.getVelocity().scale(1 + Math.abs(angle * 0.0025f));
                    }
                    } else {
                    float angle = data.rotationSpeed * amount;
                    if (Math.abs(angle) > 0) {
                        VectorUtils.rotate(proj.getVelocity(), angle, proj.getVelocity());
                        proj.setFacing(angle + proj.getFacing());
                    }
                    if (MathUtils.isWithinRange(proj.getLocation(), data.finalPoint, 30))
                        data.rotate = false;
                }
            }
        }
        HashMap<DamagingProjectileAPI, ArcaneMissilesData> cloneMapA = new HashMap<>(localData.ArcaneMissiles);
        for (Map.Entry<DamagingProjectileAPI, ArcaneMissilesData> entry : cloneMapA.entrySet()) {
            if (!engine.isEntityInPlay(entry.getKey()))
                localData.ArcaneMissiles.remove(entry.getKey());
        }
    }

    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);

        final List<NawiaFxData> NawiaFxList = localData.NawiaFxList;
        for (NawiaFxData FX : NawiaFxList) {
            float fractionTimePast = FX.timePast / FX.animTime;
            fractionTimePast = MagicAnim.smooth(fractionTimePast);

            if (FX.timePast <= FX.ring1_MaxTime) {
                FX.ring1.setAngle(FX.ring1_Angle + FX.angle - (FX.ring1_RotationSpeed * FX.timePast));
                FX.ring1.setSize(FX.ring1_Size + FX.ring1_grow * fractionTimePast, FX.ring1_Size + FX.ring1_grow * fractionTimePast);
                FX.ring1.setNormalBlend();
                FX.ring1.setAlphaMult(1 - fractionTimePast);
                FX.ring1.renderAtCenter(FX.location.x, FX.location.y);
            }

            if (FX.timePast <= FX.ring2_MaxTime) {
                FX.ring2.setAngle(FX.ring2_Angle + FX.angle + (FX.ring2_RotationSpeed * FX.timePast));
                FX.ring2.setSize(FX.ring2_Size + FX.ring2_grow * fractionTimePast, -(FX.ring2_Size + FX.ring2_grow * fractionTimePast));
                FX.ring1.setNormalBlend();
                FX.ring2.setAlphaMult(1 - fractionTimePast);
                FX.ring2.renderAtCenter(FX.location.x, FX.location.y);
            }
        }

        //final List<ShipAPI> AurasToRender = localData.FluxRaptureRender;
        for (Map.Entry<ShipAPI, Float> entry : localData.FluxRaptureRender.entrySet()) {
            ShipAPI ship = entry.getKey();
            ShipSystemAPI system = ship.getSystem();
            float effectLevel = system.getEffectLevel();
            float angle = entry.getValue() * 5;
            ShipSystemAPI.SystemState state = system.getState();

            float alphaMultOuter = 0.35f;
            if (effectLevel <= 0.2f) {
                alphaMultOuter *= MagicAnim.smooth(effectLevel * 5f);
            } else if (effectLevel >= 0.8f) {
                alphaMultOuter *= MagicAnim.smooth((1 - effectLevel) * 5f);
            }

            float alphaMultInner = 0.35f;
            switch (state) {
                case IN:
                    if (effectLevel <= 0.2f) {
                        alphaMultInner *= MagicAnim.smooth(effectLevel * 5f);
                    }
                    break;
                case OUT:
                    alphaMultInner *= MagicAnim.smooth(effectLevel);
                    break;
            }

            switch (state) {
                case IN:
                    OuterRing.setAlphaMult(alphaMultOuter);
                    OuterRing.setCenter(OuterRing.getHeight(), 0);
                    for (float i = 0; i < 4; i++) {
                        OuterRing.setAngle(-angle + 90 * i);
                        OuterRing.renderAtCenter(ship.getLocation().getX(), ship.getLocation().getY());
                    }
                case ACTIVE:
                case OUT:
                    InnerRing.setAlphaMult(alphaMultInner);
                    InnerRing.setCenter(InnerRing.getHeight(), 0);
                    for (float i = 0; i < 4; i++) {
                        InnerRing.setAngle(angle + 90 * i);
                        InnerRing.renderAtCenter(ship.getLocation().getX(), ship.getLocation().getY());
                    }
            }
        }
        for (animationRenderData FX : localData.animationRenderList) {
            int frame = (int) (FX.FPS * FX.time);
            if (frame > FX.numFrames - 1) frame = FX.numFrames - 1;
            String frameNum;
            if (frame < 10) {
                frameNum = "0" + frame;
            } else {
                frameNum = "" + frame;
            }
            SpriteAPI sprite = Global.getSettings().getSprite("fx", FX.spriteName + frameNum);
            float flip = FX.flip;
            if (FX.size != null){
                sprite.setSize(FX.size.x,FX.size.y * flip);
            } else {
                sprite.setWidth(sprite.getWidth() * flip);
            }
            sprite.setAngle(FX.angle);
            sprite.renderAtCenter(FX.position.x, FX.position.y);
        }
    }

    public static void AddNawiaFX(Vector2f location, float angle) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        localData.NawiaFxList.add(new NawiaFxData(location, angle));
    }

    public static void AddDefenceSuppressorTarget(ShipAPI ship, float Duration) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (localData.defenceSuppressor.containsKey(ship)) {
            float durationMult = Duration / localData.defenceSuppressor.get(ship) * 2;
            if (durationMult > 1) durationMult = 1;
            localData.defenceSuppressor.put(ship, localData.defenceSuppressor.get(ship) + (Duration * durationMult));
        } else {
            localData.defenceSuppressor.put(ship, Duration);
        }
    }

    public static void markTargetDamagedByZlydzen(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (localData.ZlydzenTargets.containsKey(ship)) {
            localData.ZlydzenTargets.get(ship).markAsHit();
            localData.ZlydzenTargets.get(ship).advance(amount);
        } else {
            localData.ZlydzenTargets.put(ship, new ZlydzenTargetsData(ship, amount));
        }
    }

    public static void AddFluxRaptureShip(ShipAPI ship) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        localData.FluxRaptureRender.put(ship, 0f);
    }

    public static void AddArcaneMissiles(DamagingProjectileAPI proj, Vector2f target, float time, float rangeBeforeCurving, float startingSpeed) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        localData.ArcaneMissiles.put(proj, new ArcaneMissilesData(proj, target, time, rangeBeforeCurving, startingSpeed));
    }

    private static final class LocalData {
        final List<NawiaFxData> NawiaFxList = new ArrayList<>(250);
        final List<animationRenderData> animationRenderList = new ArrayList<>(250);
        final HashMap<ShipAPI, Float> FluxRaptureRender = new HashMap<>(10);
        final HashMap<ShipAPI, Float> defenceSuppressor = new HashMap<>(25);
        final HashMap<ShipAPI, ZlydzenTargetsData> ZlydzenTargets = new HashMap<>(50);
        final HashMap<DamagingProjectileAPI, ArcaneMissilesData> ArcaneMissiles = new HashMap<>(250);
    }

    private static final class ZlydzenTargetsData {

        public ZlydzenTargetsData(ShipAPI target, float amount) {
            this.target = target;
            this.wasAffectedLastCheck = true;
            advance(amount);
        }

        ShipAPI target;
        float power = 0f;
        boolean wasAffectedLastCheck;

        float timeRise = 4f;
        float timeFall = 2f;

        public void advance(float amount) {
            if (wasAffectedLastCheck) {
                power += amount / timeRise;
            } else {
                power -= amount / timeFall;
            }
            if (power > 1) power = 1;
            if (power < 0) power = 0;
        }

        public void markAsHit() {
            this.wasAffectedLastCheck = true;
        }
    }

    private static final class ArcaneMissilesData {

        public ArcaneMissilesData(DamagingProjectileAPI proj, Vector2f target, float timeBeforeCurving, float rangeBeforeCurving, float startingSpeed) {
            this.proj = proj;
            this.finalPoint = target;
            this.timeBeforeCurving = timeBeforeCurving;
            this.startingSpeed = startingSpeed;
            this.rangeBeforeCurving = rangeBeforeCurving;
            this.speedUpOnCurving = 1 / startingSpeed;
            this.speedUpOnCurving *= (1 - startingSpeed * rangeBeforeCurving) / (1 - rangeBeforeCurving);
        }

        DamagingProjectileAPI proj;
        final Vector2f finalPoint;
        float time;
        final float timeBeforeCurving;
        Float rotationSpeed = null;
        boolean rotate = true;
        float startingSpeed;
        float rangeBeforeCurving;
        float speedUpOnCurving = 1;
    }

    private static final class NawiaFxData {

        private final ArrayList<SpriteAPI> ringList = new ArrayList<>();

        {
            ringList.add(Global.getSettings().getSprite("fx", "vic_nawia_ring1"));
            ringList.add(Global.getSettings().getSprite("fx", "vic_nawia_ring2"));
        }

        public NawiaFxData(Vector2f location, float angle) {
            this.location = location;
            this.angle = angle;

            animTime = MathUtils.getRandomNumberInRange(0.5f, 0.6f);

            ring1 = ringList.get(MathUtils.getRandomNumberInRange(0, ringList.size() - 1));

            ring1_MaxTime = animTime * MathUtils.getRandomNumberInRange(1f, 1f);
            ring1_Size = MathUtils.getRandomNumberInRange(10, 20);
            ring1_grow = MathUtils.getRandomNumberInRange(12, 16);
            ring1_RotationSpeed = MathUtils.getRandomNumberInRange(200f, 300f);
            ring1_Angle = MathUtils.getRandomNumberInRange(-45f, 45f);

            ring2 = ringList.get(MathUtils.getRandomNumberInRange(0, ringList.size() - 1));

            ring2_MaxTime = animTime * MathUtils.getRandomNumberInRange(1f, 1f);
            ring2_Size = ring1_Size * MathUtils.getRandomNumberInRange(1.3f, 1.4f);
            ring2_grow = MathUtils.getRandomNumberInRange(12, 16);
            ring2_RotationSpeed = MathUtils.getRandomNumberInRange(200f, 300f);
            ring2_Angle = MathUtils.getRandomNumberInRange(-45f, 45f);

            Global.getCombatEngine().addSmoothParticle(
                    location,
                    new Vector2f(),
                    MathUtils.getRandomNumberInRange(20, 30),
                    1,
                    animTime,
                    new Color(MathUtils.getRandomNumberInRange(130, 180), MathUtils.getRandomNumberInRange(20, 60), 255, 255)
            );
            for (float i = 0; i < MathUtils.getRandomNumberInRange(4, 8); i++) {

                Vector2f move = Misc.getUnitVectorAtDegreeAngle(MathUtils.getRandomNumberInRange(-20, 20) + angle);
                Global.getCombatEngine().addHitParticle(
                        location,
                        new Vector2f(move.x * MathUtils.getRandomNumberInRange(25, 125), move.y * MathUtils.getRandomNumberInRange(25, 125)),
                        MathUtils.getRandomNumberInRange(5, 20),
                        MathUtils.getRandomNumberInRange(0.8f, 1f),
                        animTime * MathUtils.getRandomNumberInRange(0.5f, 1.5f),
                        new Color(MathUtils.getRandomNumberInRange(90, 180), MathUtils.getRandomNumberInRange(20, 100), 255, 255)
                );
            }
        }

        Vector2f location;
        float angle;
        float animTime;

        SpriteAPI ring1;
        float
                ring1_MaxTime,
                ring1_Size,
                ring1_grow,
                ring1_RotationSpeed,
                ring1_Angle;

        SpriteAPI ring2;
        float
                ring2_MaxTime,
                ring2_Size,
                ring2_grow,
                ring2_RotationSpeed,
                ring2_Angle;

        float timePast = 0;

        private void advance(float amount) {
            timePast += amount;
        }
    }

    private static final class animationRenderData {

        public animationRenderData(String spriteName, int numFrames, float duration, float angle, Vector2f position, Vector2f size, boolean flip) {
            this.spriteName = spriteName;
            this.numFrames = numFrames;
            this.duration = duration;
            this.FPS = numFrames / duration;
            this.angle = angle;
            this.position = new Vector2f(position);
            this.size = size;
            if(flip && Math.random()>0.5f) this.flip = -1;
        }

        public animationRenderData(String spriteName, int numFrames, float duration, float angle, Vector2f position, Vector2f size) {
            this.spriteName = spriteName;
            this.numFrames = numFrames;
            this.duration = duration;
            this.FPS = numFrames / duration;
            this.angle = angle;
            this.position = new Vector2f(position);
            this.size = size;
        }

        public animationRenderData(String spriteName, int numFrames, float duration, float angle, Vector2f position) {
            this.spriteName = spriteName;
            this.numFrames = numFrames;
            this.duration = duration;
            this.FPS = numFrames / duration;
            this.angle = angle;
            this.position = new Vector2f(position);
            this.size = null;
        }

        private float time = 0;
        private final String spriteName;
        private final int numFrames;
        private final float duration;
        private final float FPS;
        private final float angle;
        private final Vector2f position;
        private final Vector2f size;
        private int flip = 1;
    }
}