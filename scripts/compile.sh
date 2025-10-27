#!/bin/bash
echo "=========================================="
echo "Compiling GpuEdgeCloudSim v1.0"
echo "=========================================="

# Use Java 21
export JAVA_HOME=/usr/lib/jvm/jdk-21.0.9
export PATH=$JAVA_HOME/bin:$PATH

# Clean and create bin directory
rm -rf ../../bin
mkdir -p ../../bin

# Compile all Java sources
$JAVA_HOME/bin/javac -classpath "../../lib/cloudsim-7.0.0-alpha.jar:../../lib/commons-math3-3.6.1.jar:../../lib/colt.jar" \
      -sourcepath ../../src \
      -d ../../bin \
      ../../src/edu/boun/edgecloudsim/applications/gpusim/GpuSimulationMain.java

if [ $? -eq 0 ]; then
    echo "✓ Compilation successful!"
    echo "Binary files created in ../../bin/"
else
    echo "✗ Compilation failed!"
    exit 1
fi
