package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.MagicAnim;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class vic_combatPlugin extends BaseEveryFrameCombatPlugin {

    private static final String DATA_KEY = "vic_combatPluginData";
    private CombatEngineAPI engine;

    //Verlioka
    private final IntervalUtil timer = new IntervalUtil(0.25f, 0.25f);


    //Nawia


    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData());
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
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
    }

    public static void AddNawiaFX(Vector2f location, float angle){
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        localData.NawiaFxList.add(new NawiaFxData(location,angle));
    }

    private static final class LocalData {
        final List<NawiaFxData> NawiaFxList = new ArrayList<>(250);
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