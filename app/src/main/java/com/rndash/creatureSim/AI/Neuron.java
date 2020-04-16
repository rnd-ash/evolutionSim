package com.rndash.creatureSim.AI;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Represents a neuron within the network
 */
public class Neuron implements NetworkRenderable {
    public final int id; // Unique ID of the neuron
    public double inputSum; // Input sum from all inputs
    public double outputValue; // Output value from network
    public final ArrayList<NeuronConnection> outputs; // All outputs from this neuron
    public int layer; // Layer number
    public Neuron(int no) {
        this.id = no;
        this.inputSum = 0;
        this.outputValue = 0;
        this.outputs = new ArrayList<>();
        this.layer = 0;
    }

    /**
     * Sigmoid activation based on input value
     * @param input Network input
     * @return Sigmoid result (Range 0 to 1)
     */
    private double sigmoid(double input) {
        return 1.0 / (1.0 + Math.pow(Math.E, -input));
    }

    /**
     * Activate the neuron
     */
    public void activate() {
        // Neuron has others connected to it, so get sigmoid of the input first
        if (this.layer != 0) {
            this.outputValue = sigmoid(this.inputSum);
        }

        // Send the output value to all its connected neurons...
        outputs.forEach((NeuronConnection c) -> {
            if (c.enabled) { // but ONLY if the connection is enabled!
                c.child.inputSum += c.weight * outputValue;
            }
        });
    }

    /**
     * Checks if this neuron is connected to another neuron
     * @param n Neuron to check against
     * @return True if the neurons are connected, false if they are not
     */
    public boolean isConnectedTo(Neuron n) {
        // Neurons can't be connected. Not in the same layer!
        if (n.layer == this.layer) {
            return false;
        }
        // Check backwards as the querying neuron is behind this one
        if (n.layer < this.layer) {
            for (NeuronConnection c : n.outputs) {
                if (c.child == this) {
                    return true;
                }
            }
        } else {
            // Normal, check all neurons this is connected to
            for (NeuronConnection c : this.outputs) {
                if (c.child == n) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a clone of this neuron since JVM does not support
     * direct memory copy, we have to copy all the values from this
     * to the clone
     * @return A clone of this neuron
     */
    public Neuron clone() {
        Neuron clone = new Neuron(this.id);
        clone.layer = this.layer;
        return clone;
    }

    @Override
    public void render(Canvas c, Paint p, int x, int y, int h, int w) {
        p.setColor(Color.RED);
        c.drawCircle(x, y, w/2F, p);
    }
}
