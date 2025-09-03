import java.io.*;
import java.util.*;

public class TestRunner {
    public static void main(String[] args) {
        TestFramework framework = new TestFramework();
        
        try {
            if (args.length == 0) {
                printUsage();
                return;
            }
            
            String mode = args[0].toLowerCase();
            
            switch (mode) {
                case "single":
                    runSingleMatch(args, framework);
                    break;
                case "tournament":
                    runTournament(args, framework);
                    break;
                case "benchmark":
                    runBenchmark(args, framework);
                    break;
                case "quick":
                    runQuickTest(args, framework);
                    break;
                case "analysis":
                    runAnalysis(args, framework);
                    break;
                default:
                    System.err.println("Unknown mode: " + mode);
                    printUsage();
            }
        } catch (Exception e) {
            System.err.println("Error running tests: " + e.getMessage());
            e.printStackTrace();
        } finally {
            framework.close();
        }
    }
    
    private static void runSingleMatch(String[] args, TestFramework framework) {
        if (args.length < 3) {
            System.err.println("Usage: java TestRunner single <agent1> <agent2> [options]");
            return;
        }
        
        String agent1 = args[1];
        String agent2 = args[2];
        
        TestFramework.TestConfig config = parseConfig(args, 3);
        framework.setVerbose(true);
        
        System.out.println("Running single match: " + agent1 + " vs " + agent2);
        TestFramework.TestResult result = framework.runMatch(agent1, agent2, config);
        
        System.out.println("\n=== MATCH RESULT ===");
        System.out.println(result);
        printDetailedResult(result);
    }
    
    private static void runTournament(String[] args, TestFramework framework) {
        TestFramework.TestConfig config = parseConfig(args, 1);
        
        boolean verbose = hasFlag(args, "--verbose") || hasFlag(args, "-v");
        framework.setVerbose(verbose);
        
        System.out.println("Starting full tournament...");
        System.out.println("Configuration: " + configToString(config));
        
        TestFramework.TournamentResult tournament = framework.runTournament(config);
        tournament.printResults();
        
        if (hasFlag(args, "--export")) {
            exportResults(tournament, "tournament_results.csv");
        }
    }
    
    private static void runBenchmark(String[] args, TestFramework framework) {
        System.out.println("Running benchmark tests...");
        
        String[] testAgents = {"RandomAgent", "DefensiveAgent", "AggressiveAgent", "StrategicAgent"};
        TestFramework.TestConfig config = new TestFramework.TestConfig();
        config.maxTurns = 50;
        config.matchesPerPair = 1;
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < testAgents.length; i++) {
            for (int j = i + 1; j < testAgents.length; j++) {
                System.out.printf("Testing %s vs %s... ", testAgents[i], testAgents[j]);
                
                long matchStart = System.currentTimeMillis();
                TestFramework.TestResult result = framework.runMatch(testAgents[i], testAgents[j], config);
                long matchDuration = System.currentTimeMillis() - matchStart;
                
                System.out.printf("Winner: %s (%dms)%n", result.winner, matchDuration);
            }
        }
        
        long totalDuration = System.currentTimeMillis() - startTime;
        System.out.printf("%nBenchmark completed in %dms%n", totalDuration);
    }
    
    private static void runQuickTest(String[] args, TestFramework framework) {
        System.out.println("Running quick agent validation tests...");
        
        String[] agents = {"RandomAgent", "Agent", "DefensiveAgent", "AggressiveAgent"};
        TestFramework.TestConfig config = new TestFramework.TestConfig();
        config.maxTurns = 20;
        config.ringSize = 10;
        
        int passed = 0;
        int total = 0;
        
        for (String agent : agents) {
            System.out.printf("Testing %s... ", agent);
            total++;
            
            try {
                TestFramework.TestResult result = framework.runMatch(agent, "RandomAgent", config);
                if (!result.endReason.startsWith("Exception")) {
                    System.out.println("PASS");
                    passed++;
                } else {
                    System.out.println("FAIL: " + result.endReason);
                }
            } catch (Exception e) {
                System.out.println("FAIL: " + e.getMessage());
            }
        }
        
        System.out.printf("%nQuick test results: %d/%d agents passed%n", passed, total);
    }
    
    private static void runAnalysis(String[] args, TestFramework framework) {
        if (args.length < 2) {
            System.err.println("Usage: java TestRunner analysis <agent_name> [options]");
            return;
        }
        
        String targetAgent = args[1];
        System.out.println("Running detailed analysis for: " + targetAgent);
        
        String[] opponents = {"RandomAgent", "DefensiveAgent", "AggressiveAgent", "StrategicAgent", "MLAgent"};
        TestFramework.TestConfig config = new TestFramework.TestConfig();
        config.matchesPerPair = 5;
        
        System.out.println("\n=== AGENT ANALYSIS: " + targetAgent + " ===");
        
        int totalWins = 0;
        int totalMatches = 0;
        long totalTime = 0;
        
        for (String opponent : opponents) {
            if (opponent.equals(targetAgent)) continue;
            
            System.out.printf("%nVs %s:%n", opponent);
            
            int wins = 0;
            for (int i = 0; i < config.matchesPerPair; i++) {
                TestFramework.TestResult result = framework.runMatch(targetAgent, opponent, config);
                if (result.winner.equals(targetAgent)) wins++;
                totalMatches++;
                totalTime += result.duration;
                
                System.out.printf("  Match %d: %s (%s, %d turns, %dms)%n", 
                    i + 1, result.winner.equals(targetAgent) ? "WIN" : "LOSS", 
                    result.endReason, result.finalTurn, result.duration);
            }
            
            totalWins += wins;
            double winRate = (double) wins / config.matchesPerPair * 100;
            System.out.printf("  Result: %d/%d wins (%.1f%%)%n", wins, config.matchesPerPair, winRate);
        }
        
        double overallWinRate = (double) totalWins / totalMatches * 100;
        long avgTime = totalTime / totalMatches;
        
        System.out.printf("%n=== SUMMARY ===%n");
        System.out.printf("Overall: %d/%d wins (%.1f%%)%n", totalWins, totalMatches, overallWinRate);
        System.out.printf("Average match time: %dms%n", avgTime);
        
        if (overallWinRate > 70) {
            System.out.println("Performance: EXCELLENT");
        } else if (overallWinRate > 50) {
            System.out.println("Performance: GOOD");
        } else if (overallWinRate > 30) {
            System.out.println("Performance: AVERAGE");
        } else {
            System.out.println("Performance: NEEDS IMPROVEMENT");
        }
    }
    
    private static TestFramework.TestConfig parseConfig(String[] args, int startIndex) {
        TestFramework.TestConfig config = new TestFramework.TestConfig();
        
        for (int i = startIndex; i < args.length; i++) {
            String arg = args[i];
            
            if (arg.startsWith("--ring-size=")) {
                config.ringSize = Integer.parseInt(arg.substring(12));
            } else if (arg.startsWith("--max-turns=")) {
                config.maxTurns = Integer.parseInt(arg.substring(12));
            } else if (arg.startsWith("--matches=")) {
                config.matchesPerPair = Integer.parseInt(arg.substring(10));
            } else if (arg.startsWith("--starting-soldiers=")) {
                config.startingSoldiers = Integer.parseInt(arg.substring(20));
            } else if (arg.startsWith("--visibility=")) {
                config.visibilityRange = Integer.parseInt(arg.substring(13));
            } else if (arg.equals("--no-reverse")) {
                config.reverseMatches = false;
            }
        }
        
        return config;
    }
    
    private static boolean hasFlag(String[] args, String flag) {
        for (String arg : args) {
            if (arg.equals(flag)) return true;
        }
        return false;
    }
    
    private static String configToString(TestFramework.TestConfig config) {
        return String.format("Ring:%d, Turns:%d, Matches:%d, Soldiers:%d, Visibility:%d", 
            config.ringSize, config.maxTurns, config.matchesPerPair, 
            config.startingSoldiers, config.visibilityRange);
    }
    
    private static void printDetailedResult(TestFramework.TestResult result) {
        System.out.println("Duration: " + result.duration + "ms");
        System.out.println("Final turn: " + result.finalTurn);
        System.out.println("End reason: " + result.endReason);
        
        if (!result.turnHistory.isEmpty()) {
            System.out.println("\n=== TURN HISTORY (last 10 turns) ===");
            int startTurn = Math.max(0, result.turnHistory.size() - 10);
            
            for (int i = startTurn; i < result.turnHistory.size(); i++) {
                TestFramework.TurnData turn = result.turnHistory.get(i);
                System.out.printf("Turn %3d: %s=%d/%d, %s=%d/%d%n", 
                    turn.turn,
                    result.agent1, turn.agent1Soldiers, turn.agent1Nodes,
                    result.agent2, turn.agent2Soldiers, turn.agent2Nodes);
            }
        }
    }
    
    private static void exportResults(TestFramework.TournamentResult tournament, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Agent1,Agent2,Winner,EndReason,Turns,Duration");
            
            for (TestFramework.TestResult result : tournament.getResults()) {
                writer.printf("%s,%s,%s,%s,%d,%d%n",
                    result.agent1, result.agent2, result.winner, 
                    result.endReason, result.finalTurn, result.duration);
            }
            
            System.out.println("Results exported to: " + filename);
        } catch (IOException e) {
            System.err.println("Failed to export results: " + e.getMessage());
        }
    }
    
    private static void printUsage() {
        System.out.println("RINGwars Test Runner");
        System.out.println("\nUsage modes:");
        System.out.println("  single <agent1> <agent2>     - Run single match");
        System.out.println("  tournament                   - Run full tournament");
        System.out.println("  benchmark                    - Quick performance test");
        System.out.println("  quick                        - Validate all agents");
        System.out.println("  analysis <agent>             - Detailed agent analysis");
        System.out.println("\nOptions:");
        System.out.println("  --ring-size=N               - Set ring size");
        System.out.println("  --max-turns=N               - Set maximum turns");
        System.out.println("  --matches=N                 - Matches per pair");
        System.out.println("  --starting-soldiers=N       - Starting soldier count");
        System.out.println("  --visibility=N              - Visibility range");
        System.out.println("  --no-reverse                - Disable reverse matches");
        System.out.println("  --verbose, -v               - Verbose output");
        System.out.println("  --export                    - Export results to CSV");
        System.out.println("\nExamples:");
        System.out.println("  java TestRunner single RandomAgent StrategicAgent --verbose");
        System.out.println("  java TestRunner tournament --matches=5 --export");
        System.out.println("  java TestRunner analysis MLAgent --ring-size=20");
    }
}