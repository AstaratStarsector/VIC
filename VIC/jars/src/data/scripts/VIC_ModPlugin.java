package data.scripts;


import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import data.scripts.plugins.timer.VIC_TimeTracker;
import data.scripts.world.VICGen;
import exerelin.campaign.SectorManager;


public class VIC_ModPlugin extends BaseModPlugin {

    @Override
    public void onNewGame() {
        //Nex compatibility setting, if there is no nex or corvus mode(Nex), just generate the system
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        if (!haveNexerelin || SectorManager.getCorvusMode()) {
            new VICGen().generate(Global.getSector());
        }

        if (!Global.getSector().hasScript(VIC_TimeTracker.class)) {
            Global.getSector().addScript(new VIC_TimeTracker());
        }
    }

}