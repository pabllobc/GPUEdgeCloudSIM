
package edu.boun.edgecloudsim.edge_client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe GpuTask.
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 */
public class GpuTaskTest {
    
    private GpuTask gpuTask;
    
    @BeforeEach
    public void setUp() {
        // Cria tarefa com 10000 MI de CPU e 5000 GFLOPS de GPU
        gpuTask = new GpuTask(
            0,           // cloudletId
            0,           // taskType
            10000,       // cloudletLength (CPU)
            2,           // pesNumber
            1024,        // cloudletFileSize
            512,         // cloudletOutputSize
            5000,        // gpuLength
            100,         // gpuInputData
            50,          // gpuOutputData
            2048         // requiredGpuMemory
        );
    }
    
    @Test
    public void testGpuTaskCreation() {
        assertNotNull(gpuTask);
        assertEquals(5000, gpuTask.getGpuLength());
        assertEquals(100, gpuTask.getGpuInputData());
        assertEquals(50, gpuTask.getGpuOutputData());
        assertEquals(2048, gpuTask.getRequiredGpuMemory());
    }
    
    @Test
    public void testRequiresGpu() {
        assertTrue(gpuTask.requiresGpu());
    }
    
    @Test
    public void testGpuIntensityCalculation() {
        // gpuIntensity = gpuLength / (cpuLength + gpuLength)
        // = 5000 / (10000 + 5000) = 0.333...
        double intensity = gpuTask.getGpuIntensity();
        assertEquals(0.333, intensity, 0.01);
    }
    
    @Test
    public void testHasEnoughGpuMemory() {
        assertTrue(gpuTask.hasEnoughGpuMemory(4096));
        assertFalse(gpuTask.hasEnoughGpuMemory(1024));
    }
    
    @Test
    public void testGpuMetrics() {
        gpuTask.setGpuStartTime(10.0);
        gpuTask.setGpuFinishTime(15.0);
        gpuTask.setGpuExecutionTime(4.0);
        gpuTask.setGpuDataTransferTime(0.5);
        gpuTask.setGpuDataBackTime(0.3);
        gpuTask.setActualGpuUtilization(85.0);
        gpuTask.setExecutedGpuId(0);
        
        assertEquals(10.0, gpuTask.getGpuStartTime());
        assertEquals(15.0, gpuTask.getGpuFinishTime());
        assertEquals(4.0, gpuTask.getGpuExecutionTime());
        assertEquals(0.5, gpuTask.getGpuDataTransferTime());
        assertEquals(0.3, gpuTask.getGpuDataBackTime());
        assertEquals(85.0, gpuTask.getActualGpuUtilization());
        assertEquals(0, gpuTask.getExecutedGpuId());
    }
    
    @Test
    public void testTotalGpuTime() {
        gpuTask.setGpuDataTransferTime(0.5);
        gpuTask.setGpuExecutionTime(4.0);
        gpuTask.setGpuDataBackTime(0.3);
        
        double totalTime = gpuTask.getTotalGpuTime();
        assertEquals(4.8, totalTime, 0.001);
    }
    
    @Test
    public void testExpectedGpuUtilization() {
        assertEquals(100.0, gpuTask.getExpectedGpuUtilization());
        
        gpuTask.setExpectedGpuUtilization(75.0);
        assertEquals(75.0, gpuTask.getExpectedGpuUtilization());
    }
}
