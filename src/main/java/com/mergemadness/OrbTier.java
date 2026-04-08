package com.mergemadness;

import com.jme3.math.ColorRGBA;

/**
 * Defines all orb tiers — their radius, color, glow, point value, and label.
 * Tiers 0-4 are droppable; tiers 5-10 can only be created by merging.
 */
public class OrbTier {

    public static final int DROP_MAX_TIER = 4; // tiers 0-4 are droppable
    public static final int TOTAL_TIERS = 11;

    // Weighted probabilities for drop tiers (index 0-4)
    public static final int[] DROP_WEIGHTS = {35, 30, 20, 10, 5};

    private final float radius;
    private final ColorRGBA color;
    private final ColorRGBA glowColor;
    private final int points;
    private final String label;

    private OrbTier(float radius, ColorRGBA color, ColorRGBA glowColor, int points, String label) {
        this.radius = radius;
        this.color = color;
        this.glowColor = glowColor;
        this.points = points;
        this.label = label;
    }

    // All tier definitions
    public static final OrbTier[] TIERS = {
        //           radius  color                                      glow                                          pts   label
        new OrbTier(0.28f,  new ColorRGBA(1.0f, 0.176f, 0.482f, 1f),  new ColorRGBA(1.0f, 0.176f, 0.482f, 0.4f),   2,    "2"),
        new OrbTier(0.36f,  new ColorRGBA(1.0f, 0.420f, 0.102f, 1f),  new ColorRGBA(1.0f, 0.420f, 0.102f, 0.4f),   4,    "4"),
        new OrbTier(0.44f,  new ColorRGBA(1.0f, 0.890f, 0.102f, 1f),  new ColorRGBA(1.0f, 0.890f, 0.102f, 0.4f),   8,    "8"),
        new OrbTier(0.54f,  new ColorRGBA(0.224f, 1.0f, 0.078f, 1f),  new ColorRGBA(0.224f, 1.0f, 0.078f, 0.4f),   16,   "16"),
        new OrbTier(0.66f,  new ColorRGBA(0.0f, 0.941f, 1.0f, 1f),    new ColorRGBA(0.0f, 0.941f, 1.0f, 0.4f),     32,   "32"),
        new OrbTier(0.80f,  new ColorRGBA(0.102f, 0.561f, 1.0f, 1f),  new ColorRGBA(0.102f, 0.561f, 1.0f, 0.4f),   64,   "64"),
        new OrbTier(0.96f,  new ColorRGBA(0.749f, 0.373f, 1.0f, 1f),  new ColorRGBA(0.749f, 0.373f, 1.0f, 0.4f),   128,  "128"),
        new OrbTier(1.12f,  new ColorRGBA(1.0f, 0.176f, 0.482f, 1f),  new ColorRGBA(1.0f, 0.176f, 0.482f, 0.5f),   256,  "256"),
        new OrbTier(1.30f,  new ColorRGBA(1.0f, 0.890f, 0.102f, 1f),  new ColorRGBA(1.0f, 0.890f, 0.102f, 0.5f),   512,  "512"),
        new OrbTier(1.50f,  new ColorRGBA(0.0f, 0.941f, 1.0f, 1f),    new ColorRGBA(0.0f, 0.941f, 1.0f, 0.6f),     1024, "1K"),
        new OrbTier(1.72f,  new ColorRGBA(1.0f, 0.102f, 1.0f, 1f),    new ColorRGBA(1.0f, 0.102f, 1.0f, 0.6f),     2048, "2K"),
    };

    /**
     * Pick a random droppable tier using weighted probabilities.
     */
    public static int pickRandomDropTier() {
        int total = 0;
        for (int w : DROP_WEIGHTS) total += w;
        int r = (int) (Math.random() * total);
        for (int i = 0; i < DROP_WEIGHTS.length; i++) {
            r -= DROP_WEIGHTS[i];
            if (r <= 0) return i;
        }
        return 0;
    }

    public float getRadius() { return radius; }
    public ColorRGBA getColor() { return color.clone(); }
    public ColorRGBA getGlowColor() { return glowColor.clone(); }
    public int getPoints() { return points; }
    public String getLabel() { return label; }
}
