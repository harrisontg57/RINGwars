import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class TestFramework {
    private static final String[] AGENT_CLASSES = {
        "RandomAgent", "Agent", "DefensiveAgent", "AggressiveAgent",
        "BorderAgent", "ExpanderAgent", "ConservativeAgent", "ClusterAgent",
        "AdaptiveAgent", "StrategicAgent", "MLAgent"
    };
    
    private static final String[] AGENT_LANGUAGES = {
        "java", "java", "java", "java", "java", "java", 
        "java", "java", "java", "java", "java"
    };
    
    private int maxTurns = 100;
    private int maxSoldiers = 10000;
    private int visibilityRange = 5;
    private boolean verbose = false;
    private PrintWriter logWriter;
    
    public TestFramework() {
        try {
            logWriter = new PrintWriter(new FileWriter("test_results.log", true));
        } catch (IOException e) {
            System.err.println("Warning: Could not create log file");
        }
    }
    
    public TestResult runMatch(String agent1Class, String agent2Class, TestConfig config) {
        log("=== Starting Match: " + agent1Class + " vs " + agent2Class + " ===");
        
        try {
            setupAgentDirectories(agent1Class, agent2Class);
            
            Agent_Details[] agents = createAgentDetails(agent1Class, agent2Class);
            HashMap<String, Agent_Details> agentLookup = createAgentLookup(agents);
            
            Simulation sim = new Simulation(
                config.ringSize, agents, config.maxSoldiers, config.startingSoldiers,
                config.visibilityRange, config.growthPerTurn, config.bonusGrowthPerTurn, agentLookup
            );
            
            TestResult result = new TestResult(agent1Class, agent2Class, config);
            result.startTime = System.currentTimeMillis();
            
            for (int turn = 1; turn <= config.maxTurns; turn++) {
                if (verbose) log("Turn " + turn);
                
                try {
                    sim.make_turn();
                    
                    Simulation.World_State currentState = sim.get_state_history().get(sim.get_state_history().size() - 1);
                    result.recordTurn(turn, currentState);
                    
                    if (currentState.victory) {
                        result.winner = determineWinner(currentState, agent1Class, agent2Class);
                        result.endReason = "Victory";
                        result.finalTurn = turn;
                        break;
                    }
                    
                    if (isStalemate(sim.get_state_history(), turn)) {
                        result.winner = determineLeader(currentState, agent1Class, agent2Class);
                        result.endReason = "Stalemate";
                        result.finalTurn = turn;
                        break;
                    }
                    
                } catch (Exception e) {
                    log("Error in turn " + turn + ": " + e.getMessage());
                    result.winner = "Error";
                    result.endReason = "Exception: " + e.getMessage();
                    result.finalTurn = turn;
                    break;
                }
            }
            
            if (result.winner == null) {
                Simulation.World_State finalState = sim.get_state_history().get(sim.get_state_history().size() - 1);
                result.winner = determineLeader(finalState, agent1Class, agent2Class);
                result.endReason = "Turn Limit";
                result.finalTurn = config.maxTurns;
            }
            
            result.endTime = System.currentTimeMillis();
            result.duration = result.endTime - result.startTime;
            
            log("Match Result: " + result.winner + " (" + result.endReason + ") in " + result.finalTurn + " turns");
            
            cleanupAgentDirectories(agent1Class, agent2Class);
            
            return result;
            
        } catch (Exception e) {
            log("Match failed: " + e.getMessage());
            e.printStackTrace();
            return new TestResult(agent1Class, agent2Class, config, "Error", "Exception: " + e.getMessage());
        }
    }
    
    private Agent_Details[] createAgentDetails(String agent1Class, String agent2Class) {
        Color color1 = new Color(255, 100, 100);
        Color color2 = new Color(100, 100, 255);
        
        Agent_Details agent1 = new Agent_Details(agent1Class, "red", "java", color1);
        Agent_Details agent2 = new Agent_Details(agent2Class, "blue", "java", color2);
        
        return new Agent_Details[]{agent1, agent2};
    }
    
    private HashMap<String, Agent_Details> createAgentLookup(Agent_Details[] agents) {
        HashMap<String, Agent_Details> lookup = new HashMap<>();
        lookup.put("red", agents[0]);
        lookup.put("blue", agents[1]);
        return lookup;
    }
    
    private void setupAgentDirectories(String agent1Class, String agent2Class) throws IOException {
        createAgentDirectory("red");
        createAgentDirectory("blue");
    }
    
    private void createAgentDirectory(String name) throws IOException {
        File dir = new File(name);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    private void cleanupAgentDirectories(String agent1Class, String agent2Class) {
        cleanupDirectory("red");
        cleanupDirectory("blue");
    }
    
    private void cleanupDirectory(String dirName) {
        File dir = new File(dirName);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }
    
    private String determineWinner(Simulation.World_State state, String agent1, String agent2) {
        int agent1Total = state.get_player_total(new Agent_Details(agent1, "red", "java", Color.RED));
        int agent2Total = state.get_player_total(new Agent_Details(agent2, "blue", "java", Color.BLUE));
        
        if (agent1Total > 0 && agent2Total == 0) return agent1;
        if (agent2Total > 0 && agent1Total == 0) return agent2;
        
        return agent1Total > agent2Total ? agent1 : agent2;
    }
    
    private String determineLeader(Simulation.World_State state, String agent1, String agent2) {
        try {
            int agent1Total = 0;
            int agent2Total = 0;
            
            for (int i = 0; i < state.owners.size(); i++) {
                Color owner = state.owners.get(i);
                int count = state.counts.get(i);
                
                if (owner.getRed() > 200 && owner.getBlue() < 200) {
                    agent1Total += count;
                } else if (owner.getBlue() > 200 && owner.getRed() < 200) {
                    agent2Total += count;
                }
            }
            
            return agent1Total > agent2Total ? agent1 : agent2;
        } catch (Exception e) {
            return agent1;
        }
    }
    
    private boolean isStalemate(ArrayList<Simulation.World_State> history, int currentTurn) {
        if (history.size() < 20) return false;
        
        int checkTurns = Math.min(10, history.size() - 1);
        Simulation.World_State recent = history.get(history.size() - 1);
        
        for (int i = 1; i <= checkTurns; i++) {
            Simulation.World_State past = history.get(history.size() - 1 - i);
            if (!statesAreSimilar(recent, past)) {
                return false;
            }
        }
        
        return currentTurn > 50;
    }
    
    private boolean statesAreSimilar(Simulation.World_State state1, Simulation.World_State state2) {
        if (state1.counts.size() != state2.counts.size()) return false;
        
        int differences = 0;
        for (int i = 0; i < state1.counts.size(); i++) {
            if (Math.abs(state1.counts.get(i) - state2.counts.get(i)) > 2) {
                differences++;
            }
        }
        
        return differences < 3;
    }
    
    public TournamentResult runTournament(TestConfig config) {
        log("=== Starting Tournament ===");
        TournamentResult tournament = new TournamentResult();
        
        for (int i = 0; i < AGENT_CLASSES.length; i++) {
            for (int j = i + 1; j < AGENT_CLASSES.length; j++) {
                String agent1 = AGENT_CLASSES[i];
                String agent2 = AGENT_CLASSES[j];
                
                for (int match = 0; match < config.matchesPerPair; match++) {
                    TestResult result = runMatch(agent1, agent2, config);
                    tournament.addResult(result);
                    
                    if (config.reverseMatches && match == 0) {
                        TestResult reverseResult = runMatch(agent2, agent1, config);
                        tournament.addResult(reverseResult);
                    }
                }
            }
        }
        
        tournament.calculateStatistics();
        return tournament;
    }
    
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    private void log(String message) {
        String timestamp = new Date().toString();
        String logMessage = "[" + timestamp + "] " + message;
        
        if (verbose) {
            System.out.println(logMessage);
        }
        
        if (logWriter != null) {
            logWriter.println(logMessage);
            logWriter.flush();
        }
    }
    
    public void close() {
        if (logWriter != null) {
            logWriter.close();
        }
    }
    
    public static class TestConfig {
        public int ringSize = 15;
        public int startingSoldiers = 50;
        public int maxSoldiers = 10000;
        public int visibilityRange = 5;
        public int growthPerTurn = 10;
        public int bonusGrowthPerTurn = 5;
        public int maxTurns = 100;
        public int matchesPerPair = 3;
        public boolean reverseMatches = true;
        
        public TestConfig() {}
        
        public TestConfig(int ringSize, int maxTurns) {
            this.ringSize = ringSize;
            this.maxTurns = maxTurns;
        }
    }
    
    public static class TestResult {
        public String agent1, agent2, winner, endReason;
        public TestConfig config;
        public long startTime, endTime, duration;
        public int finalTurn;
        public ArrayList<TurnData> turnHistory = new ArrayList<>();
        
        public TestResult(String agent1, String agent2, TestConfig config) {
            this.agent1 = agent1;
            this.agent2 = agent2;
            this.config = config;
        }
        
        public TestResult(String agent1, String agent2, TestConfig config, String winner, String endReason) {
            this(agent1, agent2, config);
            this.winner = winner;
            this.endReason = endReason;
        }
        
        public void recordTurn(int turn, Simulation.World_State state) {
            turnHistory.add(new TurnData(turn, state));
        }
        
        @Override
        public String toString() {
            return String.format("%s vs %s: %s wins (%s) in %d turns [%dms]", 
                agent1, agent2, winner, endReason, finalTurn, duration);
        }
    }
    
    public static class TurnData {
        public int turn;
        public int agent1Soldiers, agent2Soldiers, agent1Nodes, agent2Nodes;
        
        public TurnData(int turn, Simulation.World_State state) {
            this.turn = turn;
            
            for (int i = 0; i < state.owners.size(); i++) {
                Color owner = state.owners.get(i);
                int count = state.counts.get(i);
                
                if (owner.getRed() > 200 && owner.getBlue() < 200) {
                    agent1Soldiers += count;
                    agent1Nodes++;
                } else if (owner.getBlue() > 200 && owner.getRed() < 200) {
                    agent2Soldiers += count;
                    agent2Nodes++;
                }
            }
        }
    }
    
    public static class TournamentResult {
        private ArrayList<TestResult> results = new ArrayList<>();
        private HashMap<String, Integer> wins = new HashMap<>();
        private HashMap<String, Integer> totalMatches = new HashMap<>();
        private HashMap<String, Long> totalDuration = new HashMap<>();
        
        public void addResult(TestResult result) {
            results.add(result);
        }
        
        public void calculateStatistics() {
            for (TestResult result : results) {
                totalMatches.put(result.agent1, totalMatches.getOrDefault(result.agent1, 0) + 1);
                totalMatches.put(result.agent2, totalMatches.getOrDefault(result.agent2, 0) + 1);
                
                totalDuration.put(result.agent1, totalDuration.getOrDefault(result.agent1, 0L) + result.duration);
                totalDuration.put(result.agent2, totalDuration.getOrDefault(result.agent2, 0L) + result.duration);
                
                if (result.winner.equals(result.agent1)) {
                    wins.put(result.agent1, wins.getOrDefault(result.agent1, 0) + 1);
                } else if (result.winner.equals(result.agent2)) {
                    wins.put(result.agent2, wins.getOrDefault(result.agent2, 0) + 1);
                }
            }
        }
        
        public void printResults() {
            System.out.println("\n=== TOURNAMENT RESULTS ===");
            System.out.printf("%-15s %8s %8s %8s %10s%n", "Agent", "Wins", "Matches", "Win%", "Avg Time");
            System.out.println("-".repeat(60));
            
            for (String agent : AGENT_CLASSES) {
                int agentWins = wins.getOrDefault(agent, 0);
                int agentMatches = totalMatches.getOrDefault(agent, 0);
                long agentDuration = totalDuration.getOrDefault(agent, 0L);
                
                double winRate = agentMatches > 0 ? (double) agentWins / agentMatches * 100 : 0;
                long avgTime = agentMatches > 0 ? agentDuration / agentMatches : 0;
                
                System.out.printf("%-15s %8d %8d %7.1f%% %8dms%n", 
                    agent, agentWins, agentMatches, winRate, avgTime);
            }
            
            System.out.println("\nTotal matches: " + results.size());
        }
        
        public ArrayList<TestResult> getResults() {
            return results;
        }
    }
}