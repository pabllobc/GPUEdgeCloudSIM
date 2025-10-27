/*
 * Title:        GpuEdgeCloudSim - Main Simulation Application
 * 
 * Description:  
 * GpuSimulationMain is the entry point for GPU-enabled EdgeCloudSim simulations.
 * Demonstrates complete integration of GPU components with EdgeCloudSim framework.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2025, GpuEdgeCloudSim Project
 * Author:       Pabllo Borges Cardoso
 */

package edu.boun.edgecloudsim.applications.gpusim;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

import edu.boun.edgecloudsim.core.ScenarioFactory;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;

/**
 * Main entry point for GpuEdgeCloudSim simulations.
 * Follows EdgeCloudSim patterns for GPU-enabled edge computing scenarios.
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 */
public class GpuSimulationMain {
	
	/**
	 * Main method - entry point of the GPU simulation.
	 * 
	 * @param args Command line arguments:
	 *             [0] - Configuration file path
	 *             [1] - Edge devices file path  
	 *             [2] - Applications file path
	 *             [3] - Output folder path
	 *             [4] - Iteration number
	 */
	public static void main(String[] args) {
		// Disable console output of CloudSim library for cleaner logs
		Log.disable();
		
		// Enable console output and file output of this application
		SimLogger.enablePrintLog();
		
		// Initialize simulation parameters with default values
		int iterationNumber = 1;
		String configFile = "";
		String outputFolder = "";
		String edgeDevicesFile = "";
		String applicationsFile = "";
		
		// Parse command line arguments or use default configuration files
		if (args.length == 5){
			configFile = args[0];
			edgeDevicesFile = args[1];
			applicationsFile = args[2];
			outputFolder = args[3];
			iterationNumber = Integer.parseInt(args[4]);
		}
		else{
			SimLogger.printLine("========================================");
			SimLogger.printLine("GpuEdgeCloudSim v1.0");
			SimLogger.printLine("========================================");
			SimLogger.printLine("Simulation setting file, output folder and iteration number not provided!");
			SimLogger.printLine("Using default configuration files...");
			configFile = "scripts/gpusim/config/config.properties";
			applicationsFile = "scripts/gpusim/config/applications.xml";
			edgeDevicesFile = "scripts/gpusim/config/edge_devices.xml";
			outputFolder = "sim_results/gpusim/ite" + iterationNumber;
		}

		// Load settings from configuration file
		SimSettings SS = SimSettings.getInstance();
		if(SS.initialize(configFile, edgeDevicesFile, applicationsFile) == false){
			SimLogger.printLine("ERROR: Cannot initialize simulation settings!");
			SimLogger.printLine("Please check your configuration files.");
			System.exit(1);
		}
		
		// Enable file logging and prepare output directory if configured
		if(SS.getFileLoggingEnabled()){
			SimLogger.enableFileLog();
			SimUtils.cleanOutputFolder(outputFolder);
		}
		
		// Initialize date formatter and log simulation start time
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date SimulationStartDate = Calendar.getInstance().getTime();
		String now = df.format(SimulationStartDate);
		SimLogger.printLine("========================================");
		SimLogger.printLine("GPU Simulation Started");
		SimLogger.printLine("========================================");
		SimLogger.printLine("Start time: " + now);
		SimLogger.printLine("Iteration: " + iterationNumber);
		SimLogger.printLine("----------------------------------------------------------------------");

		// Triple nested loop to run all combinations of mobile devices, scenarios, and policies
		for(int j=SS.getMinNumOfMobileDev(); j<=SS.getMaxNumOfMobileDev(); j+=SS.getMobileDevCounterSize())
		{
			for(int k=0; k<SS.getSimulationScenarios().length; k++)
			{
				for(int i=0; i<SS.getOrchestratorPolicies().length; i++)
				{
					// Get current simulation scenario and orchestrator policy
					String simScenario = SS.getSimulationScenarios()[k];
					String orchestratorPolicy = SS.getOrchestratorPolicies()[i];
					Date ScenarioStartDate = Calendar.getInstance().getTime();
					now = df.format(ScenarioStartDate);
					
					// Log scenario details and parameters
					SimLogger.printLine("┌────────────────────────────────────────────────────────────┐");
					SimLogger.printLine("│ Scenario Configuration");
					SimLogger.printLine("├────────────────────────────────────────────────────────────┤");
					SimLogger.printLine("│ Scenario:         " + simScenario);
					SimLogger.printLine("│ Policy:           " + orchestratorPolicy);
					SimLogger.printLine("│ Iteration:        " + iterationNumber);
					SimLogger.printLine("│ Devices:          " + j);
					SimLogger.printLine("│ Duration:         " + SS.getSimulationTime()/60 + " minutes");
					SimLogger.printLine("│ Start time:       " + now);
					SimLogger.printLine("└────────────────────────────────────────────────────────────┘");
					
					SimLogger.getInstance().simStarted(outputFolder,"SIMRESULT_" + simScenario + "_"  + orchestratorPolicy + "_" + j + "DEVICES");
					
					try
					{
						// Initialize the CloudSim package - must be called before creating any entities
						int num_user = 2;   // Number of grid users for CloudSim
						Calendar calendar = Calendar.getInstance();
						boolean trace_flag = false;  // Disable trace events for performance
				
						// Initialize the CloudSim library with simulation parameters
						CloudSim.init(num_user, calendar, trace_flag, 0.01);
						
						// Create GpuEdgeCloudSim scenario factory with current parameters
						ScenarioFactory gpuFactory = new GpuScenarioFactory(j, SS.getSimulationTime(), orchestratorPolicy, simScenario);
						
						// Create EdgeCloudSim simulation manager
						SimManager manager = new SimManager(gpuFactory, j, simScenario, orchestratorPolicy);
						
						// Execute the simulation
						SimLogger.printLine("Starting GPU-enabled simulation...");
						manager.startSimulation();
						SimLogger.printLine("✓ Simulation completed successfully");
					}
					catch (Exception e)
					{
						SimLogger.printLine("✗ ERROR: The simulation has been terminated due to an unexpected error");
						e.printStackTrace();
						System.exit(1);
					}
					
					// Log scenario completion time and duration
					Date ScenarioEndDate = Calendar.getInstance().getTime();
					now = df.format(ScenarioEndDate);
					SimLogger.printLine("Scenario finished at " + now);
					SimLogger.printLine("Duration: " + SimUtils.getTimeDifference(ScenarioStartDate, ScenarioEndDate));
					SimLogger.printLine("----------------------------------------------------------------------");
				} // End of orchestrator policies loop
			} // End of simulation scenarios loop
		} // End of mobile devices loop

		// Log total simulation completion time
		Date SimulationEndDate = Calendar.getInstance().getTime();
		now = df.format(SimulationEndDate);
		SimLogger.printLine("========================================");
		SimLogger.printLine("GPU Simulation Finished");
		SimLogger.printLine("========================================");
		SimLogger.printLine("End time: " + now);
		SimLogger.printLine("Total duration: " + SimUtils.getTimeDifference(SimulationStartDate, SimulationEndDate));
		SimLogger.printLine("Results saved to: " + outputFolder);
	}
}
