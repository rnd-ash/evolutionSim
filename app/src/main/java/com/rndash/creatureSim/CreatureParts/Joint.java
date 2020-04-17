package com.rndash.creatureSim.CreatureParts;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Represents a muscle that acts as a contracting spring
 * that connects 2 nodes together
 */
public class Joint extends CreaturePart {
    private final Node parent; // Anchor point
    final Node child; // Suspend point
    double defaultLength; // Default length
    double targetLength; // Target length for a contraction
    float strength; // Strength
    int stepsContracted = 0; // Counter for muscle contraction to avoid explosive contractions / expansions
    private boolean isContracting = false; // Boolean indicating if the joint is contracting
    private boolean isStatic = false; // --DEBUGGING--

    /**
     * Debugging only - Generates a static muscle that is rigid and does not contract
     * @param n1 Parent node for connection
     * @param n2 Child node for connection
     */
    public Joint(Node n1, Node n2) {
        this(n1, n2, 0.0F);
    }

    /**
     * Generates a spring based muscle joint
     * @param n1 Parent node for connection
     * @param n2 Child node for connection
     * @param strength Strength rating (from 0.1 - 10). If 0 is specified, the joint acts like a rigid body
     */
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

    /**
     * Returns the current length of the joint
     * @return Length of joint
     */
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
    public void simStepUpdate(long stepMillis) {
        super.simStepUpdate(stepMillis);
        Vector v = parent.getSimPos().minus(child.getSimPos());
        double len = v.getLength();
        // If the muscle is contracting, we should slowly decrease the resting length
        // To avoid an explosive contraction
        if (!isStatic || this.getLength() <= defaultLength * 0.5) {
            if (isContracting && targetLength > (defaultLength * 0.5)) {
                stepsContracted++;
                targetLength -= strength / 25; // 4cm per frame of contraction
            }
            // Same logic but to avoid exploisve expansion
            else if (stepsContracted > 0 && targetLength < defaultLength) {
                stepsContracted--;
                targetLength += strength / 25; // 4cm per frame of contraction
            }
        }
        // Implementation of hookes law for spring force calculation
        double distanceFromRest = len - targetLength; // Get how far from resting position we are
        double hooksValue;
        // Calculate the hookes value rating
        if (isStatic) {
            hooksValue =  -100 * distanceFromRest; // Static shouldn't flex. Maximum hooks law value
        } else {
            // * 9.81 such that we go back to kg, which is the mass of the node
            hooksValue = -(strength*9.81) * distanceFromRest;
        }
        // Normalise the force vector
        v.normalise();
        // Calculate how much force should be applied
        Vector force = v.times(hooksValue);
        // Apply the forces to parent and child nodes.
        // Times by 1.25 so it acts like a muscle with body and not a metal spring
        parent.forces = parent.forces.add(force.times(1.25));
        child.forces = child.forces.minus(force.times(1.25));
    }

    /**
     * Called to relax the muscle (Stop contractions)
     */
    public void relax() {
        if (this.isContracting) {
            this.isContracting = false;
        }
    }

    /**
     * Called to contract the muscle
     */
    public void contract() {
        if (!this.isContracting) {
            this.isContracting = true;
        }
    }
}
