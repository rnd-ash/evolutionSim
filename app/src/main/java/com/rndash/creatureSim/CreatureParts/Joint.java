package com.rndash.creatureSim.CreatureParts;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Joint extends CreaturePart {
    private final Node parent; // Anchor point
    final Node child; // Suspend point
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

    int stepsContracted = 0;
    @Override
    public void simStepUpdate(long stepMillis) {
        super.simStepUpdate(stepMillis);
        Vector v = parent.getSimPos().minus(child.getSimPos());
        double len = v.getLength();
        if (!isStatic || this.getLength() <= defaultLength * 0.5) {
            if (isContracting && targetLength > (defaultLength * 0.5)) {
                stepsContracted++;
                targetLength -= strength / 25; // 1cm per frame of contraction
            } else if (stepsContracted > 0 && targetLength < defaultLength) {
                stepsContracted--;
                targetLength += strength / 25;
            }
        }

        double distanceFromRest = len - targetLength;
        double hooksValue;
        if (isStatic) {
            hooksValue =  -100 * distanceFromRest; // Static shouldn't flex. Maximum hooks law value
        } else {
            // * 9.81 such that we go back to kg, which is the mass of the node
            hooksValue = -(strength*9.81) * distanceFromRest;
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
}
