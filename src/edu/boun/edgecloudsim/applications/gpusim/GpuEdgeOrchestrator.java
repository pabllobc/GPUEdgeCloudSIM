/*
 * Title:        GpuEdgeCloudSim - GPU Edge Orchestrator
 * 
 * Description:  
 * GpuEdgeOrchestrator makes intelligent task offloading decisions considering
 * GPU availability, utilization, memory, network latency, and energy consumption.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2025, GpuEdgeCloudSim Project
 * Author:       Pabllo Borges Cardoso
 */

package edu.boun.edgecloudsim.applications.gpusim;

import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_client.GpuTask;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.boun.edgecloudsim.edge_server.Gpu;
import edu.boun.edgecloudsim.edge_server.GpuEdgeHost;
import edu.boun.edgecloudsim.edge_server.GpuEdgeVM;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.core.SimSettings.NETWORK_DELAY_TYPES;

/**
 * GPU-aware edge orchestrator for intelligent task placement.
 * 
 * <p>Implements sophisticated orchestration policies considering:</p>
 * <ul>
 *   <li><b>GPU Availability:</b> Ensures target edge server has free GPUs</li>
 *   <li><b>GPU Memory:</b> Validates sufficient VRAM for task requirements</li>
 *   <li><b>Network Latency:</b> Prioritizes nearby edge servers to minimize data transfer</li>
 *   <li><b>Load Balancing:</b> Distributes tasks to prevent GPU overload</li>
 *   <li><b>Energy Efficiency:</b> Considers GPU power consumption in decisions</li>
 * </ul>
 * 
 * <p><b>Supported Orchestration Policies:</b></p>
 * <pre>
 * ┌──────────────────────┬────────────────────────────────────────────┐
 * │ Policy               │ Description                                │
 * ├──────────────────────┼────────────────────────────────────────────┤
 * │ NEAREST_GPU          │ Select closest edge with available GPU     │
 * │ LOAD_BALANCED_GPU    │ Distribute tasks to least utilized GPU     │
 * │ MEMORY_AWARE_GPU     │ Prioritize GPUs with most free memory      │
 * │ ENERGY_EFFICIENT_GPU │ Minimize GPU energy consumption            │
 * │ HYBRID_GPU           │ Weighted combination of metrics            │
 * └──────────────────────┴────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>Decision Flow:</b></p>
 * <pre>
 * Task Arrival
 *      ↓
 * Check if GPU Required
 *      ↓
 * [YES] → Find Edge Servers with Available GPUs
 *      ↓
 * Apply Orchestration Policy
 *      ↓
 * Select Best Edge Server
 *      ↓
 * Select Best VM with GPU
 *      ↓
 * Return Offloading Decision
 * </pre>
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 * @since GpuEdgeCloudSim v1.0
 */
public class GpuEdgeOrchestrator extends EdgeOrchestrator {
    
    /** Orchestration policy identifier */
    private String policy;
    
    /** Simulation scenario name */
    private String simScenario;
    
    /** Weight for network latency in hybrid policy */
    private static final double LATENCY_WEIGHT = 0.4;
    
    /** Weight for GPU utilization in hybrid policy */
    private static final double GPU_UTIL_WEIGHT = 0.3;
    
    /** Weight for GPU memory in hybrid policy */
    private static final double GPU_MEMORY_WEIGHT = 0.3;
    
    /**
     * Constructs a GPU edge orchestrator with specified policy.
     * 
     * @param _policy Orchestration policy name
     * @param _simScenario Simulation scenario name
     */
    public GpuEdgeOrchestrator(String _policy, String _simScenario) {
        this.policy = _policy;
        this.simScenario = _simScenario;
        
        SimLogger.printLine("GpuEdgeOrchestrator initialized with policy: " + _policy);
    }
    
    /**
     * Initializes the orchestrator (called by SimManager).
     */
    @Override
    public void initialize() {
        SimLogger.printLine("GpuEdgeOrchestrator initialization complete.");
    }
    
    /**
     * Determines which edge server (datacenter) should process the task.
     * 
     * <p>Decision logic:</p>
     * <ol>
     *   <li>Check if task requires GPU (instanceof GpuTask)</li>
     *   <li>Get list of edge datacenters with available GPUs</li>
     *   <li>Apply orchestration policy to select best datacenter</li>
     *   <li>Return datacenter index (SimSettings.GENERIC_EDGE_DEVICE_ID for edge)</li>
     * </ol>
     * 
     * @param task Task to be offloaded
     * @return Datacenter ID (SimSettings.GENERIC_EDGE_DEVICE_ID for edge,
     *         SimSettings.CLOUD_DATACENTER_ID for cloud)
     */
    @Override
    public int getDeviceToOffload(Task task) {
        // Check if this is a GPU task
        if (!(task instanceof GpuTask)) {
            // Not a GPU task, use standard edge offloading
            return SimSettings.GENERIC_EDGE_DEVICE_ID;
        }
        
        GpuTask gpuTask = (GpuTask) task;
        
        // Get task location
        Location taskLocation = task.getSubmittedLocation();
        
        // Get all edge datacenters
        List<Datacenter> edgeDatacenters = SimManager.getInstance()
                                                      .getEdgeServerManager()
                                                      .getDatacenterList();
        
        // Find best datacenter based on policy
        int bestDatacenterId = -1;
        double bestScore = Double.MAX_VALUE;
        
        for (Datacenter datacenter : edgeDatacenters) {
            // Check if datacenter has available GPU resources
            if (!hasAvailableGpu(datacenter, gpuTask)) {
                continue;
            }
            
            // Calculate score based on policy
            double score = calculateDatacenterScore(datacenter, gpuTask, taskLocation);
            
            if (score < bestScore) {
                bestScore = score;
                bestDatacenterId = datacenter.getId();
            }
        }
        
        // If no edge server has available GPU, offload to cloud
        if (bestDatacenterId == -1) {
            SimLogger.printLine(CloudSim.clock() + ": No available GPU on edge for Task #" + 
                              task.getCloudletId() + ", offloading to cloud");
            return SimSettings.CLOUD_DATACENTER_ID;
        }
        
        return SimSettings.GENERIC_EDGE_DEVICE_ID;
    }
    
    /**
     * Selects which VM should execute the task on the chosen datacenter.
     * 
     * <p>VM selection criteria:</p>
     * <ul>
     *   <li>VM must have GPU allocated (for GPU tasks)</li>
     *   <li>GPU must have sufficient memory</li>
     *   <li>VM must have sufficient CPU resources</li>
     *   <li>Prefer VMs with lower current load</li>
     * </ul>
     * 
     * @param task Task to be executed
     * @param deviceId Datacenter ID where task will execute
     * @return VM instance to execute the task, or null if none available
     */
    @Override
    public Vm getVmToOffload(Task task, int deviceId) {
        if (!(task instanceof GpuTask)) {
            // For non-GPU tasks, use standard VM selection
            return getAvailableEdgeVm(deviceId);
        }
        
        GpuTask gpuTask = (GpuTask) task;
        
        // Get datacenter
        Datacenter datacenter = CloudSim.getEntity(deviceId);
        if (datacenter == null) {
            SimLogger.printLine("ERROR: Datacenter " + deviceId + " not found!");
            return null;
        }
        
        // Search for best VM with GPU
        GpuEdgeVM bestVm = null;
        double bestUtilization = Double.MAX_VALUE;
        
        for (Host host : datacenter.getHostList()) {
            if (!(host instanceof GpuEdgeHost)) {
                continue;
            }
            
            GpuEdgeHost gpuHost = (GpuEdgeHost) host;
            
            for (Vm vm : host.getVmList()) {
                if (!(vm instanceof GpuEdgeVM)) {
                    continue;
                }
                
                GpuEdgeVM gpuVm = (GpuEdgeVM) vm;
                
                // Check if VM has GPU allocated
                if (!gpuVm.hasGpu()) {
                    continue;
                }
                
                // Check if GPU has sufficient memory
                Gpu gpu = gpuVm.getGpu();
                if (gpu.getAvailableMemory() < gpuTask.getRequiredGpuMemory()) {
                    continue;
                }
                
                // Check VM CPU availability
                if (gpuVm.getCurrentRequestedTotalMips() >= gpuVm.getMips() * gpuVm.getNumberOfPes()) {
                    continue;
                }
                
                // Calculate combined utilization (CPU + GPU)
                double cpuUtil = gpuVm.getCurrentRequestedTotalMips() / 
                               (gpuVm.getMips() * gpuVm.getNumberOfPes());
                double gpuUtil = gpu.getUtilization() / 100.0;
                double combinedUtil = (cpuUtil + gpuUtil) / 2.0;
                
                if (combinedUtil < bestUtilization) {
                    bestUtilization = combinedUtil;
                    bestVm = gpuVm;
                }
            }
        }
        
        if (bestVm == null) {
            SimLogger.printLine("WARNING: No suitable GPU VM found for Task #" + 
                              task.getCloudletId());
        }
        
        return bestVm;
    }
    
    /**
     * Checks if a datacenter has available GPU resources for a task.
     * 
     * @param datacenter Datacenter to check
     * @param gpuTask GPU task requiring resources
     * @return true if datacenter has available GPU, false otherwise
     */
    private boolean hasAvailableGpu(Datacenter datacenter, GpuTask gpuTask) {
        for (Host host : datacenter.getHostList()) {
            if (!(host instanceof GpuEdgeHost)) {
                continue;
            }
            
            GpuEdgeHost gpuHost = (GpuEdgeHost) host;
            
            // Check if host has available GPU with sufficient memory
            for (Gpu gpu : gpuHost.getGpuList()) {
                if (gpu.isAvailable() || 
                    gpu.getAvailableMemory() >= gpuTask.getRequiredGpuMemory()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Calculates a score for datacenter based on orchestration policy.
     * Lower score = better choice.
     * 
     * @param datacenter Datacenter to evaluate
     * @param gpuTask GPU task to place
     * @param taskLocation Task submission location
     * @return Score (lower is better)
     */
    private double calculateDatacenterScore(Datacenter datacenter, 
                                           GpuTask gpuTask, 
                                           Location taskLocation) {
        switch (policy) {
            case "NEAREST_GPU":
                return calculateNetworkLatency(datacenter, taskLocation);
                
            case "LOAD_BALANCED_GPU":
                return calculateGpuUtilization(datacenter);
                
            case "MEMORY_AWARE_GPU":
                return calculateMemoryPressure(datacenter);
                
            case "ENERGY_EFFICIENT_GPU":
                return calculateEnergyScore(datacenter);
                
            case "HYBRID_GPU":
            default:
                return calculateHybridScore(datacenter, taskLocation);
        }
    }
    
    /**
     * Calculates network latency to datacenter.
     */
    private double calculateNetworkLatency(Datacenter datacenter, Location taskLocation) {
        // Get datacenter location from first host
        if (datacenter.getHostList().isEmpty()) {
            return Double.MAX_VALUE;
        }
        
        Host host = datacenter.getHostList().get(0);
        if (!(host instanceof GpuEdgeHost)) {
            return Double.MAX_VALUE;
        }
        
        Location datacenterLocation = ((GpuEdgeHost) host).getLocation();
        double distance = taskLocation.getDistance(datacenterLocation);
        
        // Estimate latency based on distance (simplified model)
        return distance * SimSettings.getInstance().getInternalLanDelay();
    }
    
    /**
     * Calculates average GPU utilization of datacenter.
     */
    private double calculateGpuUtilization(Datacenter datacenter) {
        double totalUtil = 0.0;
        int gpuCount = 0;
        
        for (Host host : datacenter.getHostList()) {
            if (!(host instanceof GpuEdgeHost)) {
                continue;
            }
            
            GpuEdgeHost gpuHost = (GpuEdgeHost) host;
            for (Gpu gpu : gpuHost.getGpuList()) {
                totalUtil += gpu.getUtilization();
                gpuCount++;
            }
        }
        
        return gpuCount > 0 ? totalUtil / gpuCount : 100.0;
    }
    
    /**
     * Calculates memory pressure (inverse of available memory).
     */
    private double calculateMemoryPressure(Datacenter datacenter) {
        long totalAvailableMemory = 0;
        
        for (Host host : datacenter.getHostList()) {
            if (!(host instanceof GpuEdgeHost)) {
                continue;
            }
            
            GpuEdgeHost gpuHost = (GpuEdgeHost) host;
            totalAvailableMemory += gpuHost.getAvailableGpuMemory();
        }
        
        // Return inverse (lower available memory = higher score)
        return totalAvailableMemory > 0 ? 1.0 / totalAvailableMemory : Double.MAX_VALUE;
    }
    
    /**
     * Calculates energy score (simplified model).
     */
    private double calculateEnergyScore(Datacenter datacenter) {
        // Energy is proportional to GPU utilization
        return calculateGpuUtilization(datacenter);
    }
    
    /**
     * Calculates hybrid score combining multiple metrics.
     */
    private double calculateHybridScore(Datacenter datacenter, Location taskLocation) {
        double latency = calculateNetworkLatency(datacenter, taskLocation);
        double gpuUtil = calculateGpuUtilization(datacenter);
        double memPressure = calculateMemoryPressure(datacenter);
        
        // Normalize and combine with weights
        double normalizedLatency = latency / 100.0; // Assume max 100ms
        double normalizedUtil = gpuUtil / 100.0;
        double normalizedMemory = memPressure * 1000.0; // Scale memory pressure
        
        return (LATENCY_WEIGHT * normalizedLatency) +
               (GPU_UTIL_WEIGHT * normalizedUtil) +
               (GPU_MEMORY_WEIGHT * normalizedMemory);
    }
    
    /**
     * Helper method to get an available edge VM (for non-GPU tasks).
     */
    private Vm getAvailableEdgeVm(int deviceId) {
        Datacenter datacenter = CloudSim.getEntity(deviceId);
        if (datacenter == null) {
            return null;
        }
        
        for (Host host : datacenter.getHostList()) {
            for (Vm vm : host.getVmList()) {
                if (vm instanceof EdgeVM) {
                    EdgeVM edgeVm = (EdgeVM) vm;
                    if (edgeVm.getCurrentRequestedTotalMips() < 
                        edgeVm.getMips() * edgeVm.getNumberOfPes()) {
                        return edgeVm;
                    }
                }
            }
        }
        
        return null;
    }
}
