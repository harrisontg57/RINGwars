cmake_minimum_required(VERSION 3.16)
project(RINGwars LANGUAGES NONE)

# Enable Java support
find_package(Java REQUIRED)
include(UseJava)

# Set Java compiler flags
set(CMAKE_JAVA_COMPILE_FLAGS "-source;17;-target;17")

# Define source files
set(CORE_SOURCES
    COREapp.java
    Simulation.java
    World.java
    Agent_Details.java
)

set(AGENT_SOURCES
    Agent.java
    RandomAgent.java
    DefensiveAgent.java
    AggressiveAgent.java
    BorderAgent.java
    ExpanderAgent.java
    ConservativeAgent.java
    ClusterAgent.java
    AdaptiveAgent.java
    StrategicAgent.java
    MLAgent.java
)

set(TEST_SOURCES
    TestFramework.java
    TestRunner.java
)

# Create main application jar
add_jar(RINGwars
    ${CORE_SOURCES}
    ENTRY_POINT COREapp
)

# Create individual agent jars
foreach(agent_file ${AGENT_SOURCES})
    get_filename_component(agent_name ${agent_file} NAME_WE)
    add_jar(${agent_name}
        ${agent_file}
        ENTRY_POINT ${agent_name}
    )
endforeach()

# Create test framework jar
add_jar(TestFramework
    ${TEST_SOURCES}
    ${CORE_SOURCES}
    ENTRY_POINT TestRunner
)

# Install targets
install_jar(RINGwars ${CMAKE_INSTALL_PREFIX}/bin)
install_jar(TestFramework ${CMAKE_INSTALL_PREFIX}/bin)

foreach(agent_file ${AGENT_SOURCES})
    get_filename_component(agent_name ${agent_file} NAME_WE)
    install_jar(${agent_name} ${CMAKE_INSTALL_PREFIX}/bin)
endforeach()

# Custom targets for compilation
add_custom_target(compile-all
    COMMAND ${Java_JAVAC_EXECUTABLE} *.java
    WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
    COMMENT "Compiling all Java files"
)

add_custom_target(clean-class
    COMMAND ${CMAKE_COMMAND} -E remove *.class
    WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
    COMMENT "Cleaning class files"
)

# Run targets
add_custom_target(run-core
    COMMAND ${Java_JAVA_EXECUTABLE} COREapp
    WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
    DEPENDS RINGwars
    COMMENT "Running RINGwars application"
)

# Test framework targets
add_custom_target(test-quick
    COMMAND ${Java_JAVA_EXECUTABLE} TestRunner quick
    WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
    DEPENDS TestFramework
    COMMENT "Running quick agent validation"
)

add_custom_target(test-benchmark
    COMMAND ${Java_JAVA_EXECUTABLE} TestRunner benchmark
    WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
    DEPENDS TestFramework
    COMMENT "Running performance benchmark"
)

add_custom_target(test-tournament
    COMMAND ${Java_JAVA_EXECUTABLE} TestRunner tournament --matches=3
    WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
    DEPENDS TestFramework
    COMMENT "Running tournament"
)

add_custom_target(test-all
    COMMAND ./run_tests.sh all
    WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
    DEPENDS TestFramework
    COMMENT "Running comprehensive test suite"
)

# Individual agent test targets
set(TEST_STEP 1)
set(TEST_LOCATION "test")
set(TEST_SOLDIERS 10)

foreach(agent_file ${AGENT_SOURCES})
    get_filename_component(agent_name ${agent_file} NAME_WE)
    add_custom_target(test-single-${agent_name}
        COMMAND ${Java_JAVA_EXECUTABLE} ${agent_name} ${TEST_STEP} ${TEST_LOCATION} ${TEST_SOLDIERS}
        WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
        DEPENDS ${agent_name}
        COMMENT "Testing individual ${agent_name}"
    )
    
    add_custom_target(test-analysis-${agent_name}
        COMMAND ${Java_JAVA_EXECUTABLE} TestRunner analysis ${agent_name}
        WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
        DEPENDS TestFramework
        COMMENT "Running detailed analysis for ${agent_name}"
    )
endforeach()

# Documentation
add_custom_target(docs
    COMMAND ${Java_JAVADOC_EXECUTABLE} -d docs *.java
    WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
    COMMENT "Generating Javadoc documentation"
)

# Package target
add_custom_target(package-all
    COMMAND ${CMAKE_COMMAND} -E tar czf RINGwars-agents.tar.gz *.java *.class
    WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
    COMMENT "Creating package with all agents"
)

# Print available targets
add_custom_target(help-targets
    COMMAND ${CMAKE_COMMAND} -E echo "Available targets:"
    COMMAND ${CMAKE_COMMAND} -E echo "Build targets:"
    COMMAND ${CMAKE_COMMAND} -E echo "  compile-all           - Compile all Java files"
    COMMAND ${CMAKE_COMMAND} -E echo "  clean-class           - Remove all .class files"
    COMMAND ${CMAKE_COMMAND} -E echo "  RINGwars              - Build main application"
    COMMAND ${CMAKE_COMMAND} -E echo "  TestFramework         - Build test framework"
    COMMAND ${CMAKE_COMMAND} -E echo "Run targets:"
    COMMAND ${CMAKE_COMMAND} -E echo "  run-core              - Run the main RINGwars application"
    COMMAND ${CMAKE_COMMAND} -E echo "Test targets:"
    COMMAND ${CMAKE_COMMAND} -E echo "  test-quick            - Quick agent validation"
    COMMAND ${CMAKE_COMMAND} -E echo "  test-benchmark        - Performance benchmark"
    COMMAND ${CMAKE_COMMAND} -E echo "  test-tournament       - Run tournament"
    COMMAND ${CMAKE_COMMAND} -E echo "  test-all              - Comprehensive test suite"
    COMMAND ${CMAKE_COMMAND} -E echo "  test-single-<agent>   - Test individual agents (e.g., test-single-RandomAgent)"
    COMMAND ${CMAKE_COMMAND} -E echo "  test-analysis-<agent> - Analyze individual agents (e.g., test-analysis-MLAgent)"
    COMMAND ${CMAKE_COMMAND} -E echo "Utility targets:"
    COMMAND ${CMAKE_COMMAND} -E echo "  docs                  - Generate Javadoc documentation"
    COMMAND ${CMAKE_COMMAND} -E echo "  package-all           - Create tar.gz with all files"
    COMMENT "Listing available targets"
)

# Set install prefix if not specified
if(CMAKE_INSTALL_PREFIX_INITIALIZED_TO_DEFAULT)
    set(CMAKE_INSTALL_PREFIX "${CMAKE_SOURCE_DIR}/install" CACHE PATH "Install prefix" FORCE)
endif()

# Create directories for saves and agent folders
file(MAKE_DIRECTORY ${CMAKE_SOURCE_DIR}/saves)
file(MAKE_DIRECTORY ${CMAKE_SOURCE_DIR}/test)