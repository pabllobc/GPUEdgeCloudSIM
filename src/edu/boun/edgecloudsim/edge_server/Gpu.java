
/*
 * Title:        GpuEdgeCloudSim - Gpu
 * 
 * Description:  
 * Gpu represents a physical GPU resource in an EdgeHost, modeling its hardware 
 * characteristics and utilization state for GPU-accelerated edge computing.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2025, GpuEdgeCloudSim Project
 * Author:       Pabllo Borges Cardoso
 */

package edu.boun.edgecloudsim.edge_server;

import edu.boun.edgecloudsim.utils.SimLogger;

/**
 * Represents a physical GPU resource in an edge computing host.
 * 
 * This class models GPU hardware characteristics including computational capacity
 * (GFLOPS), memory (VRAM), CUDA cores, and memory bandwidth. It tracks allocation
 * state and utilization metrics essential for GPU-aware task scheduling and
 * resource management in edge computing scenarios.
 * 
 * Key features:
 * - Hardware specification modeling (CUDA cores, GFLOPS, VRAM, bandwidth)
 * - Dynamic utilization and memory tracking
 * - VM allocation management (exclusive mode)
 * - Execution time calculation for GPU workloads
 * - Data transfer time estimation based on memory bandwidth
 * 
 * Design philosophy:
 * - Follows EdgeCloudSim's resource modeling patterns
 * - Supports exclusive allocation (1 GPU : 1 VM) in v1.0
 * - Extensible for shared allocation in future versions
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 * @since GpuEdgeCloudSim v1.0
 */
public class Gpu {
    
    /** Identificador único da GPU dentro do host */
    private int id;
    
    /** Tipo/modelo da GPU (ex: "NVIDIA_T4", "NVIDIA_A100") */
    private String gpuType;
    
    /** Número de CUDA cores disponíveis */
    private int cudaCores;
    
    /** Capacidade computacional em GFLOPS (Giga Floating Point Operations Per Second) */
    private double gflops;
    
    /** Memória total da GPU em MB */
    private long gpuMemory;
    
    /** Largura de banda da memória GPU em GB/s */
    private double memoryBandwidth;
    
    /** Utilização atual da GPU em percentual (0-100%) */
    private double utilization;
    
    /** Memória GPU atualmente ocupada em MB */
    private long usedMemory;
    
    /** VM atualmente alocada nesta GPU (null se disponível) */
    private GpuEdgeVM allocatedVm;
    
    /** Host que contém esta GPU */
    private GpuEdgeHost host;
    
    /**
     * Construtor completo para criar uma GPU com todas as especificações.
     * 
     * @param id Identificador único da GPU
     * @param gpuType Tipo/modelo da GPU (ex: "NVIDIA_T4")
     * @param cudaCores Número de CUDA cores
     * @param gflops Capacidade computacional em GFLOPS
     * @param gpuMemory Memória total em MB
     * @param memoryBandwidth Largura de banda da memória em GB/s
     */
    public Gpu(int id, String gpuType, int cudaCores, double gflops, 
               long gpuMemory, double memoryBandwidth) {
        this.id = id;
        this.gpuType = gpuType;
        this.cudaCores = cudaCores;
        this.gflops = gflops;
        this.gpuMemory = gpuMemory;
        this.memoryBandwidth = memoryBandwidth;
        this.utilization = 0.0;
        this.usedMemory = 0;
        this.allocatedVm = null;
        this.host = null;
    }
    
    /**
     * Construtor simplificado para criar uma GPU com especificações básicas.
     * Tipo da GPU será definido como "Generic_GPU".
     * 
     * @param id Identificador único da GPU
     * @param cudaCores Número de CUDA cores
     * @param gflops Capacidade computacional em GFLOPS
     * @param gpuMemory Memória total em MB
     */
    public Gpu(int id, int cudaCores, double gflops, long gpuMemory) {
        this(id, "Generic_GPU", cudaCores, gflops, gpuMemory, 100.0);
    }
    
    // ==================== GETTERS BÁSICOS ====================
    
    /**
     * Retorna o identificador único da GPU.
     * @return ID da GPU
     */
    public int getId() {
        return id;
    }
    
    /**
     * Retorna o tipo/modelo da GPU.
     * @return Tipo da GPU (ex: "NVIDIA_T4")
     */
    public String getGpuType() {
        return gpuType;
    }
    
    /**
     * Retorna o número de CUDA cores.
     * @return Número de CUDA cores
     */
    public int getCudaCores() {
        return cudaCores;
    }
    
    /**
     * Retorna a capacidade computacional em GFLOPS.
     * @return Capacidade em GFLOPS
     */
    public double getGflops() {
        return gflops;
    }
    
    /**
     * Retorna a memória total da GPU em MB.
     * @return Memória total em MB
     */
    public long getGpuMemory() {
        return gpuMemory;
    }
    
    /**
     * Retorna a largura de banda da memória em GB/s.
     * @return Largura de banda em GB/s
     */
    public double getMemoryBandwidth() {
        return memoryBandwidth;
    }
    
    /**
     * Retorna o host que contém esta GPU.
     * @return Host da GPU
     */
    public GpuEdgeHost getHost() {
        return host;
    }
    
    // ==================== GETTERS DE ESTADO ====================
    
    /**
     * Retorna a utilização atual da GPU em percentual.
     * @return Utilização (0-100%)
     */
    public double getUtilization() {
        return utilization;
    }
    
    /**
     * Retorna a memória GPU atualmente ocupada em MB.
     * @return Memória ocupada em MB
     */
    public long getUsedMemory() {
        return usedMemory;
    }
    
    /**
     * Retorna a memória GPU disponível em MB.
     * @return Memória disponível em MB
     */
    public long getAvailableMemory() {
        return gpuMemory - usedMemory;
    }
    
    /**
     * Retorna a VM atualmente alocada nesta GPU.
     * @return VM alocada ou null se disponível
     */
    public GpuEdgeVM getAllocatedVm() {
        return allocatedVm;
    }
    
    /**
     * Verifica se a GPU está disponível (não alocada).
     * @return true se disponível, false caso contrário
     */
    public boolean isAvailable() {
        return allocatedVm == null;
    }
    
    // ==================== SETTERS ====================
    
    /**
     * Define o host que contém esta GPU.
     * @param host Host da GPU
     */
    public void setHost(GpuEdgeHost host) {
        this.host = host;
    }
    
    /**
     * Define a utilização atual da GPU.
     * Valida o valor para estar entre 0 e 100%.
     * 
     * @param utilization Utilização em percentual (0-100%)
     */
    public void setUtilization(double utilization) {
        if (utilization < 0.0) {
            this.utilization = 0.0;
        } else if (utilization > 100.0) {
            this.utilization = 100.0;
        } else {
            this.utilization = utilization;
        }
    }
    
    /**
     * Define a memória GPU atualmente ocupada.
     * Valida para não exceder a memória total.
     * 
     * @param usedMemory Memória ocupada em MB
     */
    public void setUsedMemory(long usedMemory) {
        if (usedMemory < 0) {
            this.usedMemory = 0;
        } else if (usedMemory > gpuMemory) {
            this.usedMemory = gpuMemory;
            SimLogger.printLine("Warning: GPU " + id + " memory exceeded! Capping at " + gpuMemory + " MB");
        } else {
            this.usedMemory = usedMemory;
        }
    }
    
    /**
     * Define a VM alocada nesta GPU.
     * @param vm VM a ser alocada (null para desalocar)
     */
    public void setAllocatedVm(GpuEdgeVM vm) {
        this.allocatedVm = vm;
    }
    
    // ==================== MÉTODOS DE GERENCIAMENTO ====================
    
    /**
     * Aloca memória GPU para uma tarefa.
     * Verifica se há memória disponível suficiente antes de alocar.
     * 
     * @param memorySize Quantidade de memória a alocar em MB
     * @return true se a alocação foi bem-sucedida, false caso contrário
     */
    public boolean allocateMemory(long memorySize) {
        if (memorySize < 0) {
            SimLogger.printLine("Error: Cannot allocate negative memory on GPU " + id);
            return false;
        }
        
        if (usedMemory + memorySize > gpuMemory) {
            SimLogger.printLine("Error: GPU " + id + " insufficient memory. " +
                              "Required: " + memorySize + " MB, Available: " + getAvailableMemory() + " MB");
            return false;
        }
        
        usedMemory += memorySize;
        return true;
    }
    
    /**
     * Libera memória GPU previamente alocada.
     * 
     * @param memorySize Quantidade de memória a liberar em MB
     * @return true se a liberação foi bem-sucedida, false caso contrário
     */
    public boolean deallocateMemory(long memorySize) {
        if (memorySize < 0) {
            SimLogger.printLine("Error: Cannot deallocate negative memory on GPU " + id);
            return false;
        }
        
        if (memorySize > usedMemory) {
            SimLogger.printLine("Warning: GPU " + id + " trying to deallocate more memory than used. " +
                              "Resetting to 0.");
            usedMemory = 0;
        } else {
            usedMemory -= memorySize;
        }
        
        return true;
    }
    
    /**
     * Calcula o tempo de execução de uma tarefa nesta GPU.
     * Tempo = gpuLength (GFLOPS) / capacidade GPU (GFLOPS)
     * 
     * @param gpuLength Tamanho da tarefa em GFLOPS
     * @return Tempo de execução em segundos
     */
    public double calculateExecutionTime(long gpuLength) {
        if (gflops == 0) {
            SimLogger.printLine("Error: GPU " + id + " has 0 GFLOPS capacity!");
            return Double.MAX_VALUE;
        }
        
        // Tempo = trabalho / taxa
        // gpuLength está em GFLOPS (trabalho total)
        // gflops é a taxa de processamento (GFLOPS por segundo)
        return (double) gpuLength / gflops;
    }
    
    /**
     * Calcula o tempo de transferência de dados para a GPU.
     * Tempo = dataSize (MB) / memoryBandwidth (GB/s) convertido para segundos
     * 
     * @param dataSize Tamanho dos dados em MB
     * @return Tempo de transferência em segundos
     */
    public double calculateDataTransferTime(long dataSize) {
        if (memoryBandwidth == 0) {
            SimLogger.printLine("Error: GPU " + id + " has 0 GB/s memory bandwidth!");
            return Double.MAX_VALUE;
        }
        
        // Converter MB para GB e dividir pela largura de banda em GB/s
        double dataSizeGB = dataSize / 1024.0;
        return dataSizeGB / memoryBandwidth;
    }
    
    /**
     * Reseta a GPU para o estado inicial (disponível).
     * Libera a VM alocada, zera utilização e memória usada.
     */
    public void reset() {
        this.allocatedVm = null;
        this.utilization = 0.0;
        this.usedMemory = 0;
    }
    
    // ==================== MÉTODOS DE STRING ====================
    
    /**
     * Retorna representação em string da GPU com informações básicas.
     * @return String com ID, tipo, GFLOPS, memória e estado de alocação
     */
    @Override
    public String toString() {
        return String.format("GPU[id=%d, type=%s, cudaCores=%d, gflops=%.2f, memory=%dMB, " +
                           "bandwidth=%.2fGB/s, utilization=%.2f%%, available=%s]",
                           id, gpuType, cudaCores, gflops, gpuMemory, memoryBandwidth, 
                           utilization, isAvailable());
    }
}
