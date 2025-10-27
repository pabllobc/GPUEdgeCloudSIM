
/*
 * Title:        GpuEdgeCloudSim - GpuEdgeServerManager
 * 
 * Description:  
 * GpuEdgeServerManager manages GPU-enabled edge infrastructure, creating
 * datacenters with GPU-equipped hosts and GPU-aware VMs.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2025, GpuEdgeCloudSim Project
 * Author:       Pabllo Borges Cardoso
 */

package edu.boun.edgecloudsim.edge_server;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;

/**
 * Manager for GPU-enabled edge server infrastructure.
 * 
 * This class extends EdgeServerManager to create and manage edge datacenters
 * with GPU hardware support. It parses XML configuration files to instantiate
 * GpuEdgeHosts with GPU resources and creates GpuEdgeVMs with GPU allocation.
 * 
 * Architecture:
 * - Creates GpuEdgeHosts with CPU + GPU resources
 * - Instantiates GpuEdgeVMs with optional GPU requirements
 * - Uses GpuEdgeVmAllocationPolicy_Custom for GPU-aware VM placement
 * - Integrates with EdgeCloudSim's datacenter framework
 * 
 * XML Configuration structure:
 * <pre>
 * {@code
 * <datacenters>
 *   <datacenter arch="x86" os="Linux" vmm="Xen">
 *     <location>
 *       <x_pos>0</x_pos>
 *       <y_pos>0</y_pos>
 *     </location>
 *     <costPerBw>0.1</costPerBw>
 *     <hosts>
 *       <host>
 *         <core>4</core>
 *         <mips>20000</mips>
 *         <ram>8192</ram>
 *         <storage>1000000</storage>
 *         <gpus>
 *           <gpu>
 *             <type>NVIDIA_T4</type>
 *             <cudaCores>2560</cudaCores>
 *             <gflops>8100</gflops>
 *             <memory>16384</memory>
 *             <bandwidth>320</bandwidth>
 *           </gpu>
 *         </gpus>
 *         <VMs>
 *           <VM vmm="Xen" requiresGpu="true">
 *             <core>2</core>
 *             <mips>10000</mips>
 *             <ram>2048</ram>
 *             <storage>10000</storage>
 *           </VM>
 *         </VMs>
 *       </host>
 *     </hosts>
 *   </datacenter>
 * </datacenters>
 * }
 * </pre>
 * 
 * Key features:
 * - XML-driven datacenter configuration
 * - Multi-host, multi-GPU support
 * - GPU-aware VM allocation
 * - Combined CPU+GPU utilization tracking
 * - Integration with EdgeCloudSim simulation framework
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 * @since GpuEdgeCloudSim v1.0
 */
public class GpuEdgeServerManager extends EdgeServerManager {
    
    /** Contador de IDs de hosts */
    private int hostIdCounter;
    
    /** Contador de IDs de VMs */
    private int vmIdCounter;
    
    /**
     * Construtor padrão.
     * Inicializa contadores de IDs.
     */
    public GpuEdgeServerManager() {
        super();
        this.hostIdCounter = 0;
        this.vmIdCounter = 0;
    }
    
    /**
     * Inicializa o gerenciador de servidores GPU.
     * Prepara estruturas de dados para datacenter e VM management.
     */
    @Override
    public void initialize() {
        SimLogger.printLine("GpuEdgeServerManager initializing...");
        // Inicializações adicionais podem ser adicionadas aqui
    }
    
    /**
     * Retorna a política de alocação de VMs para datacenters GPU.
     * Usa GpuEdgeVmAllocationPolicy_Custom para GPU-aware allocation.
     * 
     * @param list Lista de hosts do datacenter
     * @param dataCenterIndex Índice do datacenter
     * @return Política de alocação GPU-aware
     */
    @Override
    public VmAllocationPolicy getVmAllocationPolicy(List<? extends Host> list, 
                                                     int dataCenterIndex) {
        return new GpuEdgeVmAllocationPolicy_Custom(list, dataCenterIndex);
    }
    
    /**
     * Inicia os datacenters edge com suporte a GPU.
     * Lê configurações do edge_devices.xml e cria GpuEdgeHosts.
     * 
     * @throws Exception se houver erro na criação dos datacenters
     */
    @Override
    public void startDatacenters() throws Exception {
        SimLogger.printLine("Starting GPU-enabled edge datacenters...");
        
        // Lê configuração XML
        Document doc = SimSettings.getInstance().getEdgeDevicesDocument();
        NodeList datacenterList = doc.getElementsByTagName("datacenter");
        
        for (int i = 0; i < datacenterList.getLength(); i++) {
            Node datacenterNode = datacenterList.item(i);
            Element datacenterElement = (Element) datacenterNode;
            
            // Cria datacenter GPU
            Datacenter datacenter = createGpuDatacenter(i, datacenterElement);
            localDatacenters.add(datacenter);
        }
        
        SimLogger.printLine("GPU-enabled edge datacenters started successfully. " +
                          "Total datacenters: " + localDatacenters.size());
    }
    
    /**
     * Termina todos os datacenters edge.
     */
    @Override
    public void terminateDatacenters() {
        SimLogger.printLine("Terminating GPU-enabled edge datacenters...");
        
        for (Datacenter datacenter : localDatacenters) {
            datacenter.shutdownEntity();
        }
        
        localDatacenters.clear();
        SimLogger.printLine("GPU-enabled edge datacenters terminated.");
    }
    
    /**
     * Cria a lista de VMs edge com suporte a GPU.
     * Lê configurações do edge_devices.xml e aloca GPUs conforme necessário.
     * 
     * @param brokerId ID do broker
     */
    @Override
    public void createVmList(int brokerId) {
        SimLogger.printLine("Creating GPU-enabled VM list...");
        
        // Lê configuração XML
        Document doc = SimSettings.getInstance().getEdgeDevicesDocument();
        NodeList datacenterList = doc.getElementsByTagName("datacenter");
        
        for (int i = 0; i < datacenterList.getLength(); i++) {
            Node datacenterNode = datacenterList.item(i);
            Element datacenterElement = (Element) datacenterNode;
            NodeList hostNodeList = datacenterElement.getElementsByTagName("host");
            
            for (int j = 0; j < hostNodeList.getLength(); j++) {
                vmList.add(new ArrayList<EdgeVM>());
                
                Node hostNode = hostNodeList.item(j);
                Element hostElement = (Element) hostNode;
                NodeList vmNodeList = hostElement.getElementsByTagName("VM");
                
                for (int k = 0; k < vmNodeList.getLength(); k++) {
                    Node vmNode = vmNodeList.item(k);
                    Element vmElement = (Element) vmNode;
                    
                    // Lê configurações da VM
                    String vmm = vmElement.getAttribute("vmm");
                    boolean requiresGpu = Boolean.parseBoolean(
                        vmElement.getAttribute("requiresGpu"));
                    
                    int numOfCores = Integer.parseInt(
                        vmElement.getElementsByTagName("core").item(0).getTextContent());
                    double mips = Double.parseDouble(
                        vmElement.getElementsByTagName("mips").item(0).getTextContent());
                    int ram = Integer.parseInt(
                        vmElement.getElementsByTagName("ram").item(0).getTextContent());
                    long storage = Long.parseLong(
                        vmElement.getElementsByTagName("storage").item(0).getTextContent());
                    
                    // Cria GpuEdgeVM
                    GpuEdgeVM vm = new GpuEdgeVM(
                        vmIdCounter,
                        brokerId,
                        mips,
                        numOfCores,
                        ram,
                        100000, // Bandwidth padrão
                        storage,
                        vmm,
                        new org.cloudbus.cloudsim.CloudletSchedulerTimeShared(),
                        requiresGpu ? new GpuCloudletSchedulerTimeShared() : null,
                        requiresGpu
                    );
                    
                    vmList.get(hostIdCounter).add(vm);
                    vmIdCounter++;
                }
                
                hostIdCounter++;
            }
        }
        
        SimLogger.printLine("GPU-enabled VM list created. Total VMs: " + vmIdCounter);
    }
    
    /**
     * Calcula a utilização média (CPU + GPU) de todos os edge servers.
     * 
     * @return Utilização média em percentual (0-100%)
     */
    @Override
    public double getAvgUtilization() {
        double totalUtilization = 0.0;
        int numHosts = 0;
        
        for (Datacenter datacenter : localDatacenters) {
            List<? extends Host> hostList = datacenter.getHostList();
            
            for (Host host : hostList) {
                if (host instanceof GpuEdgeHost) {
                    GpuEdgeHost gpuHost = (GpuEdgeHost) host;
                    totalUtilization += gpuHost.getAvgUtilization();
                } else {
                    // Calculate CPU utilization for regular hosts
                    double cpuUtil = 0.0;
                    if (!host.getVmList().isEmpty()) {
                        double totalMips = host.getTotalMips();
                        double allocatedMips = 0.0;
                        for (Vm vm : host.getVmList()) {
                            allocatedMips += vm.getCurrentRequestedTotalMips();
                        }
                        cpuUtil = (allocatedMips / totalMips) * 100.0;
                    }
                    totalUtilization += cpuUtil;
                }
                numHosts++;
            }
        }
        
        return numHosts > 0 ? totalUtilization / numHosts : 0.0;
    }
    
    // ==================== MÉTODOS PRIVADOS ====================
    
    /**
     * Cria um datacenter edge com hosts GPU.
     * 
     * @param dataCenterIndex Índice do datacenter
     * @param datacenterElement Elemento XML com configurações
     * @return Datacenter criado
     * @throws Exception se houver erro na criação
     */
    private Datacenter createGpuDatacenter(int dataCenterIndex, 
                                            Element datacenterElement) throws Exception {
        
        String arch = datacenterElement.getAttribute("arch");
        String os = datacenterElement.getAttribute("os");
        String vmm = datacenterElement.getAttribute("vmm");
        double costPerBw = Double.parseDouble(
            datacenterElement.getElementsByTagName("costPerBw").item(0).getTextContent());
        
        // Cria lista de hosts GPU
        List<GpuEdgeHost> hostList = createGpuHosts(datacenterElement);
        
        // Cria características do datacenter
        double time_zone = 3.0; // Padrão
        double costPerSec = 0.0;
        double costPerMem = 0.0;
        double costPerStorage = 0.0;
        
        LinkedList<Storage> storageList = new LinkedList<>();
        
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
            arch, os, vmm, new ArrayList<>(hostList), time_zone, costPerSec, 
            costPerMem, costPerStorage, costPerBw);
        
        // Cria datacenter
        String datacenterName = "GpuEdgeDatacenter_" + dataCenterIndex;
        VmAllocationPolicy vmAllocationPolicy = getVmAllocationPolicy(hostList, dataCenterIndex);
        
        Datacenter datacenter = new Datacenter(
            datacenterName,
            characteristics,
            vmAllocationPolicy,
            storageList,
            0 // Scheduling interval
        );
        
        SimLogger.printLine("GPU Datacenter " + dataCenterIndex + " created with " + 
                          hostList.size() + " hosts");
        
        return datacenter;
    }
    
    /**
     * Cria lista de hosts GPU a partir do XML.
     * 
     * @param datacenterElement Elemento XML com configurações de hosts
     * @return Lista de GpuEdgeHosts criados
     */
    private List<GpuEdgeHost> createGpuHosts(Element datacenterElement) {
        List<GpuEdgeHost> hostList = new ArrayList<>();
        
        NodeList hostNodeList = datacenterElement.getElementsByTagName("host");
        
        for (int i = 0; i < hostNodeList.getLength(); i++) {
            Node hostNode = hostNodeList.item(i);
            Element hostElement = (Element) hostNode;
            
            // Lê configurações do host
            int numOfCores = Integer.parseInt(
                hostElement.getElementsByTagName("core").item(0).getTextContent());
            double mips = Double.parseDouble(
                hostElement.getElementsByTagName("mips").item(0).getTextContent());
            int ram = Integer.parseInt(
                hostElement.getElementsByTagName("ram").item(0).getTextContent());
            long storage = Long.parseLong(
                hostElement.getElementsByTagName("storage").item(0).getTextContent());
            long bandwidth = 100000; // Padrão
            
            // Cria lista de PEs (CPU cores)
            List<Pe> peList = createPeList(numOfCores, mips);
            
            // Cria lista de GPUs
            List<Gpu> gpuList = createGpuList(hostElement);
            
            // Cria provisionador GPU
            GpuProvisioner gpuProvisioner = new GpuProvisionerSimple(gpuList);
            
            // Cria GpuEdgeHost
            GpuEdgeHost host = new GpuEdgeHost(
                hostIdCounter,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bandwidth),
                storage,
                peList,
                new VmSchedulerTimeShared(peList),
                gpuList,
                gpuProvisioner
            );
            
            // Define localização (se disponível no XML)
            Element locationElement = (Element) datacenterElement.getElementsByTagName("location").item(0);
            if (locationElement != null) {
                int xPos = Integer.parseInt(
                    locationElement.getElementsByTagName("x_pos").item(0).getTextContent());
                int yPos = Integer.parseInt(
                    locationElement.getElementsByTagName("y_pos").item(0).getTextContent());
                // placeTypeIndex=0 (edge), servingWlanId=-1 (not applicable for edge hosts)
                host.setPlace(new Location(0, -1, xPos, yPos));
            }
            
            hostList.add(host);
            hostIdCounter++;
        }
        
        return hostList;
    }
    
    /**
     * Cria lista de GPUs a partir do XML.
     * 
     * @param hostElement Elemento XML com configurações de GPUs
     * @return Lista de GPUs criadas
     */
    private List<Gpu> createGpuList(Element hostElement) {
        List<Gpu> gpuList = new ArrayList<>();
        
        NodeList gpusNodeList = hostElement.getElementsByTagName("gpus");
        if (gpusNodeList.getLength() == 0) {
            return gpuList; // Sem GPUs
        }
        
        Element gpusElement = (Element) gpusNodeList.item(0);
        NodeList gpuNodeList = gpusElement.getElementsByTagName("gpu");
        
        for (int i = 0; i < gpuNodeList.getLength(); i++) {
            Node gpuNode = gpuNodeList.item(i);
            Element gpuElement = (Element) gpuNode;
            
            String type = gpuElement.getElementsByTagName("type").item(0).getTextContent();
            int cudaCores = Integer.parseInt(
                gpuElement.getElementsByTagName("cudaCores").item(0).getTextContent());
            double gflops = Double.parseDouble(
                gpuElement.getElementsByTagName("gflops").item(0).getTextContent());
            long memory = Long.parseLong(
                gpuElement.getElementsByTagName("memory").item(0).getTextContent());
            double bandwidth = Double.parseDouble(
                gpuElement.getElementsByTagName("bandwidth").item(0).getTextContent());
            
            Gpu gpu = new Gpu(i, type, cudaCores, gflops, memory, bandwidth);
            gpuList.add(gpu);
        }
        
        return gpuList;
    }
    
    /**
     * Cria lista de Processing Elements (CPU cores) a partir do XML.
     * 
     * @param numOfCores Número de cores
     * @param mips MIPS por core
     * @return Lista de PEs criados
     */
    private List<Pe> createPeList(int numOfCores, double mips) {
        List<Pe> peList = new ArrayList<>();
        
        for (int i = 0; i < numOfCores; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(mips)));
        }
        
        return peList;
    }
}
