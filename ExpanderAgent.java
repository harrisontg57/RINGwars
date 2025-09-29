import java.io.*;
import java.util.*;

public class ExpanderAgent {
    public static void main(String[] args) {
        if (args.length < 3) return;
        
        int step = Integer.parseInt(args[0]);
        String agentLocation = args[1];
        int newSoldiers = Integer.parseInt(args[2]);
        
        ExpanderAgent agent = new ExpanderAgent();
        agent.makeMove(step, agentLocation, newSoldiers);
    }
    
    public void makeMove(int step, String agentLocation, int newSoldiers) {
        try {
            List<Integer> counts = readCounts(agentLocation, step);
            List<String> owners = readOwners(agentLocation, step);
            List<String> moves = new ArrayList<>();
            
            List<Integer> expansionTargets = findExpansionTargets(owners);
            Map<Integer, Integer> expansionCosts = new HashMap<>();
            
            for (int target : expansionTargets) {
                expansionCosts.put(target, counts.get(target) + 1);
            }
            
            expansionTargets.sort((a, b) -> Integer.compare(expansionCosts.get(a), expansionCosts.get(b)));
            
            int expansionBudget = (int)(newSoldiers * 0.7);
            int defenseBudget = newSoldiers - expansionBudget;
            
            for (int target : expansionTargets) {
                int cost = expansionCosts.get(target);
                if (expansionBudget >= cost) {
                    moves.add(target + "," + cost);
                    expansionBudget -= cost;
                }
            }
            
            List<Integer> ownedPositions = getOwnedPositions(owners);
            if (!ownedPositions.isEmpty()) {
                List<Integer> weakPositions = new ArrayList<>();
                for (int pos : ownedPositions) {
                    if (counts.get(pos) < 3) {
                        weakPositions.add(pos);
                    }
                }
                
                weakPositions.sort((a, b) -> Integer.compare(counts.get(a), counts.get(b)));
                
                for (int pos : weakPositions) {
                    if (defenseBudget > 0) {
                        int needed = Math.min(defenseBudget, 3 - counts.get(pos));
                        moves.add(pos + "," + needed);
                        defenseBudget -= needed;
                    }
                }
                
                if (defenseBudget > 0 || expansionBudget > 0) {
                    int remaining = defenseBudget + expansionBudget;
                    int centralPos = findCentralPosition(ownedPositions, owners.size());
                    moves.add(centralPos + "," + remaining);
                }
            }
            
            writeMoves(agentLocation, moves);
        } catch (Exception e) {
            try { writeMoves(agentLocation, new ArrayList<>()); } catch (IOException ignored) {}
        }
    }
    
    private List<Integer> findExpansionTargets(List<String> owners) {
        List<Integer> targets = new ArrayList<>();
        for (int i = 0; i < owners.size(); i++) {
            if (owners.get(i).equals("N") && canExpand(i, owners)) {
                targets.add(i);
            }
        }
        return targets;
    }
    
    private boolean canExpand(int pos, List<String> owners) {
        int size = owners.size();
        int left = (pos - 1 + size) % size;
        int right = (pos + 1) % size;
        return owners.get(left).equals("Y") || owners.get(right).equals("Y");
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
    
    private int findCentralPosition(List<Integer> ownedPositions, int ringSize) {
        if (ownedPositions.isEmpty()) return 0;
        
        Map<Integer, Integer> connectivity = new HashMap<>();
        for (int pos : ownedPositions) {
            int connections = 0;
            int left = (pos - 1 + ringSize) % ringSize;
            int right = (pos + 1) % ringSize;
            if (ownedPositions.contains(left)) connections++;
            if (ownedPositions.contains(right)) connections++;
            connectivity.put(pos, connections);
        }
        
        return ownedPositions.stream()
                .max((a, b) -> Integer.compare(connectivity.get(a), connectivity.get(b)))
                .orElse(ownedPositions.get(0));
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