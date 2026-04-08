package com.mergemadness;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages particle effects for merge explosions using flat Quads.
 */
public class ParticleManager {

    private final Node particleNode;
    private final AssetManager assetManager;
    private final List<Particle> particles = new ArrayList<>();

    private static class Particle {
        Geometry geom;
        Material mat;
        float vx, vy;
        float life;
        float decay;
        ColorRGBA color;
    }

    public ParticleManager(Node rootNode, AssetManager assetManager) {
        this.assetManager = assetManager;
        this.particleNode = new Node("particles");
        rootNode.attachChild(particleNode);
    }

    public void spawnBurst(float x, float y, ColorRGBA color, int count) {
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            float size = 0.06f + (float) Math.random() * 0.12f;

            Quad q = new Quad(size, size);
            p.geom = new Geometry("p", q);
            p.color = color.clone();
            p.mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            p.mat.setColor("Color", p.color);
            p.mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            p.mat.getAdditionalRenderState().setDepthWrite(false);
            p.mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
            p.geom.setMaterial(p.mat);
            p.geom.setQueueBucket(RenderQueue.Bucket.Transparent);

            double angle = Math.random() * Math.PI * 2;
            float speed = 2f + (float) Math.random() * 6f;
            p.vx = (float) Math.cos(angle) * speed;
            p.vy = (float) Math.sin(angle) * speed;
            p.life = 1.0f;
            p.decay = 0.025f + (float) Math.random() * 0.04f;

            p.geom.setLocalTranslation(x - size / 2f, y - size / 2f, 4f);
            particleNode.attachChild(p.geom);
            particles.add(p);
        }
    }

    public void spawnFlash(float x, float y, ColorRGBA color, float radius) {
        Particle p = new Particle();
        float size = radius * 2f;
        Quad q = new Quad(size, size);
        p.geom = new Geometry("flash", q);
        p.color = new ColorRGBA(color.r, color.g, color.b, 0.5f);
        p.mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        p.mat.setColor("Color", p.color);
        p.mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        p.mat.getAdditionalRenderState().setDepthWrite(false);
        p.mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        p.geom.setMaterial(p.mat);
        p.geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        p.vx = 0;
        p.vy = 0;
        p.life = 1.0f;
        p.decay = 0.06f;
        p.geom.setLocalTranslation(x - size / 2f, y - size / 2f, 3.5f);
        particleNode.attachChild(p.geom);
        particles.add(p);
    }

    public void update(float tpf) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.life -= p.decay;
            if (p.life <= 0) {
                p.geom.removeFromParent();
                it.remove();
                continue;
            }

            Vector3f pos = p.geom.getLocalTranslation();
            p.vy -= 8f * tpf; // gravity
            pos.x += p.vx * tpf;
            pos.y += p.vy * tpf;
            p.geom.setLocalTranslation(pos);

            p.color.a = p.life * 0.7f;
            p.mat.setColor("Color", p.color);
            p.geom.setLocalScale(Math.max(0.1f, p.life));
        }
    }

    public void clear() {
        for (Particle p : particles) p.geom.removeFromParent();
        particles.clear();
    }
}
