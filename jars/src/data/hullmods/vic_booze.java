package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.hullmods.BaseAlcoholHullmodEffect;
import com.fs.starfarer.api.alcoholism.memory.AddictionStatus;
import com.fs.starfarer.api.alcoholism.memory.Alcohol;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_booze extends BaseAlcoholHullmodEffect {

    public static float
            minDamageDealtIncrease = -0.1f,
            maxDamageDealtIncrease = 0.3f,
            positiveCritChance = 0.2f,
            critDamageIncrease = 2f,

            minDamageDealtDecrease = -0.3f,
            maxDamageDealtDecrease = 0.1f,
            negativeCritChance = 0.2f,
            critDamageDecrease = 0.5f,

            minDamageTakenIncrease = -0.1f,
            maxDamageTakenIncrease = 0.3f;

    float opad = 10f;
    float spad = 3f;
    Color positive = Misc.getPositiveHighlightColor();
    Color neutral = Misc.getGrayColor();
    Color negative = Misc.getNegativeHighlightColor();

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        super.advanceInCombat(ship, amount);

        if (ship.getCaptain() != null
                || ship.getShipAI() == null
                || ship.getShipAI().getConfig().personalityOverride != null) return;

        Alcohol alcohol = getAlcohol();
        AddictionStatus status = alcohol.getAddictionStatus();
        float effectMult = status.getAddictionValue();

        if (!isWithdrawal() && getAlcohol().getAddictionStatus().isConsuming()) {
            if (Global.getSector().getPlayerFaction().getDoctrine().getAggression() < 4)
                ship.getShipAI().getConfig().personalityOverride = Personalities.AGGRESSIVE;
            else ship.getShipAI().getConfig().personalityOverride = Personalities.RECKLESS;
        }

        if (!ship.hasListenerOfClass(vic_boozeDealtListener.class)) {
            vic_boozeDealtListener listener = new vic_boozeDealtListener(effectMult, isWithdrawal());
            ship.addListener(listener);
        }

        if (!ship.hasListenerOfClass(vic_boozeTakenListener.class)) {
            vic_boozeTakenListener listener = new vic_boozeTakenListener(effectMult);
            ship.addListener(listener);
        }
    }

    @Override
    public void applyPositives(MutableShipStatsAPI stats, float effectMult, String id) {

    }

    @Override
    public void applyNegatives(MutableShipStatsAPI stats, float effectMult, String id) {

    }

    @Override
    public void applyWithdrawal(MutableShipStatsAPI stats, float effectMult, String id) {

    }

    @Override
    public void addPositiveEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {


        tooltip.addSectionHeading("Positive Effect", Misc.getTextColor(), new Color(50, 100, 50, 255), Alignment.MID, 10f);

        tooltip.addPara("Damage dealt randomized between %s-%s  [Max.: %s-%s]",
                opad,
                positive,
                100 + toPercent(minDamageDealtIncrease, effectMult) + "%",
                100 + toPercent(maxDamageDealtIncrease, effectMult) + "%",
                100 + toPercent(minDamageDealtIncrease, 1) + "%",
                100 + toPercent(maxDamageDealtIncrease, 1) + "%");

        tooltip.addPara("%s chance to deal %s damage  [Max.: %s]",
                spad,
                positive,
                toPercent(positiveCritChance, effectMult) + "%",
                toPercent(critDamageIncrease, 1) + "%",
                toPercent(positiveCritChance, 1) + "%");

        tooltip.addPara("Ships without officers behave as %s. If already %s - as %s.",
                spad,
                positive,
                Misc.ucFirst(Personalities.AGGRESSIVE),
                Misc.ucFirst(Personalities.AGGRESSIVE),
                Misc.ucFirst(Personalities.RECKLESS));
    }

    @Override
    public void addNegativeEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {


        tooltip.addSectionHeading("Negative Effect", Misc.getTextColor(), new Color(150, 100, 50, 255), Alignment.MID, 10f);

        tooltip.addPara("Damage taken randomized between %s-%s  [Max.: %s-%s]",
                opad,
                negative,
                100 + toPercent(minDamageTakenIncrease, effectMult) + "%",
                100 + toPercent(maxDamageTakenIncrease, effectMult) + "%",
                100 + toPercent(minDamageTakenIncrease, 1) + "%",
                100 + toPercent(maxDamageTakenIncrease, 1) + "%");
    }

    @Override
    public void addWithdrawalEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {


        tooltip.addSectionHeading("Withdrawal Effect", Misc.getTextColor(), new Color(150, 50, 50, 255), Alignment.MID, 10f);

        tooltip.addPara("Damage dealt randomized between %s-%s  [Max.: %s-%s]",
                opad,
                negative,
                100 + toPercent(minDamageDealtDecrease, effectMult) + "%",
                100 + toPercent(maxDamageDealtDecrease, effectMult) + "%",
                100 + toPercent(minDamageDealtDecrease, 1) + "%",
                100 + toPercent(maxDamageDealtDecrease, 1) + "%");

        tooltip.addPara("%s chance to deal %s damage  [Max.: %s]",
                spad,
                negative,
                toPercent(negativeCritChance, effectMult) + "%",
                toPercent(negativeCritChance, 1) + "%",
                toPercent(critDamageDecrease, 1) + "%");
    }

    public float toPercent(float num, float effectMult) {
        return Math.round(num * 100 * effectMult);
    }

    static class vic_boozeDealtListener implements DamageDealtModifier {

        float effectMult;
        boolean isWithdrawal;

        public vic_boozeDealtListener(float effectMult, boolean isWithdrawal) {
            this.effectMult = effectMult;
            this.isWithdrawal = isWithdrawal;
        }

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (target == null) return null;
            float damageMulti = 1;
            if (!isWithdrawal) {
                damageMulti *= MathUtils.getRandomNumberInRange(1 - (minDamageDealtIncrease * effectMult), 1 + (maxDamageDealtIncrease * effectMult));

                if (Math.random() <= positiveCritChance * effectMult) damageMulti *= critDamageIncrease;
            } else {
                damageMulti *= MathUtils.getRandomNumberInRange(1 - (minDamageDealtDecrease * effectMult), 1 + (maxDamageDealtDecrease * effectMult));

                if (Math.random() <= negativeCritChance * effectMult) damageMulti *= critDamageDecrease;
            }

            String id = "vic_boozeDealt";
            damage.getModifier().modifyMult(id, damageMulti);
            return id;
        }
    }

    static class vic_boozeTakenListener implements DamageTakenModifier {

        float effectMult;

        public vic_boozeTakenListener(float effectMult) {
            this.effectMult = effectMult;
        }

        @Override
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (target == null) return null;

            float damageMulti = 1;
            damageMulti *= MathUtils.getRandomNumberInRange(1 - (0.1f * minDamageTakenIncrease), 1 + (0.3f * maxDamageTakenIncrease));

            String id = "vic_boozeTaken";
            damage.getModifier().modifyMult(id, damageMulti);
            return null;
        }
    }


}
