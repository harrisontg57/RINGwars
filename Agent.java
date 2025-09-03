import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Agent {
    private String name;
    private Random random;
    
    public Agent(String name) {
        this.name = name;
        this.random = new Random();
    }
    
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java Agent <step> <agent_location> <new_soldiers>");
            return;
        }
        
        int step = Integer.parseInt(args[0]);
        String agentLocation = args[1];
        int newSoldiers = Integer.parseInt(args[2]);
        
        Agent agent = new Agent(agentLocation);
        agent.makeMove(step, agentLocation, newSoldiers);
    }
    
    public void makeMove(int step, String agentLocation, int newSoldiers) {
        try {
            List<Integer> counts = readCounts(agentLocation, step);
            List<String> owners = readOwners(agentLocation, step);
            
            List<String> moves = new ArrayList<>();
            
            if (counts.size() == owners.size()) {
                for (int i = 0; i < counts.size(); i++) {
                    if (owners.get(i).equals("N") && newSoldiers > 0) {
                        int soldiersToPlace = Math.min(random.nextInt(3) + 1, newSoldiers);
                        moves.add(i + "," + soldiersToPlace);
                        newSoldiers -= soldiersToPlace;
                        if (newSoldiers <= 0) break;
                    }
                }
                
                if (newSoldiers > 0) {
                    for (int i = 0; i < counts.size() && newSoldiers > 0; i++) {
                        if (owners.get(i).equals("Y")) {
                            int soldiersToAdd = Math.min(random.nextInt(2) + 1, newSoldiers);
                            moves.add(i + "," + soldiersToAdd);
                            newSoldiers -= soldiersToAdd;
                        }
                    }
                }
            }
            
            writeMoves(agentLocation, moves);
            
        } catch (Exception e) {
            System.err.println("Error making move: " + e.getMessage());
            try {
                writeMoves(agentLocation, new ArrayList<>());
            } catch (IOException ioException) {
                System.err.println("Error writing empty moves: " + ioException.getMessage());
            }
        }
    }
    
    private List<Integer> readCounts(String agentLocation, int step) throws IOException {
        List<Integer> result = new ArrayList<>();
        String filename = agentLocation + "/" + step + ".txt";
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.split(",");
                for (String part : parts) {
                    result.add(Integer.parseInt(part.trim()));
                }
            }
        }
        return result;
    }
    
    private List<String> readOwners(String agentLocation, int step) throws IOException {
        List<String> result = new ArrayList<>();
        String filename = agentLocation + "/" + step + ".txt";
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            reader.readLine();
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.split(",");
                for (String part : parts) {
                    result.add(part.trim());
                }
            }
        }
        return result;
    }
    
    private void writeMoves(String agentLocation, List<String> moves) throws IOException {
        String filename = agentLocation + "/move.txt";
        try (FileWriter writer = new FileWriter(filename)) {
            for (String move : moves) {
                writer.write(move + "\n");
            }
        }
    }
}