package com.rndash.creatureSim.CreatureParts;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import androidx.annotation.NonNull;
import com.rndash.creatureSim.GameEngine;

import java.util.ArrayList;

public class Node extends CreaturePart {
    protected static int UUID = 0;
    public final float radius;
    private final float mass_newtons;
    public int node_uuid;
    private boolean isStationary;
    private Color renderColor;
    public Node(float sim_x, float sim_y, float width, boolean isStationary, Color renderColour) {
        this.sim_pos = new Vector(sim_x, sim_y);
        this.radius = width / 2.0F;
        this.mass_newtons = (float) ((Math.PI * Math.pow(radius, 2.0)*2));
        updateSimCoords(this.sim_pos);
        this.node_uuid = UUID;
        UUID++;
        this.isStationary = isStationary;
        this.renderColor = renderColour;
    }

    @Override
    public void onMoved(float s_x, float s_y) {
    }

    @Override
    public void render(Canvas c, Paint p) {
        p.setColor(renderColor.toArgb());
        c.drawCircle((float) this.screen_pos.getX(), (float) this.screen_pos.getY(), this.radius * GameEngine.PIXELS_PER_M, p);
    }

    @Override
    boolean isCoordsTouching(float s_x, float s_y) {
        return false;
    }

    @Override
    public void simStepUpdate(long stepMillis) {
        if(!isStationary) {
            this.forces = this.forces.add(new Vector(0, -9.81).times(mass_newtons));
            this.velocities = this.velocities.add(this.forces.times(mass_newtons * (stepMillis/1000F)));
            this.velocities = this.velocities.times(0.99);
            this.sim_pos = this.sim_pos.add(velocities.times((stepMillis/1000F)));
            if (isOnGround()) {
                this.sim_pos.setY(2);
                this.velocities.setX(this.velocities.getX() * 0.3);
                if (this.velocities.getY() < 0) {
                    this.velocities.setY(this.velocities.getY() * -0.2);
                }
            }
            this.forces = new Vector(0, 0);
        }
        this.updateSimCoords(this.sim_pos);
    }

    public boolean isOnGround() {
        return this.sim_pos.getY() <= 2;
    }

    public void setPos(Vector v) {
        this.sim_pos = v;
    }

    public void setRenderColor(Color c) {
        this.renderColor = c;
    }

    public Node clone() {
        Node n = new Node((float) this.getSimPos().getX(), (float) this.getSimPos().getY(), radius*2, isStationary, this.renderColor);
        n.node_uuid = this.node_uuid;
        return n;
    }
}
