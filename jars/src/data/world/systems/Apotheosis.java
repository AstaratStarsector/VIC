package data.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.*;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.Random;


public class Apotheosis{

    public float syncOrbitDays = -80f;

    public void generate(SectorAPI sector) {

        LocationAPI hyper = Global.getSector().getHyperspace();

        StarSystemAPI system = sector.createStarSystem("Apotheosis");
        system.getLocation().set(1300, -25500);
        system.setBackgroundTextureFilename("graphics/backgrounds/apotheosis_background.jpg");
        system.addTag(Tags.THEME_UNSAFE);
        system.addTag(Tags.THEME_HIDDEN);
        system.addTag(Tags.STORY_CRITICAL);
        system.addTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER);


        // create the star and generate the hyperspace anchor for this system
        PlanetAPI ApotheosisQuasar = system.initStar("vic_quasar_apotheosis", // unique id for this star
                "quasar", // id in planets.json
                400f,        // radius (in pixels at default zoom)
                350f, // corona radius, from star edge
                -10f, // solar wind burn level
                0.0f, // flare probability
                25f); // cr loss mult


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

/*
        system.addRingBand(ApotheosisQuasar, "misc", "rings_dust0", 256f, 4, Color.red, 500f, 600f, 5f);
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

        system.addRingBand(ApotheosisQuasar, "misc", "rings_asteroids0", 80f, 1, new Color(101, 76, 49), 160f, 600f, 7f);
        system.addRingBand(ApotheosisQuasar, "misc", "rings_asteroids0", 80f, 2, new Color(101, 76, 49), 160f, 700f, 10f);
        //system.addRingBand(ApotheosisQuasar, "misc", "rings_asteroids0", 80f, 3, new Color(101, 76, 49), 160f, 800f, 15f);
        */


        float orbitRadius5 = ApotheosisQuasar.getRadius() * 3f;
        float bandWidth5 = 160f;
        int numBands5 = 2;

        for (float i = 0; i < numBands5; i++) {
            float radius = orbitRadius5 - i * bandWidth5 * 0.25f - i * bandWidth5 * 0.1f;
            float orbitDays = radius * 0.3f / (30f + 10f * Misc.random.nextFloat());
            WeightedRandomPicker<String> rings = new WeightedRandomPicker<>();
            rings.add("rings_asteroids0");
            String ring = rings.pick();
            RingBandAPI visual = system.addRingBand(ApotheosisQuasar, "misc", ring, 80f, 1, new Color(101, 76, 49, 255), bandWidth5,
                    radius + bandWidth5 + 50 / 2f, -orbitDays);
            RingBandAPI visual2 = system.addRingBand(ApotheosisQuasar, "misc", ring, 80f, 2, new Color(101, 76, 49, 255), bandWidth5,
                    radius + bandWidth5 + 150 / 2f, -orbitDays);
            float spiralFactor = 2f + Misc.random.nextFloat() * 5f;
            visual.setSpiral(true);
            visual.setMinSpiralRadius(0f);
            visual.setSpiralFactor(spiralFactor);
            visual2.setSpiral(true);
            visual2.setMinSpiralRadius(0f);
            visual2.setSpiralFactor(spiralFactor);
        }


        float orbitRadius1 = ApotheosisQuasar.getRadius() * 16f;
        float bandWidth1 = 256f;
        int numBands1 = 8;

        for (float i = 0; i < numBands1; i++) {
            float radius = orbitRadius1 - i * bandWidth1 * 0.25f - i * bandWidth1 * 0.1f;
            float orbitDays = radius * 0.3f / (30f + 10f * Misc.random.nextFloat());
            WeightedRandomPicker<String> rings = new WeightedRandomPicker<>();
            rings.add("rings_dust0");
            rings.add("rings_ice0");
            String ring = rings.pick();
            RingBandAPI visual = system.addRingBand(ApotheosisQuasar, "misc", ring, 256f, 0, new Color(46, 35, 173), bandWidth1,
                    radius + bandWidth1 / 2f, -orbitDays);
            RingBandAPI visual2 = system.addRingBand(ApotheosisQuasar, "misc", ring, 256f, 1, new Color(46, 35, 173), bandWidth1,
                    radius + bandWidth1 / 2f, -orbitDays);
            RingBandAPI visual3 = system.addRingBand(ApotheosisQuasar, "misc", ring, 256f, 2, new Color(46, 35, 173), bandWidth1,
                    radius + bandWidth1 / 2f, -orbitDays);
            float spiralFactor = 2f + Misc.random.nextFloat() * 5f;
            visual.setSpiral(true);
            visual.setMinSpiralRadius(ApotheosisQuasar.getRadius() / 3);
            visual.setSpiralFactor(spiralFactor);
            visual2.setSpiral(true);
            visual2.setMinSpiralRadius(ApotheosisQuasar.getRadius() / 2);
            visual2.setSpiralFactor(spiralFactor);
            visual3.setSpiral(true);
            visual3.setMinSpiralRadius(ApotheosisQuasar.getRadius());
            visual3.setSpiralFactor(spiralFactor);

        }


        float orbitRadius4 = ApotheosisQuasar.getRadius() * 14f;
        float bandWidth4 = 100f;
        int numBands4 = 1;

        for (float i = 0; i < numBands4; i++) {
            float radius = orbitRadius4 / 2 - i * bandWidth4 * 0.25f - i * bandWidth4 * 0.1f;
            float orbitDays = radius * 0.3f / (30f + 10f * Misc.random.nextFloat());
            WeightedRandomPicker<String> rings = new WeightedRandomPicker<>();
            rings.add("rings_asteroids0");
            String ring = rings.pick();
            RingBandAPI visual = system.addRingBand(ApotheosisQuasar, "misc", ring, 130f, 1, Color.red, 130,
                    radius + bandWidth4 / 2f, -orbitDays);
            RingBandAPI visual2 = system.addRingBand(ApotheosisQuasar, "misc", ring, 130f, 2, Color.red, 130,
                    radius + bandWidth4 / 2f, -orbitDays);
            RingBandAPI visual3 = system.addRingBand(ApotheosisQuasar, "misc", ring, 100f, 1, Color.red, 130,
                    radius + bandWidth4 / 2f, -orbitDays);
            float spiralFactor = 2f + Misc.random.nextFloat() * 5f;
            visual.setSpiral(true);
            visual.setMinSpiralRadius(ApotheosisQuasar.getRadius() / 2f);
            visual.setSpiralFactor(spiralFactor);
            visual2.setSpiral(true);
            visual2.setMinSpiralRadius(ApotheosisQuasar.getRadius() / 2f);
            visual2.setSpiralFactor(spiralFactor);
            visual3.setSpiral(true);
            visual3.setMinSpiralRadius(ApotheosisQuasar.getRadius() / 2f);
            visual3.setSpiralFactor(spiralFactor);
        }


        float orbitRadius3 = ApotheosisQuasar.getRadius() * 10f;
        float bandWidth3 = 256f;
        int numBands3 = 12;

        for (float i = 0; i < numBands3; i++) {
            float radius = orbitRadius3 - i * bandWidth3 * 0.25f - i * bandWidth3 * 0.1f;
            float orbitDays = radius * 0.3f / (30f + 10f * Misc.random.nextFloat());
            WeightedRandomPicker<String> rings = new WeightedRandomPicker<>();
            rings.add("rings_ice0");
            rings.add("rings_dust0");
            String ring = rings.pick();
            RingBandAPI visual = system.addRingBand(ApotheosisQuasar, "misc", ring, 256f, 2, Color.red, bandWidth3,
                    radius + bandWidth3 / 2f, -orbitDays);
            float spiralFactor = 2f + Misc.random.nextFloat() * 5f;
            visual.setSpiral(true);
            visual.setMinSpiralRadius(0);
            visual.setSpiralFactor(spiralFactor);
        }


        float orbitRadius2 = ApotheosisQuasar.getRadius() * 5f;
        float bandWidth2 = 256f;
        int numBands2 = 12;

        for (float i = 0; i < numBands2; i++) {
            float radius = orbitRadius2 - i * bandWidth2 * 0.25f - i * bandWidth2 * 0.1f;
            float orbitDays = radius * 0.3f / (30f + 10f * Misc.random.nextFloat());
            WeightedRandomPicker<String> rings = new WeightedRandomPicker<>();
            rings.add("rings_ice0");
            rings.add("rings_dust0");
            String ring = rings.pick();
            RingBandAPI visual = system.addRingBand(ApotheosisQuasar, "misc", ring, 256f, 3, Color.red, bandWidth2,
                    radius + bandWidth2 / 2f, -orbitDays);
            float spiralFactor = 2f + Misc.random.nextFloat() * 5f;
            visual.setSpiral(true);
            visual.setMinSpiralRadius(0);
            visual.setSpiralFactor(spiralFactor);
        }


        SectorEntityToken ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(orbitRadius1, orbitRadius1 / 2f, ApotheosisQuasar, "Accretion Disk"));
        ring.addTag(Tags.ACCRETION_DISK);
        ring.setCircularOrbit(ApotheosisQuasar, 0, 0, -100);


        PlanetAPI LostHope = system.addPlanet("vic_planet_LostHope",
                ApotheosisQuasar,
                "Lost Hope",
                "irradiated",
                0,
                250,
                6800,
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
        MarketAPI market = Global.getFactory().createMarket("vic_ApotheosisAbandonedStationMarket", "Abandoned Station", 0);
        market.setPrimaryEntity(neutralStation);
        market.setFactionId(neutralStation.getFaction().getId());
        market.addIndustry(Industries.SPACEPORT);
        market.addCondition(Conditions.ABANDONED_STATION);
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        ((StoragePlugin) market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);

        neutralStation.setMarket(market);
        neutralStation.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addSupplies(MathUtils.getRandomNumberInRange(40, 60));
        neutralStation.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity(Commodities.ORGANS, MathUtils.getRandomNumberInRange(30, 40));
        neutralStation.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addMothballedShip(FleetMemberType.SHIP, "vic_buffalo_vic_standard", "Techoblade");
        neutralStation.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().sort();

        //Inner Jump Point
        /*
        JumpPointAPI innerJumpPoint = Global.getFactory().createJumpPoint(
                "apotheosis_jump_point",
                "Apotheosis Jump Point");

        innerJumpPoint.setCircularOrbit(ApotheosisQuasar, 0, 5000, syncOrbitDays);
        innerJumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(innerJumpPoint);
        system.setHasSystemwideNebula(true);

         */

        //Loot at shit
        SectorEntityToken ResearchStation = DerelictThemeGenerator.addSalvageEntity(system, "station_research", Factions.DERELICT);
        ResearchStation.setCircularOrbit(ApotheosisQuasar, 180, 2000, syncOrbitDays);

        addDerelict(system, ApotheosisQuasar, "vic_valafar_assault", ShipRecoverySpecial.ShipCondition.WRECKED, 800 + ((float) Math.random() * 200f), 0, (Math.random() < 0.4));
        addDerelict(system, ApotheosisQuasar, "vic_thamuz_standard", ShipRecoverySpecial.ShipCondition.WRECKED, 800 + ((float) Math.random() * 200f), 120, (Math.random() < 0.4));
        addDerelict(system, ApotheosisQuasar, "vic_cresil_bombardier", ShipRecoverySpecial.ShipCondition.WRECKED, 800 + ((float) Math.random() * 200f), 240, (Math.random() < 0.4));

        addDerelict(system, ApotheosisQuasar, "vic_jezebeth_assault", ShipRecoverySpecial.ShipCondition.BATTERED, 1500 + ((float) Math.random() * 200f), 0, (Math.random() < 0.4));
        addDerelict(system, ApotheosisQuasar, "vic_moloch_standard", ShipRecoverySpecial.ShipCondition.BATTERED, 1500 + ((float) Math.random() * 200f), 120, (Math.random() < 0.4));
        addDerelict(system, ApotheosisQuasar, "vic_samael_standard", ShipRecoverySpecial.ShipCondition.BATTERED, 1500 + ((float) Math.random() * 200f), 240, (Math.random() < 0.4));

        addDerelict(system, ApotheosisQuasar, "vic_xaphan_skirmisher", ShipRecoverySpecial.ShipCondition.BATTERED, 2200 + ((float) Math.random() * 200f), 0, (Math.random() < 0.4));
        addDerelict(system, ApotheosisQuasar, "vic_kobal_agony", ShipRecoverySpecial.ShipCondition.BATTERED, 2200 + ((float) Math.random() * 200f), 120, (Math.random() < 0.4));
        addDerelict(system, ApotheosisQuasar, "vic_pruflas_Demolisher", ShipRecoverySpecial.ShipCondition.BATTERED, 2200 + ((float) Math.random() * 200f), 240, (Math.random() < 0.4));

        generateNebula(system);

        system.setLightColor(new Color(255, 110, 25, 136)); // light color in entire system, affects all entities

        // generates hyperspace destinations for in-system jump points
        system.generateAnchorIfNeeded();
        NascentGravityWellAPI well = Global.getSector().createNascentGravityWell(LostHope, 0f);
        well.addTag(Tags.NO_ENTITY_TOOLTIP);
        well.setColorOverride(new Color(255, 139, 0));
        hyper.addEntity(well);
        well.autoUpdateHyperLocationBasedOnInSystemEntityAtRadius(LostHope, 10);
        /*
        system.autogenerateHyperspaceJumpPoints(true, false);
        for (JumpPointAPI hole : system.getAutogeneratedJumpPointsInHyper()){
            if (hole.isStarAnchor()){
                hole.clearDestinations();
            }
        }
         */

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

        float orbitDays = orbitRadius * MathUtils.getRandomNumberInRange(0.7f, 1.3f) / 50;
        ship.setCircularOrbit(focus, (float) MathUtils.getRandomNumberInRange(-10, 10) + angle, orbitRadius, orbitDays);

        WeightedRandomPicker<String> factions = new WeightedRandomPicker<>();
        factions.add("vic");
        if (recoverable) {
            SalvageSpecialAssigner.ShipRecoverySpecialCreator creator = new SalvageSpecialAssigner.ShipRecoverySpecialCreator(null, 0, 0, false, null, factions);
            Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
        }
    }

    protected void generateNebula(StarSystemAPI system) {
        Random random = new Random(getStartingSeed());
        float holeRadius = (int) getRandomFloat(random, 6500, 7000);
        float w = 15000;
        float h = 40000;

        // First make a solid map-spanning nebula
        SectorEntityToken nebulaTiles = Misc.addNebulaFromPNG("data/campaign/terrain/nebula_solid.png",
                0, 0, // Center of nebula
                system, // Location to add to
                "terrain", "nebula_amber", // Texture to use, uses xxx_map for map
                4, 4, Terrain.NEBULA, StarAge.OLD);

        nebulaTiles.getLocation().set(0, 0);

        BaseTiledTerrain nebula = getNebula(system);
        nebula.setTerrainName("Siwang Cloud");
        NebulaEditor editor = new NebulaEditor(nebula);

        // Donut hole
        editor.clearArc(0, 0, 0, holeRadius, 0, 360);

        // Do some random arcs
        // Taken from vanilla's SectorProcGen.java
        int numArcs = 2;

        for (int i = 0; i < numArcs; i++) {
            float dist = w / 2f + w / 2f * random.nextFloat();
            float angle = random.nextFloat() * 360f;

            Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
            dir.scale(dist - (w / 12f + w / 3f * random.nextFloat()));

            float width = 800f * (1f + 2f * random.nextFloat());

            float clearThreshold = 0f + 0.5f * random.nextFloat();

            editor.clearArc(dir.x, dir.y, dist - width / 2f, dist + width / 2f, 0, 360f, clearThreshold);
        }

        // Clear planet orbit paths
        SectorEntityToken center = system.getCenter();
        for (PlanetAPI planet : system.getPlanets()) {
            if (planet == center) {
                continue;
            }
            if (MathUtils.isWithinRange(center, planet, holeRadius - 3000)) {
                continue;
            }
            float dist = MathUtils.getDistance(center, planet);
            float width = 2000 + planet.getRadius() * 4;
            float clearThreshold = 0f + 0.5f * random.nextFloat();
            editor.clearArc(0, 0, dist - width / 2f, dist + width / 2f, 0, 360f, clearThreshold);
        }

        // Noise
        editor.regenNoise();
        editor.noisePrune(0.6f);
        editor.regenNoise();
    }

    long getStartingSeed() {
        String seedStr = Global.getSector().getSeedString().replaceAll("[^0-9]", "");
        return Long.parseLong(seedStr);
    }

    float getRandomFloat(Random random, float min, float max) {
        return min + (max - min) * random.nextFloat();
    }

    BaseTiledTerrain getNebula(StarSystemAPI system) {
        for (CampaignTerrainAPI curr : system.getTerrainCopy()) {
            if (curr.getPlugin().getTerrainId().equals(Terrain.NEBULA)) {
                return (BaseTiledTerrain) (curr.getPlugin());
            }
        }
        return null;
    }

}