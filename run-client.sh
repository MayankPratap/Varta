#!/bin/bash

echo "Starting Polling Chat Client..."
echo "Make sure the server is running first!"
echo ""

# Compile the project to ensure latest changes
mvn compile -q

# Run the client using Java directly from compiled classes
echo "Client starting..."
# Use the same Java version that Maven uses for compilation
MAVEN_JAVA_HOME=$(mvn help:system 2>/dev/null | grep "java.home=" | cut -d'=' -f2)
echo "Using Java: $MAVEN_JAVA_HOME"

if [ -z "$MAVEN_JAVA_HOME" ]; then
    echo "Could not determine Maven's Java home, using default..."
    MAVEN_JAVA_HOME=/opt/homebrew/Cellar/openjdk/23.0.2/libexec/openjdk.jdk/Contents/Home
fi

$MAVEN_JAVA_HOME/bin/java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" com.mprataps.varta.client.PollingChatClient