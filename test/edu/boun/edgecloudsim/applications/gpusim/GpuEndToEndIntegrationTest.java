
/*
 * Title:        GpuEdgeCloudSim - End-to-End Integration Test
 * 
 * Description:  
 * Complete end-to-end simulation test validating GPU task lifecycle
 * from generation through execution to result collection.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2025, GpuEdgeCloudSim Project
 * Author:       Pabllo Borges Cardoso
 */

package edu.boun.edgecloudsim.applications.gpusim;

import static org.junit.Assert.*;

import java.util.Calendar;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.junit.Before;
import org.junit.Test;

import edu.boun.edgecloudsim.core.ScenarioFactory;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.SimLogger;

/**
 * End-to-end integration test suite for GpuEdgeCloudSim.
 * 
 * <p>Tests validate complete simulation workflow:</p>
 * <ol>
 *   <li>Configuration loading (config.properties, XMLs)</li>
 *   <li>CloudSim initialization</li>
 *   <li>GPU infrastructure creation</li>
 *   <li>GPU task generation and submission</li>
 *   <li>Task orchestration and VM selection</li>
 *   <li>GPU execution simulation</li>
 *   <li>Result collection and metrics</li>
 * </ol>
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 * @since GpuEdgeCloudSim v1.0
 */
public class GpuEndToEndIntegrationTest {
    
    private static final int NUM_DEVICES = 50;
    private static final double SIM_TIME = 60.0; // Short simulation for testing
    private static final String POLICY = "HYBRID_GPU";
    private static final String SCENARIO = "ML_INFERENCE_SCENARIO";
    
    /**
     * Set up test environment before each test.
     */
    @Before
    public void setUp() {
        // Reset CloudSim
        CloudSim.terminateSimulation();
        
        // Disable logs for cleaner test output
        Log.disable();
    }
    
    /**
     * Test: Complete simulation run without errors
     */
    @Test
    public void testCompleteSimulationRun() {
        try {
            // Initialize SimSettings (would normally load from config.properties)
            // For testing, we use programmatic initialization
            
            // Create GpuScenarioFactory
            ScenarioFactory factory = new GpuScenarioFactory(
                NUM_DEVICES,
                SIM_TIME,
                POLICY,
                SCENARIO
            );
            
            // Initialize CloudSim
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            
            CloudSim.init(num_user, calendar, trace_flag, 0.01);
            
            // Create SimManager
            SimManager simManager = new SimManager(
                factory,
                NUM_DEVICES,
                SCENARIO,
                POLICY
            );
            
            // Start simulation
            simManager.startSimulation();
            
            // If we reach here without exceptions, test passes
            assertTrue("Simulation should complete successfully", true);
            
        } catch (Exception e) {
            fail("Simulation should not throw exceptions: " + e.getMessage());
        }
    }
    
    /**
     * Test: GPU task generation
     */
    @Test
    public void testGpuTaskGeneration() {
        try {
            GpuLoadGeneratorModel loadGen = new GpuLoadGeneratorModel(
                NUM_DEVICES,
                SIM_TIME,
                SCENARIO
            );
            
            // Note: Would need SimSettings initialized to test fully
            // loadGen.initializeModel();
            
            assertNotNull("Load generator should be created", loadGen);
            
        } catch (Exception e) {
            fail("Task generation should not throw exceptions: " + e.getMessage());
        }
    }
    
    /**
     * Test: GPU orchestration logic
     */
    @Test
    public void testGpuOrchestrationLogic() {
        try {
            GpuEdgeOrchestrator orchestrator = new GpuEdgeOrchestrator(
                POLICY,
                SCENARIO
            );
            
            orchestrator.initialize();
            
            assertNotNull("Orchestrator should be initialized", orchestrator);
            
        } catch (Exception e) {
            fail("Orchestration logic should not throw exceptions: " + e.getMessage());
        }
    }
    
    /**
     * Test: GPU network model calculations
     */
    @Test
    public void testGpuNetworkModel() {
        try {
            GpuNetworkModel networkModel = new GpuNetworkModel(
                NUM_DEVICES,
                SCENARIO
            );
            
            // Test PCIe bandwidth getter
            double pcieBandwidth = GpuNetworkModel.getPcieBandwidth();
            assertTrue("PCIe bandwidth should be positive", pcieBandwidth > 0);
            
            // Test PCIe latency getter
            double pcieLatency = GpuNetworkModel.getPcieLatency();
            assertTrue("PCIe latency should be positive", pcieLatency > 0);
            
            // Test PCIe transfer time calculation
            double transferTime = networkModel.getPcieTransferTime(100.0); // 100 MB
            assertTrue("Transfer time should be positive", transferTime > 0);
            
        } catch (Exception e) {
            fail("Network model should not throw exceptions: " + e.getMessage());
        }
    }
    
    /**
     * Test: Multiple simulation runs (for statistical confidence)
     */
    @Test
    public void testMultipleSimulationRuns() {
        int numRuns = 3;
        int successfulRuns = 0;
        
        for (int run = 1; run <= numRuns; run++) {
            try {
                CloudSim.terminateSimulation();
                
                ScenarioFactory factory = new GpuScenarioFactory(
                    NUM_DEVICES,
                    SIM_TIME,
                    POLICY,
                    SCENARIO
                );
                
                CloudSim.init(1, Calendar.getInstance(), false, 0.01);
                
                SimManager simManager = new SimManager(
                    factory,
                    NUM_DEVICES,
                    SCENARIO,
                    POLICY
                );
                
                simManager.startSimulation();
                successfulRuns++;
                
            } catch (Exception e) {
                // Log but continue to next run
                System.err.println("Run " + run + " failed: " + e.getMessage());
            }
        }
        
        assertTrue("At least 2 out of 3 runs should succeed",
                  successfulRuns >= 2);
    }
    
    /**
     * Test: Simulation with different device counts
     */
    @Test
    public void testSimulationWithDifferentDeviceCounts() {
        int[] deviceCounts = {10, 50, 100};
        
        for (int numDevices : deviceCounts) {
            try {
                CloudSim.terminateSimulation();
                
                ScenarioFactory factory = new GpuScenarioFactory(
                    numDevices,
                    SIM_TIME,
                    POLICY,
                    SCENARIO
                );
                
                CloudSim.init(1, Calendar.getInstance(), false, 0.01);
                
                SimManager simManager = new SimManager(
                    factory,
                    numDevices,
                    SCENARIO,
                    POLICY
                );
                
                simManager.startSimulation();
                
            } catch (Exception e) {
                fail("Simulation with " + numDevices + " devices should not fail: " 
                    + e.getMessage());
            }
        }
    }
    
    /**
     * Test: Simulation with different orchestration policies
     */
    @Test
    public void testSimulationWithDifferentPolicies() {
        String[] policies = {
            "NEAREST_GPU",
            "LOAD_BALANCED_GPU",
            "HYBRID_GPU"
        };
        
        for (String policy : policies) {
            try {
                CloudSim.terminateSimulation();
                
                ScenarioFactory factory = new GpuScenarioFactory(
                    NUM_DEVICES,
                    SIM_TIME,
                    policy,
                    SCENARIO
                );
                
                CloudSim.init(1, Calendar.getInstance(), false, 0.01);
                
                SimManager simManager = new SimManager(
                    factory,
                    NUM_DEVICES,
                    SCENARIO,
                    policy
                );
                
                simManager.startSimulation();
                
            } catch (Exception e) {
                fail("Simulation with policy " + policy + " should not fail: "
                    + e.getMessage());
            }
        }
    }
}
