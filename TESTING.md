# RINGwars Testing Framework

A comprehensive testing framework for evaluating AI agents without GUI, supporting tournaments, benchmarks, and detailed analysis.

## Quick Start

```bash
# Build test framework
cmake . && make TestFramework

# Run quick validation
make test-quick

# Run full tournament
make test-tournament

# Use shell script for advanced testing
./run_tests.sh all
```

## Test Framework Components

### 1. TestFramework.java
Core testing infrastructure that:
- Runs headless simulations
- Manages agent processes
- Records game statistics
- Handles tournament management
- Provides result analysis

### 2. TestRunner.java
Command-line interface with modes:
- **single** - Run individual matches
- **tournament** - Full round-robin tournament
- **benchmark** - Performance testing
- **quick** - Agent validation
- **analysis** - Detailed agent evaluation

### 3. run_tests.sh
Bash script for batch testing:
- Automated test execution
- Environment setup
- Result organization
- Comprehensive test suites

## Testing Modes

### Quick Validation
Tests all agents for basic functionality:
```bash
java TestRunner quick
# or
make test-quick
./run_tests.sh quick
```

### Single Match
Run specific agent matchups:
```bash
java TestRunner single RandomAgent StrategicAgent --verbose
# or
./run_tests.sh match RandomAgent StrategicAgent --ring-size=20
```

### Tournament
Full round-robin tournament:
```bash
java TestRunner tournament --matches=5 --export
# or
make test-tournament
./run_tests.sh tournament 5
```

### Agent Analysis
Detailed evaluation of specific agents:
```bash
java TestRunner analysis MLAgent
# or
make test-analysis-MLAgent
./run_tests.sh analysis MLAgent
```

### Performance Benchmark
Speed and resource usage testing:
```bash
java TestRunner benchmark
# or
make test-benchmark
./run_tests.sh benchmark
```

### Stress Testing
Comprehensive testing across configurations:
```bash
./run_tests.sh stress
```

## Configuration Options

### Game Parameters
- `--ring-size=N` - Ring battlefield size (default: 15)
- `--max-turns=N` - Maximum game length (default: 100)
- `--starting-soldiers=N` - Initial soldiers per agent (default: 50)
- `--visibility=N` - Agent visibility range (default: 5)
- `--matches=N` - Matches per agent pair (default: 3)

### Test Options
- `--verbose` - Detailed output
- `--export` - Export results to CSV
- `--no-reverse` - Skip reverse matches

## CMake Integration

### Available Test Targets

#### Quick Testing
```bash
make test-quick           # Agent validation
make test-benchmark       # Performance test
make test-tournament      # Tournament
make test-all            # Complete test suite
```

#### Individual Agents
```bash
make test-single-RandomAgent      # Basic agent test
make test-analysis-StrategicAgent # Detailed analysis
```

#### Build Targets
```bash
make TestFramework        # Build test framework
make compile-all         # Compile all sources
make help-targets        # Show all targets
```

## Output and Results

### Console Output
- Real-time match progress
- Win/loss statistics
- Performance metrics
- Error reporting

### Log Files
- `test_results.log` - Detailed execution log
- `test_results/` - Organized result files
- `tournament_results.csv` - Exportable data

### Result Analysis
Tournament results include:
- Win rates per agent
- Average game duration
- Performance rankings
- Head-to-head statistics

## Example Workflows

### Development Testing
```bash
# Quick validation during development
make test-quick

# Test specific agent changes
./run_tests.sh match MyNewAgent StrategicAgent --verbose

# Detailed analysis
./run_tests.sh analysis MyNewAgent
```

### Performance Evaluation
```bash
# Benchmark suite
make test-benchmark

# Stress testing
./run_tests.sh stress

# Full tournament
./run_tests.sh tournament 10
```

### Research Analysis
```bash
# Export tournament data
java TestRunner tournament --matches=10 --export

# Agent-specific analysis
for agent in RandomAgent DefensiveAgent AggressiveAgent; do
    ./run_tests.sh analysis $agent
done
```

## Troubleshooting

### Common Issues

**Compilation Errors**
```bash
# Ensure Java 17+
java -version

# Recompile everything
make clean-class && make compile-all
```

**Agent Failures**
```bash
# Test individual agents
make test-single-RandomAgent

# Check agent directories
ls -la red/ blue/
```

**Test Environment**
```bash
# Clean test environment
rm -rf red blue *.txt *.log

# Reset test directories
mkdir -p test_results logs
```

### Performance Tips

**Memory Settings**
```bash
export JAVA_OPTS="-Xmx4g -Xms1g"
./run_tests.sh tournament
```

**Parallel Testing**
```bash
# Run multiple test instances
./run_tests.sh tournament 3 &
./run_tests.sh benchmark &
wait
```

## Extending the Framework

### Custom Test Modes
Add new modes to TestRunner.java:
```java
case "custom":
    runCustomTest(args, framework);
    break;
```

### New Metrics
Extend TestResult class:
```java
public class TestResult {
    // Add custom metrics
    public double averageResponseTime;
    public int strategicComplexity;
}
```

### Agent Integration
Ensure agents follow the interface:
```java
public static void main(String[] args) {
    int step = Integer.parseInt(args[0]);
    String agentLocation = args[1];
    int newSoldiers = Integer.parseInt(args[2]);
    // Agent logic
}
```

## Integration with CI/CD

### GitHub Actions Example
```yaml
- name: Run RINGwars Tests
  run: |
    cmake . && make TestFramework
    ./run_tests.sh quick
    ./run_tests.sh benchmark
```

### Jenkins Pipeline
```groovy
stage('Test Agents') {
    steps {
        sh 'make test-quick'
        sh 'make test-benchmark'
        archiveArtifacts 'test_results/**'
    }
}
```

The testing framework provides comprehensive evaluation capabilities for RINGwars agents, supporting both development workflows and research analysis.