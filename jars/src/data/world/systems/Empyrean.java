package data.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.ids.vic_Items;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static data.scripts.plugins.vic_addBooze.addBooze;
import static data.scripts.plugins.vic_addBooze.addBrewery;
import static data.world.VICGen.addMarketplace;


public class Empyrean{

    public void generate(SectorAPI sector) {

        StarSystemAPI system = sector.createStarSystem("Empyrean");
        system.getLocation().set(-2400, -19000);
        system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "music_vic_zenith's_call");
        system.setBackgroundTextureFilename("graphics/backgrounds/empyrean_background_blue.jpg");

        // create the star and generate the hyperspace anchor for this system
        PlanetAPI EmpyreanStar = system.initStar("vic_star_empyrean", // unique id for this star
                "star_yellow_supergiant", // id in planets.json
                1300f,        // radius (in pixels at default zoom)
                1000, // corona radius, from star edge
                10f, // solar wind burn level
                0.5f, // flare probability
                5f); // cr loss mult

        system.setLightColor(new Color(255, 199, 98)); // light color in entire system, affects all entities

        //adds Nebulas into the system
        SectorEntityToken empyrean_nebula = Misc.addNebulaFromPNG("data/campaign/terrain/empyrean_nebula.png",
                0, 0, // center of nebula
                system, // location to add to
                "terrain", "nebula", //"nebula_blue", // texture to use, uses xxx_map for map
                4, 4, StarAge.AVERAGE); // number of cells in texture


        // Phlegethon: "Happy Labour" Summer Camp
        PlanetAPI Phlegethon = system.addPlanet("vic_planet_phlegethon",
                EmpyreanStar,
                "Phlegethon",
                "lava_minor",
                50f,
                130f,
                2800f,
                60f);
        Phlegethon.setCustomDescriptionId("vic_phlegethon"); //reference descriptions.csv
        Phlegethon.setInteractionImage("illustrations", "vic_phlegethon_illustration");


        MarketAPI Phlegeton_market = addMarketplace(
                "vic",
                Phlegethon,
                null,
                "Phlegethon",
                5,
                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_5,
                                Conditions.VERY_HOT,
                                Conditions.NO_ATMOSPHERE,
                                Conditions.TECTONIC_ACTIVITY,
                                Conditions.POLLUTION,
                                Conditions.RARE_ORE_MODERATE,
                                Conditions.ORE_MODERATE,
                                "vic_autoFactory",
                                "vic_orbitalLazor"
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
                                Industries.REFINING,
                                Industries.WAYSTATION,
                                Industries.ORBITALWORKS
                        )
                ),
                0.3f, //tariffs
                false, //freeport
                true //junk and chatter
        );


        Phlegeton_market.getIndustry(Industries.ORBITALWORKS).setSpecialItem(new SpecialItemData(Items.CORRUPTED_NANOFORGE, null));
        Phlegeton_market.setImmigrationIncentivesOn(true);

        // Archangel Mining Station
        SectorEntityToken Archangel = system.addCustomEntity("vic_archangel",
                "\"Archangel\" Orbital Mining Station",
                "vic_archangel_mining_station",
                "vic");
        Archangel.setCircularOrbitPointingDown(EmpyreanStar, 50f, 2200f, 60f);
        Archangel.setCustomDescriptionId("vic_archangel");

        //Archangel Mining Beam
        SectorEntityToken MiningBeam = system.addCustomEntity("vic_archangel_beam", null, "vic_archangel_mining_beam", null); //add the thing orbiting the market
        MiningBeam.setCircularOrbitPointingDown(EmpyreanStar, 50f, 2455f, 60f); //set as circular orbit

        //Archangel Planetary effect
        SectorEntityToken PlanetaryEffect = system.addCustomEntity("vic_archangel_planetary_effect", null, "vic_archangel_planetary_effect", null); //add the thing orbiting the market
        PlanetaryEffect.setCircularOrbitPointingDown(EmpyreanStar, 50f, 2800f, 60f); //set as circular orbit


        // add  Nav Buoy
        SectorEntityToken CocytusNavBuoy = system.addCustomEntity("cocytus_nav_buoy", "Cocytus Navigation Buoy", "nav_buoy", "vic");
        CocytusNavBuoy.setCircularOrbitPointingDown(EmpyreanStar, 360f * (float) Math.random(), 4300, 185);


        // Cocytus: Archipelago Homeworld
        PlanetAPI Cocytus = system.addPlanet("vic_planet_cocytus",
                EmpyreanStar,
                "Cocytus",
                "cocytus_islands",
                200,
                220f,
                4700f,
                120f);
        Cocytus.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "cocytus"));
        Cocytus.getSpec().setGlowColor(new Color(255, 255, 255, 255));
        Cocytus.getSpec().setUseReverseLightForGlow(true);
        Cocytus.applySpecChanges();
        Cocytus.setInteractionImage("illustrations", "vic_cocytus_illustration");
        Cocytus.setCustomDescriptionId("vic_cocytus"); //reference descriptions.csv

        MarketAPI Cocytus_market = addMarketplace("vic",
                Cocytus,
                null,
                "Cocytus",
                7,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_7,
                                Conditions.FARMLAND_POOR,
                                Conditions.ORGANICS_TRACE,
                                Conditions.HABITABLE,
                                Conditions.ORGANIZED_CRIME,
                                Conditions.TERRAN,
                                Conditions.MILD_CLIMATE,
                                Conditions.REGIONAL_CAPITAL,
                                Conditions.URBANIZED_POLITY,
                                Conditions.STEALTH_MINEFIELDS
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE,
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_BLACK
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.MEGAPORT,
                                Industries.STARFORTRESS_MID,
                                Industries.HEAVYBATTERIES,
                                Industries.HIGHCOMMAND,
                                Industries.WAYSTATION,
                                Industries.FARMING,
                                ("victourism"),
                                ("vic_revCenter"),
                                Industries.LIGHTINDUSTRY
                        )
                ),
                //tariffs
                0.3f,
                //freeport
                true,
                //junk and chatter
                true);

        Cocytus_market.getIndustry(Industries.FARMING).setSpecialItem(new SpecialItemData(vic_Items.GMOfarm, null));
        Cocytus_market.setImmigrationIncentivesOn(true);
        CargoAPI cargo = Cocytus_market.getSubmarket(Submarkets.GENERIC_MILITARY).getCargo();
        cargo.addSpecial(new SpecialItemData(vic_Items.GMOfarm, null), 1);
        if (Global.getSettings().getModManager().isModEnabled("alcoholism")){
            addBrewery(Cocytus_market);
        }

        //Inner Jump Point
        JumpPointAPI innerJumpPoint = Global.getFactory().createJumpPoint(

                "inner_jump_point",

                "Cocytus Jump Point");

        innerJumpPoint.setCircularOrbit(system.getEntityById("vic_star_empyrean"), 170, 3600, 120f);
        innerJumpPoint.setRelatedPlanet(Cocytus);
        system.addEntity(innerJumpPoint);


        //Hyperspace controversy
        SectorEntityToken Hyperspace_Stabilizer1 = system.addCustomEntity("vic_stabilizer1",
                "Hyperspace Coherence Adjustment Unit",
                "vic_jump_point_stabilizer",
                "vic");
        Hyperspace_Stabilizer1.setCircularOrbitPointingDown(innerJumpPoint, 240f, 150f, 5f);
        Hyperspace_Stabilizer1.setCustomDescriptionId("vic_jump_point_stabilizer");

        SectorEntityToken Hyperspace_Stabilizer2 = system.addCustomEntity("vic_stabilizer2",
                "Hyperspace Coherence Adjustment Unit",
                "vic_jump_point_stabilizer",
                "vic");
        Hyperspace_Stabilizer2.setCircularOrbitPointingDown(innerJumpPoint, 120f, 150f, 5f);
        Hyperspace_Stabilizer2.setCustomDescriptionId("vic_jump_point_stabilizer");

        SectorEntityToken Hyperspace_Stabilizer3 = system.addCustomEntity("vic_stabilizer3",
                "Hyperspace Coherence Adjustment Unit",
                "vic_jump_point_stabilizer",
                "vic");
        Hyperspace_Stabilizer3.setCircularOrbitPointingDown(innerJumpPoint, 0f, 150f, 5f);
        Hyperspace_Stabilizer3.setCustomDescriptionId("vic_jump_point_stabilizer");


        //Inner asteroid ring
        system.addAsteroidBelt(EmpyreanStar, 50, 5700, 800, 250, 400, Terrain.ASTEROID_BELT, "Inner Band");
        system.addRingBand(EmpyreanStar, "misc", "rings_asteroids0", 256f, 3, Color.gray, 256f, 5700f - 200, 250f);
        system.addRingBand(EmpyreanStar, "misc", "rings_asteroids0", 256f, 0, Color.YELLOW, 256f, 5700f, 350f);
        system.addRingBand(EmpyreanStar, "misc", "rings_asteroids0", 256f, 2, Color.red, 256f, 5700f + 200, 400f);
        system.addRingBand(EmpyreanStar, "misc", "rings_dust0", 512f, 1, Color.blue,  512f, 5500, 450f);
        system.addRingBand(EmpyreanStar, "misc", "rings_dust0", 512f, 4, Color.yellow, 512f, 5600, 500f);
        system.addRingBand(EmpyreanStar, "misc", "rings_dust0", 512f, 3, Color.gray, 512f, 5750, 600f);

        // Purgatory: Hell Toxic World
        PlanetAPI Purgatory = system.addPlanet("vic_planet_purgatory",
                EmpyreanStar,
                "Purgatory",
                "toxic",
                360f * (float) Math.random(),
                150f,
                6800f,
                180f);
        Purgatory.setCustomDescriptionId("vic_purgatory"); //reference descriptions.csv
        Purgatory.setInteractionImage("illustrations", "vic_purgatory_illustration");

        MarketAPI Purgatory_market = addMarketplace("vic",
                Purgatory,
                null,
                "Purgatory",
                5,
                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_5,
                                Conditions.TOXIC_ATMOSPHERE,
                                Conditions.EXTREME_WEATHER,
                                Conditions.COLD,
                                Conditions.ORE_SPARSE,
                                Conditions.ORGANICS_COMMON,
                                Conditions.DENSE_ATMOSPHERE,
                                Conditions.STEALTH_MINEFIELDS
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
                                Industries.STARFORTRESS_MID,
                                Industries.HEAVYBATTERIES,
                                Industries.MILITARYBASE,
                                ("vicbiolabs"),
                                Industries.WAYSTATION
                        )
                ),
                //tariffs
                0.3f,
                //freeport
                false,
                //junk and chatter
                false);

        Purgatory_market.setImmigrationIncentivesOn(true);


        // add Sensor Array
        SectorEntityToken PurgatorySensorArray = system.addCustomEntity("purgatory_sensor_array", "Outer Empyrean Sensor Array", "sensor_array", "vic");
        PurgatorySensorArray.setCircularOrbitPointingDown(EmpyreanStar, 360f * (float) Math.random(), 7400, 200);


        //Empyrean Inactive Gate
        SectorEntityToken EmpyreanGate = system.addCustomEntity("empyrean_gate", "Empyrean Gate", "inactive_gate", null); //add the thing orbiting the market
        EmpyreanGate.setCircularOrbitPointingDown(EmpyreanStar, 360f * (float) Math.random(), 7750f, 220f); //set as circular orbit


        //add Comm relay
        SectorEntityToken OuterRelay = system.addCustomEntity("Outer_relay", // unique id
                "Outer Empyrean Comm Relay", // name - if null, defaultName from custom_entities.json will be used
                "comm_relay", // type of object, defined in custom_entities.json
                "vic"); // faction
        OuterRelay.setCircularOrbitPointingDown(EmpyreanStar, 180f, 11000, 265);



        /*
        //Malebolge and the Giants
        PlanetAPI Malebolge = system.addPlanet("vic_Empyrean_malebolge", EmpyreanStar, "Malebolge", "gas_giant", 180f, 420, 11000, 265);

        Malebolge.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE);
        Malebolge.getMarket().addCondition(Conditions.HIGH_GRAVITY);
        Malebolge.getMarket().addCondition(Conditions.VOLATILES_ABUNDANT);


        PlanetAPI Nimrod = system.addPlanet("vic_Empyrean_nimrod", Malebolge, "Nimrod", "frozen2", 360f * (float) Math.random(), 100, 900, 12);

        Nimrod.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
        Nimrod.getMarket().addCondition(Conditions.VERY_COLD);
        Nimrod.getMarket().addCondition(Conditions.RARE_ORE_ULTRARICH);


        PlanetAPI Ephialtes = system.addPlanet("vic_Empyrean_ephialtes", Malebolge, "Ephialtes", "frozen3", 360f * (float) Math.random(), 120, 1450, 20);

        Ephialtes.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
        Ephialtes.getMarket().addCondition(Conditions.VERY_COLD);
        Ephialtes.getMarket().addCondition(Conditions.ORE_ULTRARICH);

        PlanetAPI Briareus = system.addPlanet("vic_Empyrean_briareus", Malebolge, "Briareus", "barren", 360f * (float) Math.random(), 90, 1950, 45);

        Briareus.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
        Briareus.getMarket().addCondition(Conditions.COLD);
        Briareus.getMarket().addCondition(Conditions.VOLATILES_ABUNDANT);

        PlanetAPI Antaeus = system.addPlanet("vic_Empyrean_antaeus", Malebolge, "Antaeus", "barren-bombarded", 360f * (float) Math.random(), 130, 2600, 70);

        Antaeus.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
        Antaeus.getMarket().addCondition(Conditions.COLD);
        Antaeus.getMarket().addCondition(Conditions.METEOR_IMPACTS);
        Antaeus.getMarket().addCondition(Conditions.RARE_ORE_RICH);
        Antaeus.getMarket().addCondition(Conditions.ORE_RICH);
        Antaeus.getMarket().addCondition(Conditions.VOLATILES_DIFFUSE);

        system.addRingBand(Malebolge, "misc", "rings_asteroids0", 256f, 0, Color.gray, 256f, 700, 20f);
        system.addRingBand(Malebolge, "misc", "rings_asteroids0", 256, 1, Color.red, 256f, 1200, 30f);
        system.addRingBand(Malebolge, "misc", "rings_ice0", 256f, 1, Color.yellow, 256f, 1700, 45f);
        system.addRingBand(Malebolge, "misc", "rings_asteroids0", 256f, 2, Color.gray, 256f, 2300, 60f);
        system.addAsteroidBelt(Malebolge, 50, 700, 200, 30, 30, Terrain.ASTEROID_BELT, "Malebolge Inner Asteroid Field");
        system.addAsteroidBelt(Malebolge, 100, 2300, 250, 80, 80, Terrain.ASTEROID_BELT, "Malebolge Outer Asteroid Field");

        SectorEntityToken Malebolge_magfield = system.addTerrain(Terrain.MAGNETIC_FIELD,
                new MagneticFieldParams(Malebolge.getRadius() + 150f, // terrain effect band width
                        (Malebolge.getRadius() + 150f) / 2f, // terrain effect middle radius
                        Malebolge, // entity that it's around
                        Malebolge.getRadius() + 50f, // visual band start
                        Malebolge.getRadius() + 50f + 200f, // visual band end
                        new Color(50, 20, 100, 50), // base color
                        0.15f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                        new Color(90, 180, 40),
                        new Color(130, 145, 90),
                        new Color(165, 110, 145),
                        new Color(95, 55, 160),
                        new Color(45, 0, 130),
                        new Color(20, 0, 130),
                        new Color(10, 0, 150)));
        Malebolge_magfield.setCircularOrbit(Malebolge, 0, 0, 100);

         */


        //Dis and its Furies (Tisiphone, Megaera, Alecto)
        PlanetAPI Dis = system.addPlanet("vic_Empyrean_dis", EmpyreanStar, "Dis", "ice_giant", 360f, 400, 11000, 265);

        Dis.getMarket().addCondition(Conditions.VERY_COLD);
        Dis.getMarket().addCondition(Conditions.HIGH_GRAVITY);
        Dis.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
        Dis.getMarket().addCondition(Conditions.TECTONIC_ACTIVITY);
        Dis.getMarket().addCondition(Conditions.ORE_MODERATE);
        Dis.getMarket().addCondition(Conditions.VOLATILES_PLENTIFUL);
        Dis.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);


        PlanetAPI Tisiphone = system.addPlanet("vic_Empyrean_tisiphone", Dis, "Tisiphone", "cryovolcanic", 45, 80, 700, 15);


        Tisiphone.getMarket().addCondition(Conditions.COLD);
        Tisiphone.getMarket().addCondition(Conditions.POOR_LIGHT);
        Tisiphone.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE);
        Tisiphone.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
        Tisiphone.getMarket().addCondition(Conditions.VOLATILES_ABUNDANT);
        Tisiphone.getMarket().addCondition(Conditions.ORE_MODERATE);
        Tisiphone.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);


        system.addAsteroidBelt(Dis, 100, 1100, 128, 40, 80, Terrain.ASTEROID_BELT, "Dis Inner Ring");
        system.addRingBand(Dis, "misc", "rings_asteroids0", 256f, 0, Color.blue, 256f, 1100, 20f);
        system.addRingBand(Dis, "misc", "rings_special0", 256f, 0, Color.white, 256f, 1120, 30f);

        PlanetAPI Megaera = system.addPlanet("vic_planet_megaera", Dis, "Megaera", "rocky_ice", 360f * (float) Math.random(), 110, 1400, 40);
        Megaera.setCustomDescriptionId("vic_megaera");
        Megaera.setInteractionImage("illustrations", "vic_megaera_illustration");

        //Megaera_Market
        MarketAPI Megaera_market = addMarketplace(
                "vic",
                Megaera,
                null,
                "Megaera",
                4,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_4,
                                Conditions.COLD,
                                Conditions.THIN_ATMOSPHERE,
                                Conditions.POLLUTION,
                                Conditions.POOR_LIGHT,
                                Conditions.ORE_RICH,
                                Conditions.RARE_ORE_MODERATE,
                                Conditions.VOLATILES_PLENTIFUL
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
                                Industries.HEAVYBATTERIES,
                                Industries.WAYSTATION,
                                Industries.ORBITALSTATION_MID,
                                Industries.FUELPROD
                        )
                ),
                //tariffs
                0.3f,
                //freeport
                false,
                //junk and chatter
                false);
        //adds Synchrotron to the Fuel Production industry
        Megaera_market.getIndustry(Industries.FUELPROD).setSpecialItem(new SpecialItemData(Items.SYNCHROTRON, null));
        Megaera_market.setImmigrationIncentivesOn(true);


        system.addAsteroidBelt(Dis, 100, 1600, 150, 50, 100, Terrain.ASTEROID_BELT, "Dis Outer Ring");
        system.addRingBand(Dis, "misc", "rings_special0", 256f, 0, Color.red, 256f, 1650, 55f);

        PlanetAPI Alecto = system.addPlanet("vic_planet_alecto", Dis, "Alecto", "frozen", 360f * (float) Math.random(), 110, 2000, 70);


        Alecto.getMarket().addCondition(Conditions.COLD);
        Alecto.getMarket().addCondition(Conditions.POOR_LIGHT);
        Alecto.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
        Alecto.getMarket().addCondition(Conditions.VOLATILES_DIFFUSE);
        Alecto.getMarket().addCondition(Conditions.ORE_MODERATE);


        system.addRingBand(Dis, "misc", "rings_asteroids0", 256f, 0, Color.gray, 256f, 2300, 50f);
        system.addRingBand(Dis, "misc", "rings_ice0", 256f, 0, Color.yellow, 256f, 2320, 30f);


        //Outer Asteroid belt
        system.addRingBand(EmpyreanStar, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 14700, 280f);
        system.addAsteroidBelt(EmpyreanStar, 100, 15000, 150, 300, 400, Terrain.ASTEROID_BELT, "Empyrean Outer Asteroid Field");
        system.addRingBand(EmpyreanStar, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 15000, 300f);
        system.addRingBand(EmpyreanStar, "misc", "rings_dust0", 256f, 0, Color.gray, 256f, 15020, 330f);
        system.addRingBand(EmpyreanStar, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 15040, 310f);
        system.addRingBand(EmpyreanStar, "misc", "rings_dust0", 256f, 0, Color.darkGray, 256f, 150060, 370f);
        system.addRingBand(EmpyreanStar, "misc", "rings_dust0", 256f, 0, Color.lightGray, 256f, 150060, 350f);
        system.addAsteroidBelt(EmpyreanStar, 100, 15100, 1200, 300, 400, Terrain.ASTEROID_BELT, "Empyrean Outer Asteroid Field");
        system.addRingBand(EmpyreanStar, "misc", "rings_asteroids0", 256f, 3, Color.gray, 256f, 15100f - 200, 350f);
        system.addRingBand(EmpyreanStar, "misc", "rings_asteroids0", 256f, 0, Color.gray, 256f, 15100f, 340f);
        system.addRingBand(EmpyreanStar, "misc", "rings_asteroids0", 256f, 2, Color.gray, 256f, 15100f + 200, 400f);


        //Pather vanguard planet Fortuna
        PlanetAPI Fortuna = system.addPlanet("vic_planet_fortuna", EmpyreanStar, "Fortuna", "barren", 360f * (float) Math.random(), 120, 17000, 300);
        Fortuna.setCustomDescriptionId("vic_fortuna");

        //Fortuna_Market
        MarketAPI Fortuna_market = addMarketplace(
                "luddic_path",
                Fortuna,
                null,
                "Fortuna",
                4,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_4,
                                Conditions.VERY_COLD,
                                Conditions.NO_ATMOSPHERE,
                                Conditions.METEOR_IMPACTS,
                                Conditions.POOR_LIGHT,
                                Conditions.RARE_ORE_ABUNDANT
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
                                Industries.WAYSTATION,
                                Industries.MINING,
                                Industries.HEAVYBATTERIES,
                                Industries.MILITARYBASE,
                                Industries.ORBITALSTATION

                        )
                ),
                //tariffs
                0.3f,
                //freeport
                false,
                //junk and chatter
                true);


        //Fringe planet Dismay
        PlanetAPI Dismay = system.addPlanet("vic_planet_dismay", EmpyreanStar, "Dismay", "frozen2", 360f * (float) Math.random(), 120, 18000f, 330);

        Dismay.getMarket().addCondition(Conditions.VERY_COLD);
        Dismay.getMarket().addCondition(Conditions.DARK);
        Dismay.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
        Dismay.getMarket().addCondition(Conditions.ORE_ULTRARICH);
        Dismay.getMarket().addCondition(Conditions.RARE_ORE_MODERATE);
        Dismay.getMarket().addCondition(Conditions.VOLATILES_PLENTIFUL);


        /*
        // Let's procgen some stuff here cause fuck doing that manually
        float ProcgenRadius = StarSystemGenerator.addOrbitingEntities(system, EmpyreanStar, StarAge.YOUNG,
                3, 5, // min/max entities to add
                19000, // radius to start adding at
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