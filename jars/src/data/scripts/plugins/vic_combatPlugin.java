package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.combat.entities.Ship;
import data.scripts.shipsystems.vic_shockDischarger;
import data.scripts.util.MagicAnim;
import data.scripts.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class vic_combatPlugin extends BaseEveryFrameCombatPlugin {

    static final String DATA_KEY = "vic_combatPluginData";
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
                    if (!ship.getSystem().isActive()){
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
            if (buff.duration <= 0){
                ship.getMutableStats().getEnergyWeaponDamageMult().unmodify("vic_hunterBuff");
                ship.getMutableStats().getBallisticWeaponDamageMult().unmodify("vic_hunterBuff");
                ship.getMutableStats().getMaxSpeed().unmodify("vic_hunterBuff");
                ship.getMutableStats().getShieldDamageTakenMult().unmodify("vic_hunterBuff");
                localData.hunterDriveBuffs.remove(ship);
            } else {
                ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult("vic_hunterBuff", 1 + (0.1f * buff.stacks));
                ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult("vic_hunterBuff", 1 + (0.1f * buff.stacks));
                ship.getMutableStats().getMaxSpeed().modifyPercent("vic_hunterBuff", 5f * buff.stacks);
                ship.getMutableStats().getShieldDamageTakenMult().modifyMult("vic_hunterBuff", 1 + (0.05f * buff.stacks));

                if (ship == engine.getPlayerShip())
                    engine.maintainStatusForPlayerShip("vic_hunterBuff", "graphics/icons/hullsys/vic_hunterWeb.png", "Hunter pride " + (Math.round(buff.duration * 10f) / 10f) + " s", "Empower " + 20 * buff.stacks + "%", false);
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

        //note: Nawia Rain
        for (NawiaRainData data : new ArrayList<>(localData.NawiaRain)){
            if (data.ship == null || !data.ship.isAlive()) continue;
            data.amount += amount * data.ship.getMutableStats().getBallisticRoFMult().getModifiedValue();
            while (data.amount >= data.timePerProj){
                data.amount -= data.timePerProj;
                data.projesSpawned ++;
                data.ship.getFluxTracker().increaseFlux(data.weapon.getFluxCostToFire() * 0.5f * (1/data.projMax), false);
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
                if (MagicRender.screenCheck (0.5f, SpawnPoint)) vic_combatPlugin.AddNawiaFX(SpawnPoint, proj.getFacing());
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
        for (NawiaRainData data : new ArrayList<>(localData.NawiaRain)){
            if (data.projesSpawned >= data.projMax || !data.ship.isAlive()) localData.NawiaRain.remove(data);
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
                    alphaMultOuter =  0;
                    break;
                case ACTIVE:
                    alphaMultOuter = 0;
                    break;
                case COOLDOWN:
                    float CD = system.getCooldownRemaining();
                    if (CD <=1) alphaMultOuter *= 1 - CD;
                    else alphaMultOuter = 0;
                case IDLE:
                    alphaMultInner = 0;
            }
            //Global.getCombatEngine().maintainStatusForPlayerShip("vic_shockDischarger", "graphics/icons/hullsys/emp_emitter.png", "Flux Rapture", Math.round(alphaMultInner * 100f)/100f + "n/" + Math.round(alphaMultOuter * 100f)/100f, false);

            if (alphaMultOuter > 0){
                OuterRing.setAlphaMult(alphaMultOuter);
                OuterRing.setCenter(OuterRing.getHeight(), 0);
                for (float i = 0; i < 4; i++) {
                    OuterRing.setAngle(-angle + 90 * i);
                    OuterRing.renderAtCenter(ship.getLocation().getX(), ship.getLocation().getY());
                }
            }
            if (alphaMultInner > 0){
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
        } catch (Exception ignored){

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
        if (localData.hunterDriveBuffs.containsKey(ship)){
            buff = localData.hunterDriveBuffs.get(ship);
            buff.duration = buff.maxDuration;
        } else {
            buff = new hunterBuffData(ship);
            localData.hunterDriveBuffs.put(ship, buff);
        }
        buff.addStacks(hullSize);

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
        final HashMap<ShipAPI, hunterBuffData> hunterDriveBuffs= new HashMap<>(10);
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
        NawiaRainData(Vector2f hitPoint, Vector2f targetVelocity, ShipAPI ship, DamagingProjectileAPI projectile){
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
        public hunterBuffData(ShipAPI ship){
            this.ship = ship;
            duration = maxDuration;
        }
        Integer stacks = 0;
        float duration;
        float maxDuration = 30;
        Integer maxStacks = 5;
        ShipAPI ship;

        public void addStacks(ShipAPI.HullSize hullSize){
            int add = 0;
            switch (hullSize){
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