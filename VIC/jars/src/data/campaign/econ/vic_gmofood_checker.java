package data.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.econ.impl.Farming;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

public class vic_gmofood_checker extends Farming {

    @Override
    public boolean isAvailableToBuild() {
        SectorAPI sector = Global.getSector();

        FactionAPI player = sector.getFaction(Factions.PLAYER);
        FactionAPI vic = sector.getFaction("vic");

        boolean canBuild = market.getPlanetEntity() != null &&
                (player.getRelationshipLevel(vic).isAtWorst(RepLevel.WELCOMING) ||
                        Global.getSector().getPlayerFaction().knowsIndustry(getId()));

        return canBuild;
    }

    @Override
    public String getUnavailableReason() {
        return "Advanced GMO Food Industry unavailable.";
    }

    @Override
    public boolean showWhenUnavailable() {
        return false;
    }
}