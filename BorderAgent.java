import java.io.*;
import java.util.*;

public class BorderAgent {
    public static void main(String[] args) {
        if (args.length < 3) return;
        
        int step = Integer.parseInt(args[0]);
        String agentLocation = args[1];
        int newSoldiers = Integer.parseInt(args[2]);
        
        BorderAgent agent = new BorderAgent();
        agent.makeMove(step, agentLocation, newSoldiers);
    }
    
    public void makeMove(int step, String agentLocation, int newSoldiers) {
        try {
            List<Integer> counts = readCounts(agentLocation, step);
            List<String> owners = readOwners(agentLocation, step);
            List<String> moves = new ArrayList<>();
            
            Map<Integer, Integer> borderStrength = new HashMap<>();
            List<Integer> borderPositions = new ArrayList<>();
            
            for (int i = 0; i < owners.size(); i++) {
                if (owners.get(i).equals("Y")) {
                    if (isBorderPosition(i, owners)) {
                        borderPositions.add(i);
                        borderStrength.put(i, calculateBorderThreat(i, counts, owners));
                    }
                }
            }
            
            borderPositions.sort((a, b) -> Integer.compare(borderStrength.get(b), borderStrength.get(a)));
            
            int reserveForExpansion = newSoldiers / 4;
            int defensebudget = newSoldiers - reserveForExpansion;
            
            for (int pos : borderPositions) {
                if (defensebudget > 0) {
                    int threat = borderStrength.get(pos);
                    int reinforcement = Math.min(defensebudget, Math.max(threat, 2));
                    moves.add(pos + "," + reinforcement);
                    defensebudget -= reinforcement;
                }
            }
            
            if (reserveForExpansion > 0) {
                for (int i = 0; i < owners.size(); i++) {
                    if (owners.get(i).equals("N") && canExpand(i, owners)) {
                        int expandForce = Math.min(reserveForExpansion, counts.get(i) + 2);
                        moves.add(i + "," + expandForce);
                        reserveForExpansion -= expandForce;
                        if (reserveForExpansion <= 0) break;
                    }
                }
            }
            
            if (defensebudget > 0 || reserveForExpansion > 0) {
                int remaining = defensebudget + reserveForExpansion;
                if (!borderPositions.isEmpty()) {
                    int strongestBorder = borderPositions.get(0);
                    moves.add(strongestBorder + "," + remaining);
                }
            }
            
            writeMoves(agentLocation, moves);
        } catch (Exception e) {
            try { writeMoves(agentLocation, new ArrayList<>()); } catch (IOException ignored) {}
        }
    }
    
    private boolean isBorderPosition(int pos, List<String> owners) {
        int size = owners.size();
        int left = (pos - 1 + size) % size;
        int right = (pos + 1) % size;
        return !owners.get(left).equals("Y") || !owners.get(right).equals("Y");
    }
    
    private int calculateBorderThreat(int pos, List<Integer> counts, List<String> owners) {
        int size = owners.size();
        int left = (pos - 1 + size) % size;
        int right = (pos + 1) % size;
        int threat = 0;
        
        if (owners.get(left).equals("E")) threat += counts.get(left);
        if (owners.get(right).equals("E")) threat += counts.get(right);
        if (owners.get(left).equals("N")) threat += 1;
        if (owners.get(right).equals("N")) threat += 1;
        
        return threat;
    }
    
    private boolean canExpand(int pos, List<String> owners) {
        int size = owners.size();
        int left = (pos - 1 + size) % size;
        int right = (pos + 1) % size;
        return owners.get(left).equals("Y") || owners.get(right).equals("Y");
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