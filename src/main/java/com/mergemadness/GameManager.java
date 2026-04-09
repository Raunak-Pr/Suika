package com.mergemadness;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Core game manager — orchestrates game states, physics, visuals, input, and HUD.
 */
public class GameManager {

    public enum GameState {
        TITLE, PLAYING, GAME_OVER
    }

    private final MergeMadnessApp app;
    private final AssetManager assetManager;
    private final InputManager inputManager;
    private final Camera cam;
    private final Node rootNode;
    private final Node guiNode;

    private PhysicsEngine physics;
    private HudManager hud;
    private ParticleManager particleMgr;

    private GameState state = GameState.TITLE;
    private final List<Orb> orbs = new ArrayList<>();
    private final List<OrbVisual> orbVisuals = new ArrayList<>();
    private int score = 0;
    private int bestScore = 0;
    private int comboCount = 0;
    private float comboTimer = 0;
    private float dangerTimer = 0;
    private int currentTier = 0;
    private int nextTier = 0;
    private boolean canDrop = true;
    private float dropCooldown = 0;
    private float mouseWorldX = 0;

    private int screenW, screenH;
    private float viewHalfW, viewHalfH;

    private Node arenaNode;
    private Node orbNode;
    private Geometry guideLineGeom;
    private Geometry previewOrbGeom;

    // Z-layers (camera at z=50 looking at z=0)
    static final float Z_BG       = 0f;
    static final float Z_WALLS    = 1f;
    static final float Z_DANGER   = 2f;
    static final float Z_ORBS     = 3f;
    static final float Z_GUIDE    = 5f;
    static final float Z_PREVIEW  = 6f;

    private static final Preferences prefs = Preferences.userNodeForPackage(GameManager.class);

    public GameManager(MergeMadnessApp app) {
        this.app = app;
        this.assetManager = app.getAssetManager();
        this.inputManager = app.getInputManager();
        this.cam = app.getCamera();
        this.rootNode = app.getRootNode();
        this.guiNode = app.getGuiNode();
    }

    public void initialize() {
        screenW = cam.getWidth();
        screenH = cam.getHeight();

        float aspect = (float) screenW / screenH;
        viewHalfH = 8f;
        viewHalfW = viewHalfH * aspect;

        // Orthographic camera: looking down -Z
        cam.setParallelProjection(true);
        cam.setFrustum(0.1f, 1000f, -viewHalfW, viewHalfW, -viewHalfH, viewHalfH);
        cam.setLocation(new Vector3f(0, 0, 50));
        cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);

        System.out.println("[MM] Screen=" + screenW + "x" + screenH +
                " viewHalf=" + viewHalfW + "x" + viewHalfH);

        bestScore = prefs.getInt("bestScore", 0);
        physics = new PhysicsEngine();
        hud = new HudManager(guiNode, assetManager, screenW, screenH);
        hud.updateBest(bestScore);

        createArena();

        orbNode = new Node("orbNode");
        orbNode.setCullHint(com.jme3.scene.Spatial.CullHint.Never);
        rootNode.attachChild(orbNode);

        particleMgr = new ParticleManager(rootNode, assetManager);
        setupInput();

        currentTier = OrbTier.pickRandomDropTier();
        nextTier = OrbTier.pickRandomDropTier();
        hud.showTitleScreen();
        updatePreviewOrb();
    }

    // ---- Coordinate mapping: screen pixels → world units ----

    private float screenToWorldX(float sx) {
        return (sx / screenW - 0.5f) * 2f * viewHalfW;
    }

    // ---- Arena ----

    private void createArena() {
        arenaNode = new Node("arena");
        rootNode.attachChild(arenaNode);

        float hw = PhysicsEngine.ARENA_HALF_WIDTH;
        float floorY = PhysicsEngine.ARENA_FLOOR_Y;
        float ceilY  = PhysicsEngine.ARENA_CEILING_Y;
        float halfH  = (ceilY - floorY) / 2f;
        float midY   = (ceilY + floorY) / 2f;

        // Background fill
        makeBox("bg", 0, midY, Z_BG, hw, halfH,
                new ColorRGBA(0.05f, 0.05f, 0.12f, 1f), false);

        // Left wall
        makeBox("wallL", -hw - 0.06f, midY, Z_WALLS, 0.06f, halfH,
                new ColorRGBA(0.22f, 0.22f, 0.5f, 1f), false);
        // Right wall
        makeBox("wallR", hw + 0.06f, midY, Z_WALLS, 0.06f, halfH,
                new ColorRGBA(0.22f, 0.22f, 0.5f, 1f), false);
        // Floor
        makeBox("floor", 0, floorY - 0.06f, Z_WALLS, hw + 0.12f, 0.06f,
                new ColorRGBA(0.18f, 0.18f, 0.4f, 1f), false);

        // Danger line
        makeBox("danger", 0, PhysicsEngine.DANGER_LINE_Y, Z_DANGER, hw, 0.04f,
                new ColorRGBA(1f, 0.1f, 0.3f, 0.7f), true);

        // Guide line
        guideLineGeom = makeBox("guide", 0, midY, Z_GUIDE, 0.025f, halfH,
                new ColorRGBA(1f, 1f, 1f, 0.25f), true);

        // Preview orb — flat quad like the orbs
        float prevSize = 1f;
        Quad prevMesh = new Quad(prevSize, prevSize);
        previewOrbGeom = new Geometry("preview", prevMesh);
        Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", new ColorRGBA(1, 1, 1, 0.4f));
        m.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        m.getAdditionalRenderState().setDepthWrite(false);
        m.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        previewOrbGeom.setMaterial(m);
        previewOrbGeom.setQueueBucket(RenderQueue.Bucket.Transparent);
        previewOrbGeom.setLocalTranslation(-prevSize / 2f,
                PhysicsEngine.DANGER_LINE_Y + 0.8f - prevSize / 2f, Z_PREVIEW);
        arenaNode.attachChild(previewOrbGeom);
    }

    private Geometry makeBox(String name, float x, float y, float z,
                             float halfW, float halfH, ColorRGBA color, boolean alpha) {
        Box box = new Box(halfW, halfH, 0.01f);
        Geometry g = new Geometry(name, box);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        if (alpha) {
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            mat.getAdditionalRenderState().setDepthWrite(false);
            g.setQueueBucket(RenderQueue.Bucket.Transparent);
        }
        g.setMaterial(mat);
        g.setLocalTranslation(x, y, z);
        arenaNode.attachChild(g);
        return g;
    }

    private void updatePreviewOrb() {
        OrbTier t = OrbTier.TIERS[currentTier];
        float size = t.getRadius() * 2f;
        previewOrbGeom.setLocalScale(size);
        ColorRGBA c = t.getColor();
        c.a = 0.45f;
        previewOrbGeom.getMaterial().setColor("Color", c);
        // Re-center the quad at current mouse position
        previewOrbGeom.setLocalTranslation(mouseWorldX - size / 2f,
                PhysicsEngine.DANGER_LINE_Y + 0.8f - size / 2f, Z_PREVIEW);
    }

    // ---- Input ----

    private void setupInput() {
        inputManager.setCursorVisible(true);

        inputManager.addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("MX+", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("MX-", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("MY+", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("MY-", new MouseAxisTrigger(MouseInput.AXIS_Y, true));

        inputManager.addListener((ActionListener) (name, pressed, tpf) -> {
            if ("Click".equals(name) && pressed) {
                System.out.println("[MM] CLICK state=" + state);
                handleClick();
            }
        }, "Click");

        inputManager.addListener((AnalogListener) (name, value, tpf) -> {
            syncMouse();
        }, "MX+", "MX-", "MY+", "MY-");
    }

    private void syncMouse() {
        Vector2f sp = inputManager.getCursorPosition();
        mouseWorldX = screenToWorldX(sp.x);

        float r = OrbTier.TIERS[currentTier].getRadius();
        float hw = PhysicsEngine.ARENA_HALF_WIDTH;
        mouseWorldX = Math.max(-hw + r, Math.min(hw - r, mouseWorldX));

        float midY = (PhysicsEngine.ARENA_CEILING_Y + PhysicsEngine.ARENA_FLOOR_Y) / 2f;
        guideLineGeom.setLocalTranslation(mouseWorldX, midY, Z_GUIDE);

        // Preview quad: position is bottom-left corner, so offset by half-size
        float size = OrbTier.TIERS[currentTier].getRadius() * 2f;
        float scale = previewOrbGeom.getLocalScale().x;
        previewOrbGeom.setLocalTranslation(mouseWorldX - scale / 2f,
                PhysicsEngine.DANGER_LINE_Y + 0.8f - scale / 2f, Z_PREVIEW);
    }

    private void handleClick() {
        switch (state) {
            case TITLE:     startGame(); break;
            case PLAYING:   dropOrb();   break;
            case GAME_OVER: startGame(); break;
        }
    }

    // ---- Game Actions ----

    private void startGame() {
        clearAllOrbs();
        particleMgr.clear();
        score = 0;
        comboCount = 0;
        comboTimer = 0;
        dangerTimer = 0;
        canDrop = true;
        dropCooldown = 0;

        currentTier = OrbTier.pickRandomDropTier();
        nextTier = OrbTier.pickRandomDropTier();
        updatePreviewOrb();

        hud.updateScore(0);
        hud.updateBest(bestScore);
        hud.showGamePlay();
        state = GameState.PLAYING;
        System.out.println("[MM] Game started");
    }

    private void dropOrb() {
        if (!canDrop || dropCooldown > 0) return;

        Orb orb = new Orb(mouseWorldX, PhysicsEngine.DANGER_LINE_Y + 0.3f, currentTier);
        orb.setVy(-2f);
        orbs.add(orb);

        OrbVisual vis = new OrbVisual(orb, assetManager, hud.getFont(), Z_ORBS);
        orbVisuals.add(vis);
        orbNode.attachChild(vis.getNode());

        System.out.println("[MM] Drop tier=" + currentTier + " x=" +
                String.format("%.2f", mouseWorldX));

        currentTier = nextTier;
        nextTier = OrbTier.pickRandomDropTier();
        updatePreviewOrb();
        canDrop = false;
        dropCooldown = 0.4f;
    }

    private void clearAllOrbs() {
        for (OrbVisual v : orbVisuals) v.detach();
        orbVisuals.clear();
        orbs.clear();
    }

    // ---- Update ----

    public void update(float tpf) {
        hud.update(tpf);
        if (particleMgr != null) particleMgr.update(tpf);
        syncMouse();

        if (state != GameState.PLAYING) return;

        if (dropCooldown > 0) {
            dropCooldown -= tpf;
            if (dropCooldown <= 0) canDrop = true;
        }

        if (comboTimer > 0) {
            comboTimer -= tpf;
            if (comboTimer <= 0) comboCount = 0;
        }

        List<PhysicsEngine.MergeEvent> merges = physics.step(orbs, tpf);
        for (PhysicsEngine.MergeEvent me : merges) handleMerge(me);

        Iterator<OrbVisual> it = orbVisuals.iterator();
        while (it.hasNext()) {
            OrbVisual v = it.next();
            if (v.getOrb().isMarkedForRemoval() || v.getOrb().isMerging()) {
                v.detach();
                it.remove();
            }
        }
        orbs.removeIf(o -> o.isMarkedForRemoval() || o.isMerging());

        for (OrbVisual v : orbVisuals) v.updatePosition();

        boolean danger = physics.isAnyOrbAboveDanger(orbs);
        if (danger) {
            dangerTimer += tpf;
            hud.showDanger(true, dangerTimer / 2f);
            if (dangerTimer > 2.0f) { triggerGameOver(); return; }
        } else {
            dangerTimer = Math.max(0, dangerTimer - tpf * 2);
            hud.showDanger(false, 0);
        }
    }

    private void handleMerge(PhysicsEngine.MergeEvent me) {
        me.orbA.markForRemoval();
        me.orbB.markForRemoval();

        Orb merged = new Orb(me.x, me.y, me.newTier,
                (me.orbA.getVx() + me.orbB.getVx()) * 0.3f,
                (me.orbA.getVy() + me.orbB.getVy()) * 0.3f + 1f);
        orbs.add(merged);

        OrbVisual vis = new OrbVisual(merged, assetManager, hud.getFont(), Z_ORBS);
        orbVisuals.add(vis);
        orbNode.attachChild(vis.getNode());

        comboCount++;
        comboTimer = 1.5f;
        int pts = OrbTier.TIERS[me.newTier].getPoints() * Math.max(1, comboCount);
        score += pts;
        hud.updateScore(score);
        hud.showCombo(comboCount);

        ColorRGBA c = OrbTier.TIERS[me.newTier].getColor();
        particleMgr.spawnBurst(me.x, me.y, c, 10 + me.newTier * 3);
        particleMgr.spawnFlash(me.x, me.y,
                OrbTier.TIERS[me.newTier].getGlowColor(),
                OrbTier.TIERS[me.newTier].getRadius() * 2f);
    }

    private void triggerGameOver() {
        state = GameState.GAME_OVER;
        boolean newBest = score > bestScore;
        if (newBest) { bestScore = score; prefs.putInt("bestScore", bestScore); }
        hud.showGameOver(score, bestScore, newBest);
        System.out.println("[MM] GAME OVER score=" + score);
    }
}
