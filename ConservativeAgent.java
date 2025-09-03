import java.io.*;
import java.util.*;

public class ConservativeAgent {
    public static void main(String[] args) {
        if (args.length < 3) return;
        
        int step = Integer.parseInt(args[0]);
        String agentLocation = args[1];
        int newSoldiers = Integer.parseInt(args[2]);
        
        ConservativeAgent agent = new ConservativeAgent();
        agent.makeMove(step, agentLocation, newSoldiers);
    }
    
    public void makeMove(int step, String agentLocation, int newSoldiers) {
        try {
            List<Integer> counts = readCounts(agentLocation, step);
            List<String> owners = readOwners(agentLocation, step);
            List<String> moves = new ArrayList<>();
            
            List<Integer> ownedPositions = getOwnedPositions(owners);
            
            int safetyReserve = Math.max(2, newSoldiers / 5);
            int availableSoldiers = newSoldiers - safetyReserve;
            
            List<Integer> vulnerablePositions = new ArrayList<>();
            for (int pos : ownedPositions) {
                if (isVulnerable(pos, counts, owners)) {
                    vulnerablePositions.add(pos);
                }
            }
            
            vulnerablePositions.sort((a, b) -> Integer.compare(counts.get(a), counts.get(b)));
            
            for (int pos : vulnerablePositions) {
                if (availableSoldiers > 0) {
                    int reinforcement = Math.min(availableSoldiers, 4);
                    moves.add(pos + "," + reinforcement);
                    availableSoldiers -= reinforcement;
                }
            }
            
            List<Integer> safeMoves = findSafeMoves(owners, counts);
            for (int target : safeMoves) {
                if (availableSoldiers > 0) {
                    int cost = counts.get(target) + 2;
                    if (cost <= availableSoldiers) {
                        moves.add(target + "," + cost);
                        availableSoldiers -= cost;
                    }
                }
            }
            
            if (availableSoldiers > 0 || safetyReserve > 0) {
                int remaining = availableSoldiers + safetyReserve;
                if (!ownedPositions.isEmpty()) {
                    int safestPosition = findSafestPosition(ownedPositions, counts, owners);
                    moves.add(safestPosition + "," + remaining);
                }
            }
            
            writeMoves(agentLocation, moves);
        } catch (Exception e) {
            try { writeMoves(agentLocation, new ArrayList<>()); } catch (IOException ignored) {}
        }
    }
    
    private boolean isVulnerable(int pos, List<Integer> counts, List<String> owners) {
        int size = owners.size();
        int left = (pos - 1 + size) % size;
        int right = (pos + 1) % size;
        
        boolean hasEnemyNeighbor = owners.get(left).equals("E") || owners.get(right).equals("E");
        boolean isWeaklyDefended = counts.get(pos) < 3;
        
        return hasEnemyNeighbor && isWeaklyDefended;
    }
    
    private List<Integer> findSafeMoves(List<String> owners, List<Integer> counts) {
        List<Integer> safeMoves = new ArrayList<>();
        for (int i = 0; i < owners.size(); i++) {
            if (owners.get(i).equals("N") && isSafeTarget(i, owners, counts)) {
                safeMoves.add(i);
            }
        }
        safeMoves.sort((a, b) -> Integer.compare(counts.get(a), counts.get(b)));
        return safeMoves;
    }
    
    private boolean isSafeTarget(int pos, List<String> owners, List<Integer> counts) {
        int size = owners.size();
        int left = (pos - 1 + size) % size;
        int right = (pos + 1) % size;
        
        boolean hasOwnedNeighbor = owners.get(left).equals("Y") || owners.get(right).equals("Y");
        boolean noEnemyNeighbors = !owners.get(left).equals("E") && !owners.get(right).equals("E");
        boolean lowCost = counts.get(pos) <= 2;
        
        return hasOwnedNeighbor && noEnemyNeighbors && lowCost;
    }
    
    private int findSafestPosition(List<Integer> ownedPositions, List<Integer> counts, List<String> owners) {
        int safest = ownedPositions.get(0);
        int maxSafety = calculateSafety(safest, counts, owners);
        
        for (int pos : ownedPositions) {
            int safety = calculateSafety(pos, counts, owners);
            if (safety > maxSafety) {
                maxSafety = safety;
                safest = pos;
            }
        }
        return safest;
    }
    
    private int calculateSafety(int pos, List<Integer> counts, List<String> owners) {
        int size = owners.size();
        int left = (pos - 1 + size) % size;
        int right = (pos + 1) % size;
        
        int safety = counts.get(pos);
        if (owners.get(left).equals("Y")) safety += 2;
        if (owners.get(right).equals("Y")) safety += 2;
        if (owners.get(left).equals("E")) safety -= 3;
        if (owners.get(right).equals("E")) safety -= 3;
        
        return safety;
    }
    
    private List<Integer> getOwnedPositions(List<String> owners) {
        List<Integer> owned = new ArrayList<>();
        for (int i = 0; i < owners.size(); i++) {
            if (owners.get(i).equals("Y")) {
                owned.add(i);
            }
        }
        return owned;
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