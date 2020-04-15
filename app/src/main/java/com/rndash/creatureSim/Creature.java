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
    public int fitness = 0;
    public int lifespan = 500;
    public double bestScore = 0;
    private boolean isDead = false;
    public double score = 0;
    public int generation = 0;
    public Brain brain;
    Color color;
    CreatureBuilder cb;
    int epoch;

    public Creature(CreatureBuilder c) {
        epoch = 0;
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
        this.fitness = (int) this.score * lifespan;
    }

    public void aiTick() {
        if (isDead) {
            return;
        }
        // Get vision status
        ArrayList<Double> vision = new ArrayList<>();
        for (Node n : nodes) {
            vision.add(getRelativeDistanceToBrain(n).getX());
            vision.add(getRelativeDistanceToBrain(n).getY());
            vision.add(n.getSimPos().getY());
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
        // Now get the current score of the AI
        this.score = nodes.get(0).getSimPos().getX();
        epoch++;
        if (epoch >= 50) {
            epoch = 0;
            if (this.score >= this.bestScore + 0.20c) {
                lifespan++;
                this.bestScore = this.score;
            } else {
                isDead = true;
            }
        }
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
