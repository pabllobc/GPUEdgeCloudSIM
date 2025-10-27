
/*
 * Title:        GpuEdgeCloudSim - GpuTask
 * 
 * Description:  
 * GpuTask extends Task to include GPU-specific requirements and metrics
 * for heterogeneous CPU+GPU workloads in edge computing.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2025, GpuEdgeCloudSim Project
 * Author:       Pabllo Borges Cardoso
 */

package edu.boun.edgecloudsim.edge_client;

import org.cloudbus.cloudsim.UtilizationModel;

/**
 * Extended Task class for GPU-accelerated edge computing workloads.
 * 
 * This class extends EdgeCloudSim's Task to support heterogeneous workloads
 * that require both CPU and GPU processing. It models GPU-specific requirements
 * (GFLOPS, VRAM, data transfer) and tracks execution metrics for GPU phases.
 * 
 * Task execution model:
 * 1. CPU → GPU data transfer (gpuInputData MB)
 * 2. GPU kernel execution (gpuLength GFLOPS)
 * 3. GPU → CPU data transfer (gpuOutputData MB)
 * 
 * The CPU portion (cloudletLength MI) can execute before, after, or
 * concurrently with GPU processing depending on the scheduler.
 * 
 * Key features:
 * - Dual CPU+GPU workload modeling
 * - GPU memory requirement tracking
 * - Data transfer time estimation
 * - Execution metrics collection (actual vs expected)
 * - GPU intensity calculation for orchestration decisions
 * 
 * Use cases:
 * - Deep learning inference at the edge
 * - Computer vision processing
 * - Scientific computing offloading
 * - Video transcoding and encoding
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 * @since GpuEdgeCloudSim v1.0
 */
public class GpuTask extends Task {
    
    // ==================== REQUISITOS GPU ====================
    
    /** Tamanho da carga de trabalho GPU em GFLOPS */
    private long gpuLength;
    
    /** Tamanho dos dados de entrada para a GPU em MB */
    private long gpuInputData;
    
    /** Tamanho dos dados de saída da GPU em MB */
    private long gpuOutputData;
    
    /** Memória GPU necessária em MB */
    private long requiredGpuMemory;
    
    /** Utilização GPU esperada em percentual (0-100%) */
    private double expectedGpuUtilization;
    
    // ==================== MÉTRICAS DE EXECUÇÃO GPU ====================
    
    /** ID da GPU onde esta tarefa foi executada (-1 se não executada) */
    private int executedGpuId;
    
    /** Tempo de transferência CPU → GPU em segundos */
    private double gpuDataTransferTime;
    
    /** Tempo de execução na GPU em segundos */
    private double gpuExecutionTime;
    
    /** Tempo de transferência GPU → CPU em segundos */
    private double gpuDataBackTime;
    
    /** Utilização GPU real durante a execução (0-100%) */
    private double actualGpuUtilization;
    
    /** Timestamp do início da transferência CPU → GPU */
    private double gpuStartTime;
    
    /** Timestamp do fim da execução GPU completa */
    private double gpuFinishTime;
    
    /**
     * Construtor completo para GpuTask com requisitos CPU e GPU.
     * 
     * @param mobileDeviceId ID do dispositivo que gerou a tarefa
     * @param cloudletId ID único da tarefa
     * @param cloudletLength Tamanho do processamento CPU em MI
     * @param pesNumber Número de PEs (CPU cores) necessários
     * @param cloudletFileSize Tamanho do arquivo de entrada em bytes
     * @param cloudletOutputSize Tamanho do arquivo de saída em bytes
     * @param utilizationModelCpu Modelo de utilização de CPU
     * @param utilizationModelRam Modelo de utilização de RAM
     * @param utilizationModelBw Modelo de utilização de largura de banda
     * @param gpuLength Tamanho da carga GPU em GFLOPS
     * @param gpuInputData Dados de entrada para GPU em MB
     * @param gpuOutputData Dados de saída da GPU em MB
     * @param requiredGpuMemory Memória GPU necessária em MB
     */
    public GpuTask(int mobileDeviceId, int cloudletId, long cloudletLength, 
                   int pesNumber, long cloudletFileSize, long cloudletOutputSize,
                   UtilizationModel utilizationModelCpu,
                   UtilizationModel utilizationModelRam,
                   UtilizationModel utilizationModelBw,
                   long gpuLength, long gpuInputData, long gpuOutputData,
                   long requiredGpuMemory) {
        super(mobileDeviceId, cloudletId, cloudletLength, pesNumber, 
              cloudletFileSize, cloudletOutputSize, utilizationModelCpu, 
              utilizationModelRam, utilizationModelBw);
        
        this.gpuLength = gpuLength;
        this.gpuInputData = gpuInputData;
        this.gpuOutputData = gpuOutputData;
        this.requiredGpuMemory = requiredGpuMemory;
        this.expectedGpuUtilization = 100.0; // Default: assume full utilization
        
        // Inicializa métricas
        this.executedGpuId = -1;
        this.gpuDataTransferTime = 0.0;
        this.gpuExecutionTime = 0.0;
        this.gpuDataBackTime = 0.0;
        this.actualGpuUtilization = 0.0;
        this.gpuStartTime = 0.0;
        this.gpuFinishTime = 0.0;
    }
    
    /**
     * Construtor simplificado para GpuTask (compatibilidade com Task).
     * Utiliza modelos de utilização padrão do CloudSim.
     * 
     * @param cloudletId ID único da tarefa
     * @param taskType ID do tipo de tarefa
     * @param cloudletLength Tamanho do processamento CPU em MI
     * @param pesNumber Número de PEs necessários
     * @param cloudletFileSize Tamanho do arquivo de entrada
     * @param cloudletOutputSize Tamanho do arquivo de saída
     * @param gpuLength Tamanho da carga GPU em GFLOPS
     * @param gpuInputData Dados de entrada para GPU em MB
     * @param gpuOutputData Dados de saída da GPU em MB
     * @param requiredGpuMemory Memória GPU necessária em MB
     */
    public GpuTask(int cloudletId, int taskType, long cloudletLength, 
                   int pesNumber, long cloudletFileSize, long cloudletOutputSize,
                   long gpuLength, long gpuInputData, long gpuOutputData,
                   long requiredGpuMemory) {
        this(-1, cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize,
             null, null, null, gpuLength, gpuInputData, gpuOutputData, requiredGpuMemory);
        this.setTaskType(taskType);
    }
    
    // ==================== GETTERS DE REQUISITOS GPU ====================
    
    /**
     * Retorna o tamanho da carga de trabalho GPU.
     * @return Tamanho em GFLOPS
     */
    public long getGpuLength() {
        return gpuLength;
    }
    
    /**
     * Retorna o tamanho dos dados de entrada para GPU.
     * @return Tamanho em MB
     */
    public long getGpuInputData() {
        return gpuInputData;
    }
    
    /**
     * Retorna o tamanho dos dados de saída da GPU.
     * @return Tamanho em MB
     */
    public long getGpuOutputData() {
        return gpuOutputData;
    }
    
    /**
     * Retorna a memória GPU necessária.
     * @return Memória em MB
     */
    public long getRequiredGpuMemory() {
        return requiredGpuMemory;
    }
    
    /**
     * Retorna a utilização GPU esperada.
     * @return Utilização esperada em percentual (0-100%)
     */
    public double getExpectedGpuUtilization() {
        return expectedGpuUtilization;
    }
    
    // ==================== GETTERS DE MÉTRICAS GPU ====================
    
    /**
     * Retorna o ID da GPU onde a tarefa foi executada.
     * @return ID da GPU ou -1 se não executada
     */
    public int getExecutedGpuId() {
        return executedGpuId;
    }
    
    /**
     * Retorna o tempo de transferência CPU → GPU.
     * @return Tempo em segundos
     */
    public double getGpuDataTransferTime() {
        return gpuDataTransferTime;
    }
    
    /**
     * Retorna o tempo de execução na GPU.
     * @return Tempo em segundos
     */
    public double getGpuExecutionTime() {
        return gpuExecutionTime;
    }
    
    /**
     * Retorna o tempo de transferência GPU → CPU.
     * @return Tempo em segundos
     */
    public double getGpuDataBackTime() {
        return gpuDataBackTime;
    }
    
    /**
     * Retorna a utilização GPU real durante execução.
     * @return Utilização em percentual (0-100%)
     */
    public double getActualGpuUtilization() {
        return actualGpuUtilization;
    }
    
    /**
     * Retorna o timestamp de início da execução GPU.
     * @return Timestamp em segundos de simulação
     */
    public double getGpuStartTime() {
        return gpuStartTime;
    }
    
    /**
     * Retorna o timestamp de fim da execução GPU.
     * @return Timestamp em segundos de simulação
     */
    public double getGpuFinishTime() {
        return gpuFinishTime;
    }
    
    // ==================== SETTERS DE MÉTRICAS GPU ====================
    
    /**
     * Define o ID da GPU onde a tarefa foi executada.
     * @param gpuId ID da GPU
     */
    public void setExecutedGpuId(int gpuId) {
        this.executedGpuId = gpuId;
    }
    
    /**
     * Define o tempo de transferência CPU → GPU.
     * @param time Tempo em segundos
     */
    public void setGpuDataTransferTime(double time) {
        this.gpuDataTransferTime = time;
    }
    
    /**
     * Define o tempo de execução na GPU.
     * @param time Tempo em segundos
     */
    public void setGpuExecutionTime(double time) {
        this.gpuExecutionTime = time;
    }
    
    /**
     * Define o tempo de transferência GPU → CPU.
     * @param time Tempo em segundos
     */
    public void setGpuDataBackTime(double time) {
        this.gpuDataBackTime = time;
    }
    
    /**
     * Define a utilização GPU real durante execução.
     * @param utilization Utilização em percentual (0-100%)
     */
    public void setActualGpuUtilization(double utilization) {
        this.actualGpuUtilization = utilization;
    }
    
    /**
     * Define o timestamp de início da execução GPU.
     * @param time Timestamp em segundos de simulação
     */
    public void setGpuStartTime(double time) {
        this.gpuStartTime = time;
    }
    
    /**
     * Define o timestamp de fim da execução GPU.
     * @param time Timestamp em segundos de simulação
     */
    public void setGpuFinishTime(double time) {
        this.gpuFinishTime = time;
    }
    
    /**
     * Define a utilização GPU esperada.
     * @param utilization Utilização esperada em percentual (0-100%)
     */
    public void setExpectedGpuUtilization(double utilization) {
        this.expectedGpuUtilization = utilization;
    }
    
    // ==================== MÉTODOS DE CÁLCULO ====================
    
    /**
     * Calcula o tempo total de processamento GPU.
     * Inclui transferências de dados (CPU→GPU, GPU→CPU) e execução.
     * 
     * @return Tempo total em segundos
     */
    public double getTotalGpuTime() {
        return gpuDataTransferTime + gpuExecutionTime + gpuDataBackTime;
    }
    
    /**
     * Verifica se esta tarefa requer processamento GPU.
     * 
     * @return true se gpuLength > 0, false caso contrário
     */
    public boolean requiresGpu() {
        return gpuLength > 0;
    }
    
    /**
     * Calcula a proporção de processamento GPU em relação ao total.
     * 
     * Intensity = gpuLength / (cpuLength + gpuLength)
     * Valores próximos de 1.0 indicam tarefas GPU-intensive.
     * Valores próximos de 0.0 indicam tarefas CPU-intensive.
     * 
     * @return Razão GPU/(CPU+GPU) entre 0 e 1
     */
    public double getGpuIntensity() {
        long totalLength = getCloudletLength() + gpuLength;
        if (totalLength == 0) {
            return 0.0;
        }
        return (double) gpuLength / totalLength;
    }
    
    /**
     * Verifica se a tarefa tem memória GPU suficiente alocada.
     * 
     * @param availableMemory Memória disponível em MB
     * @return true se a memória é suficiente, false caso contrário
     */
    public boolean hasEnoughGpuMemory(long availableMemory) {
        return availableMemory >= requiredGpuMemory;
    }
    
    /**
     * Retorna representação em string da GpuTask com métricas GPU.
     * @return String com informações CPU e GPU
     */
    @Override
    public String toString() {
        return String.format("GpuTask[id=%d, cpuLength=%d MI, gpuLength=%d GFLOPS, " +
                           "gpuMemory=%d MB, gpuIntensity=%.2f, executedOnGpu=%d]",
                           getCloudletId(), getCloudletLength(), gpuLength, 
                           requiredGpuMemory, getGpuIntensity(), executedGpuId);
    }
}
