import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Simulation {
    private int scale; //Size of the ring
    public World world;
    public Agent_Details[] agents;
    public double fixedGrowthperTurn;
    public double ownershipBonusGrowth;
    public int visibility_range;
    public int max_soldiers;

    public int step;
    public int active_step; //for use with the GUI

    public ArrayList<World_State> state_history;
    public HashSet<String> active_agents;
    public HashMap<String, Agent_Details> agentLookup;

    public Random rand;

    public Simulation(int scale, Agent_Details[] agents, int max_soldiers, int starting_soldiers, int visibility_range, int perTurn, int bonusPerTurn, HashMap<String, Agent_Details> agentLookup){
        this.scale = scale;

        this.agents = agents; //not yet connected.
        this.fixedGrowthperTurn = ((double) perTurn) / 100.0;
        this.ownershipBonusGrowth = ((double) bonusPerTurn) / 100.0;
        this.visibility_range = visibility_range;
        this.max_soldiers = max_soldiers;

        this.step = 1;
        this.active_step = 1;

        this.state_history = new ArrayList<>();

        this.agentLookup = agentLookup;
        this.rand = new Random();
        
        //Agent_Details[] myagents = new Agent_Details[2];
        //Color color = new Color(100,100,50);
        //Agent_Details red = new Agent_Details("Agent2", "red", "java", color);
        //Agent_Details blue = new Agent_Details("Agent3", "blue", "java", Color.BLUE);
        //myagents[0] = red;
        //myagents[1] = blue;

        this.world = new World(this.scale, this.max_soldiers, starting_soldiers, this.visibility_range, this.agents, this.agentLookup); //true is the absorb value, will be assigned in GUI.
        //this.agents = myagents; //delete later
        active_agents = new HashSet<>();
        for (Agent_Details a : this.agents) {
            active_agents.add(a.locname);  
        }
        World_State ws = new World_State(this.world, 0, new ArrayList<>(), false, active_agents, this.step%2,0);
        state_history.add(ws);
    }
    
    public int make_state_file(Agent_Details agent, int step, double grow_percent, double bonus_grow_percent) {
        List<World.Node_State> myView = this.world.get_perspective(agent);
        int total_soldiers = 0;
        String counts = "";
        String owners = "";
        int total_nodes = 0;
        for (World.Node_State s: myView) {
            counts = counts + s.count() + ",";
            owners = owners + s.owner() + ",";
            if (s.owner().equals("Y")) { //THIS SEEMS LIKE ITS WRONG...
                total_soldiers = total_soldiers + s.count();
                total_nodes++;
            }
        }
        double bonus = 1.0 + (bonus_grow_percent*total_nodes);
        int grow = (int) (total_soldiers * grow_percent * bonus);
        String c_string = counts.substring(0, counts.length() - 1);
        String o_string = owners.substring(0, owners.length() - 1);
        try {
            FileWriter writer = new FileWriter(agent.locname+"/"+step+".txt");
            writer.write(c_string);
            writer.append("\n" + o_string);
            //append new soldier count here..Followed by max soldiers?
            writer.append("\n" + grow);
            writer.append("\n" + max_soldiers);
            writer.close(); // Always close the writer to finalize the output and free resources
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
        //some function appendMove() which adds the move.txt file to the end of this file.
        return grow;
    }

    public ArrayList<Movement> readMove(Agent_Details agent) {
    //reads a given agents move
    //agent name is their actually name / folder loc
    ArrayList<Movement> movements = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(agent.locname+"/move.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(","); // Split line into parts
                // Process the parts array as needed
                Movement m = new Movement(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),agent);
                movements.add(m); //location
            }

        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
        return movements;
    }

    public void commandAgent(Agent_Details agent, int step, int new_soldiers) {
        //name should be the class file name of the agent
        //for testing
        String agent_loc = agent.getlocName();
        String agent_filename = agent.getFileName();
        try {
            // Define the command and arguments in a list
            List<String> commands = new ArrayList<>();
            //commands.add("java");
            if (!agent.getLang().equals("")) {
                commands.add(agent.getLang());
            }
            
            commands.add(agent_filename);
            commands.add(Integer.toString(step));
            commands.add(agent_loc);
            //commands.add(Integer.toString(soldiersPerTurn)); //new soldiers count.
            commands.add(Integer.toString(new_soldiers)); //REMOVE THIS!!!!!
            //add on stuff about new number of agents etc

            //Create a ProcessBuilder
            ProcessBuilder builder = new ProcessBuilder(commands);
            builder.redirectErrorStream(true); // Redirect error stream to the output stream

            // Start the process
            long startTime = System.nanoTime();
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the process to complete and get the exit value
            int exitValue = process.waitFor();
            long endTime = System.nanoTime();
            long durationMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            System.out.println("Process exited with code " + exitValue + " and took " + durationMillis + " milliseconds.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Movement> merge_moves(ArrayList<ArrayList<Movement>> bothMoves) {
        ArrayList<Movement> merged_moves = new ArrayList<>();
        Movement[] tmp_map1 = new Movement[this.scale];
        Movement[] tmp_map2 = new Movement[this.scale];
        ArrayList<Movement> moves1 = bothMoves.get(0);
        ArrayList<Movement> moves2 = bothMoves.get(1);
        for (Movement move: moves1) {
            if (move.change < 0) {
                merged_moves.add(move);
            } else {
                if (tmp_map1[(move.loc+move.agent.myStart)%this.scale] == null) {
                    tmp_map1[(move.loc+move.agent.myStart)%this.scale] = move;
                } else {
                    tmp_map1[(move.loc+move.agent.myStart)%this.scale].change = tmp_map1[(move.loc+move.agent.myStart)%this.scale].change + move.change;
                }
                
            }
            
        }
        for (Movement move: moves2) {
            if (move.change < 0) {
                merged_moves.add(move);
            } else {
                if (tmp_map2[(move.loc+move.agent.myStart)%this.scale] == null) {
                    tmp_map2[(move.loc+move.agent.myStart)%this.scale] = move;
                } else {
                    tmp_map2[(move.loc+move.agent.myStart)%this.scale].change = tmp_map2[(move.loc+move.agent.myStart)%this.scale].change + move.change;
                }
            }
        }
        for (int i = 0; i < this.scale; i++) {
            
            if (tmp_map1[i] != null && tmp_map2[i] != null) {
                //System.out.println(tmp_map1[i]);
                int diff = tmp_map1[i].change - tmp_map2[i].change;
                if (diff > 0) {
                    Movement nMove = new Movement(tmp_map1[i].loc, diff, tmp_map1[i].agent);
                    merged_moves.add(nMove);
                } else if (diff < 0) {
                    Movement nMove = new Movement(tmp_map2[i].loc, -1*diff, tmp_map2[i].agent);
                    merged_moves.add(nMove);
                }

            } else if (tmp_map1[i] != null) {
                merged_moves.add(tmp_map1[i]);
            } else if (tmp_map2[i] != null) {
                merged_moves.add(tmp_map2[i]);
            }
        }
        return merged_moves;
    }

    public ArrayList<Movement> check_legal(Agent_Details agent, ArrayList<Movement> moves, int newSoldiers) {
        //checks that all moves in moves are legal and returns an empty array list if any are illegal
        //or the original moves if all are legal
        
        //first merge moves so that there's only one per node..
        ArrayList<Integer> total_moves = new ArrayList<>(Collections.nCopies(this.scale, 0)); //this needs to be a full array of the full ring
        int total_change = 0;
        //System.out.println(total_moves.size());
        for (Movement move : moves) {
            //System.out.println("MOVE: " + move.loc);
            int tmp = total_moves.get(move.loc);
            tmp = tmp + move.change;
            total_moves.set(move.loc, tmp);
            total_change = total_change + move.change;      
        }
        if (total_change > newSoldiers) {
            //added too many new soldiers
            System.out.println(agent.locname + " made an illegal move: Moved more than owned");
            return new ArrayList<Movement>(); //return an empty list since there is an illegal move
        }
        //then analyze them
        List<World.Node_State> aworld = this.world.get_perspective(agent);
        int i = 0;
        //System.out.println(total_moves.size());
        for (int move : total_moves) {
            //System.out.println(i);
            World.Node_State state = aworld.get(i);
            if (move < 0) {
                if (state.owner() != "Y") {
                    //removing from an opponent
                    System.out.println(agent.locname + " made an illegal move: removing from an opponent");
                    System.out.println(state.owner());
                    return new ArrayList<Movement>(); //return an empty list since there is an illegal move
                }
                if (state.count() < move*-1) {
                    //too few soldiers to remove that many
                    System.out.println(agent.locname + " made an illegal move: Removed More than existed on that node");
                    return new ArrayList<Movement>(); 
                }
            }
            if (move > 0) {
                int gloc = (agent.myStart + i)%this.scale;
                if(world.nodes.get(gloc).visible_in_range(agent.locname, this.visibility_range) != 1) {
                    //node out of range of owned nodes
                    System.out.println(agent.locname + " made an illegal move");
                    return new ArrayList<Movement>();
                }
            }

            i++;
        }

        return moves;
    }

    public ArrayList<World_State> get_state_history() {
        return this.state_history;
    }

    public void make_turn() {
        //save state history
        //update state files for both agents
        //command both agents to make new move.txt files
        //read both agent's moves into a Movement arrays while CHECKING FOR LEGAL MOVES
        //Resolve local conflicts in the two movement arrays into a new single array
        //Add the remaining soldiers from the merged movement array to the world
        //Battle -resolve edge battles in the world.
        //detect winner
        
        //iterate the step count
        
        ArrayList<ArrayList<Movement>> bothMoves = new ArrayList<>();
        ArrayList<Movement> bothMoves_combo = new ArrayList<>();
        for (Agent_Details agent: agents) {
            int grow = this.make_state_file(agent, this.step, this.fixedGrowthperTurn, this.ownershipBonusGrowth);
            //grow now includes the bonus growth
            this.commandAgent(agent, this.step, grow);
            ArrayList<Movement> moves = this.readMove(agent); //should return an empty arraylist if the moves were illegal.
            moves = this.check_legal(agent, moves, grow);
            System.out.println(moves.toString());
            bothMoves.add(moves);
            bothMoves_combo.addAll(moves);
            System.out.println(bothMoves.toString());
            
        }
        ArrayList<Movement> merged_moves = this.merge_moves(bothMoves);

        for (Movement move : merged_moves) {
            int gi = (move.loc + move.agent.myStart)%this.scale;
            this.world.getNode(gi).addSoldiers(move.agent, move.change);
        }
        //EDGE BATTLES
        int resolve_dir = this.step%2;
        int resolve_start = this.rand.nextInt(this.scale);
        //int resolve_start = 19;
        this.world.resolve(resolve_dir, resolve_start);

        //Check for Victory
        //right now it happens when world_state object is created.

        //bothMoves_combo actually needs to store the state after the edge battle...
        World_State ws = new World_State(world, step, bothMoves_combo, false, active_agents,resolve_dir,resolve_start);
        state_history.add(ws);
        this.step++;
    }

    public static class Movement {
        public int loc;
        public int change;
        public Agent_Details agent;
        public Movement(int loc, int change, Agent_Details agent) {
            this.loc = loc;
            this.change = change;
            this.agent = agent;
        }
        public String toString() {
            return "(" + agent.locname + ", "+ this.loc + ", " + this.change + ")";
        }
    }

    public class World_State {
    public int step;
    //public int superStep;
    public List<Integer> counts;
    public List<Color> owners; //made it colors for convienience.
    public ArrayList<Movement> moves;
    public Boolean victory;
    public int resolve_dir;
    public int resolve_start;
    public HashSet<String> active_agents;
    public HashMap<String, Integer> player_totals;
    public World_State(World w, int step, ArrayList<Movement> moves, Boolean victory, HashSet<String> active_agents, int resolve_dir, int resolve_start) {
        this.step = step;
        //this.superStep = superStep;
        this.counts = new ArrayList<>();
        this.owners = new ArrayList<>();
        this.moves = moves;
        this.resolve_dir = resolve_dir;
        this.resolve_start = resolve_start;
        this.victory = victory;
        this.active_agents = active_agents;
        this.player_totals = new HashMap<>();
        for (String a : active_agents) {
            this.player_totals.put(a, 0);
        }

        for (int i = 0; i < w.nodes.size(); i++) {
            World.Node node = w.nodes.get(i);
            this.counts.add(node.getSoldiers());
            String anonOwner = node.getOwner();
            Color c;
            if (anonOwner.equals("N")) {
                c = Color.GRAY;
            } else {
                //System.out.println();
                //System.out.println(anonOwner);
                //System.out.println(w.anonToName.get(anonOwner));
                //System.out.println(agentLookup.get(w.anonToName.get(anonOwner)));
                //System.out.println();
                c = agentLookup.get(anonOwner).getColor();
                int tmp_count = this.player_totals.get(anonOwner);
                this.player_totals.put(anonOwner,tmp_count+node.getSoldiers());
                //c = Color.BLUE;
            }
            this.owners.add(c);
        }
        for (int s : this.player_totals.values()) {
            if (s == 0) {
                this.victory = true; //OBVIOUSLY THIS FUNCTION ONLY WORKS WITH 2 PLAYERS
            }
        }
    }
    public int get_player_total(Agent_Details agent) {
        return this.player_totals.get(agent.locname);
    }
    public String toString() {
        return this.owners.toString();
    }
    }

    
 

    public static void main(String[] args) {
        Agent_Details[] myagents = new Agent_Details[2];
        Color color = new Color(100,100,50);
        Agent_Details red = new Agent_Details("Agent2", "red", "java", color);
        Agent_Details blue = new Agent_Details("Agent3", "blue", "java", Color.BLUE);
        myagents[0] = red;
        myagents[1] = blue;
        //Simulation sim = new Simulation(15, myagents, 10000, 50, 5, 10);
        //sim.make_state_file(red, 0, 1.5);
        //sim.make_turn();
        //sim.make_turn();
        //sim.make_turn();
    }


}
