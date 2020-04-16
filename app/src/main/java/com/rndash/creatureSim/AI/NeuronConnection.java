package com.rndash.creatureSim.AI;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

/**
 * Represents a connection between 2 neurons
 */
public class NeuronConnection implements NetworkRenderable{
    public double weight; // This connections weight
    public final Neuron parent; // Parent neuron
    public final Neuron child; // Child neuron
    public boolean enabled; // Check to see if this is enabled or not
    public final int innovationNumber; // Mutation number - used for mutation comparisons

    public NeuronConnection(Neuron parent, Neuron child, double w, int innovation) {
        this.parent = parent;
        this.child = child;
        this.weight = w;
        this.innovationNumber = innovation;
        this.enabled = true;
    }

    /**
     * Randomly modify the weight to act as a 'mutation'
     */
    public void mutateWeight() {
        double rnd = Math.random();
        if (rnd < 0.1) { // 10% chance of a mutation
            this.weight = (Math.random() * 2) -1; // Random from -1 to 1;
        } else {
            this.weight += (new Random().nextGaussian() / 50);
            // Clamp weights so are not out of expected range
            if (this.weight > 1) {
                this.weight = 1;
            }
            if (this.weight < -1) {
                this.weight = -1;
            }
        }
    }

    /**
     * Clones the attribute of this network connection to a new Neuron
     * connection pair
     * @param from Parent neuron
     * @param to Child neuron
     */
    public NeuronConnection clone(Neuron from, Neuron to) {
        NeuronConnection clone = new NeuronConnection(from, to, this.weight, this.innovationNumber);
        clone.enabled = this.enabled;
        return clone;
    }


    @Override
    public void render(Canvas c, Paint p, int x, int y, int h, int w) {
        if (this.enabled) {
            p.setStrokeWidth(3);
            // Trigger threshold for creature
            if (parent.outputValue > 0.5) {
                p.setColor(Color.YELLOW);
            } else {
                if (weight > 0.5) {
                    p.setColor(Color.GRAY);
                } else {
                    p.setColor(Color.BLACK);
                }
            }
            c.drawLine(x, y, h, w, p);
        }
    }
}
