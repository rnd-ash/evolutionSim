package com.rndash.creatureSim.Species;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import com.rndash.creatureSim.AI.NeuronConnectionHistory;
import com.rndash.creatureSim.Creature;
import com.rndash.creatureSim.CreatureBuilder;

import java.util.ArrayList;
import java.util.Comparator;

public class Population {
    static int MAX_SIMULATIONS = 5;
    static int MAX_CREATURES_PER_SIM = 20;
    ArrayList<Creature> creatures;
    Creature bestCreature;
    public double bestScore;
    public double globalBestScore;
    int generation;
    ArrayList<NeuronConnectionHistory> history;
    ArrayList<Species> species;
    public int batchNo;
    boolean isPopulating;
    CreatureBuilder model;
    private double generationsSinceNew;
    public Population(CreatureBuilder cb) {
        this.model = cb;
        cb.resetPos();
        this.creatures = new ArrayList<>();
        this.bestCreature = null;
        this.bestScore = 0;
        this.globalBestScore = 0;
        this.generation = 1;
        this.history = new ArrayList();
        this.creatures = new ArrayList<>();
        this.species = new ArrayList<>();
        this.isPopulating = false;
        this.batchNo = 0;
        for (int i = 0; i < MAX_SIMULATIONS; i++) {
            for (int j = 0; j < MAX_CREATURES_PER_SIM; j++) {
                this.creatures.add(new Creature(model));
                this.creatures.get(this.creatures.size()-1).brain.fullyConnect(this.history);
                this.creatures.get(this.creatures.size()-1).brain.generateNetwork();
            }
        }
        this.generationsSinceNew = 0;
    }
    public void render(Canvas c, Paint p) {
        if (this.isPopulating) {return;}
        for (Creature creature : creatures) {
            if (!creature.isDead()) {
                creature.drawCreature(c, p);
            }
        }
    }

    public void simulationTick(long millis) {
        if (this.isPopulating) {return;}
        int alive = 0;
        for (Creature c : creatures) {
            if (!c.isDead()) {
                alive++;
                c.simulationStep(millis);
                c.aiTick();
                if (c.score > this.globalBestScore) {
                    this.globalBestScore = c.score;
                }
            }
        }
        if (alive == 0) {
            this.batchNo++;
            // Halt rendering whilst we modify everything
            this.isPopulating = true;
            this.naturalSelection();
            this.isPopulating = false;
        }
    }

    void setBestPlayer() {
        Creature temp = this.species.get(0).creatures.get(0);
        temp.generation = this.generation;

        if (temp.score >= this.bestScore) {
            this.bestScore = temp.bestScore;
            this.bestCreature = temp;
        }
    }

    public void naturalSelection() {
        Creature previousBest = this.creatures.get(0);
        this.speciate();
        this.calculateFitness();
        this.sortSpecies();
        this.cullSpecies();
        this.setBestPlayer();
        this.killStaleSpecies();
        this.killBadSpecies();

        if (this.generationsSinceNew>= 0 || this.bestScore > 100) {
            this.generationsSinceNew = 0;
        }

        double averageSum = this.getAvgFitnessSum();
        ArrayList<Creature> children = new ArrayList<>();
        for (int j = 0; j < this.species.size(); j++) { //for each this.species
            children.add(this.species.get(j).champion.clone());
            int NoOfChildren = (int) Math.floor(this.species.get(j).averageFitness / averageSum * this.creatures.size()) - 1;
            Log.d("Natural Selection", String.format("Species %d needs to make %d new children", j, NoOfChildren));
            for (int i = 0; i < Math.abs(NoOfChildren); i++) {
                children.add(this.species.get(j).makeChild(this.history));
            }
        }
        if (children.size() < this.creatures.size()) {
            children.add(previousBest.clone());
        }
        while (children.size() < MAX_CREATURES_PER_SIM * MAX_SIMULATIONS) {
            children.add(this.species.get(0).makeChild(history));
        }
        this.creatures = children;
        this.generation++;
        this.generationsSinceNew++;
        for (int i = 0; i < this.creatures.size(); i++) { //generate networks for each of the children
            this.creatures.get(i).brain.generateNetwork();
        }
    }

    void speciate() {
        species.forEach(s -> s.creatures.clear());
        for (int i = 0; i < this.creatures.size(); i++) {
            boolean found = false;
            for (Species s : this.species) {
                if (s.sameSpecies(this.creatures.get(i).brain)) {
                    s.addToSpecies(this.creatures.get(i));
                    found = true;
                    break;
                }
            }
            if (!found) {
                this.species.add(new Species(this.creatures.get(i)));
            }
        }
    }

    void calculateFitness() {
        this.creatures.forEach(Creature::calculateFitness);
    }


    void sortSpecies() {
        species.forEach(Species::sortSpecies);
        this.species.sort(new Comparator<Species>() {
            @Override
            public int compare(Species o1, Species o2) {
                return Double.compare(o1.bestFitness, o2.bestFitness);
            }
        });
    }

    void killStaleSpecies() {
        ArrayList<Species> toRemove = new ArrayList<>();
        for (Species s : this.species) {
            if (s.staleness >= 15) {
                toRemove.add(s);
            }
        }
        toRemove.forEach(r -> {this.species.remove(r);});
    }

    /**
     * Species is so bad it shouldn't be allowed to have any more children
     * Kill the species now
     */
    void killBadSpecies() {
        double averageSum = this.getAvgFitnessSum();
        ArrayList<Species> toRemove = new ArrayList<>();
        for (Species s : this.species) {
            if (s.averageFitness / averageSum * this.creatures.size() < 1) {
                toRemove.add(s);
            }
        }
        toRemove.forEach(r -> {this.species.remove(r);});
    }

    double getAvgFitnessSum() {
        return this.species.stream().mapToDouble(s -> s.averageFitness).sum();
    }

    void cullSpecies() {
        this.species.forEach(s -> {
            s.cull();
            s.fitnessSharing();
            s.setAverage();
        });
    }
}
