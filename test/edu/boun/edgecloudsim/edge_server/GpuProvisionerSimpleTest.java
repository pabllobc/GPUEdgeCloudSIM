
package edu.boun.edgecloudsim.edge_server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Testes unitários para a classe GpuProvisionerSimple.
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 */
public class GpuProvisionerSimpleTest {
    
    private GpuProvisionerSimple provisioner;
    private List<Gpu> gpuList;
    private Gpu gpu1;
    private Gpu gpu2;
    
    @BeforeEach
    public void setUp() {
        gpuList = new ArrayList<>();
        gpu1 = new Gpu(0, "NVIDIA_T4", 2560, 8100.0, 16384, 320.0);
        gpu2 = new Gpu(1, "NVIDIA_A100", 6912, 19500.0, 40960, 1555.0);
        gpuList.add(gpu1);
        gpuList.add(gpu2);
        
        provisioner = new GpuProvisionerSimple(gpuList);
    }
    
    @Test
    public void testProvisionerCreation() {
        assertNotNull(provisioner);
        assertEquals(2, provisioner.getGpuList().size());
    }
    
    @Test
    public void testHasAvailableGpu() {
        assertTrue(provisioner.hasAvailableGpu());
    }
    
    @Test
    public void testGetAvailableGpu() {
        Gpu availableGpu = provisioner.getAvailableGpu();
        assertNotNull(availableGpu);
        assertTrue(availableGpu.isAvailable());
    }
    
    @Test
    public void testGetAvailableGpuWithMemory() {
        // Busca GPU com pelo menos 32GB
        Gpu gpu = provisioner.getAvailableGpuWithMemory(32768);
        assertNotNull(gpu);
        assertEquals(gpu2.getId(), gpu.getId()); // Deve retornar A100
    }
    
    @Test
    public void testGetAvailableGpuWithInsufficientMemory() {
        // Busca GPU com 100GB (não existe)
        Gpu gpu = provisioner.getAvailableGpuWithMemory(102400);
        assertNull(gpu);
    }
    
    @Test
    public void testGetAvailableGpuList() {
        List<Gpu> available = provisioner.getAvailableGpuList();
        assertEquals(2, available.size());
    }
    
    @Test
    public void testGetAllocatedGpuList() {
        List<Gpu> allocated = provisioner.getAllocatedGpuList();
        assertEquals(0, allocated.size());
    }
    
    @Test
    public void testAverageUtilization() {
        gpu1.setUtilization(50.0);
        gpu2.setUtilization(75.0);
        
        double avgUtil = provisioner.getAverageUtilization();
        assertEquals(62.5, avgUtil, 0.001);
    }
}
