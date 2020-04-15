package com.rndash.creatureSim.AI;

import java.util.ArrayList;

public class NeuronConnectionHistory {
    Neuron parent;
    Neuron child;
    int innovationNumber;
    ArrayList<Integer> innovationNumbers = new ArrayList<>();
    public NeuronConnectionHistory(Neuron from, Neuron to, int innovation, ArrayList<Integer> innovationNos) {
        this.parent = from;
        this.child = to;
        this.innovationNumber = innovation;
        this.innovationNumbers.clear();
        this.innovationNumbers.addAll(innovationNos);
    }

    public boolean matches(Brain b, Neuron from, Neuron to) {
        if (b.connections.size() == this.innovationNumbers.size()) {
            if (from.id == this.parent.id && to.id == this.child.id) {
                for (int i = 0; i < b.connections.size(); i++) {
                    if (!this.innovationNumbers.contains(b.connections.get(i).innovationNumber)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
