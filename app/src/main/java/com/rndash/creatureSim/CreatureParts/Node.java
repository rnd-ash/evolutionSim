package com.rndash.creatureSim.CreatureParts;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.rndash.creatureSim.GameEngine;

/**
 * Represents a node of the creature.
 * These act
 */
public class Node extends CreaturePart {
    protected static int UUID = 0; // ID Counter (to generate UUIDs)
    public final float radius; // Radius of the node
    private final float mass_newtons; // Mass in newtons
    public int node_uuid; // Unique ID
    private final boolean isStationary; // Debugging only
    private Color renderColor; // Render colour of the node
    public Node(float sim_x, float sim_y, float width, boolean isStationary, Color renderColour) {
        this.sim_pos = new Vector(sim_x, sim_y);
        this.radius = width / 2.0F;
        // KG to newtons, let mass = area of node/2
        this.mass_newtons = (float) ((Math.PI * Math.pow(radius, 2.0)*9.81/2));
        updateSimCoords(this.sim_pos);
        this.node_uuid = UUID;
        UUID++;
        this.isStationary = isStationary;
        this.renderColor = renderColour;
    }

    @Override
    public void render(Canvas c, Paint p) {
        p.setColor(renderColor.toArgb());
        c.drawCircle((float) this.screen_pos.getX(), (float) this.screen_pos.getY(), this.radius * GameEngine.PIXELS_PER_M, p);
    }

    @Override
    public void simStepUpdate(long stepMillis) {
        // Only do force calculations if the node is not fixed in world
        if(!isStationary) {
            // Calculate gravity force
            this.forces = this.forces.add(new Vector(0, -9.81).times(mass_newtons));
            // Add forces to velocities
            this.velocities = this.velocities.add(this.forces.times(mass_newtons * (stepMillis/1000F)));
            // Times 0.9 to add for some overall drag
            this.velocities = this.velocities.times(0.9);
            // Now add velocities to position vector to get new position of body
            this.sim_pos = this.sim_pos.add(velocities.times((stepMillis/1000F)));
            // Check if the node is touching the floor
            if (isOnGround()) {
                this.sim_pos.setY(2);
                // Drag the feet on the ground by * 0.2
                this.velocities.setX(this.velocities.getX() * 0.2);
                if (this.velocities.getY() < 0) {
                    // Bouncing detected, retain some negative Y velocity
                    this.velocities.setY(this.velocities.getY() * -0.2);
                }
            }
            // Reset force vector for next step cycle
            this.forces = new Vector(0, 0);
        }
        this.updateSimCoords(this.sim_pos);
    }

    /**
     * Checks if node is on the floor
     * @return Boolean indicating if node is touching the floor
     */
    public boolean isOnGround() {
        return this.sim_pos.getY() <= 2;
    }

    /**
     * Sets the render colour of the node
     * @param c Colour of the node
     */
    public void setRenderColor(Color c) {
        this.renderColor = c;
    }

    /**
     * Generates a copy of this node without references
     * @return A new clone of this node
     */
    public Node clone() {
        Node n = new Node((float) this.getSimPos().getX(), (float) this.getSimPos().getY(), radius*2, isStationary, this.renderColor);
        n.node_uuid = this.node_uuid;
        return n;
    }
}
