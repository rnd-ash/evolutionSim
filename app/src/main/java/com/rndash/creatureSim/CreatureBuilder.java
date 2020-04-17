package com.rndash.creatureSim;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import com.rndash.creatureSim.Creator.Button;
import com.rndash.creatureSim.Creator.ButtonAction;
import com.rndash.creatureSim.CreatureParts.Joint;
import com.rndash.creatureSim.CreatureParts.Node;

import java.util.ArrayList;

/**
 * Class for handling the UI of the creature builder,
 * as well as the blueprint for the creature
 */
public class CreatureBuilder {
    private final Button addJ = new Button("Add Joint", 0, 400, 40, Color.valueOf(Color.WHITE), Color.valueOf(Color.BLACK));
    private final Button addN = new Button("Add Node", 0, 600, 40, Color.valueOf(Color.WHITE), Color.valueOf(Color.BLACK));
    private final Button undo = new Button("Undo", 0, 800, 40, Color.valueOf(Color.WHITE), Color.valueOf(Color.BLACK));
    protected static class NodeData {
        public float x;
        public float y;
        public final float width;
        public final boolean isStationary;
        public final Color col;
        public int uuid;
        public NodeData(float x, float y, float width, boolean isStationary, Color c) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.isStationary = isStationary;
            this.col = c;
        }

        protected void onMove(float x, float y) {
            this.x = x / GameEngine.PIXELS_PER_M;
            this.y = y / GameEngine.PIXELS_PER_M;
        }
        protected void render(Canvas c, Paint p) {
            p.setColor(Color.WHITE);
            c.drawCircle(x * GameEngine.PIXELS_PER_M, y * GameEngine.PIXELS_PER_M, width * GameEngine.PIXELS_PER_M, p);
        }

        protected boolean isTouching(MotionEvent m) {
            boolean isX = m.getX() / GameEngine.PIXELS_PER_M < this.x + width && m.getX() / GameEngine.PIXELS_PER_M > this.x - width;
            boolean isY = m.getY() / GameEngine.PIXELS_PER_M < this.y + width && m.getY() / GameEngine.PIXELS_PER_M > this.y - width;
            return isX && isY;
        }

    }
    protected class JointData {
        public final int parentUUID;
        public final int childUUID;
        public final float strength;
        public final boolean isStatic;
        public JointData(int parent, int child, float strength, boolean isStatic) {
            this.parentUUID = parent;
            this.childUUID = child;
            this.strength = strength;
            this.isStatic = isStatic;
        }
        public void render(Canvas c, Paint p) {
            NodeData n1 = nodes.get(parentUUID);
            NodeData n2 = nodes.get(childUUID);
            p.setColor(Color.BLACK);
            p.setStrokeWidth(10);
            c.drawLine(n1.x * GameEngine.PIXELS_PER_M, n1.y * GameEngine.PIXELS_PER_M,
                    n2.x * GameEngine.PIXELS_PER_M, n2.y * GameEngine.PIXELS_PER_M, p);
        }
    }

    private final ArrayList<Integer> actions = new ArrayList<>();
    public static CreatureBuilder getTestCerature() {
        CreatureBuilder c = new CreatureBuilder();
        Color color = Color.valueOf(0.5F, 0.5F, 0.5F);
        c.addNode(10.0F, 10.0F, 1.0F, false, color);
        c.addNode(15.0F, 10.0F, 1.0F, false, color);
        c.addNode(10.0F, 15.0F, 1.0F, false, color);
        c.addNode(15.0F, 15.0F, 1.0F, false, color);
        c.addJoint(0,1, 2.0F, false);
        c.addJoint(1,2,2.0F, false);
        c.addJoint(2,3,2.0F, false);
        c.addJoint(3,0,2.0F, false);
        c.addJoint(0,2,2.0F, false);
        c.addJoint(3,1,2.0F, false);
        return c;
    }
    public CreatureBuilder() {
        this.joints = new ArrayList<>();
        this.nodes = new ArrayList<>();
        addN.setOnClick(new ButtonAction() {
            @Override
            public void onClick() {
                addNode();
            }
        });
        addJ.setOnClick(new ButtonAction() {
            @Override
            public void onClick() {
                if (inJointEditMode) {
                    inJointEditMode = false;
                    addJ.changeText("Add Joint");
                } else {
                    inJointEditMode = true;
                    addJ.changeText("Back");
                }
            }
        });

        undo.setOnClick(new ButtonAction() {
            @Override
            public void onClick() {
                undoAction();
            }
        });
    }

    boolean inJointEditMode = false;
    public void onClick(MotionEvent event) {
        addJ.detectClick(event);
        undo.detectClick(event);
        addN.detectClick(event);
        if (!inJointEditMode) {
            startJointNodeIndex = null;
            if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                for (NodeData n : nodes) {
                    if (n.isTouching(event)) {
                        n.onMove(event.getX(), event.getY());
                        break;
                    }
                }
            }
        } else {
            if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                if (startJointNodeIndex != null) {
                    screen_x = event.getX();
                    screen_y = event.getY();
                }
                for (int i = 0; i < nodes.size(); i++) {
                    NodeData n = nodes.get(i);
                    if (n.isTouching(event)) {
                        screen_x = event.getX();
                        screen_y = event.getY();
                        if (startJointNodeIndex == null) {
                            startJointNodeIndex = i;
                            break;
                        } else if (i != startJointNodeIndex) {
                            this.joints.add(new JointData(startJointNodeIndex, i, 1.0F, false));
                            startJointNodeIndex = null;
                            break;
                        }
                    }
                }
            }
        }
    }

    protected void addNode() {
        this.nodes.add(new NodeData(20, 20, 1, false, Color.valueOf(Color.RED)));
        this.actions.add(0);
    }

    protected void undoAction() {
        if (actions.size() > 1) {
            switch (actions.get(actions.size() - 1)) {
                case 0:
                    this.nodes.remove(this.nodes.size() - 1);
                case 1:
                    this.joints.remove(this.joints.size() - 1);
            }
            actions.remove(actions.size() - 1);
        }
    }


    private final ArrayList<JointData> joints;
    private final ArrayList<NodeData> nodes;
    public void addJoint(int start, int end, float strength, boolean isRigid) {
        Log.d("ADD-JOINT", String.format("Adding new joint. UUIDS: %d,%d",start, end));
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
            // Now relocate all the nodes (-2.1 needed so they dont spawn through the ground)
            n.x -= (minPos_x);
            n.y -= (minPos_y-2);
        }
        Log.d("SPAWN", String.format("Min pivot: %.2f, %.2f", minPos_x, minPos_y));
    }

    public void assumeEditPosition() {
        resetPos();
        nodes.forEach(n -> {n.y += 10; n.x += 40;}); // Sort of center the UI
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

    public boolean isCreatureValid() {
        return this.joints.size() > 1 && this.nodes.size() >= 2;
    }

    public void drawTempLine(Canvas c, Paint p, NodeData start, float screen_x, float screen_y) {
        p.setStrokeWidth(10);
        p.setColor(Color.BLACK);
        c.drawLine(start.x * GameEngine.PIXELS_PER_M, start.y * GameEngine.PIXELS_PER_M, screen_x, screen_y, p);
    }
    private Integer startJointNodeIndex = null;
    private float screen_x;
    private float screen_y;
    public void render(Canvas c, Paint p) {
        addJ.draw(c, p);
        if (!inJointEditMode) {
            addN.draw(c, p);
        }
        for (JointData j : this.joints) {
            j.render(c, p);
        }
        for (NodeData n : this.nodes) {
            n.render(c, p);
        }
        if (startJointNodeIndex != null) {
            drawTempLine(c, p, nodes.get(startJointNodeIndex), screen_x, screen_y);
        }
    }
}
