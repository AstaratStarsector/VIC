package  data.world.systems;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.ids.vic_Items;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static data.world.VICGen.addMarketplace;


public class Ittir{

    public void generate(SectorAPI sector) {


        StarSystemAPI system = sector.createStarSystem("Ittir");
        system.getLocation().set(-2700, -22000);
        system.setBackgroundTextureFilename("graphics/backgrounds/ittir_background.jpg");


        // create the star and generate the hyperspace anchor for this system
        PlanetAPI IttirStar = system.initStar("vic_star_ittir", // unique id for this star
                "star_white", // id in planets.json
                650f,        // radius (in pixels at default zoom)
                600, // corona radius, from star edge
                5f, // solar wind burn level
                1f, // flare probability
                2f); // cr loss mult


        // Kalada planet - VIC latest acquisition
        PlanetAPI Kalada = system.addPlanet("vic_planet_kalada",
                IttirStar,
                "Kalada",
                "desert",
                0,
                150f,
                2500f,
                80f);
        Kalada.setCustomDescriptionId("vic_kalada"); //reference descriptions.csv
        Kalada.setInteractionImage("illustrations", "vic_kalada_illustration");

        MarketAPI Kalada_market = addMarketplace(
                "vic",
                Kalada,
                null,
                "Kalada",
                5,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_6,
                                Conditions.HOT,
                                Conditions.INIMICAL_BIOSPHERE,
                                Conditions.HABITABLE,
                                Conditions.FARMLAND_POOR,
                                Conditions.ORE_RICH,
                                Conditions.ORGANICS_COMMON
                        )
                ),

                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE,
                                Submarkets.SUBMARKET_BLACK
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.SPACEPORT,
                                Industries.MINING,
                                Industries.GROUNDDEFENSES,
                                Industries.WAYSTATION,
                                Industries.STARFORTRESS_MID,
                                Industries.MILITARYBASE,
                                Industries.FARMING
                        )
                ),
                0.3f, //tariffs
                true, //freeport
                true //junk and chatter
        );

        Kalada_market.getIndustry(Industries.FARMING).setSpecialItem(new SpecialItemData(vic_Items.GMOfarm, null));
        Kalada_market.setImmigrationIncentivesOn(true);


        system.addAsteroidBelt(IttirStar, 50, 3000, 400, 300, 400, Terrain.ASTEROID_BELT, "Inner Band");
        system.addRingBand(IttirStar, "misc", "rings_asteroids0", 256f, 3, Color.gray, 256f, 3000, 250f);


        // Mithos planet - the Church awakens
        PlanetAPI Mithos = system.addPlanet("vic_planet_mithos",
                IttirStar,
                "Mithos",
                "terran-eccentric",
                180,
                170f,
                3500f,
                120f);
        Mithos.setCustomDescriptionId("vic_mithos"); //reference descriptions.csv


        MarketAPI Mithos_market = addMarketplace(
                "luddic_church",
                Mithos,
                null,
                "Mithos",
                6,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_6,
                                Conditions.HABITABLE,
                                Conditions.TECTONIC_ACTIVITY,
                                Conditions.FARMLAND_RICH,
                                Conditions.RUINS_EXTENSIVE,
                                Conditions.ORGANICS_PLENTIFUL,
                                Conditions.ORE_SPARSE
                        )
                ),

                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE,
                                Submarkets.SUBMARKET_BLACK
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.MEGAPORT,
                                Industries.MINING,
                                Industries.HEAVYBATTERIES,
                                Industries.BATTLESTATION,
                                Industries.FARMING,
                                Industries.WAYSTATION,
                                Industries.MILITARYBASE,
                                Industries.HEAVYINDUSTRY
                        )
                ),
                //tariffs
                0.3f,
                //freeport
                false,
                //junk and chatter
                true);


        //Mithos Jump Point
        JumpPointAPI MithosJumpPoint = Global.getFactory().createJumpPoint(

                "mithos_jump_point",

                "Mithos Jump Point");

        MithosJumpPoint.setCircularOrbit(system.getEntityById("vic_star_ittir"), 220, 4000, 100f);
        MithosJumpPoint.setRelatedPlanet(Mithos);
        system.addEntity(MithosJumpPoint);


        //  Bernadian planet - Where Hegemony made their mistake
        PlanetAPI Bernadian = system.addPlanet("vic_planet_bernadian",
                IttirStar,
                "Bernadian",
                "tundra",
                360 * (float) Math.random(),
                140f,
                6000f,
                100f);
        Bernadian.setCustomDescriptionId("vic_bernadian"); //reference descriptions.csv


        MarketAPI Bernadian_market = addMarketplace(
                "hegemony",
                Bernadian,
                null,
                "Bernadian",
                4,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_4,
                                Conditions.EXTREME_WEATHER,
                                Conditions.DENSE_ATMOSPHERE,
                                Conditions.ORE_RICH,
                                Conditions.RARE_ORE_MODERATE,
                                Conditions.VOLATILES_DIFFUSE


                        )
                ),

                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE,
                                Submarkets.SUBMARKET_BLACK
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.SPACEPORT,
                                Industries.MINING,
                                Industries.GROUNDDEFENSES,
                                Industries.WAYSTATION,
                                Industries.PATROLHQ,
                                Industries.REFINING

                        )
                ),
                //tariffs
                0.3f,
                //freeport
                false,
                //junk and chatter
                true);


        //  Maria planet - Independents are scared
        PlanetAPI Maria = system.addPlanet("vic_planet_maria",
                IttirStar,
                "Maria",
                "frozen",
                360 * (float) Math.random(),
                220f,
                8000f,
                170f);
        Maria.setCustomDescriptionId("vic_maria"); //reference descriptions.csv


        MarketAPI Maria_market = addMarketplace(
                "independent",
                Maria,
                null,
                "Maria",
                5,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_5,
                                Conditions.COLD,
                                Conditions.HIGH_GRAVITY,
                                Conditions.POLLUTION,
                                Conditions.VOLATILES_ABUNDANT,
                                Conditions.ORE_MODERATE,
                                Conditions.RARE_ORE_MODERATE


                        )
                ),

                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE,
                                Submarkets.SUBMARKET_BLACK
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.MEGAPORT,
                                Industries.MINING,
                                Industries.GROUNDDEFENSES,
                                Industries.WAYSTATION,
                                Industries.PATROLHQ,
                                Industries.HEAVYINDUSTRY,
                                Industries.ORBITALSTATION_MID,
                                Industries.MILITARYBASE

                        )
                ),
                //tariffs
                0.3f,
                //freeport
                true,
                //junk and chatter
                true);


        //  Pierre planet - Maria's dearest
        PlanetAPI Pierre = system.addPlanet("vic_planet_pierre",
                Maria,
                "Pierre",
                "barren-bombarded",
                30,
                100f,
                1000f,
                30f);

        Pierre.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
        Pierre.getMarket().addCondition(Conditions.COLD);
        Pierre.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);
        Pierre.getMarket().addCondition(Conditions.ORE_SPARSE);
        Pierre.getMarket().addCondition(Conditions.METEOR_IMPACTS);
        Pierre.getMarket().addCondition(Conditions.VOLATILES_DIFFUSE);


        //  Becquerel planet - Maria's dearest
        PlanetAPI Becquerel = system.addPlanet("vic_planet_becquerel",
                Maria,
                "Becquerel",
                "rocky_ice",
                230,
                60f,
                1500f,
                60f);

        Becquerel.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
        Becquerel.getMarket().addCondition(Conditions.VERY_COLD);
        Becquerel.getMarket().addCondition(Conditions.RARE_ORE_ABUNDANT);
        Becquerel.getMarket().addCondition(Conditions.ORE_SPARSE);
        Becquerel.getMarket().addCondition(Conditions.LOW_GRAVITY);


        // add Sensor Array
        SectorEntityToken MithosSensorArray = system.addCustomEntity("Mithos_sensor_array", "Mithos Sensor Array", "sensor_array_makeshift", "luddic_church");
        MithosSensorArray.setCircularOrbitPointingDown(IttirStar, 360f * (float) Math.random(), 3800, 100);


        // add Comm Relay
        SectorEntityToken MariaCommRelay = system.addCustomEntity("Maria_comm_relay", "Maria Comm Relay", "comm_relay", "hegemony");
        MariaCommRelay.setCircularOrbitPointingDown(Maria, 30f, 1200, 60);


        //add Nav Buoy
        SectorEntityToken KaladaBuoy = system.addCustomEntity("kalada_nav_buoy", // unique id
                "Kalada Nav Buoy", // name - if null, defaultName from custom_entities.json will be used
                "nav_buoy_makeshift", // type of object, defined in custom_entities.json
                "vic"); // faction
        KaladaBuoy.setCircularOrbitPointingDown(IttirStar, 360f * (float) Math.random(), 2000, 35);


        //Ittir Inactive Gate
        SectorEntityToken EmpyreanGate = system.addCustomEntity("ittir_gate", "Ittir Gate", "inactive_gate", null); //add the thing orbiting the market
        EmpyreanGate.setCircularOrbitPointingDown(IttirStar, 360f * (float) Math.random(), 5000f, 145f); //set as circular orbit


        /*
        // Let's procgen some stuff here cause fuck doing that manually
        float ProcgenOne = StarSystemGenerator.addOrbitingEntities(system, IttirStar, StarAge.AVERAGE,
                4, 5, // min/max entities to add
                8000, // radius to start adding at
                3, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true); // whether to use custom or system-name based names


        float ProcgenTwo = StarSystemGenerator.addOrbitingEntities(system, IttirStar, StarAge.OLD,
                2, 3, // min/max entities to add
                10000, // radius to start adding at
                3, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true); // whether to use custom or system-name based names

         */


        // generates hyperspace destinations for in-system jump points
        system.autogenerateHyperspaceJumpPoints(true, true);

        //Finally cleans up hyperspace
        cleanup(system);


    }


    //Learning from Tart scripts
    //Clean nearby Nebula
    private void cleanup(StarSystemAPI system) {
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);

    }

}