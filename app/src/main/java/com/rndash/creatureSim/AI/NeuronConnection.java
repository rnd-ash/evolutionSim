package com.rndash.creatureSim.AI;

import java.util.Random;

public class NeuronConnection implements NetworkRenderable{
    public double weight;
    public Neuron parent;
    public Neuron child;
    public boolean enabled;
    public int innovationNumber;

    public NeuronConnection(Neuron parent, Neuron child, double w, int innovation) {
        this.parent = parent;
        this.child = child;
        this.weight = w;
        this.innovationNumber = innovation;
        this.enabled = true;
    }

    public void mutateWeight() {
        double rnd = Math.random();
        if (rnd < 0.1) { // 10% chance of a mutation
            this.weight = (Math.random() * 2) -1; // Random from -1 to 1;
        } else {
            this.weight += (new Random().nextGaussian() / 50); // TODO random gaussian
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
     * @param from
     * @param to
     */
    public NeuronConnection clone(Neuron from, Neuron to) {
        NeuronConnection clone = new NeuronConnection(from, to, this.weight, this.innovationNumber);
        clone.enabled = this.enabled;
        return clone;
    }


    @Override
    public void render() {

    }
}