
/*
 * Title:        GpuEdgeCloudSim - GpuCloudletSchedulerTimeShared
 * 
 * Description:  
 * GpuCloudletSchedulerTimeShared implements time-slicing GPU scheduling
 * where multiple tasks share GPU resources through time division.
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

import edu.boun.edgecloudsim.edge_client.GpuTask;
import edu.boun.edgecloudsim.utils.SimLogger;

/**
 * Time-shared GPU task scheduler implementation.
 * 
 * This scheduler implements a fair time-slicing policy where multiple GPU tasks
 * share the GPU's computational resources. Each task receives an equal fraction
 * of the GPU's GFLOPS capacity, proportional to the number of active tasks.
 * 
 * Scheduling algorithm:
 * - Fair sharing: GFLOPS divided equally among N active tasks
 * - No preemption: Tasks run until completion (no context switching modeled)
 * - FCFS within time-slices: First-come-first-served for submission order
 * 
 * Example:
 * - GPU with 10 GFLOPS
 * - 3 tasks running simultaneously
 * - Each task gets 10/3 = 3.33 GFLOPS effective capacity
 * 
 * Key features:
 * - Supports multiple concurrent GPU tasks
 * - Automatic resource redistribution when tasks complete
 * - Tracks remaining work for each task
 * - Calculates completion times based on shared resources
 * 
 * Limitations:
 * - No priority levels (all tasks equal)
 * - No memory contention modeling
 * - Assumes perfect time-slicing (no overhead)
 * - No task migration support
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 * @since GpuEdgeCloudSim v1.0
 */
public class GpuCloudletSchedulerTimeShared implements GpuCloudletScheduler {
    
    /** GPU gerenciada por este escalonador */
    private Gpu gpu;
    
    /** Lista de tarefas GPU em execução */
    private List<GpuTask> runningTasks;
    
    /** Lista de tarefas GPU aguardando execução */
    private List<GpuTask> waitingTasks;
    
    /** Lista de tarefas GPU completadas */
    private List<GpuTask> completedTasks;
    
    /** Mapa de trabalho restante para cada tarefa (taskId -> remaining GFLOPS) */
    private Map<Integer, Double> taskRemainingWorkMap;
    
    /** Último tempo de atualização do escalonamento */
    private double previousTime;
    
    /**
     * Construtor padrão (GPU será definida via initialize()).
     */
    public GpuCloudletSchedulerTimeShared() {
        this.runningTasks = new ArrayList<>();
        this.waitingTasks = new ArrayList<>();
        this.completedTasks = new ArrayList<>();
        this.taskRemainingWorkMap = new HashMap<>();
        this.previousTime = 0.0;
        this.gpu = null;
    }
    
    /**
     * Construtor com GPU especificada.
     * 
     * @param gpu GPU a ser gerenciada
     */
    public GpuCloudletSchedulerTimeShared(Gpu gpu) {
        this();
        initialize(gpu);
    }
    
    /**
     * Inicializa o escalonador com uma GPU.
     * 
     * @param gpu GPU a ser gerenciada
     * @throws IllegalArgumentException se gpu for null
     */
    @Override
    public void initialize(Gpu gpu) {
        if (gpu == null) {
            throw new IllegalArgumentException("GPU cannot be null");
        }
        this.gpu = gpu;
        SimLogger.printLine("GpuCloudletSchedulerTimeShared initialized for GPU " + gpu.getId());
    }
    
    /**
     * Submete uma tarefa GPU para execução.
     * 
     * No modo time-shared, todas as tarefas são aceitas e adicionadas
     * à fila de running tasks, redistribuindo os recursos automaticamente.
     * 
     * @param gpuTask Tarefa GPU a ser executada
     * @return true sempre (time-shared aceita todas as tarefas)
     */
    @Override
    public boolean submitGpuTask(GpuTask gpuTask) {
        if (gpuTask == null || gpu == null) {
            SimLogger.printLine("Error: Cannot submit null task or GPU not initialized");
            return false;
        }
        
        // Verifica se há memória GPU suficiente
        if (!gpu.allocateMemory(gpuTask.getRequiredGpuMemory())) {
            SimLogger.printLine("Error: Insufficient GPU memory for task " + gpuTask.getCloudletId());
            return false;
        }
        
        // Adiciona à lista de running tasks
        runningTasks.add(gpuTask);
        
        // Registra o trabalho total restante (em GFLOPS)
        taskRemainingWorkMap.put(gpuTask.getCloudletId(), (double) gpuTask.getGpuLength());
        
        // Marca o início da execução GPU
        gpuTask.setGpuStartTime(previousTime);
        gpuTask.setExecutedGpuId(gpu.getId());
        
        // Redistribui recursos entre todas as tarefas ativas
        redistributeGpuResources();
        
        SimLogger.printLine("GPU Task " + gpuTask.getCloudletId() + " submitted to GPU " + 
                          gpu.getId() + " (" + runningTasks.size() + " tasks running)");
        
        return true;
    }
    
    /**
     * Remove uma tarefa GPU da execução.
     * 
     * @param gpuTask Tarefa a ser removida
     * @return true se removida com sucesso
     */
    @Override
    public boolean removeGpuTask(GpuTask gpuTask) {
        if (gpuTask == null) {
            return false;
        }
        
        boolean removed = runningTasks.remove(gpuTask) || waitingTasks.remove(gpuTask);
        
        if (removed) {
            // Libera memória GPU
            gpu.deallocateMemory(gpuTask.getRequiredGpuMemory());
            taskRemainingWorkMap.remove(gpuTask.getCloudletId());
            redistributeGpuResources();
            SimLogger.printLine("GPU Task " + gpuTask.getCloudletId() + " removed from GPU " + gpu.getId());
        }
        
        return removed;
    }
    
    /**
     * Atualiza o processamento das tarefas GPU.
     * 
     * Calcula o progresso de cada tarefa baseado no tempo decorrido e
     * capacidade GPU alocada. Finaliza tarefas completadas e redistribui recursos.
     * 
     * @param currentTime Tempo atual da simulação
     * @return Próximo tempo de evento ou Double.MAX_VALUE
     */
    @Override
    public double updateGpuTaskProcessing(double currentTime) {
        if (gpu == null || runningTasks.isEmpty()) {
            return Double.MAX_VALUE;
        }
        
        double timeSpan = currentTime - previousTime;
        
        if (timeSpan <= 0) {
            return Double.MAX_VALUE; // Nenhuma atualização necessária
        }
        
        // Capacidade GPU dividida entre tarefas ativas
        int numRunningTasks = runningTasks.size();
        double gflopsPerTask = gpu.getGflops() / numRunningTasks;
        
        // Atualiza trabalho restante de cada tarefa
        List<GpuTask> tasksToComplete = new ArrayList<>();
        
        for (GpuTask task : runningTasks) {
            int taskId = task.getCloudletId();
            double remainingWork = taskRemainingWorkMap.get(taskId);
            
            // Trabalho executado neste time span
            double workDone = gflopsPerTask * timeSpan;
            double newRemainingWork = remainingWork - workDone;
            
            if (newRemainingWork <= 0) {
                // Tarefa completada
                tasksToComplete.add(task);
            } else {
                // Atualiza trabalho restante
                taskRemainingWorkMap.put(taskId, newRemainingWork);
            }
        }
        
        // Finaliza tarefas completadas
        for (GpuTask task : tasksToComplete) {
            finishGpuTask(task);
        }
        
        // Atualiza utilização da GPU
        if (!runningTasks.isEmpty()) {
            gpu.setUtilization(100.0); // GPU totalmente utilizada se há tarefas
        } else {
            gpu.setUtilization(0.0);
        }
        
        previousTime = currentTime;
        
        // Calcula próximo evento (conclusão da próxima tarefa)
        if (runningTasks.isEmpty()) {
            return Double.MAX_VALUE;
        }
        
        // Encontra a tarefa que completará primeiro
        double minTimeToComplete = Double.MAX_VALUE;
        for (GpuTask task : runningTasks) {
            double remainingWork = taskRemainingWorkMap.get(task.getCloudletId());
            double timeToComplete = remainingWork / gflopsPerTask;
            if (timeToComplete < minTimeToComplete) {
                minTimeToComplete = timeToComplete;
            }
        }
        
        return currentTime + minTimeToComplete;
    }
    
    /**
     * Retorna a lista de tarefas em execução.
     * 
     * @return Lista imutável de tarefas em execução
     */
    @Override
    public List<GpuTask> getRunningGpuTasks() {
        return new ArrayList<>(runningTasks);
    }
    
    /**
     * Retorna a lista de tarefas aguardando.
     * 
     * @return Lista imutável de tarefas em espera
     */
    @Override
    public List<GpuTask> getWaitingGpuTasks() {
        return new ArrayList<>(waitingTasks);
    }
    
    /**
     * Retorna a lista de tarefas completadas.
     * 
     * @return Lista imutável de tarefas finalizadas
     */
    @Override
    public List<GpuTask> getCompletedGpuTasks() {
        return new ArrayList<>(completedTasks);
    }
    
    /**
     * Calcula a utilização da GPU baseada no número de tarefas ativas.
     * 
     * @return Utilização em percentual (0-100%)
     */
    @Override
    public double getGpuUtilization() {
        return gpu != null ? gpu.getUtilization() : 0.0;
    }
    
    /**
     * Verifica se há tarefas em execução.
     * 
     * @return true se há tarefas em execução
     */
    @Override
    public boolean hasRunningTasks() {
        return !runningTasks.isEmpty();
    }
    
    /**
     * Retorna a GPU gerenciada.
     * 
     * @return GPU gerenciada
     */
    @Override
    public Gpu getGpu() {
        return gpu;
    }
    
    /**
     * Redistribui os recursos GPU entre tarefas ativas.
     * 
     * No modo time-shared, cada tarefa recebe uma fração igual
     * da capacidade total da GPU.
     */
    private void redistributeGpuResources() {
        if (runningTasks.isEmpty()) {
            return;
        }
        
        // Cada tarefa recebe capacidade igual
        // Informacional: não afeta diretamente o processamento
        // (cálculo feito em updateGpuTaskProcessing)
        
        SimLogger.printLine("GPU " + gpu.getId() + " resources redistributed among " + 
                          runningTasks.size() + " tasks");
    }
    
    /**
     * Finaliza uma tarefa GPU e move para completed tasks.
     * 
     * @param gpuTask Tarefa a ser finalizada
     */
    private void finishGpuTask(GpuTask gpuTask) {
        runningTasks.remove(gpuTask);
        completedTasks.add(gpuTask);
        
        // Define métricas finais
        gpuTask.setGpuFinishTime(previousTime);
        double executionTime = previousTime - gpuTask.getGpuStartTime();
        gpuTask.setGpuExecutionTime(executionTime);
        gpuTask.setActualGpuUtilization(gpu.getUtilization());
        
        // Libera memória GPU
        gpu.deallocateMemory(gpuTask.getRequiredGpuMemory());
        taskRemainingWorkMap.remove(gpuTask.getCloudletId());
        
        // Redistribui recursos restantes
        redistributeGpuResources();
        
        SimLogger.printLine("GPU Task " + gpuTask.getCloudletId() + " completed on GPU " + 
                          gpu.getId() + " (execution time: " + executionTime + "s)");
    }
}
