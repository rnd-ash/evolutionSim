package com.rndash.creatureSim.AI;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;

public class Neuron implements NetworkRenderable {
    public int id;
    public double inputSum;
    public double outputValue;
    public ArrayList<NeuronConnection> outputs;
    public int layer;
    public Neuron(int no) {
        this.id = no;
        this.inputSum = 0;
        this.outputValue = 0;
        this.outputs = new ArrayList<>();
        this.layer = 0;
    }

    private double sigmoid(double input) {
        return 1.0 / (1.0 + Math.pow(Math.E, -input));
    }

    public void activate() {
        if (this.layer != 0) {
            this.outputValue = sigmoid(this.inputSum);
        }

        outputs.forEach((NeuronConnection c) -> {
            if (c.enabled) {
                c.child.inputSum += c.weight * outputValue;
            }
        });
    }

    public boolean isConnectedTo(Neuron n) {
        if (n.layer == this.layer) {
            return false;
        }
        if (n.layer < this.layer) {
            for (NeuronConnection c : n.outputs) {
                if (c.child == this) {
                    return true;
                }
            }
        } else {
            for (NeuronConnection c : this.outputs) {
                if (c.child == n) {
                    return true;
                }
            }
        }
        return false;
    }

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
