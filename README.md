# RINGwars

A strategic ring-based warfare simulation game where AI agents compete for territory control on a circular battlefield.

## Overview

RINGwars is a Java-based simulation where agents place soldiers on nodes in a circular ring, attempting to control territory and eliminate opponents. The game features turn-based mechanics with growth, battles, and strategic positioning.

## Features

- **Circular Ring Battlefield**: Nodes arranged in a ring with adjacency-based mechanics
- **Turn-based Strategy**: Agents make moves each turn with soldier placement and movement
- **Growth Mechanics**: Controlled territories generate additional soldiers over time
- **Battle Resolution**: Adjacent enemy forces engage in combat
- **Multiple AI Agents**: 11 different agent strategies from simple to complex
- **GUI Interface**: Visual representation of the battlefield and game state
- **Simulation Recording**: Game states saved for replay and analysis

## Game Mechanics

### Core Rules
- **Ring Structure**: Battlefield consists of nodes arranged in a circle
- **Soldier Placement**: Agents place soldiers on nodes they control or can reach
- **Visibility Range**: Agents can only see nodes within a certain distance
- **Growth Phase**: Controlled territories generate new soldiers each turn
- **Battle Phase**: Adjacent enemy forces fight, with larger forces winning
- **Victory Condition**: Eliminate all enemy soldiers to win

### Agent Capabilities
- Read current game state from step files
- Place soldiers on accessible nodes
- Write moves to move.txt files
- Receive growth bonuses for controlled territory

## Available Agents

### Simple Agents
1. **RandomAgent** - Makes completely random moves
2. **Agent** - Basic strategy focusing on neutral territory first
3. **DefensiveAgent** - Prioritizes defending owned territory

### Intermediate Agents
4. **AggressiveAgent** - Focuses on attacking enemy positions
5. **BorderAgent** - Reinforces border positions strategically
6. **ExpanderAgent** - Systematic expansion with cost-benefit analysis
7. **ConservativeAgent** - Risk-averse strategy with safety reserves

### Advanced Agents
8. **ClusterAgent** - Creates and maintains concentrated soldier groups
9. **AdaptiveAgent** - Adapts strategy based on enemy behavior patterns
10. **StrategicAgent** - Multi-phase approach (early/mid/late game strategies)
11. **MLAgent** - Machine learning inspired with feature extraction and learning

## Building and Running

### Prerequisites
- Java 17 or higher
- CMake 3.16 or higher
- Make build system

### Build with CMake
```bash
# Generate build files
cmake .

# Build all targets
make

# Or build specific components
make RINGwars         # Main application
make RandomAgent      # Individual agent
make compile-all      # Compile all Java files
```

### Manual Build
```bash
# Compile all Java files
javac *.java

# Run main application
java COREapp

# Run individual agent (for testing)
java RandomAgent 1 test 10
```

## Usage

### Running the Main Application
```bash
java -jar RINGwars.jar
# or
make run-core
```

### Testing Agents
```bash
# Test individual agents
make test-RandomAgent
make test-StrategicAgent

# Manual testing
java RandomAgent <step> <agent_location> <new_soldiers>
```

### Game Configuration
The simulation supports various parameters:
- **Ring Size**: Number of nodes in the ring
- **Starting Soldiers**: Initial soldier count per agent
- **Visibility Range**: How far agents can see
- **Growth Rates**: Base and bonus growth percentages
- **Max Soldiers**: Upper limit for soldier counts

## File Structure

```
RINGwars/
├── COREapp.java           # Main application with GUI
├── Simulation.java        # Core game logic and simulation
├── World.java            # Ring battlefield implementation
├── Agent_Details.java    # Agent configuration class
├── Agent.java            # Basic agent implementation
├── *Agent.java           # Various AI agent strategies
├── CMakeLists.txt        # Build configuration
├── README.md            # This file
└── saves/               # Game state saves directory
```

## Agent Development

### Creating Custom Agents

1. **Implement Main Method**:
```java
public static void main(String[] args) {
    int step = Integer.parseInt(args[0]);
    String agentLocation = args[1];
    int newSoldiers = Integer.parseInt(args[2]);
    // Your agent logic here
}
```

2. **Read Game State**:
```java
// Read soldier counts from step file
BufferedReader reader = new BufferedReader(new FileReader(agentLocation + "/" + step + ".txt"));
String countsLine = reader.readLine();  // Soldier counts
String ownersLine = reader.readLine();  // Territory owners ('Y'=yours, 'E'=enemy, 'N'=neutral)
```

3. **Write Moves**:
```java
// Write moves to move.txt
FileWriter writer = new FileWriter(agentLocation + "/move.txt");
writer.write("position,soldiers\n");  // Format: position,soldier_count
```

### Agent Strategy Tips
- **Early Game**: Focus on expansion and claiming neutral territory
- **Mid Game**: Balance offense, defense, and continued expansion  
- **Late Game**: Concentrate forces for decisive battles
- **Visibility**: Remember you can only see nodes within visibility range
- **Growth**: Control more territory for soldier generation bonuses
- **Adjacency**: You can only place soldiers on or adjacent to controlled nodes

## CMake Targets

- `make compile-all` - Compile all Java source files
- `make clean-class` - Remove compiled .class files
- `make run-core` - Execute the main RINGwars application
- `make test-<agent>` - Test specific agents (e.g., `make test-MLAgent`)
- `make docs` - Generate Javadoc documentation
- `make package-all` - Create distribution package
- `make help-targets` - Display all available targets

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingAgent`)
3. Commit your changes (`git commit -m 'Add amazing new agent strategy'`)
4. Push to the branch (`git push origin feature/AmazingAgent`)
5. Open a Pull Request

## License

This project is open source. See the license file for details.

## Acknowledgments

- Inspired by classic territorial strategy games
- Built for AI agent competition and strategy development
- Educational tool for game theory and strategic thinking