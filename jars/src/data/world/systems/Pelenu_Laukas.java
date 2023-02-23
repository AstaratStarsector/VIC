package data.world.systems;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static data.world.VICGen.addMarketplace;


public class Pelenu_Laukas {

    public void generate(SectorAPI sector) {


        StarSystemAPI system = sector.createStarSystem("Pelenu Laukas");
        system.getLocation().set(-12500, -14500);
        system.setBackgroundTextureFilename("graphics/backgrounds/pelenulaukas_background.jpg");


        // create the star and generate the hyperspace anchor for this system
        PlanetAPI PelenuLaukasStar = system.initStar("vic_star_pelenulaukas", // unique id for this star
                "star_blue_giant", // id in planets.json
                1150f,        // radius (in pixels at default zoom)
                800, // corona radius, from star edge
                15f, // solar wind burn level
                3f, // flare probability
                5f); // cr loss mult

        system.setLightColor(new Color(245, 225, 255)); // light color in entire system, affects all entities


        PlanetAPI KeliautojasStar = system.addPlanet("vic_planet_keliautojas",
                PelenuLaukasStar,
                "Keliautojas",
                "star_red_dwarf",
                45f,
                400f,
                4000f,
                100f);

        system.setSecondary(KeliautojasStar);
        //val4.setCustomDescriptionId("star_red_dwarf");
        system.addCorona(KeliautojasStar, 200, 2f, 0.5f, 2f);


        // Lemtis planet - Bad position
        PlanetAPI Lemtis = system.addPlanet("vic_planet_lemtis",
                PelenuLaukasStar,
                "Lemtis",
                "rocky_metallic",
                45f,
                120f,
                2375f,
                100f);
        Lemtis.setCustomDescriptionId("vic_lemtis"); //reference descriptions.csv


        SectorEntityToken field = system.addTerrain(Terrain.MAGNETIC_FIELD,
                new MagneticFieldParams(400f, // terrain effect band width
                        270, // terrain effect middle radius
                        Lemtis, // entity that it's around
                        200f, // visual band start
                        470f, // visual band end
                        new Color(50, 20, 100, 40), // base color
                        1f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                        new Color(50, 20, 110, 130),
                        new Color(150, 30, 120, 150),
                        new Color(200, 50, 130, 190),
                        new Color(250, 70, 150, 240),
                        new Color(200, 80, 130, 255),
                        new Color(75, 0, 160),
                        new Color(127, 0, 255)
                ));
        field.setCircularOrbit(Lemtis, 0, 0, 150);


        MarketAPI Lemtis_market = addMarketplace(
                "luddic_path",
                Lemtis,
                null,
                "Lemtis",
                4,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_4,
                                Conditions.VERY_HOT,
                                Conditions.NO_ATMOSPHERE,
                                Conditions.ORE_RICH,
                                Conditions.RARE_ORE_RICH,
                                Conditions.IRRADIATED
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
                                Industries.REFINING,
                                Industries.PATROLHQ
                        )
                ),
                //tariffs
                0.3f,
                //freeport
                false,
                //junk and chatter
                false);


        system.addRingBand(PelenuLaukasStar, "misc", "rings_dust0", 256f, 3, Color.orange, 500f, 4700f, 70f);
        system.addRingBand(PelenuLaukasStar, "misc", "rings_asteroids0", 256f, 3, new Color(101, 76, 49), 160f, 4700f, 90f);


        //Inner Jump Point
        JumpPointAPI innerJumpPoint = Global.getFactory().createJumpPoint("inner_jump_point", "Pelenu Laukas Jump Point");

        innerJumpPoint.setCircularOrbit(system.getEntityById("vic_star_pelenulaukas"), 225, 2500, 100);
        system.addEntity(innerJumpPoint);


        // Pyktis planet - Angery military base
        PlanetAPI Pyktis = system.addPlanet("vic_planet_pyktis",
                PelenuLaukasStar,
                "Pyktis",
                "desert1",
                360 * (float) Math.random(),
                150f,
                5200f,
                100f);
        Pyktis.setCustomDescriptionId("vic_pyktis"); //reference descriptions.csv


        MarketAPI Pyktis_market = addMarketplace(
                "tritachyon",
                Pyktis,
                null,
                "Pyktis",
                5,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_5,
                                Conditions.HOT,
                                Conditions.ORE_SPARSE,
                                Conditions.RARE_ORE_MODERATE,
                                Conditions.VOLATILES_DIFFUSE,
                                Conditions.HABITABLE
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
                                Industries.HEAVYINDUSTRY,
                                Industries.MILITARYBASE,
                                Industries.BATTLESTATION
                        )
                ),
                //tariffs
                0.3f,
                //freeport
                false,
                //junk and chatter
                false);


        // Ramus planet - calm farming borderland planet
        PlanetAPI Ramus = system.addPlanet("vic_planet_ramus",
                PelenuLaukasStar,
                "Ramus",
                "arid",
                360f,
                200f,
                7400f,
                140f);
        Ramus.setCustomDescriptionId("vic_ramus"); //reference descriptions.csv


        MarketAPI Ramus_market = addMarketplace(
                "persean",
                Ramus,
                null,
                "Ramus",
                5,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_5,
                                Conditions.HOT,
                                Conditions.ORE_SPARSE,
                                Conditions.ORGANICS_PLENTIFUL,
                                Conditions.FARMLAND_ADEQUATE,
                                Conditions.HABITABLE
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
                                Industries.FARMING,
                                Industries.LIGHTINDUSTRY,
                                Industries.ORBITALSTATION
                        )
                ),
                //tariffs
                0.3f,
                //freeport
                false,
                //junk and chatter
                false);


        // Siaubas planet - The Atrocity
        PlanetAPI Siaubas = system.addPlanet("vic_planet_siaubas",
                PelenuLaukasStar,
                "Siaubas",
                "arid",
                180f,
                165f,
                8000f,
                140f);
        Siaubas.setCustomDescriptionId("vic_siaubas"); //reference descriptions.csv


        Siaubas.getMarket().addCondition(Conditions.HOT);
        Siaubas.getMarket().addCondition(Conditions.INIMICAL_BIOSPHERE);
        Siaubas.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);
        Siaubas.getMarket().addCondition(Conditions.ORE_SPARSE);
        Siaubas.getMarket().addCondition(Conditions.ORGANICS_TRACE);
        Siaubas.getMarket().addCondition(Conditions.FARMLAND_POOR);
        Siaubas.getMarket().addCondition(Conditions.RUINS_VAST);

        CustomCampaignEntityAPI beacon = system.addCustomEntity(null, null, Entities.WARNING_BEACON, Factions.NEUTRAL);
        beacon.setCircularOrbitPointingDown(PelenuLaukasStar, 180f, 7800f, 140f);
        beacon.getMemoryWithoutUpdate().set("$pelenulaukas", true);


        system.addRingBand(PelenuLaukasStar, "misc", "rings_dust0", 300f, 3, Color.white, 300f, 10200f, 350f);
        system.addAsteroidBelt(PelenuLaukasStar, 75, 10400, 800, 250, 400, Terrain.ASTEROID_BELT, "Outer Asteroid Belt");
        system.addRingBand(PelenuLaukasStar, "misc", "rings_asteroids0", 256f, 3, Color.black, 160f, 10500f, 325f);
        system.addRingBand(PelenuLaukasStar, "misc", "rings_dust0", 300f, 2, Color.gray, 300f, 10400f, 300);


        //Boe's Refuge - Pirates' party place
        SectorEntityToken BoeRefuge = system.addCustomEntity("vic_planet_boe_refuge", "Boe's Refuge", "station_side06", "pirates");
        BoeRefuge.setCircularOrbitPointingDown(PelenuLaukasStar, 360 * (float) Math.random(), 10800f, 200f);
        BoeRefuge.setCustomDescriptionId("vic_boerefuge");

        MarketAPI BoeRefuge_market = addMarketplace(
                "pirates",
                BoeRefuge,
                null,
                "Boe's Refuge",
                4,


                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_4,
                                Conditions.NO_ATMOSPHERE,
                                Conditions.OUTPOST
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
                                Industries.HEAVYBATTERIES,
                                Industries.HEAVYINDUSTRY,
                                Industries.BATTLESTATION,
                                Industries.REFINING
                        )
                ),

                0.18f,
                true,
                false);


        // add Comm Relay
        SectorEntityToken OuterCommRelay = system.addCustomEntity("outer_comm_relay", "Outer Comm Relay", "comm_relay_makeshift", "luddic_path");
        OuterCommRelay.setCircularOrbitPointingDown(PelenuLaukasStar, 360f * (float) Math.random(), 9000f, 200);


        // add Sensor Array
        SectorEntityToken RamusSensorArray = system.addCustomEntity("Ramus_sensor_array", "Ramus Sensor Array", "sensor_array_makeshift", "luddic_path");
        RamusSensorArray.setCircularOrbitPointingDown(Ramus, 360f * (float) Math.random(), 400, 20f);


        //add Nav Buoy
        SectorEntityToken CentralNavBuoy = system.addCustomEntity("central_nav_buoy", // unique id
                "Central Nav Buoy", // name - if null, defaultName from custom_entities.json will be used
                "nav_buoy_makeshift", // type of object, defined in custom_entities.json
                "luddic_path"); // faction
        CentralNavBuoy.setCircularOrbitPointingDown(PelenuLaukasStar, 360f * (float) Math.random(), 6000, 170);


        // Let's procgen some stuff here cause fuck doing that manually
        float ProcgenOne = StarSystemGenerator.addOrbitingEntities(system, PelenuLaukasStar, StarAge.AVERAGE,
                4, 5, // min/max entities to add
                9000, // radius to start adding at
                3, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true); // whether to use custom or system-name based names


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