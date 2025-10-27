
/*
 * Title:        GpuEdgeCloudSim - Scenario Factory Integration Test
 * 
 * Description:  
 * End-to-end integration test for GpuScenarioFactory validating all
 * component instantiation and integration with EdgeCloudSim framework.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2025, GpuEdgeCloudSim Project
 * Author:       Pabllo Borges Cardoso
 */

package edu.boun.edgecloudsim.applications.gpusim;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeServerManager;
import edu.boun.edgecloudsim.edge_server.GpuEdgeServerManager;
import edu.boun.edgecloudsim.mobility.MobilityModel;
import edu.boun.edgecloudsim.mobility.NomadicMobility;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.boun.edgecloudsim.task_generator.LoadGeneratorModel;

/**
 * Integration test suite for GpuScenarioFactory.
 * 
 * <p>Tests validate:</p>
 * <ul>
 *   <li>Factory instantiation with valid parameters</li>
 *   <li>All component creation methods</li>
 *   <li>Correct type of returned components</li>
 *   <li>Component configuration consistency</li>
 *   <li>Integration with EdgeCloudSim framework</li>
 * </ul>
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 * @since GpuEdgeCloudSim v1.0
 */
public class GpuScenarioFactoryIntegrationTest {
    
    private GpuScenarioFactory factory;
    private static final int NUM_DEVICES = 100;
    private static final double SIM_TIME = 300.0;
    private static final String POLICY = "HYBRID_GPU";
    private static final String SCENARIO = "TEST_SCENARIO";
    
    /**
     * Set up test environment before each test.
     */
    @Before
    public void setUp() {
        factory = new GpuScenarioFactory(
            NUM_DEVICES,
            SIM_TIME,
            POLICY,
            SCENARIO
        );
    }
    
    /**
     * Test: Factory instantiation with valid parameters
     */
    @Test
    public void testFactoryInstantiation() {
        assertNotNull("Factory should be instantiated", factory);
    }
    
    /**
     * Test: Load generator model creation
     */
    @Test
    public void testGetLoadGeneratorModel() {
        LoadGeneratorModel loadGenerator = factory.getLoadGeneratorModel();
        
        assertNotNull("Load generator should not be null", loadGenerator);
        assertTrue("Load generator should be GpuLoadGeneratorModel", 
                  loadGenerator instanceof GpuLoadGeneratorModel);
    }
    
    /**
     * Test: Edge orchestrator creation
     */
    @Test
    public void testGetEdgeOrchestrator() {
        EdgeOrchestrator orchestrator = factory.getEdgeOrchestrator();
        
        assertNotNull("Orchestrator should not be null", orchestrator);
        assertTrue("Orchestrator should be GpuEdgeOrchestrator",
                  orchestrator instanceof GpuEdgeOrchestrator);
    }
    
    /**
     * Test: Mobility model creation
     */
    @Test
    public void testGetMobilityModel() {
        MobilityModel mobilityModel = factory.getMobilityModel();
        
        assertNotNull("Mobility model should not be null", mobilityModel);
        assertTrue("Mobility model should be NomadicMobility",
                  mobilityModel instanceof NomadicMobility);
    }
    
    /**
     * Test: Network model creation
     */
    @Test
    public void testGetNetworkModel() {
        NetworkModel networkModel = factory.getNetworkModel();
        
        assertNotNull("Network model should not be null", networkModel);
        assertTrue("Network model should be GpuNetworkModel",
                  networkModel instanceof GpuNetworkModel);
    }
    
    /**
     * Test: Edge server manager creation
     */
    @Test
    public void testGetEdgeServerManager() {
        EdgeServerManager edgeServerManager = factory.getEdgeServerManager();
        
        assertNotNull("Edge server manager should not be null", edgeServerManager);
        assertTrue("Edge server manager should be GpuEdgeServerManager",
                  edgeServerManager instanceof GpuEdgeServerManager);
    }
    
    /**
     * Test: All components can be created without exceptions
     */
    @Test
    public void testAllComponentsCreation() {
        try {
            assertNotNull(factory.getLoadGeneratorModel());
            assertNotNull(factory.getEdgeOrchestrator());
            assertNotNull(factory.getMobilityModel());
            assertNotNull(factory.getNetworkModel());
            assertNotNull(factory.getEdgeServerManager());
            assertNotNull(factory.getCloudServerManager());
            assertNotNull(factory.getMobileServerManager());
            assertNotNull(factory.getMobileDeviceManager());
        } catch (Exception e) {
            fail("All components should be created without exceptions: " + e.getMessage());
        }
    }
    
    /**
     * Test: Factory with different orchestrator policies
     */
    @Test
    public void testFactoryWithDifferentPolicies() {
        String[] policies = {
            "NEAREST_GPU",
            "LOAD_BALANCED_GPU",
            "MEMORY_AWARE_GPU",
            "ENERGY_EFFICIENT_GPU",
            "HYBRID_GPU"
        };
        
        for (String policy : policies) {
            GpuScenarioFactory testFactory = new GpuScenarioFactory(
                NUM_DEVICES, SIM_TIME, policy, SCENARIO
            );
            
            EdgeOrchestrator orchestrator = testFactory.getEdgeOrchestrator();
            assertNotNull("Orchestrator should be created for policy: " + policy, 
                         orchestrator);
        }
    }
    
    /**
     * Test: Factory with different scenarios
     */
    @Test
    public void testFactoryWithDifferentScenarios() {
        String[] scenarios = {
            "ML_INFERENCE_SCENARIO",
            "VIDEO_PROCESSING_SCENARIO",
            "MIXED_WORKLOAD_SCENARIO",
            "AR_VR_SCENARIO"
        };
        
        for (String scenario : scenarios) {
            GpuScenarioFactory testFactory = new GpuScenarioFactory(
                NUM_DEVICES, SIM_TIME, POLICY, scenario
            );
            
            LoadGeneratorModel loadGen = testFactory.getLoadGeneratorModel();
            assertNotNull("Load generator should be created for scenario: " + scenario,
                         loadGen);
        }
    }
    
    /**
     * Test: Factory with edge cases for number of devices
     */
    @Test
    public void testFactoryWithEdgeCaseDevices() {
        // Test with minimal devices
        GpuScenarioFactory minFactory = new GpuScenarioFactory(
            1, SIM_TIME, POLICY, SCENARIO
        );
        assertNotNull(minFactory.getLoadGeneratorModel());
        
        // Test with large number of devices
        GpuScenarioFactory maxFactory = new GpuScenarioFactory(
            10000, SIM_TIME, POLICY, SCENARIO
        );
        assertNotNull(maxFactory.getLoadGeneratorModel());
    }
    
    /**
     * Test: Factory with edge cases for simulation time
     */
    @Test
    public void testFactoryWithEdgeCaseSimTime() {
        // Test with minimal time
        GpuScenarioFactory minFactory = new GpuScenarioFactory(
            NUM_DEVICES, 10.0, POLICY, SCENARIO
        );
        assertNotNull(minFactory.getLoadGeneratorModel());
        
        // Test with large simulation time
        GpuScenarioFactory maxFactory = new GpuScenarioFactory(
            NUM_DEVICES, 10000.0, POLICY, SCENARIO
        );
        assertNotNull(maxFactory.getLoadGeneratorModel());
    }
}
