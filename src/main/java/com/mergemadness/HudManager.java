package com.mergemadness;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;

/**
 * Manages the heads-up display: score, best score, combo text,
 * and overlay messages (title screen, game over).
 */
public class HudManager {

    private final Node guiNode;
    private final BitmapFont font;
    private final int screenWidth;
    private final int screenHeight;

    // HUD elements
    private BitmapText scoreText;
    private BitmapText bestText;
    private BitmapText comboText;
    private BitmapText titleText;
    private BitmapText titleSubText;
    private BitmapText gameOverText;
    private BitmapText gameOverScoreText;
    private BitmapText gameOverBestText;
    private BitmapText tapToPlayText;
    private BitmapText newBestText;
    private BitmapText dangerText;

    private float comboFadeTimer = 0;
    private float dangerPulseTimer = 0;

    public HudManager(Node guiNode, AssetManager assetManager, int screenWidth, int screenHeight) {
        this.guiNode = guiNode;
        this.font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        createHudElements();
    }

    private void createHudElements() {
        // Score display (top left)
        scoreText = createText("0", 32, new ColorRGBA(0f, 0.94f, 1f, 1f));
        scoreText.setLocalTranslation(16, screenHeight - 16, 0);
        guiNode.attachChild(scoreText);

        // Best score (below score)
        bestText = createText("BEST: 0", 14, new ColorRGBA(1f, 1f, 1f, 0.4f));
        bestText.setLocalTranslation(16, screenHeight - 52, 0);
        guiNode.attachChild(bestText);

        // Combo display (center top)
        comboText = createText("", 24, new ColorRGBA(1f, 0.89f, 0.1f, 1f));
        comboText.setLocalTranslation(screenWidth / 2f - 60, screenHeight - 20, 0);
        comboText.setColor(new ColorRGBA(1f, 0.89f, 0.1f, 0f));
        guiNode.attachChild(comboText);

        // Danger warning
        dangerText = createText("⚠ DANGER!", 20, new ColorRGBA(1f, 0.1f, 0.3f, 0f));
        dangerText.setLocalTranslation(screenWidth / 2f - 60, screenHeight - 80, 0);
        guiNode.attachChild(dangerText);

        // Title screen
        titleText = createText("MERGE MADNESS", 36, new ColorRGBA(1f, 0.18f, 0.48f, 1f));
        centerText(titleText, screenHeight / 2f + 60);
        guiNode.attachChild(titleText);

        titleSubText = createText("Drop & merge orbs — don't overflow!", 16, new ColorRGBA(1f, 1f, 1f, 0.5f));
        centerText(titleSubText, screenHeight / 2f + 20);
        guiNode.attachChild(titleSubText);

        tapToPlayText = createText("CLICK TO PLAY", 22, new ColorRGBA(0f, 0.94f, 1f, 1f));
        centerText(tapToPlayText, screenHeight / 2f - 40);
        guiNode.attachChild(tapToPlayText);

        // Game Over screen (hidden initially)
        gameOverText = createText("GAME OVER", 36, new ColorRGBA(1f, 0.1f, 0.3f, 1f));
        centerText(gameOverText, screenHeight / 2f + 80);
        gameOverText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        guiNode.attachChild(gameOverText);

        gameOverScoreText = createText("0", 48, new ColorRGBA(0f, 0.94f, 1f, 1f));
        centerText(gameOverScoreText, screenHeight / 2f + 20);
        gameOverScoreText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        guiNode.attachChild(gameOverScoreText);

        newBestText = createText("★ NEW BEST! ★", 20, new ColorRGBA(1f, 0.89f, 0.1f, 1f));
        centerText(newBestText, screenHeight / 2f - 20);
        newBestText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        guiNode.attachChild(newBestText);

        gameOverBestText = createText("BEST: 0", 16, new ColorRGBA(1f, 1f, 1f, 0.4f));
        centerText(gameOverBestText, screenHeight / 2f - 50);
        gameOverBestText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        guiNode.attachChild(gameOverBestText);
    }

    private BitmapText createText(String text, float size, ColorRGBA color) {
        BitmapText t = new BitmapText(font, false);
        t.setText(text);
        t.setSize(size);
        t.setColor(color);
        return t;
    }

    private void centerText(BitmapText text, float y) {
        text.setLocalTranslation(
            (screenWidth - text.getLineWidth()) / 2f,
            y,
            0
        );
    }

    // ---- Updates ----

    public void updateScore(int score) {
        scoreText.setText(String.valueOf(score));
    }

    public void updateBest(int best) {
        bestText.setText("BEST: " + best);
    }

    public void showCombo(int count) {
        if (count >= 2) {
            comboText.setText("COMBO x" + count);
            comboText.setColor(new ColorRGBA(1f, 0.89f, 0.1f, 1f));
            centerText(comboText, screenHeight - 20);
            comboFadeTimer = 1.5f;
        }
    }

    public void showDanger(boolean active, float intensity) {
        if (active) {
            dangerPulseTimer += 0.1f;
            float alpha = (float) (Math.sin(dangerPulseTimer * 6) * 0.4 + 0.5) * Math.min(1f, intensity);
            dangerText.setColor(new ColorRGBA(1f, 0.1f, 0.3f, alpha));
        } else {
            dangerText.setColor(new ColorRGBA(1f, 0.1f, 0.3f, 0f));
            dangerPulseTimer = 0;
        }
    }

    public void update(float tpf) {
        // Fade combo
        if (comboFadeTimer > 0) {
            comboFadeTimer -= tpf;
            if (comboFadeTimer <= 0) {
                comboText.setColor(new ColorRGBA(1f, 0.89f, 0.1f, 0f));
            }
        }
    }

    // ---- Screen State ----

    public void showTitleScreen() {
        titleText.setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);
        titleSubText.setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);
        tapToPlayText.setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);
        gameOverText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        gameOverScoreText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        gameOverBestText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        newBestText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
    }

    public void showGamePlay() {
        titleText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        titleSubText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        tapToPlayText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        gameOverText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        gameOverScoreText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        gameOverBestText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        newBestText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
    }

    public void showGameOver(int score, int best, boolean isNewBest) {
        titleText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        titleSubText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        tapToPlayText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);

        gameOverText.setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);
        gameOverScoreText.setText(String.valueOf(score));
        centerText(gameOverScoreText, screenHeight / 2f + 20);
        gameOverScoreText.setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);
        gameOverBestText.setText("BEST: " + best);
        centerText(gameOverBestText, screenHeight / 2f - 50);
        gameOverBestText.setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);

        if (isNewBest) {
            newBestText.setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);
        } else {
            newBestText.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        }

        // Reuse tapToPlayText as retry prompt
        tapToPlayText.setText("CLICK TO RETRY");
        centerText(tapToPlayText, screenHeight / 2f - 90);
        tapToPlayText.setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);
    }

    public BitmapFont getFont() {
        return font;
    }
}
