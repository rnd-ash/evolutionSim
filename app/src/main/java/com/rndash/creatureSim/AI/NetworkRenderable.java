package com.rndash.creatureSim.AI;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Interface for objects within the network that are going to be rendered on screen
 */
public interface NetworkRenderable {
    /**
     * Function to use when rendering to screen
     * @param c Canvas object
     * @param p Paint object
     * @param x Start X coordinate
     * @param y Start Y coordinate
     * @param h Height of object
     * @param w Width of object
     */
    void render(Canvas c, Paint p, int x, int y, int h, int w);
}
