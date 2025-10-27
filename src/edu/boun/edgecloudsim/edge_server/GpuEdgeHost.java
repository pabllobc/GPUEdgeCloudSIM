
/*
 * Title:        GpuEdgeCloudSim - GpuEdgeHost
 * 
 * Description:  
 * GpuEdgeHost extends EdgeHost to include GPU hardware resources,
 * enabling GPU-accelerated edge computing scenarios.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2025, GpuEdgeCloudSim Project
 * Author:       Pabllo Borges Cardoso
 */

package edu.boun.edgecloudsim.edge_server;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import edu.boun.edgecloudsim.utils.SimLogger;

/**
 * Extended EdgeHost with GPU hardware support for edge computing.
 * 
 * This class extends EdgeHost to provide GPU resources alongside traditional
 * CPU, RAM, and bandwidth resources. It manages a pool of physical GPUs and
 * uses a GpuProvisioner to allocate GPUs to VMs requiring GPU acceleration.
 * 
 * Architecture:
 * - Inherits CPU/RAM/BW provisioning from Host/EdgeHost
 * - Adds GPU pool management and provisioning
 * - Integrates GPU allocation with VM lifecycle (create/destroy)
 * - Tracks combined CPU+GPU utilization
 * 
 * Resource model:
 * - N CPU cores (Pe list from Host)
 * - M GPUs (managed by GpuProvisioner)
 * - RAM, bandwidth, storage (from Host)
 * - Location information (from EdgeHost)
 * 
 * Key features:
 * - Multi-GPU host support
 * - GPU-aware VM creation
 * - Memory-aware GPU selection
 * - Combined CPU+GPU utilization tracking
 * - Automatic GPU deallocation on VM destruction
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 * @since GpuEdgeCloudSim v1.0
 */
public class GpuEdgeHost extends EdgeHost {
    
    /** Lista de GPUs físicas disponíveis neste host */
    private List<Gpu> gpuList;
    
    /** Provisionador responsável por alocar GPUs para VMs */
    private GpuProvisioner gpuProvisioner;
    
    /**
     * Construtor completo para GpuEdgeHost com recursos CPU e GPU.
     * 
     * @param id Identificador único do host
     * @param ramProvisioner Provisionador de memória RAM
     * @param bwProvisioner Provisionador de largura de banda
     * @param storage Capacidade de armazenamento em MB
     * @param peList Lista de elementos de processamento (CPU cores)
     * @param vmScheduler Escalonador de VMs
     * @param gpuList Lista de GPUs disponíveis
     * @param gpuProvisioner Provisionador de GPUs
     */
    public GpuEdgeHost(int id, RamProvisioner ramProvisioner,
                       BwProvisioner bwProvisioner, long storage,
                       List<? extends Pe> peList, VmScheduler vmScheduler,
                       List<Gpu> gpuList, GpuProvisioner gpuProvisioner) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
        
        this.gpuList = new ArrayList<>(gpuList);
        this.gpuProvisioner = gpuProvisioner;
        
        // Associa cada GPU a este host
        for (Gpu gpu : this.gpuList) {
            gpu.setHost(this);
        }
        
        SimLogger.printLine("GpuEdgeHost " + id + " created with " + gpuList.size() + " GPUs");
    }
    
    // ==================== GETTERS ====================
    
    /**
     * Retorna a lista de GPUs deste host.
     * @return Lista imutável de GPUs
     */
    public List<Gpu> getGpuList() {
        return new ArrayList<>(gpuList);
    }
    
    /**
     * Retorna o provisionador de GPU.
     * @return Provisionador de GPU
     */
    public GpuProvisioner getGpuProvisioner() {
        return gpuProvisioner;
    }
    
    /**
     * Retorna o número total de GPUs neste host.
     * @return Número de GPUs
     */
    public int getNumberOfGpus() {
        return gpuList.size();
    }
    
    /**
     * Retorna uma GPU específica pelo ID.
     * 
     * @param gpuId ID da GPU
     * @return GPU correspondente ou null se não encontrada
     */
    public Gpu getGpu(int gpuId) {
        return gpuList.stream()
                     .filter(gpu -> gpu.getId() == gpuId)
                     .findFirst()
                     .orElse(null);
    }
    
    // ==================== MÉTODOS DE ALOCAÇÃO GPU ====================
    
    /**
     * Aloca uma GPU específica para uma VM.
     * Delega ao GpuProvisioner para realizar a alocação.
     * 
     * @param vm VM que receberá a GPU
     * @param gpu GPU a ser alocada
     * @return true se a alocação foi bem-sucedida, false caso contrário
     */
    public boolean allocateGpuForVm(GpuEdgeVM vm, Gpu gpu) {
        return gpuProvisioner.allocateGpuForVm(vm, gpu);
    }
    
    /**
     * Desaloca a GPU de uma VM.
     * Libera a GPU para outras VMs.
     * 
     * @param vm VM da qual a GPU será desalocada
     */
    public void deallocateGpuForVm(GpuEdgeVM vm) {
        gpuProvisioner.deallocateGpuForVm(vm);
    }
    
    /**
     * Verifica se há GPUs disponíveis neste host.
     * 
     * @return true se há pelo menos uma GPU disponível
     */
    public boolean hasAvailableGpu() {
        return gpuProvisioner.hasAvailableGpu();
    }
    
    /**
     * Retorna uma GPU disponível do pool.
     * 
     * @return GPU disponível ou null se nenhuma disponível
     */
    public Gpu getAvailableGpu() {
        return gpuProvisioner.getAvailableGpu();
    }
    
    /**
     * Retorna uma GPU disponível com memória suficiente.
     * 
     * @param requiredMemory Memória mínima necessária em MB
     * @return GPU disponível com memória suficiente ou null
     */
    public Gpu getAvailableGpuWithMemory(long requiredMemory) {
        return gpuProvisioner.getAvailableGpuWithMemory(requiredMemory);
    }
    
    // ==================== MÉTODOS DE MÉTRICAS ====================
    
    /**
     * Calcula a utilização média de CPU e GPU combinadas.
     * Retorna a média ponderada da utilização de CPU e GPU.
     * 
     * @return Utilização média em percentual (0-100%)
     */
    public double getAvgUtilization() {
        // Calculate CPU utilization based on VMs
        double cpuUtilization = 0.0;
        if (!getVmList().isEmpty()) {
            double totalMips = getTotalMips();
            double allocatedMips = 0.0;
            for (Vm vm : getVmList()) {
                allocatedMips += vm.getCurrentRequestedTotalMips();
            }
            cpuUtilization = (allocatedMips / totalMips) * 100.0;
        }
        
        double gpuUtilization = getAvgGpuUtilization();
        
        // Se não há GPUs, retorna apenas utilização de CPU
        if (gpuList.isEmpty()) {
            return cpuUtilization;
        }
        
        // Média ponderada: (CPU + GPU) / 2
        return (cpuUtilization + gpuUtilization) / 2.0;
    }
    
    /**
     * Calcula a utilização média das GPUs.
     * 
     * @return Utilização média das GPUs em percentual (0-100%)
     */
    public double getAvgGpuUtilization() {
        if (gpuList.isEmpty()) {
            return 0.0;
        }
        
        double totalUtilization = gpuList.stream()
                                        .mapToDouble(Gpu::getUtilization)
                                        .sum();
        
        return totalUtilization / gpuList.size();
    }
    
    /**
     * Retorna a memória GPU total disponível neste host.
     * 
     * @return Memória GPU total em MB
     */
    public long getTotalGpuMemory() {
        return gpuList.stream()
                     .mapToLong(Gpu::getGpuMemory)
                     .sum();
    }
    
    /**
     * Retorna a memória GPU disponível (não alocada) neste host.
     * 
     * @return Memória GPU disponível em MB
     */
    public long getAvailableGpuMemory() {
        return gpuList.stream()
                     .mapToLong(Gpu::getAvailableMemory)
                     .sum();
    }
    
    /**
     * Retorna a capacidade computacional total de GPU em GFLOPS.
     * 
     * @return Capacidade total em GFLOPS
     */
    public double getTotalGpuGflops() {
        return gpuList.stream()
                     .mapToDouble(Gpu::getGflops)
                     .sum();
    }
    
    // ==================== MÉTODOS DE CRIAÇÃO DE VM (Override) ====================
    
    /**
     * Cria uma VM neste host (estende comportamento do Host).
     * Se a VM é GpuEdgeVM e requer GPU, tenta alocar uma antes de criar a VM.
     * 
     * @param vm VM a ser criada
     * @return true se a VM foi criada com sucesso, false caso contrário
     */
    @Override
    public boolean vmCreate(Vm vm) {
        // Tenta criar a VM usando a lógica padrão do Host
        boolean created = super.vmCreate(vm);
        
        if (!created) {
            return false;
        }
        
        // Se é GpuEdgeVM e requer GPU, aloca uma
        if (vm instanceof GpuEdgeVM) {
            GpuEdgeVM gpuVm = (GpuEdgeVM) vm;
            
            if (gpuVm.requiresGpu()) {
                // Busca GPU disponível com memória suficiente (se especificado)
                Gpu availableGpu = getAvailableGpu();
                
                if (availableGpu == null) {
                    SimLogger.printLine("Warning: No GPU available for VM " + vm.getId() + 
                                      " on host " + getId());
                    // VM foi criada mas sem GPU - pode ser aceitável dependendo do cenário
                    return true;
                }
                
                // Aloca a GPU para a VM
                boolean gpuAllocated = allocateGpuForVm(gpuVm, availableGpu);
                
                if (!gpuAllocated) {
                    SimLogger.printLine("Warning: Failed to allocate GPU for VM " + vm.getId() + 
                                      " on host " + getId());
                }
            }
        }
        
        return true;
    }
    
    /**
     * Destrói uma VM neste host (estende comportamento do Host).
     * Se a VM é GpuEdgeVM com GPU alocada, libera a GPU primeiro.
     * 
     * @param vm VM a ser destruída
     */
    @Override
    public void vmDestroy(Vm vm) {
        // Se é GpuEdgeVM com GPU, libera a GPU
        if (vm instanceof GpuEdgeVM) {
            GpuEdgeVM gpuVm = (GpuEdgeVM) vm;
            
            if (gpuVm.hasGpu()) {
                deallocateGpuForVm(gpuVm);
                SimLogger.printLine("GPU deallocated from VM " + vm.getId() + " on host " + getId());
            }
        }
        
        // Destrói a VM usando a lógica padrão do Host
        super.vmDestroy(vm);
    }
    
    /**
     * Retorna representação em string do GpuEdgeHost.
     * @return String com ID, CPUs, GPUs e localização
     */
    @Override
    public String toString() {
        return String.format("GpuEdgeHost[id=%d, cpus=%d, gpus=%d, location=%s]",
                           getId(), getNumberOfPes(), getNumberOfGpus(), 
                           getLocation() != null ? getLocation().toString() : "unknown");
    }
}
