
/*
 * Title:        GpuEdgeCloudSim - GpuEdgeVM
 * 
 * Description:  
 * GpuEdgeVM extends EdgeVM to include GPU allocation and GPU task scheduling,
 * enabling heterogeneous CPU+GPU workloads in edge computing.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2025, GpuEdgeCloudSim Project
 * Author:       Pabllo Borges Cardoso
 */

package edu.boun.edgecloudsim.edge_server;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.CloudletScheduler;

import edu.boun.edgecloudsim.edge_client.GpuTask;
import edu.boun.edgecloudsim.utils.SimLogger;

/**
 * Extended EdgeVM with GPU support for heterogeneous edge computing.
 * 
 * This class extends EdgeVM to support GPU-accelerated workloads at the edge.
 * It manages GPU allocation, GPU task scheduling, and tracks GPU utilization
 * alongside traditional CPU metrics.
 * 
 * Architecture:
 * - Inherits CPU/RAM/BW resources from Vm/EdgeVM
 * - Adds GPU resource allocation (0 or 1 GPU in exclusive mode)
 * - Integrates GPU task scheduler for GPU workload management
 * - Supports both GPU-requiring and non-GPU VMs
 * 
 * GPU allocation modes:
 * - EXCLUSIVE: 1 GPU dedicated to this VM (default for v1.0)
 * - SHARED: GPU time-sliced among multiple VMs (future)
 * 
 * Key features:
 * - Optional GPU requirement flag
 * - GPU task submission and execution
 * - Combined CPU+GPU utilization tracking
 * - Dynamic GPU reconfiguration support
 * - Memory-aware GPU task management
 * 
 * Use cases:
 * - Deep learning inference VMs at the edge
 * - Computer vision processing VMs
 * - Scientific computing VMs
 * - Mixed CPU-only and GPU-enabled VMs in same infrastructure
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 * @since GpuEdgeCloudSim v1.0
 */
public class GpuEdgeVM extends EdgeVM {
    
    /** GPU alocada para esta VM (null se não tiver GPU) */
    private Gpu allocatedGpu;
    
    /** Escalonador de tarefas GPU para esta VM */
    private GpuCloudletScheduler gpuCloudletScheduler;
    
    /** Indica se esta VM requer GPU */
    private boolean requiresGpu;
    
    /** Modo de alocação GPU (EXCLUSIVE ou SHARED) */
    private GpuAllocationMode gpuAllocationMode;
    
    /**
     * Modos de alocação de GPU para VMs.
     */
    public enum GpuAllocationMode {
        /**
         * Modo exclusivo: 1 GPU : 1 VM (padrão v1.0)
         */
        EXCLUSIVE,
        
        /**
         * Modo compartilhado: 1 GPU : N VMs (futuro)
         */
        SHARED
    }
    
    /**
     * Construtor completo para GpuEdgeVM com suporte a GPU.
     * 
     * @param id Identificador único da VM
     * @param userId ID do usuário/broker
     * @param mips Capacidade de processamento em MIPS
     * @param numberOfPes Número de cores de CPU
     * @param ram Memória RAM em MB
     * @param bw Largura de banda em Mbps
     * @param size Armazenamento em MB
     * @param vmm Monitor de máquina virtual
     * @param cloudletScheduler Escalonador de tarefas CPU
     * @param gpuCloudletScheduler Escalonador de tarefas GPU
     * @param requiresGpu Indica se esta VM requer GPU
     */
    public GpuEdgeVM(int id, int userId, double mips, int numberOfPes,
                     int ram, long bw, long size, String vmm,
                     CloudletScheduler cloudletScheduler,
                     GpuCloudletScheduler gpuCloudletScheduler,
                     boolean requiresGpu) {
        super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
        
        this.gpuCloudletScheduler = gpuCloudletScheduler;
        this.requiresGpu = requiresGpu;
        this.allocatedGpu = null;
        this.gpuAllocationMode = GpuAllocationMode.EXCLUSIVE; // Padrão v1.0
        
        SimLogger.printLine("GpuEdgeVM " + id + " created (requiresGpu=" + requiresGpu + ")");
    }
    
    /**
     * Construtor alternativo sem escalonador GPU (para VMs sem GPU).
     * 
     * @param id Identificador único da VM
     * @param userId ID do usuário/broker
     * @param mips Capacidade de processamento em MIPS
     * @param numberOfPes Número de cores de CPU
     * @param ram Memória RAM em MB
     * @param bw Largura de banda em Mbps
     * @param size Armazenamento em MB
     * @param vmm Monitor de máquina virtual
     * @param cloudletScheduler Escalonador de tarefas CPU
     */
    public GpuEdgeVM(int id, int userId, double mips, int numberOfPes,
                     int ram, long bw, long size, String vmm,
                     CloudletScheduler cloudletScheduler) {
        this(id, userId, mips, numberOfPes, ram, bw, size, vmm, 
             cloudletScheduler, null, false);
    }
    
    // ==================== GETTERS E SETTERS GPU ====================
    
    /**
     * Define a GPU alocada para esta VM.
     * Também inicializa o escalonador GPU se disponível.
     * 
     * @param gpu GPU a ser alocada
     */
    public void setGpu(Gpu gpu) {
        this.allocatedGpu = gpu;
        
        // Inicializa o escalonador com a GPU
        if (gpu != null && gpuCloudletScheduler != null) {
            gpuCloudletScheduler.initialize(gpu);
        }
    }
    
    /**
     * Retorna a GPU alocada para esta VM.
     * @return GPU alocada ou null
     */
    public Gpu getGpu() {
        return allocatedGpu;
    }
    
    /**
     * Verifica se esta VM possui GPU alocada.
     * @return true se tem GPU alocada, false caso contrário
     */
    public boolean hasGpu() {
        return allocatedGpu != null;
    }
    
    /**
     * Verifica se esta VM requer GPU.
     * @return true se requer GPU, false caso contrário
     */
    public boolean requiresGpu() {
        return requiresGpu;
    }
    
    /**
     * Define se esta VM requer GPU.
     * @param requiresGpu true se requer, false caso contrário
     */
    public void setRequiresGpu(boolean requiresGpu) {
        this.requiresGpu = requiresGpu;
    }
    
    /**
     * Retorna o escalonador de tarefas GPU.
     * @return Escalonador GPU ou null
     */
    public GpuCloudletScheduler getGpuCloudletScheduler() {
        return gpuCloudletScheduler;
    }
    
    /**
     * Define o escalonador de tarefas GPU.
     * Se já existe GPU alocada, inicializa o escalonador com ela.
     * 
     * @param scheduler Escalonador GPU
     */
    public void setGpuCloudletScheduler(GpuCloudletScheduler scheduler) {
        this.gpuCloudletScheduler = scheduler;
        
        // Se já tem GPU, inicializa o escalonador
        if (allocatedGpu != null && scheduler != null) {
            scheduler.initialize(allocatedGpu);
        }
    }
    
    /**
     * Retorna o modo de alocação GPU.
     * @return Modo de alocação (EXCLUSIVE ou SHARED)
     */
    public GpuAllocationMode getGpuAllocationMode() {
        return gpuAllocationMode;
    }
    
    /**
     * Define o modo de alocação GPU.
     * @param mode Modo de alocação
     */
    public void setGpuAllocationMode(GpuAllocationMode mode) {
        this.gpuAllocationMode = mode;
    }
    
    // ==================== MÉTODOS DE EXECUÇÃO GPU ====================
    
    /**
     * Submete uma tarefa GPU para execução nesta VM.
     * Delega ao GpuCloudletScheduler se disponível.
     * 
     * @param gpuTask Tarefa GPU a ser executada
     * @return true se a tarefa foi aceita, false caso contrário
     */
    public boolean submitGpuTask(GpuTask gpuTask) {
        if (gpuCloudletScheduler == null) {
            SimLogger.printLine("Error: VM " + getId() + " has no GPU scheduler");
            return false;
        }
        
        if (allocatedGpu == null) {
            SimLogger.printLine("Error: VM " + getId() + " has no GPU allocated");
            return false;
        }
        
        return gpuCloudletScheduler.submitGpuTask(gpuTask);
    }
    
    /**
     * Remove uma tarefa GPU da fila de execução.
     * 
     * @param gpuTask Tarefa GPU a ser removida
     * @return true se a tarefa foi removida, false caso contrário
     */
    public boolean removeGpuTask(GpuTask gpuTask) {
        if (gpuCloudletScheduler == null) {
            return false;
        }
        
        return gpuCloudletScheduler.removeGpuTask(gpuTask);
    }
    
    /**
     * Retorna a lista de tarefas GPU em execução.
     * 
     * @return Lista de GpuTasks em execução ou lista vazia
     */
    public List<GpuTask> getRunningGpuTasks() {
        if (gpuCloudletScheduler == null) {
            return new ArrayList<>();
        }
        
        return gpuCloudletScheduler.getRunningGpuTasks();
    }
    
    /**
     * Retorna o número de tarefas GPU em execução.
     * 
     * @return Número de tarefas GPU ativas
     */
    public int getNumberOfRunningGpuTasks() {
        return getRunningGpuTasks().size();
    }
    
    // ==================== MÉTODOS DE MÉTRICAS GPU ====================
    
    /**
     * Calcula a utilização atual da GPU.
     * Retorna 0 se não tiver GPU alocada.
     * 
     * @return Utilização GPU em percentual (0-100%)
     */
    public double getGpuUtilization() {
        if (allocatedGpu == null) {
            return 0.0;
        }
        
        return allocatedGpu.getUtilization();
    }
    
    /**
     * Calcula a utilização combinada de CPU e GPU.
     * Média ponderada: (CPU + GPU) / 2
     * 
     * @return Utilização média (CPU + GPU) / 2
     */
    public double getCombinedUtilization() {
        double cpuUtil = getCurrentRequestedTotalMips() / (getMips() * getNumberOfPes()) * 100.0;
        double gpuUtil = getGpuUtilization();
        
        if (allocatedGpu == null) {
            return cpuUtil;
        }
        
        return (cpuUtil + gpuUtil) / 2.0;
    }
    
    /**
     * Retorna a memória GPU disponível nesta VM.
     * 
     * @return Memória GPU disponível em MB ou 0 se sem GPU
     */
    public long getAvailableGpuMemory() {
        if (allocatedGpu == null) {
            return 0;
        }
        
        return allocatedGpu.getAvailableMemory();
    }
    
    /**
     * Retorna a capacidade computacional GPU desta VM.
     * 
     * @return Capacidade em GFLOPS ou 0 se sem GPU
     */
    public double getGpuGflops() {
        if (allocatedGpu == null) {
            return 0.0;
        }
        
        return allocatedGpu.getGflops();
    }
    
    // ==================== MÉTODOS DE RECONFIGURAÇÃO ====================
    
    /**
     * Reconfigura a capacidade da GPU (se suportado).
     * Implementação futura para time-slicing dinâmico ou MIG.
     * 
     * @param newGflops Nova capacidade em GFLOPS
     */
    public void reconfigureGpu(double newGflops) {
        if (allocatedGpu == null) {
            SimLogger.printLine("Warning: Cannot reconfigure GPU - no GPU allocated to VM " + getId());
            return;
        }
        
        // TODO: Implementar reconfiguração dinâmica de GPU
        // Pode envolver MIG (Multi-Instance GPU) ou time-slicing ajustável
        SimLogger.printLine("Info: GPU reconfiguration not yet implemented for VM " + getId());
    }
    
    /**
     * Retorna representação em string da GpuEdgeVM.
     * @return String com ID, MIPS, GPU status
     */
    @Override
    public String toString() {
        return String.format("GpuEdgeVM[id=%d, mips=%.2f, hasGpu=%s, gpuGflops=%.2f]",
                           getId(), getMips(), hasGpu(), getGpuGflops());
    }
}
