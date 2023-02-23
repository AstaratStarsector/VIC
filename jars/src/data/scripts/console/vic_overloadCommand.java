package data.scripts.console;

import com.fs.starfarer.api.Global;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;

public class vic_overloadCommand implements BaseCommand {

    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull BaseCommand.CommandContext context) {
        float overloadDur;
        if (args.isEmpty()){
            overloadDur = 10f;
        } else try
        {
            overloadDur = Float.parseFloat(args);
        }
        catch (NumberFormatException ex)
        {
            return CommandResult.BAD_SYNTAX;
        }
        Global.getCombatEngine().getPlayerShip().getFluxTracker().beginOverloadWithTotalBaseDuration(overloadDur);
        return null;
    }
}
