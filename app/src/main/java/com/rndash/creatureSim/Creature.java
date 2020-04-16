package com.rndash.creatureSim;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import com.rndash.creatureSim.AI.Brain;
import com.rndash.creatureSim.CreatureParts.Joint;
import com.rndash.creatureSim.CreatureParts.Node;
import com.rndash.creatureSim.CreatureParts.Vector;

import java.util.ArrayList;

public class Creature {
    public ArrayList<Node> nodes = new ArrayList<>();
    public ArrayList<Joint> joints = new ArrayList<>();
    public double fitness = 0;
    public int lifespan = 500;
    public double bestScore = 0;
    private boolean isDead = false;
    public double score = -1;
    public int generation = 0;
    public Brain brain;
    Color color;
    CreatureBuilder cb;
    public double avgDistance = 0;
    private double staleness = 0;
    public Creature(CreatureBuilder c) {
        this.cb = c; // Reference to blueprint
        this.nodes = c.getNodes();
        this.joints = c.getJoints(this.nodes);
        this.color = Color.valueOf((float) Math.random(), (float) Math.random(), (float) Math.random());
        brain = new Brain(this.nodes.size()*3, this.joints.size());
    }

    public void drawCreature(Canvas c, Paint p) {
        for (Joint j : joints) {
            j.render(c, p);
        }
        for (Node n : nodes) {
            n.render(c, p);
        }
    }

    public void simulationStep(long stepMillis) {
        for (Joint j : joints) {
            j.simStepUpdate(stepMillis);
        }
        for (Node n : nodes) {
            n.simStepUpdate(stepMillis);
        }

    }

    public void reset() {
        this.nodes = cb.getNodes();
        this.joints = cb.getJoints(this.nodes);
    }

    public Vector getRelativeDistanceToBrain(Node n) {
        return n.getSimPos().minus(this.nodes.get(0).getSimPos());
    }

    public boolean isDead() {
        return isDead;
    }

    public Creature crossover(Creature p2) {
        Creature child = new Creature(this.cb);
        child.brain = this.brain.crossover(p2.brain);
        child.brain.generateNetwork();
        child.color = Color.valueOf(
                (float) (this.color.red() + Math.random()/10F),
                (float) (this.color.green() + Math.random()/10F),
                (float) (this.color.blue() + Math.random()/10F)
        );
        child.nodes.forEach((Node n) -> {n.setRenderColor(child.color);});
        return child;
    }

    public void calculateFitness() {
        this.fitness = (this.score * this.score);
        this.fitness *= (0.9 + (1-0.9) * (this.score/this.lifespan) / (0.9));
        //Log.d("FITNESS", String.format("Creature fitness is %.4f, Score was %.4f, Distance is %.4f", this.fitness, this.score, this.nodes.stream().mapToDouble(j -> j.getSimPos().getX()).sum() / this.nodes.size()));
    }

    public void kill() {
        this.isDead = true;
    }

    public void aiTick() {
        if (isDead) {
            return;
        }
        // Get vision status
        ArrayList<Double> vision = new ArrayList<>();
        for (Node n : nodes) {
            vision.add(n.getVelocities().getX());
            vision.add(n.getVelocities().getY());
            vision.add(n.getForces().getX());
            vision.add(n.getForces().getY());
        }

        // Now act on the NN
        ArrayList<Double> decision = this.brain.feedForward(vision);
        for (int i = 0; i < decision.size(); i++) {
            if (decision.get(i) < 0.5) {
                joints.get(i).relax();
            } else {
                joints.get(i).contract();
            }
        }
        this.lifespan++;
        // Get average X distance from start
        double distance = this.nodes.stream().mapToDouble(n -> n.getSimPos().getX()).sum() / this.nodes.size();
        this.avgDistance = distance;
        this.score = Math.min(1, distance / 100F); // 100M should be the end target
        if (this.score > this.bestScore + 0.01) { // For a 10 seconds period we should see at least a 1m difference
            this.bestScore = this.score;
            this.staleness = 0;
        }
        if (this.staleness == 500) {
            this.isDead = true;
        }
        if (this.isFlat()) {
            this.isDead = true;
        }
        this.staleness++;
    }

    /**
     * Checks if the creature has pancaked itself - in which case it must die.
     * @return True if its a pancake, False if it isn't
     */
    private boolean isFlat() {
        return this.nodes.stream().mapToDouble(n -> (n.getSimPos().getY())).average().getAsDouble() < 2.1;
    }

    public Creature clone() {
        Creature clone = new Creature(this.cb);
        clone.brain = this.brain.clone();
        clone.fitness = this.fitness;
        clone.brain.generateNetwork();
        clone.generation = this.generation;
        clone.color = this.color;
        return clone;
    }

}
