package com.rndash.creatureSim;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import com.rndash.creatureSim.CreatureParts.Joint;
import com.rndash.creatureSim.CreatureParts.Node;
import com.rndash.creatureSim.CreatureParts.Vector;

import java.util.ArrayList;
import java.util.UUID;

// Class for handling custom creatures that are being built
public class CreatureBuilder {
    protected class NodeData {
        public float x;
        public float y;
        public float width;
        public boolean isStationary;
        public Color col;
        public int uuid;
        public NodeData(float x, float y, float width, boolean isStationary, Color c) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.isStationary = isStationary;
            this.col = c;
        }
    }

    protected class JointData {
        public int parentUUID;
        public int childUUID;
        public float strength;
        public boolean isStatic;
        public JointData(int parent, int child, float strength, boolean isStatic) {
            this.parentUUID = parent;
            this.childUUID = child;
            this.strength = strength;
            this.isStatic = isStatic;
        }
    }


    public static CreatureBuilder getTestCerature() {
        CreatureBuilder c = new CreatureBuilder();
        Color color = Color.valueOf(0.5F, 0.5F, 0.5F);
        c.addNode(10.0F, 10.0F, 1.0F, false, color);
        c.addNode(15.0F, 10.0F, 1.0F, false, color);
        c.addNode(10.0F, 15.0F, 1.0F, false, color);
        c.addNode(15.0F, 15.0F, 1.0F, false, color);
        c.addJoint(0,1, 1.0F, false);
        c.addJoint(1,2,1.0F, false);
        c.addJoint(2,3,1.0F, false);
        c.addJoint(3,0,1.0F, false);
        c.addJoint(0,2,1.0F, false);
        c.addJoint(3,1,1.0F, false);
        return c;
    }
    public CreatureBuilder() {
        this.joints = new ArrayList<>();
        this.nodes = new ArrayList<>();
    }
    private ArrayList<JointData> joints;
    private ArrayList<NodeData> nodes;

    public void addJoint(int start, int end, float strength, boolean isRigid) {
        this.joints.add(new JointData(start, end, strength, isRigid));
    }

    public void addNode(float x, float y, float width, boolean isStationary, Color c) {
        this.nodes.add(new NodeData(x, y, width, isStationary, c));
    }

    public void resetPos() {
        // Start pos should be so that the furthest and lowest node is at 0,0
        double minPos_x = Double.POSITIVE_INFINITY;
        double minPos_y = Double.POSITIVE_INFINITY;
        for (NodeData n : nodes) {
            if (n.y < minPos_y) {
                minPos_y = n.y;
            }
            if (n.x < minPos_x) {
                minPos_x = n.x;
            }
        }
        for (NodeData n : nodes) {
            // Now relocate all the nodes (-2 needed so they dont spawn through the ground)
            n.x -= minPos_x;
            n.y -= minPos_y;
        }
        Log.d("SPAWN", String.format("Min pivot: %.2f, %.2f", minPos_x, minPos_y));
    }

    public ArrayList<Joint> getJoints(ArrayList<Node> connections) {
        ArrayList<Joint> clone = new ArrayList<>();
        for (JointData j : this.joints) {
            Joint joint;
            if(j.isStatic) {
                 joint = new Joint(connections.get(j.parentUUID), connections.get(j.childUUID));
            } else {
                joint = new Joint(connections.get(j.parentUUID), connections.get(j.childUUID), j.strength);
            }
            clone.add(joint);
        }
        return clone;
    }

    public ArrayList<Node> getNodes() {
        ArrayList<Node> clone = new ArrayList<>();
        for (NodeData n : this.nodes) {
            Node newNode = new Node(n.x, n.y,n.width, n.isStationary, n.col);
            newNode.node_uuid = n.uuid;
            clone.add(newNode);
        }
        return clone;
    }

    public void DrawCreature(Canvas c, Paint p) {

    }
}
