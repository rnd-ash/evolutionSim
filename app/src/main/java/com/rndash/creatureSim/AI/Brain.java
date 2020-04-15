package com.rndash.creatureSim.AI;

import android.graphics.Color;

import java.util.ArrayList;

public class Brain {
    public ArrayList<NeuronConnection> connections;
    public int inputs;
    public int outputs;
    public int layers;
    public int nextNeuron;
    public ArrayList<Neuron> nodes;
    public ArrayList<Neuron> network;
    public int biasNode;
    public Brain(int inputs, int outputs) {
        this(inputs, outputs, false);
    }

    public Brain(int inputs, int outputs, boolean crossover) {
        this.connections = new ArrayList();
        this.nodes = new ArrayList<>();
        this.inputs = inputs;
        this.outputs = outputs;
        this.layers = 3; // TODO Modify for better performance - Maybe 3 or 4 would work?
        this.nextNeuron = 0;
        this.network = new ArrayList<>();
        if (crossover) {
            return;
        }

        for (int i = 0; i < inputs; i++) {
            nodes.add(new Neuron(i));
            this.nextNeuron++;
            this.nodes.get(i).layer = 0;
        }

        for (int i = 0; i < outputs; i++) {
            nodes.add(new Neuron(i+this.inputs));
            nodes.get(i + this.inputs).layer = 1;
            this.nextNeuron++;
        }

        this.nodes.add(new Neuron(this.nextNeuron)); // Bias
        this.biasNode = this.nextNeuron;
        this.nextNeuron++;
        this.nodes.get(this.biasNode).layer = 0;
    }

    public Neuron getNeuron(int number) {
        for (Neuron n : this.nodes) {
            if (n.id == number) {
                return n;
            }
        }
        return null;
    }

    public void connectNeurons() {
        this.nodes.forEach((Neuron n) -> {
            n.outputs.clear();
        });

        for (NeuronConnection connection : this.connections) {
            connection.parent.outputs.add(connection);
        }
    }

    public ArrayList<Double> feedForward(ArrayList<Double> inputs) {
        for (int i = 0; i < this.inputs; i++) {
            this.nodes.get(i).outputValue = inputs.get(i);
        }
        this.nodes.get(this.biasNode).outputValue = 1;

        network.forEach(Neuron::activate);
        ArrayList<Double> outputs = new ArrayList<>();

        for (int i = 0; i < this.outputs; i++) {
            outputs.add(this.nodes.get(this.inputs + i).outputValue);
        }

        for (Neuron node : this.nodes) {
            node.inputSum = 0;
        }
        return outputs;
    }

    public void generateNetwork() {
        this.connectNeurons();
        this.network.clear();
        for (int l =0; l < this.layers; l++) { // For each layer
            for (Neuron node : this.nodes) { // For each node
                if (node.layer == l) { // If node is in layer
                    this.network.add(node);
                }
            }
        }
    }

    /**
     * Returns the innovation number for the new mutation
     *
     * If the mutation has never been seen before, its given a new unique number
     * If the mutation matches a previous mutation, then it will have the same number as the previous
     * matching mutation
     * @param innovationHistory
     * @param parent
     * @param child
     */
    public int getInnovationNumber(ArrayList<NeuronConnectionHistory> innovationHistory, Neuron parent, Neuron child) {
        boolean isNew = true;
        int connectionNumber = nextNeuron;
        for (NeuronConnectionHistory neuronConnectionHistory : innovationHistory) {
            if (neuronConnectionHistory.matches(this, parent, child)) {
                isNew = false;
                connectionNumber = neuronConnectionHistory.innovationNumber;
                break;
            }
        }
        if (isNew) {
            ArrayList<Integer> innoNumbers = new ArrayList<>();
            for (NeuronConnection connection : this.connections) {
                innoNumbers.add(connection.innovationNumber);
            }
            innovationHistory.add(new NeuronConnectionHistory(parent, child, connectionNumber, innoNumbers));
            nextNeuron++;
        }
        return connectionNumber;
    }

    /**
     * Mutate the network by adding a new node
     * Pick a random connection, disable it, then add 2 new connections
     * @param innovationHistory
     */
    public void addNode(ArrayList<NeuronConnectionHistory> innovationHistory) {
        if (this.connections.size() == 0) {
            this.addConnection(innovationHistory);
            return;
        }
        int randomChoice = (int) Math.floor(Math.random() * this.connections.size());
        while (this.connections.get(randomChoice).parent == this.nodes.get(this.biasNode) && this.connections.size() != 1) {
            randomChoice = (int) Math.floor(Math.random() * this.connections.size());
        }
        this.connections.get(randomChoice).enabled = false; // Disable the random connection
        int newNodeNo = this.nextNeuron;
        this.nodes.add(new Neuron(newNodeNo));
        this.nextNeuron++;
        int connectionInnovationNumber = this.getInnovationNumber(innovationHistory, this.connections.get(randomChoice).parent, this.getNeuron(newNodeNo));
        this.connections.add(new NeuronConnection(this.connections.get(randomChoice).parent, this.getNeuron(newNodeNo), 1, connectionInnovationNumber));


        connectionInnovationNumber = this.getInnovationNumber(innovationHistory, this.getNeuron(newNodeNo), this.connections.get(randomChoice).child);
        //add a new connection from the new node with a weight the same as the disabled connection
        this.connections.add(new NeuronConnection(this.getNeuron(newNodeNo), this.connections.get(randomChoice).child, this.connections.get(randomChoice).weight, connectionInnovationNumber));
        this.getNeuron(newNodeNo).layer = this.connections.get(randomChoice).parent.layer + 1;


        connectionInnovationNumber = this.getInnovationNumber(innovationHistory, this.nodes.get(this.biasNode), this.getNeuron(newNodeNo));
        //connect the bias to the new node with a weight of 0
        this.connections.add(new NeuronConnection(this.nodes.get(this.biasNode), this.getNeuron(newNodeNo), 0, connectionInnovationNumber));
        //if the layer of the new node is equal to the layer of the output node of the old connection then a new layer needs to be created
        //more accurately the layer numbers of all layers equal to or greater than this new node need to be incrimented
        if (this.getNeuron(newNodeNo).layer == this.connections.get(randomChoice).child.layer) {
            for (int i = 0; i < this.nodes.size() - 1; i++) { //dont include this newest node
                if (this.nodes.get(i).layer >= this.getNeuron(newNodeNo).layer) {
                    this.nodes.get(i).layer++;
                }
            }
            this.layers++;
        }
        this.connectNeurons();
    }

    /**
     * Checks the new choice for new connection to check if its stupid
     * @param r1 New neuron 1 ID
     * @param r2 New neuron 2 ID
     * @return boolean indicating if the connection is a stupid choice
     */
    private boolean randomConnectionsAreShit(int r1, int r2) {
        if (this.nodes.get(r1).layer == this.nodes.get(r2).layer) return true; // Same layer! (Can't connect)
        if (this.nodes.get(r1).isConnectedTo(this.nodes.get(r2))) return true; // Already connected! (Can't connect)
        return false;
    }

    public void addConnection(ArrayList<NeuronConnectionHistory> innovationHistory) {
        if (this.isFullyConnected()) {
            return;
        }
        int random1 = (int) Math.floor(Math.random() * this.nodes.size());
        int random2 = (int) Math.floor(Math.random() * this.nodes.size());

        while(this.randomConnectionsAreShit(random1, random2)) {
            random1 = (int) Math.floor(Math.random() * this.nodes.size());
            random2 = (int) Math.floor(Math.random() * this.nodes.size());
        }
        int temp;
        // If the first random is bigger than the destination neuron layer, swap them
        if (this.nodes.get(random1).layer > this.nodes.get(random2).layer) {
            temp = random2;
            random2 = random1;
            random1 = temp;
        }
        int connNumber = this.getInnovationNumber(innovationHistory, this.nodes.get(random1), this.nodes.get(random2));
        this.connections.add(new NeuronConnection(this.nodes.get(random1), this.nodes.get(random2), Math.random()*2 - 1, connNumber));
        this.connectNeurons();
    }

    public void fullyConnect(ArrayList<NeuronConnectionHistory> history) {
        for (int i = 0; i < this.inputs; i++) {
            for (int j = 0; j < this.outputs; j++) {
                int number = this.getInnovationNumber(history, this.nodes.get(i), this.nodes.get(this.nodes.size() - j - 2));
                this.connections.add(new NeuronConnection(this.nodes.get(i), this.nodes.get(this.nodes.size() - j - 2), Math.random()*2 -1, number));
            }
        }
        int number = this.getInnovationNumber(history, this.nodes.get(this.biasNode), this.nodes.get(this.nodes.size() - 2));
        this.connections.add(new NeuronConnection(this.nodes.get(this.biasNode), this.nodes.get(this.nodes.size() -  2), Math.random()*2 -1, number));
        number = this.getInnovationNumber(history, this.nodes.get(this.biasNode), this.nodes.get(this.nodes.size() - 3));
        this.connections.add(new NeuronConnection(this.nodes.get(this.biasNode), this.nodes.get(this.nodes.size() - 3), Math.random()*2 -1, number));
        this.connectNeurons();
    }

    public boolean isFullyConnected() {
        int max = 0;
        ArrayList<Double> connection = new ArrayList<>();
        for (int i = 0; i < this.layers; i++) {
            connection.add(0.0);
        }
        for (int i = 0; i < this.nodes.size(); i++) {
            connection.set(this.nodes.get(i).layer, connection.get(this.nodes.get(i).layer) + 1);
        }
        for (int i = 0; i < this.layers - 1; i++) {
            int nodesInFront = 0;
            for (int j = i + 1; j < this.layers; j++) {
                nodesInFront += connection.get(j);
            }
            max += connection.get(i) * nodesInFront;
        }
        return max == this.connections.size();
    }

    public void mutate(ArrayList<NeuronConnectionHistory> history) {
        if (this.connections.size() == 0) {
            this.addConnection(history);
        }
        double rand1 = Math.random();
        if (rand1 < 0.8) {
            for (int i = 0; i < this.connections.size(); i++) {
                this.connections.get(i).mutateWeight();
            }
        }
        double rand2 = Math.random();
        if (rand2 < 0.05) {
            this.addConnection(history);
        }
        double rand3 = Math.random();
        if (rand3 < 0.01) {
            this.addNode(history);
        }
    }

    private int matchingGene(Brain b, int innovationNumber) {
        for (int i = 0; i < b.connections.size(); i++) {
            if (b.connections.get(i).innovationNumber == innovationNumber) {
                return i;
            }
        }
        return -1;
    }

    /**
     * I am mum. Generate an offspring using the connections from both mum and dad to represent
     * genes.
     * @param dady The other parent brain (dad)
     * @return a new baby brain
     */
    public Brain crossover(Brain dady) {
        Brain myBaby = new Brain(this.inputs, this.outputs, true);
        myBaby.connections.clear();
        myBaby.nodes.clear();
        myBaby.layers = this.layers;
        myBaby.nextNeuron = this.nextNeuron;
        myBaby.biasNode = this.biasNode;

        ArrayList<NeuronConnection> childConnections = new ArrayList<>();
        ArrayList<Boolean> isEnabled = new ArrayList<>();

        for (int i = 0; i < this.connections.size(); i++) {
            boolean setEnabled = true;
            int parentConnection = this.matchingGene(dady, this.connections.get(i).innovationNumber);
            if (parentConnection != -1) { // Matching gene found
                // Either 1 of the parents geners are disabled
                if (!this.connections.get(i).enabled || !dady.connections.get(parentConnection).enabled) {
                    // Then give the baby a 75% chance of disabling its gene
                    if (Math.random() < 0.75) {
                        setEnabled = false;
                    }
                }
                double rand1 = Math.random();
                if (rand1 < 0.5) {
                    childConnections.add(this.connections.get(i));
                } else {
                    childConnections.add(dady.connections.get(parentConnection));
                }
            } else {
                childConnections.add(this.connections.get(i));
                setEnabled = this.connections.get(i).enabled;
            }
            isEnabled.add(setEnabled);
        }
        for (int i = 0; i < this.nodes.size(); i++) {
            myBaby.nodes.add(this.nodes.get(i).clone());
        }
        for (int i = 0; i < childConnections.size(); i++) {
            myBaby.connections.add(childConnections.get(i).clone(myBaby.getNeuron(childConnections.get(i).parent.id), myBaby.getNeuron(childConnections.get(i).child.id)));
        }

        myBaby.connectNeurons(); // Connect neurons in the baby
        return myBaby;
    }

    public Brain clone() {
        Brain clone = new Brain(this.inputs, this.outputs, true);
        for (int i = 0; i < this.nodes.size(); i++) { //copy this.nodes
            clone.nodes.add(this.nodes.get(i).clone());
        }

        //copy all the connections so that they connect the clone new this.nodes

        for (int i = 0; i < this.nodes.size(); i++) { //copy genes
            clone.connections.add(this.connections.get(i).clone(clone.getNeuron(this.connections.get(i).parent.id), clone.getNeuron(this.connections.get(i).child.id)));
        }

        clone.layers = this.layers;
        clone.nextNeuron = this.nextNeuron;
        clone.biasNode = this.biasNode;
        clone.connectNeurons();

        return clone;
    }
}
