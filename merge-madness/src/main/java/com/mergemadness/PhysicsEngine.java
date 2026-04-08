package com.mergemadness;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple 2D physics engine for the orb arena.
 * Handles gravity, friction, wall/floor collisions, and orb-vs-orb resolution.
 */
public class PhysicsEngine {

    // Arena boundaries (in world units, centered at 0)
    public static final float ARENA_HALF_WIDTH = 4.2f;
    public static final float ARENA_FLOOR_Y = -7.0f;
    public static final float ARENA_CEILING_Y = 7.0f;
    public static final float DANGER_LINE_Y = 5.0f;

    // Physics constants
    private static final float GRAVITY = -12.0f;       // downward acceleration
    private static final float FRICTION = 0.985f;       // velocity damping per frame
    private static final float BOUNCE = 0.50f;          // collision restitution
    private static final float WALL_BOUNCE = 0.35f;     // wall restitution
    private static final float FLOOR_BOUNCE = 0.45f;    // floor restitution
    private static final float SETTLE_THRESHOLD = 0.08f; // velocity below which we consider settled

    /**
     * Step the physics simulation by the given delta time.
     * Returns a list of merge events that occurred this frame.
     */
    public List<MergeEvent> step(List<Orb> orbs, float tpf) {
        List<MergeEvent> merges = new ArrayList<>();

        // Apply gravity + friction + movement
        for (Orb o : orbs) {
            if (o.isMarkedForRemoval() || o.isMerging()) continue;

            o.setVy(o.getVy() + GRAVITY * tpf);
            o.setVx(o.getVx() * FRICTION);
            o.setVy(o.getVy() * FRICTION);
            o.setX(o.getX() + o.getVx() * tpf);
            o.setY(o.getY() + o.getVy() * tpf);
            o.decrementGrace();

            // Wall collisions
            float r = o.getRadius();
            if (o.getX() - r < -ARENA_HALF_WIDTH) {
                o.setX(-ARENA_HALF_WIDTH + r);
                o.setVx(Math.abs(o.getVx()) * WALL_BOUNCE);
            }
            if (o.getX() + r > ARENA_HALF_WIDTH) {
                o.setX(ARENA_HALF_WIDTH - r);
                o.setVx(-Math.abs(o.getVx()) * WALL_BOUNCE);
            }

            // Floor collision
            if (o.getY() - r < ARENA_FLOOR_Y) {
                o.setY(ARENA_FLOOR_Y + r);
                o.setVy(Math.abs(o.getVy()) * FLOOR_BOUNCE);
                if (Math.abs(o.getVy()) < SETTLE_THRESHOLD) {
                    o.setVy(0);
                }
            }
        }

        // Orb-vs-orb collisions and merge detection
        for (int i = 0; i < orbs.size(); i++) {
            Orb a = orbs.get(i);
            if (a.isMarkedForRemoval() || a.isMerging()) continue;

            for (int j = i + 1; j < orbs.size(); j++) {
                Orb b = orbs.get(j);
                if (b.isMarkedForRemoval() || b.isMerging()) continue;

                float dx = b.getX() - a.getX();
                float dy = b.getY() - a.getY();
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float minDist = a.getRadius() + b.getRadius();

                if (dist < minDist && dist > 0.001f) {
                    // Resolve overlap
                    float overlap = minDist - dist;
                    float nx = dx / dist;
                    float ny = dy / dist;
                    float totalR = a.getRadius() + b.getRadius();
                    float aRatio = b.getRadius() / totalR;
                    float bRatio = a.getRadius() / totalR;

                    a.setX(a.getX() - nx * overlap * aRatio);
                    a.setY(a.getY() - ny * overlap * aRatio);
                    b.setX(b.getX() + nx * overlap * bRatio);
                    b.setY(b.getY() + ny * overlap * bRatio);

                    // Velocity response
                    float relVx = a.getVx() - b.getVx();
                    float relVy = a.getVy() - b.getVy();
                    float relDot = relVx * nx + relVy * ny;
                    if (relDot > 0) {
                        float impulse = relDot * BOUNCE;
                        a.setVx(a.getVx() - impulse * nx * aRatio);
                        a.setVy(a.getVy() - impulse * ny * aRatio);
                        b.setVx(b.getVx() + impulse * nx * bRatio);
                        b.setVy(b.getVy() + impulse * ny * bRatio);
                    }

                    // Merge check — same tier and not max tier
                    if (a.getTier() == b.getTier() && a.getTier() < OrbTier.TOTAL_TIERS - 1) {
                        a.setMerging(true);
                        b.setMerging(true);
                        float mx = (a.getX() + b.getX()) / 2f;
                        float my = (a.getY() + b.getY()) / 2f;
                        merges.add(new MergeEvent(a, b, a.getTier() + 1, mx, my));
                    }
                }
            }
        }

        return merges;
    }

    /**
     * Check if any non-grace orb is above the danger line.
     */
    public boolean isAnyOrbAboveDanger(List<Orb> orbs) {
        for (Orb o : orbs) {
            if (o.isMarkedForRemoval() || o.isMerging()) continue;
            if (o.getDropGraceFrames() <= 0 && (o.getY() + o.getRadius()) > DANGER_LINE_Y) {
                return true;
            }
        }
        return false;
    }

    /**
     * Represents a merge event between two orbs.
     */
    public static class MergeEvent {
        public final Orb orbA;
        public final Orb orbB;
        public final int newTier;
        public final float x;
        public final float y;

        public MergeEvent(Orb a, Orb b, int newTier, float x, float y) {
            this.orbA = a;
            this.orbB = b;
            this.newTier = newTier;
            this.x = x;
            this.y = y;
        }
    }
}
