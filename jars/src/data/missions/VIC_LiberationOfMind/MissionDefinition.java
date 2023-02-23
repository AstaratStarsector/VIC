package data.missions.VIC_LiberationOfMind;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        api.initFleet(FleetSide.PLAYER, "ASCV", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "CGR", FleetGoal.ESCAPE, true);

        api.setFleetTagline(FleetSide.PLAYER, "VIC Renegades");
        api.setFleetTagline(FleetSide.ENEMY, "Luddic Church Civilian Fleet");

        api.addBriefingItem("Exterminate the Tyrant's servants and their spawn");


        FleetMemberAPI flagship = api.addToFleet(FleetSide.PLAYER, "vic_stolas_hunter", FleetMemberType.SHIP, "ASCV Pale Sun", true);

        FactionAPI pirates = Global.getSettings().createBaseFaction(Factions.PIRATES);
        PersonAPI officer = pirates.createRandomPerson(FullName.Gender.MALE);
        officer.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
        officer.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 1);
        officer.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
        officer.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
        officer.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 1);
        officer.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 1);
        officer.getStats().setLevel(6);
        officer.setFaction("vic");
        officer.setPersonality(Personalities.RECKLESS);
        officer.getName().setFirst("Markus");
        officer.getName().setLast("Yuhas");
        officer.getName().setGender(FullName.Gender.MALE);
        officer.setPortraitSprite("graphics/portraits/characters/vic_yuhas_notFucked.png");
        flagship.setCaptain(officer);
        float maxCR = flagship.getRepairTracker().getMaxCR();
        flagship.getRepairTracker().setCR(maxCR);

		api.addToFleet(FleetSide.PLAYER, "vic_pruflas_Demolisher", FleetMemberType.SHIP, "ASCV Sun Eater", false);
		api.addToFleet(FleetSide.PLAYER, "vic_pruflas_Demolisher", FleetMemberType.SHIP, "ASCV Outbreak", false);

        //api.addToFleet(FleetSide.PLAYER, "vic_shabriri_drone", FleetMemberType.SHIP, "VIC Warden", false);


        FactionAPI church = Global.getSettings().createBaseFaction(Factions.LUDDIC_CHURCH);

        FleetMemberAPI enemyFlagship = api.addToFleet(FleetSide.ENEMY, "legion_Escort", FleetMemberType.SHIP, church.pickRandomShipName(), true);
        api.addToFleet(FleetSide.ENEMY, "colossus_Standard", FleetMemberType.SHIP, church.pickRandomShipName(), false);
        api.addToFleet(FleetSide.ENEMY, "starliner_Standard", FleetMemberType.SHIP, church.pickRandomShipName(), false);
        api.addToFleet(FleetSide.ENEMY, "nebula_Standard", FleetMemberType.SHIP, church.pickRandomShipName(), false);
        api.addToFleet(FleetSide.ENEMY, "nebula_Standard", FleetMemberType.SHIP, church.pickRandomShipName(), false);
        api.addToFleet(FleetSide.ENEMY, "buffalo_Standard", FleetMemberType.SHIP, church.pickRandomShipName(), false);
        api.addToFleet(FleetSide.ENEMY, "buffalo_Standard", FleetMemberType.SHIP, church.pickRandomShipName(), false);
        api.addToFleet(FleetSide.ENEMY, "hound_luddic_church_Standard", FleetMemberType.SHIP, church.pickRandomShipName(), false);
        api.addToFleet(FleetSide.ENEMY, "hound_luddic_church_Standard", FleetMemberType.SHIP, church.pickRandomShipName(), false);
        api.addToFleet(FleetSide.ENEMY, "condor_Attack", FleetMemberType.SHIP, church.pickRandomShipName(), false);
        api.addToFleet(FleetSide.ENEMY, "condor_Attack", FleetMemberType.SHIP, church.pickRandomShipName(), false);
        api.addToFleet(FleetSide.ENEMY, "eradicator_Assault", FleetMemberType.SHIP, church.pickRandomShipName(), false);
        api.addToFleet(FleetSide.ENEMY, "eradicator_Assault", FleetMemberType.SHIP, church.pickRandomShipName(), false);
        api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, church.pickRandomShipName(), false);
        api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, church.pickRandomShipName(), false);

        PersonAPI officerChurch = church.createRandomPerson(FullName.Gender.MALE);
        officerChurch.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
        officerChurch.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
        officerChurch.getStats().setSkillLevel(Skills.FIELD_MODULATION, 1);
        officerChurch.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 1);
        officerChurch.getStats().setSkillLevel(Skills.FIGHTER_UPLINK, 1);
        officerChurch.getStats().setSkillLevel(Skills.CARRIER_GROUP, 1);
        officerChurch.getStats().setLevel(6);
        officerChurch.setFaction("luddic_church");
        officerChurch.setPersonality(Personalities.STEADY);
        officerChurch.getName().setFirst("Andrew");
        officerChurch.getName().setLast("Uele");
        officerChurch.getName().setGender(FullName.Gender.MALE);
        officerChurch.setPortraitSprite("graphics/portraits/portrait_luddic00.png");
        enemyFlagship.setCaptain(officerChurch);
        float maxCRChurch = enemyFlagship.getRepairTracker().getMaxCR();
        enemyFlagship.getRepairTracker().setCR(maxCRChurch);


        api.defeatOnShipLoss("ASCV Pale Sun");

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
