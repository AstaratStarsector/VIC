package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.MagicAnim;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class vic_nawiaVisuals extends BaseEveryFrameCombatPlugin {

    private final ArrayList<SpriteAPI> ringList = new ArrayList<>();
    private final Vector2f location;
    private final float angle;
    private final float animTime;

    private final SpriteAPI ring1;
    private final float
            ring1_MaxTime,
            ring1_Size,
            ring1_grow,
            ring1_RotationSpeed,
            ring1_Angle;

    private final SpriteAPI ring2;
    private final float
            ring2_MaxTime,
            ring2_Size,
            ring2_grow,
            ring2_RotationSpeed,
            ring2_Angle;

    private final CombatEngineAPI engine;
    private float
            timePast = 0;

    {
        ringList.add(Global.getSettings().getSprite("fx", "vic_nawia_ring1"));
        ringList.add(Global.getSettings().getSprite("fx", "vic_nawia_ring2"));
    }

    public vic_nawiaVisuals(@NotNull Vector2f location, float angle) {
        this.location = location;
        this.angle = angle;
        this.engine = Global.getCombatEngine();

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

        engine.addSmoothParticle(
                location,
                new Vector2f(),
                MathUtils.getRandomNumberInRange(20, 30),
                1,
                animTime,
                new Color(MathUtils.getRandomNumberInRange(130, 180), MathUtils.getRandomNumberInRange(20, 60), 255, 255)
        );
        for (float i = 0; i < MathUtils.getRandomNumberInRange(4, 8); i++) {

            Vector2f move = Misc.getUnitVectorAtDegreeAngle(MathUtils.getRandomNumberInRange(-20, 20) + angle);
            engine.addHitParticle(
                    location,
                    new Vector2f(move.x * MathUtils.getRandomNumberInRange(25, 125), move.y * MathUtils.getRandomNumberInRange(25, 125)),
                    MathUtils.getRandomNumberInRange(5, 20),
                    MathUtils.getRandomNumberInRange(0.8f, 1f),
                    animTime * MathUtils.getRandomNumberInRange(0.5f, 1.5f),
                    new Color(MathUtils.getRandomNumberInRange(90, 180), MathUtils.getRandomNumberInRange(20, 100), 255, 255)
            );

        }
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine.isPaused()) return;

        timePast += amount;

        if (timePast >= animTime) {
            engine.removePlugin(this);
        }
    }

    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {
        super.renderInWorldCoords(viewport);
        float fractionTimePast = timePast / animTime;
        fractionTimePast = MagicAnim.smooth(fractionTimePast);

        if (timePast <= ring1_MaxTime) {
            ring1.setAngle(ring1_Angle + angle - (ring1_RotationSpeed * timePast));
            ring1.setSize(ring1_Size + ring1_grow * fractionTimePast, ring1_Size + ring1_grow * fractionTimePast);
            ring1.setNormalBlend();
            ring1.setAlphaMult(1 - fractionTimePast);
            ring1.renderAtCenter(location.x, location.y);
        }

        if (timePast <= ring2_MaxTime) {
            ring2.setAngle(ring2_Angle + angle + (ring2_RotationSpeed * timePast));
            ring2.setSize(ring2_Size + ring2_grow * fractionTimePast, -(ring2_Size + ring2_grow * fractionTimePast));
            ring1.setNormalBlend();
            ring2.setAlphaMult(1 - fractionTimePast);
            ring2.renderAtCenter(location.x, location.y);
        }
    }
}