package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.combat.entities.Missile;
import data.scripts.plugins.timer.VIC_TimeTracker;
import data.scripts.plugins.vic_brandEngineUpgradesDetectionRange;
import data.scripts.weapons.ai.vic_disruptorShot_AI;
import data.scripts.weapons.ai.vic_noMissilesAI;
import data.scripts.weapons.ai.vic_verlioka;
import data.world.VICGen;
import exerelin.campaign.SectorManager;


public class VIC_ModPlugin extends BaseModPlugin {

    public static final String VERLIOKA = "vic_verlioka_shot";
    public static final String DISRUPTOR = "vic_disruptorShot_mie";
    public static final String QLMARKER = "vic_quantumLungeMarker";

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case VERLIOKA:
                return new PluginPick<MissileAIPlugin>(new vic_verlioka(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case DISRUPTOR:
                return new PluginPick<MissileAIPlugin>(new vic_disruptorShot_AI((Missile) missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case QLMARKER:
                return new PluginPick<MissileAIPlugin>(new vic_noMissilesAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            default:
        }
        return null;
    }


    @Override
    public void onNewGame() {
        //Nex compatibility setting, if there is no nex or corvus mode(Nex), just generate the system
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        if (!haveNexerelin || SectorManager.getCorvusMode()) {
            new VICGen().generate(Global.getSector());
        }

        if (!Global.getSector().hasScript(VIC_TimeTracker.class)) {
            Global.getSector().addScript(new VIC_TimeTracker());
            Global.getSector().addScript(new vic_brandEngineUpgradesDetectionRange());
        }
    }

}