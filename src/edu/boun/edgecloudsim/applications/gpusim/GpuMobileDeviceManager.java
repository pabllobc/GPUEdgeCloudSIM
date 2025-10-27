/*
 * Title:        GpuEdgeCloudSim - GPU Mobile Device Manager
 * 
 * Description:  
 * GpuMobileDeviceManager handles GPU task submission from mobile devices,
 * coordinating with edge orchestrator for intelligent offloading decisions.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2025, GpuEdgeCloudSim Project
 * Author:       Pabllo Borges Cardoso
 */

package edu.boun.edgecloudsim.applications.gpusim;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_client.GpuTask;
import edu.boun.edgecloudsim.edge_client.MobileDeviceManager;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.TaskProperty;

/**
 * Manager for mobile devices submitting GPU-accelerated tasks.
 * 
 * <p>Extends MobileDeviceManager to handle GPU task lifecycle:</p>
 * <ul>
 *   <li><b>Task Creation:</b> Instantiate GpuTask with GPU requirements</li>
 *   <li><b>Orchestration:</b> Coordinate with GpuEdgeOrchestrator for placement</li>
 *   <li><b>Submission:</b> Send task to selected edge server/VM</li>
 *   <li><b>Result Handling:</b> Process completed GPU task results</li>
 *   <li><b>Metrics Collection:</b> Track GPU execution times, energy, etc.</li>
 * </ul>
 * 
 * <p><b>GPU Task Lifecycle:</b></p>
 * <pre>
 * ┌──────────────┐
 * │ Task Created │  (from LoadGeneratorModel)
 * └──────┬───────┘
 *        ↓
 * ┌──────────────────┐
 * │ submitTask()     │  (MobileDeviceManager)
 * └──────┬───────────┘
 *        ↓
 * ┌──────────────────────┐
 * │ getDeviceToOffload() │  (GpuEdgeOrchestrator)
 * └──────┬───────────────┘
 *        ↓
 * ┌──────────────────┐
 * │ getVmToOffload() │  (GpuEdgeOrchestrator)
 * └──────┬───────────┘
 *        ↓
 * ┌────────────────────┐
 * │ Submit to GPU VM   │  (via CloudSim)
 * └──────┬─────────────┘
 *        ↓
 * ┌────────────────────┐
 * │ GPU Execution      │  (GpuEdgeVM + Gpu)
 * └──────┬─────────────┘
 *        ↓
 * ┌────────────────────┐
 * │ processEvent()     │  (Result handling)
 * └────────────────────┘
 * </pre>
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 * @since GpuEdgeCloudSim v1.0
 */
public class GpuMobileDeviceManager extends MobileDeviceManager {
    
    /** Custom event tag for GPU task completion */
    private static final int GPU_TASK_COMPLETED = CloudSimTags.END_OF_SIMULATION + 100;
    
    /** CPU utilization model (full utilization) */
    private UtilizationModel cpuUtilizationModel;
    
    /** Task ID counter for generating unique task IDs */
    private int taskIdCounter = 0;
    
    /** Statistics counters */
    private int submittedGpuTasks = 0;
    private int completedGpuTasks = 0;
    private int failedGpuTasks = 0;
    
    /**
     * Constructs a GPU mobile device manager.
     * 
     * @throws Exception if broker initialization fails
     */
    public GpuMobileDeviceManager() throws Exception {
        super();
        this.cpuUtilizationModel = new UtilizationModelFull();
    }
    
    /**
     * Initializes the GPU mobile device manager.
     * Called by SimManager before simulation starts.
     */
    @Override
    public void initialize() {
        SimLogger.printLine("GpuMobileDeviceManager initialized.");
        
        // Reset statistics
        submittedGpuTasks = 0;
        completedGpuTasks = 0;
        failedGpuTasks = 0;
    }
    
    /**
     * Returns the CPU utilization model for tasks.
     * 
     * @return UtilizationModelFull (100% CPU utilization during execution)
     */
    @Override
    public UtilizationModel getCpuUtilizationModel() {
        return cpuUtilizationModel;
    }
    
    /**
     * Submits a GPU task for processing.
     * 
     * <p>Submission workflow:</p>
     * <ol>
     *   <li>Create GpuTask from TaskProperty</li>
     *   <li>Query orchestrator for target datacenter</li>
     *   <li>Query orchestrator for target VM</li>
     *   <li>Submit task to CloudSim for execution</li>
     *   <li>Log submission for metrics</li>
     * </ol>
     * 
     * @param taskProperty Task properties including GPU requirements
     */
    @Override
    public void submitTask(TaskProperty taskProperty) {
        double currentTime = CloudSim.clock();
        
        // Create GpuTask with characteristics
        // Generate unique task ID (use incremental counter)
        int taskId = taskIdCounter++;
        
        // Get task configuration from SimSettings
        int taskType = taskProperty.getTaskType();
        double[] appConfig = SimSettings.getInstance().getTaskLookUpTable()[taskType];
        
        // Extract GPU properties from configuration
        // appConfig indices: [0]=usage%, [1]=prob_cloud, [2]=poisson_interarrival, [3]=delay_sensitivity,
        //                    [4]=task_length, [5]=required_core, [6]=input_file, [7]=output_file
        //                    [8]=gpu_length, [9]=gpu_memory, [10]=gpu_input, [11]=gpu_output
        long gpuLength = (appConfig.length > 8) ? (long)appConfig[8] : 0;
        long gpuMemory = (appConfig.length > 9) ? (long)appConfig[9] : 0;
        long gpuInputData = (appConfig.length > 10) ? (long)appConfig[10] : 0;
        long gpuOutputData = (appConfig.length > 11) ? (long)appConfig[11] : 0;
        
        GpuTask gpuTask = new GpuTask(
            taskProperty.getMobileDeviceId(),
            taskId,
            taskProperty.getLength(),
            taskProperty.getPesNumber(),
            taskProperty.getInputFileSize(),
            taskProperty.getOutputFileSize(),
            getCpuUtilizationModel(),
            getCpuUtilizationModel(),
            getCpuUtilizationModel(),
            gpuLength,
            gpuInputData,
            gpuOutputData,
            gpuMemory
        );
        
        // Set task submission location (from mobility model)
        Location submissionLocation = SimManager.getInstance()
                                                .getMobilityModel()
                                                .getLocation(
                                                    taskProperty.getMobileDeviceId(),
                                                    currentTime
                                                );
        gpuTask.setSubmittedLocation(submissionLocation);
        
        // Set utilization models
        gpuTask.setUtilizationModelCpu(cpuUtilizationModel);
        gpuTask.setUtilizationModelRam(cpuUtilizationModel);
        gpuTask.setUtilizationModelBw(cpuUtilizationModel);
        
        // Query orchestrator for target datacenter
        int targetDatacenterId = SimManager.getInstance()
                                           .getEdgeOrchestrator()
                                           .getDeviceToOffload(gpuTask);
        
        if (targetDatacenterId == SimSettings.GENERIC_EDGE_DEVICE_ID) {
            // Offload to edge
            submitToEdge(gpuTask, targetDatacenterId);
        } else if (targetDatacenterId == SimSettings.CLOUD_DATACENTER_ID) {
            // Offload to cloud
            submitToCloud(gpuTask, targetDatacenterId);
        } else {
            // Failed to find suitable target
            SimLogger.printLine("ERROR: No suitable datacenter for GPU Task #" + taskId);
            failedGpuTasks++;
            
            // Log failure - no VM available
            SimLogger.getInstance().rejectedDueToVMCapacity(gpuTask.getCloudletId(), currentTime, 0);
        }
    }
    
    /**
     * Submits GPU task to edge datacenter.
     * 
     * @param gpuTask GPU task to submit
     * @param datacenterId Edge datacenter ID
     */
    private void submitToEdge(GpuTask gpuTask, int datacenterId) {
        double currentTime = CloudSim.clock();
        
        // Query orchestrator for target VM
        Vm targetVm = SimManager.getInstance()
                                .getEdgeOrchestrator()
                                .getVmToOffload(gpuTask, datacenterId);
        
        if (targetVm == null) {
            SimLogger.printLine("ERROR: No suitable GPU VM for Task #" + 
                              gpuTask.getCloudletId());
            failedGpuTasks++;
            
            // Log VM capacity rejection
            SimLogger.getInstance().rejectedDueToVMCapacity(gpuTask.getCloudletId(), currentTime, 0);
            return;
        }
        
        // Calculate network upload delay
        double uploadDelay = SimManager.getInstance()
                                       .getNetworkModel()
                                       .getUploadDelay(
                                           gpuTask.getMobileDeviceId(),
                                           datacenterId,
                                           gpuTask
                                       );
        
        // Set task submission time (accounting for network delay)
        gpuTask.setSubmissionTime(currentTime + uploadDelay);
        
        // Bind task to VM
        bindCloudletToVm(gpuTask.getCloudletId(), targetVm.getId());
        
        // Submit to CloudSim
        schedule(getId(), uploadDelay, CloudSimTags.CLOUDLET_SUBMIT, gpuTask);
        
        submittedGpuTasks++;
        
        SimLogger.printLine(String.format(
            "%.3f: GPU Task #%d submitted to Edge VM #%d (Upload Delay: %.3fs)",
            currentTime, gpuTask.getCloudletId(), targetVm.getId(), uploadDelay
        ));
    }
    
    /**
     * Submits GPU task to cloud datacenter.
     * 
     * @param gpuTask GPU task to submit
     * @param datacenterId Cloud datacenter ID
     */
    private void submitToCloud(GpuTask gpuTask, int datacenterId) {
        double currentTime = CloudSim.clock();
        
        // Query orchestrator for cloud VM
        Vm targetVm = SimManager.getInstance()
                                .getEdgeOrchestrator()
                                .getVmToOffload(gpuTask, datacenterId);
        
        if (targetVm == null) {
            SimLogger.printLine("ERROR: No suitable cloud VM for Task #" + 
                              gpuTask.getCloudletId());
            failedGpuTasks++;
            
            // Log VM capacity rejection
            SimLogger.getInstance().rejectedDueToVMCapacity(gpuTask.getCloudletId(), currentTime, 0);
            return;
        }
        
        // Calculate WAN upload delay (to cloud)
        double uploadDelay = SimManager.getInstance()
                                       .getNetworkModel()
                                       .getUploadDelay(
                                           gpuTask.getMobileDeviceId(),
                                           datacenterId,
                                           gpuTask
                                       );
        
        gpuTask.setSubmissionTime(currentTime + uploadDelay);
        
        bindCloudletToVm(gpuTask.getCloudletId(), targetVm.getId());
        schedule(getId(), uploadDelay, CloudSimTags.CLOUDLET_SUBMIT, gpuTask);
        
        submittedGpuTasks++;
        
        SimLogger.printLine(String.format(
            "%.3f: GPU Task #%d submitted to Cloud VM #%d (Upload Delay: %.3fs)",
            currentTime, gpuTask.getCloudletId(), targetVm.getId(), uploadDelay
        ));
    }
    
    /**
     * Processes CloudSim events including GPU task completion.
     * 
     * @param ev Simulation event to process
     */
    @Override
    public void processEvent(SimEvent ev) {
        switch (ev.getTag()) {
            case CloudSimTags.CLOUDLET_RETURN:
                processGpuTaskReturn(ev);
                break;
            default:
                super.processEvent(ev);
                break;
        }
    }
    
    /**
     * Processes completed GPU task results.
     * 
     * @param ev Event containing completed cloudlet
     */
    private void processGpuTaskReturn(SimEvent ev) {
        Cloudlet cloudlet = (Cloudlet) ev.getData();
        
        if (!(cloudlet instanceof GpuTask)) {
            // Standard task, use default processing
            super.processEvent(ev);
            return;
        }
        
        GpuTask gpuTask = (GpuTask) cloudlet;
        double currentTime = CloudSim.clock();
        
        // Calculate download delay
        int datacenterId = gpuTask.getResourceId();
        double downloadDelay = SimManager.getInstance()
                                         .getNetworkModel()
                                         .getDownloadDelay(
                                             gpuTask.getMobileDeviceId(),
                                             datacenterId,
                                             gpuTask
                                         );
        
        // Total completion time includes download
        double completionTime = currentTime + downloadDelay;
        
        // Mark as completed
        completedGpuTasks++;
        
        // Log task end
        SimLogger.getInstance().taskEnded(gpuTask.getCloudletId(), completionTime);
        
        SimLogger.printLine(String.format(
            "%.3f: GPU Task #%d completed (Exec: %.3fs, GPU: %.3fs, Download: %.3fs)",
            completionTime,
            gpuTask.getCloudletId(),
            gpuTask.getActualCPUTime(),
            gpuTask.getGpuExecutionTime(),
            downloadDelay
        ));
    }
    
    /**
     * Gets statistics for submitted GPU tasks.
     * 
     * @return Number of submitted GPU tasks
     */
    public int getSubmittedGpuTasks() {
        return submittedGpuTasks;
    }
    
    /**
     * Gets statistics for completed GPU tasks.
     * 
     * @return Number of completed GPU tasks
     */
    public int getCompletedGpuTasks() {
        return completedGpuTasks;
    }
    
    /**
     * Gets statistics for failed GPU tasks.
     * 
     * @return Number of failed GPU tasks
     */
    public int getFailedGpuTasks() {
        return failedGpuTasks;
    }
    
    /**
     * Calculates GPU task success rate.
     * 
     * @return Success rate as percentage (0-100)
     */
    public double getGpuTaskSuccessRate() {
        if (submittedGpuTasks == 0) {
            return 0.0;
        }
        return (completedGpuTasks * 100.0) / submittedGpuTasks;
    }
}
