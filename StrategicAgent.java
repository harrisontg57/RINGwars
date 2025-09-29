import java.io.*;
import java.util.*;

public class StrategicAgent {
    public static void main(String[] args) {
        if (args.length < 3) return;
        
        int step = Integer.parseInt(args[0]);
        String agentLocation = args[1];
        int newSoldiers = Integer.parseInt(args[2]);
        
        StrategicAgent agent = new StrategicAgent();
        agent.makeMove(step, agentLocation, newSoldiers);
    }
    
    public void makeMove(int step, String agentLocation, int newSoldiers) {
        try {
            List<Integer> counts = readCounts(agentLocation, step);
            List<String> owners = readOwners(agentLocation, step);
            List<String> moves = new ArrayList<>();
            
            GamePhase phase = determineGamePhase(step, owners, counts);
            GameState gameState = analyzeGameState(owners, counts);
            
            switch (phase) {
                case EARLY_GAME:
                    executeEarlyGameStrategy(moves, owners, counts, newSoldiers, gameState);
                    break;
                case MID_GAME:
                    executeMidGameStrategy(moves, owners, counts, newSoldiers, gameState);
                    break;
                case LATE_GAME:
                    executeLateGameStrategy(moves, owners, counts, newSoldiers, gameState);
                    break;
            }
            
            writeMoves(agentLocation, moves);
        } catch (Exception e) {
            try { writeMoves(agentLocation, new ArrayList<>()); } catch (IOException ignored) {}
        }
    }
    
    private GamePhase determineGamePhase(int step, List<String> owners, List<Integer> counts) {
        int totalSoldiers = counts.stream().mapToInt(Integer::intValue).sum();
        int controlledNodes = (int) owners.stream().filter(o -> o.equals("Y")).count();
        
        if (step < 10 || totalSoldiers < 100) {
            return GamePhase.EARLY_GAME;
        } else if (step < 25 || controlledNodes < owners.size() / 3) {
            return GamePhase.MID_GAME;
        } else {
            return GamePhase.LATE_GAME;
        }
    }
    
    private GameState analyzeGameState(List<String> owners, List<Integer> counts) {
        int myNodes = (int) owners.stream().filter(o -> o.equals("Y")).count();
        int enemyNodes = (int) owners.stream().filter(o -> o.equals("E")).count();
        int neutralNodes = (int) owners.stream().filter(o -> o.equals("N")).count();
        
        int myStrength = 0;
        int enemyStrength = 0;
        
        for (int i = 0; i < owners.size(); i++) {
            if (owners.get(i).equals("Y")) myStrength += counts.get(i);
            if (owners.get(i).equals("E")) enemyStrength += counts.get(i);
        }
        
        return new GameState(myNodes, enemyNodes, neutralNodes, myStrength, enemyStrength);
    }
    
    private void executeEarlyGameStrategy(List<String> moves, List<String> owners, List<Integer> counts, int newSoldiers, GameState state) {
        int expansionBudget = (int)(newSoldiers * 0.8);
        int defenseBudget = newSoldiers - expansionBudget;
        
        List<Integer> neutralTargets = new ArrayList<>();
        for (int i = 0; i < owners.size(); i++) {
            if (owners.get(i).equals("N") && canExpand(i, owners)) {
                neutralTargets.add(i);
            }
        }
        
        neutralTargets.sort((a, b) -> Integer.compare(counts.get(a), counts.get(b)));
        
        for (int target : neutralTargets) {
            if (expansionBudget > 0) {
                int cost = counts.get(target) + 1;
                if (cost <= expansionBudget) {
                    moves.add(target + "," + cost);
                    expansionBudget -= cost;
                }
            }
        }
        
        if (defenseBudget > 0) {
            List<Integer> ownedPositions = getOwnedPositions(owners);
            if (!ownedPositions.isEmpty()) {
                int stronghold = ownedPositions.get(0);
                moves.add(stronghold + "," + (defenseBudget + expansionBudget));
            }
        }
    }
    
    private void executeMidGameStrategy(List<String> moves, List<String> owners, List<Integer> counts, int newSoldiers, GameState state) {
        int attackBudget = state.myStrength > state.enemyStrength ? (int)(newSoldiers * 0.6) : (int)(newSoldiers * 0.3);
        int defenseBudget = (int)(newSoldiers * 0.4);
        int expansionBudget = newSoldiers - attackBudget - defenseBudget;
        
        List<Integer> vulnerablePositions = new ArrayList<>();
        for (int i = 0; i < owners.size(); i++) {
            if (owners.get(i).equals("Y") && isVulnerable(i, owners)) {
                vulnerablePositions.add(i);
            }
        }
        
        for (int pos : vulnerablePositions) {
            if (defenseBudget > 0) {
                int defense = Math.min(defenseBudget, 4);
                moves.add(pos + "," + defense);
                defenseBudget -= defense;
            }
        }
        
        List<Integer> attackTargets = findWeakEnemyPositions(owners, counts);
        for (int target : attackTargets) {
            if (attackBudget > 0) {
                int attackForce = Math.min(attackBudget, counts.get(target) + 2);
                moves.add(target + "," + attackForce);
                attackBudget -= attackForce;
            }
        }
        
        if (expansionBudget > 0) {
            for (int i = 0; i < owners.size(); i++) {
                if (owners.get(i).equals("N") && canExpand(i, owners) && expansionBudget > 0) {
                    int cost = Math.min(expansionBudget, counts.get(i) + 1);
                    moves.add(i + "," + cost);
                    expansionBudget -= cost;
                }
            }
        }
        
        int remaining = attackBudget + defenseBudget + expansionBudget;
        if (remaining > 0) {
            List<Integer> owned = getOwnedPositions(owners);
            if (!owned.isEmpty()) {
                moves.add(owned.get(0) + "," + remaining);
            }
        }
    }
    
    private void executeLateGameStrategy(List<String> moves, List<String> owners, List<Integer> counts, int newSoldiers, GameState state) {
        if (state.myStrength > state.enemyStrength * 1.2) {
            executeFinishingMove(moves, owners, counts, newSoldiers);
        } else if (state.myStrength < state.enemyStrength * 0.8) {
            executeDesperateDefense(moves, owners, counts, newSoldiers);
        } else {
            executePositionalPlay(moves, owners, counts, newSoldiers);
        }
    }
    
    private void executeFinishingMove(List<String> moves, List<String> owners, List<Integer> counts, int newSoldiers) {
        for (int i = 0; i < owners.size() && newSoldiers > 0; i++) {
            if (owners.get(i).equals("E")) {
                int overwhelmingForce = Math.min(newSoldiers, counts.get(i) * 2);
                moves.add(i + "," + overwhelmingForce);
                newSoldiers -= overwhelmingForce;
            }
        }
        
        if (newSoldiers > 0) {
            List<Integer> owned = getOwnedPositions(owners);
            if (!owned.isEmpty()) {
                moves.add(owned.get(0) + "," + newSoldiers);
            }
        }
    }
    
    private void executeDesperateDefense(List<String> moves, List<String> owners, List<Integer> counts, int newSoldiers) {
        List<Integer> criticalPositions = new ArrayList<>();
        for (int i = 0; i < owners.size(); i++) {
            if (owners.get(i).equals("Y") && isCritical(i, owners, counts)) {
                criticalPositions.add(i);
            }
        }
        
        criticalPositions.sort((a, b) -> Integer.compare(counts.get(a), counts.get(b)));
        
        for (int pos : criticalPositions) {
            if (newSoldiers > 0) {
                int maxDefense = Math.min(newSoldiers, 8);
                moves.add(pos + "," + maxDefense);
                newSoldiers -= maxDefense;
            }
        }
    }
    
    private void executePositionalPlay(List<String> moves, List<String> owners, List<Integer> counts, int newSoldiers) {
        executeMidGameStrategy(moves, owners, counts, newSoldiers, analyzeGameState(owners, counts));
    }
    
    private List<Integer> findWeakEnemyPositions(List<String> owners, List<Integer> counts) {
        List<Integer> targets = new ArrayList<>();
        for (int i = 0; i < owners.size(); i++) {
            if (owners.get(i).equals("E") && canAttack(i, owners) && counts.get(i) < 5) {
                targets.add(i);
            }
        }
        targets.sort((a, b) -> Integer.compare(counts.get(a), counts.get(b)));
        return targets;
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
    
    private boolean isCritical(int pos, List<String> owners, List<Integer> counts) {
        return isVulnerable(pos, owners) && counts.get(pos) < 3;
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
    
    enum GamePhase { EARLY_GAME, MID_GAME, LATE_GAME }
    
    static class GameState {
        int myNodes, enemyNodes, neutralNodes, myStrength, enemyStrength;
        
        GameState(int myNodes, int enemyNodes, int neutralNodes, int myStrength, int enemyStrength) {
            this.myNodes = myNodes;
            this.enemyNodes = enemyNodes;
            this.neutralNodes = neutralNodes;
            this.myStrength = myStrength;
            this.enemyStrength = enemyStrength;
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