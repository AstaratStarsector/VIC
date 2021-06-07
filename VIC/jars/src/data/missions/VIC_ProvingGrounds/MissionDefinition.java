package data.missions.VIC_ProvingGrounds;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        api.initFleet(FleetSide.PLAYER, "VIC", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "SIM", FleetGoal.ATTACK, true);

        api.setFleetTagline(FleetSide.PLAYER, "Simulated VIC Forces");
        api.setFleetTagline(FleetSide.ENEMY, "Simulated Enemy Forces");

        api.addBriefingItem("Test the efficiency of Aerospace Corps vessels");


		api.addToFleet(FleetSide.PLAYER, "vic_apollyon_standart", FleetMemberType.SHIP, "VIC Sun eater", true);
		api.addToFleet(FleetSide.PLAYER, "vic_oriax_standard", FleetMemberType.SHIP, "VIC Outbreak", false);

        api.addToFleet(FleetSide.PLAYER, "vic_valafar_assault", FleetMemberType.SHIP, "VIC Void Reaver", false);
        api.addToFleet(FleetSide.PLAYER, "vic_thamuz_standart", FleetMemberType.SHIP, "VIC Despoiler", false);
        api.addToFleet(FleetSide.PLAYER, "vic_cresil_assault", FleetMemberType.SHIP, "VIC 13", false);


        api.addToFleet(FleetSide.PLAYER, "vic_moloch_standart", FleetMemberType.SHIP, "VIC Maw of the Void", false);
        api.addToFleet(FleetSide.PLAYER, "vic_samael_standart", FleetMemberType.SHIP, "VIC Scarab", false);
        api.addToFleet(FleetSide.PLAYER, "vic_jezebeth_standart", FleetMemberType.SHIP, "VIC Beast", false);

        api.addToFleet(FleetSide.PLAYER, "vic_xaphan_skirmisher", FleetMemberType.SHIP, "VIC Abyss Walker", false);
        api.addToFleet(FleetSide.PLAYER, "vic_kobal_standart", FleetMemberType.SHIP, "VIC Repressor", false);
        api.addToFleet(FleetSide.PLAYER, "vic_pruflas_skirmish", FleetMemberType.SHIP, "VIC Keeper of Peace", false);
        api.addToFleet(FleetSide.PLAYER, "vic_nybbas_plasma", FleetMemberType.SHIP, "VIC Warden", false);

        //api.addToFleet(FleetSide.PLAYER, "vic_shabriri_drone", FleetMemberType.SHIP, "VIC Warden", false);


        FactionAPI hegemony = Global.getSettings().createBaseFaction(Factions.HEGEMONY);
        FleetMemberAPI member;
        member = api.addToFleet(FleetSide.ENEMY, "mora_Strike", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());


        api.defeatOnShipLoss("SIN Falen");

        float width = 16000f;
        float height = 16000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        for (int i = 0; i < 6; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            api.addNebula(x, y, radius);
        }

        api.addObjective(width * 0.35f, -height * 0.1f, "nav_buoy");
        api.addObjective(-width * 0.35f, -height * 0.1f, "nav_buoy");
        api.addObjective(0f, -height * 0.3f, "sensor_array");
        api.addObjective(width * 0.2f, height * 0.35f, "comm_relay");
        api.addObjective(-width * 0.2f, height * 0.35f, "comm_relay");

        api.addNebula(0f, -height * 0.3f, 1000f);
        api.addNebula(width * 0.15f, -height * 0.05f, 2000f);
        api.addNebula(-width * 0.15f, -height * 0.05f, 2000f);

        api.addRingAsteroids(0f, 0f, 40f, width, 30f, 40f, 400);

        api.addPlanet(0, 0, 350f, "ice_giant", 0f, true);
    }
}
