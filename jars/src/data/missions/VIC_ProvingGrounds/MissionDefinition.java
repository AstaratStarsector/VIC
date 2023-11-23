package data.missions.VIC_ProvingGrounds;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        api.initFleet(FleetSide.PLAYER, "ASCV", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "SIM", FleetGoal.ATTACK, true);

        api.setFleetTagline(FleetSide.PLAYER, "Simulated VIC Forces");
        api.setFleetTagline(FleetSide.ENEMY, "Simulated Enemy Forces");

        api.addBriefingItem("Test the efficiency of Aerospace Corps vessels");



        FactionAPI VIC = Global.getSettings().createBaseFaction("vic");

		api.addToFleet(FleetSide.PLAYER, "vic_apollyon_standard", FleetMemberType.SHIP, "ASCV Sun Eater", true);
		api.addToFleet(FleetSide.PLAYER, "vic_oriax_standard", FleetMemberType.SHIP, "ASCV Outbreak", false);
        api.addToFleet(FleetSide.PLAYER, "vic_raum_laidlaw", FleetMemberType.SHIP, "ASCV Ruination", false);

        api.addToFleet(FleetSide.PLAYER, "vic_focalor_l_standard", FleetMemberType.SHIP, VIC.pickRandomShipName(), false);
        api.addToFleet(FleetSide.PLAYER, "vic_focalor_m_standard", FleetMemberType.SHIP, VIC.pickRandomShipName(), false);
        api.addToFleet(FleetSide.PLAYER, "vic_valafar_assault", FleetMemberType.SHIP, "ASCV Void Reaver", false);
        api.addToFleet(FleetSide.PLAYER, "vic_thamuz_standard", FleetMemberType.SHIP, "ASCV Despoiler", false);
        api.addToFleet(FleetSide.PLAYER, "vic_cresil_assault", FleetMemberType.SHIP, "ASCV 13", false);

        api.addToFleet(FleetSide.PLAYER, "vic_moloch_standard", FleetMemberType.SHIP, "ASCV Maw of The Void", false);
        api.addToFleet(FleetSide.PLAYER, "vic_samael_standard", FleetMemberType.SHIP, "ASCV Scarab", false);
        api.addToFleet(FleetSide.PLAYER, "vic_jezebeth_standard", FleetMemberType.SHIP, "ASCV Beast", false);

        api.addToFleet(FleetSide.PLAYER, "vic_xaphan_skirmisher", FleetMemberType.SHIP, "ASCV Abyss Walker", false);
        api.addToFleet(FleetSide.PLAYER, "vic_kobal_standard", FleetMemberType.SHIP, "ASCV Repressor", false);
        api.addToFleet(FleetSide.PLAYER, "vic_pruflas_skirmish", FleetMemberType.SHIP, "ASCV Keeper of Peace", false);
        api.addToFleet(FleetSide.PLAYER, "vic_nybbas_plasma", FleetMemberType.SHIP, "ASCV Warden", false);

        //api.addToFleet(FleetSide.PLAYER, "vic_shabriri_drone", FleetMemberType.SHIP, "VIC Warden", false);


        FactionAPI hegemony = Global.getSettings().createBaseFaction(Factions.HEGEMONY);
        FleetMemberAPI member;
        member = api.addToFleet(FleetSide.ENEMY, "mora_Strike", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());

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
