package data.campaign.econ;

import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.Farming;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.ids.vic_Items;


public class vic_gmofood extends Farming {

    private int getSupplyForCondition() {
        if (market.hasCondition(Conditions.FARMLAND_POOR)) return 1;
        if (market.hasCondition(Conditions.FARMLAND_ADEQUATE)) return 2;
        if (market.hasCondition(Conditions.FARMLAND_RICH)) return 3;
        if (market.hasCondition(Conditions.FARMLAND_BOUNTIFUL)) return 4;

        return 0;
    }

    private int getSupplyForConditionWater() {
        if (market.hasCondition(Conditions.WATER_SURFACE)) return 1;

        return 0;
    }

    public void apply() {
        super.apply(true);
        int size = this.market.getSize();
        demand(Commodities.ORGANICS, size - 2);
        demand(vic_Items.GENETECH, (int) ((size - 1) * 0.5f));
        if (getSupplyForConditionWater() == 1) {
            demand(Commodities.HEAVY_MACHINERY, size);
            supply(Commodities.FOOD, size + getSupplyForConditionWater());
        }
        if (getSupplyForCondition() > 0) {
            supply(Commodities.FOOD, size + getSupplyForCondition());
        } else {
            supply(Commodities.FOOD, 0);
        }

        String desc = this.getNameForModifier();
        Pair<String, Integer> deficit = this.getMaxDeficit("organics", "vic_genetech");
        int maxDeficit = size + 4;
        if (deficit.two > maxDeficit) {
            deficit.two = maxDeficit;
        }
        applyDeficitToProduction(1, deficit, Commodities.FOOD);

    }


    public void unapply() {
        super.unapply();
        MemoryAPI memory = this.market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason(memory, "$population", this.getModId(), false, -1.0F);
    }


    protected void addPostDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        if (this.market.isPlayerOwned()) {
            float opad = 10.0F;
        }
    }

    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
        return mode != IndustryTooltipMode.NORMAL || this.isFunctional();
    }


    public String getRouteSourceId() {
        return this.getMarket().getId() + "_" + "vicgmofarms";
    }

    /*
    public boolean isAvailableToBuild() {
        SectorAPI sector = Global.getSector();

        FactionAPI player = sector.getFaction(Factions.PLAYER);
        FactionAPI vic = sector.getFaction("vic");

        return market.getPlanetEntity() != null &&
                (player.getRelationshipLevel(vic).isAtWorst(RepLevel.WELCOMING) ||
                        Global.getSector().getPlayerFaction().knowsIndustry(getId()));
    }

    public boolean showWhenUnavailable() {
        return true;
    }

     */

    public boolean isAvailableToBuild() {
        return false;
    }

    public boolean showWhenUnavailable() {
        return false;
    }

}


