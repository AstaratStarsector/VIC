package data.campaign.intel;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class VIC_declineIntel extends BaseIntelPlugin {

	protected MarketAPI market;
	protected String name;
	protected FactionAPI faction;
	protected float size;
	
	public VIC_declineIntel(MarketAPI market,float size) {
		this.market = market;
		this.faction = market.getFaction();
		this.size = size;
		name = market.getName();
		endAfterDelay(30f);
	}
	
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);
		
		bullet(info);
		boolean isUpdate = getListInfoParam() != null;


		if (mode != ListInfoMode.IN_DESC)
			info.addPara("Colony on " + name + " has declined to size " + Math.round(size), pad, faction.getBaseUIColor(), name);

		if (size == 2){
			info.addPara("Colony will be decivilized soon.", pad);
		}

		unindent(info);
	}
	
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		info.addPara(getName(), c, 0f);
		addBulletPoints(info, mode);
	}
	
	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		
		info.addImage(getFactionForUIColors().getLogo(), width, 128, opad);

		boolean isPlanet = market.getPlanetEntity() != null;
		String noun;
		if (isPlanet){
			noun = "planet";
		} else {
			noun = "station";
		}

		if (isEnding())
		info.addPara("The virus devours " + name + " and its population. The estimation is, that soon there will be no humans left in the colony.",
					opad, faction.getBaseUIColor());
		
		addBulletPoints(info, ListInfoMode.IN_DESC);
	}
	
	@Override
	public String getIcon() {
		//return "graphics/cons/marketsD_gaseous_eruption.png";
		return Global.getSettings().getSpriteName("nex_vicVbombing", "nex_vicVbombingIcon");
	}
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_DECIVILIZED);
		return tags;
	}

	public String getSortString() {
		return "Decline of " + name;
	}
	
	public String getName() {
		return  "Decline of " + name;
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return faction;
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return market.getPrimaryEntity();
	}

	public boolean shouldRemoveIntel() {
		return isEnded();
	}

	@Override
	public String getCommMessageSound() {
		return super.getCommMessageSound();
	}

	@Override
	public Color getTitleColor(ListInfoMode mode) {
		return Global.getSector().getPlayerFaction().getBaseUIColor();
	}

}







