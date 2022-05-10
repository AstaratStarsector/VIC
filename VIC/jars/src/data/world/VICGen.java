package data.world;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;
import data.world.systems.Apotheosis;
import data.world.systems.Empyrean;
import data.world.systems.Ittir;
import data.world.systems.Pelenu_Laukas;

import java.util.ArrayList;


public class VICGen implements SectorGeneratorPlugin {


    //Shorthand function for adding a market
    public static MarketAPI addMarketplace(String factionID, SectorEntityToken primaryEntity, ArrayList<SectorEntityToken> connectedEntities, String name,
                                           int size, ArrayList<String> marketConditions, ArrayList<String> submarkets, ArrayList<String> industries, float tarrif,
                                           boolean freePort, boolean withJunkAndChatter) {
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        String planetID = primaryEntity.getId();
        String marketID = planetID + "_market";

        MarketAPI newMarket = Global.getFactory().createMarket(marketID, name, size);


        newMarket.setFactionId(factionID);

        newMarket.getTariff().modifyFlat("generator", tarrif);

        //Adds submarkets
        if (null != submarkets) {
            for (String market : submarkets) {
                newMarket.addSubmarket(market);
            }
        }

        //Adds market conditions
        for (String condition : marketConditions) {
            newMarket.addCondition(condition);
        }

        //Add market industries
        for (String industry : industries) {
            newMarket.addIndustry(industry);
        }

        //Sets us to a free port, if we should
        newMarket.setFreePort(freePort);

        //Adds our connected entities, if any
        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                newMarket.getConnectedEntities().add(entity);
            }
        }

        newMarket.setPrimaryEntity(primaryEntity);
        globalEconomy.addMarket(newMarket, withJunkAndChatter);
        primaryEntity.setMarket(newMarket);
        primaryEntity.setFaction(factionID);

        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                entity.setMarket(newMarket);
                entity.setFaction(factionID);
            }
        }


        for (MarketConditionAPI mc : newMarket.getConditions())
        {
            mc.setSurveyed(true);
        }
        newMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);


        //Finally, return the newly-generated market
        return newMarket;
    }

    @Override
    public void generate(SectorAPI sector) {

        FactionAPI vic = sector.getFaction("vic");
        //Generate your system
        new Empyrean().generate(sector);
        new Apotheosis().generate(sector);
        new Ittir().generate(sector);
        new Pelenu_Laukas().generate(sector);

        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("vic");

        //set relationship
        vic.setRelationship(Factions.LUDDIC_CHURCH, -1f);
        vic.setRelationship(Factions.LUDDIC_PATH, -1f);
        vic.setRelationship(Factions.TRITACHYON, -0.7f);
        vic.setRelationship(Factions.PERSEAN, 0.3f);
        vic.setRelationship(Factions.PIRATES, 0f);
        vic.setRelationship(Factions.INDEPENDENT, -0.2f);
        vic.setRelationship(Factions.DIKTAT, -0.1f);
        vic.setRelationship(Factions.LIONS_GUARD, -0.1f);
        vic.setRelationship(Factions.HEGEMONY, 0.6f);
        vic.setRelationship(Factions.REMNANTS, -1f);
    }
}

