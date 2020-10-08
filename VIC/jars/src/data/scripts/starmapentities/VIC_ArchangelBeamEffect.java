package data.scripts.starmapentities;

import java.awt.Color;

import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import data.scripts.util.MagicFakeBeam;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator.RemnantSystemType;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class VIC_ArchangelBeamEffect extends BaseCustomEntityPlugin {

	public static String GLOW_COLOR_KEY = "$core_beaconGlowColor";
	public static String PING_COLOR_KEY = "$core_beaconPingColor";
	
	public static float GLOW_FREQUENCY = 1f; // on/off cycles per second
	
	
	//private SectorEntityToken entity;
	
	transient private SpriteAPI sprite;
	transient private SpriteAPI glow;
	transient private SpriteAPI original;
	
	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		//this.entity = entity;
		entity.setDetectionRangeDetailsOverrideMult(0.75f);
		readResolve();
	}
	
	Object readResolve() {
		sprite = Global.getSettings().getSprite("campaignEntities", "vic_archangel_beam_core");
		glow = Global.getSettings().getSprite("campaignEntities", "vic_archangel_beam_glow2");
		original = Global.getSettings().getSprite("campaignEntities", "vic_archangel_beam");
		return this;
	}
	
	private float phase = 0f;
	private float phase2 = 0f;
	private float freqMult = 1f;
	private float sincePing = 10f;
	public void advance(float amount) {
		phase += amount * GLOW_FREQUENCY * freqMult * 0.3f;
		while (phase > 1f) phase --;
		phase2 += amount * GLOW_FREQUENCY * freqMult * 0.1f;
		while (phase2 > 1f) phase2 --;
		

	}

	public float getRenderRange() {
		return entity.getRadius() + 100f;
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		float alphaMult = viewport.getAlphaMult();
		if (alphaMult <= 0f) return;
		
		CustomEntitySpecAPI spec = entity.getCustomEntitySpec();
		if (spec == null) return;
		
		float w = spec.getSpriteWidth();
		float h = spec.getSpriteHeight();
		
		Vector2f loc = entity.getLocation();

		
		float glowAlpha = 0f;
		if (phase < 0.5f) glowAlpha = phase * 2f;
		if (phase >= 0.5f) glowAlpha = (1f - (phase - 0.5f) * 2f);
		
		float glowAngle1 = (((phase * 1.3f) % 1) - 0.5f);
		float glowAngle2 = (((phase * 1.9f) % 1) - 0.5f);
//		glowAngle1 = 0f;
//		glowAngle2 = 0f;
		
		boolean glowAsLayer = false;
		if (glowAsLayer) {
			//glow.setAngle(entity.getFacing() - 90f);
			Color glowColor = new Color(255, 0, 0,255);
			//Color glowColor = entity.getFaction().getBrightUIColor();

			//glow.setColor(Color.white);
			glow.setColor(glowColor);
			
			glow.setSize(w, h);
			glow.setAlphaMult(alphaMult * glowAlpha);
			glow.setAdditiveBlend();
			
			glow.setAngle(entity.getFacing() - 90f + glowAngle1);
			glow.renderAtCenter(loc.x, loc.y);
			
			glow.setAngle(entity.getFacing() - 90f + glowAngle2);
			glow.setAlphaMult(alphaMult * glowAlpha * 0.5f);
			glow.renderAtCenter(loc.x, loc.y);

		} else {
			//glow.setAngle(entity.getFacing() - 90f);
			Color glowColor = new Color(255, 0, 0,255);
			//Color glowColor = entity.getFaction().getBrightUIColor();

			//glow.setColor(Color.white);
			glow.setColor(glowColor);

			glow.setSize(w, h);
			glow.setAlphaMult(alphaMult);

			glow.setAngle(entity.getFacing() - 90f);
			//glow.renderAtCenter(loc.x, loc.y);

			//glow.renderRegionAtCenter(loc.x, loc.y, 0,glowAlpha, 1,1 - glowAlpha);

            Vector2f facing = Misc.getUnitVectorAtDegreeAngle(entity.getFacing());
            float moveRange;
			float tmp = 0;

			sprite.setSize(w, h);
			sprite.setAlphaMult(alphaMult);

			sprite.setAngle(entity.getFacing() - 90f);


			glowColor = new Color(0, 247, 255, 206);
			sprite.setColor(glowColor);
			moveRange = phase * sprite.getHeight();
			sprite.renderRegionAtCenter(loc.x + facing.x * moveRange, loc.y + facing.y * moveRange, 0,0, 1,1 - phase);


			if (phase >=  1f) tmp = phase - 2f;
			else tmp = phase - 1f;
			moveRange = tmp * glow.getHeight()  + 1.5f;
			sprite.renderRegionAtCenter(loc.x + facing.x * moveRange, loc.y + facing.y * moveRange, 0,1, 1, -phase);

			original.setSize(w/1.5f, h);
			original.setAlphaMult(alphaMult);
			

			original.setAngle(entity.getFacing() - 90f);
			original.renderAtCenter(loc.x, loc.y);


			glowColor = new Color(49, 243, 198, 158);
			glow.setColor(glowColor);
			moveRange = phase2 * glow.getHeight();
			glow.renderRegionAtCenter(loc.x + facing.x * moveRange, loc.y + facing.y * moveRange, 0,0, 1,1 - phase2);


            if (phase2 >=  1f) tmp = phase2 - 2f;
            else tmp = phase2 - 1f;
            moveRange = tmp * glow.getHeight() + 1.5f;
            glow.renderRegionAtCenter(loc.x + facing.x * moveRange, loc.y + facing.y * moveRange, 0,1, 1, -phase2);




		}
	}

}









