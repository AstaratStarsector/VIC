package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.RecentUnrest;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class VIC_FactionVBomb {

    public static void addBombardVisual(SectorEntityToken target) {
        if (target != null && target.isInCurrentLocation()) {
            int num = (int) Math.round((3.15 * Math.pow(target.getRadius(), 2)) / 800);
            num *= 2;
            if (num > 200) num = 200;
            if (num < 10) num = 10;
            target.addScript(new VIC_FactionVBomb.ViralBombardmentAnimation(num, target));
        }
    }

    public void doBombardment(FactionAPI faction, MarketAPI market) {

        market.addCondition("VIC_VBomb_scar");

        Color toxinColor = new Color(121, 182, 5, 164);
        if (market.getPlanetEntity() != null) {
            PlanetAPI planet = market.getPlanetEntity();
            if (planet.getSpec() != null){
                planet.getSpec().setAtmosphereColor(toxinColor);
                if (planet.getSpec().getCloudTexture() != null){
                    planet.getSpec().setCloudColor(toxinColor);
                }
                planet.applySpecChanges();
            }
        }

        String reason = faction.getDisplayName() + " bombardment";
        RecentUnrest.get(market).add(2, reason);

        Misc.setFlagWithReason(market.getMemoryWithoutUpdate(), MemFlags.RECENTLY_BOMBARDED,
                faction.getId(), true, 30f);

        addBombardVisual(market.getPrimaryEntity());
    }

    public static class ViralBombardmentAnimation implements EveryFrameScript {
        int num = 0;
        SectorEntityToken target;
        int added = 0;
        float elapsed = 0;
        public ViralBombardmentAnimation(int num, SectorEntityToken target) {
            this.num = num;
            this.target = target;
        }

        public boolean runWhilePaused() {
            return false;
        }

        public boolean isDone() {
            return added >= num;
        }

        public void advance(float amount) {
            elapsed += amount * MathUtils.getRandomNumberInRange(0.6f, 1f);
            if (elapsed < 0.15f) return;

            elapsed = 0f;

            int curr = Math.round(MathUtils.getRandomNumberInRange(0.6f, 1f) * num / 10);
            if (curr < 1) curr = 1;

            Color color = new Color(166, 255, 0, 255);

            Vector2f vel = new Vector2f();

            if (target.getOrbit() != null &&
                    target.getCircularOrbitRadius() > 0 &&
                    target.getCircularOrbitPeriod() > 0 &&
                    target.getOrbitFocus() != null) {
                float circumference = 2f * (float) Math.PI * target.getCircularOrbitRadius();
                float speed = circumference / target.getCircularOrbitPeriod();

                float dir = Misc.getAngleInDegrees(target.getLocation(), target.getOrbitFocus().getLocation()) + 90f;
                vel = Misc.getUnitVectorAtDegreeAngle(dir);
                vel.scale(speed / Global.getSector().getClock().getSecondsPerDay());
            }

            for (int i = 0; i < curr; i++) {
                float glowSize = 25f + 75f * (float) Math.random();
                float angle = (float) Math.random() * 360f;
                float dist = (float) Math.sqrt(MathUtils.getRandomNumberInRange(0.01f, 0.95f)) * target.getRadius();

                float factor = 0.5f + 0.5f * (1f - (float) Math.sqrt(dist / target.getRadius()));
                glowSize *= factor;
                Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
                loc.scale(dist);
                Vector2f.add(loc, target.getLocation(), loc);

                Color c2 = Misc.scaleColor(color, factor);
                //c2 = color;
                Misc.addHitGlow(target.getContainingLocation(), loc, vel, glowSize, c2);
                added++;

                if (i == 0) {

                    dist = Misc.getDistance(loc, Global.getSector().getPlayerFleet().getLocation());
                    if (dist < HyperspaceTerrainPlugin.STORM_STRIKE_SOUND_RANGE) {

                        float volumeMult = 1f - (dist / HyperspaceTerrainPlugin.STORM_STRIKE_SOUND_RANGE);
                        volumeMult = (float) Math.sqrt(volumeMult);
                        volumeMult *= 0.1f * factor;

                        if (volumeMult > 0) {
                            Global.getSoundPlayer().playSound("mine_explosion", 1f, 1f * volumeMult, loc, Misc.ZERO);
                        }

                    }

                }
            }
        }
    }
}
