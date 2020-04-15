package com.rndash.creatureSim.CreatureParts;

import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.annotation.NonNull;
import com.rndash.creatureSim.GameEngine;

public abstract class CreaturePart {
    protected Vector screen_pos = new Vector(0.0, 0.0);
    protected Vector sim_pos = new Vector(0.0, 0.0);
    protected Vector forces = new Vector(0.0, 0.0);
    protected Vector velocities = new Vector(0.0, 0.0);

    public abstract void render(Canvas c, Paint p);

    protected void updateSimCoords(Vector sim_pos) {
            this.screen_pos.setX((sim_pos.getX() * GameEngine.PIXELS_PER_M) + (GameEngine.CAMERA_POS_SIM_X * GameEngine.PIXELS_PER_M));
            this.screen_pos.setY(GameEngine.max_screen_height - ((float) sim_pos.getY() * GameEngine.PIXELS_PER_M));
    }

    abstract boolean isCoordsTouching(float s_x, float s_y);

    public void simStepUpdate(long stepMillis) {
    }


    public void onClicked(float s_x, float s_y) {
    }

    public void onMoved(float s_x, float s_y) {
    }

    public Vector getSimPos() { return this.sim_pos; }
}
