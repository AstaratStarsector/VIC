package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc.Token;

import java.util.List;
import java.util.Map;

public class haveUMOOptions extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

		for (FleetMemberAPI shipToCheck : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()){
			if (shipToCheck.getVariant().hasHullMod("mhmods_testhullmod")){
				return true;
			}

		}

		return false;
	}
}
