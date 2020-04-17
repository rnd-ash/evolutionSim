package com.rndash.creatureSim.Species;

import com.rndash.creatureSim.AI.Brain;
import com.rndash.creatureSim.AI.NeuronConnection;
import com.rndash.creatureSim.AI.NeuronConnectionHistory;
import com.rndash.creatureSim.Creature;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Represents a species
 * Being a group of creatures with similar mutations
 */
public class Species {
    final ArrayList<Creature> creatures; // List of creatures
    double bestFitness; // Best fitness
    Creature champion; // Champion for this species
    double averageFitness; // Average fitness for this species
    int staleness; // Staleness rating
    Brain representative; // This species champion brain
    double maxTravelled = 0; // Longest time the species could walk for

    // TODO - Make these 3 params configurable
    // This leads to different natural selection behaviour
    final double execessCoeff = 1;
    final double weightDiffCoeff = 0.5;
    final double compatibilityThreshold = 3;


    public Species(Creature c) {
        this.creatures = new ArrayList<>();
        this.bestFitness = 0;
        this.averageFitness = 0;
        this.staleness = 0;

        if (c != null) {
            this.creatures.add(c);
            this.bestFitness = c.fitness;
            this.representative = c.brain.clone();
            this.champion = c.clone();
        }
    }


    /**
     * Check if species are the same based on their brain
     * @param b Comparison brain
     * @return Boolean indicating if brains are the same or not, indicating an identical species
     */
    public boolean sameSpecies(Brain b) {
        double excessAndDisjoint = this.getExcessDisjoint(b, this.representative);
        double averageWeightDiff = this.averageWeightDiff(b, this.representative);
        double normaliser = b.connections.size() - 20;
        if (normaliser < 1) {
            normaliser = 1;
        }

        double compatibility = (this.execessCoeff * excessAndDisjoint / normaliser) + (this.weightDiffCoeff * averageWeightDiff); //compatibility formula
        return (this.compatibilityThreshold > compatibility);
    }

    /**
     * Adds a new creature to the species
     * @param c Creature to add
     */
    public void addToSpecies(Creature c) {
        this.creatures.add(c);
    }

    /**
     * Gets a value for how different the 2 brains are
     * @param b1 Input brain 1 for comparison
     * @param b2 Input brain 2 for comparison
     * @return Number of different connections * Total number of connections
     */
    public double getExcessDisjoint(Brain b1, Brain b2) {
        double matching = 0.0;
        for (NeuronConnection c1 : b1.connections) {
            for (NeuronConnection c2 : b2.connections) {
                if (c1.innovationNumber == c2.innovationNumber) {
                    matching++;
                    break;
                }
            }
        }
        // -2 needed cause each brain has an additional bias weight
        return b1.connections.size() + b2.connections.size() - 2 * (matching);
    }

    /**
     * Gets average weight difference between 2 brains over all neurons
     * @param b1 Comparison brain 1
     * @param b2 Comparison brain 2
     * @return Average differences between the brains' weight
     */
    public double averageWeightDiff(Brain b1, Brain b2) {
        // No connections, return 0
        if (b1.connections.size() == 0 || b2.connections.size() == 0) {
            return 0;
        }


        int matching = 0;
        double totalDiff = 0;
        for (NeuronConnection c1 : b1.connections) {
            for (NeuronConnection c2 : b2.connections) {
                if (c1.innovationNumber == c2.innovationNumber) {
                    // Matching connection
                    matching++;
                    totalDiff += Math.abs(c1.weight - c2.weight);
                    break;
                }
            }
        }
        if (matching == 0) { //avoids divide by 0
            return 100;
        }
        return totalDiff / matching;
    }

    /**
     * Sort this species based on creature ranking
     */
    public void sortSpecies() {
        this.creatures.sort(new Comparator<Creature>() {
            @Override
            public int compare(Creature o1, Creature o2) {
                return Double.compare(o1.fitness, o2.fitness);
            }
        });
        if (this.creatures.size() == 0) {
            this.staleness = 200;
            return;
        }
        //if new best player
        if (this.creatures.get(0).fitness > this.bestFitness) {
            this.staleness = 0;
            this.bestFitness = this.creatures.get(0).fitness;
            this.representative = this.creatures.get(0).brain.clone();
            this.maxTravelled = this.creatures.get(0).avgDistance;
        } else { //if no new best player
            this.staleness++;
        }
    }


    void setAverage() {
        this.averageFitness = this.creatures.stream().mapToDouble(c -> c.fitness).sum() / this.creatures.size();
    }

    /**
     * Make a child creature from a random mother and father
     * @param history Mutation history
     * @return A new baby creature
     */
    Creature makeChild(ArrayList<NeuronConnectionHistory> history) {
        Creature baby;
        // 25% chance the baby will be a twin of an existing creature
        if (Math.random() < 0.25) {
            baby = this.selectCreature();
        } else {
            // Choose a random mother + father
            Creature mum = this.selectCreature();
            Creature dad = this.selectCreature();

            // crossover the strongest parent with the weakest parent
            // to make the strongest baby possible
            if (mum.fitness < dad.fitness) {
                baby = dad.crossover(mum);
            } else {
                baby = mum.crossover(dad);
            }
            // Mutate the baby's brain
            baby.brain.mutate(history);
        }
        return baby;
    }

    /**
     * Returns a random creature in the species based on a random threshold
     * @return Random creature
     */
    public Creature selectCreature() {
        double fitnessSum = creatures.stream().mapToDouble(c -> c.fitness).sum();
        double random = Math.random() * fitnessSum;
        double runningSum = 0;
        for (Creature c : creatures) {
            runningSum += c.fitness;
            if (runningSum > random) {
                return c;
            }
        }
        return this.creatures.get(0);
    }

    /**
     * Remove the bottom 50% of the creatures in the list
     */
    public void cull() {
        ArrayList<Creature> toRemove = new ArrayList<>();
        if (this.creatures.size() > 2) {
            for (int i = this.creatures.size()/2; i < this.creatures.size(); i++) {
                toRemove.add(this.creatures.get(i));
            }
        }
        toRemove.forEach(this.creatures::remove);
    }

    /**
     * Share fitness across all creatures in the list, helps avoid the creatures in the species
     * becoming too different
     */
    public void fitnessSharing() {
        for (Creature c : creatures) {
            c.fitness /= creatures.size();
        }
    }
}