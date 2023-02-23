package data.scripts.starmapentities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import data.scripts.util.MagicAnim;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class VIC_ArchangelPlanetaryEffect extends BaseCustomEntityPlugin {

    public static float GLOW_FREQUENCY = 1f; // on/off cycles per second

    //private SectorEntityToken entity;

    transient private SpriteAPI glow;
    private float phase = 0f;
    private float phase2 = 0f;

    public void init(SectorEntityToken entity, Object pluginParams) {
        super.init(entity, pluginParams);
        entity.setDetectionRangeDetailsOverrideMult(0.75f);
        readResolve();
    }

    Object readResolve() {
        glow = Global.getSettings().getSprite("campaignEntities", "vic_archangel_planetary_glow1");
        return this;
    }

    public void advance(float amount) {
        float freqMult = 1f;
        phase += amount * GLOW_FREQUENCY * freqMult * 0.3f;
        while (phase > 1f) phase--;
        phase2 += amount * GLOW_FREQUENCY * freqMult * 0.1f;
        while (phase2 > 1f) phase2--;
    }

    public float getRenderRange() {
        return entity.getRadius() + 100f;
    }

    public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
        float alphaMult = viewport.getAlphaMult();
        if (alphaMult <= 0f) return;

        CustomEntitySpecAPI spec = entity.getCustomEntitySpec();
        if (spec == null) return;


        Vector2f loc = entity.getLocation();

        Color glowColor = new Color(0, 247, 255, 206);

        float glowAlpha;
        glowAlpha = 0.75f + (0.25f * MagicAnim.RSO(phase, 0f, 1f));

        float w = 290f;
        float h = 290f;

        glow.setSize(w, h);
        glow.setAlphaMult(alphaMult * glowAlpha);
        glow.setAngle(entity.getFacing() + 90f);
        glow.setColor(glowColor);
        glow.setAdditiveBlend();
        glow.renderAtCenter(loc.x, loc.y);
    }
}









