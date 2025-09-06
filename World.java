import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public class World {

    // Nested class Node
    public class Node {
        private int index;
        private Node left;
        private Node right;
        public int soldiers;
        public String owner;
        public int max_soldiers;

        public Node(int index) {
            this.index = index;
            this.soldiers = 0;
            this.owner = "N";
            this.max_soldiers = 0;
        }

        public String toString() {
            return "(Loc:" +this.index+", Own: " + this.owner + ", Sols:" + this.soldiers + ")";
        }

        public int getIndex() {
            return index;
        }

        public int getSoldiers() {
            return soldiers;
        }

        public void setSoldiers(int soldiers) {
            this.soldiers = soldiers;
            if (this.soldiers == 0) {
                this.setOwner("N");
            }
            //delete excess soldiers here.
            if (this.soldiers > this.max_soldiers) {
                this.soldiers = this.max_soldiers;
            }
        }

        public void addSoldiers(Agent_Details agent, int additional_soldiers) {
            //at this stage we assume the move is legal
            
            String agent_name = agent.locname;
            if (this.owner.equals("N") | this.owner.equals(agent_name)) {
                this.owner = agent_name;
                this.setSoldiers(this.soldiers + additional_soldiers);
            } else {
                if (this.soldiers >= additional_soldiers) {
                    this.setSoldiers(this.soldiers - additional_soldiers);
                } else {
                    this.owner = agent_name;
                    this.setSoldiers(additional_soldiers -  this.soldiers);
                }
            }
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public Node getLeft() {
            return left;
        }

        public void setLeft(Node left) {
            this.left = left;
        }

        public Node getRight() {
            return right;
        }

        public void setRight(Node right) {
            this.right = right;
        }

        public int visible_in_range(String agent_name, int range) {
            //In future this must search up to range
            if (agent_name.equals(this.owner)) {return 1;}
            if (range == 1) {
                if (this.left.owner.equals(agent_name) | this.right.owner.equals(agent_name)) {
                    return 1;
                } else {
                    return 0;
                }
            }
            Node myLeft = this.left;
            Node myRight = this.right;
            for (int x = 1; x <= range; x++) {
                if (myLeft.owner.equals(agent_name) | myRight.owner.equals(agent_name)) {
                    return 1;
                }
                myLeft = myLeft.left;
                myRight = myRight.right;
            }
            return 0;
        }
    }

    public record Node_State(int count, String owner) {}

    private Node head;
    public List<Node> nodes;
    public int numNodes;

    public int max_soldiers;
    public int visability_range;

    public Agent_Details[] agents;

    public HashMap<String, Integer> perspectives;

    public HashMap<String, Agent_Details> agentLookup;
    
    public World(int numNodes, int max_soldiers, int starting_soldiers, int visability_range, Agent_Details[] agents, HashMap<String, Agent_Details> agentLookup) {
        Node[] nodes_tmp = new Node[numNodes];
        this.max_soldiers = max_soldiers;
        this.visability_range = visability_range;
        this.perspectives = new HashMap<>();
        this.numNodes = numNodes;

        this.agentLookup = agentLookup;

        // Create nodes and store them in the array
        for (int i = 0; i < numNodes; i++) {
            nodes_tmp[i] = new Node(i);
        }

        // Link the nodes to form a ring
        for (int i = 0; i < numNodes; i++) {
            nodes_tmp[i].setRight(nodes_tmp[(i + 1) % numNodes]);
            nodes_tmp[i].setLeft(nodes_tmp[(i - 1 + numNodes) % numNodes]);
            nodes_tmp[i].max_soldiers = this.max_soldiers;
        }

        // Set the head to the first node
        head = nodes_tmp[0];
        nodes = Arrays.asList(nodes_tmp);

        double divisions = (double)numNodes/(double)agents.length;
        int place = 0;
        //System.out.println(" " + divisions);
        for (double i = 0; i < numNodes - 1; i = i+divisions) {
            //System.out.println(" " + place + "," + i);
            //System.out.println(Arrays.toString(agents));
            nodes.get((int)i).setOwner(agents[place].locname);
            nodes.get((int)i).setSoldiers(starting_soldiers);
            perspectives.put(agents[place].locname,(int)i);
            agents[place].myStart = (int) i;
            place = place + 1;
            if (place >= agents.length) {break;}
        }

    }

    public void resolve(int direction, int rstart) {
        /* 
        int start = (int) this.numNodes/4; //in future this will be randomized or at least vary
        //starting point is safe from triple battles
        
        if (direction == 1) {
            //resolve right
            start = start*1; //start at "top"
        } 
        if (direction == 0) {
            //resolve left
            start = start*3; //start at "bottom"
        }
        */
        int start = rstart;
        if (direction == 1) {
            //right resolve (counterclockwise)
        for (int i = start; i < start + numNodes - 1; i++) {
            Node node  = nodes.get(i%numNodes);
            if (node.getOwner().equals(node.getRight().getOwner()) | node.getOwner().equals("N") | (node.getRight().getOwner().equals("N"))) {
                    continue;
                } else {
                    int skip = 0;
                    if (i == start + numNodes - 2) {
                        //just before the border so no triple battles
                        skip = this.fight_right(node, true);
                    } else {
                        skip = this.fight_right(node, false);
                    }
                    i = i + skip;
                }
            }
        } else {
            //left resolve
            for (int i = start; i > 1 - (numNodes-start); i--) {
                //System.out.println((numNodes+i)%numNodes);
                Node node  = nodes.get((numNodes+i)%numNodes);
                if (node.getOwner().equals(node.getLeft().getOwner()) | node.getOwner().equals("N") | (node.getLeft().getOwner().equals("N"))) {
                    continue;
                } else {
                    int skip = 0;
                    if (i == 2 - (numNodes-start)) {
                        //just before the border so no triple battles
                        skip = this.fight_left(node, true);
                    } else {
                        skip = this.fight_left(node, false);
                    }
                    //int skip = this.fight_left(node);
                    i = i + skip;
                }
            }
        }
    }

    public int fight_left(Node node, Boolean noTrip) {
        Node lnode = node.getLeft();
        Node llnode = lnode.getLeft();
        if (llnode.getOwner() == node.getOwner() & !noTrip) {
            //trip battle
            if (lnode.getSoldiers() == node.getSoldiers() + llnode.getSoldiers()) {
                //all cancel out
                node.setSoldiers(0); //automatically sets owner to "N"
                lnode.setSoldiers(0);
                llnode.setSoldiers(0);
                return -2;
            }
            if (lnode.getSoldiers() > node.getSoldiers() + llnode.getSoldiers()) {
                //center node (rnode) owns both sides
                //just takes ownership, doesn't rearrange anything
                node.setOwner(lnode.getOwner());
                llnode.setOwner(lnode.getOwner());
                return -2;
            }
            if (lnode.getSoldiers() < node.getSoldiers() + llnode.getSoldiers()) {
                //center node (rnode) owns both sides
                //just takes ownership, doesn't rearrange anything
                lnode.setOwner(node.getOwner());
                return -2;
            }
        }

        if (lnode.getSoldiers() == node.getSoldiers()) {
            //soldiers cancel each other out.
            node.setSoldiers(0); //automatically sets owner to "N"
            lnode.setSoldiers(0);
            return -1;
        }
        if (lnode.getSoldiers() < node.getSoldiers()) {
            //soldiers of rnode are absorbed into node.
            node.addSoldiers(this.agentLookup.get(node.getOwner()), lnode.getSoldiers());
            lnode.setSoldiers(0);
            return -1;
        }
        if (lnode.getSoldiers() > node.getSoldiers()) {
            //soldiers of rnode are absorbed into node.
            lnode.addSoldiers(this.agentLookup.get(lnode.getOwner()), node.getSoldiers());
            node.setSoldiers(0);
            return -1;
        }

        return 0;
    }
    

    public int fight_right(Node node, Boolean noTrip) {
        Node rnode = node.getRight();
        //triple battle logic will need to go first
        Node rrnode = rnode.getRight();
        if (rrnode.getOwner() == node.getOwner() & !noTrip) {
            //trip battle
            if (rnode.getSoldiers() == node.getSoldiers() + rrnode.getSoldiers()) {
                //all cancel out
                node.setSoldiers(0); //automatically sets owner to "N"
                rnode.setSoldiers(0);
                rrnode.setSoldiers(0);
                return 2;
            }
            if (rnode.getSoldiers() > node.getSoldiers() + rrnode.getSoldiers()) {
                //center node (rnode) owns both sides
                //just takes ownership, doesn't rearrange anything
                node.setOwner(rnode.getOwner());
                rrnode.setOwner(rnode.getOwner());
                return 2;
            }
            if (rnode.getSoldiers() < node.getSoldiers() + rrnode.getSoldiers()) {
                //center node (rnode) owns both sides
                //just takes ownership, doesn't rearrange anything
                rnode.setOwner(node.getOwner());
                return 2;
            }
        }

        if (rnode.getSoldiers() == node.getSoldiers()) {
            //soldiers cancel each other out.
            node.setSoldiers(0); //automatically sets owner to "N"
            rnode.setSoldiers(0);
            return 1;
        }
        if (rnode.getSoldiers() < node.getSoldiers()) {
            //soldiers of rnode are absorbed into node.
            node.addSoldiers(this.agentLookup.get(node.getOwner()), rnode.getSoldiers());
            rnode.setSoldiers(0);
            return 1;
        }
        if (rnode.getSoldiers() > node.getSoldiers()) {
            //soldiers of rnode are absorbed into node.
            rnode.addSoldiers(this.agentLookup.get(rnode.getOwner()), node.getSoldiers());
            node.setSoldiers(0);
            return 1;
        }
        return 0;
    }

    public List<Node_State> get_perspective(Agent_Details agent) {

        List<Integer> vis = this.get_perspective_map(agent.locname, visability_range);
        int c = perspectives.get(agent.locname);
        List<Node_State> results = new ArrayList<>();
        for (int i : vis) {
            if (i == 1) {
                if (!this.nodes.get(c%this.numNodes).owner.equals(agent.locname)) {
                Node_State ns = new Node_State(this.nodes.get(c%this.numNodes).soldiers, this.nodes.get(c%this.numNodes).owner);
                results.add(ns);
                } else {
                    Node_State ns = new Node_State(this.nodes.get(c%this.numNodes).soldiers, "Y");
                results.add(ns);
                }
            } else { //add else if node indexed by a move loc BUT make this a separate function
                results.add(new Node_State(-1, "U"));
            }
            c = c + 1;
        }
        return results;
    }

    public List<Integer> get_perspective_map(String agent_name, int visR) {
        Predicate<Node> filter = node -> node.visible_in_range(agent_name, visR) == 1; 
        //String agent_name = getAnonName(agent_name1);
        //Uses the user defined filter
        int startIndex = perspectives.get(agent_name);
        List<Integer> result = new ArrayList<>();
        int n = nodes.size();

        // Iterate from startIndex to end of the list
        for (int i = startIndex; i < n; i++) {
            Node obj = nodes.get(i);
            if (filter.test(obj)) {
                result.add(1);
            } else {result.add(0);}
        }

        // Iterate from start of the list to startIndex
        for (int i = 0; i < startIndex; i++) {
            Node obj = nodes.get(i);
            if (filter.test(obj)) {
                result.add(1);
            } else {result.add(0);}
        }
        return result;
    }

    public void printNodes() {
        if (head == null) {
            System.out.println("Empty world");
            return;
        }

        Node current = head;
        do {
            System.out.print(current.getIndex() + ": " + current.getOwner() + ", " + current.getSoldiers() + "  ");
            current = current.getRight();
        } while (current != head);
        System.out.println();
    }

    public Node getNode(int index) {
        if (index < 0 || index >= nodes.size()) {
            throw new IndexOutOfBoundsException("Index out of range");
        }
        return nodes.get(index);
    }


    public static void main(String[] args) {
        int numNodes = 15; // Example number of nodes
        Agent_Details[] myagents = new Agent_Details[2];
        Color color = new Color(100,100,50);
        Agent_Details red = new Agent_Details("Agent2", "red", "java", color);
        Agent_Details blue = new Agent_Details("Agent2", "blue", "java", Color.BLUE);
        myagents[0] = red;
        myagents[1] = blue;
        //World world = new World(numNodes, 10000, 50, 5, myagents);

        // Access a node by index
        int index = 2;
        //Node node = world.getNode(index);
        //System.out.println("Node at index " + index + ": " + node.getIndex());
    }
}
