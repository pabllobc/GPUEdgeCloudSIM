/*
 * Title:        GpuEdgeCloudSim - GPU Load Generator Model
 * 
 * Description:  
 * GpuLoadGeneratorModel generates GPU-intensive tasks with realistic
 * characteristics for ML inference, video processing, and scientific computing.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2025, GpuEdgeCloudSim Project
 * Author:       Pabllo Borges Cardoso
 */

package edu.boun.edgecloudsim.applications.gpusim;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_client.GpuTask;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.task_generator.LoadGeneratorModel;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;
import edu.boun.edgecloudsim.utils.TaskProperty;

/**
 * Load generator for GPU-intensive edge computing tasks.
 * 
 * <p>Generates tasks with realistic GPU workload characteristics including:</p>
 * <ul>
 *   <li><b>ML Inference:</b> Object detection, image classification (100-500 GFLOPs)</li>
 *   <li><b>Video Processing:</b> Transcoding, filtering (500-2000 GFLOPs)</li>
 *   <li><b>Scientific Computing:</b> Simulations, matrix operations (1000-5000 GFLOPs)</li>
 *   <li><b>AR/VR Rendering:</b> Real-time graphics (200-800 GFLOPs)</li>
 * </ul>
 * 
 * <p><b>Task Generation Model:</b></p>
 * <pre>
 * Task Arrival: Poisson process with configurable λ (lambda)
 * GPU Length: Based on application type (applications.xml)
 * GPU Memory: Proportional to computation complexity
 * Data Transfer: Input/output sizes for CPU-GPU communication
 * </pre>
 * 
 * <p><b>XML Configuration Example:</b></p>
 * <pre>{@code
 * <application name="ML_INFERENCE">
 *   <usage_percentage>40</usage_percentage>
 *   <prob_cloud_selection>20</prob_cloud_selection>
 *   <poisson_interarrival>10</poisson_interarrival>
 *   <delay_sensitivity>0.6</delay_sensitivity>
 *   <task_length>5000</task_length>
 *   <required_core>1</required_core>
 *   <gpu_length>250000</gpu_length>
 *   <gpu_memory>4096</gpu_memory>
 *   <gpu_input_data>100</gpu_input_data>
 *   <gpu_output_data>50</gpu_output_data>
 * </application>
 * }</pre>
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 * @since GpuEdgeCloudSim v1.0
 */
public class GpuLoadGeneratorModel extends LoadGeneratorModel {
    
    /** Number of mobile devices generating tasks */
    private int numberOfMobileDevices;
    
    /** Total simulation duration in seconds */
    private double simulationTime;
    
    /** Simulation scenario name */
    private String simScenario;
    
    /** Task list for each mobile device */
    private List<GpuTaskSchedule>[] taskList;
    
    /**
     * Internal class representing a scheduled GPU task.
     */
    private class GpuTaskSchedule {
        double startTime;
        int taskType;
        int mobileDeviceId;
        
        GpuTaskSchedule(int _mobileDeviceId, double _startTime, int _taskType) {
            mobileDeviceId = _mobileDeviceId;
            startTime = _startTime;
            taskType = _taskType;
        }
    }
    
    /**
     * Constructs a GPU load generator model.
     * 
     * @param _numberOfMobileDevices Number of mobile devices
     * @param _simulationTime Simulation duration in seconds
     * @param _simScenario Scenario name
     */
    @SuppressWarnings("unchecked")
    public GpuLoadGeneratorModel(int _numberOfMobileDevices, double _simulationTime, String _simScenario) {
        this.numberOfMobileDevices = _numberOfMobileDevices;
        this.simulationTime = _simulationTime;
        this.simScenario = _simScenario;
        this.taskList = new ArrayList[SimSettings.getInstance().getTaskLookUpTable().length];
    }
    
    /**
     * Initializes the load generator by creating task schedules.
     * 
     * <p>Generation process:</p>
     * <ol>
     *   <li>For each application type, determine active devices</li>
     *   <li>Generate task arrival times using Poisson process</li>
     *   <li>Create GpuTaskSchedule entries with timing and type</li>
     *   <li>Store schedules for later task submission</li>
     * </ol>
     */
    @Override
    public void initializeModel() {
        SimLogger.printLine("Initializing GPU Load Generator Model...");
        
        // Get task types from applications.xml
        TaskProperty[] taskTypeArray = SimSettings.getInstance().getTaskLookUpTable();
        
        for (int taskIndex = 0; taskIndex < taskTypeArray.length; taskIndex++) {
            taskList[taskIndex] = new ArrayList<>();
            
            TaskProperty taskProperty = taskTypeArray[taskIndex];
            double poissonMean = taskProperty.getPoissonInterarrivalDelay();
            
            // Calculate number of active devices for this task type
            int numberOfActiveDevices = (int) Math.ceil(
                numberOfMobileDevices * taskProperty.getUsagePercentage() / 100.0
            );
            
            SimLogger.printLine("  Task Type " + taskIndex + ": " + 
                              taskProperty.getTaskName() + 
                              " (" + numberOfActiveDevices + " active devices)");
            
            // Generate tasks for each active device
            for (int deviceId = 0; deviceId < numberOfActiveDevices; deviceId++) {
                // Create exponential distribution for Poisson arrivals
                ExponentialDistribution expDist = new ExponentialDistribution(poissonMean);
                
                double taskStartTime = expDist.sample();
                
                // Generate tasks throughout simulation
                while (taskStartTime < simulationTime) {
                    taskList[taskIndex].add(new GpuTaskSchedule(
                        deviceId,
                        taskStartTime,
                        taskIndex
                    ));
                    
                    taskStartTime += expDist.sample();
                }
            }
            
            SimLogger.printLine("    Generated " + taskList[taskIndex].size() + 
                              " GPU tasks for this type");
        }
    }
    
    /**
     * Returns the next task to be submitted at current simulation time.
     * 
     * @param currentTime Current simulation time in seconds
     * @return GpuTask to be submitted, or null if no task scheduled
     */
    @Override
    public Task getTaskForNextCycle(double currentTime) {
        TaskProperty[] taskTypeArray = SimSettings.getInstance().getTaskLookUpTable();
        
        // Check each task type for scheduled tasks
        for (int taskIndex = 0; taskIndex < taskTypeArray.length; taskIndex++) {
            if (taskList[taskIndex].isEmpty()) {
                continue;
            }
            
            // Get next scheduled task for this type
            GpuTaskSchedule schedule = taskList[taskIndex].get(0);
            
            // Check if it's time to submit this task
            if (schedule.startTime <= currentTime) {
                // Remove from schedule
                taskList[taskIndex].remove(0);
                
                // Create GpuTask with characteristics from applications.xml
                TaskProperty taskProperty = taskTypeArray[taskIndex];
                
                int taskId = SimManager.getInstance().getTaskIdCounter();
                
                GpuTask gpuTask = new GpuTask(
                    schedule.mobileDeviceId,  // Mobile device ID
                    taskId,                   // Task ID
                    taskIndex,                // Task type
                    taskProperty.getTaskLength(),      // CPU length (MI)
                    taskProperty.getRequiredCore(),    // Number of CPU cores
                    taskProperty.getInputFileSize(),   // Input file size
                    taskProperty.getOutputFileSize(),  // Output file size
                    taskProperty.getGpuLength(),       // GPU length (GFLOPs)
                    taskProperty.getGpuInputData(),    // GPU input data (MB)
                    taskProperty.getGpuOutputData(),   // GPU output data (MB)
                    taskProperty.getRequiredGpuMemory() // Required GPU memory (MB)
                );
                
                // Set submission location (from mobility model)
                gpuTask.setSubmittedLocation(
                    SimManager.getInstance().getMobilityModel().getLocation(
                        schedule.mobileDeviceId, 
                        currentTime
                    )
                );
                
                SimLogger.printLine(currentTime + ": GPU Task #" + taskId + 
                                  " (Type: " + taskProperty.getTaskName() + 
                                  ", Device: " + schedule.mobileDeviceId + 
                                  ", GPU: " + taskProperty.getGpuLength() + " GFLOPs)");
                
                return gpuTask;
            }
        }
        
        // No task scheduled at this time
        return null;
    }
}
