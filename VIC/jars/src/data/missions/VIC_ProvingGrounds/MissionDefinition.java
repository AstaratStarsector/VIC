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
        api.initFleet(FleetSide.PLAYER, "TTS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true);

        api.setFleetTagline(FleetSide.PLAYER, "<DATA EXPUNGED>");
        api.setFleetTagline(FleetSide.ENEMY, "Hegemony Advance Scouts");

        api.addBriefingItem("Eradicate the enemy fleet");
        api.addBriefingItem("TTDS Automata must survive");

		api.addToFleet(FleetSide.PLAYER, "vic_apollyon_dominator", FleetMemberType.SHIP, "SIN Falen", true);
        api.addToFleet(FleetSide.PLAYER, "vic_cresil_support", FleetMemberType.SHIP, "TEST subject", false);
        api.addToFleet(FleetSide.PLAYER, "vic_jezebeth_command", FleetMemberType.SHIP, "TEST subject vector trusters", false);
        api.addToFleet(FleetSide.PLAYER, "vic_kobal_artillery", FleetMemberType.SHIP, "TEST subject vector lunge", false);
        api.addToFleet(FleetSide.PLAYER, "vic_moloch_enforcer", FleetMemberType.SHIP, "TEST subject vector lunge", false);
        api.addToFleet(FleetSide.PLAYER, "vic_nybbas_killer", FleetMemberType.SHIP, "TEST subject vector lunge", false);
        api.addToFleet(FleetSide.PLAYER, "vic_pruflas_skirmish", FleetMemberType.SHIP, "TEST subject vector lunge", false);
        api.addToFleet(FleetSide.PLAYER, "vic_samael_siege", FleetMemberType.SHIP, "TEST subject vector lunge", false);
        api.addToFleet(FleetSide.PLAYER, "vic_shabriri_drone", FleetMemberType.SHIP, "TEST subject vector lunge", false);
        api.addToFleet(FleetSide.PLAYER, "vic_thamuz_siege", FleetMemberType.SHIP, "TEST subject vector lunge", false);
        api.addToFleet(FleetSide.PLAYER, "vic_valafar_assault", FleetMemberType.SHIP, "TEST subject vector lunge", false);
        api.addToFleet(FleetSide.PLAYER, "vic_xaphan_assault", FleetMemberType.SHIP, "TEST subject vector lunge", false);


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