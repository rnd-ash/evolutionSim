package com.rndash.creatureSim;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.rndash.creatureSim.AI.Brain;
import com.rndash.creatureSim.CreatureParts.Joint;
import com.rndash.creatureSim.CreatureParts.Node;

import java.util.ArrayList;

public class Creature {
    public ArrayList<Node> nodes; // List of nodes on the creature
    public ArrayList<Joint> joints; // List of joints on the creature
    public double fitness = 0; // Fitness rating
    public int lifespan = 0; // Lifespan
    public double bestScore = 0; // Best score the creature has achieved
    private boolean isDead = false; // Check to see if the creature is dead
    public double score = -1; // Current score holder
    public int generation = 0; // Generation of the creature
    public Brain brain; // Brain object for the creature
    Color color; // Render colour
    final CreatureBuilder cb; // Blueprint for the creature
    public double avgDistance = 0;
    private double staleness = 0;
    public Creature(CreatureBuilder c) {
        this.cb = c; // Reference to blueprint
        this.nodes = c.getNodes();
        this.joints = c.getJoints(this.nodes);
        this.color = Color.valueOf((float) Math.random(), (float) Math.random(), (float) Math.random());
        /*
        Set the brain input and output count
        Inputs:
            For each node, its X and Y forces and velocities
        Outputs:
            For each joint, single boolean indicating if it needs to contract or not
         */
        brain = new Brain(this.nodes.size()*4, this.joints.size());
    }

    /**
     * Draws the creature on display
     * @param c Canvas object
     * @param p Paint object
     */
    public void drawCreature(Canvas c, Paint p) {
        for (Joint j : joints) {
            j.render(c, p);
        }
        for (Node n : nodes) {
            n.render(c, p);
        }
    }

    /**
     * Physics simulation step
     * @param stepMillis Interval in MS for the simulation
     */
    public void simulationStep(long stepMillis) {
        for (Joint j : joints) {
            j.simStepUpdate(stepMillis);
        }
        for (Node n : nodes) {
            n.simStepUpdate(stepMillis);
        }

    }

    /**
     * Resets the positions of all the nodes
     */
    public void reset() {
        this.nodes = cb.getNodes();
        this.joints = cb.getJoints(this.nodes);
    }

    public boolean isDead() {
        return isDead;
    }

    /**
     * Generates a new baby creature from this creature and a less-dominant parent
     * @param p2
     * @return
     */
    public Creature crossover(Creature p2) {
        Creature child = new Creature(this.cb); // Generate a new creature with this creature's blueprint
        child.brain = this.brain.crossover(p2.brain); // Clone the brain with genetics
        child.brain.generateNetwork(); // Setup the child's network
        // Mutate the child's colour so its a bit different
        child.color = Color.valueOf(
                (float) (this.color.red() + Math.random()/10F),
                (float) (this.color.green() + Math.random()/10F),
                (float) (this.color.blue() + Math.random()/10F)
        );
        child.nodes.forEach((Node n) -> {n.setRenderColor(child.color);});
        return child;
    }

    /**
     * Calculates the fitness rating for the creature
     */
    public void calculateFitness() {
        this.fitness = (this.score * this.score);
        this.fitness *= (0.9 + (1-0.9) * (this.score/this.lifespan) / (0.9));
        //Log.d("FITNESS", String.format("Creature fitness is %.4f, Score was %.4f, Distance is %.4f", this.fitness, this.score, this.nodes.stream().mapToDouble(j -> j.getSimPos().getX()).sum() / this.nodes.size()));
    }

    /**
     * Kills the creature (sad)
     */
    public void kill() {
        this.isDead = true;
    }

    /**
     * Does the AI simulation on this creature
     */
    public void aiTick() {
        // Skip if its dead
        if (isDead) {
            return;
        }
        // Get the creature AI inputs (AKA its vision)
        ArrayList<Double> vision = new ArrayList<>();
        for (Node n : nodes) {
            vision.add(n.getVelocities().getX());
            vision.add(n.getVelocities().getY());
            vision.add(n.getForces().getX());
            vision.add(n.getForces().getY());
        }

        // Now get the result of the network and respond based on its outputs
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
        if (distance < 0) { // Creature went backwards, kill
            this.isDead = true;
        }
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

    /**
     * Creates an exact clone of this creature
     * @return this creature's clone
     */
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
