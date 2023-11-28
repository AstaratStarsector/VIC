package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AsteroidAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.shipsystems.vic_shockDischarger;
import data.scripts.util.MagicAnim;
import data.scripts.utilities.vic_graphicLibEffects;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static data.scripts.utilities.vic_color.randomizeColor;
import static data.scripts.utilities.vic_finders.damagableEnemiesInRangeWOAsteroids;
import static data.scripts.weapons.vic_alkonostExplosion.explosion;

public class vic_combatPlugin extends BaseEveryFrameCombatPlugin {

    static final public String DATA_KEY = "vic_combatPluginData";
    CombatEngineAPI engine;

    //Verlioka
    private final IntervalUtil timer = new IntervalUtil(0.25f, 0.25f);


    //FluxRapture
    final SpriteAPI OuterRing = Global.getSettings().getSprite("fx", "vic_fluxRaptureSuck");
    final SpriteAPI InnerRing = Global.getSettings().getSprite("fx", "vic_fluxRaptureZap");

    {
        float OuterRingRadius = vic_shockDischarger.suckRange;
        float InnerRingRadius = vic_shockDischarger.shockRange;

        OuterRing.setSize(OuterRingRadius, OuterRingRadius);
        InnerRing.setSize(InnerRingRadius, InnerRingRadius);
    }

    //Hunter Drive
    final float
            animDuration = 0.5f,
            waveSize = 800f;


    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData());
        CombatLayeredRenderingPlugin layerRenderer = new vic_layerRenderPlugin(DATA_KEY, engine);
        engine.addLayeredRenderingPlugin(layerRenderer);
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {

        if (engine == null) return;
        if (engine.isPaused()) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        HashMap<ShipAPI, Float> cloneMap;

        //animation advance
        final List<animationRenderData> animationRenderList = localData.animationRenderList;
        for (animationRenderData FX : animationRenderList) {
            FX.time += amount;
        }
        List<animationRenderData> cloneListRender = new ArrayList<>(animationRenderList);
        for (animationRenderData FX : cloneListRender) {
            if (FX.time >= FX.duration) {
                animationRenderList.remove(FX);
            }
        }

        //note:nawia
        final List<NawiaFxData> NawiaFxList = localData.NawiaFxList;
        for (NawiaFxData FX : NawiaFxList) {
            FX.timePast += amount;
        }
        List<NawiaFxData> cloneList = new ArrayList<>(NawiaFxList);
        for (NawiaFxData FX : cloneList) {
            if (FX.timePast >= FX.animTime) {
                NawiaFxList.remove(FX);
            }
        }

        //note:Defence Suppressor
        for (Map.Entry<ShipAPI, Float> entry : localData.defenceSuppressor.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            if (!entry.getKey().isAlive() || entry.getValue() - amount < 0) {
                MutableShipStatsAPI stats = entry.getKey().getMutableStats();

                stats.getShieldDamageTakenMult().unmodify("vic_defenceSuppressor");
                stats.getShieldUpkeepMult().unmodify("vic_defenceSuppressor");

                stats.getPhaseCloakActivationCostBonus().unmodify("vic_defenceSuppressor");
                stats.getPhaseCloakCooldownBonus().unmodify("vic_defenceSuppressor");
                stats.getPhaseCloakUpkeepCostBonus().unmodify("vic_defenceSuppressor");

                stats.getEffectiveArmorBonus().unmodify("vic_defenceSuppressor");
                stats.getMaxArmorDamageReduction().unmodify("vic_defenceSuppressor");
            } else {
                entry.getKey().setJitterShields(false);
                entry.getKey().setJitterUnder(entry.getKey(), new Color(106, 0, 255), 4, 8, 2);
                MutableShipStatsAPI stats = entry.getKey().getMutableStats();

                stats.getShieldDamageTakenMult().modifyMult("vic_defenceSuppressor", 1.5f);
                stats.getShieldUpkeepMult().modifyMult("vic_defenceSuppressor", 1.5f);
                stats.getDynamic().getStat(Stats.SHIELD_PIERCED_MULT).modifyMult("vic_defenceSuppressor", 1 + 0.5f);

                stats.getPhaseCloakActivationCostBonus().modifyMult("vic_defenceSuppressor", 1.5f);
                stats.getPhaseCloakCooldownBonus().modifyMult("vic_defenceSuppressor", 1.5f);
                stats.getPhaseCloakUpkeepCostBonus().modifyMult("vic_defenceSuppressor", 1.5f);

                stats.getEffectiveArmorBonus().modifyMult("vic_defenceSuppressor", 0.5f);
                stats.getMaxArmorDamageReduction().modifyFlat("vic_defenceSuppressor", -0.075f);
                if (entry.getKey() == engine.getPlayerShip())
                    engine.maintainStatusForPlayerShip("vic_defenceSuppressor_effect", "graphics/icons/hullsys/vic_defenceSuppressor.png", "Defence Suppressor", "Defenses systems suppressed", true);
            }
            entry.setValue(entry.getValue() - amount);
        }
        cloneMap = new HashMap<>(localData.defenceSuppressor);
        for (Map.Entry<ShipAPI, Float> entry : cloneMap.entrySet()) {
            if (entry.getValue() <= 0)
                localData.defenceSuppressor.remove(entry.getKey());
        }

        //NOTE:Flux Rapture
        for (Map.Entry<ShipAPI, Float> entry : localData.FluxRaptureRender.entrySet()) {
            entry.setValue(entry.getValue() + amount);
        }

        cloneMap = new HashMap<>(localData.FluxRaptureRender);
        for (Map.Entry<ShipAPI, Float> entry : cloneMap.entrySet()) {
            if (entry.getKey().isHulk() || !entry.getKey().isAlive())
                localData.FluxRaptureRender.remove(entry.getKey());
        }

        //NOTE:HunterDrive
        cloneMap = new HashMap<>(localData.hunterDriveAnimation);
        for (Map.Entry<ShipAPI, Float> entry : cloneMap.entrySet()) {
            if (!entry.getKey().isAlive() || entry.getValue() > animDuration)
                localData.hunterDriveAnimation.remove(entry.getKey());
        }

        for (Map.Entry<ShipAPI, Float> entry : localData.hunterDriveAnimation.entrySet()) {
            entry.setValue(entry.getValue() + amount);
        }

        //Hunter Drive effect
        String ID = "vic_hunterDriveWeb";
        for (Map.Entry<ShipAPI, Float> entry : localData.hunterDriveAnimation.entrySet()) {
            entry.setValue(entry.getValue() + amount);
        }

        for (Map.Entry<ShipAPI, HashMap<ShipAPI, Float>> entry : localData.hunterDriveTargets.entrySet()) {
            ShipAPI ship = entry.getKey();

            if (ship == engine.getPlayerShip()) {
                float count = entry.getValue().size();
                if (count > 0)
                    engine.maintainStatusForPlayerShip(ID + "hunter", "graphics/icons/hullsys/vic_hunterWeb.png", "Hunter's web", "Webbed ships: " + Math.round(count), false);
            }

            for (Map.Entry<ShipAPI, Float> entry2 : entry.getValue().entrySet()) {
                ShipAPI target = entry2.getKey();
                if (!target.isAlive() || entry2.getValue() - amount <= 0) {

                    MutableShipStatsAPI stats = target.getMutableStats();

                    stats.getMaxSpeed().unmodify(ID);
                    stats.getAcceleration().unmodify(ID);
                    stats.getDeceleration().unmodify(ID);
                    stats.getMaxTurnRate().unmodify(ID);
                    stats.getTurnAcceleration().unmodify(ID);

                } else {
                    MutableShipStatsAPI stats = target.getMutableStats();

                    stats.getMaxSpeed().modifyMult(ID, 0.75f);
                    stats.getAcceleration().modifyMult(ID, 0.75f);
                    stats.getDeceleration().modifyMult(ID, 0.75f);
                    stats.getMaxTurnRate().modifyMult(ID, 0.75f);
                    stats.getTurnAcceleration().modifyMult(ID, 0.75f);

                    target.getEngineController().extendFlame(ID, 0.75f, 0.75f, 0.75f);

                    if (target == engine.getPlayerShip())
                        engine.maintainStatusForPlayerShip(ID, "graphics/icons/hullsys/vic_hunterWeb.png", "EMP web " + (Math.round(entry2.getValue() * 10f) / 10f) + " s", "Speed reduced by 25%", true);

                }
                entry2.setValue(entry2.getValue() - amount);
                if (!entry2.getKey().isAlive() && ship.isAlive()) {
                    if (!ship.getSystem().isActive()) {
                        float CDReduction = 0;
                        float CD = Global.getSettings().getShipSystemSpec("vic_hunterDrive").getCooldown(ship.getMutableStats());
                        switch (target.getHullSize()) {
                            case FRIGATE:
                                CDReduction = CD * 0.33f;
                                break;
                            case DESTROYER:
                                CDReduction = CD * 0.5f;
                                break;
                            case CRUISER:
                                CDReduction = CD * 0.66f;
                                break;
                            case CAPITAL_SHIP:
                                CDReduction = CD * 0.80f;
                                break;
                        }
                        ship.getSystem().setCooldownRemaining(ship.getSystem().getCooldownRemaining() - CDReduction);
                    }
                    AddHunterBuff(ship, entry2.getKey().getHullSize());
                }
            }
        }

        for (Map.Entry<ShipAPI, hunterBuffData> entry : new HashMap<>(localData.hunterDriveBuffs).entrySet()) {
            ShipAPI ship = entry.getKey();
            hunterBuffData buff = entry.getValue();
            buff.duration -= amount;
            if (buff.duration <= 0) {
                ship.getMutableStats().getEnergyWeaponDamageMult().unmodify("vic_hunterBuff");
                ship.getMutableStats().getBallisticWeaponDamageMult().unmodify("vic_hunterBuff");
                ship.getMutableStats().getEnergyWeaponFluxCostMod().unmodify("vic_hunterBuff");
                ship.getMutableStats().getBallisticWeaponFluxCostMod().unmodify("vic_hunterBuff");
                ship.getMutableStats().getMaxSpeed().unmodify("vic_hunterBuff");
                ship.getMutableStats().getShieldDamageTakenMult().unmodify("vic_hunterBuff");
                localData.hunterDriveBuffs.remove(ship);
            } else {
                ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult("vic_hunterBuff", 1 + (0.1f * buff.stacks));
                ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult("vic_hunterBuff", 1 + (0.1f * buff.stacks));
                ship.getMutableStats().getEnergyWeaponFluxCostMod().modifyMult("vic_hunterBuff", 1 + (0.1f * buff.stacks));
                ship.getMutableStats().getBallisticWeaponFluxCostMod().modifyMult("vic_hunterBuff", 1 + (0.1f * buff.stacks));
                ship.getMutableStats().getMaxSpeed().modifyPercent("vic_hunterBuff", 5f * buff.stacks);
                ship.getMutableStats().getShieldDamageTakenMult().modifyMult("vic_hunterBuff", 1 + (0.05f * buff.stacks));

                if (ship == engine.getPlayerShip())
                    engine.maintainStatusForPlayerShip("vic_hunterBuff", "graphics/icons/hullsys/vic_hunterWeb.png", "Hunter's Pride " + (Math.round(buff.duration * 10f) / 10f) + " s", "Empower " + 20 * buff.stacks + "%", false);
            }
        }

        for (Map.Entry<ShipAPI, HashMap<ShipAPI, Float>> entry : new HashMap<>(localData.hunterDriveTargets).entrySet()) {
            ShipAPI ship = entry.getKey();
            for (Map.Entry<ShipAPI, Float> entry2 : new HashMap<>(entry.getValue()).entrySet()) {
                if (entry2.getValue() <= 0 || !entry2.getKey().isAlive()) {
                    localData.hunterDriveTargets.get(ship).remove(entry2.getKey());
                }
            }
        }


        //NOTE: Zlydzen
        //Shield
        for (Map.Entry<ShipAPI, ZlydzenTargetsDataShield> entry : localData.ZlydzenTargetsShield.entrySet()) {
            if (!entry.getKey().isAlive() || entry.getValue().power <= 0) {
                MutableShipStatsAPI stats = entry.getKey().getMutableStats();

                stats.getShieldDamageTakenMult().unmodify("vic_zlydzen_effect");
                stats.getShieldUpkeepMult().unmodify("vic_zlydzen_effect");

            } else {
                if (entry.getValue().wasAffectedLastCheck) {
                    entry.getValue().wasAffectedLastCheck = false;
                } else {
                    entry.getValue().advance(amount * entry.getKey().getMutableStats().getTimeMult().getModifiedValue());
                }
//                entry.getKey().setJitterShields(false);
//                entry.getKey().setJitterUnder(entry.getKey(), new Color(106, 0, 255), 4, 8, 2);
                MutableShipStatsAPI stats = entry.getKey().getMutableStats();
                float effectLevel = entry.getValue().power;

                stats.getShieldDamageTakenMult().modifyMult("vic_zlydzen_effect", 1 + (0.25f * effectLevel));
                stats.getShieldUpkeepMult().modifyMult("vic_zlydzen_effect", 1 + (0.25f * effectLevel));

                if (entry.getKey() == engine.getPlayerShip())
                    engine.maintainStatusForPlayerShip("vic_zlydzen_effect_shield", "graphics/icons/hullsys/vic_zlydzenEffect.png", "Disruptor Beam", "Shield eff reduced " + Math.round(effectLevel * 100) + "%", true);
            }
        }
        HashMap<ShipAPI, ZlydzenTargetsDataShield> cloneMapZ = new HashMap<>(localData.ZlydzenTargetsShield);
        for (Map.Entry<ShipAPI, ZlydzenTargetsDataShield> entry : cloneMapZ.entrySet()) {
            if (entry.getValue().power <= 0)
                localData.ZlydzenTargetsShield.remove(entry.getKey());
        }
        //Zlydzen
        //Armour
        for (Map.Entry<ShipAPI, ZlydzenTargetsDataArmour> entry : localData.ZlydzenTargetsArmour.entrySet()) {
            if (!entry.getKey().isAlive() || entry.getValue().power <= 0) {
                MutableShipStatsAPI stats = entry.getKey().getMutableStats();

                stats.getPhaseCloakUpkeepCostBonus().unmodify("vic_zlydzen_effect");

                stats.getEffectiveArmorBonus().unmodify("vic_zlydzen_effect");
                stats.getWeaponTurnRateBonus().unmodify("vic_zlydzen_effect");

            } else {
                if (entry.getValue().wasAffectedLastCheck) {
                    entry.getValue().wasAffectedLastCheck = false;
                } else {
                    float mult = 1;
                    if (entry.getKey().isPhased()) mult = 0.5f;
                    entry.getValue().advance(amount * entry.getKey().getMutableStats().getTimeMult().getModifiedValue() * mult);
                }
//                entry.getKey().setJitterShields(false);
//                entry.getKey().setJitterUnder(entry.getKey(), new Color(106, 0, 255), 4, 8, 2);
                MutableShipStatsAPI stats = entry.getKey().getMutableStats();
                float effectLevel = entry.getValue().power;

                stats.getPhaseCloakUpkeepCostBonus().modifyMult("vic_zlydzen_effect", 1 + (2f * effectLevel));

                stats.getEffectiveArmorBonus().modifyMult("vic_zlydzen_effect", 1 - (0.25f * effectLevel));
                stats.getWeaponTurnRateBonus().modifyMult("vic_zlydzen_effect", 1 - (0.25f * effectLevel));

                if (entry.getKey() == engine.getPlayerShip())
                    if (entry.getKey().getPhaseCloak() == null) {
                        engine.maintainStatusForPlayerShip("vic_zlydzen_effect_armour", "graphics/icons/hullsys/vic_zlydzenEffect.png", "Disruptor Beam", "Armour eff reduced " + Math.round(effectLevel * 100) + "%", true);
                    } else {
                        engine.maintainStatusForPlayerShip("vic_zlydzen_effect_armour", "graphics/icons/hullsys/vic_zlydzenEffect.png", "Disruptor Beam", "Armour and phase eff reduced " + Math.round(effectLevel * 100) + "%", true);
                    }
            }
        }
        HashMap<ShipAPI, ZlydzenTargetsDataArmour> cloneMap_ZlydzenTargetsArmour = new HashMap<>(localData.ZlydzenTargetsArmour);
        for (Map.Entry<ShipAPI, ZlydzenTargetsDataArmour> entry : cloneMap_ZlydzenTargetsArmour.entrySet()) {
            if (entry.getValue().power <= 0)
                localData.ZlydzenTargetsArmour.remove(entry.getKey());
        }

        //NOTE:ArcaneMissiles
        for (Map.Entry<DamagingProjectileAPI, ArcaneMissilesData> entry : localData.ArcaneMissiles.entrySet()) {
            ArcaneMissilesData data = entry.getValue();
            if (data.time < data.timeBeforeCurving) {
                data.time += amount;
            } else if (data.rotate) {
                DamagingProjectileAPI proj = data.proj;
                if (data.rotationSpeed == null) {
                    proj.getVelocity().scale(data.speedUpOnCurving);
                    float flightTime = MathUtils.getDistance(proj.getLocation(), data.finalPoint) / Math.abs(MathUtils.getDistance(new Vector2f(), proj.getVelocity()));
                    float angle = MathUtils.getShortestRotation(proj.getFacing(), VectorUtils.getAngle(proj.getLocation(), data.finalPoint));
                    data.rotationSpeed = (angle / flightTime) * (2f);
                    //engine.addHitParticle(proj.getLocation(), new Vector2f(), 30, 1f, 0.35f, new Color(2, 225, 255, 255));

                    //turn instantly if rotation speed too high
                    if (Math.abs(data.rotationSpeed) >= 800) {
                        VectorUtils.rotate(proj.getVelocity(), angle, proj.getVelocity());
                        proj.setFacing(angle + proj.getFacing());
                        data.rotate = false;
                        //engine.addFloatingText(proj.getLocation(), Math.round(data.rotationSpeed) + "", 20, Color.WHITE, null, 0, 0);
                    } else {
                        float angle2 = data.rotationSpeed * data.timeBeforeCurving * (1 - data.rangeBeforeCurving) * 0.25f;
                        VectorUtils.rotate(proj.getVelocity(), angle2, proj.getVelocity());
                        proj.setFacing(angle2 + proj.getFacing());

                        angle = MathUtils.getShortestRotation(proj.getFacing(), VectorUtils.getAngle(proj.getLocation(), data.finalPoint));
                        data.rotationSpeed = (angle / flightTime) * (2f);
                        proj.getVelocity().scale(1 + Math.abs(angle * 0.0025f));
                    }

                    Global.getSoundPlayer().playSound(
                            "vic_vila_pulse",
                            1f,
                            1f,
                            proj.getLocation(),
                            new Vector2f()
                    );

                    float sizeMult = MathUtils.getRandomNumberInRange(0.8f, 1.2f);

                    MagicRender.battlespace(
                            Global.getSettings().getSprite("fx", "vic_laidlawExplosion3"),
                            proj.getLocation(),
                            new Vector2f(),
                            new Vector2f(60 * sizeMult, 60 * sizeMult),
                            new Vector2f(40 * sizeMult, 80 * sizeMult),
                            //angle,
                            180f + proj.getFacing(),
                            0,
                            new Color(255, 225, 225, 255),
                            true,
                            0.0f,
                            0.0f,
                            0.35f * MathUtils.getRandomNumberInRange(0.8f, 1.2f)
                    );

                    sizeMult = MathUtils.getRandomNumberInRange(0.8f, 1.2f);
                    MagicRender.battlespace(
                            Global.getSettings().getSprite("fx", "vic_laidlawExplosion3"),
                            proj.getLocation(),
                            new Vector2f(),
                            new Vector2f(60 * sizeMult, 60 * sizeMult),
                            new Vector2f(300 * sizeMult, 600 * sizeMult),
                            //angle,
                            180f + proj.getFacing(),
                            0,
                            new Color(255, 225, 225, 175),
                            true,
                            0.0f,
                            0.0f,
                            0.2f * MathUtils.getRandomNumberInRange(0.8f, 1.2f)
                    );

                    //localData.animationRenderList.add(new animationRenderData("vic_vilaBlast", 5, 0.2f, proj.getFacing(), proj.getLocation(), new Vector2f(32 * sizeMult, 32 * sizeMult), true));
                } else {
                    float angle = data.rotationSpeed * amount;
                    if (Math.abs(angle) > 0) {
                        VectorUtils.rotate(proj.getVelocity(), angle, proj.getVelocity());
                        proj.setFacing(angle + proj.getFacing());
                    }
                    if (MathUtils.isWithinRange(proj.getLocation(), data.finalPoint, 30))
                        data.rotate = false;
                }
            }
        }
        HashMap<DamagingProjectileAPI, ArcaneMissilesData> cloneMapA = new HashMap<>(localData.ArcaneMissiles);
        for (Map.Entry<DamagingProjectileAPI, ArcaneMissilesData> entry : cloneMapA.entrySet()) {
            if (!engine.isEntityInPlay(entry.getKey()))
                localData.ArcaneMissiles.remove(entry.getKey());
        }


        //NOTE: sprite render
        for (spriteRender renderData : localData.spritesRender) {
            if (renderData.timePast <= renderData.fadeIn) {
                renderData.alphaMulti = renderData.timePast / renderData.fadeIn;
            } else if (renderData.timePast - renderData.fadeIn <= renderData.timeFull) {
                renderData.alphaMulti = 1;
            } else if (renderData.timePast - renderData.fadeIn - renderData.timeFull <= renderData.fadeOut) {
                renderData.alphaMulti = 1 - (renderData.timePast - renderData.fadeIn - renderData.timeFull) / renderData.fadeOut;
            }
            renderData.sizeMulti = (float) (1f - Math.pow(1f - renderData.timePast / renderData.totalTime, 2));

            renderData.sprite.setAlphaMult(renderData.alphaMulti);
            renderData.sprite.setAngle(renderData.angle + renderData.spin * renderData.timePast);
            //renderData.sprite.setSize(renderData.startingSize.x, renderData.startingSize.y);
            renderData.sprite.setSize(renderData.startingSize.x + renderData.grow.x * renderData.sizeMulti, renderData.startingSize.y + renderData.grow.y * renderData.sizeMulti);

            renderData.timePast += amount;
        }

        for (spriteRender renderData : new ArrayList<>(localData.spritesRender)) {
            if (renderData.timePast >= renderData.totalTime) localData.spritesRender.remove(renderData);
        }

        //NOTE: quantum lunge
        for (Map.Entry<ShipAPI, Float> entry : localData.quantumLungeSpeedBoost.entrySet()) {
            entry.setValue(entry.getValue() - amount);
            MutableShipStatsAPI stats = entry.getKey().getMutableStats();
            stats.getMaxTurnRate().modifyMult("vic_quantumLunge", 1.5f);
            stats.getMaxTurnRate().modifyFlat("vic_quantumLunge", 7f);
            stats.getTurnAcceleration().modifyMult("vic_quantumLunge", 3f);
            if (engine.getPlayerShip().equals(entry.getKey())) {
                engine.maintainStatusForPlayerShip("vic_quantumLungeBoost", "graphics/icons/hullsys/maneuvering_jets.png", "Quantum Lunge Speed Boost", "Maneuverability increased by 150%", false);
            }
        }

        for (Map.Entry<ShipAPI, Float> entry : new HashMap<>(localData.quantumLungeSpeedBoost).entrySet()) {
            if (entry.getValue() <= 0) {
                MutableShipStatsAPI stats = entry.getKey().getMutableStats();
                stats.getMaxTurnRate().unmodify("vic_quantumLunge");
                stats.getMaxTurnRate().unmodify("vic_quantumLunge");
                stats.getTurnAcceleration().unmodify("vic_quantumLunge");
                localData.quantumLungeSpeedBoost.remove(entry.getKey());
            }
        }

        //NOTE: Nawia Rain
        for (NawiaRainData data : new ArrayList<>(localData.NawiaRain)) {
            if (data.ship == null || !data.ship.isAlive()) continue;
            data.amount += amount * data.ship.getMutableStats().getBallisticRoFMult().getModifiedValue();
            while (data.amount >= data.timePerProj) {
                data.amount -= data.timePerProj;
                data.projesSpawned++;
                data.ship.getFluxTracker().increaseFlux(data.weapon.getFluxCostToFire() * 0.5f * (1 / data.projMax), false);
                float toDaysRandom = MathUtils.getRandomNumberInRange(-15, 15);
                float toDaysRandom2 = MathUtils.getRandomNumberInRange(0.8f, 1.2f);
                Vector2f Dir = Misc.getUnitVectorAtDegreeAngle(data.projectile.getFacing() + toDaysRandom);
                Vector2f SpawnPoint = new Vector2f(data.hitPoint.x + Dir.x * -300 * toDaysRandom2, data.hitPoint.y + Dir.y * -300 * toDaysRandom2);

                DamagingProjectileAPI proj = (DamagingProjectileAPI) engine.spawnProjectile(data.projectile.getSource(),
                        data.projectile.getWeapon(),
                        "vic_nawia_sub",
                        SpawnPoint,
                        data.projectile.getFacing() + (toDaysRandom + MathUtils.getRandomNumberInRange(-5, 5)),
                        data.targetVelocity);

                Global.getSoundPlayer().playSound(
                        "vic_nawia_spawn",
                        1f,
                        0.5f,
                        SpawnPoint,
                        new Vector2f()
                );


                //if (MagicRender.screenCheck (0.5f, SpawnPoint)) engine.addPlugin(new vic_nawiaVisuals(SpawnPoint, proj.getFacing()));
                if (MagicRender.screenCheck(0.5f, SpawnPoint))
                    vic_combatPlugin.AddNawiaFX(SpawnPoint, proj.getFacing());
                if (MagicRender.screenCheck(0.5f, SpawnPoint)) {

                    float animTime = MathUtils.getRandomNumberInRange(0.5f, 0.6f);

                    engine.addSmoothParticle(
                            SpawnPoint,
                            new Vector2f(),
                            MathUtils.getRandomNumberInRange(20, 30),
                            1,
                            animTime,
                            new Color(MathUtils.getRandomNumberInRange(130, 180), MathUtils.getRandomNumberInRange(20, 60), 255, 255)
                    );
                    for (float I = 0; I < MathUtils.getRandomNumberInRange(4, 8); I++) {

                        Vector2f move = Misc.getUnitVectorAtDegreeAngle(MathUtils.getRandomNumberInRange(-20, 20) + proj.getFacing());
                        engine.addHitParticle(
                                SpawnPoint,
                                new Vector2f(move.x * MathUtils.getRandomNumberInRange(25, 125), move.y * MathUtils.getRandomNumberInRange(25, 125)),
                                MathUtils.getRandomNumberInRange(5, 20),
                                MathUtils.getRandomNumberInRange(0.8f, 1f),
                                animTime * MathUtils.getRandomNumberInRange(0.5f, 1.5f),
                                new Color(MathUtils.getRandomNumberInRange(90, 180), MathUtils.getRandomNumberInRange(20, 100), 255, 255)
                        );

                    }
                }
                if (data.projesSpawned >= data.projMax) break;
            }
        }
        for (NawiaRainData data : new ArrayList<>(localData.NawiaRain)) {
            if (data.projesSpawned >= data.projMax || !data.ship.isAlive()) localData.NawiaRain.remove(data);
        }

        //NOTE: XL Laidlaw
        for (XLLaidlawProjData data : new ArrayList<>(localData.XLLaidlawProjes)) {
            DamagingProjectileAPI projectile = data.proj;

            float distanceTraveled = projectile.getMoveSpeed() * amount;

            List<Vector2f> damagePoints = new ArrayList<>();

            if (!projectile.getVelocity().equals(new Vector2f())){
                while (data.distanceCounter + distanceTraveled >= 75f) {
                    float leftToCover = 75 - data.distanceCounter;
                    Vector2f damagePoint = (Vector2f) new Vector2f(projectile.getVelocity()).normalise().scale(leftToCover);
                    Vector2f.add(damagePoint, data.locLastFrame, damagePoint);
                    damagePoints.add(damagePoint);
                    distanceTraveled -= 75 - data.distanceCounter;
                    data.distanceCounter = 0;
                }
            }
            data.distanceCounter += distanceTraveled;

            //get all targets in between loc last frame and current loc
            Vector2f searchPoint = (Vector2f) Vector2f.add(projectile.getLocation(), data.locLastFrame, null).scale(0.5f);
            List<CombatEntityAPI> targets = CombatUtils.getEntitiesWithinRange(searchPoint, (projectile.getMoveSpeed() * amount + projectile.getProjectileSpec().getWidth()) * 0.5f);
            //MathUtils.getNearestPointOnLine()


            boolean shieldHit = false;
            boolean light = false;

            Vector2f collisionPoint = null;

            targets.remove(projectile.getSource());


            DamagingExplosionSpec explosion = new DamagingExplosionSpec(0.05f,
                    150,
                    75f,
                    0,
                    0,
                    CollisionClass.PROJECTILE_FF,
                    CollisionClass.PROJECTILE_FIGHTER,
                    3,
                    3,
                    1f,
                    15,
                    new Color(33, 255, 122, 255),
                    new Color(255, 150, 35, 255)
            );

            DamagingExplosionSpec explosion2 = new DamagingExplosionSpec(0.05f,
                    75,
                    32.5f,
                    0,
                    0,
                    CollisionClass.PROJECTILE_FF,
                    CollisionClass.PROJECTILE_FIGHTER,
                    3,
                    3,
                    1f,
                    15,
                    new Color(33, 255, 122, 255),
                    new Color(255, 150, 35, 255)
            );

            //detect if hull hit or shield hit
            for (CombatEntityAPI target : targets) {
                if (target instanceof DamagingProjectileAPI || target instanceof BattleObjectiveAPI) continue;

                if (target instanceof ShipAPI) {
                    ShipAPI ship = (ShipAPI) target;
                    if (ship.getOwner() == projectile.getOwner() && (ship.isFighter() || ship.isDrone())
                            || ship.getCollisionClass() == CollisionClass.NONE) {
                        continue;
                    }
                    if (ship.getShield() != null
                            && ship.getShield().isOn()
                            && MathUtils.isWithinRange(projectile.getLocation(), ((ShipAPI) target).getShieldCenterEvenIfNoShield(), ((ShipAPI) target).getShieldRadiusEvenIfNoShield() + (projectile.getProjectileSpec().getWidth() * 0.5f))
                            && ship.getShield().isWithinArc(projectile.getLocation())) {
                        shieldHit = true;
                        float angle = VectorUtils.getAngle(ship.getShield().getLocation(), projectile.getLocation());
                        collisionPoint = MathUtils.getPointOnCircumference(ship.getShield().getLocation(), ship.getShield().getRadius(), angle);
                    }
                }
                if (!shieldHit) {
                    if (target.getExactBounds() == null) {
                        collisionPoint = projectile.getLocation();
                    } else {
                        collisionPoint = CollisionUtils.getCollisionPoint(data.locLastFrame, projectile.getLocation(), target);
                    }

                    if (collisionPoint == null) {
                        if (target instanceof ShipAPI && ((ShipAPI) target).isFighter()) {
                            collisionPoint = target.getLocation();
                        } else if (CollisionUtils.isPointWithinBounds(projectile.getLocation(), target)) {
                            collisionPoint = projectile.getLocation();
                        }
                    }
                }
                if (collisionPoint == null) {
                    break;
                }
                if (shieldHit && !data.damagedAlready.contains(target)) {
                    engine.applyDamage(target,
                            collisionPoint,
                            projectile.getDamageAmount(),
                            DamageType.FRAGMENTATION,
                            projectile.getEmpAmount(),
                            false,
                            true,
                            projectile.getSource());
                    //todo: go over it and fix


                    explosion.setDamageType(DamageType.FRAGMENTATION);
                    explosion.setShowGraphic(false);
                    explosion.setMinDamage(projectile.getDamageAmount() * 0.05f);
                    explosion.setMaxDamage(projectile.getDamageAmount() * 0.1f);
                    DamagingProjectileAPI exsp = engine.spawnDamagingExplosion(explosion, projectile.getSource(), collisionPoint);
                    exsp.addDamagedAlready(target);


                    Global.getSoundPlayer().playSound("vic_xl_laidlaw_explosion", 1f + MathUtils.getRandomNumberInRange(-0.1f, 0.1f), 1f, collisionPoint, new Vector2f());


                    if (Global.getSettings().getModManager().isModEnabled("shaderLib")) {
                        light = true;
                    }

                    WaveDistortion wave = new WaveDistortion(collisionPoint, new Vector2f(0, 0));
                    wave.setIntensity(6f);
                    wave.setSize(300f);
                    wave.flip(true);
                    wave.setLifetime(0f);
                    wave.fadeOutIntensity(0.35f);
                    wave.setLocation(projectile.getLocation());
                    DistortionShader.addDistortion(wave);

                    if (light) {
                        vic_graphicLibEffects.CustomRippleDistortion(
                                collisionPoint,
                                new Vector2f(0, 0),
                                350,
                                4,
                                false,
                                0,
                                360,
                                1f,
                                0.1f,
                                0.25f,
                                0.5f,
                                0.6f,
                                0f
                        );
                    }


                    engine.spawnExplosion(
                            collisionPoint,
                            new Vector2f(0, 0),
                            new Color(255, 255, 255, 255),
                            40f,
                            0.5f);

                    engine.spawnExplosion(
                            collisionPoint,
                            new Vector2f(0, 0),
                            new Color(0, 255, 225, 125),
                            80f,
                            0.75f);

                    engine.addSmoothParticle(
                            collisionPoint,
                            new Vector2f(),
                            500,
                            2f,
                            0.5f,
                            new Color(158, 255, 255, 125));

                    engine.addHitParticle(
                            collisionPoint,
                            new Vector2f(),
                            800,
                            2f,
                            0.35f,
                            new Color(158, 255, 255, 255));

                    engine.addHitParticle(
                            collisionPoint,
                            new Vector2f(),
                            1200,
                            2f,
                            0.2f,
                            new Color(195, 255, 255, 255));


                    MagicRender.battlespace(
                            Global.getSettings().getSprite("fx", "vic_laidlawExplosion2"),
                            collisionPoint,
                            new Vector2f(),
                            new Vector2f(120, 120),
                            new Vector2f(450, 450),
                            //angle,
                            360 * (float) Math.random(),
                            0,
                            new Color(255, 200, 200, 255),
                            true,
                            0,
                            0.1f,
                            0.15f
                    );
                    MagicRender.battlespace(
                            Global.getSettings().getSprite("fx", "vic_laidlawExplosion2"),
                            collisionPoint,
                            new Vector2f(),
                            new Vector2f(155, 155),
                            new Vector2f(225, 225),
                            //angle,
                            360 * (float) Math.random(),
                            0,
                            new Color(255, 225, 225, 175),
                            true,
                            0.2f,
                            0.0f,
                            0.4f
                    );
                    MagicRender.battlespace(
                            Global.getSettings().getSprite("fx", "vic_laidlawExplosion"),
                            collisionPoint,
                            new Vector2f(),
                            new Vector2f(250, 250),
                            new Vector2f(75, 75),
                            //angle,
                            360 * (float) Math.random(),
                            0,
                            new Color(255, 255, 255, 125),
                            true,
                            0.4f,
                            0.0f,
                            2f
                    );

                    MagicRender.battlespace(
                            Global.getSettings().getSprite("fx", "vic_stolas_emp_secondary"),
                            collisionPoint,
                            new Vector2f(),
                            new Vector2f(200, 200),
                            new Vector2f(750, 750),
                            //angle,
                            360 * (float) Math.random(),
                            0,
                            new Color(95, 255, 231, 25),
                            true,
                            0,
                            0,
                            0.5f
                    );

                    MagicRender.battlespace(
                            Global.getSettings().getSprite("fx", "vic_stolas_emp_secondary"),
                            collisionPoint,
                            new Vector2f(),
                            new Vector2f(200, 200),
                            new Vector2f(750, 750),
                            //angle,
                            360 * (float) Math.random(),
                            0,
                            new Color(255, 119, 0, 25),
                            true,
                            0,
                            0,
                            1.25f
                    );

                    Vector2f nebulaSpeed1 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(projectile.getAngularVelocity() + MathUtils.getRandomNumberInRange(0f, 90f)).scale(MathUtils.getRandomNumberInRange(5f, 15f));
                    Vector2f nebulaSpeed2 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(projectile.getAngularVelocity() + MathUtils.getRandomNumberInRange(90f, 180f)).scale(MathUtils.getRandomNumberInRange(5f, 15f));
                    Vector2f nebulaSpeed3 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(projectile.getAngularVelocity() + MathUtils.getRandomNumberInRange(180f, 270f)).scale(MathUtils.getRandomNumberInRange(5f, 15f));
                    Vector2f nebulaSpeed4 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(projectile.getAngularVelocity() + MathUtils.getRandomNumberInRange(270f, 360f)).scale(MathUtils.getRandomNumberInRange(5f, 15f));
                    Vector2f nebulaSpeed5 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(projectile.getAngularVelocity()).scale(0f);

                    Global.getCombatEngine().addNebulaSmokeParticle(collisionPoint, nebulaSpeed1, 80f, 2.5f, 0.2f, 0.2f, (2f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(153, 95, 67, 125));
                    Global.getCombatEngine().addNebulaSmokeParticle(collisionPoint, nebulaSpeed2, 80f, 2.5f, 0.2f, 0.2f, (2f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(83, 51, 25, 125));
                    Global.getCombatEngine().addNebulaSmokeParticle(collisionPoint, nebulaSpeed3, 80f, 2.5f, 0.2f, 0.2f, (2f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(111, 56, 7, 125));
                    Global.getCombatEngine().addNebulaSmokeParticle(collisionPoint, nebulaSpeed4, 80f, 2.5f, 0.2f, 0.2f, (2f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(134, 107, 53, 125));
                    Global.getCombatEngine().addNebulaSmokeParticle(collisionPoint, nebulaSpeed5, 80f, 2f, 0.2f, 0.2f, 0.4f, new Color(172, 255, 230, 173));
                    //endtodo
                    if (target instanceof ShipAPI && !((ShipAPI) target).isFighter()) engine.removeEntity(projectile);
                } else {
                    if (!data.damagedAlready.contains(target)) {
                        //Get total armor on hit point by adding armor of all cells
                        float totalArmor = 0f;
                        if (target instanceof ShipAPI) {
                            ArmorGridAPI grid = ((ShipAPI) target).getArmorGrid();
                            int[] cell = grid.getCellAtLocation(collisionPoint);
                            if (cell == null) return;
                            int gridWidth = grid.getGrid().length;
                            int gridHeight = grid.getGrid()[0].length;
                            for (int i = -2; i <= 2; i++) {
                                for (int j = -2; j <= 2; j++) {
                                    if ((i == 2 || i == -2) && (j == 2 || j == -2)) continue; // skip corners

                                    int cx = cell[0] + i;
                                    int cy = cell[1] + j;

                                    if (cx < 0 || cx >= gridWidth || cy < 0 || cy >= gridHeight) continue;

                                    float mult = 0.5f;
                                    if ((i <= 1 && i >= -1 && j <= 1 && j >= -1)) { // S hits
                                        mult = 1f;
                                    }

                                    float armorInCell = grid.getArmorValue(cx, cy) * mult;
                                    totalArmor += armorInCell;
                                }
                            }
                        }
                        if (totalArmor >= 1500) {
                            engine.applyDamage(target,
                                    collisionPoint,
                                    projectile.getDamageAmount(),
                                    DamageType.FRAGMENTATION,
                                    0,
                                    true,
                                    true,
                                    projectile.getSource());
                            engine.removeEntity(projectile);
                            //Global.getLogger(vic_combatPlugin.class).info("no pen hit damage: " + projectile.getDamageAmount());
//todo: fix
                            float force = (projectile.getDamageAmount() * 0.05f);
                            CombatUtils.applyForce(target, projectile.getVelocity(), force);

                            explosion.setDamageType(DamageType.FRAGMENTATION);
                            explosion.setShowGraphic(false);
                            explosion.setMinDamage(projectile.getDamageAmount() * 0.05f);
                            explosion.setMaxDamage(projectile.getDamageAmount() * 0.1f);
                            engine.spawnDamagingExplosion(explosion, projectile.getSource(), collisionPoint);


                            Global.getSoundPlayer().playSound("vic_xl_laidlaw_explosion", 1f + MathUtils.getRandomNumberInRange(-0.1f, 0.1f), 1f, collisionPoint, new Vector2f());


                            if (Global.getSettings().getModManager().isModEnabled("shaderLib")) {
                                light = true;
                            }

                            WaveDistortion wave = new WaveDistortion(collisionPoint, new Vector2f(0, 0));
                            wave.setIntensity(6f);
                            wave.setSize(300f);
                            wave.flip(true);
                            wave.setLifetime(0f);
                            wave.fadeOutIntensity(0.35f);
                            wave.setLocation(projectile.getLocation());
                            DistortionShader.addDistortion(wave);

                            if (light) {
                                vic_graphicLibEffects.CustomRippleDistortion(
                                        collisionPoint,
                                        new Vector2f(0, 0),
                                        350,
                                        4,
                                        false,
                                        0,
                                        360,
                                        1f,
                                        0.1f,
                                        0.25f,
                                        0.5f,
                                        0.6f,
                                        0f
                                );
                            }


                            engine.spawnExplosion(
                                    collisionPoint,
                                    new Vector2f(0, 0),
                                    new Color(255, 255, 255, 255),
                                    40f,
                                    0.5f);

                            engine.spawnExplosion(
                                    collisionPoint,
                                    new Vector2f(0, 0),
                                    new Color(0, 255, 225, 125),
                                    80f,
                                    0.75f);

                            engine.addSmoothParticle(
                                    collisionPoint,
                                    new Vector2f(),
                                    500,
                                    2f,
                                    0.5f,
                                    new Color(158, 255, 255, 125));

                            engine.addHitParticle(
                                    collisionPoint,
                                    new Vector2f(),
                                    800,
                                    2f,
                                    0.35f,
                                    new Color(158, 255, 255, 255));

                            engine.addHitParticle(
                                    collisionPoint,
                                    new Vector2f(),
                                    1200,
                                    2f,
                                    0.2f,
                                    new Color(195, 255, 255, 255));


                            MagicRender.battlespace(
                                    Global.getSettings().getSprite("fx", "vic_laidlawExplosion2"),
                                    collisionPoint,
                                    new Vector2f(),
                                    new Vector2f(120, 120),
                                    new Vector2f(450, 450),
                                    //angle,
                                    360 * (float) Math.random(),
                                    0,
                                    new Color(255, 200, 200, 255),
                                    true,
                                    0,
                                    0.1f,
                                    0.15f
                            );
                            MagicRender.battlespace(
                                    Global.getSettings().getSprite("fx", "vic_laidlawExplosion2"),
                                    collisionPoint,
                                    new Vector2f(),
                                    new Vector2f(155, 155),
                                    new Vector2f(225, 225),
                                    //angle,
                                    360 * (float) Math.random(),
                                    0,
                                    new Color(255, 225, 225, 175),
                                    true,
                                    0.2f,
                                    0.0f,
                                    0.4f
                            );
                            MagicRender.battlespace(
                                    Global.getSettings().getSprite("fx", "vic_laidlawExplosion"),
                                    collisionPoint,
                                    new Vector2f(),
                                    new Vector2f(250, 250),
                                    new Vector2f(75, 75),
                                    //angle,
                                    360 * (float) Math.random(),
                                    0,
                                    new Color(255, 255, 255, 125),
                                    true,
                                    0.4f,
                                    0.0f,
                                    2f
                            );

                            MagicRender.battlespace(
                                    Global.getSettings().getSprite("fx", "vic_stolas_emp_secondary"),
                                    collisionPoint,
                                    new Vector2f(),
                                    new Vector2f(200, 200),
                                    new Vector2f(750, 750),
                                    //angle,
                                    360 * (float) Math.random(),
                                    0,
                                    new Color(95, 255, 231, 25),
                                    true,
                                    0,
                                    0,
                                    0.5f
                            );

                            MagicRender.battlespace(
                                    Global.getSettings().getSprite("fx", "vic_stolas_emp_secondary"),
                                    collisionPoint,
                                    new Vector2f(),
                                    new Vector2f(200, 200),
                                    new Vector2f(750, 750),
                                    //angle,
                                    360 * (float) Math.random(),
                                    0,
                                    new Color(255, 119, 0, 25),
                                    true,
                                    0,
                                    0,
                                    1.25f
                            );

                            Vector2f nebulaSpeed1 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(projectile.getAngularVelocity() + MathUtils.getRandomNumberInRange(0f, 90f)).scale(MathUtils.getRandomNumberInRange(5f, 15f));
                            Vector2f nebulaSpeed2 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(projectile.getAngularVelocity() + MathUtils.getRandomNumberInRange(90f, 180f)).scale(MathUtils.getRandomNumberInRange(5f, 15f));
                            Vector2f nebulaSpeed3 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(projectile.getAngularVelocity() + MathUtils.getRandomNumberInRange(180f, 270f)).scale(MathUtils.getRandomNumberInRange(5f, 15f));
                            Vector2f nebulaSpeed4 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(projectile.getAngularVelocity() + MathUtils.getRandomNumberInRange(270f, 360f)).scale(MathUtils.getRandomNumberInRange(5f, 15f));
                            Vector2f nebulaSpeed5 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(projectile.getAngularVelocity()).scale(0f);

                            Global.getCombatEngine().addNebulaSmokeParticle(collisionPoint, nebulaSpeed1, 80f, 2.5f, 0.2f, 0.2f, (2f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(153, 95, 67, 125));
                            Global.getCombatEngine().addNebulaSmokeParticle(collisionPoint, nebulaSpeed2, 80f, 2.5f, 0.2f, 0.2f, (2f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(83, 51, 25, 125));
                            Global.getCombatEngine().addNebulaSmokeParticle(collisionPoint, nebulaSpeed3, 80f, 2.5f, 0.2f, 0.2f, (2f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(111, 56, 7, 125));
                            Global.getCombatEngine().addNebulaSmokeParticle(collisionPoint, nebulaSpeed4, 80f, 2.5f, 0.2f, 0.2f, (2f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(134, 107, 53, 125));
                            Global.getCombatEngine().addNebulaSmokeParticle(collisionPoint, nebulaSpeed5, 80f, 2f, 0.2f, 0.2f, 0.4f, new Color(172, 255, 230, 173));
//endtodo
                        } else {
                            engine.applyDamage(target,
                                    collisionPoint,
                                    projectile.getDamageAmount() * 0.45f,
                                    DamageType.FRAGMENTATION,
                                    0,
                                    true,
                                    true,
                                    projectile.getSource());
                            data.damagedAlready.add(target);
                            //Global.getLogger(vic_combatPlugin.class).info("initial hit damage: " + projectile.getDamageAmount() * 0.35f);
//todo: fix
                            float force = (projectile.getDamageAmount() * 0.025f);
                            CombatUtils.applyForce(target, projectile.getVelocity(), force);

                            explosion2.setDamageType(DamageType.FRAGMENTATION);
                            explosion2.setShowGraphic(false);
                            explosion2.setMinDamage(projectile.getDamageAmount() * 0.025f);
                            explosion2.setMaxDamage(projectile.getDamageAmount() * 0.05f);
                            engine.spawnDamagingExplosion(explosion2, projectile.getSource(), collisionPoint);


                            Global.getSoundPlayer().playSound("vic_xl_laidlaw_penetration", 1f + MathUtils.getRandomNumberInRange(-0.1f, 0.1f), 1f, collisionPoint, new Vector2f());


                            if (Global.getSettings().getModManager().isModEnabled("shaderLib")) {
                                light = true;
                            }

                            WaveDistortion wave = new WaveDistortion(collisionPoint, new Vector2f(0, 0));
                            wave.setIntensity(4f);
                            wave.setSize(200f);
                            wave.flip(true);
                            wave.setLifetime(0f);
                            wave.fadeOutIntensity(0.35f);
                            wave.setLocation(projectile.getLocation());
                            DistortionShader.addDistortion(wave);

                            if (light) {
                                vic_graphicLibEffects.CustomRippleDistortion(
                                        collisionPoint,
                                        new Vector2f(0, 0),
                                        240,
                                        3,
                                        false,
                                        0,
                                        360,
                                        1f,
                                        0.1f,
                                        0.25f,
                                        0.5f,
                                        0.6f,
                                        0f
                                );
                            }


                            engine.spawnExplosion(
                                    collisionPoint,
                                    new Vector2f(0, 0),
                                    new Color(255, 255, 255, 255),
                                    25f,
                                    0.5f);

                            engine.spawnExplosion(
                                    collisionPoint,
                                    new Vector2f(0, 0),
                                    new Color(0, 255, 225, 125),
                                    50f,
                                    0.75f);

                            engine.addSmoothParticle(
                                    collisionPoint,
                                    new Vector2f(),
                                    312.5f,
                                    2f,
                                    0.5f,
                                    new Color(158, 255, 255, 125));

                            engine.addHitParticle(
                                    collisionPoint,
                                    new Vector2f(),
                                    500,
                                    2f,
                                    0.35f,
                                    new Color(158, 255, 255, 255));

                            engine.addHitParticle(
                                    collisionPoint,
                                    new Vector2f(),
                                    750,
                                    2f,
                                    0.2f,
                                    new Color(195, 255, 255, 255));


                            MagicRender.battlespace(
                                    Global.getSettings().getSprite("fx", "vic_laidlawExplosion2"),
                                    collisionPoint,
                                    new Vector2f(),
                                    new Vector2f(75, 75),
                                    new Vector2f(280, 280),
                                    //angle,
                                    360 * (float) Math.random(),
                                    0,
                                    new Color(255, 200, 200, 255),
                                    true,
                                    0,
                                    0.1f,
                                    0.15f
                            );
                            MagicRender.battlespace(
                                    Global.getSettings().getSprite("fx", "vic_laidlawExplosion2"),
                                    collisionPoint,
                                    new Vector2f(),
                                    new Vector2f(95, 95),
                                    new Vector2f(145, 145),
                                    //angle,
                                    360 * (float) Math.random(),
                                    0,
                                    new Color(255, 225, 225, 175),
                                    true,
                                    0.2f,
                                    0.0f,
                                    0.4f
                            );
                            MagicRender.battlespace(
                                    Global.getSettings().getSprite("fx", "vic_laidlawExplosion"),
                                    collisionPoint,
                                    new Vector2f(),
                                    new Vector2f(155, 155),
                                    new Vector2f(47f, 47f),
                                    //angle,
                                    360 * (float) Math.random(),
                                    0,
                                    new Color(255, 255, 255, 125),
                                    true,
                                    0.4f,
                                    0.0f,
                                    2f
                            );

                            MagicRender.battlespace(
                                    Global.getSettings().getSprite("fx", "vic_stolas_emp_secondary"),
                                    collisionPoint,
                                    new Vector2f(),
                                    new Vector2f(125, 125),
                                    new Vector2f(405, 405),
                                    //angle,
                                    360 * (float) Math.random(),
                                    0,
                                    new Color(95, 255, 231, 25),
                                    true,
                                    0,
                                    0,
                                    0.5f
                            );

                            MagicRender.battlespace(
                                    Global.getSettings().getSprite("fx", "vic_stolas_emp_secondary"),
                                    collisionPoint,
                                    new Vector2f(),
                                    new Vector2f(125, 125),
                                    new Vector2f(405, 405),
                                    //angle,
                                    360 * (float) Math.random(),
                                    0,
                                    new Color(255, 119, 0, 25),
                                    true,
                                    0,
                                    0,
                                    1.25f
                            );

                            Vector2f nebulaSpeed1 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(projectile.getAngularVelocity() + MathUtils.getRandomNumberInRange(0f, 90f)).scale(MathUtils.getRandomNumberInRange(2.5f, 10f));
                            Vector2f nebulaSpeed2 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(projectile.getAngularVelocity() + MathUtils.getRandomNumberInRange(90f, 180f)).scale(MathUtils.getRandomNumberInRange(2.5f, 10f));
                            Vector2f nebulaSpeed3 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(projectile.getAngularVelocity() + MathUtils.getRandomNumberInRange(180f, 270f)).scale(MathUtils.getRandomNumberInRange(2.5f, 10f));
                            Vector2f nebulaSpeed4 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(projectile.getAngularVelocity() + MathUtils.getRandomNumberInRange(270f, 360f)).scale(MathUtils.getRandomNumberInRange(2.5f, 10f));
                            Vector2f nebulaSpeed5 = (Vector2f) Misc.getUnitVectorAtDegreeAngle(projectile.getAngularVelocity()).scale(0f);

                            Global.getCombatEngine().addNebulaSmokeParticle(collisionPoint, nebulaSpeed1, 50f, 2.5f, 0.2f, 0.2f, (2f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(153, 95, 67, 125));
                            Global.getCombatEngine().addNebulaSmokeParticle(collisionPoint, nebulaSpeed2, 50f, 2.5f, 0.2f, 0.2f, (2f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(83, 51, 25, 125));
                            Global.getCombatEngine().addNebulaSmokeParticle(collisionPoint, nebulaSpeed3, 50f, 2.5f, 0.2f, 0.2f, (2f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(111, 56, 7, 125));
                            Global.getCombatEngine().addNebulaSmokeParticle(collisionPoint, nebulaSpeed4, 50f, 2.5f, 0.2f, 0.2f, (2f + MathUtils.getRandomNumberInRange(-0.5f, 0.5f)), new Color(134, 107, 53, 125));
                            Global.getCombatEngine().addNebulaSmokeParticle(collisionPoint, nebulaSpeed5, 50f, 2f, 0.2f, 0.2f, 0.4f, new Color(172, 255, 230, 173));
//endtodo
                        }
                    } else {
                        for (Vector2f damagePoint : damagePoints) {
                            engine.applyDamage(target,
                                    damagePoint,
                                    projectile.getDamageAmount() * 0.25f,
                                    DamageType.FRAGMENTATION,
                                    0,
                                    true,
                                    true,
                                    projectile.getSource());
                        }
                        //Global.getLogger(vic_combatPlugin.class).info("in hull hits damage: " + projectile.getDamageAmount() * 0.15f);
                    }
                }
            }
            data.locLastFrame = new Vector2f(projectile.getLocation());
            if (!engine.isEntityInPlay(projectile)) {
                localData.XLLaidlawProjes.remove(data);
            }
        }

        final Color ARC_FRINGE_COLOR = new Color(52, 255, 62);
        final Color ARC_CORE_COLOR = new Color(213, 255, 212);
        final int NUM_ARCS = 10;

        //NOTE: alkonost
        for (Map.Entry<DamagingProjectileAPI, alkonostData> entry : new HashMap<>(localData.alkonostProjes).entrySet()) {
            DamagingProjectileAPI proj = entry.getKey();
            IntervalUtil timer = entry.getValue().timer;
            Global.getSoundPlayer().playLoop("vic_alkonost_projectile_loop", proj, 1, 1, proj.getLocation(), proj.getVelocity(), 0.2f, 0.2f);

            //Arc damage
            float bonusDamage = proj.getDamageAmount() * 0.015f;
            float EMPBonusDamage = bonusDamage * 5;

            float passiveArcFrequencyMulti = 1;
            if (proj.getElapsed() != 0)
                passiveArcFrequencyMulti += org.magiclib.util.MagicAnim.smooth(proj.getElapsed() / entry.getValue().flightTime) * 0.5f;
            timer.advance(amount * passiveArcFrequencyMulti);
            if (timer.intervalElapsed()) {
                List<CombatEntityAPI> validTargets = damagableEnemiesInRangeWOAsteroids(proj.getLocation(), 300, proj.getOwner());
                int numArcs = MathUtils.getRandomNumberInRange(2, 3);
                Global.getSoundPlayer().playSound("vic_alkonost_emp_arc", MathUtils.getRandomNumberInRange(0.8f, 1.2f), 0.5f, proj.getLocation(), new Vector2f());
                for (int i = 0; i < numArcs; i++) {
                    if (validTargets.isEmpty()) {
                        engine.spawnEmpArcVisual(proj.getLocation(),
                                proj,
                                MathUtils.getRandomPointInCircle(proj.getLocation(), MathUtils.getRandomNumberInRange(100, 300)),
                                null, 10f, // thickness of the lightning bolt
                                randomizeColor(ARC_CORE_COLOR, 0.2f), //Central color
                                randomizeColor(ARC_FRINGE_COLOR, 0.2f));

                    } else {

                        //And finally, fire at a random valid target
                        CombatEntityAPI arcTarget = validTargets.get(MathUtils.getRandomNumberInRange(0, validTargets.size() - 1));

                        Global.getCombatEngine().spawnEmpArc(
                                proj.getSource(),
                                proj.getLocation(),
                                null,
                                arcTarget,
                                DamageType.ENERGY, //Damage type
                                bonusDamage, //Damage
                                EMPBonusDamage, //Emp
                                100000f, //Max range
                                "vic_alkonost_emp_arc", //Impact sound
                                10f, // thickness of the lightning bolt
                                randomizeColor(ARC_CORE_COLOR, 0.2f), //Central color
                                randomizeColor(ARC_FRINGE_COLOR, 0.2f) //Fringe Color
                        );
                        validTargets.remove(arcTarget);
                    }
                }
                //fuck up asteroid
                for (CombatEntityAPI asteroid : engine.getAsteroids()){
                    if (MathUtils.isWithinRange(asteroid.getLocation(),proj.getLocation(),350)){
                        Global.getCombatEngine().spawnEmpArc(
                                proj.getSource(),
                                proj.getLocation(),
                                null,
                                asteroid,
                                DamageType.ENERGY, //Damage type
                                bonusDamage * 15, //Damage
                                EMPBonusDamage, //Emp
                                100000f, //Max range
                                "vic_alkonost_emp_arc", //Impact sound
                                10f, // thickness of the lightning bolt
                                randomizeColor(ARC_CORE_COLOR, 0.2f), //Central color
                                randomizeColor(ARC_FRINGE_COLOR, 0.2f) //Fringe Color
                        );
                    }
                }

            }


            if (entry.getValue().flightTime - proj.getElapsed() <= 1) {
                Global.getSoundPlayer().playLoop("vic_alkonost_destabilize",proj,1,1.5f,proj.getLocation(),proj.getVelocity());
                float effectsMulti = (1f - (entry.getValue().flightTime - proj.getElapsed()));
                float speedMulti = 1f + (effectsMulti * 2f);
                float spriteSize = 1f + (effectsMulti * 0.5f);
                entry.getValue().endFlightTimer.advance(amount * speedMulti);
                if (entry.getValue().endFlightTimer.intervalElapsed()) {
                    float duration = entry.getValue().endFlightTimer.getMaxInterval() * 1 / speedMulti;
                    engine.addHitParticle(proj.getLocation(), proj.getVelocity(), 300, 1, duration * 0.3f, duration, randomizeColor(ARC_FRINGE_COLOR, 0.3f));


                    MagicRender.battlespace(
                            Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow"),
                            new Vector2f(proj.getLocation()),
                            new Vector2f(proj.getVelocity()),
                            new Vector2f(40 * spriteSize, 350 * spriteSize),
                            new Vector2f(),
                            proj.getFacing() - 45,
                            0,
                            Misc.setAlpha(randomizeColor(ARC_CORE_COLOR, 0.15f), Math.round(100 * speedMulti)),
                            true,
                            0,
                            0,
                            0f,
                            0f,
                            0,
                            0,
                            duration,
                            0f,
                            CombatEngineLayers.CONTRAILS_LAYER
                    );
                    //engine.addFloatingText(proj.getLocation(),"signal",30,Color.RED,null,0,0);
                }

            }
            //ensure that proj detonates with full damage on end of flight
            if (proj.isFading()) {
                engine.removeEntity(proj);
            }

            //detonate
            if (!engine.isEntityInPlay(proj)) {
                float damage = proj.getDamageAmount() * 0.7f;
                DamagingExplosionSpec explosion = new DamagingExplosionSpec(0.2f,
                        250,
                        125,
                        damage,
                        damage * 0.5f,
                        CollisionClass.PROJECTILE_FF,
                        CollisionClass.PROJECTILE_FIGHTER,
                        3,
                        3,
                        0.5f,
                        10,
                        new Color(33, 255, 122, 255),
                        new Color(MathUtils.getRandomNumberInRange(215, 255), MathUtils.getRandomNumberInRange(130, 170), MathUtils.getRandomNumberInRange(15, 55), 255)
                );
                explosion.setShowGraphic(false);
                explosion.setDamageType(DamageType.ENERGY);
                DamagingProjectileAPI exp = engine.spawnDamagingExplosion(
                        explosion,
                        proj.getSource(),
                        new Vector2f(proj.getLocation()),
                        false
                );
                for (CombatEntityAPI target : entry.getValue().damaged) {
                    exp.addDamagedAlready(target);
                }


                // Arcing stuff
                List<CombatEntityAPI> validTargets = new ArrayList<>();
                for (CombatEntityAPI entityToTest : CombatUtils.getEntitiesWithinRange(proj.getLocation(), 400)) {
                    if (entityToTest instanceof ShipAPI || entityToTest instanceof AsteroidAPI || entityToTest instanceof MissileAPI) {
                        //Phased targets, and targets with no collision, are ignored
                        if (entityToTest instanceof ShipAPI) {
                            if (((ShipAPI) entityToTest).isPhased()) {
                                continue;
                            }
                        }
                        if (entityToTest.getCollisionClass().equals(CollisionClass.NONE)) {
                            continue;
                        }

                        validTargets.add(entityToTest);
                    }
                }

                for (int x = 0; x < NUM_ARCS; x++) {
                    //If we have no valid targets, zap a random point near us

                    if (validTargets.isEmpty()) {
                        engine.spawnEmpArcVisual(proj.getLocation(),
                                null,
                                MathUtils.getRandomPointInCircle(proj.getLocation(), MathUtils.getRandomNumberInRange(250, 500)),
                                null, 10f, // thickness of the lightning bolt
                                randomizeColor(ARC_CORE_COLOR, 0.2f), //Central color
                                randomizeColor(ARC_FRINGE_COLOR, 0.2f));
                        Global.getSoundPlayer().playSound("vic_alkonost_emp_arc", MathUtils.getRandomNumberInRange(0.8f, 1.2f), 1, proj.getLocation(), new Vector2f());
                        continue;
                    }


                    //And finally, fire at a random valid target
                    CombatEntityAPI arcTarget = validTargets.get(MathUtils.getRandomNumberInRange(0, validTargets.size() - 1));

                    Global.getCombatEngine().spawnEmpArc(
                            proj.getSource(),
                            proj.getLocation(),
                            null,
                            arcTarget,
                            DamageType.ENERGY, //Damage type
                            bonusDamage, //Damage
                            EMPBonusDamage, //Emp
                            100000f, //Max range
                            "vic_alkonost_emp_arc", //Impact sound
                            10f, // thickness of the lightning bolt
                            randomizeColor(ARC_CORE_COLOR, 0.2f), //Central color
                            randomizeColor(ARC_FRINGE_COLOR, 0.2f) //Fringe Color
                    );
                }
                //Visual explosion shit too big, so it has its own class
                explosion(proj, Global.getCombatEngine());
                localData.alkonostProjes.remove(proj);
            }

        }

        for (vic_combatPlugin.rokhAnimationData data : new ArrayList<>(localData.rokhs)) {
            if (!data.weapon.getShip().isAlive()){
                localData.rokhs.remove(data);
                continue;
            }
            WeaponAPI weapon = data.weapon;
            if (weapon == null) continue;

            //render stuff
            if (weapon.getAmmo() == 0){
                //holoFlicker
                data.timerFlicker.advance(amount * 0.5f);
                data.timerRateChange.advance(amount);
                if (data.timerFlicker.intervalElapsed()) {
                    data.alphaChangeRate = MathUtils.getRandomNumberInRange(2.5f, 1f) * data.alphaChangeRate >= 0 ? -1 : 1;
                }
                if (data.timerFlicker.intervalElapsed()) {
                    //data.holoAlpha = Math.random() > 0.5f ? 0.5f : 1;
                }
                data.holoAlpha += data.alphaChangeRate * amount;
                data.holoAlpha = MathUtils.clamp(data.holoAlpha, 0.5f, 1);

                //glow
                data.timerGlowFlicker.advance(amount * 0.5f);
                data.timerGlowRateChange.advance(amount);
                if (data.timerGlowFlicker.intervalElapsed()) {
                    data.glowAlphaChangeRate = MathUtils.getRandomNumberInRange(2.5f, 1f) * data.glowAlphaChangeRate >= 0 ? -1 : 1;
                }
                if (data.timerGlowFlicker.intervalElapsed()) {
                    //data.glowAlpha = Math.random() > 0.5f ? 0.5f : 1;
                }
                data.glowAlpha += data.alphaChangeRate * amount;
                data.glowAlpha = MathUtils.clamp(data.holoAlpha, 0.5f, 1);

                float progress = weapon.getAmmoTracker().getReloadProgress();
                //pos of printer
                Vector2f renderPoint = data.printerPos;
                if (progress >= 0.95f){
                    float localProgress = org.magiclib.util.MagicAnim.smooth((progress - 0.95f)/ (0.05f));
                    renderPoint = (Vector2f) Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle()).scale(-64 + 26 * localProgress);

                } else if (progress >= 0.15f){
                    Global.getSoundPlayer().playLoop("vic_giga_missile_autoforge",data.weapon.getShip(),1,0.5f,data.weapon.getLocation(),data.weapon.getShip().getVelocity(), 0.3f,0.3f);
                    float localProgress = org.magiclib.util.MagicAnim.smooth((progress - 0.15f) / (1 - 0.2f));

                    renderPoint = (Vector2f) Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle()).scale(64 - 128 * localProgress);
                } else if (progress >= 0.05f){
                    float localProgress = org.magiclib.util.MagicAnim.smooth((progress - 0.05f)/ (0.1f));

                    renderPoint = (Vector2f) Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle()).scale(-64 + 128 * localProgress);
                } else {
                    float localProgress = org.magiclib.util.MagicAnim.smooth((progress)/ (0.05f));
                    renderPoint = (Vector2f) Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle()).scale(-38 - 26 * localProgress);
                }

                data.headAlpha = 1;
                if (progress >= 0.95f){
                    data.glowAlpha = 0;
                    data.headAlpha = 0;
                } else if(progress >= 0.93f){
                    data.glowAlpha = MathUtils.clamp(1 - (progress - 0.93f) / 0.02f, 0, 1);
                } else if (progress >= 0.15f && progress <= 0.17f){
                    data.glowAlpha = MathUtils.clamp((progress - 0.15f) / 0.02f, 0, 1);
                } else if (progress <= 0.15f){
                    data.glowAlpha = 0;
                    data.headAlpha = 0;
                }
                Vector2f.add(renderPoint, weapon.getLocation(), renderPoint);
                data.printerPos = new Vector2f(renderPoint);
                //pos of sparks and secondary glow
                float cycleTime = 0.05f;
                float reminder = (progress % cycleTime) / cycleTime;
                float printerHeadSway = (float) Math.cos((reminder * Math.PI * 2));
                Vector2f sideVector = Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle() - 90);
                data.headPos = Vector2f.add(renderPoint, (Vector2f) new Vector2f(sideVector).scale(13 * printerHeadSway), null);

            } else {
                Vector2f renderPoint = (Vector2f) Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle()).scale(-38);
                Vector2f.add(renderPoint, weapon.getLocation(), renderPoint);
                data.printerPos = renderPoint;
                data.glowAlpha = 0;
                data.headAlpha = 0;
            }

            if (weapon.getAmmo() == 0) {
                data.timer.advance(amount);
                if (data.timer.intervalElapsed()) {
                    float progress = weapon.getAmmoTracker().getReloadProgress();
                    if (progress >= 0.155f && progress < 0.935f) {
                        for (int i = 0; i <= MathUtils.getRandomNumberInRange(4, 6); i++) {
                            Color smoothColor = Math.random() >= 0.5f ? new Color(183, 87, 9, 255) : new Color(0, 171, 158, 255);
                            Color hitColor = Math.random() >= 0.5f ? new Color(241, 139, 63, 255) : new Color(0, 218, 200, 255);
                            float particleAngle = weapon.getCurrAngle() + MathUtils.getRandomNumberInRange(-40f, 40f);
                            if (Math.random() >= 0.5){
                                particleAngle -= 180;
                            }
                            Vector2f velocity = (Vector2f) Misc.getUnitVectorAtDegreeAngle(particleAngle).scale(MathUtils.getRandomNumberInRange(75f, 200f));
                            Vector2f.add(velocity,weapon.getShip().getVelocity(),velocity);
                            float duration = MathUtils.getRandomNumberInRange(0.15f, 0.3f);
                            engine.addSmoothParticle(data.headPos,
                                    velocity,
                                    MathUtils.getRandomNumberInRange(3f, 5f),
                                    MathUtils.getRandomNumberInRange(0.8f, 2f),
                                    duration * 0.1f,
                                    duration,
                                    randomizeColor(smoothColor, 0.2f));
                            engine.addHitParticle(data.headPos,
                                    velocity,
                                    MathUtils.getRandomNumberInRange(2f, 4f),
                                    MathUtils.getRandomNumberInRange(0.8f, 2f),
                                    duration * 0.2f,
                                    duration,
                                    randomizeColor(hitColor,0.2f));

                            vic_graphicLibEffects.customLight(data.headPos, null, 15f,0.3f,randomizeColor(new Color(245, 104, 4,255),0.1f),0f, -1f, 0.6f);

                        }
                    }
                }
            }
        }
        for (rokhRainbowMines data : new ArrayList<>(localData.rokhsMines)){
            if (!engine.isMissileAlive(data.mine)) {
                localData.rokhsMines.remove(data);
                continue;
            }
            data.mine.getEngineController().fadeToOtherColor("mine",data.color,Color.white,1,1);
            data.mine.getEngineController().extendFlame("mine",2,2,2);
        }

    }

    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);

        //Nawia
        final List<NawiaFxData> NawiaFxList = localData.NawiaFxList;
        for (NawiaFxData FX : NawiaFxList) {
            float fractionTimePast = FX.timePast / FX.animTime;
            fractionTimePast = MagicAnim.smooth(fractionTimePast);

            if (FX.timePast <= FX.ring1_MaxTime) {
                FX.ring1.setAngle(FX.ring1_Angle + FX.angle - (FX.ring1_RotationSpeed * FX.timePast));
                FX.ring1.setSize(FX.ring1_Size + FX.ring1_grow * fractionTimePast, FX.ring1_Size + FX.ring1_grow * fractionTimePast);
                FX.ring1.setNormalBlend();
                FX.ring1.setAlphaMult(1 - fractionTimePast);
                FX.ring1.renderAtCenter(FX.location.x, FX.location.y);
            }

            if (FX.timePast <= FX.ring2_MaxTime) {
                FX.ring2.setAngle(FX.ring2_Angle + FX.angle + (FX.ring2_RotationSpeed * FX.timePast));
                FX.ring2.setSize(FX.ring2_Size + FX.ring2_grow * fractionTimePast, -(FX.ring2_Size + FX.ring2_grow * fractionTimePast));
                FX.ring1.setNormalBlend();
                FX.ring2.setAlphaMult(1 - fractionTimePast);
                FX.ring2.renderAtCenter(FX.location.x, FX.location.y);
            }
        }

        //note: Flux rapture
        //final List<ShipAPI> AurasToRender = localData.FluxRaptureRender;
        for (Map.Entry<ShipAPI, Float> entry : localData.FluxRaptureRender.entrySet()) {
            ShipAPI ship = entry.getKey();
            ShipSystemAPI system = ship.getSystem();
            float effectLevel = system.getEffectLevel();
            float angle = entry.getValue() * 5;
            ShipSystemAPI.SystemState state = system.getState();

            float alphaMultOuter = 0.35f;
            /*
            if (effectLevel <= 0.2f) {
                alphaMultOuter *= MagicAnim.smooth(effectLevel * 5f);
            } else if (effectLevel >= 0.8f) {
                alphaMultOuter *= MagicAnim.smooth((1 - effectLevel) * 5f);
            }

             */


            float alphaMultInner = 0.35f;
            switch (state) {
                case IN:
                    alphaMultOuter *= 1 - effectLevel;
                    alphaMultInner *= effectLevel;
                    break;
                case OUT:
                    alphaMultInner *= effectLevel;
                    alphaMultOuter = 0;
                    break;
                case ACTIVE:
                    alphaMultOuter = 0;
                    break;
                case COOLDOWN:
                    float CD = system.getCooldownRemaining();
                    if (CD <= 1) alphaMultOuter *= 1 - CD;
                    else alphaMultOuter = 0;
                case IDLE:
                    alphaMultInner = 0;
            }
            //Global.getCombatEngine().maintainStatusForPlayerShip("vic_shockDischarger", "graphics/icons/hullsys/emp_emitter.png", "Flux Rapture", Math.round(alphaMultInner * 100f)/100f + "n/" + Math.round(alphaMultOuter * 100f)/100f, false);

            if (alphaMultOuter > 0) {
                OuterRing.setAlphaMult(alphaMultOuter);
                OuterRing.setCenter(OuterRing.getHeight(), 0);
                for (float i = 0; i < 4; i++) {
                    OuterRing.setAngle(-angle + 90 * i);
                    OuterRing.renderAtCenter(ship.getLocation().getX(), ship.getLocation().getY());
                }
            }
            if (alphaMultInner > 0) {
                InnerRing.setAlphaMult(alphaMultInner);
                InnerRing.setCenter(InnerRing.getHeight(), 0);
                for (float i = 0; i < 4; i++) {
                    InnerRing.setAngle(angle + 90 * i);
                    InnerRing.renderAtCenter(ship.getLocation().getX(), ship.getLocation().getY());
                }
            }
        }

        //Misc Animations
        for (animationRenderData FX : localData.animationRenderList) {
            int frame = (int) (FX.FPS * FX.time);
            if (frame > FX.numFrames - 1) frame = FX.numFrames - 1;
            String frameNum;
            if (frame < 10) {
                frameNum = "0" + frame;
            } else {
                frameNum = "" + frame;
            }
            SpriteAPI sprite = Global.getSettings().getSprite("fx", FX.spriteName + frameNum);
            float flip = FX.flip;
            if (FX.size != null) {
                sprite.setSize(FX.size.x, FX.size.y * flip);
            } else {
                sprite.setWidth(sprite.getWidth() * flip);
            }
            sprite.setAngle(FX.angle);
            sprite.renderAtCenter(FX.position.x, FX.position.y);

        }

        //verlioka
        for (WeaponAPI weapon : localData.verliokas) {
            if (weapon.getChargeLevel() < 1) continue;
            float range = weapon.getRange() * 1.05f;
            SpriteAPI trail = Global.getSettings().getSprite("fx", "vic_verlioka_field");
            Vector2f dir = Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle());
            Vector2f trailLoc = new Vector2f(weapon.getLocation().x + (dir.x * range), weapon.getLocation().y + (dir.y * range));
            //engine.addFloatingText(trailLoc, "o" + "", 60, Color.WHITE, ship, 0.25f, 0.25f);
            float size = range * 0.53f;
            trail.setSize(size, size * 0.5f);
            trail.setCenter(size * 0.5f, size * 0.5f);
            trail.setAngle(weapon.getCurrAngle() - 90);
            trail.renderAtCenter(trailLoc.getX(), trailLoc.getY());
        }
    }

    public static void AddNawiaFX(Vector2f location, float angle) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        localData.NawiaFxList.add(new NawiaFxData(location, angle));
    }

    public static void addVerlioka(WeaponAPI weapon) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        localData.verliokas.add(weapon);
    }

    public static void AddDefenceSuppressorTarget(ShipAPI ship, float Duration) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (localData.defenceSuppressor.containsKey(ship)) {
            float durationMult = Duration / localData.defenceSuppressor.get(ship) * 2;
            if (durationMult > 1) durationMult = 1;
            localData.defenceSuppressor.put(ship, localData.defenceSuppressor.get(ship) + (Duration * durationMult));
        } else {
            localData.defenceSuppressor.put(ship, Duration);
        }
    }

    public static void markTargetDamagedByZlydzen(ShipAPI ship, float amount, boolean shieldHit) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (shieldHit) {
            if (localData.ZlydzenTargetsShield.containsKey(ship)) {
                localData.ZlydzenTargetsShield.get(ship).markAsHit();
                localData.ZlydzenTargetsShield.get(ship).advance(amount);
            } else {
                localData.ZlydzenTargetsShield.put(ship, new ZlydzenTargetsDataShield(ship, amount));
            }
        } else {
            if (localData.ZlydzenTargetsArmour.containsKey(ship)) {
                localData.ZlydzenTargetsArmour.get(ship).markAsHit();
                localData.ZlydzenTargetsArmour.get(ship).advance(amount);
            } else {
                localData.ZlydzenTargetsArmour.put(ship, new ZlydzenTargetsDataArmour(ship, amount));
            }
        }
    }

    public static void AddFluxRaptureShip(ShipAPI ship) {
        try {
            CombatEngineAPI engine = Global.getCombatEngine();
            if (engine == null) return;
            final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
            localData.FluxRaptureRender.put(ship, MathUtils.getRandomNumberInRange(0f, 360f));
        } catch (Exception ignored) {

        }
    }

    public static void AddHunterDriveAnimation(ShipAPI ship) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        localData.hunterDriveAnimation.put(ship, 0f);
    }

    public static void AddHunterDriveTarget(ShipAPI ship, ShipAPI target) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (!localData.hunterDriveTargets.containsKey(ship)) {
            localData.hunterDriveTargets.put(ship, new HashMap<ShipAPI, Float>());
        }
        localData.hunterDriveTargets.get(ship).put(target, 10f);
    }

    public static void AddArcaneMissiles(DamagingProjectileAPI proj, Vector2f target, float time, float rangeBeforeCurving, float startingSpeed) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        localData.ArcaneMissiles.put(proj, new ArcaneMissilesData(proj, target, time, rangeBeforeCurving, startingSpeed));
    }

    public static void spriteRender(SpriteAPI sprite, Vector2f location, Vector2f velocity, Vector2f startingSize, Vector2f grow, float angle, float spin, float fadeIn, float timeFull, float fadeOut, CombatEngineLayers layer) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        localData.spritesRender.add(new spriteRender(sprite, location, velocity, startingSize, grow, angle, spin, fadeIn, timeFull, fadeOut, layer));
    }

    public static void AddQuantumLungeBoost(ShipAPI ship, float duration) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        localData.quantumLungeSpeedBoost.put(ship, duration);
    }

    public static void AddNawiaRain(Vector2f hitPoint, Vector2f targetVelocity, ShipAPI ship, DamagingProjectileAPI projectile) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        localData.NawiaRain.add(new NawiaRainData(hitPoint, targetVelocity, ship, projectile));
    }


    public static void AddHunterBuff(ShipAPI ship, ShipAPI.HullSize hullSize) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        hunterBuffData buff;
        if (localData.hunterDriveBuffs.containsKey(ship)) {
            buff = localData.hunterDriveBuffs.get(ship);
            buff.duration = buff.maxDuration;
        } else {
            buff = new hunterBuffData(ship);
            localData.hunterDriveBuffs.put(ship, buff);
        }
        buff.addStacks(hullSize);

    }

    public static void AddXLLaidlawProj(DamagingProjectileAPI projectile) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        localData.XLLaidlawProjes.add(new XLLaidlawProjData(projectile));
    }

    public static void AddAlkonostProj(DamagingProjectileAPI projectile) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        localData.alkonostProjes.put(projectile, new alkonostData(projectile));
    }

    public static void SetAsDamagedForAlkonost(DamagingProjectileAPI projectile, CombatEntityAPI target) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (localData.alkonostProjes.containsKey(projectile)) {
            localData.alkonostProjes.get(projectile).damaged.add(target);
        }
    }

    public static void AddRokh(WeaponAPI weapon) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        localData.rokhs.add(new rokhAnimationData(weapon));
    }

    public static void AddRokhMine(MissileAPI mine) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        localData.rokhsMines.add(new rokhRainbowMines(mine));
    }

    //note: local data
    public static final class LocalData {
        final List<NawiaFxData> NawiaFxList = new ArrayList<>(250);
        final List<animationRenderData> animationRenderList = new ArrayList<>(250);
        final List<WeaponAPI> verliokas = new ArrayList<>(50);
        final HashMap<ShipAPI, Float> FluxRaptureRender = new HashMap<>(10);
        final HashMap<ShipAPI, Float> defenceSuppressor = new HashMap<>(25);
        final HashMap<ShipAPI, Float> quantumLungeSpeedBoost = new HashMap<>(25);
        final HashMap<ShipAPI, ZlydzenTargetsDataShield> ZlydzenTargetsShield = new HashMap<>(50);
        final HashMap<ShipAPI, ZlydzenTargetsDataArmour> ZlydzenTargetsArmour = new HashMap<>(50);
        final HashMap<DamagingProjectileAPI, ArcaneMissilesData> ArcaneMissiles = new HashMap<>(250);
        final HashMap<ShipAPI, Float> hunterDriveAnimation = new HashMap<>(10);
        final HashMap<ShipAPI, HashMap<ShipAPI, Float>> hunterDriveTargets = new HashMap<>(10);
        final List<spriteRender> spritesRender = new ArrayList<>(50);
        final List<NawiaRainData> NawiaRain = new ArrayList<>(50);
        final HashMap<ShipAPI, hunterBuffData> hunterDriveBuffs = new HashMap<>(10);
        final List<XLLaidlawProjData> XLLaidlawProjes = new ArrayList<>(50);
        final HashMap<DamagingProjectileAPI, alkonostData> alkonostProjes = new HashMap<>(10);
        public final List<rokhAnimationData> rokhs = new ArrayList<>(10);
        public final List<rokhRainbowMines> rokhsMines = new ArrayList<>(288);
    }

    private static final class ZlydzenTargetsDataShield {

        public ZlydzenTargetsDataShield(ShipAPI target, float amount) {
            this.target = target;
            this.wasAffectedLastCheck = true;
            advance(amount);
        }

        ShipAPI target;
        float power = 0f;
        boolean wasAffectedLastCheck;

        float timeRise = 6f;
        float timeFall = 4f;

        public void advance(float amount) {
            if (wasAffectedLastCheck) {
                power += amount / timeRise;
            } else {
                power -= amount / timeFall;
            }
            if (power > 1) power = 1;
            if (power < 0) power = 0;
        }

        public void markAsHit() {
            this.wasAffectedLastCheck = true;
        }
    }

    private static final class ZlydzenTargetsDataArmour {

        public ZlydzenTargetsDataArmour(ShipAPI target, float amount) {
            this.target = target;
            this.wasAffectedLastCheck = true;
            advance(amount);
        }

        ShipAPI target;
        float power = 0f;
        boolean wasAffectedLastCheck;

        float timeRise = 6f;
        float timeFall = 4f;

        public void advance(float amount) {
            if (wasAffectedLastCheck) {
                power += amount / timeRise;
            } else {
                power -= amount / timeFall;
            }
            if (power > 1) power = 1;
            if (power < 0) power = 0;
        }

        public void markAsHit() {
            this.wasAffectedLastCheck = true;
        }
    }

    private static final class ArcaneMissilesData {

        public ArcaneMissilesData(DamagingProjectileAPI proj, Vector2f target, float timeBeforeCurving, float rangeBeforeCurving, float startingSpeed) {
            this.proj = proj;
            this.finalPoint = target;
            this.timeBeforeCurving = timeBeforeCurving;
            this.startingSpeed = startingSpeed;
            this.rangeBeforeCurving = rangeBeforeCurving;
            this.speedUpOnCurving = 1 / startingSpeed;
            this.speedUpOnCurving *= (1 - startingSpeed * rangeBeforeCurving) / (1 - rangeBeforeCurving);
        }

        DamagingProjectileAPI proj;
        final Vector2f finalPoint;
        float time;
        final float timeBeforeCurving;
        Float rotationSpeed = null;
        boolean rotate = true;
        float startingSpeed;
        float rangeBeforeCurving;
        float speedUpOnCurving = 1;
    }

    private static final class NawiaRainData {
        NawiaRainData(Vector2f hitPoint, Vector2f targetVelocity, ShipAPI ship, DamagingProjectileAPI projectile) {
            this.hitPoint = hitPoint;
            this.targetVelocity = targetVelocity;
            this.ship = ship;
            this.projectile = projectile;
            this.weapon = projectile.getWeapon();
        }

        float projMax = 15;
        float timePerProj = 2f / projMax;
        float amount = 0;
        float projesSpawned = 0;
        Vector2f hitPoint;
        Vector2f targetVelocity;
        DamagingProjectileAPI projectile;
        ShipAPI ship;
        WeaponAPI weapon;
    }


    private static final class NawiaFxData {

        private final ArrayList<SpriteAPI> ringList = new ArrayList<>();

        {
            ringList.add(Global.getSettings().getSprite("fx", "vic_nawia_ring1"));
            ringList.add(Global.getSettings().getSprite("fx", "vic_nawia_ring2"));
        }

        public NawiaFxData(Vector2f location, float angle) {
            this.location = location;
            this.angle = angle;

            animTime = MathUtils.getRandomNumberInRange(0.5f, 0.6f);

            ring1 = ringList.get(MathUtils.getRandomNumberInRange(0, ringList.size() - 1));

            ring1_MaxTime = animTime * MathUtils.getRandomNumberInRange(1f, 1f);
            ring1_Size = MathUtils.getRandomNumberInRange(10, 20);
            ring1_grow = MathUtils.getRandomNumberInRange(12, 16);
            ring1_RotationSpeed = MathUtils.getRandomNumberInRange(200f, 300f);
            ring1_Angle = MathUtils.getRandomNumberInRange(-45f, 45f);

            ring2 = ringList.get(MathUtils.getRandomNumberInRange(0, ringList.size() - 1));

            ring2_MaxTime = animTime * MathUtils.getRandomNumberInRange(1f, 1f);
            ring2_Size = ring1_Size * MathUtils.getRandomNumberInRange(1.3f, 1.4f);
            ring2_grow = MathUtils.getRandomNumberInRange(12, 16);
            ring2_RotationSpeed = MathUtils.getRandomNumberInRange(200f, 300f);
            ring2_Angle = MathUtils.getRandomNumberInRange(-45f, 45f);

            Global.getCombatEngine().addSmoothParticle(
                    location,
                    new Vector2f(),
                    MathUtils.getRandomNumberInRange(20, 30),
                    1,
                    animTime,
                    new Color(MathUtils.getRandomNumberInRange(130, 180), MathUtils.getRandomNumberInRange(20, 60), 255, 255)
            );
            for (float i = 0; i < MathUtils.getRandomNumberInRange(4, 8); i++) {

                Vector2f move = Misc.getUnitVectorAtDegreeAngle(MathUtils.getRandomNumberInRange(-20, 20) + angle);
                Global.getCombatEngine().addHitParticle(
                        location,
                        new Vector2f(move.x * MathUtils.getRandomNumberInRange(25, 125), move.y * MathUtils.getRandomNumberInRange(25, 125)),
                        MathUtils.getRandomNumberInRange(5, 20),
                        MathUtils.getRandomNumberInRange(0.8f, 1f),
                        animTime * MathUtils.getRandomNumberInRange(0.5f, 1.5f),
                        new Color(MathUtils.getRandomNumberInRange(90, 180), MathUtils.getRandomNumberInRange(20, 100), 255, 255)
                );
            }
        }

        Vector2f location;
        float angle;
        float animTime;

        SpriteAPI ring1;
        float
                ring1_MaxTime,
                ring1_Size,
                ring1_grow,
                ring1_RotationSpeed,
                ring1_Angle;

        SpriteAPI ring2;
        float
                ring2_MaxTime,
                ring2_Size,
                ring2_grow,
                ring2_RotationSpeed,
                ring2_Angle;

        float timePast = 0;

        private void advance(float amount) {
            timePast += amount;
        }
    }

    private static final class hunterBuffData {
        public hunterBuffData(ShipAPI ship) {
            this.ship = ship;
            duration = maxDuration;
        }

        Integer stacks = 0;
        float duration;
        float maxDuration = 30;
        Integer maxStacks = 5;
        ShipAPI ship;

        public void addStacks(ShipAPI.HullSize hullSize) {
            int add = 0;
            switch (hullSize) {
                case FRIGATE:
                    add = 1;
                    break;
                case DESTROYER:
                    add = 2;
                    break;
                case CRUISER:
                    add = 3;
                    break;
                case CAPITAL_SHIP:
                    add = 5;
                    break;
            }
            stacks += add;
            stacks = Math.min(maxStacks, stacks);
        }
    }

    static final class XLLaidlawProjData {
        public XLLaidlawProjData(DamagingProjectileAPI proj) {
            this.proj = proj;
            locLastFrame = proj.getLocation();
        }

        DamagingProjectileAPI proj;
        float distanceCounter;
        Vector2f locLastFrame;
        List<CombatEntityAPI> damagedAlready = new ArrayList<>();


    }

    static final class alkonostData {
        public alkonostData(DamagingProjectileAPI proj) {
            this.proj = proj;
            this.flightTime = proj.getWeapon().getRange() / proj.getMoveSpeed();
        }

        DamagingProjectileAPI proj;
        List<CombatEntityAPI> damaged = new ArrayList<>();
        IntervalUtil timer = new IntervalUtil(0.3f, 0.5f);
        IntervalUtil endFlightTimer = new IntervalUtil(0.2f, 0.2f);
        float flightTime;

    }

    static final class rokhAnimationData {
        public rokhAnimationData(WeaponAPI weapon) {
            this.weapon = weapon;
        }

        WeaponAPI weapon;
        IntervalUtil timer = new IntervalUtil(0.05f, 0.1f);
        float holoAlpha = 1;
        float alphaChangeRate = 0.1f;
        IntervalUtil timerRateChange = new IntervalUtil(0.7f, 1.5f);
        IntervalUtil timerFlicker = new IntervalUtil(0.05f, 0.25f);

        float glowAlpha = 1;
        float glowAlphaChangeRate = 0.1f;
        IntervalUtil timerGlowRateChange = new IntervalUtil(0.7f, 1.5f);
        IntervalUtil timerGlowFlicker = new IntervalUtil(0.1f, 0.3f);

        Vector2f printerPos = new Vector2f();
        Vector2f headPos = new Vector2f();

        float headAlpha = 1;
    }

    static final class rokhRainbowMines {
        public rokhRainbowMines(MissileAPI mine){
            this.mine = mine;
            color = new Color(MathUtils.getRandomNumberInRange(0,255),MathUtils.getRandomNumberInRange(0,255),MathUtils.getRandomNumberInRange(0,255),255);
        }

        Color color;
        MissileAPI mine;

    }

    private static final class animationRenderData {

        public animationRenderData(String spriteName, int numFrames, float duration, float angle, Vector2f position, Vector2f size, boolean flip) {
            this.spriteName = spriteName;
            this.numFrames = numFrames;
            this.duration = duration;
            this.FPS = numFrames / duration;
            this.angle = angle;
            this.position = new Vector2f(position);
            this.size = size;
            if (flip && Math.random() > 0.5f) this.flip = -1;
        }

        public animationRenderData(String spriteName, int numFrames, float duration, float angle, Vector2f position, Vector2f size) {
            this.spriteName = spriteName;
            this.numFrames = numFrames;
            this.duration = duration;
            this.FPS = numFrames / duration;
            this.angle = angle;
            this.position = new Vector2f(position);
            this.size = size;
        }

        public animationRenderData(String spriteName, int numFrames, float duration, float angle, Vector2f position) {
            this.spriteName = spriteName;
            this.numFrames = numFrames;
            this.duration = duration;
            this.FPS = numFrames / duration;
            this.angle = angle;
            this.position = new Vector2f(position);
            this.size = null;
        }

        private float time = 0;
        private final String spriteName;
        private final int numFrames;
        private final float duration;
        private final float FPS;
        private final float angle;
        private final Vector2f position;
        private final Vector2f size;
        private int flip = 1;
    }

    static final class spriteRender {

        spriteRender(
                SpriteAPI sprite,
                Vector2f location,
                Vector2f velocity,
                Vector2f startingSize,
                Vector2f grow,
                float angle,
                float spin,
                float fadeIn,
                float timeFull,
                float fadeOut,
                CombatEngineLayers layer) {
            this.sprite = sprite;
            this.location = new Vector2f(location);
            this.velocity = new Vector2f(velocity);
            this.startingSize = startingSize;
            this.grow = grow;
            this.angle = angle;
            this.spin = spin;
            this.fadeIn = fadeIn;
            this.timeFull = timeFull;
            this.fadeOut = fadeOut;
            this.layer = layer;
            totalTime = fadeIn + timeFull + fadeOut;
            if (fadeIn <= 0) alphaMulti = 1;
        }

        SpriteAPI sprite;
        Vector2f location;
        Vector2f velocity;
        Vector2f startingSize;
        Vector2f grow;
        float angle;
        float spin;
        float fadeIn;
        float timeFull;
        float fadeOut;
        float totalTime;
        float timePast = 0;
        float alphaMulti = 0;
        float sizeMulti = 0;
        CombatEngineLayers layer;
    }
}