package com.rndash.creatureSim.CreatureParts;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.annotation.NonNull;

public class Joint extends CreaturePart {
    private Node parent; // Anchor point
    Node child; // Suspend point
    double defaultLength;
    double targetLength;
    float strength;
    private boolean isContracting = false;
    private boolean isStatic = false;

    public Joint(Node n1, Node n2) {
        this(n1, n2, 0.0F);
    }

    public Joint(Node n1, Node n2, float strength) {
        this.parent = n1;
        this.child = n2;
        if (strength == 0.0) {
            isStatic = true;
        } else {
            this.strength = strength * 10;
        }
        defaultLength = getLength();
        targetLength = getLength();
    }

    private double getLength() {
        return parent.getSimPos().minus(child.getSimPos()).getLength();
    }

    @Override
    public void render(Canvas c, Paint p) {
        if (isStatic) {
            p.setColor(Color.GRAY);
            p.setStrokeWidth(5);
        } else if (isContracting){
            p.setColor(Color.RED);
            p.setStrokeWidth(strength * (float) (defaultLength / getLength()));
        } else {
            p.setColor(Color.BLACK);
            p.setStrokeWidth(strength * (float) (defaultLength / getLength()));
        }
        c.drawLine((float) parent.screen_pos.getX(), (float) parent.screen_pos.getY(), (float) child.screen_pos.getX(), (float) child.screen_pos.getY(), p);
    }

    @Override
    boolean isCoordsTouching(float s_x, float s_y) {
        return false;
    }

    int stepsContracted = 0;
    @Override
    public void simStepUpdate(long stepMillis) {
        super.simStepUpdate(stepMillis);
        Vector v = parent.getSimPos().minus(child.getSimPos());
        double len = v.getLength();
        if (!isStatic || this.getLength() <= defaultLength * 0.5) {
            if (isContracting && targetLength > (defaultLength * 0.5)) {
                stepsContracted++;
                targetLength -= strength / 50; // 1cm per frame of contraction
            } else if (stepsContracted > 0 && targetLength < defaultLength) {
                stepsContracted--;
                targetLength += strength / 50;
            }
        }

        double distanceFromRest = len - targetLength;
        double hooksValue;
        if (isStatic) {
            hooksValue =  -100 * distanceFromRest; // Static shouldn't flex. Maximum hooks law value
        } else {
            hooksValue = -strength*2 * distanceFromRest;
        }
        v.normalise();
        Vector force = v.times(hooksValue);
        parent.forces = parent.forces.add(force);
        child.forces = child.forces.minus(force);
    }

    // 2 inputs for the AI, they can either relax or contract the joints (muscles)
    public void relax() {
        if (this.isContracting) {
            this.isContracting = false;
        }
    }

    public void contract() {
        if (!this.isContracting) {
            this.isContracting = true;
        }
    }

    public int getVisionPosition() {
        if (this.isContracting) { return 1; }
        else { return 0; }
    }

    public Joint clone(Node n1, Node n2) {
        if (n1.node_uuid != this.parent.node_uuid || n2.node_uuid != this.child.node_uuid) {
            throw new IllegalArgumentException("Parent/child node UUID does not match!");
        }
        Joint j = new Joint(n1, n2);
        j.isContracting = this.isContracting;
        j.isStatic = this.isStatic;
        j.targetLength = this.targetLength;
        j.defaultLength = this.defaultLength;
        j.stepsContracted = this.stepsContracted;
        j.strength = this.strength;
        return j;
    }
}
