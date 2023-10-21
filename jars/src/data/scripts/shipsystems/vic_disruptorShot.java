package data.scripts.shipsystems;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.impl.combat.MineStrikeStatsAIInfoProvider;
import org.magiclib.util.MagicRender;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class vic_disruptorShot extends BaseShipSystemScript implements MineStrikeStatsAIInfoProvider {
	
	public static float MINE_RANGE = 1500f;
	
	public static final float MIN_SPAWN_DIST = 75f;
	
	public static final float LIVE_TIME = 5f;
	
	public static final Color JITTER_COLOR = new Color(255,155,255,75);
	public static final Color JITTER_UNDER_COLOR = new Color(255,155,255,155);

	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		//boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		
		float jitterLevel = effectLevel;
		if (state == State.OUT) {
			jitterLevel *= jitterLevel;
		}
		float jitterRangeBonus = jitterLevel * 25f;
		
		ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 11, 0f, 3f + jitterRangeBonus);
		ship.setJitter(this, JITTER_COLOR, jitterLevel, 4, 0f, 0 + jitterRangeBonus);
		
		if (state == State.IN) {
		} else if (effectLevel >= 1) {

			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(ship.getFacing());
			Vector2f target = new Vector2f(ship.getLocation().x + dir.x * 200,ship.getLocation().y + dir.y * 200);

			spawnMine(ship, target);
		}
	}
	
	
	public void unapply(MutableShipStatsAPI stats, String id) {
	}
	
	public void spawnMine(ShipAPI source, Vector2f mineLoc) {
		CombatEngineAPI engine = Global.getCombatEngine();
		Vector2f currLoc = Misc.getPointAtRadius(mineLoc, 30f + (float) Math.random() * 30f);
		//Vector2f currLoc = null;

		//Vector2f currLoc = mineLoc;
		MissileAPI mine = (MissileAPI) engine.spawnProjectile(
				source,
				null,
				"vic_disruptor_system",
				mineLoc,
				source.getFacing(), null
		);

		if (source != null) {
			float extraDamageMult = source.getMutableStats().getMissileWeaponDamageMult().getModifiedValue();
			mine.getDamage().setMultiplier(mine.getDamage().getMultiplier() * extraDamageMult);
		}

		float fadeInTime = 0.5f;
		//mine.getVelocity().scale(0);
		mine.fadeOutThenIn(fadeInTime);
		mine.setCollisionClass(CollisionClass.NONE);


		float liveTime = LIVE_TIME;
		//liveTime = 0.01f;
		//mine.setFlightTime(mine.getMaxFlightTime() - liveTime);
		
		Global.getSoundPlayer().playSound("mine_teleport", 1f, 1f, mine.getLocation(), mine.getVelocity());

		Global.getCombatEngine().addPlugin(createMissileJitterPlugin(mine, fadeInTime));
	}

	protected EveryFrameCombatPlugin createMissileJitterPlugin(final MissileAPI mine, final float fadeInTime) {
		return new BaseEveryFrameCombatPlugin() {
			float elapsed = 0f;
			private float rotation = 0f;
			@Override
			public void advance(float amount, List<InputEventAPI> events) {
				if (Global.getCombatEngine().isPaused()) return;

				elapsed += amount;
				Vector2f location = mine.getLocation();

				rotation += 120 * amount;
				if (rotation > 360)rotation -= 360;
				SpriteAPI innerRIng = Global.getSettings().getSprite("fx", "vic_disruptorInnerRing");

				MagicRender.singleframe(innerRIng, location, new Vector2f(mine.getSpriteAPI().getWidth(),mine.getSpriteAPI().getHeight()), rotation, new Color(255, 255, 255, 220), false);

				SpriteAPI outerRIng = Global.getSettings().getSprite("fx", "vic_disruptorOuterRing");

				MagicRender.singleframe(outerRIng, location, new Vector2f(mine.getSpriteAPI().getWidth()* 1.7f,mine.getSpriteAPI().getHeight()* 1.7f), -rotation, new Color(255, 255, 255, 220), false);

				SpriteAPI ring = Global.getSettings().getSprite("fx", "vic_shieldsRing256");

				MagicRender.singleframe(ring, location, new Vector2f(450,450), -rotation, new Color(18, 255, 218, 144), false);
				MagicRender.singleframe(ring, location, new Vector2f(450,450), rotation, new Color(18, 255, 218, 144), false);

				mine.setMinePrimed(true);

				if (elapsed > 10 || !Global.getCombatEngine().getMissiles().contains(mine)) {
					Global.getCombatEngine().removePlugin(this);
				}
			}
		};
	}

	protected float getMaxRange(ShipAPI ship) {
		return getMineRange(ship);
	}

	@Override
	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		if (system.isOutOfAmmo()) return null;
		if (system.getState() != SystemState.IDLE) return null;
		
		Vector2f target = ship.getMouseTarget();
		if (target != null) {
			float dist = Misc.getDistance(ship.getLocation(), target);
			float max = getMaxRange(ship) + ship.getCollisionRadius();
			if (dist > max) {
				return "OUT OF RANGE";
			} else {
				return "READY";
			}
		}
		return null;
	}

	
	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		return ship.getMouseTarget() != null;
	}

	public float getFuseTime() {
		return 3f;
	}

	@Override
	public float getMineRange(ShipAPI ship) {
		return MINE_RANGE;
	}
	
}








