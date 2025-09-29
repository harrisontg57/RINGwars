import java.io.*;
import java.util.*;

public class AggressiveAgent {
    private Random random = new Random();
    
    public static void main(String[] args) {
        if (args.length < 3) return;
        
        int step = Integer.parseInt(args[0]);
        String agentLocation = args[1];
        int newSoldiers = Integer.parseInt(args[2]);
        
        AggressiveAgent agent = new AggressiveAgent();
        agent.makeMove(step, agentLocation, newSoldiers);
    }
    
    public void makeMove(int step, String agentLocation, int newSoldiers) {
        try {
            List<Integer> counts = readCounts(agentLocation, step);
            List<String> owners = readOwners(agentLocation, step);
            List<String> moves = new ArrayList<>();
            
            List<Integer> attackTargets = new ArrayList<>();
            List<Integer> neutralTargets = new ArrayList<>();
            
            for (int i = 0; i < owners.size(); i++) {
                if (owners.get(i).equals("E")) {
                    attackTargets.add(i);
                } else if (owners.get(i).equals("N")) {
                    neutralTargets.add(i);
                }
            }
            
            attackTargets.sort((a, b) -> Integer.compare(counts.get(a), counts.get(b)));
            neutralTargets.sort((a, b) -> Integer.compare(counts.get(a), counts.get(b)));
            
            for (int target : attackTargets) {
                if (newSoldiers > 0 && canAttack(target, owners)) {
                    int attackForce = Math.min(newSoldiers, counts.get(target) + random.nextInt(5) + 2);
                    moves.add(target + "," + attackForce);
                    newSoldiers -= attackForce;
                }
            }
            
            for (int target : neutralTargets) {
                if (newSoldiers > 0) {
                    int claimForce = Math.min(newSoldiers, Math.max(1, counts.get(target) + 1));
                    moves.add(target + "," + claimForce);
                    newSoldiers -= claimForce;
                }
            }
            
            if (newSoldiers > 0) {
                List<Integer> owned = getOwnedPositions(owners);
                if (!owned.isEmpty()) {
                    int stronghold = owned.get(random.nextInt(owned.size()));
                    moves.add(stronghold + "," + newSoldiers);
                }
            }
            
            writeMoves(agentLocation, moves);
        } catch (Exception e) {
            try { writeMoves(agentLocation, new ArrayList<>()); } catch (IOException ignored) {}
        }
    }
    
    private boolean canAttack(int pos, List<String> owners) {
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