import java.io.*;
import java.util.*;

public class MLAgent {
    private static final double LEARNING_RATE = 0.1;
    private static Map<String, Double> featureWeights = new HashMap<>();
    private static List<GameSample> trainingData = new ArrayList<>();
    
    static {
        initializeWeights();
    }
    
    public static void main(String[] args) {
        if (args.length < 3) return;
        
        int step = Integer.parseInt(args[0]);
        String agentLocation = args[1];
        int newSoldiers = Integer.parseInt(args[2]);
        
        MLAgent agent = new MLAgent();
        agent.makeMove(step, agentLocation, newSoldiers);
    }
    
    public void makeMove(int step, String agentLocation, int newSoldiers) {
        try {
            List<Integer> counts = readCounts(agentLocation, step);
            List<String> owners = readOwners(agentLocation, step);
            List<String> moves = new ArrayList<>();
            
            Map<String, Double> features = extractFeatures(owners, counts, step);
            updateModel(features);
            
            List<MoveOption> moveOptions = generateMoveOptions(owners, counts, newSoldiers);
            scoreAndRankMoves(moveOptions, features);
            
            executeBestMoves(moves, moveOptions, newSoldiers);
            
            recordGameSample(features, moveOptions);
            
            writeMoves(agentLocation, moves);
        } catch (Exception e) {
            try { writeMoves(agentLocation, new ArrayList<>()); } catch (IOException ignored) {}
        }
    }
    
    private static void initializeWeights() {
        featureWeights.put("territory_ratio", 2.5);
        featureWeights.put("strength_ratio", 3.0);
        featureWeights.put("border_pressure", 1.8);
        featureWeights.put("expansion_potential", 2.0);
        featureWeights.put("clustering_bonus", 1.5);
        featureWeights.put("defensive_need", 2.2);
        featureWeights.put("attack_opportunity", 1.9);
        featureWeights.put("late_game_bonus", 1.3);
    }
    
    private Map<String, Double> extractFeatures(List<String> owners, List<Integer> counts, int step) {
        Map<String, Double> features = new HashMap<>();
        
        int myNodes = 0, enemyNodes = 0, neutralNodes = 0;
        int myStrength = 0, enemyStrength = 0;
        int borderPressure = 0, expansionPotential = 0, clusterSize = 0;
        int defensiveNeeds = 0, attackOpportunities = 0;
        
        for (int i = 0; i < owners.size(); i++) {
            String owner = owners.get(i);
            int count = counts.get(i);
            
            if (owner.equals("Y")) {
                myNodes++;
                myStrength += count;
                if (isVulnerable(i, owners)) defensiveNeeds++;
                clusterSize += getClusterContribution(i, owners);
            } else if (owner.equals("E")) {
                enemyNodes++;
                enemyStrength += count;
                if (canAttack(i, owners) && count < 5) attackOpportunities++;
            } else {
                neutralNodes++;
                if (canExpand(i, owners)) expansionPotential++;
            }
            
            if (owner.equals("Y") && (isAdjacent(i, owners, "E") || isAdjacent(i, owners, "N"))) {
                borderPressure++;
            }
        }
        
        features.put("territory_ratio", (double) myNodes / Math.max(1, myNodes + enemyNodes));
        features.put("strength_ratio", (double) myStrength / Math.max(1, myStrength + enemyStrength));
        features.put("border_pressure", (double) borderPressure / Math.max(1, myNodes));
        features.put("expansion_potential", (double) expansionPotential / Math.max(1, neutralNodes));
        features.put("clustering_bonus", (double) clusterSize / Math.max(1, myNodes));
        features.put("defensive_need", (double) defensiveNeeds / Math.max(1, myNodes));
        features.put("attack_opportunity", (double) attackOpportunities / Math.max(1, enemyNodes));
        features.put("late_game_bonus", step > 20 ? 1.0 : 0.0);
        
        return features;
    }
    
    private List<MoveOption> generateMoveOptions(List<String> owners, List<Integer> counts, int newSoldiers) {
        List<MoveOption> options = new ArrayList<>();
        
        for (int i = 0; i < owners.size(); i++) {
            String owner = owners.get(i);
            
            if (owner.equals("N") && canExpand(i, owners)) {
                int cost = counts.get(i) + 1;
                if (cost <= newSoldiers) {
                    options.add(new MoveOption(i, cost, MoveType.EXPAND));
                }
            } else if (owner.equals("E") && canAttack(i, owners)) {
                int cost = counts.get(i) + 2;
                if (cost <= newSoldiers) {
                    options.add(new MoveOption(i, cost, MoveType.ATTACK));
                }
            } else if (owner.equals("Y")) {
                for (int reinforce = 1; reinforce <= Math.min(5, newSoldiers); reinforce++) {
                    options.add(new MoveOption(i, reinforce, MoveType.REINFORCE));
                }
            }
        }
        
        return options;
    }
    
    private void scoreAndRankMoves(List<MoveOption> options, Map<String, Double> features) {
        for (MoveOption option : options) {
            double score = 0.0;
            
            switch (option.type) {
                case EXPAND:
                    score += featureWeights.get("expansion_potential") * features.get("expansion_potential");
                    score += featureWeights.get("clustering_bonus") * 0.5;
                    break;
                case ATTACK:
                    score += featureWeights.get("attack_opportunity") * features.get("attack_opportunity");
                    score += featureWeights.get("strength_ratio") * features.get("strength_ratio");
                    break;
                case REINFORCE:
                    score += featureWeights.get("defensive_need") * features.get("defensive_need");
                    score += featureWeights.get("border_pressure") * features.get("border_pressure");
                    break;
            }
            
            score += featureWeights.get("late_game_bonus") * features.get("late_game_bonus");
            score += Math.random() * 0.1;
            
            option.score = score;
        }
        
        options.sort((a, b) -> Double.compare(b.score, a.score));
    }
    
    private void executeBestMoves(List<String> moves, List<MoveOption> options, int newSoldiers) {
        int budget = newSoldiers;
        Set<Integer> usedPositions = new HashSet<>();
        
        for (MoveOption option : options) {
            if (budget >= option.cost && !usedPositions.contains(option.position)) {
                moves.add(option.position + "," + option.cost);
                budget -= option.cost;
                usedPositions.add(option.position);
                
                if (budget <= 0) break;
            }
        }
        
        if (budget > 0) {
            for (int i = 0; i < options.size(); i++) {
                MoveOption option = options.get(i);
                if (option.type == MoveType.REINFORCE && !usedPositions.contains(option.position)) {
                    moves.add(option.position + "," + budget);
                    break;
                }
            }
        }
    }
    
    private void updateModel(Map<String, Double> features) {
        if (trainingData.size() > 10) {
            for (GameSample sample : trainingData) {
                for (String feature : features.keySet()) {
                    if (sample.features.containsKey(feature)) {
                        double currentWeight = featureWeights.get(feature);
                        double error = sample.outcome - predictOutcome(sample.features);
                        double gradient = error * sample.features.get(feature);
                        featureWeights.put(feature, currentWeight + LEARNING_RATE * gradient);
                    }
                }
            }
        }
    }
    
    private double predictOutcome(Map<String, Double> features) {
        double prediction = 0.0;
        for (Map.Entry<String, Double> entry : features.entrySet()) {
            prediction += featureWeights.getOrDefault(entry.getKey(), 0.0) * entry.getValue();
        }
        return Math.tanh(prediction);
    }
    
    private void recordGameSample(Map<String, Double> features, List<MoveOption> moves) {
        double outcome = features.get("territory_ratio") + features.get("strength_ratio") - 1.0;
        trainingData.add(new GameSample(new HashMap<>(features), outcome));
        
        if (trainingData.size() > 50) {
            trainingData.remove(0);
        }
    }
    
    private boolean canExpand(int pos, List<String> owners) {
        int size = owners.size();
        return owners.get((pos - 1 + size) % size).equals("Y") || owners.get((pos + 1) % size).equals("Y");
    }
    
    private boolean canAttack(int pos, List<String> owners) {
        int size = owners.size();
        return owners.get((pos - 1 + size) % size).equals("Y") || owners.get((pos + 1) % size).equals("Y");
    }
    
    private boolean isVulnerable(int pos, List<String> owners) {
        int size = owners.size();
        return owners.get((pos - 1 + size) % size).equals("E") || owners.get((pos + 1) % size).equals("E");
    }
    
    private boolean isAdjacent(int pos, List<String> owners, String targetType) {
        int size = owners.size();
        return owners.get((pos - 1 + size) % size).equals(targetType) || owners.get((pos + 1) % size).equals(targetType);
    }
    
    private int getClusterContribution(int pos, List<String> owners) {
        int size = owners.size();
        int contribution = 1;
        if (owners.get((pos - 1 + size) % size).equals("Y")) contribution++;
        if (owners.get((pos + 1) % size).equals("Y")) contribution++;
        return contribution;
    }
    
    enum MoveType { EXPAND, ATTACK, REINFORCE }
    
    static class MoveOption {
        int position, cost;
        MoveType type;
        double score;
        
        MoveOption(int position, int cost, MoveType type) {
            this.position = position;
            this.cost = cost;
            this.type = type;
        }
    }
    
    static class GameSample {
        Map<String, Double> features;
        double outcome;
        
        GameSample(Map<String, Double> features, double outcome) {
            this.features = features;
            this.outcome = outcome;
        }
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