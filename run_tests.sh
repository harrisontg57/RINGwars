#!/bin/bash

# RINGwars Test Suite Runner
# Usage: ./run_tests.sh [mode] [options]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
JAVA_OPTS="-Xmx2g"
TEST_DIR="test_results"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Functions
print_banner() {
    echo -e "${BLUE}"
    echo "================================================================"
    echo "                    RINGwars Test Suite"
    echo "================================================================"
    echo -e "${NC}"
}

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    print_status "Checking prerequisites..."
    
    if ! command -v java &> /dev/null; then
        print_error "Java not found. Please install Java 17 or higher."
        exit 1
    fi
    
    java_version=$(java -version 2>&1 | grep -oP 'version "([0-9]+)' | grep -oP '[0-9]+')
    if [ "$java_version" -lt 17 ]; then
        print_error "Java 17 or higher required. Found version $java_version."
        exit 1
    fi
    
    if [ ! -f "TestRunner.class" ] && [ ! -f "TestRunner.jar" ]; then
        print_status "Compiling test framework..."
        javac -cp . *.java
        if [ $? -ne 0 ]; then
            print_error "Compilation failed."
            exit 1
        fi
    fi
    
    print_status "Prerequisites OK"
}

setup_test_environment() {
    print_status "Setting up test environment..."
    
    mkdir -p "$TEST_DIR"
    mkdir -p logs
    
    # Clean up any existing test directories
    for color in red blue; do
        if [ -d "$color" ]; then
            rm -rf "$color"
        fi
    done
    
    print_status "Test environment ready"
}

run_quick_validation() {
    print_status "Running quick agent validation..."
    
    java $JAVA_OPTS TestRunner quick | tee "$TEST_DIR/quick_test_$TIMESTAMP.log"
    
    if [ ${PIPESTATUS[0]} -eq 0 ]; then
        print_status "Quick validation completed"
    else
        print_warning "Some quick tests failed"
    fi
}

run_benchmark() {
    print_status "Running performance benchmark..."
    
    java $JAVA_OPTS TestRunner benchmark | tee "$TEST_DIR/benchmark_$TIMESTAMP.log"
    
    if [ ${PIPESTATUS[0]} -eq 0 ]; then
        print_status "Benchmark completed"
    else
        print_warning "Benchmark encountered issues"
    fi
}

run_tournament() {
    local matches=${1:-3}
    local ring_size=${2:-15}
    local max_turns=${3:-100}
    
    print_status "Running tournament (matches=$matches, ring=$ring_size, turns=$max_turns)..."
    
    java $JAVA_OPTS TestRunner tournament \
        --matches=$matches \
        --ring-size=$ring_size \
        --max-turns=$max_turns \
        --export \
        | tee "$TEST_DIR/tournament_$TIMESTAMP.log"
    
    if [ ${PIPESTATUS[0]} -eq 0 ]; then
        print_status "Tournament completed"
        if [ -f "tournament_results.csv" ]; then
            mv "tournament_results.csv" "$TEST_DIR/tournament_results_$TIMESTAMP.csv"
            print_status "Results exported to $TEST_DIR/tournament_results_$TIMESTAMP.csv"
        fi
    else
        print_error "Tournament failed"
        return 1
    fi
}

run_agent_analysis() {
    local agent=$1
    
    if [ -z "$agent" ]; then
        print_error "Agent name required for analysis"
        return 1
    fi
    
    print_status "Running detailed analysis for $agent..."
    
    java $JAVA_OPTS TestRunner analysis "$agent" \
        | tee "$TEST_DIR/analysis_${agent}_$TIMESTAMP.log"
    
    if [ ${PIPESTATUS[0]} -eq 0 ]; then
        print_status "Analysis completed for $agent"
    else
        print_error "Analysis failed for $agent"
        return 1
    fi
}

run_custom_match() {
    local agent1=$1
    local agent2=$2
    shift 2
    
    if [ -z "$agent1" ] || [ -z "$agent2" ]; then
        print_error "Two agent names required for custom match"
        return 1
    fi
    
    print_status "Running custom match: $agent1 vs $agent2"
    
    java $JAVA_OPTS TestRunner single "$agent1" "$agent2" "$@" \
        | tee "$TEST_DIR/match_${agent1}_vs_${agent2}_$TIMESTAMP.log"
    
    if [ ${PIPESTATUS[0]} -eq 0 ]; then
        print_status "Match completed"
    else
        print_error "Match failed"
        return 1
    fi
}

run_stress_test() {
    print_status "Running stress test..."
    
    local agents=("RandomAgent" "DefensiveAgent" "AggressiveAgent" "StrategicAgent" "MLAgent")
    local configs=("10 50" "15 75" "20 100" "25 150")
    
    for config in "${configs[@]}"; do
        local ring_size=$(echo $config | cut -d' ' -f1)
        local max_turns=$(echo $config | cut -d' ' -f2)
        
        print_status "Stress test: ring_size=$ring_size, max_turns=$max_turns"
        
        for ((i=0; i<${#agents[@]}; i++)); do
            for ((j=i+1; j<${#agents[@]}; j++)); do
                print_status "Testing ${agents[i]} vs ${agents[j]}"
                
                java $JAVA_OPTS TestRunner single "${agents[i]}" "${agents[j]}" \
                    --ring-size=$ring_size --max-turns=$max_turns \
                    >> "$TEST_DIR/stress_test_$TIMESTAMP.log" 2>&1
                
                if [ $? -ne 0 ]; then
                    print_warning "Failed: ${agents[i]} vs ${agents[j]} (ring=$ring_size)"
                fi
            done
        done
    done
    
    print_status "Stress test completed"
}

cleanup() {
    print_status "Cleaning up temporary files..."
    
    # Remove agent directories
    for color in red blue; do
        if [ -d "$color" ]; then
            rm -rf "$color"
        fi
    done
    
    # Clean up any stray files
    find . -name "*.txt" -path "./red/*" -o -path "./blue/*" -delete 2>/dev/null || true
}

show_usage() {
    echo "RINGwars Test Suite"
    echo ""
    echo "Usage: $0 [mode] [options]"
    echo ""
    echo "Modes:"
    echo "  quick                    - Run quick validation tests"
    echo "  benchmark               - Run performance benchmark"
    echo "  tournament [matches]    - Run full tournament"
    echo "  analysis <agent>        - Analyze specific agent"
    echo "  match <agent1> <agent2> - Single match with options"
    echo "  stress                  - Comprehensive stress testing"
    echo "  all                     - Run all test modes"
    echo ""
    echo "Tournament options:"
    echo "  tournament [matches] [ring_size] [max_turns]"
    echo "  Default: tournament 3 15 100"
    echo ""
    echo "Match options (passed to TestRunner):"
    echo "  --ring-size=N           - Set ring size"
    echo "  --max-turns=N           - Set maximum turns"
    echo "  --starting-soldiers=N   - Starting soldier count"
    echo "  --visibility=N          - Visibility range"
    echo ""
    echo "Environment:"
    echo "  JAVA_OPTS              - Java options (default: -Xmx2g)"
    echo ""
    echo "Examples:"
    echo "  $0 quick"
    echo "  $0 tournament 5"
    echo "  $0 analysis MLAgent"
    echo "  $0 match RandomAgent StrategicAgent --verbose"
    echo "  $0 all"
}

# Main execution
main() {
    print_banner
    
    if [ $# -eq 0 ]; then
        show_usage
        exit 1
    fi
    
    local mode=$1
    shift
    
    check_prerequisites
    setup_test_environment
    
    case $mode in
        quick)
            run_quick_validation
            ;;
        benchmark)
            run_benchmark
            ;;
        tournament)
            run_tournament "$@"
            ;;
        analysis)
            run_agent_analysis "$1"
            ;;
        match)
            run_custom_match "$@"
            ;;
        stress)
            run_stress_test
            ;;
        all)
            print_status "Running comprehensive test suite..."
            run_quick_validation
            sleep 2
            run_benchmark
            sleep 2
            run_tournament 3
            sleep 2
            run_stress_test
            print_status "Comprehensive test suite completed"
            ;;
        help|--help|-h)
            show_usage
            ;;
        *)
            print_error "Unknown mode: $mode"
            show_usage
            exit 1
            ;;
    esac
    
    cleanup
    
    print_status "All tests completed. Results in $TEST_DIR/"
    echo -e "${GREEN}Test run finished at $(date)${NC}"
}

# Trap for cleanup on exit
trap cleanup EXIT

# Run main function
main "$@"