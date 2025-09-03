import java.io.*;
import java.util.*;

public class AdaptiveAgent {
    private static Map<String, List<String>> gameHistory = new HashMap<>();
    
    public static void main(String[] args) {
        if (args.length < 3) return;
        
        int step = Integer.parseInt(args[0]);
        String agentLocation = args[1];
        int newSoldiers = Integer.parseInt(args[2]);
        
        AdaptiveAgent agent = new AdaptiveAgent();
        agent.makeMove(step, agentLocation, newSoldiers);
    }
    
    public void makeMove(int step, String agentLocation, int newSoldiers) {
        try {
            List<Integer> counts = readCounts(agentLocation, step);
            List<String> owners = readOwners(agentLocation, step);
            List<String> moves = new ArrayList<>();
            
            String gameState = encodeGameState(owners, counts);
            updateHistory(agentLocation, gameState);
            
            EnemyBehavior behavior = analyzeEnemyBehavior(agentLocation, owners, counts);
            Strategy strategy = selectStrategy(behavior, step);
            
            switch (strategy) {
                case AGGRESSIVE:
                    makeAggressiveMoves(moves, owners, counts, newSoldiers);
                    break;
                case DEFENSIVE:
                    makeDefensiveMoves(moves, owners, counts, newSoldiers);
                    break;
                case EXPANSIVE:
                    makeExpansiveMoves(moves, owners, counts, newSoldiers);
                    break;
                default:
                    makeBalancedMoves(moves, owners, counts, newSoldiers);
                    break;
            }
            
            writeMoves(agentLocation, moves);
        } catch (Exception e) {
            try { writeMoves(agentLocation, new ArrayList<>()); } catch (IOException ignored) {}
        }
    }
    
    private EnemyBehavior analyzeEnemyBehavior(String agentLocation, List<String> owners, List<Integer> counts) {
        List<String> history = gameHistory.get(agentLocation);
        if (history == null || history.size() < 3) {
            return EnemyBehavior.UNKNOWN;
        }
        
        int aggressiveActions = 0;
        int defensiveActions = 0;
        int expansiveActions = 0;
        
        for (int i = 1; i < history.size(); i++) {
            String prev = history.get(i-1);
            String curr = history.get(i);
            
            if (detectAggression(prev, curr)) aggressiveActions++;
            if (detectDefensiveness(prev, curr)) defensiveActions++;
            if (detectExpansion(prev, curr)) expansiveActions++;
        }
        
        if (aggressiveActions > defensiveActions && aggressiveActions > expansiveActions) {
            return EnemyBehavior.AGGRESSIVE;
        } else if (defensiveActions > expansiveActions) {
            return EnemyBehavior.DEFENSIVE;
        } else if (expansiveActions > 0) {
            return EnemyBehavior.EXPANSIVE;
        }
        
        return EnemyBehavior.BALANCED;
    }
    
    private Strategy selectStrategy(EnemyBehavior behavior, int step) {
        switch (behavior) {
            case AGGRESSIVE:
                return Strategy.DEFENSIVE;
            case DEFENSIVE:
                return Strategy.AGGRESSIVE;
            case EXPANSIVE:
                return step < 10 ? Strategy.AGGRESSIVE : Strategy.DEFENSIVE;
            default:
                return Strategy.BALANCED;
        }
    }
    
    private void makeAggressiveMoves(List<String> moves, List<String> owners, List<Integer> counts, int newSoldiers) {
        for (int i = 0; i < owners.size() && newSoldiers > 0; i++) {
            if (owners.get(i).equals("E") && canAttack(i, owners)) {
                int attack = Math.min(newSoldiers, counts.get(i) + 3);
                moves.add(i + "," + attack);
                newSoldiers -= attack;
            }
        }
        distributeRemaining(moves, owners, newSoldiers);
    }
    
    private void makeDefensiveMoves(List<String> moves, List<String> owners, List<Integer> counts, int newSoldiers) {
        for (int i = 0; i < owners.size() && newSoldiers > 0; i++) {
            if (owners.get(i).equals("Y") && isVulnerable(i, owners)) {
                int defense = Math.min(newSoldiers, 5);
                moves.add(i + "," + defense);
                newSoldiers -= defense;
            }
        }
        distributeRemaining(moves, owners, newSoldiers);
    }
    
    private void makeExpansiveMoves(List<String> moves, List<String> owners, List<Integer> counts, int newSoldiers) {
        for (int i = 0; i < owners.size() && newSoldiers > 0; i++) {
            if (owners.get(i).equals("N") && canExpand(i, owners)) {
                int expand = Math.min(newSoldiers, counts.get(i) + 1);
                moves.add(i + "," + expand);
                newSoldiers -= expand;
            }
        }
        distributeRemaining(moves, owners, newSoldiers);
    }
    
    private void makeBalancedMoves(List<String> moves, List<String> owners, List<Integer> counts, int newSoldiers) {
        int third = newSoldiers / 3;
        makeDefensiveMoves(moves, owners, counts, third);
        makeExpansiveMoves(moves, owners, counts, third);
        makeAggressiveMoves(moves, owners, counts, newSoldiers - 2 * third);
    }
    
    private void distributeRemaining(List<String> moves, List<String> owners, int remaining) {
        if (remaining > 0) {
            for (int i = 0; i < owners.size() && remaining > 0; i++) {
                if (owners.get(i).equals("Y")) {
                    moves.add(i + "," + remaining);
                    break;
                }
            }
        }
    }
    
    private String encodeGameState(List<String> owners, List<Integer> counts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < owners.size(); i++) {
            sb.append(owners.get(i)).append(counts.get(i)).append(",");
        }
        return sb.toString();
    }
    
    private void updateHistory(String agentLocation, String gameState) {
        gameHistory.computeIfAbsent(agentLocation, k -> new ArrayList<>()).add(gameState);
    }
    
    private boolean detectAggression(String prev, String curr) {
        return curr.length() > prev.length() && curr.contains("E");
    }
    
    private boolean detectDefensiveness(String prev, String curr) {
        return curr.contains("Y") && prev.contains("Y");
    }
    
    private boolean detectExpansion(String prev, String curr) {
        return curr.contains("N") && !prev.contains("N");
    }
    
    private boolean canAttack(int pos, List<String> owners) {
        int size = owners.size();
        return owners.get((pos - 1 + size) % size).equals("Y") || owners.get((pos + 1) % size).equals("Y");
    }
    
    private boolean canExpand(int pos, List<String> owners) {
        int size = owners.size();
        return owners.get((pos - 1 + size) % size).equals("Y") || owners.get((pos + 1) % size).equals("Y");
    }
    
    private boolean isVulnerable(int pos, List<String> owners) {
        int size = owners.size();
        return owners.get((pos - 1 + size) % size).equals("E") || owners.get((pos + 1) % size).equals("E");
    }
    
    enum EnemyBehavior { AGGRESSIVE, DEFENSIVE, EXPANSIVE, BALANCED, UNKNOWN }
    enum Strategy { AGGRESSIVE, DEFENSIVE, EXPANSIVE, BALANCED }
    
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