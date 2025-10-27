
/*
 * Title:        GpuEdgeCloudSim - GpuProvisioner
 * 
 * Description:  
 * GpuProvisioner defines the contract for GPU allocation and provisioning
 * to virtual machines in GPU-enabled edge computing infrastructure.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2025, GpuEdgeCloudSim Project
 * Author:       Pabllo Borges Cardoso
 */

package edu.boun.edgecloudsim.edge_server;

import java.util.List;

/**
 * Interface for GPU provisioning and allocation policies.
 * 
 * This interface defines the contract for managing GPU resources across
 * virtual machines in edge computing hosts. It provides methods for
 * allocating/deallocating GPUs, checking availability, and querying
 * GPU resource state.
 * 
 * Design follows the CloudSim provisioner pattern (RamProvisioner, BwProvisioner)
 * adapted for GPU resources with edge computing considerations.
 * 
 * Implementations define specific allocation strategies:
 * - Simple: Exclusive allocation (1 GPU : 1 VM)
 * - Shared: Time-slicing or MIG-based sharing (future)
 * - Priority-based: QoS-aware allocation (future)
 * 
 * Key responsibilities:
 * - Manage pool of GPUs in a host
 * - Allocate GPUs to VMs based on policy
 * - Track allocation state and availability
 * - Provide resource discovery and selection
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 * @since GpuEdgeCloudSim v1.0
 */
public interface GpuProvisioner {
    
    /**
     * Aloca uma GPU específica para uma VM.
     * 
     * Verifica disponibilidade da GPU antes de realizar a alocação.
     * Atualiza o estado da GPU e registra a alocação VM-GPU.
     * 
     * @param vm VM que receberá a GPU
     * @param gpu GPU a ser alocada
     * @return true se a alocação foi bem-sucedida, false caso contrário
     */
    boolean allocateGpuForVm(GpuEdgeVM vm, Gpu gpu);
    
    /**
     * Desaloca a GPU de uma VM, tornando-a disponível novamente.
     * 
     * Libera a GPU alocada para a VM, reseta seu estado e remove
     * o registro de alocação. A GPU fica disponível para outras VMs.
     * 
     * @param vm VM da qual a GPU será desalocada
     */
    void deallocateGpuForVm(GpuEdgeVM vm);
    
    /**
     * Verifica se há GPUs disponíveis no pool.
     * 
     * @return true se há pelo menos uma GPU disponível, false caso contrário
     */
    boolean hasAvailableGpu();
    
    /**
     * Retorna uma GPU disponível do pool.
     * 
     * Estratégia de seleção depende da implementação:
     * - First-fit: Primeira GPU disponível
     * - Best-fit: GPU com menor fragmentação de recursos
     * - Round-robin: Distribuição balanceada
     * 
     * @return GPU disponível ou null se não houver
     */
    Gpu getAvailableGpu();
    
    /**
     * Retorna uma GPU disponível com memória suficiente.
     * 
     * Busca uma GPU que esteja disponível e possua pelo menos
     * a quantidade de memória solicitada livre.
     * 
     * @param requiredMemory Memória mínima necessária em MB
     * @return GPU disponível com memória suficiente ou null
     */
    Gpu getAvailableGpuWithMemory(long requiredMemory);
    
    /**
     * Retorna a lista completa de GPUs gerenciadas pelo provisioner.
     * 
     * @return Lista imutável de todas as GPUs (alocadas e disponíveis)
     */
    List<Gpu> getGpuList();
    
    /**
     * Retorna a lista de GPUs disponíveis (não alocadas).
     * 
     * @return Lista de GPUs que podem ser alocadas
     */
    List<Gpu> getAvailableGpuList();
    
    /**
     * Retorna a lista de GPUs atualmente alocadas.
     * 
     * @return Lista de GPUs que estão em uso por VMs
     */
    List<Gpu> getAllocatedGpuList();
}
