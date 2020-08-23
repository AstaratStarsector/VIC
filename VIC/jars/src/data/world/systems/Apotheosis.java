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
import com.fs.starfarer.api.impl.campaign.econ.impl.FuelProduction;
import com.fs.starfarer.api.impl.campaign.econ.impl.HeavyIndustry;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.EventHorizonPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import org.lazywizard.lazylib.MathUtils;

import static data.scripts.world.VICGen.addMarketplace;


public class Apotheosis {

    public void generate(SectorAPI sector) {


        StarSystemAPI system = sector.createStarSystem("Apotheosis");
        system.getLocation().set(1300, -25500);
        system.setBackgroundTextureFilename("graphics/backgrounds/apotheosis_background.jpg");


        // create the star and generate the hyperspace anchor for this system
        PlanetAPI ApotheosisQuasar = system.initStar("vic_quasar_apotheosis)", // unique id for this star
                "quasar", // id in planets.json
                400f,		// radius (in pixels at default zoom)
                250f, // corona radius, from star edge
                -15f, // solar wind burn level
                0.0f, // flare probability
                25f); // cr loss mult

        SectorEntityToken IttirEventHorizon = system.addTerrain(Terrain.EVENT_HORIZON,
                new EventHorizonPlugin.CoronaParams(4000,
                        0,
                        ApotheosisQuasar,
                        -16f,
                        0f,
                        15f));

        SectorEntityToken IttirEventHorizonOuter = system.addTerrain(Terrain.EVENT_HORIZON,
                new EventHorizonPlugin.CoronaParams(2000,
                        3000,
                        ApotheosisQuasar,
                        -5f,
                        0f,
                        -1f));


        SectorEntityToken ApotheosisPulsarBeam = system.addTerrain(Terrain.PULSAR_BEAM,
                new StarCoronaTerrainPlugin.CoronaParams(20000,
                        3000,
                        ApotheosisQuasar,
                        50f,
                        0f,
                        30f));

        ApotheosisPulsarBeam.setCircularOrbit(ApotheosisQuasar, 0, 0, 10);




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


        system.setLightColor(new Color(255, 110, 25)); // light color in entire system, affects all entities


         //Inner Jump Point
        JumpPointAPI innerJumpPoint = Global.getFactory().createJumpPoint(

                "apotheosis_jump_point",

                "Apotheosis Jump Point");

        innerJumpPoint.setCircularOrbit(ApotheosisQuasar, 170, 3600, 120f);
        innerJumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(innerJumpPoint);


        // generates hyperspace destinations for in-system jump points
        system.autogenerateHyperspaceJumpPoints(true, true);

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

}