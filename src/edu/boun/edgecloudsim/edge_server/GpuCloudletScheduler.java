
/*
 * Title:        GpuEdgeCloudSim - GpuCloudletScheduler
 * 
 * Description:  
 * GpuCloudletScheduler defines the interface for scheduling GPU tasks
 * on a single GPU resource within a virtual machine.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2025, GpuEdgeCloudSim Project
 * Author:       Pabllo Borges Cardoso
 */

package edu.boun.edgecloudsim.edge_server;

import java.util.List;

import edu.boun.edgecloudsim.edge_client.GpuTask;

/**
 * Interface for GPU task scheduling within a virtual machine.
 * 
 * This interface defines the contract for managing GPU task execution
 * on a single GPU resource allocated to a VM. It handles task queuing,
 * execution scheduling, and resource time-slicing for GPU workloads.
 * 
 * Design philosophy:
 * - Follows CloudSim's CloudletScheduler pattern adapted for GPUs
 * - Decouples scheduling policy from GPU hardware model
 * - Enables pluggable scheduling strategies
 * 
 * Scheduling modes:
 * - Time-shared: Multiple tasks share GPU via time-slicing
 * - Space-shared: One task executes exclusively (future)
 * - Priority-based: QoS-aware scheduling (future)
 * 
 * Key responsibilities:
 * - Accept and queue GPU tasks
 * - Distribute GPU capacity among active tasks
 * - Track task execution progress
 * - Report utilization metrics
 * - Manage task lifecycle (running → completed)
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 * @since GpuEdgeCloudSim v1.0
 */
public interface GpuCloudletScheduler {
    
    /**
     * Inicializa o escalonador com uma GPU específica.
     * 
     * Deve ser chamado antes de submeter tarefas. Associa o escalonador
     * com a GPU física que ele irá gerenciar.
     * 
     * @param gpu GPU a ser gerenciada pelo escalonador
     * @throws IllegalArgumentException se gpu for null
     */
    void initialize(Gpu gpu);
    
    /**
     * Submete uma tarefa GPU para execução.
     * 
     * A tarefa é adicionada à fila apropriada (running ou waiting) dependendo
     * da política de escalonamento e disponibilidade de recursos.
     * 
     * @param gpuTask Tarefa GPU a ser executada
     * @return true se a tarefa foi aceita, false caso contrário
     */
    boolean submitGpuTask(GpuTask gpuTask);
    
    /**
     * Remove uma tarefa GPU da fila de execução.
     * 
     * Pode ser usado para cancelamento de tarefas ou migração entre VMs.
     * 
     * @param gpuTask Tarefa a ser removida
     * @return true se a tarefa foi removida, false se não encontrada
     */
    boolean removeGpuTask(GpuTask gpuTask);
    
    /**
     * Atualiza o escalonamento das tarefas GPU.
     * 
     * Chamado periodicamente pelo simulador para:
     * - Atualizar progresso de execução das tarefas
     * - Finalizar tarefas completadas
     * - Reescalonar recursos entre tarefas ativas
     * - Promover tarefas waiting → running se houver recursos
     * 
     * @param currentTime Tempo atual da simulação em segundos
     * @return Próximo tempo de evento relevante ou Double.MAX_VALUE se nenhum
     */
    double updateGpuTaskProcessing(double currentTime);
    
    /**
     * Retorna a lista de tarefas GPU atualmente em execução.
     * 
     * @return Lista imutável de tarefas em execução
     */
    List<GpuTask> getRunningGpuTasks();
    
    /**
     * Retorna a lista de tarefas GPU aguardando execução.
     * 
     * @return Lista imutável de tarefas em espera
     */
    List<GpuTask> getWaitingGpuTasks();
    
    /**
     * Retorna a lista de tarefas GPU completadas.
     * 
     * @return Lista imutável de tarefas finalizadas
     */
    List<GpuTask> getCompletedGpuTasks();
    
    /**
     * Calcula a utilização atual da GPU gerenciada.
     * 
     * Baseado no número de tarefas ativas e distribuição de recursos.
     * 
     * @return Utilização em percentual (0-100%)
     */
    double getGpuUtilization();
    
    /**
     * Verifica se há tarefas GPU em execução.
     * 
     * @return true se há tarefas em execução, false caso contrário
     */
    boolean hasRunningTasks();
    
    /**
     * Retorna a GPU gerenciada por este escalonador.
     * 
     * @return GPU gerenciada
     */
    Gpu getGpu();
}
