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
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.MathUtils;
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
        float OuterRingRadius = vic_shockDischarger.suckRange * 2;
        float InnerRingRadius = vic_shockDischarger.shockRange * 2;

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
    }

    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);

        final List<NawiaFxData> NawiaFxList = localData.NawiaFxList;
        for (NawiaFxData FX : NawiaFxList){
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
        for (Map.Entry<ShipAPI, Float> entry : localData.FluxRaptureRender.entrySet()){
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
            switch (state){
                case IN:
                    if (effectLevel <= 0.2f){
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
                    OuterRing.setAngle(-angle);
                    OuterRing.renderAtCenter(ship.getLocation().getX(),ship.getLocation().getY());
                    InnerRing.setAlphaMult(alphaMultInner);
                    InnerRing.setAngle(angle);
                    InnerRing.renderAtCenter(ship.getLocation().getX(),ship.getLocation().getY());
                    break;
                case ACTIVE:
                case OUT:
                    InnerRing.setAlphaMult(alphaMultInner);
                    InnerRing.setAngle(angle);
                    InnerRing.renderAtCenter(ship.getLocation().getX(),ship.getLocation().getY());
            }
        }
    }

    public static void AddNawiaFX(Vector2f location, float angle){
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        localData.NawiaFxList.add(new NawiaFxData(location,angle));
    }

    public static void AddDefenceSuppressorTarget(ShipAPI ship, float Duration){
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (localData.defenceSuppressor.containsKey(ship)){
            float durationMult = Duration / localData.defenceSuppressor.get(ship) * 2;
            if (durationMult > 1) durationMult = 1;
            localData.defenceSuppressor.put(ship, localData.defenceSuppressor.get(ship) + (Duration * durationMult));
        } else {
            localData.defenceSuppressor.put(ship, Duration);
        }
    }

    public static void AddFluxRaptureShip (ShipAPI ship){
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        localData.FluxRaptureRender.put(ship, 0f);
    }

    private static final class LocalData {
        final List<NawiaFxData> NawiaFxList = new ArrayList<>(250);
        final HashMap<ShipAPI, Float> FluxRaptureRender = new HashMap<>(10);
        final HashMap<ShipAPI, Float> defenceSuppressor = new HashMap<>(25);
    }

    private static final class NawiaFxData {

        private final ArrayList<SpriteAPI> ringList = new ArrayList<>();
        {
            ringList.add(Global.getSettings().getSprite("fx", "vic_nawia_ring1"));
            ringList.add(Global.getSettings().getSprite("fx", "vic_nawia_ring2"));
        }

        public NawiaFxData(@NotNull Vector2f location, float angle){
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

        private void advance(float amount){
            timePast += amount;
        }
    }
}