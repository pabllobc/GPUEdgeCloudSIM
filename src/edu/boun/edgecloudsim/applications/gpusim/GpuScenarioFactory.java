/*
 * Title:        GpuEdgeCloudSim - GPU Scenario Factory
 * 
 * Description:  
 * GpuScenarioFactory provides GPU-enabled instances of all required
 * EdgeCloudSim components for GPU-accelerated edge computing simulations.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2025, GpuEdgeCloudSim Project
 * Author:       Pabllo Borges Cardoso
 */

package edu.boun.edgecloudsim.applications.gpusim;

import edu.boun.edgecloudsim.cloud_server.CloudServerManager;
import edu.boun.edgecloudsim.cloud_server.DefaultCloudServerManager;
import edu.boun.edgecloudsim.core.ScenarioFactory;
import edu.boun.edgecloudsim.edge_client.MobileDeviceManager;
import edu.boun.edgecloudsim.edge_client.mobile_processing_unit.DefaultMobileServerManager;
import edu.boun.edgecloudsim.edge_client.mobile_processing_unit.MobileServerManager;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeServerManager;
import edu.boun.edgecloudsim.edge_server.GpuEdgeServerManager;
import edu.boun.edgecloudsim.mobility.MobilityModel;
import edu.boun.edgecloudsim.mobility.NomadicMobility;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.boun.edgecloudsim.task_generator.IdleActiveLoadGenerator;
import edu.boun.edgecloudsim.task_generator.LoadGeneratorModel;

/**
 * Factory for creating GPU-enabled EdgeCloudSim simulation components.
 * 
 * This factory provides GPU-aware implementations of all core EdgeCloudSim modules,
 * enabling simulations of GPU-accelerated edge computing scenarios with realistic
 * GPU task scheduling, resource allocation, and energy modeling.
 * 
 * <p><b>Architecture Overview:</b></p>
 * <pre>
 * ┌─────────────────────────────────────────────────┐
 * │           GpuScenarioFactory                    │
 * ├─────────────────────────────────────────────────┤
 * │ + getLoadGeneratorModel() → GpuLoadGenerator    │
 * │ + getEdgeOrchestrator() → GpuEdgeOrchestrator   │
 * │ + getMobilityModel() → NomadicMobility          │
 * │ + getNetworkModel() → GpuNetworkModel           │
 * │ + getEdgeServerManager() → GpuEdgeServerManager │
 * │ + getCloudServerManager() → DefaultCloudServer  │
 * │ + getMobileServerManager() → DefaultMobile...   │
 * │ + getMobileDeviceManager() → GpuDeviceManager   │
 * └─────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>GPU-aware task generation with compute-intensive workloads</li>
 *   <li>Intelligent GPU task orchestration and offloading</li>
 *   <li>GPU-enabled edge server infrastructure management</li>
 *   <li>Network modeling considering GPU data transfer overhead</li>
 *   <li>Mobile device management for GPU task submission</li>
 * </ul>
 * 
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * // Create factory with simulation parameters
 * GpuScenarioFactory factory = new GpuScenarioFactory(
 *     1000,                    // Number of mobile devices
 *     600.0,                   // Simulation time (seconds)
 *     "GPU_AWARE_POLICY",      // Orchestration policy
 *     "GPU_INTENSIVE_SCENARIO" // Scenario name
 * );
 * 
 * // Use factory in SimManager
 * SimManager simManager = SimManager.getInstance();
 * simManager.startSimulation(factory);
 * }</pre>
 * 
 * <p><b>Configuration Requirements:</b></p>
 * <ul>
 *   <li>edge_devices.xml: Must include GPU specifications in host definitions</li>
 *   <li>applications.xml: Must define GPU task characteristics (gpuLength, gpuMemory)</li>
 *   <li>config.properties: Must set gpu_enabled=true and related parameters</li>
 * </ul>
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 * @since GpuEdgeCloudSim v1.0
 * 
 * @see edu.boun.edgecloudsim.core.ScenarioFactory
 * @see edu.boun.edgecloudsim.edge_server.GpuEdgeServerManager
 * @see GpuEdgeOrchestrator
 * @see GpuLoadGeneratorModel
 */
public class GpuScenarioFactory implements ScenarioFactory {
    
    /** Number of mobile devices in the simulation */
    private int numOfMobileDevice;
    
    /** Total simulation time in seconds */
    private double simulationTime;
    
    /** Orchestration policy for GPU task placement */
    private String orchestratorPolicy;
    
    /** Simulation scenario name */
    private String simScenario;
    
    /**
     * Constructs a GpuScenarioFactory with specified simulation parameters.
     * 
     * @param _numOfMobileDevice Number of mobile devices generating GPU tasks
     * @param _simulationTime Total simulation duration in seconds
     * @param _orchestratorPolicy Orchestration policy (e.g., "GPU_AWARE", "LOAD_BALANCED")
     * @param _simScenario Scenario name (e.g., "ML_INFERENCE", "VIDEO_PROCESSING")
     */
    public GpuScenarioFactory(int _numOfMobileDevice,
                              double _simulationTime,
                              String _orchestratorPolicy,
                              String _simScenario) {
        this.numOfMobileDevice = _numOfMobileDevice;
        this.simulationTime = _simulationTime;
        this.orchestratorPolicy = _orchestratorPolicy;
        this.simScenario = _simScenario;
    }
    
    /**
     * Creates a GPU-aware load generator model.
     * 
     * <p>Generates tasks with GPU processing requirements including:</p>
     * <ul>
     *   <li>GPU computation length (GFLOPs)</li>
     *   <li>GPU memory requirements (MB)</li>
     *   <li>CPU-GPU data transfer sizes</li>
     *   <li>Task arrival patterns (Poisson or Active/Idle)</li>
     * </ul>
     * 
     * @return GpuLoadGeneratorModel instance for GPU task generation
     */
    @Override
    public LoadGeneratorModel getLoadGeneratorModel() {
        return new GpuLoadGeneratorModel(numOfMobileDevice, simulationTime, simScenario);
    }
    
    /**
     * Creates a GPU-aware edge orchestrator.
     * 
     * <p>Implements intelligent orchestration considering:</p>
     * <ul>
     *   <li>GPU availability and utilization</li>
     *   <li>GPU memory constraints</li>
     *   <li>Network latency for data transfer</li>
     *   <li>Energy consumption of GPU processing</li>
     * </ul>
     * 
     * @return GpuEdgeOrchestrator instance for GPU task placement decisions
     */
    @Override
    public EdgeOrchestrator getEdgeOrchestrator() {
        return new GpuEdgeOrchestrator(orchestratorPolicy, simScenario);
    }
    
    /**
     * Creates a mobility model for mobile devices.
     * 
     * <p>Uses nomadic mobility pattern where devices:</p>
     * <ul>
     *   <li>Stay within a region for a period</li>
     *   <li>Move to new locations occasionally</li>
     *   <li>Realistic for smartphone and IoT scenarios</li>
     * </ul>
     * 
     * @return NomadicMobility instance for device movement simulation
     */
    @Override
    public MobilityModel getMobilityModel() {
        return new NomadicMobility(numOfMobileDevice, simulationTime);
    }
    
    /**
     * Creates a GPU-aware network model.
     * 
     * <p>Extends standard network model to account for:</p>
     * <ul>
     *   <li>Large GPU data transfers (input/output tensors)</li>
     *   <li>Memory bandwidth limitations</li>
     *   <li>PCIe transfer overhead</li>
     *   <li>Network congestion from parallel GPU tasks</li>
     * </ul>
     * 
     * @return GpuNetworkModel instance for GPU-aware network simulation
     */
    @Override
    public NetworkModel getNetworkModel() {
        return new GpuNetworkModel(numOfMobileDevice, simScenario);
    }
    
    /**
     * Creates a GPU-enabled edge server manager.
     * 
     * <p>Manages GPU-equipped edge infrastructure including:</p>
     * <ul>
     *   <li>GpuEdgeHosts with CPU + GPU resources</li>
     *   <li>GpuEdgeVMs with GPU allocation</li>
     *   <li>GPU provisioning and scheduling</li>
     *   <li>GPU energy modeling</li>
     * </ul>
     * 
     * @return GpuEdgeServerManager instance for GPU infrastructure management
     */
    @Override
    public EdgeServerManager getEdgeServerManager() {
        return new GpuEdgeServerManager();
    }
    
    /**
     * Creates a cloud server manager (standard implementation).
     * 
     * <p>Uses default CloudServerManager as cloud layer typically has
     * ample GPU resources and is not the bottleneck in edge scenarios.</p>
     * 
     * @return DefaultCloudServerManager instance
     */
    @Override
    public CloudServerManager getCloudServerManager() {
        return new DefaultCloudServerManager();
    }
    
    /**
     * Creates a mobile server manager (standard implementation).
     * 
     * <p>Mobile devices typically don't have integrated GPUs in edge scenarios,
     * so uses default implementation for CPU-only local processing.</p>
     * 
     * @return DefaultMobileServerManager instance
     */
    @Override
    public MobileServerManager getMobileServerManager() {
        return new DefaultMobileServerManager();
    }
    
    /**
     * Creates a GPU-aware mobile device manager.
     * 
     * <p>Manages mobile device operations including:</p>
     * <ul>
     *   <li>GPU task submission to edge servers</li>
     *   <li>Task result reception and processing</li>
     *   <li>Communication with edge orchestrator</li>
     *   <li>Performance metrics collection</li>
     * </ul>
     * 
     * @return GpuMobileDeviceManager instance for mobile device coordination
     * @throws Exception if mobile device manager creation fails
     */
    @Override
    public MobileDeviceManager getMobileDeviceManager() throws Exception {
        return new GpuMobileDeviceManager();
    }
}
