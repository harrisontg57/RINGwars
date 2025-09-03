import java.io.*;
import java.util.*;

public class DefensiveAgent {
    public static void main(String[] args) {
        if (args.length < 3) return;
        
        int step = Integer.parseInt(args[0]);
        String agentLocation = args[1];
        int newSoldiers = Integer.parseInt(args[2]);
        
        DefensiveAgent agent = new DefensiveAgent();
        agent.makeMove(step, agentLocation, newSoldiers);
    }
    
    public void makeMove(int step, String agentLocation, int newSoldiers) {
        try {
            List<Integer> counts = readCounts(agentLocation, step);
            List<String> owners = readOwners(agentLocation, step);
            List<String> moves = new ArrayList<>();
            
            List<Integer> ownedPositions = new ArrayList<>();
            List<Integer> threatenedPositions = new ArrayList<>();
            
            for (int i = 0; i < owners.size(); i++) {
                if (owners.get(i).equals("Y")) {
                    ownedPositions.add(i);
                    if (isAdjacent(i, owners, "E") || counts.get(i) < 5) {
                        threatenedPositions.add(i);
                    }
                }
            }
            
            Collections.sort(threatenedPositions, (a, b) -> Integer.compare(counts.get(a), counts.get(b)));
            
            for (int pos : threatenedPositions) {
                if (newSoldiers > 0) {
                    int reinforcement = Math.min(newSoldiers, 5);
                    moves.add(pos + "," + reinforcement);
                    newSoldiers -= reinforcement;
                }
            }
            
            for (int pos : ownedPositions) {
                if (newSoldiers > 0 && !threatenedPositions.contains(pos)) {
                    int reinforcement = Math.min(newSoldiers, 2);
                    moves.add(pos + "," + reinforcement);
                    newSoldiers -= reinforcement;
                }
            }
            
            writeMoves(agentLocation, moves);
        } catch (Exception e) {
            try { writeMoves(agentLocation, new ArrayList<>()); } catch (IOException ignored) {}
        }
    }
    
    private boolean isAdjacent(int pos, List<String> owners, String enemyType) {
        int size = owners.size();
        int left = (pos - 1 + size) % size;
        int right = (pos + 1) % size;
        return owners.get(left).equals(enemyType) || owners.get(right).equals(enemyType);
    }
    
    private List<Integer> readCounts(String agentLocation, int step) throws IOException {
        List<Integer> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(agentLocation + "/" + step + ".txt"))) {
            String line = reader.readLine();
            if (line != null) {
                for (String part : line.split(",")) {
                    result.add(Integer.parseInt(part.trim()));
                }
            }
        }
        return result;
    }
    
    private List<String> readOwners(String agentLocation, int step) throws IOException {
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(agentLocation + "/" + step + ".txt"))) {
            reader.readLine();
            String line = reader.readLine();
            if (line != null) {
                for (String part : line.split(",")) {
                    result.add(part.trim());
                }
            }
        }
        return result;
    }
    
    private void writeMoves(String agentLocation, List<String> moves) throws IOException {
        try (FileWriter writer = new FileWriter(agentLocation + "/move.txt")) {
            for (String move : moves) {
                writer.write(move + "\n");
            }
        }
    }
}