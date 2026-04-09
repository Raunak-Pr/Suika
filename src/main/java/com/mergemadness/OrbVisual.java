package com.mergemadness;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

/**
 * Visual representation of an Orb using flat Quads (2D).
 * Avoids 3D sphere depth/culling issues with orthographic cameras.
 */
public class OrbVisual {

    private final Node node;
    private final Orb orb;
    private final float zLayer;

    public OrbVisual(Orb orb, AssetManager am, BitmapFont font, float zLayer) {
        this.orb = orb;
        this.zLayer = zLayer;
        OrbTier tier = OrbTier.TIERS[orb.getTier()];
        float r = orb.getRadius();

        node = new Node("orb-" + System.nanoTime());

        // Glow quad — large, semi-transparent, behind body
        float glowSize = r * 3.4f;
        Geometry glow = coloredQuad(am, "glow", glowSize, tier.getGlowColor(), 0.30f);
        glow.setLocalTranslation(-glowSize / 2f, -glowSize / 2f, -0.2f);
        node.attachChild(glow);

        // Body quad — solid colored square
        float bodySize = r * 2f;
        Geometry body = coloredQuad(am, "body", bodySize, tier.getColor(), 1.0f);
        body.setLocalTranslation(-bodySize / 2f, -bodySize / 2f, 0f);
        node.attachChild(body);

        // Highlight — small white square for "shine" effect
        float hlSize = r * 0.6f;
        Geometry hl = coloredQuad(am, "hl", hlSize, ColorRGBA.White, 0.25f);
        hl.setLocalTranslation(-r * 0.5f, r * 0.1f, 0.05f);
        node.attachChild(hl);

        // Tier label text
        BitmapText label = new BitmapText(font, false);
        label.setText(tier.getLabel());
        float fontSize = Math.max(0.30f, r * 1.0f);
        label.setSize(fontSize);
        label.setColor(ColorRGBA.White);
        label.setLocalTranslation(
            -label.getLineWidth() / 2f,
             label.getLineHeight() / 3f,
             0.1f
        );
        node.attachChild(label);

        updatePosition();
    }

    /**
     * Create a flat colored quad with face culling disabled.
     * All quads use the Transparent bucket with depth test disabled so they
     * always composite correctly over the opaque arena geometry in orthographic
     * projection. Back-to-front z-sorting within the Transparent bucket ensures
     * glow (z=-0.2) renders before the body (z=0) which renders before the
     * highlight (z=0.05), giving the correct layering order.
     */
    private static Geometry coloredQuad(AssetManager am, String name,
                                         float size, ColorRGBA color, float alpha) {
        Quad mesh = new Quad(size, size);
        Geometry g = new Geometry(name, mesh);
        Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        ColorRGBA c = color.clone();
        c.a = alpha;
        mat.setColor("Color", c);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mat.getAdditionalRenderState().setDepthWrite(false);
        mat.getAdditionalRenderState().setDepthTest(false);
        g.setQueueBucket(RenderQueue.Bucket.Transparent);
        g.setMaterial(mat);
        return g;
    }

    public void updatePosition() {
        node.setLocalTranslation(orb.getX(), orb.getY(), zLayer);
    }

    public Node getNode() { return node; }
    public Orb getOrb()   { return orb; }

    public void detach() {
        if (node.getParent() != null) node.removeFromParent();
    }
}
