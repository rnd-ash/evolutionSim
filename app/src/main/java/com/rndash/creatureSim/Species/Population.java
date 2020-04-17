package com.rndash.creatureSim.Species;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import com.rndash.creatureSim.AI.NeuronConnectionHistory;
import com.rndash.creatureSim.Creature;
import com.rndash.creatureSim.CreatureBuilder;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Represents an entire population of species
 */
public class Population {
    ArrayList<Creature> creatures; // Creatures being simulated now
    Creature bestCreature; // Best creature in all generations
    public double bestScore; // Best score in current run
    public double globalBestScore; // Best score out of all runs
    int generation; // Generation counter
    public final ArrayList<NeuronConnectionHistory> history; // Mutation history for all species
    final ArrayList<Species> species; // Species list
    public int batchNo; // Batch number (not generation!)
    boolean isPopulating; // lock boolean indicating if we are re-populating the creatures list
    final CreatureBuilder model; // Model for all new creatures to use
    private double generationsSinceNew; // Generations since we had to reset the network entirely due to staleness
    public double maxTravelled; // Longest distance travelled
    Creature currentBest; // Current best creature
    public Population(CreatureBuilder cb, int size) {
        this.model = cb;
        this.model.resetPos();
        this.creatures = new ArrayList<>();
        this.bestCreature = null;
        this.bestScore = 0;
        this.globalBestScore = 0;
        this.generation = 1;
        this.history = new ArrayList<>();
        this.creatures = new ArrayList<>();
        this.species = new ArrayList<>();
        this.isPopulating = false;
        this.batchNo = 0;
        this.generationsSinceNew = 0;
        // Generate some new species
        for (int i = 0; i < size; i++) {
            this.creatures.add(new Creature(model));
            this.creatures.get(this.creatures.size()-1).brain.fullyConnect(this.history);
            this.creatures.get(this.creatures.size()-1).brain.generateNetwork();
        }
    }

    /**
     * Renders all the creatures currently being simulated to the display
     * @param c Canvas object
     * @param p Paint object
     */
    public void render(Canvas c, Paint p) {
        // Currently touching the creatures list on another thread, don't render!
        if (this.isPopulating) {return;}
        for (Creature creature : creatures) {
            if (!creature.isDead()) {
                // only render alive creatures
                creature.drawCreature(c, p);
            }
        }
        // Render the brain if its not null
        if (this.currentBest != null) {
            if (this.currentBest.brain != null) {
                this.currentBest.brain.render(c, p, 700, 100, 0, 0);
            }
        }
    }

    // Used to control when AI is triggered
    private long lastAITime = 0;
    /**
     * Run a simulation tick on all currently simulated species
     * @param millis Number of milliseconds elapsed since we last did a simulation tick
     */
    public void simulationTick(long millis) {
        // Limit the AI to do 10 times per second, if not the creatures
        // Look like a jittery mess!
        lastAITime+= millis;
        boolean doAITick = this.lastAITime >= 100;
        if (doAITick) {
            this.lastAITime -= 100;
        }
        // Currently touching the creatures list on another thread, don't simulate!
        if (this.isPopulating) {return;}
        int alive = 0;
        for (Creature c : creatures) {
            // Only simulate alive creatures
            if (!c.isDead()) {
                alive++;
                c.simulationStep(millis);
                if (doAITick) {c.aiTick(); };
                if (c.score > this.globalBestScore) {
                    this.globalBestScore = c.score;
                    this.maxTravelled = c.avgDistance;
                    this.bestCreature = c;
                }
                if  (c.score > this.bestScore) {
                    this.currentBest = c;
                }
            }
        }
        // No more left alive, time to re-generate the species
        if (alive == 0) {
            // Halt rendering whilst we modify everything
            this.isPopulating = true;
            creatures.forEach(Creature::kill);
            this.batchNo++;
            this.naturalSelection();
            this.isPopulating = false;
        }
    }

    /**
     * Sets the best player out of all species
     */
    void setBestPlayer() {
        try {
            Creature temp = this.species.get(0).creatures.get(0);
            temp.generation = this.generation;

            if (temp.score >= this.bestScore) {
                this.bestScore = temp.bestScore;
                this.bestCreature = temp;
            }
        } catch (IndexOutOfBoundsException e) {
            Log.e("BESTPLAYER", String.format("Error. Index out of bounds! Species %d has only %d players", 0, species.get(0).creatures.size()));
        }
    }

    /**
     * -- HERE BE DRAGONS --
     * Runs a simulated natural selection
     */
    public void naturalSelection() {
        Creature previousBest = this.creatures.get(0); // get our previous champion
        creatures.forEach(Creature::reset); // Reset the position models for all creatures
        this.speciate(); // Now speciate current species
        this.calculateFitness(); // Calculate fitness for all species
        this.sortSpecies(); // Sort the species based on fitness
        this.cullSpecies(); // Thanos snap the bottom 50% out of existence
        this.setBestPlayer(); // Now select the new best player
        this.killStaleSpecies(); // Kill any more species that are stale
        this.killBadSpecies(); // Kill any more species that are deemed as bad

        // Reset species generation counter as everything is still OK
        if (this.generationsSinceNew >= 0 || this.bestScore > 100) {
            this.generationsSinceNew = 0;
        }
        Log.d("Natural selection", String.format("Generation %d, Number of mutations: %d", this.generation, this.history.size()));
        // Get an average fitness for reproduction
        double averageSum = this.getAvgFitnessSum();
        ArrayList<Creature> children = new ArrayList<>();
        // Do this in all remaining species
        for (Species value : this.species) {
            children.add(value.champion.clone()); // Clone the champion as a child
            // Calculate how many more children should be added
            int NoOfChildren = (int) Math.floor(value.averageFitness / averageSum * this.creatures.size()) - 1;
            // Now make more children for the current species
            for (int i = 0; i < Math.abs(NoOfChildren); i++) {
                children.add(value.makeChild(this.history));
            }
        }
        // Check now that we have correct number of children. If not, add the previous best champion
        if (children.size() < this.creatures.size()) {
            children.add(previousBest.clone());
        }
        // Still need to add more children! Just make children based on the best species
        if (this.species.size() > 0) {
            while (children.size() < this.creatures.size()) {
                children.add(this.species.get(0).makeChild(history));
            }
        } else {
            // No more good species, re-populate
            while (children.size() < this.creatures.size()) {
                Creature c = new Creature(model);
                c.brain.fullyConnect(this.history);
                children.add(c);
            }
        }
        this.creatures = children;
        this.generation++;
        this.generationsSinceNew++;
        for (Creature creature : this.creatures) { //generate networks for each of the children
            creature.brain.generateNetwork();
        }
    }

    /**
     * Generates some new species if they don't already exist
     */
    void speciate() {
        species.forEach(s -> s.creatures.clear());
        for (Creature creature : this.creatures) {
            boolean found = false;
            for (Species s : this.species) {
                if (s.sameSpecies(creature.brain)) {
                    s.addToSpecies(creature);
                    found = true;
                    break;
                }
            }
            if (!found) {
                this.species.add(new Species(creature));
            }
        }
    }

    /**
     * Calculates fitness for each creature in current simulation
     */
    void calculateFitness() {
        this.creatures.forEach(Creature::calculateFitness);
    }

    /**
     * Sorts the species list based on average fitness (Descending)
     */
    void sortSpecies() {
        species.forEach(Species::sortSpecies);
        this.species.sort(new Comparator<Species>() {
            @Override
            public int compare(Species o1, Species o2) {
                return Double.compare(o1.bestFitness, o2.bestFitness);
            }
        });
    }

    /**
     * Kill any species that have gone stale
     */
    void killStaleSpecies() {
        species.removeIf(s -> (s.staleness >= 15));
    }

    /**
     * Species is so bad it shouldn't be allowed to have any more children
     * Kill the species now
     */
    void killBadSpecies() {
        double averageSum = this.getAvgFitnessSum();
        species.removeIf(s -> (s.averageFitness / averageSum * this.creatures.size() < 1));
        species.removeIf(s -> (s.creatures.size() == 0));
    }

    /**
     * Get average fitness of all species
     * @return Average fitness
     */
    double getAvgFitnessSum() {
        return this.species.stream().mapToDouble(s -> s.averageFitness).sum();
    }

    /**
     * Remove the bottom 50% of species out of existanse!
     */
    void cullSpecies() {
        this.species.forEach(s -> {
            s.cull();
            s.fitnessSharing();
            s.setAverage();
        });
    }
}
