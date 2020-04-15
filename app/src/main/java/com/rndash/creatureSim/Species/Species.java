package com.rndash.creatureSim.Species;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import com.rndash.creatureSim.AI.Brain;
import com.rndash.creatureSim.AI.NeuronConnection;
import com.rndash.creatureSim.AI.NeuronConnectionHistory;
import com.rndash.creatureSim.Creature;
import com.rndash.creatureSim.CreatureBuilder;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public class Species {
    ArrayList<Creature> creatures;
    double bestFitness;
    Creature champion;
    double averageFitness;
    int staleness;
    Brain representative;

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

    public void addToSpecies(Creature c) {
        this.creatures.add(c);
    }

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
        return (b1.connections.size() + b2.connections.size() - 2) * matching;
    }

    public double averageWeightDiff(Brain b1, Brain b2) {
        if (b1.connections.size() == 0 || b2.connections.size() == 0) {
            return 0;
        }


        int matching = 0;
        double totalDiff = 0;
        for (NeuronConnection c1 : b1.connections) {
            for (NeuronConnection c2 : b2.connections) {
                if (c1.innovationNumber == c2.innovationNumber) {
                    matching++;
                    totalDiff += Math.abs(c1.weight - c2.weight);
                    break;
                }
            }
        }
        if (matching == 0) { //divide by 0 error
            return 100;
        }
        return totalDiff / matching;
    }

    public void sortSpecies() {
        ArrayList<Creature> temp = new ArrayList<>();

        //selection short
        int index = this.creatures.size();
        for (int i = 0; i < index; i++) {
            double max = 0;
            int maxIndex = 0;
            for (int j = 0; j < this.creatures.size(); j++) {
                if (this.creatures.get(j).fitness > max) {
                    max = this.creatures.get(j).fitness;
                    maxIndex = j;
                }
            }
            temp.add(this.creatures.get(maxIndex));
            this.creatures.remove(maxIndex);
            i--;
            index--;
        }

        // this.players = (ArrayList) temp.clone();
        this.creatures = temp;
        if (this.creatures.size() == 0) {
            this.staleness = 200;
            return;
        }
        //if new best player
        if (this.creatures.get(0).fitness > this.bestFitness) {
            this.staleness = 0;
            this.bestFitness = this.creatures.get(0).fitness;
            this.representative = this.creatures.get(0).brain.clone();
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

    public void cull() {
        ArrayList<Creature> toRemove = new ArrayList<>();
        if (this.creatures.size() > 2) {
            for (int i = this.creatures.size()/2; i < this.creatures.size(); i++) {
                toRemove.add(this.creatures.get(i));
            }
        }
        toRemove.forEach(r -> {this.creatures.remove(r);});
    }

    public void fitnessSharing() {
        for (Creature c : creatures) {
            c.fitness /= creatures.size();
        }
    }
}