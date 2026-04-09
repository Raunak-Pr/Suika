package com.mergemadness;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.system.AppSettings;

/**
 * Merge Madness — Main Application Entry Point
 * 
 * An addictive Suika-style merge game built with jMonkeyEngine 3.8.1-stable.
 * Drop orbs into the arena. Matching orbs merge into larger tiers.
 * Don't let them overflow past the danger line!
 */
public class MergeMadnessApp extends SimpleApplication {

    private GameManager gameManager;

    public static void main(String[] args) {
        MergeMadnessApp app = new MergeMadnessApp();

        AppSettings settings = new AppSettings(true);
        settings.setTitle("Merge Madness");
        settings.setWidth(420);
        settings.setHeight(720);
        settings.setResizable(true);
        settings.setVSync(true);
        settings.setFrameRate(60);
        settings.setSamples(4); // anti-aliasing
        settings.setGammaCorrection(false);

        app.setSettings(settings);
        app.setShowSettings(false); // skip the jME settings dialog
        app.setPauseOnLostFocus(false);
        app.start();
    }

    @Override
    public void reshape(int w, int h) {
        // Set viewport fractions before super.reshape() recalculates the frustum,
        // then force-restore our fixed frustum afterwards.
        if (gameManager != null) {
            gameManager.prepareViewport(w, h);
        }
        super.reshape(w, h);
        if (gameManager != null) {
            gameManager.onResize(w, h);
        }
    }

    @Override
    public void simpleInitApp() {
        // Disable default fly camera and show mouse cursor
        flyCam.setEnabled(false);
        inputManager.setCursorVisible(true);

        // Set background color
        viewPort.setBackgroundColor(new ColorRGBA(0.039f, 0.039f, 0.102f, 1f)); // #0a0a1a

        // Disable stats and FPS display
        setDisplayStatView(false);
        setDisplayFps(false);

        // Initialize the game manager
        gameManager = new GameManager(this);
        gameManager.initialize();
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (gameManager != null) {
            gameManager.update(tpf);
        }
    }

    public GameManager getGameManager() {
        return gameManager;
    }
}
