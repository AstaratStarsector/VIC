package data.world.systems;



import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.FuelProduction;
import com.fs.starfarer.api.impl.campaign.econ.impl.HeavyIndustry;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.EventHorizonPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;

import static data.world.VICGen.addMarketplace;


public class Apotheosis {

    public float syncOrbitDays = -80f;

    public void generate(SectorAPI sector) {


        StarSystemAPI system = sector.createStarSystem("Apotheosis");
        system.getLocation().set(1300, -25500);
        system.setBackgroundTextureFilename("graphics/backgrounds/apotheosis_background.jpg");


        // create the star and generate the hyperspace anchor for this system
        PlanetAPI ApotheosisQuasar = system.initStar("vic_quasar_apotheosis", // unique id for this star
                "quasar", // id in planets.json
                400f,		// radius (in pixels at default zoom)
                350f, // corona radius, from star edge
                -10f, // solar wind burn level
                0.0f, // flare probability
                25f); // cr loss mult


        /*
        SectorEntityToken IttirEventHorizon = system.addTerrain(Terrain.EVENT_HORIZON,
                new EventHorizonPlugin.CoronaParams(2000,
                        1000,
                        ApotheosisQuasar,
                        -10f,
                        0f,
                        15f));

         */

        SectorEntityToken IttirEventHorizonOuter = system.addTerrain(Terrain.EVENT_HORIZON,
                new EventHorizonPlugin.CoronaParams(3000,
                        2000,
                        ApotheosisQuasar,
                        -9f,
                        0f,
                        20f));


        SectorEntityToken ApotheosisPulsarBeam = system.addTerrain(Terrain.PULSAR_BEAM,
                new StarCoronaTerrainPlugin.CoronaParams(50000,
                        25000,
                        ApotheosisQuasar,
                        50f,
                        0f,
                        30f));



        system.addRingBand(ApotheosisQuasar, "misc", "rings_dust0", 256f, 4, Color.red, 500f, 600f, 5f);
        system.addRingBand(ApotheosisQuasar, "misc", "rings_asteroids0", 80f, 1, new Color(101, 76, 49), 160f, 600f, 7f);
        system.addRingBand(ApotheosisQuasar, "misc", "rings_asteroids0", 80f, 2, new Color(101, 76, 49), 160f, 700f, 10f);
        system.addRingBand(ApotheosisQuasar, "misc", "rings_asteroids0", 80f, 3, new Color(101, 76, 49), 160f, 800f, 15f);
        system.addRingBand(ApotheosisQuasar, "misc", "rings_dust0", 256f, 1, Color.white, 500f, 800f, 20f);
        system.addRingBand(ApotheosisQuasar, "misc", "rings_dust0", 256f, 2, Color.gray, 500f, 1000f, 26f);
        system.addRingBand(ApotheosisQuasar, "misc", "rings_dust0", 256f, 3, Color.orange, 500f, 1200f, 32f);
        system.addRingBand(ApotheosisQuasar, "misc", "rings_asteroids0", 160f, 3, new Color(101, 76, 49), 160f, 1300f, 40f);
        system.addRingBand(ApotheosisQuasar, "misc", "rings_dust0", 256f, 4, Color.red, 500f, 1400f, 35f);
        system.addRingBand(ApotheosisQuasar, "misc", "rings_dust0", 256f, 1, Color.white, 500f, 1400f, 25f);
        system.addRingBand(ApotheosisQuasar, "misc", "rings_dust0", 300f, 1, Color.red, 500f, 1700f, 30f);
        system.addRingBand(ApotheosisQuasar, "misc", "rings_dust0", 256f, 1, Color.yellow, 500f, 2000f, 43f);
        system.addRingBand(ApotheosisQuasar, "misc", "rings_dust0", 500f, 2, Color.gray, 1000f, 2000f, 50f);
        system.addRingBand(ApotheosisQuasar, "misc", "rings_dust0", 1000f, 4, Color.red, 1000f, 2500f, 80f);
        system.addRingBand(ApotheosisQuasar, "misc", "rings_dust0", 256f, 2, Color.yellow, 256f, 2500f, 150f);
        system.addRingBand(ApotheosisQuasar, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 2800f, 200f);
        system.addRingBand(ApotheosisQuasar, "misc", "rings_dust0", 256f, 1, Color.red, 256f, 3000f, 250f);



        PlanetAPI LostHope = system.addPlanet("vic_planet_LostHope",
                ApotheosisQuasar,
                "Lost Hope",
                "irradiated",
                0,
                250,
                4000,
                syncOrbitDays);


        //Abandoned Station
        SectorEntityToken neutralStation = system.addCustomEntity("vic_ApotheosisAbandonedStation", "Abandoned Station", "station_side05", "neutral");
        neutralStation.setCircularOrbitPointingDown(LostHope, 0, 600, syncOrbitDays);
        neutralStation.getMemory().set("$abandonedStation", true);
        neutralStation.setDiscoverable(true);
        neutralStation.setDiscoveryXP(2000f);
        neutralStation.setSensorProfile(0.3f);
        neutralStation.setCustomDescriptionId("vic_ApotheosisAbandonedStation");
        neutralStation.setInteractionImage("illustrations", "abandoned_station3");
        MarketAPI market = Global.getFactory().createMarket("vic_ApotheosisAbandonedStationMarket","Abandoned Station",0);
                market.setPrimaryEntity(neutralStation);
                market.setFactionId(neutralStation.getFaction().getId());
                market.addIndustry(Industries.SPACEPORT);
                market.addCondition(Conditions.ABANDONED_STATION);
                market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
                ((StoragePlugin) market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
        neutralStation.setMarket(market);
        neutralStation.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addSupplies(MathUtils.getRandomNumberInRange(40, 60));
        neutralStation.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity(Commodities.ORGANS, MathUtils.getRandomNumberInRange(30, 40));
        neutralStation.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addMothballedShip(FleetMemberType.SHIP, "vic_xaphan_skirmisher", Global.getSector().getFaction("vic").pickRandomShipName());
        neutralStation.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().sort();

        //Inner Jump Point
        JumpPointAPI innerJumpPoint = Global.getFactory().createJumpPoint(
                "apotheosis_jump_point",
                "Apotheosis Jump Point");

        innerJumpPoint.setCircularOrbit(ApotheosisQuasar, MathUtils.getRandomNumberInRange(-4,0), 5000, syncOrbitDays);
        innerJumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(innerJumpPoint);

        //Loot at shit
        SectorEntityToken ResearchStation = DerelictThemeGenerator.addSalvageEntity(system, "station_research", Factions.DERELICT);
        ResearchStation.setCircularOrbit(ApotheosisQuasar, 180, 2000, syncOrbitDays);

        addDerelict(system, ApotheosisQuasar, "vic_valafar_assault", ShipRecoverySpecial.ShipCondition.WRECKED, 800 + ((float) Math.random() * 200f), 0,  (Math.random()<0.4));
        addDerelict(system, ApotheosisQuasar, "vic_jezebeth_assault", ShipRecoverySpecial.ShipCondition.WRECKED, 800 + ((float) Math.random() * 200f), 120,  (Math.random()<0.4));
        addDerelict(system, ApotheosisQuasar, "vic_kobal_agony", ShipRecoverySpecial.ShipCondition.WRECKED, 800 + ((float) Math.random() * 200f), 240,  (Math.random()<0.4));
        addDerelict(system, ApotheosisQuasar, "vic_xaphan_skirmisher", ShipRecoverySpecial.ShipCondition.BATTERED, 1200 + ((float) Math.random() * 200f), 0,  (Math.random()<0.4));
        addDerelict(system, ApotheosisQuasar, "vic_moloch_standart", ShipRecoverySpecial.ShipCondition.BATTERED, 1200 + ((float) Math.random() * 200f), 120,  (Math.random()<0.4));
        addDerelict(system, ApotheosisQuasar, "vic_samael_standart", ShipRecoverySpecial.ShipCondition.BATTERED, 1200 + ((float) Math.random() * 200f), 240,  (Math.random()<0.4));


        system.setLightColor(new Color(255, 110, 25, 136)); // light color in entire system, affects all entities


        // generates hyperspace destinations for in-system jump points
        system.autogenerateHyperspaceJumpPoints(true, false);

        //Finally cleans up hyperspace
        cleanup(system);
    }



    //Learning from Tart scripts
    //Clean nearby Nebula
    private void cleanup (StarSystemAPI system){
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);

    }

    protected void addDerelict(StarSystemAPI system,
                               SectorEntityToken focus,
                               String variantId,
                               ShipRecoverySpecial.ShipCondition condition,
                               float orbitRadius,
                               float angle,
                               boolean recoverable) {
        DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId, condition), true);
        SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
        ship.setDiscoverable(true);

        float orbitDays = orbitRadius * MathUtils.getRandomNumberInRange(0.8f, 1.2f) / 50;
        ship.setCircularOrbit(focus, (float) MathUtils.getRandomNumberInRange(-10,10) + angle, orbitRadius, orbitDays);

        WeightedRandomPicker<String> factions= new WeightedRandomPicker<>();
        factions.add("vic");
        if (recoverable) {
            SalvageSpecialAssigner.ShipRecoverySpecialCreator creator = new SalvageSpecialAssigner.ShipRecoverySpecialCreator(null, 0, 0, false, null, factions);
            Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
        }
    }

}