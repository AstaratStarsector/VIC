package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;


public class ArchangelBeamPlugin extends BaseCustomEntityPlugin {

    public static String GLOW_COLOR_KEY = "$core_beaconGlowColor";
    public static String PING_COLOR_KEY = "$core_beaconPingColor";
    public static float GLOW_FREQUENCY = 1.0F;
    private transient SpriteAPI sprite;
    private transient SpriteAPI glow;
    private float phase = 0.0F;
    private float freqMult = 1.0F;
    private float sincePing = 10.0F;

    public ArchangelBeamPlugin() {
    }

    public void init(SectorEntityToken entity, Object pluginParams) {
        super.init(entity, pluginParams);
        entity.setDetectionRangeDetailsOverrideMult(0.75F);
        this.readResolve();
    }

    Object readResolve() {
        this.sprite = Global.getSettings().getSprite("campaignEntities", "warning_beacon");
        this.glow = Global.getSettings().getSprite("campaignEntities", "warning_beacon_glow");
        return this;
    }

    public void advance(float amount) {
        for (this.phase += amount * GLOW_FREQUENCY * this.freqMult; this.phase > 1.0F; --this.phase) {
        }
    }

    public float getRenderRange() {
        return this.entity.getRadius() + 100.0F;
    }

    public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
        float alphaMult = viewport.getAlphaMult();
        if (alphaMult > 0.0F) {
            CustomEntitySpecAPI spec = this.entity.getCustomEntitySpec();
            if (spec != null) {
                float w = spec.getSpriteWidth();
                float h = spec.getSpriteHeight();
                Vector2f loc = this.entity.getLocation();
                this.sprite.setAngle(this.entity.getFacing() - 90.0F);
                this.sprite.setSize(w, h);
                this.sprite.setAlphaMult(alphaMult);
                this.sprite.setNormalBlend();
                this.sprite.renderAtCenter(loc.x, loc.y);
                float glowAlpha = 0.0F;
                if (this.phase < 0.5F) {
                    glowAlpha = this.phase * 2.0F;
                }

                if (this.phase >= 0.5F) {
                    glowAlpha = 1.0F - (this.phase - 0.5F) * 2.0F;
                }

                float glowAngle1 = (this.phase * 1.3F % 1.0F - 0.5F) * 12.0F;
                float glowAngle2 = (this.phase * 1.9F % 1.0F - 0.5F) * 12.0F;
                boolean glowAsLayer = true;
                if (glowAsLayer) {
                    Color glowColor = new Color(255, 200, 0, 255);
                    if (this.entity.getMemoryWithoutUpdate().contains(GLOW_COLOR_KEY)) {
                        glowColor = (Color) this.entity.getMemoryWithoutUpdate().get(GLOW_COLOR_KEY);
                    }

                    this.glow.setColor(glowColor);
                    this.glow.setSize(w, h);
                    this.glow.setAlphaMult(alphaMult * glowAlpha);
                    this.glow.setAdditiveBlend();
                    this.glow.setAngle(this.entity.getFacing() - 90.0F + glowAngle1);
                    this.glow.renderAtCenter(loc.x, loc.y);
                    this.glow.setAngle(this.entity.getFacing() - 90.0F + glowAngle2);
                    this.glow.setAlphaMult(alphaMult * glowAlpha * 0.5F);
                    this.glow.renderAtCenter(loc.x, loc.y);
                } else {
                    this.glow.setAngle(this.entity.getFacing() - 90.0F);
                    this.glow.setColor(new Color(255, 165, 100));
                    float gs = w * 3.0F;
                    this.glow.setSize(gs, gs);
                    this.glow.setAdditiveBlend();
                    float spacing = 10.0F;
                    this.glow.setAlphaMult(alphaMult * glowAlpha * 0.5F);
                    this.glow.renderAtCenter(loc.x - spacing, loc.y);
                    this.glow.renderAtCenter(loc.x + spacing, loc.y);
                    this.glow.setAlphaMult(alphaMult * glowAlpha);
                    this.glow.setSize(gs * 0.25F, gs * 0.25F);
                    this.glow.renderAtCenter(loc.x - spacing, loc.y);
                    this.glow.renderAtCenter(loc.x + spacing, loc.y);
                }

            }
        }
    }

    public void createMapTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        String post = "";
        Color color = this.entity.getFaction().getBaseUIColor();
        Color postColor = color;
        if (this.entity.getMemoryWithoutUpdate().getBoolean(RemnantThemeGenerator.RemnantSystemType.DESTROYED.getBeaconFlag())) {
            post = " - Low";
            postColor = Misc.getPositiveHighlightColor();
        } else if (this.entity.getMemoryWithoutUpdate().getBoolean(RemnantThemeGenerator.RemnantSystemType.SUPPRESSED.getBeaconFlag())) {
            post = " - Medium";
            postColor = Misc.getHighlightColor();
        } else if (this.entity.getMemoryWithoutUpdate().getBoolean(RemnantThemeGenerator.RemnantSystemType.RESURGENT.getBeaconFlag())) {
            post = " - High";
            postColor = Misc.getNegativeHighlightColor();
        }

        tooltip.addPara(this.entity.getName() + post, 0.0F, color, postColor, new String[]{post.replaceFirst(" - ", "")});
    }

    public boolean hasCustomMapTooltip() {
        return true;
    }

    public void appendToCampaignTooltip(TooltipMakerAPI tooltip, SectorEntityToken.VisibilityLevel level) {
        if (level == SectorEntityToken.VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS || level == SectorEntityToken.VisibilityLevel.COMPOSITION_DETAILS) {
            String post = "";
            Color color = Misc.getTextColor();
            Color postColor = color;
            if (this.entity.getMemoryWithoutUpdate().getBoolean(RemnantThemeGenerator.RemnantSystemType.DESTROYED.getBeaconFlag())) {
                post = "low";
                postColor = Misc.getPositiveHighlightColor();
            } else if (this.entity.getMemoryWithoutUpdate().getBoolean(RemnantThemeGenerator.RemnantSystemType.SUPPRESSED.getBeaconFlag())) {
                post = "medium";
                postColor = Misc.getHighlightColor();
            } else if (this.entity.getMemoryWithoutUpdate().getBoolean(RemnantThemeGenerator.RemnantSystemType.RESURGENT.getBeaconFlag())) {
                post = "high";
                postColor = Misc.getNegativeHighlightColor();
            }

            if (!post.isEmpty()) {
                tooltip.setParaFontDefault();
                tooltip.addPara("    - Danger level: " + post, 10.0F, color, postColor, new String[]{post});
            }
        }

    }
}
