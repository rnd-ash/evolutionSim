package com.rndash.creatureSim.AI;

import java.util.ArrayList;

/**
 * Represents a history of mutations for a given NeuronConnection
 */
public class NeuronConnectionHistory {
    final Neuron parent; // Parent neuron
    final Neuron child; // Child neuron
    final int innovationNumber; // Mutation ID
    final ArrayList<Integer> innovationNumbers = new ArrayList<>();
    public NeuronConnectionHistory(Neuron from, Neuron to, int innovation, ArrayList<Integer> innovationNos) {
        this.parent = from;
        this.child = to;
        this.innovationNumber = innovation;
        this.innovationNumbers.clear();
        this.innovationNumbers.addAll(innovationNos);
    }

    /**
     * Check to see if a mutation in the given Brain matches this mutation
     * @param b Brain that we are checking against
     * @param from Parent neuron for mutation check
     * @param to Child neuron for mutation check
     * @return True if this mutation is identical to an existing on
     */
    public boolean matches(Brain b, Neuron from, Neuron to) {
        // Mutations can only be the same if we have the same number of ID's
        if (b.connections.size() == this.innovationNumbers.size()) {
            // Check if the the neurons in question match this objects pair
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
