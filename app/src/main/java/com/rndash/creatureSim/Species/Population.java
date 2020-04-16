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
    ArrayList<Creature> creatures;
    Creature bestCreature;
    public double bestScore;
    public double globalBestScore;
    int generation;
    public ArrayList<NeuronConnectionHistory> history;
    ArrayList<Species> species;
    public int batchNo;
    boolean isPopulating;
    CreatureBuilder model;
    private double generationsSinceNew;
    double simTime;
    public double maxTravelled;
    Creature currentBest;
    public Population(CreatureBuilder cb, int size) {
        this.model = cb;
        this.model.resetPos();
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
        this.simTime = 0;
        for (int i = 0; i < size; i++) {
            this.creatures.add(new Creature(model));
            this.creatures.get(this.creatures.size()-1).brain.fullyConnect(this.history);
            this.creatures.get(this.creatures.size()-1).brain.generateNetwork();
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
        if (this.currentBest != null) {
            if (this.currentBest.brain != null) {
                this.currentBest.brain.render(c, p, 700, 100, 0, 0);
            }
        }
    }

    public void simulationTick(long millis) {
        if (this.isPopulating) {return;}
        this.simTime += millis;
        int alive = 0;
        for (Creature c : creatures) {
            if (!c.isDead()) {
                alive++;
                c.simulationStep(millis);
                c.aiTick();
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
        if (alive == 0) {
            // Halt rendering whilst we modify everything
            this.isPopulating = true;
            creatures.forEach(Creature::kill);
            this.batchNo++;
            this.naturalSelection();
            this.isPopulating = false;
        }
    }

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

    public void naturalSelection() {
        this.simTime = 0;
        Creature previousBest = this.creatures.get(0);
        creatures.forEach(Creature::reset);
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
        Log.d("Natural selection", String.format("Generation %d, Number of mutations: %d, Species: %d", this.generation, this.history.size(), this.species.size()));
        double averageSum = this.getAvgFitnessSum();
        ArrayList<Creature> children = new ArrayList<>();
        for (Species value : this.species) { //for each this.species
            children.add(value.champion.clone());
            int NoOfChildren = (int) Math.floor(value.averageFitness / averageSum * this.creatures.size()) - 1;
            //Log.d("Natural Selection", String.format("Species %d needs to make %d new children", j, NoOfChildren));
            for (int i = 0; i < Math.abs(NoOfChildren); i++) {
                children.add(value.makeChild(this.history));
            }
        }
        if (children.size() < this.creatures.size()) {
            children.add(previousBest.clone());
        }
        while (children.size() < this.creatures.size()) {
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
        species.removeIf(s -> (s.bestFitness >= 15));
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
