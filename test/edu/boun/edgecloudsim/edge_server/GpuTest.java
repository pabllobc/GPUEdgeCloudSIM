
package edu.boun.edgecloudsim.edge_server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe Gpu.
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 */
public class GpuTest {
    
    private Gpu gpu;
    
    @BeforeEach
    public void setUp() {
        // Cria GPU para testes: NVIDIA T4 com 8100 GFLOPS
        gpu = new Gpu(0, "NVIDIA_T4", 2560, 8100.0, 16384, 320.0);
    }
    
    @Test
    public void testGpuCreation() {
        assertNotNull(gpu);
        assertEquals(0, gpu.getId());
        assertEquals("NVIDIA_T4", gpu.getGpuType());
        assertEquals(2560, gpu.getCudaCores());
        assertEquals(8100.0, gpu.getGflops());
        assertEquals(16384, gpu.getGpuMemory());
        assertEquals(320.0, gpu.getMemoryBandwidth());
    }
    
    @Test
    public void testGpuInitialState() {
        assertTrue(gpu.isAvailable());
        assertEquals(0.0, gpu.getUtilization());
        assertEquals(0, gpu.getUsedMemory());
        assertEquals(16384, gpu.getAvailableMemory());
        assertNull(gpu.getAllocatedVm());
    }
    
    @Test
    public void testMemoryAllocation() {
        // Aloca 8GB de memória
        boolean allocated = gpu.allocateMemory(8192);
        assertTrue(allocated);
        assertEquals(8192, gpu.getUsedMemory());
        assertEquals(8192, gpu.getAvailableMemory());
    }
    
    @Test
    public void testMemoryAllocationExceeded() {
        // Tenta alocar mais memória que disponível
        boolean allocated = gpu.allocateMemory(20000);
        assertFalse(allocated);
        assertEquals(0, gpu.getUsedMemory()); // Memória não deve ser alterada
    }
    
    @Test
    public void testMemoryDeallocation() {
        gpu.allocateMemory(8192);
        assertEquals(8192, gpu.getUsedMemory());
        
        boolean deallocated = gpu.deallocateMemory(4096);
        assertTrue(deallocated);
        assertEquals(4096, gpu.getUsedMemory());
    }
    
    @Test
    public void testExecutionTimeCalculation() {
        // Tarefa de 8100 GFLOPS deve levar 1 segundo em GPU de 8100 GFLOPS
        double executionTime = gpu.calculateExecutionTime(8100);
        assertEquals(1.0, executionTime, 0.001);
    }
    
    @Test
    public void testDataTransferTimeCalculation() {
        // Transferir 320 MB (0.3125 GB) a 320 GB/s deve levar ~0.001 segundos
        double transferTime = gpu.calculateDataTransferTime(320);
        assertTrue(transferTime > 0);
    }
    
    @Test
    public void testGpuReset() {
        gpu.allocateMemory(8192);
        gpu.setUtilization(75.0);
        
        gpu.reset();
        
        assertEquals(0, gpu.getUsedMemory());
        assertEquals(0.0, gpu.getUtilization());
        assertNull(gpu.getAllocatedVm());
        assertTrue(gpu.isAvailable());
    }
    
    @Test
    public void testUtilizationBounds() {
        // Testa limites de utilização
        gpu.setUtilization(-10.0);
        assertEquals(0.0, gpu.getUtilization());
        
        gpu.setUtilization(150.0);
        assertEquals(100.0, gpu.getUtilization());
        
        gpu.setUtilization(50.0);
        assertEquals(50.0, gpu.getUtilization());
    }
}
