package data.scripts;



import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import data.scripts.world.VICGen;
import static com.fs.starfarer.api.Global.getSettings;
import exerelin.campaign.SectorManager;



public class VIC_ModPlugin extends BaseModPlugin

{



       // public static void initVIC()

      //  {

      //      new VICGen().generate(Global.getSector());

      //  }



            @Override
            public void onNewGame() {
                //Nex compatibility setting, if there is no nex or corvus mode(Nex), just generate the system
                boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
                if (!haveNexerelin || SectorManager.getCorvusMode()) {
                    new VICGen().generate(Global.getSector());
                }
            }


        //initVIC();

}