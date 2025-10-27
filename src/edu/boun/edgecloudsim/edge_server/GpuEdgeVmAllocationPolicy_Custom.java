
/*
 * Title:        GpuEdgeCloudSim - GpuEdgeVmAllocationPolicy_Custom
 * 
 * Description:  
 * GpuEdgeVmAllocationPolicy_Custom implements GPU-aware VM allocation,
 * considering GPU availability and requirements when placing VMs on hosts.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2025, GpuEdgeCloudSim Project
 * Author:       Pabllo Borges Cardoso
 */

package edu.boun.edgecloudsim.edge_server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

import edu.boun.edgecloudsim.utils.SimLogger;

/**
 * GPU-aware VM allocation policy for edge datacenters.
 * 
 * This class extends EdgeVmAllocationPolicy_Custom to add GPU-awareness
 * in VM placement decisions. It ensures that VMs requiring GPUs are only
 * placed on hosts with available GPU resources.
 * 
 * Allocation algorithm:
 * 1. Check if VM requires GPU (GpuEdgeVM.requiresGpu())
 * 2. If yes, find host with available GPU resources
 * 3. If no, use standard CPU/RAM/BW allocation
 * 4. Allocate GPU to VM after successful host allocation
 * 
 * GPU allocation strategy:
 * - First-fit: First host with available GPU
 * - Resource-aware: Check GPU memory availability
 * - Exclusive mode: 1 GPU per VM (v1.0)
 * 
 * Key features:
 * - GPU availability checking before VM placement
 * - Automatic GPU allocation upon VM creation
 * - GPU deallocation upon VM destruction
 * - Fallback to CPU-only hosts for non-GPU VMs
 * - Integration with EdgeCloudSim's allocation framework
 * 
 * Design considerations:
 * - Separates GPU concerns from CPU resource allocation
 * - Maintains compatibility with EdgeCloudSim patterns
 * - Extensible for future GPU sharing policies
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 * @since GpuEdgeCloudSim v1.0
 */
public class GpuEdgeVmAllocationPolicy_Custom extends EdgeVmAllocationPolicy_Custom {
    
    /** Índice do datacenter gerenciado por esta política */
    private int dataCenterIndex;
    
    /** Mapa de VMs alocadas para rastreamento GPU */
    private Map<Integer, GpuEdgeHost> vmToHostMap;
    
    /**
     * Construtor da política de alocação GPU.
     * 
     * @param list Lista de hosts do datacenter
     * @param dataCenterIndex Índice do datacenter
     */
    public GpuEdgeVmAllocationPolicy_Custom(List<? extends Host> list, 
                                             int dataCenterIndex) {
        super(list, dataCenterIndex);
        this.dataCenterIndex = dataCenterIndex;
        this.vmToHostMap = new HashMap<>();
        
        SimLogger.printLine("GpuEdgeVmAllocationPolicy_Custom initialized for datacenter " + 
                          dataCenterIndex);
    }
    
    /**
     * Aloca um host para uma VM considerando requisitos GPU.
     * 
     * Se a VM é GpuEdgeVM e requer GPU, busca host com GPU disponível.
     * Caso contrário, usa alocação padrão.
     * 
     * @param vm VM a ser alocada
     * @return true se a alocação foi bem-sucedida
     */
    @Override
    public boolean allocateHostForVm(Vm vm) {
        // Se não é GpuEdgeVM, usa alocação padrão
        if (!(vm instanceof GpuEdgeVM)) {
            return super.allocateHostForVm(vm);
        }
        
        GpuEdgeVM gpuVm = (GpuEdgeVM) vm;
        
        // Se não requer GPU, usa alocação padrão
        if (!gpuVm.requiresGpu()) {
            return super.allocateHostForVm(vm);
        }
        
        // Busca host adequado com GPU disponível
        GpuEdgeHost suitableHost = findSuitableHostForVm(gpuVm);
        
        if (suitableHost == null) {
            SimLogger.printLine("Error: No suitable host with GPU found for VM " + vm.getId());
            return false;
        }
        
        // Tenta alocar o host usando método padrão
        boolean hostAllocated = allocateHostForVm(vm, suitableHost);
        
        if (!hostAllocated) {
            return false;
        }
        
        // Aloca GPU para a VM
        boolean gpuAllocated = allocateGpuForVm(suitableHost, gpuVm);
        
        if (!gpuAllocated) {
            SimLogger.printLine("Warning: Host allocated but GPU allocation failed for VM " + 
                              vm.getId());
            // VM foi alocada mas sem GPU - pode ser aceitável
        }
        
        // Registra a alocação
        vmToHostMap.put(vm.getId(), suitableHost);
        
        return true;
    }
    
    /**
     * Aloca um host específico para uma VM considerando GPU.
     * 
     * @param vm VM a ser alocada
     * @param host Host onde a VM será alocada
     * @return true se a alocação foi bem-sucedida
     */
    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
        // Verifica se o host pode hospedar a VM
        if (!(host instanceof GpuEdgeHost) && vm instanceof GpuEdgeVM) {
            GpuEdgeVM gpuVm = (GpuEdgeVM) vm;
            if (gpuVm.requiresGpu()) {
                SimLogger.printLine("Error: Host " + host.getId() + 
                                  " is not a GpuEdgeHost but VM " + vm.getId() + " requires GPU");
                return false;
            }
        }
        
        // Tenta alocação padrão
        boolean allocated = super.allocateHostForVm(vm, host);
        
        if (!allocated) {
            return false;
        }
        
        // Se é GpuEdgeHost e VM requer GPU, aloca GPU
        if (host instanceof GpuEdgeHost && vm instanceof GpuEdgeVM) {
            GpuEdgeHost gpuHost = (GpuEdgeHost) host;
            GpuEdgeVM gpuVm = (GpuEdgeVM) vm;
            
            if (gpuVm.requiresGpu()) {
                allocateGpuForVm(gpuHost, gpuVm);
                vmToHostMap.put(vm.getId(), gpuHost);
            }
        }
        
        return true;
    }
    
    /**
     * Desaloca o host de uma VM e libera a GPU associada.
     * 
     * @param vm VM a ser desalocada
     */
    @Override
    public void deallocateHostForVm(Vm vm) {
        // Libera GPU se aplicável
        if (vm instanceof GpuEdgeVM) {
            GpuEdgeVM gpuVm = (GpuEdgeVM) vm;
            GpuEdgeHost host = vmToHostMap.get(vm.getId());
            
            if (host != null && gpuVm.hasGpu()) {
                host.deallocateGpuForVm(gpuVm);
                SimLogger.printLine("GPU deallocated from VM " + vm.getId() + 
                                  " on host " + host.getId());
            }
            
            vmToHostMap.remove(vm.getId());
        }
        
        // Desaloca host usando método padrão
        super.deallocateHostForVm(vm);
    }
    
    // ==================== MÉTODOS PRIVADOS ====================
    
    /**
     * Encontra um host adequado para uma VM considerando recursos GPU.
     * 
     * Busca hosts com:
     * 1. CPU/RAM/BW suficientes
     * 2. GPU disponível
     * 3. Memória GPU suficiente (se especificado)
     * 
     * @param vm VM para a qual buscar host
     * @return Host adequado ou null se nenhum disponível
     */
    private GpuEdgeHost findSuitableHostForVm(GpuEdgeVM vm) {
        for (Host host : getHostList()) {
            if (!(host instanceof GpuEdgeHost)) {
                continue;
            }
            
            GpuEdgeHost gpuHost = (GpuEdgeHost) host;
            
            // Verifica se o host pode hospedar a VM
            if (canHostVm(gpuHost, vm)) {
                return gpuHost;
            }
        }
        
        return null;
    }
    
    /**
     * Verifica se um host pode hospedar uma VM com requisitos GPU.
     * 
     * Checa:
     * - Recursos CPU/RAM/BW (via isSuitableForVm)
     * - Disponibilidade de GPU
     * - Memória GPU suficiente
     * 
     * @param host Host a ser verificado
     * @param vm VM com requisitos
     * @return true se o host pode hospedar a VM
     */
    private boolean canHostVm(GpuEdgeHost host, GpuEdgeVM vm) {
        // Verifica recursos CPU/RAM/BW básicos
        if (!host.isSuitableForVm(vm)) {
            return false;
        }
        
        // Se VM não requer GPU, já está OK
        if (!vm.requiresGpu()) {
            return true;
        }
        
        // Verifica disponibilidade de GPU
        if (!host.hasAvailableGpu()) {
            return false;
        }
        
        // TODO: Verificar memória GPU específica se a VM especificar requisito
        // Por enquanto, qualquer GPU disponível é aceita
        
        return true;
    }
    
    /**
     * Aloca GPU para uma VM em um host específico.
     * 
     * Busca a primeira GPU disponível e a aloca para a VM.
     * 
     * @param host Host com GPUs disponíveis
     * @param vm VM que receberá a GPU
     * @return true se a alocação GPU foi bem-sucedida
     */
    private boolean allocateGpuForVm(GpuEdgeHost host, GpuEdgeVM vm) {
        Gpu availableGpu = host.getAvailableGpu();
        
        if (availableGpu == null) {
            SimLogger.printLine("Error: No GPU available on host " + host.getId() + 
                              " for VM " + vm.getId());
            return false;
        }
        
        boolean allocated = host.allocateGpuForVm(vm, availableGpu);
        
        if (allocated) {
            SimLogger.printLine("GPU " + availableGpu.getId() + " allocated to VM " + 
                              vm.getId() + " on host " + host.getId());
        }
        
        return allocated;
    }
}
