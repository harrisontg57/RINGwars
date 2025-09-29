import java.io.*;
import java.util.*;

public class ClusterAgent {
    public static void main(String[] args) {
        if (args.length < 3) return;
        
        int step = Integer.parseInt(args[0]);
        String agentLocation = args[1];
        int newSoldiers = Integer.parseInt(args[2]);
        
        ClusterAgent agent = new ClusterAgent();
        agent.makeMove(step, agentLocation, newSoldiers);
    }
    
    public void makeMove(int step, String agentLocation, int newSoldiers) {
        try {
            List<Integer> counts = readCounts(agentLocation, step);
            List<String> owners = readOwners(agentLocation, step);
            List<String> moves = new ArrayList<>();
            
            List<Cluster> clusters = identifyClusters(owners);
            Map<Cluster, Integer> clusterStrength = new HashMap<>();
            
            for (Cluster cluster : clusters) {
                int strength = 0;
                for (int pos : cluster.positions) {
                    strength += counts.get(pos);
                }
                clusterStrength.put(cluster, strength);
            }
            
            clusters.sort((a, b) -> Integer.compare(clusterStrength.get(b), clusterStrength.get(a)));
            
            int clusterBudget = (int)(newSoldiers * 0.6);
            int expansionBudget = newSoldiers - clusterBudget;
            
            if (!clusters.isEmpty()) {
                Cluster strongestCluster = clusters.get(0);
                int centerPos = findClusterCenter(strongestCluster, counts);
                int reinforcement = Math.min(clusterBudget, newSoldiers / 2);
                moves.add(centerPos + "," + reinforcement);
                clusterBudget -= reinforcement;
                
                for (int pos : strongestCluster.positions) {
                    if (clusterBudget > 0 && pos != centerPos) {
                        int boost = Math.min(clusterBudget, 3);
                        moves.add(pos + "," + boost);
                        clusterBudget -= boost;
                    }
                }
            }
            
            List<Integer> expansionTargets = findClusterExpansion(clusters, owners, counts);
            for (int target : expansionTargets) {
                if (expansionBudget > 0) {
                    int cost = counts.get(target) + 1;
                    if (cost <= expansionBudget) {
                        moves.add(target + "," + cost);
                        expansionBudget -= cost;
                    }
                }
            }
            
            if (clusterBudget > 0 || expansionBudget > 0) {
                int remaining = clusterBudget + expansionBudget;
                List<Integer> owned = getOwnedPositions(owners);
                if (!owned.isEmpty()) {
                    int strongestPos = owned.stream().max((a, b) -> Integer.compare(counts.get(a), counts.get(b))).get();
                    moves.add(strongestPos + "," + remaining);
                }
            }
            
            writeMoves(agentLocation, moves);
        } catch (Exception e) {
            try { writeMoves(agentLocation, new ArrayList<>()); } catch (IOException ignored) {}
        }
    }
    
    private List<Cluster> identifyClusters(List<String> owners) {
        List<Cluster> clusters = new ArrayList<>();
        boolean[] visited = new boolean[owners.size()];
        
        for (int i = 0; i < owners.size(); i++) {
            if (owners.get(i).equals("Y") && !visited[i]) {
                Cluster cluster = new Cluster();
                expandCluster(i, owners, visited, cluster);
                if (cluster.positions.size() >= 2) {
                    clusters.add(cluster);
                }
            }
        }
        return clusters;
    }
    
    private void expandCluster(int pos, List<String> owners, boolean[] visited, Cluster cluster) {
        if (visited[pos] || !owners.get(pos).equals("Y")) return;
        
        visited[pos] = true;
        cluster.positions.add(pos);
        
        int size = owners.size();
        int left = (pos - 1 + size) % size;
        int right = (pos + 1) % size;
        
        expandCluster(left, owners, visited, cluster);
        expandCluster(right, owners, visited, cluster);
    }
    
    private int findClusterCenter(Cluster cluster, List<Integer> counts) {
        return cluster.positions.stream().max((a, b) -> Integer.compare(counts.get(a), counts.get(b))).orElse(cluster.positions.get(0));
    }
    
    private List<Integer> findClusterExpansion(List<Cluster> clusters, List<String> owners, List<Integer> counts) {
        List<Integer> targets = new ArrayList<>();
        for (Cluster cluster : clusters) {
            for (int pos : cluster.positions) {
                int size = owners.size();
                int left = (pos - 1 + size) % size;
                int right = (pos + 1) % size;
                
                if (owners.get(left).equals("N") && !targets.contains(left)) {
                    targets.add(left);
                }
                if (owners.get(right).equals("N") && !targets.contains(right)) {
                    targets.add(right);
                }
            }
        }
        targets.sort((a, b) -> Integer.compare(counts.get(a), counts.get(b)));
        return targets;
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
    
    private static class Cluster {
        List<Integer> positions = new ArrayList<>();
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