
/*
 * Title:        GpuEdgeCloudSim - GpuProvisionerSimple
 * 
 * Description:  
 * GpuProvisionerSimple implements a simple exclusive GPU allocation policy
 * where each GPU is allocated to at most one VM at a time (1 GPU : 1 VM).
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2025, GpuEdgeCloudSim Project
 * Author:       Pabllo Borges Cardoso
 */

package edu.boun.edgecloudsim.edge_server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.boun.edgecloudsim.utils.SimLogger;

/**
 * Simple implementation of GPU provisioning with exclusive allocation.
 * 
 * This provisioner implements a straightforward 1:1 GPU-to-VM allocation model
 * where each GPU can be assigned to at most one VM at any given time.
 * This is the default mode for NVIDIA GPUs without MIG (Multi-Instance GPU).
 * 
 * Allocation strategy:
 * - First-fit: Returns the first available GPU
 * - Exclusive mode: No GPU sharing between VMs
 * - Simple tracking: Uses map for O(1) VM-to-GPU lookup
 * 
 * Key features:
 * - Fast allocation/deallocation (O(1) for map operations)
 * - Memory-aware GPU selection
 * - Utilization monitoring across all GPUs
 * - Thread-safe operations (future consideration)
 * 
 * Limitations:
 * - No GPU sharing (to be addressed in future versions with time-slicing)
 * - No QoS considerations (all VMs treated equally)
 * - No GPU migration support (future work)
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 * @since GpuEdgeCloudSim v1.0
 */
public class GpuProvisionerSimple implements GpuProvisioner {
    
    /** Lista de GPUs gerenciadas por este provisioner */
    private List<Gpu> gpuList;
    
    /** Mapa para rastreamento rápido de alocações VM -> GPU */
    private Map<Integer, Gpu> vmGpuMap;
    
    /**
     * Construtor que inicializa o provisioner com uma lista de GPUs.
     * 
     * @param gpuList Lista de GPUs a serem gerenciadas (não deve ser null)
     * @throws IllegalArgumentException se gpuList for null
     */
    public GpuProvisionerSimple(List<Gpu> gpuList) {
        if (gpuList == null) {
            throw new IllegalArgumentException("GPU list cannot be null");
        }
        
        this.gpuList = new ArrayList<>(gpuList);
        this.vmGpuMap = new HashMap<>();
        
        SimLogger.printLine("GpuProvisionerSimple initialized with " + gpuList.size() + " GPUs");
    }
    
    /**
     * Aloca uma GPU específica para uma VM (modo exclusivo).
     * 
     * Verifica se a GPU está disponível antes de alocar. Se a VM já possui
     * uma GPU alocada, desaloca a anterior antes de alocar a nova.
     * 
     * @param vm VM que receberá a GPU
     * @param gpu GPU a ser alocada
     * @return true se a alocação foi bem-sucedida, false caso contrário
     */
    @Override
    public boolean allocateGpuForVm(GpuEdgeVM vm, Gpu gpu) {
        if (vm == null || gpu == null) {
            SimLogger.printLine("Error: Cannot allocate null VM or GPU");
            return false;
        }
        
        // Verifica se a GPU está disponível
        if (!gpu.isAvailable()) {
            SimLogger.printLine("Error: GPU " + gpu.getId() + " is already allocated to VM " + 
                              gpu.getAllocatedVm().getId());
            return false;
        }
        
        // Se a VM já tem uma GPU, desaloca primeiro
        if (vmGpuMap.containsKey(vm.getId())) {
            deallocateGpuForVm(vm);
        }
        
        // Aloca a GPU para a VM
        gpu.setAllocatedVm(vm);
        vm.setGpu(gpu);
        vmGpuMap.put(vm.getId(), gpu);
        
        SimLogger.printLine("GPU " + gpu.getId() + " allocated to VM " + vm.getId());
        return true;
    }
    
    /**
     * Desaloca a GPU de uma VM.
     * 
     * Reseta o estado da GPU e remove do mapa de alocações.
     * A GPU fica disponível para outras VMs.
     * 
     * @param vm VM da qual a GPU será desalocada
     */
    @Override
    public void deallocateGpuForVm(GpuEdgeVM vm) {
        if (vm == null) {
            SimLogger.printLine("Error: Cannot deallocate GPU from null VM");
            return;
        }
        
        Gpu gpu = vmGpuMap.get(vm.getId());
        if (gpu != null) {
            gpu.reset();
            vm.setGpu(null);
            vmGpuMap.remove(vm.getId());
            SimLogger.printLine("GPU " + gpu.getId() + " deallocated from VM " + vm.getId());
        }
    }
    
    /**
     * Verifica se há GPUs disponíveis.
     * 
     * @return true se há pelo menos uma GPU disponível
     */
    @Override
    public boolean hasAvailableGpu() {
        return gpuList.stream().anyMatch(Gpu::isAvailable);
    }
    
    /**
     * Retorna a primeira GPU disponível encontrada (first-fit).
     * 
     * @return GPU disponível ou null se nenhuma disponível
     */
    @Override
    public Gpu getAvailableGpu() {
        return gpuList.stream()
                     .filter(Gpu::isAvailable)
                     .findFirst()
                     .orElse(null);
    }
    
    /**
     * Retorna uma GPU disponível com memória suficiente.
     * 
     * Busca a primeira GPU que esteja disponível e tenha pelo menos
     * a quantidade de memória solicitada livre.
     * 
     * @param requiredMemory Memória mínima necessária em MB
     * @return GPU disponível com memória suficiente ou null
     */
    @Override
    public Gpu getAvailableGpuWithMemory(long requiredMemory) {
        return gpuList.stream()
                     .filter(gpu -> gpu.isAvailable() && gpu.getAvailableMemory() >= requiredMemory)
                     .findFirst()
                     .orElse(null);
    }
    
    /**
     * Retorna a lista completa de GPUs gerenciadas.
     * 
     * @return Lista imutável de GPUs
     */
    @Override
    public List<Gpu> getGpuList() {
        return new ArrayList<>(gpuList);
    }
    
    /**
     * Retorna a lista de GPUs disponíveis (não alocadas).
     * 
     * @return Lista de GPUs disponíveis
     */
    @Override
    public List<Gpu> getAvailableGpuList() {
        return gpuList.stream()
                     .filter(Gpu::isAvailable)
                     .collect(Collectors.toList());
    }
    
    /**
     * Retorna a lista de GPUs atualmente alocadas.
     * 
     * @return Lista de GPUs alocadas
     */
    @Override
    public List<Gpu> getAllocatedGpuList() {
        return gpuList.stream()
                     .filter(gpu -> !gpu.isAvailable())
                     .collect(Collectors.toList());
    }
    
    /**
     * Retorna a GPU alocada para uma VM específica.
     * 
     * @param vmId ID da VM
     * @return GPU alocada para a VM ou null se não houver
     */
    public Gpu getGpuForVm(int vmId) {
        return vmGpuMap.get(vmId);
    }
    
    /**
     * Calcula a utilização média de todas as GPUs gerenciadas.
     * 
     * @return Utilização média em percentual (0-100%)
     */
    public double getAverageUtilization() {
        if (gpuList.isEmpty()) {
            return 0.0;
        }
        
        double totalUtilization = gpuList.stream()
                                        .mapToDouble(Gpu::getUtilization)
                                        .sum();
        
        return totalUtilization / gpuList.size();
    }
}
