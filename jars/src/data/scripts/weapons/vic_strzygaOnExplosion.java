package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class vic_strzygaOnExplosion implements ProximityExplosionEffect {
    public vic_strzygaOnExplosion() {
    }

    public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
        CombatEngineAPI engine = Global.getCombatEngine();

        Color PARTICLE_COLOR_CORE = new Color(255, 255, 255, 255);
        Color EXPLOSION_COLOR = new Color(255, 152, 99, 255);
        Color NEBULA_COLOR = new Color(83, 75, 75, 175);


        engine.addHitParticle(originalProjectile.getLocation(), new Vector2f(0,0), 25f, 10f, 0.15f, PARTICLE_COLOR_CORE);
        engine.spawnExplosion(originalProjectile.getLocation(), new Vector2f(0,0), EXPLOSION_COLOR, 50f, 0.25f);
        engine.addNebulaSmokeParticle(originalProjectile.getLocation(), new Vector2f(0,0), 30f, 1.5f, 0.1f, 0.1f, 0.75f, NEBULA_COLOR);

    }
}

