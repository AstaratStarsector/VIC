package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;

import static data.hullmods.vic_raumWeaponSwapper.WEAPON_SLOT;

public class vic_dummyForRaumSwapper extends vic_dummyForSwaper{

    public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CampaignUIAPI.CoreUITradeMode mode) {
        if (spec == null) return true;

        if (marketOrNull == null) return false;
        if (mode == CampaignUIAPI.CoreUITradeMode.NONE || mode == null) return false;

        boolean knowShip = Global.getSector().getPlayerFaction().knowsShip(ship.getHullSpec().getBaseHullId()) || marketOrNull.getFaction().knowsShip(ship.getHullSpec().getBaseHullId());
        if (!knowShip) return false;




        for (Industry ind : marketOrNull.getIndustries()) {
            if (ind.getSpec().hasTag(Industries.TAG_STATION) || ind.getSpec().hasTag(Industries.TAG_SPACEPORT)) return true;
        }

        return false;
    }

    public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CampaignUIAPI.CoreUITradeMode mode) {
        if (spec == null) return null;

        boolean knowShip = Global.getSector().getPlayerFaction().knowsShip(ship.getHullSpec().getHullId()) || (marketOrNull != null && marketOrNull.getFaction().knowsShip(ship.getHullSpec().getHullId()));
        if (!knowShip) return "Can only be swapped if the player or the market's faction knows blueprint of this ship.";
        return "Can only be swapped at a colony with a spaceport or an orbital station";
    }

}
