/*
 * Title:        GpuEdgeCloudSim - GPU Network Model
 * 
 * Description:  
 * GpuNetworkModel extends standard network modeling to account for large
 * GPU data transfers, PCIe overhead, and memory bandwidth limitations.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2025, GpuEdgeCloudSim Project
 * Author:       Pabllo Borges Cardoso
 */

package edu.boun.edgecloudsim.applications.gpusim;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_client.GpuTask;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.network.MM1Queue;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.core.SimSettings.NETWORK_DELAY_TYPES;

/**
 * Network model accounting for GPU-specific data transfer characteristics.
 * 
 * <p><b>GPU Data Transfer Stages:</b></p>
 * <pre>
 * ┌─────────────┐     WLAN      ┌──────────────┐    PCIe     ┌─────┐
 * │   Mobile    │ ────────────> │ Edge Server  │ ─────────> │ GPU │
 * │   Device    │   (WiFi/5G)   │   (Host)     │  (16 GB/s) │     │
 * └─────────────┘               └──────────────┘             └─────┘
 *      ↑                                                         │
 *      └─────────────────────────────────────────────────────────┘
 *                    Result Transfer (Output Data)
 * </pre>
 * 
 * <p><b>Delay Components:</b></p>
 * <ul>
 *   <li><b>WLAN Upload:</b> Mobile device → Edge server (input data)</li>
 *   <li><b>PCIe Transfer:</b> Host memory → GPU memory (GPU input)</li>
 *   <li><b>GPU Computation:</b> Kernel execution time</li>
 *   <li><b>PCIe Transfer:</b> GPU memory → Host memory (GPU output)</li>
 *   <li><b>WLAN Download:</b> Edge server → Mobile device (result)</li>
 * </ul>
 * 
 * <p><b>Network Characteristics:</b></p>
 * <table border="1">
 * <tr><th>Link Type</th><th>Bandwidth</th><th>Latency</th></tr>
 * <tr><td>WLAN (WiFi 6)</td><td>100-300 Mbps</td><td>5-20 ms</td></tr>
 * <tr><td>5G</td><td>500-1000 Mbps</td><td>10-30 ms</td></tr>
 * <tr><td>WAN (to Cloud)</td><td>20-50 Mbps</td><td>50-200 ms</td></tr>
 * <tr><td>PCIe 3.0 x16</td><td>15.75 GB/s</td><td>< 1 ms</td></tr>
 * <tr><td>GPU Memory Bus</td><td>300-900 GB/s</td><td>< 0.1 ms</td></tr>
 * </table>
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 * @since GpuEdgeCloudSim v1.0
 */
public class GpuNetworkModel extends NetworkModel {
    
    /** PCIe bandwidth in GB/s (PCIe 3.0 x16) */
    private static final double PCIE_BANDWIDTH_GBPS = 15.75;
    
    /** PCIe latency in milliseconds */
    private static final double PCIE_LATENCY_MS = 0.5;
    
    /** Number of mobile devices */
    private int numberOfMobileDevices;
    
    /** Simulation scenario */
    private String simScenario;
    
    /** MM1 queue for WLAN modeling */
    private MM1Queue wlanQueue;
    
    /**
     * Constructs a GPU network model.
     * 
     * @param _numberOfMobileDevices Number of mobile devices
     * @param _simScenario Simulation scenario name
     */
    public GpuNetworkModel(int _numberOfMobileDevices, String _simScenario) {
        super(_numberOfMobileDevices, _simScenario);
        this.numberOfMobileDevices = _numberOfMobileDevices;
        this.simScenario = _simScenario;
    }
    
    /**
     * Initializes the network model with queue configurations.
     */
    @Override
    public void initialize() {
        super.initialize();
        
        // Initialize WLAN queue for GPU data transfers
        double wlanServiceRate = SimSettings.getInstance().getWlanBandwidth() / 
                                (double) SimSettings.getInstance().getTaskLookUpTable()[0].getInputFileSize();
        
        wlanQueue = new MM1Queue(wlanServiceRate, "WLAN_GPU");
        
        SimLogger.printLine("GpuNetworkModel initialized with PCIe bandwidth: " + 
                          PCIE_BANDWIDTH_GBPS + " GB/s");
    }
    
    /**
     * Calculates upload delay including GPU data transfer to edge server.
     * 
     * <p>Delay calculation:</p>
     * <pre>
     * Total Upload Delay = WLAN Upload + PCIe Transfer (Host → GPU)
     * 
     * WLAN Upload = baseDelay + (fileSize / wlanBandwidth)
     * PCIe Transfer = PCIE_LATENCY + (gpuInputData / pcieBandwidth)
     * </pre>
     * 
     * @param deviceId Mobile device ID
     * @param datacenterId Target datacenter ID
     * @param task Task being uploaded
     * @return Total upload delay in seconds
     */
    @Override
    public double getUploadDelay(int deviceId, int datacenterId, Task task) {
        // Get standard WLAN upload delay
        double wlanUploadDelay = super.getUploadDelay(deviceId, datacenterId, task);
        
        // Check if this is a GPU task requiring additional PCIe transfer
        if (!(task instanceof GpuTask)) {
            return wlanUploadDelay;
        }
        
        GpuTask gpuTask = (GpuTask) task;
        
        // Calculate PCIe transfer time for GPU input data
        double gpuInputDataMB = gpuTask.getGpuInputData();
        double gpuInputDataGB = gpuInputDataMB / 1024.0;
        
        double pcieTransferTime = (PCIE_LATENCY_MS / 1000.0) + 
                                 (gpuInputDataGB / PCIE_BANDWIDTH_GBPS);
        
        // Total upload delay
        double totalDelay = wlanUploadDelay + pcieTransferTime;
        
        SimLogger.printLine(String.format(
            "  GPU Upload Delay: WLAN=%.3fs, PCIe=%.3fs, Total=%.3fs",
            wlanUploadDelay, pcieTransferTime, totalDelay
        ));
        
        return totalDelay;
    }
    
    /**
     * Calculates download delay including GPU data transfer from edge server.
     * 
     * <p>Delay calculation:</p>
     * <pre>
     * Total Download Delay = PCIe Transfer (GPU → Host) + WLAN Download
     * 
     * PCIe Transfer = PCIE_LATENCY + (gpuOutputData / pcieBandwidth)
     * WLAN Download = baseDelay + (outputSize / wlanBandwidth)
     * </pre>
     * 
     * @param deviceId Mobile device ID
     * @param datacenterId Source datacenter ID
     * @param task Task being downloaded
     * @return Total download delay in seconds
     */
    @Override
    public double getDownloadDelay(int deviceId, int datacenterId, Task task) {
        // Get standard WLAN download delay
        double wlanDownloadDelay = super.getDownloadDelay(deviceId, datacenterId, task);
        
        // Check if this is a GPU task
        if (!(task instanceof GpuTask)) {
            return wlanDownloadDelay;
        }
        
        GpuTask gpuTask = (GpuTask) task;
        
        // Calculate PCIe transfer time for GPU output data
        double gpuOutputDataMB = gpuTask.getGpuOutputData();
        double gpuOutputDataGB = gpuOutputDataMB / 1024.0;
        
        double pcieTransferTime = (PCIE_LATENCY_MS / 1000.0) + 
                                 (gpuOutputDataGB / PCIE_BANDWIDTH_GBPS);
        
        // Total download delay
        double totalDelay = pcieTransferTime + wlanDownloadDelay;
        
        SimLogger.printLine(String.format(
            "  GPU Download Delay: PCIe=%.3fs, WLAN=%.3fs, Total=%.3fs",
            pcieTransferTime, wlanDownloadDelay, totalDelay
        ));
        
        return totalDelay;
    }
    
    /**
     * Calculates network delay between datacenters (MAN/WAN).
     * 
     * @param srcDeviceId Source device/datacenter ID
     * @param destDeviceId Destination device/datacenter ID
     * @param task Task being transferred
     * @param delayType Type of network delay (MAN or WAN)
     * @return Network delay in seconds
     */
    @Override
    public double getNetworkDelay(int srcDeviceId, int destDeviceId, Task task, 
                                  NETWORK_DELAY_TYPES delayType) {
        // For GPU tasks, standard inter-datacenter delays apply
        // (GPU data is already in host memory at source datacenter)
        return super.getNetworkDelay(srcDeviceId, destDeviceId, task, delayType);
    }
    
    /**
     * Estimates total network overhead for a GPU task.
     * 
     * <p>Includes:</p>
     * <ul>
     *   <li>WLAN upload from mobile device</li>
     *   <li>PCIe transfer to GPU</li>
     *   <li>PCIe transfer from GPU</li>
     *   <li>WLAN download to mobile device</li>
     * </ul>
     * 
     * @param gpuTask GPU task to analyze
     * @param deviceId Mobile device ID
     * @param datacenterId Edge datacenter ID
     * @return Total network overhead in seconds
     */
    public double getTotalGpuNetworkOverhead(GpuTask gpuTask, int deviceId, int datacenterId) {
        double uploadDelay = getUploadDelay(deviceId, datacenterId, gpuTask);
        double downloadDelay = getDownloadDelay(deviceId, datacenterId, gpuTask);
        
        return uploadDelay + downloadDelay;
    }
    
    /**
     * Calculates PCIe transfer time for given data size.
     * 
     * @param dataSizeMB Data size in megabytes
     * @return Transfer time in seconds
     */
    public double getPcieTransferTime(double dataSizeMB) {
        double dataSizeGB = dataSizeMB / 1024.0;
        return (PCIE_LATENCY_MS / 1000.0) + (dataSizeGB / PCIE_BANDWIDTH_GBPS);
    }
    
    /**
     * Gets PCIe bandwidth in GB/s.
     * 
     * @return PCIe bandwidth
     */
    public static double getPcieBandwidth() {
        return PCIE_BANDWIDTH_GBPS;
    }
    
    /**
     * Gets PCIe latency in milliseconds.
     * 
     * @return PCIe latency
     */
    public static double getPcieLatency() {
        return PCIE_LATENCY_MS;
    }
}
