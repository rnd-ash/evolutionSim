package com.rndash.creatureSim.CreatureParts;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.rndash.creatureSim.GameEngine;

/**
 * Part of the creature that has physics and is rendered to the screen
 */
public abstract class CreaturePart {
    protected final Vector screen_pos = new Vector(0.0, 0.0); // Screen position center
    protected Vector sim_pos = new Vector(0.0, 0.0); // Simulation position center (represents meters in the sim)
    protected Vector forces = new Vector(0.0, 0.0); // Forces applied in the simulation
    protected Vector velocities = new Vector(0.0, 0.0); // Velocities applied in the simulation

    /**
     * Returns the velocities vector
     * @return  velocity Vector
     */
    public Vector getVelocities() { return this.velocities; }

    /**
     * Returns the force vector
     * @return force Vector
     */
    public Vector getForces() { return this.forces; }

    /**
     * Renders the object on screen
     * @param c Canvas object
     * @param p Paint object
     */
    public abstract void render(Canvas c, Paint p);

    /**
     * Updates the screen-space coordinates based on the simulation position
     * @param sim_pos Simulation position vector
     */
    protected void updateSimCoords(Vector sim_pos) {
            this.screen_pos.setX((sim_pos.getX() * GameEngine.PIXELS_PER_M) + (GameEngine.CAMERA_POS_SIM_X * GameEngine.PIXELS_PER_M));
            this.screen_pos.setY(GameEngine.max_screen_height - ((float) sim_pos.getY() * GameEngine.PIXELS_PER_M));
    }

    /**
     * Updates the physics simulation
     * @param stepMillis Milliseconds elapsed since last physics update
     */
    public void simStepUpdate(long stepMillis) {
    }

    /**
     * Returns the Simulation position
     * @return Simulation position center of this object
     */
    public Vector getSimPos() { return this.sim_pos; }
}
