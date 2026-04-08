package com.mergemadness;

import com.jme3.math.Vector2f;

/**
 * Represents a single orb in the game arena.
 * Handles its own physics state (position, velocity, tier, grace frames).
 */
public class Orb {

    private final Vector2f position;
    private final Vector2f velocity;
    private int tier;
    private float radius;
    private boolean markedForRemoval;
    private boolean merging;
    private int dropGraceFrames; // frames before danger-zone check applies

    public Orb(float x, float y, int tier) {
        this.position = new Vector2f(x, y);
        this.velocity = new Vector2f(0, 0);
        this.tier = tier;
        this.radius = OrbTier.TIERS[tier].getRadius();
        this.markedForRemoval = false;
        this.merging = false;
        this.dropGraceFrames = 90; // ~1.5 seconds at 60fps
    }

    public Orb(float x, float y, int tier, float vx, float vy) {
        this(x, y, tier);
        this.velocity.set(vx, vy);
        this.dropGraceFrames = 0; // merged orbs get no grace
    }

    // -- Getters & Setters --

    public Vector2f getPosition() { return position; }
    public float getX() { return position.x; }
    public float getY() { return position.y; }
    public void setX(float x) { position.x = x; }
    public void setY(float y) { position.y = y; }

    public Vector2f getVelocity() { return velocity; }
    public float getVx() { return velocity.x; }
    public float getVy() { return velocity.y; }
    public void setVx(float vx) { velocity.x = vx; }
    public void setVy(float vy) { velocity.y = vy; }

    public int getTier() { return tier; }
    public float getRadius() { return radius; }

    public boolean isMarkedForRemoval() { return markedForRemoval; }
    public void markForRemoval() { this.markedForRemoval = true; }

    public boolean isMerging() { return merging; }
    public void setMerging(boolean merging) { this.merging = merging; }

    public int getDropGraceFrames() { return dropGraceFrames; }
    public void decrementGrace() {
        if (dropGraceFrames > 0) dropGraceFrames--;
    }

    /**
     * Distance to another orb (center-to-center).
     */
    public float distanceTo(Orb other) {
        return position.distance(other.position);
    }

    /**
     * Check if this orb overlaps with another.
     */
    public boolean overlaps(Orb other) {
        return distanceTo(other) < (this.radius + other.radius);
    }
}
