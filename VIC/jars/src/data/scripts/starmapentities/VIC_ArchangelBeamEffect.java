package data.scripts.starmapentities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class VIC_ArchangelBeamEffect extends BaseCustomEntityPlugin {

    public static float GLOW_FREQUENCY = 1f; //Speed

    transient private SpriteAPI sprite;
    transient private SpriteAPI glow;
    transient private SpriteAPI original;
    transient private SpriteAPI flare;

    private float phase = 0f;
    private float phase2 = 0f;
    private float FlareSizeRandom = 0f;
    private final float freqMult = 1f;

    public void init(SectorEntityToken entity, Object pluginParams) {
        super.init(entity, pluginParams);
        entity.setDetectionRangeDetailsOverrideMult(0.75f);
        readResolve();
    }

    Object readResolve() {
        sprite = Global.getSettings().getSprite("campaignEntities", "vic_archangel_beam_core");
        glow = Global.getSettings().getSprite("campaignEntities", "vic_archangel_beam_glow2");
        original = Global.getSettings().getSprite("campaignEntities", "vic_archangel_beam");
        flare = Global.getSettings().getSprite("campaignEntities", "vic_archangel_flare2");
        return this;
    }

    public void advance(float amount) {
        phase += amount * GLOW_FREQUENCY * freqMult * 0.3f;
        while (phase > 1f) phase--;
        phase2 += amount * GLOW_FREQUENCY * freqMult * 0.1f;
        while (phase2 > 1f) phase2--;
        FlareSizeRandom = (float) Math.random();
    }

    public float getRenderRange() {
        return entity.getRadius() + 300f;
    }

    public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
        float alphaMult = viewport.getAlphaMult();
        if (alphaMult <= 0f) return;

        CustomEntitySpecAPI spec = entity.getCustomEntitySpec();
        if (spec == null) return;

        float w = spec.getSpriteWidth();
        float h = spec.getSpriteHeight();

        Vector2f loc = entity.getLocation();

        Vector2f facing = Misc.getUnitVectorAtDegreeAngle(entity.getFacing());
        float moveRange;
        float tmp;

        sprite.setSize(w / 1.5f, h);
        sprite.setAlphaMult(alphaMult);
        sprite.setAngle(entity.getFacing() - 90f);

        Color glowColor;
        //Fringe
        glowColor = new Color(0, 247, 255, 206);
        sprite.setSize(w / 1.5f, h);
        sprite.setAlphaMult(alphaMult);
        sprite.setAngle(entity.getFacing() - 90f);
        sprite.setColor(glowColor);
        moveRange = phase * sprite.getHeight();
        sprite.renderRegionAtCenter(loc.x + facing.x * moveRange, loc.y + facing.y * moveRange, 0, 0, 1, 1 - phase);
        //Top part
        if (phase >= 1f) tmp = phase - 2f;
        else tmp = phase - 1f;
        moveRange = tmp * glow.getHeight() + 1.5f;
        sprite.renderRegionAtCenter(loc.x + facing.x * moveRange, loc.y + facing.y * moveRange, 0, 1, 1, -phase);

        //Core
        original.setSize(w / 3f, h);
        original.setAlphaMult(alphaMult);
        original.setAngle(entity.getFacing() - 90f);
        original.renderAtCenter(loc.x, loc.y);

        //Helix
        glowColor = new Color(49, 243, 198, 158);
        glow.setColor(glowColor);
        glow.setAngle(entity.getFacing() - 90f);
        moveRange = phase2 * glow.getHeight();
        glow.renderRegionAtCenter(loc.x + facing.x * moveRange, loc.y + facing.y * moveRange, 0, 0, 1, 1 - phase2);
        //Top part
        if (phase2 >= 1f) tmp = phase2 - 2f;
        else tmp = phase2 - 1f;
        moveRange = tmp * glow.getHeight() + 0.5f;
        glow.renderRegionAtCenter(loc.x + facing.x * moveRange, loc.y + facing.y * moveRange, 0, 1, 1, -phase2);

        //Flare
        flare.setSize(256f * (0.9f + 0.3f * FlareSizeRandom) * 0.50f, 128f * (0.9f + 0.3f * FlareSizeRandom) * 0.75f);
        flare.setAlphaMult(alphaMult);
        flare.setAngle(entity.getFacing() + 90f);
        flare.renderAtCenter(loc.x + facing.x * 213, loc.y + facing.y * 213);


    }
}










