# 📐 GpuEdgeCloudSim - Fase 1: Análise Arquitetural Profunda

**Autor:** Pabllo Borges Cardoso  
**Data:** 23 de Outubro de 2025  
**Versão:** 1.0  
**Projeto:** GpuEdgeCloudSim - Extensão do EdgeCloudSim com Suporte a GPU

---

## 📋 Sumário Executivo

Este documento apresenta a análise arquitetural profunda do **EdgeCloudSim**, focando especialmente no pacote `edge_server/` e nos mecanismos de extensibilidade do framework. O objetivo é fornecer uma base sólida para a implementação das classes GPU (GpuEdgeHost, GpuEdgeVM, GpuTask) na Fase 2 do projeto GpuEdgeCloudSim.

### Principais Descobertas

1. **Arquitetura Modular Extensível**: O EdgeCloudSim utiliza o padrão Factory (ScenarioFactory) que permite extensões sem modificar o core.
2. **Hierarquia Clara de Classes**: As classes de servidor seguem uma estrutura bem definida (Manager → Host → VM).
3. **Pontos de Extensão Identificados**: 5 pontos críticos onde as classes GPU podem ser inseridas.
4. **Fluxo de Eventos Centralizado**: SimManager coordena todo o ciclo de vida da simulação via eventos discretos.

---

## 1. 🏗️ Arquitetura Geral do EdgeCloudSim

### 1.1 Estrutura de Pacotes

```
edu.boun.edgecloudsim/
├── core/                    → Núcleo do simulador (SimManager, ScenarioFactory, SimSettings)
├── edge_server/             → Gerenciamento de recursos edge (FOCO DESTA ANÁLISE)
├── cloud_server/            → Gerenciamento de recursos cloud
├── edge_client/             → Dispositivos móveis e brokers
│   └── mobile_processing_unit/ → Processamento local em dispositivos
├── edge_orchestrator/       → Decisões de offloading
├── network/                 → Modelos de rede (latência, banda)
├── mobility/                → Modelos de mobilidade
├── task_generator/          → Geração de carga de trabalho
└── utils/                   → Utilitários (Location, SimLogger, etc.)
```

### 1.2 Princípios Arquiteturais

**🎯 Filosofia de Design (conforme PERSONA.MD):**
> "Não modificar o core (`core/`). Estender via `ScenarioFactory`."

**Características Principais:**
- **Simulação por Eventos Discretos (DES)**: Baseado em CloudSim 7.0.0-alpha
- **Modularidade**: Cada camada (Edge, Cloud, Mobile) é intercambiável
- **Configuração XML**: Especificação de recursos via arquivos XML
- **Factory Pattern**: Ponto central de extensibilidade

---

## 2. 📦 Análise Detalhada do Pacote `edge_server/`

### 2.1 Visão Geral das Classes

O pacote `edge_server/` contém **5 classes fundamentais**:

| Classe | Tipo | Responsabilidade | LOC |
|--------|------|------------------|-----|
| `EdgeServerManager` | Abstract | Define contrato para gerenciamento de edge servers | 127 |
| `DefaultEdgeServerManager` | Concrete | Implementação padrão do gerenciamento | 298 |
| `EdgeHost` | Concrete | Host físico com localização geográfica | 81 |
| `EdgeVM` | Concrete | VM com tipo e reconfiguração de MIPS | 95 |
| `EdgeVmAllocationPolicy_Custom` | Concrete | Política de alocação baseada em XML | 231 |

### 2.2 Diagrama de Classes (ASCII)

```
┌─────────────────────────────────────────────────────────────────┐
│                      EdgeServerManager                          │
│  (abstract)                                                     │
├─────────────────────────────────────────────────────────────────┤
│  # localDatacenters: List<Datacenter>                          │
│  # vmList: List<List<EdgeVM>>                                  │
├─────────────────────────────────────────────────────────────────┤
│  + getVmList(hostId: int): List<EdgeVM>                        │
│  + getDatacenterList(): List<Datacenter>                       │
│  + initialize(): void [abstract]                                │
│  + getVmAllocationPolicy(...): VmAllocationPolicy [abstract]   │
│  + startDatacenters(): void [abstract]                          │
│  + terminateDatacenters(): void [abstract]                      │
│  + createVmList(brokerId: int): void [abstract]                │
│  + getAvgUtilization(): double [abstract]                       │
└─────────────────────────────────────────────────────────────────┘
                              △
                              │ extends
                              │
┌─────────────────────────────────────────────────────────────────┐
│               DefaultEdgeServerManager                          │
│  (concrete)                                                     │
├─────────────────────────────────────────────────────────────────┤
│  - hostIdCounter: int                                          │
├─────────────────────────────────────────────────────────────────┤
│  + initialize(): void                                           │
│  + getVmAllocationPolicy(...): VmAllocationPolicy              │
│  + startDatacenters(): void                                     │
│  + terminateDatacenters(): void                                 │
│  + createVmList(brokerId: int): void                           │
│  + getAvgUtilization(): double                                  │
│  - createDatacenter(...): Datacenter                            │
│  - createHosts(...): List<EdgeHost>                            │
└─────────────────────────────────────────────────────────────────┘
                    │ creates
                    ├──────────────┐
                    ▼              ▼
┌──────────────────────────┐  ┌────────────────────────┐
│      EdgeHost            │  │      EdgeVM            │
│   (extends Host)         │  │   (extends Vm)         │
├──────────────────────────┤  ├────────────────────────┤
│  - location: Location    │  │  - type: VM_TYPES      │
├──────────────────────────┤  ├────────────────────────┤
│  + setPlace(Location)    │  │  + getVmType()         │
│  + getLocation()         │  │  + reconfigureMips()   │
└──────────────────────────┘  └────────────────────────┘
```

### 2.3 Análise Classe por Classe

#### 2.3.1 `EdgeServerManager` (Abstract)

**Propósito:** Contrato para gerenciamento da infraestrutura edge.

**Atributos Principais:**
```java
protected List<Datacenter> localDatacenters;  // Lista de datacenters edge
protected List<List<EdgeVM>> vmList;          // VMs organizadas por hostId
```

**Métodos Críticos:**
- `initialize()`: Inicialização do gerenciador
- `startDatacenters()`: Cria e inicia datacenters CloudSim
- `createVmList(brokerId)`: Cria VMs e associa ao broker
- `getAvgUtilization()`: Calcula utilização média de CPU

**🔑 Ponto de Extensão GPU:**
> Para criar `GpuEdgeServerManager`, estenda esta classe abstrata e implemente os métodos criando `GpuEdgeHost` e `GpuEdgeVM`.

---

#### 2.3.2 `DefaultEdgeServerManager` (Concrete)

**Propósito:** Implementação padrão do gerenciamento edge.

**Fluxo de Criação de Infraestrutura:**

```
startDatacenters()
    │
    ├─→ Lê edge_devices.xml
    │
    ├─→ Para cada <datacenter>:
    │       │
    │       ├─→ createHosts()
    │       │      │
    │       │      ├─→ Extrai localização (x, y, wlan_id, attractiveness)
    │       │      │
    │       │      ├─→ Para cada <host>:
    │       │      │       ├─→ Cria PEs (Processing Elements)
    │       │      │       ├─→ Cria EdgeHost com provisioners
    │       │      │       └─→ Define Location
    │       │      │
    │       │      └─→ Retorna List<EdgeHost>
    │       │
    │       ├─→ createDatacenter()
    │       │      │
    │       │      ├─→ Cria DatacenterCharacteristics
    │       │      ├─→ Cria VmAllocationPolicy (EdgeVmAllocationPolicy_Custom)
    │       │      └─→ Cria Datacenter CloudSim
    │       │
    │       └─→ Adiciona à localDatacenters
    │
    └─→ Retorna

createVmList(brokerId)
    │
    ├─→ Lê edge_devices.xml
    │
    ├─→ Para cada <datacenter>:
    │       │
    │       └─→ Para cada <host>:
    │               │
    │               └─→ Para cada <VM>:
    │                       │
    │                       ├─→ Extrai configurações (core, mips, ram, storage)
    │                       ├─→ Calcula bandwidth proporcional
    │                       ├─→ Cria EdgeVM com CloudletSchedulerTimeShared
    │                       └─→ Adiciona à vmList[hostId]
    │
    └─→ Retorna
```

**🔍 Observações Importantes:**

1. **Bandwidth Proporcional** (linha 144):
   ```java
   long bandwidth = SimSettings.getInstance().getWlanBandwidth() / 
                    (hostNodeList.getLength() + vmNodeList.getLength());
   ```
   → Para GPUs, considerar banda adicional para transferência de dados.

2. **VmScheduler:** Usa `VmSchedulerSpaceShared` (linha 287)
   → Para GPUs, pode ser necessário scheduler customizado.

3. **CloudletScheduler:** Usa `CloudletSchedulerTimeShared` (linha 147)
   → Para GPUs, considerar time-slicing de tarefas GPU.

---

#### 2.3.3 `EdgeHost` (Concrete)

**Propósito:** Host físico edge com informação de localização.

**Extensão do CloudSim Host:**
```java
public class EdgeHost extends Host {
    private Location location;  // ÚNICA ADIÇÃO: localização geográfica
    
    public void setPlace(Location _location) { ... }
    public Location getLocation() { ... }
}
```

**Classe Location** (utils package):
```java
public class Location {
    private int placeTypeIndex;     // Tipo de local (0-2)
    private int wlanId;             // ID da WLAN
    private int xPos;               // Coordenada X
    private int yPos;               // Coordenada Y
    // ... métodos
}
```

**🔑 Ponto de Extensão GPU:**
> Criar `GpuEdgeHost extends EdgeHost` e adicionar:
> - `List<Gpu> gpuList` (lista de GPUs físicas)
> - `GpuProvisioner gpuProvisioner` (alocador de GPU para VMs)
> - Métodos: `getGpuList()`, `getAvailableGpu()`, etc.

---

#### 2.3.4 `EdgeVM` (Concrete)

**Propósito:** VM edge com tipo e reconfiguração dinâmica de MIPS.

**Características Principais:**

1. **Tipo de VM:**
   ```java
   private SimSettings.VM_TYPES type = SimSettings.VM_TYPES.EDGE_VM;
   ```

2. **Reconfiguração Dinâmica:**
   ```java
   public void reconfigureMips(double mips) {
       super.setMips(mips);
       super.getHost().getVmScheduler().deallocatePesForVm(this);
       
       List<Double> mipsShareAllocated = new ArrayList<>();
       for(int i=0; i<getNumberOfPes(); i++)
           mipsShareAllocated.add(mips);
       
       super.getHost().getVmScheduler().allocatePesForVm(this, mipsShareAllocated);
   }
   ```

**🔑 Ponto de Extensão GPU:**
> Criar `GpuEdgeVM extends EdgeVM` e adicionar:
> - `Gpu allocatedGpu` (GPU alocada para esta VM)
> - `GpuCloudletScheduler gpuCloudletScheduler` (scheduler para tarefas GPU)
> - Métodos: `getGpu()`, `setGpu()`, `getGpuUtilization()`, etc.

---

#### 2.3.5 `EdgeVmAllocationPolicy_Custom` (Concrete)

**Propósito:** Política de alocação baseada em configuração XML.

**Algoritmo de Alocação:**

```
allocateHostForVm(vm)
    │
    ├─→ Verifica se VM já está alocada
    ├─→ Verifica se é EdgeVM
    │
    ├─→ Percorre edge_devices.xml:
    │       │
    │       └─→ Conta VMs até encontrar vm.getId()
    │               │
    │               ├─→ Armazena dataCenterIndex
    │               ├─→ Armazena hostIndex
    │               └─→ vmFound = true
    │
    ├─→ Se encontrado E pertence a este datacenter:
    │       │
    │       ├─→ host = getHostList().get(hostIndex)
    │       ├─→ result = host.vmCreate(vm)
    │       │
    │       └─→ Se sucesso:
    │               ├─→ vmTable.put(vm.uid, host)
    │               ├─→ createdVmNum++
    │               └─→ Log "VM alocada"
    │
    └─→ Retorna result
```

**🔑 Ponto de Extensão GPU:**
> Criar `GpuEdgeVmAllocationPolicy_Custom extends EdgeVmAllocationPolicy_Custom`:
> - Verificar disponibilidade de GPU antes de alocar VM
> - Implementar lógica de alocação de GPU física para VM
> - Considerar requisitos de memória GPU

---

## 3. 🔧 Análise do ScenarioFactory Pattern

### 3.1 Interface ScenarioFactory

**Propósito:** Ponto central de extensibilidade do EdgeCloudSim.

```java
public interface ScenarioFactory {
    LoadGeneratorModel getLoadGeneratorModel();
    EdgeOrchestrator getEdgeOrchestrator();
    MobilityModel getMobilityModel();
    NetworkModel getNetworkModel();
    EdgeServerManager getEdgeServerManager();        // ← FOCO GPU
    CloudServerManager getCloudServerManager();
    MobileServerManager getMobileServerManager();
    MobileDeviceManager getMobileDeviceManager();
}
```

### 3.2 Padrão de Uso

**Exemplo de Implementação (tutorial1):**

```java
public class SampleScenarioFactory implements ScenarioFactory {
    private int numOfMobileDevice;
    private double simulationTime;
    private String orchestratorPolicy;
    private String simScenario;
    
    @Override
    public EdgeServerManager getEdgeServerManager() {
        return new DefaultEdgeServerManager();  // ← Retorna implementação padrão
    }
    
    // ... outros métodos factory
}
```

**🎯 Aplicação para GpuEdgeCloudSim:**

```java
public class GpuScenarioFactory implements ScenarioFactory {
    @Override
    public EdgeServerManager getEdgeServerManager() {
        return new GpuEdgeServerManager();  // ← Retorna implementação GPU
    }
    
    // ... pode manter outros padrões ou customizar
}
```

### 3.3 Fluxo de Criação de Componentes

```
MainApp.main()
    │
    ├─→ SimSettings.initialize(config, devices, apps)
    │
    ├─→ Para cada iteração:
    │       │
    │       └─→ Para cada (devices, scenario, policy):
    │               │
    │               ├─→ CloudSim.init()
    │               │
    │               ├─→ factory = new SampleScenarioFactory(...)
    │               │
    │               ├─→ manager = new SimManager(factory, ...)
    │               │       │
    │               │       └─→ SimManager.constructor:
    │               │               │
    │               │               ├─→ loadGenerator = factory.getLoadGeneratorModel()
    │               │               ├─→ mobilityModel = factory.getMobilityModel()
    │               │               ├─→ networkModel = factory.getNetworkModel()
    │               │               ├─→ edgeOrchestrator = factory.getEdgeOrchestrator()
    │               │               ├─→ edgeServerManager = factory.getEdgeServerManager()  ← CRIA EDGE INFRA
    │               │               ├─→ cloudServerManager = factory.getCloudServerManager()
    │               │               ├─→ mobileServerManager = factory.getMobileServerManager()
    │               │               └─→ mobileDeviceManager = factory.getMobileDeviceManager()
    │               │
    │               ├─→ manager.startSimulation()
    │               │       │
    │               │       ├─→ edgeServerManager.startDatacenters()      ← CRIA DATACENTERS
    │               │       ├─→ edgeServerManager.createVmList(brokerId)  ← CRIA VMs
    │               │       ├─→ cloudServerManager.startDatacenters()
    │               │       ├─→ cloudServerManager.createVmList(brokerId)
    │               │       ├─→ mobileServerManager.startDatacenters()
    │               │       ├─→ mobileServerManager.createVmList(brokerId)
    │               │       └─→ CloudSim.startSimulation()
    │               │
    │               └─→ SimLogger.simStarted()
    │
    └─→ FIM
```

**🔑 Ponto de Injeção GPU:**
> No método `factory.getEdgeServerManager()`, retornar uma instância de `GpuEdgeServerManager` que criará toda a hierarquia GPU (GpuEdgeHost → GpuEdgeVM).

---

## 4. ⚙️ Análise do SimManager (Fluxo de Eventos)

### 4.1 Estrutura do SimManager

**Herança:** `SimManager extends SimEntity` (CloudSim)

**Eventos Discretos Definidos:**
```java
private static final int CREATE_TASK = 0;        // Criação de tarefa
private static final int CHECK_ALL_VM = 1;       // Verificação de VMs
private static final int GET_LOAD_LOG = 2;       // Log de carga
private static final int PRINT_PROGRESS = 3;     // Progresso
private static final int STOP_SIMULATION = 4;    // Término
```

### 4.2 Ciclo de Vida da Simulação

```
SimManager.constructor()
    │
    ├─→ Cria e inicializa todos os módulos via ScenarioFactory
    │
    ├─→ SimManager.startSimulation()
    │       │
    │       ├─→ edgeServerManager.startDatacenters()
    │       ├─→ edgeServerManager.createVmList(brokerId)
    │       ├─→ cloudServerManager.startDatacenters()
    │       ├─→ cloudServerManager.createVmList(brokerId)
    │       ├─→ mobileServerManager.startDatacenters()
    │       ├─→ mobileServerManager.createVmList(brokerId)
    │       │
    │       └─→ CloudSim.startSimulation()
    │               │
    │               └─→ Dispara SimManager.startEntity()
    │
    └─→ SimManager.startEntity()
            │
            ├─→ Submete VMs ao MobileDeviceManager:
            │       ├─→ Edge VMs
            │       ├─→ Cloud VMs
            │       └─→ Mobile VMs
            │
            ├─→ Agenda eventos CREATE_TASK para cada tarefa
            │
            ├─→ Agenda eventos periódicos:
            │       ├─→ CHECK_ALL_VM (5s)
            │       ├─→ PRINT_PROGRESS (simTime/100)
            │       ├─→ GET_LOAD_LOG (interval configurável)
            │       └─→ STOP_SIMULATION (simTime)
            │
            └─→ Loop de Eventos (CloudSim)
                    │
                    └─→ SimManager.processEvent(ev)
                            │
                            ├─→ CREATE_TASK:
                            │       └─→ mobileDeviceManager.submitTask(task)
                            │
                            ├─→ CHECK_ALL_VM:
                            │       └─→ Verifica se todas VMs foram criadas
                            │
                            ├─→ GET_LOAD_LOG:
                            │       ├─→ SimLogger.addVmUtilizationLog(...)
                            │       └─→ Reagenda próximo log
                            │
                            ├─→ PRINT_PROGRESS:
                            │       ├─→ Exibe % de progresso
                            │       └─→ Reagenda
                            │
                            └─→ STOP_SIMULATION:
                                    ├─→ CloudSim.terminateSimulation()
                                    └─→ SimLogger.simStopped()
```

### 4.3 Eventos Customizados

**🔑 Para adicionar suporte a eventos GPU:**

1. **Definir novos tipos de evento:**
   ```java
   private static final int GPU_TASK_CREATED = 10;
   private static final int GPU_TASK_COMPLETED = 11;
   private static final int CHECK_GPU_UTILIZATION = 12;
   ```

2. **Estender processEvent():**
   ```java
   @Override
   public void processEvent(SimEvent ev) {
       synchronized(this) {
           switch(ev.getTag()) {
               // ... casos existentes
               case GPU_TASK_CREATED:
                   handleGpuTaskCreation((GpuTask) ev.getData());
                   break;
               // ...
           }
       }
   }
   ```

---

## 5. 🎯 Pontos de Extensão Identificados para GPU

### 5.1 Resumo dos 5 Pontos Críticos

| # | Ponto de Extensão | Localização | Estratégia |
|---|-------------------|-------------|------------|
| **1** | **GpuEdgeServerManager** | `edge_server/` | Estender `EdgeServerManager` |
| **2** | **GpuEdgeHost** | `edge_server/` | Estender `EdgeHost` |
| **3** | **GpuEdgeVM** | `edge_server/` | Estender `EdgeVM` |
| **4** | **GpuTask** | `edge_client/` | Estender `Task` |
| **5** | **GpuScenarioFactory** | `applications/` | Implementar `ScenarioFactory` |

### 5.2 Detalhamento de Cada Ponto

#### **Ponto 1: GpuEdgeServerManager**

**Localização:** `/home/ubuntu/EdgeCloudSim/src/edu/boun/edgecloudsim/edge_server/GpuEdgeServerManager.java`

**Estrutura Proposta:**

```java
package edu.boun.edgecloudsim.edge_server;

import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.Host;
import java.util.List;

/**
 * Gerenciador de servidores edge com suporte a GPU.
 * Estende EdgeServerManager para criar infraestrutura GPU+CPU.
 */
public class GpuEdgeServerManager extends EdgeServerManager {
    private int hostIdCounter;
    
    @Override
    public void initialize() {
        hostIdCounter = 0;
    }
    
    @Override
    public VmAllocationPolicy getVmAllocationPolicy(List<? extends Host> list, int dataCenterIndex) {
        return new GpuEdgeVmAllocationPolicy_Custom(list, dataCenterIndex);
    }
    
    @Override
    public void startDatacenters() throws Exception {
        // Similar ao DefaultEdgeServerManager, mas cria GpuEdgeHost
        Document doc = SimSettings.getInstance().getEdgeDevicesDocument();
        NodeList datacenterList = doc.getElementsByTagName("datacenter");
        
        for (int i = 0; i < datacenterList.getLength(); i++) {
            Element datacenterElement = (Element) datacenterList.item(i);
            localDatacenters.add(createGpuDatacenter(i, datacenterElement));
        }
    }
    
    @Override
    public void createVmList(int brokerId) {
        // Similar ao DefaultEdgeServerManager, mas cria GpuEdgeVM
        // Lê configurações GPU do XML e aloca GPUs para VMs
    }
    
    @Override
    public double getAvgUtilization() {
        // Calcular utilização CPU + GPU
        double totalCpuUtilization = 0;
        double totalGpuUtilization = 0;
        // ... implementação
    }
    
    private Datacenter createGpuDatacenter(int index, Element datacenterElement) throws Exception {
        // Cria datacenter com GpuEdgeHosts
        List<GpuEdgeHost> hostList = createGpuHosts(datacenterElement);
        // ... resto da criação do datacenter
    }
    
    private List<GpuEdgeHost> createGpuHosts(Element datacenterElement) {
        // Similar a createHosts() mas cria GpuEdgeHost com lista de GPUs
        List<GpuEdgeHost> hostList = new ArrayList<>();
        
        // Extrai localização
        Element location = (Element) datacenterElement.getElementsByTagName("location").item(0);
        // ...
        
        NodeList hostNodeList = datacenterElement.getElementsByTagName("host");
        for (int j = 0; j < hostNodeList.getLength(); j++) {
            Element hostElement = (Element) hostNodeList.item(j);
            
            // Cria PEs normais (CPU)
            List<Pe> peList = createPeList(hostElement);
            
            // NOVO: Cria lista de GPUs
            List<Gpu> gpuList = createGpuList(hostElement);
            
            // Cria GpuEdgeHost
            GpuEdgeHost host = new GpuEdgeHost(
                hostIdCounter,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bandwidth),
                storage,
                peList,
                new VmSchedulerSpaceShared(peList),
                gpuList,
                new GpuProvisionerSimple(gpuList)  // ← NOVO
            );
            
            host.setPlace(new Location(...));
            hostList.add(host);
            hostIdCounter++;
        }
        
        return hostList;
    }
    
    private List<Gpu> createGpuList(Element hostElement) {
        // Lê <gpu> elements do XML e cria objetos Gpu
        List<Gpu> gpuList = new ArrayList<>();
        NodeList gpuNodeList = hostElement.getElementsByTagName("gpu");
        
        for (int i = 0; i < gpuNodeList.getLength(); i++) {
            Element gpuElement = (Element) gpuNodeList.item(i);
            
            int gpuId = i;
            int cudaCores = Integer.parseInt(gpuElement.getElementsByTagName("cuda_cores").item(0).getTextContent());
            double gflops = Double.parseDouble(gpuElement.getElementsByTagName("gflops").item(0).getTextContent());
            long gpuMemory = Long.parseLong(gpuElement.getElementsByTagName("gpu_memory").item(0).getTextContent());
            
            Gpu gpu = new Gpu(gpuId, cudaCores, gflops, gpuMemory);
            gpuList.add(gpu);
        }
        
        return gpuList;
    }
}
```

---

#### **Ponto 2: GpuEdgeHost**

**Localização:** `/home/ubuntu/EdgeCloudSim/src/edu/boun/edgecloudsim/edge_server/GpuEdgeHost.java`

**Estrutura Proposta:**

```java
package edu.boun.edgecloudsim.edge_server;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import java.util.List;

/**
 * Host edge com suporte a GPUs físicas.
 * Estende EdgeHost para adicionar gerenciamento de recursos GPU.
 */
public class GpuEdgeHost extends EdgeHost {
    private List<Gpu> gpuList;              // Lista de GPUs físicas
    private GpuProvisioner gpuProvisioner;  // Alocador de GPU para VMs
    
    public GpuEdgeHost(
            int id,
            RamProvisioner ramProvisioner,
            BwProvisioner bwProvisioner,
            long storage,
            List<? extends Pe> peList,
            VmScheduler vmScheduler,
            List<Gpu> gpuList,
            GpuProvisioner gpuProvisioner) {
        
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
        
        this.gpuList = gpuList;
        this.gpuProvisioner = gpuProvisioner;
    }
    
    /**
     * Retorna a lista de GPUs deste host.
     */
    public List<Gpu> getGpuList() {
        return gpuList;
    }
    
    /**
     * Retorna o provisionador de GPU.
     */
    public GpuProvisioner getGpuProvisioner() {
        return gpuProvisioner;
    }
    
    /**
     * Aloca GPU para uma VM.
     */
    public boolean allocateGpuForVm(GpuEdgeVM vm, Gpu gpu) {
        return gpuProvisioner.allocateGpuForVm(vm, gpu);
    }
    
    /**
     * Desaloca GPU de uma VM.
     */
    public void deallocateGpuForVm(GpuEdgeVM vm) {
        gpuProvisioner.deallocateGpuForVm(vm);
    }
    
    /**
     * Verifica se há GPU disponível.
     */
    public boolean hasAvailableGpu() {
        return gpuProvisioner.hasAvailableGpu();
    }
    
    /**
     * Retorna a GPU disponível com maior capacidade.
     */
    public Gpu getAvailableGpu() {
        return gpuProvisioner.getAvailableGpu();
    }
    
    /**
     * Calcula utilização média das GPUs.
     */
    public double getAvgGpuUtilization() {
        double totalUtilization = 0;
        for (Gpu gpu : gpuList) {
            totalUtilization += gpu.getUtilization();
        }
        return totalUtilization / gpuList.size();
    }
}
```

**Classe Auxiliar: Gpu**

```java
package edu.boun.edgecloudsim.edge_server;

/**
 * Representa uma GPU física em um EdgeHost.
 */
public class Gpu {
    private int id;
    private int cudaCores;          // Número de CUDA cores
    private double gflops;          // Capacidade em GFLOPS
    private long gpuMemory;         // Memória GPU em MB
    private double utilization;     // Utilização atual (0-100%)
    private GpuEdgeVM allocatedVm;  // VM alocada (null se livre)
    
    public Gpu(int id, int cudaCores, double gflops, long gpuMemory) {
        this.id = id;
        this.cudaCores = cudaCores;
        this.gflops = gflops;
        this.gpuMemory = gpuMemory;
        this.utilization = 0.0;
        this.allocatedVm = null;
    }
    
    // Getters e Setters
    public int getId() { return id; }
    public int getCudaCores() { return cudaCores; }
    public double getGflops() { return gflops; }
    public long getGpuMemory() { return gpuMemory; }
    public double getUtilization() { return utilization; }
    public void setUtilization(double utilization) { this.utilization = utilization; }
    public GpuEdgeVM getAllocatedVm() { return allocatedVm; }
    public void setAllocatedVm(GpuEdgeVM vm) { this.allocatedVm = vm; }
    public boolean isAvailable() { return allocatedVm == null; }
}
```

**Classe Auxiliar: GpuProvisioner**

```java
package edu.boun.edgecloudsim.edge_server;

import java.util.List;

/**
 * Interface para provisionamento de GPU.
 */
public interface GpuProvisioner {
    boolean allocateGpuForVm(GpuEdgeVM vm, Gpu gpu);
    void deallocateGpuForVm(GpuEdgeVM vm);
    boolean hasAvailableGpu();
    Gpu getAvailableGpu();
}

/**
 * Implementação simples de provisionamento de GPU.
 */
public class GpuProvisionerSimple implements GpuProvisioner {
    private List<Gpu> gpuList;
    
    public GpuProvisionerSimple(List<Gpu> gpuList) {
        this.gpuList = gpuList;
    }
    
    @Override
    public boolean allocateGpuForVm(GpuEdgeVM vm, Gpu gpu) {
        if (gpu.isAvailable()) {
            gpu.setAllocatedVm(vm);
            return true;
        }
        return false;
    }
    
    @Override
    public void deallocateGpuForVm(GpuEdgeVM vm) {
        for (Gpu gpu : gpuList) {
            if (gpu.getAllocatedVm() == vm) {
                gpu.setAllocatedVm(null);
                gpu.setUtilization(0.0);
                break;
            }
        }
    }
    
    @Override
    public boolean hasAvailableGpu() {
        return gpuList.stream().anyMatch(Gpu::isAvailable);
    }
    
    @Override
    public Gpu getAvailableGpu() {
        return gpuList.stream()
                      .filter(Gpu::isAvailable)
                      .findFirst()
                      .orElse(null);
    }
}
```

---

#### **Ponto 3: GpuEdgeVM**

**Localização:** `/home/ubuntu/EdgeCloudSim/src/edu/boun/edgecloudsim/edge_server/GpuEdgeVM.java`

**Estrutura Proposta:**

```java
package edu.boun.edgecloudsim.edge_server;

import org.cloudbus.cloudsim.CloudletScheduler;

/**
 * VM Edge com suporte a execução de tarefas GPU.
 * Estende EdgeVM para adicionar gerenciamento de GPU alocada.
 */
public class GpuEdgeVM extends EdgeVM {
    private Gpu allocatedGpu;                       // GPU alocada para esta VM
    private GpuCloudletScheduler gpuCloudletScheduler;  // Scheduler GPU
    
    public GpuEdgeVM(
            int id,
            int userId,
            double mips,
            int numberOfPes,
            int ram,
            long bw,
            long size,
            String vmm,
            CloudletScheduler cloudletScheduler,
            GpuCloudletScheduler gpuCloudletScheduler) {
        
        super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
        
        this.gpuCloudletScheduler = gpuCloudletScheduler;
        this.allocatedGpu = null;
    }
    
    /**
     * Define a GPU alocada para esta VM.
     */
    public void setGpu(Gpu gpu) {
        this.allocatedGpu = gpu;
    }
    
    /**
     * Retorna a GPU alocada.
     */
    public Gpu getGpu() {
        return allocatedGpu;
    }
    
    /**
     * Verifica se esta VM possui GPU alocada.
     */
    public boolean hasGpu() {
        return allocatedGpu != null;
    }
    
    /**
     * Retorna o scheduler de GPU.
     */
    public GpuCloudletScheduler getGpuCloudletScheduler() {
        return gpuCloudletScheduler;
    }
    
    /**
     * Calcula utilização da GPU.
     */
    public double getGpuUtilization() {
        if (allocatedGpu != null) {
            return allocatedGpu.getUtilization();
        }
        return 0.0;
    }
    
    /**
     * Reconfigura capacidade da GPU (se suportado).
     */
    public void reconfigureGpu(double newGflops) {
        if (allocatedGpu != null) {
            // Implementar lógica de reconfiguração
        }
    }
}
```

**Classe Auxiliar: GpuCloudletScheduler**

```java
package edu.boun.edgecloudsim.edge_server;

import java.util.List;

/**
 * Scheduler para tarefas GPU em uma VM.
 * Define como múltiplas tarefas GPU compartilham a GPU.
 */
public abstract class GpuCloudletScheduler {
    private Gpu gpu;
    
    public GpuCloudletScheduler(Gpu gpu) {
        this.gpu = gpu;
    }
    
    /**
     * Adiciona tarefa GPU à fila de execução.
     */
    public abstract boolean submitGpuTask(GpuTask task);
    
    /**
     * Remove tarefa GPU da execução.
     */
    public abstract void removeGpuTask(GpuTask task);
    
    /**
     * Calcula utilização atual da GPU.
     */
    public abstract double getGpuUtilization();
    
    /**
     * Retorna lista de tarefas em execução.
     */
    public abstract List<GpuTask> getRunningTasks();
}

/**
 * Implementação time-shared para tarefas GPU.
 */
public class GpuCloudletSchedulerTimeShared extends GpuCloudletScheduler {
    private List<GpuTask> runningTasks;
    
    public GpuCloudletSchedulerTimeShared(Gpu gpu) {
        super(gpu);
        this.runningTasks = new ArrayList<>();
    }
    
    @Override
    public boolean submitGpuTask(GpuTask task) {
        runningTasks.add(task);
        // Redistribuir GFLOPS entre tarefas
        redistributeGpuResources();
        return true;
    }
    
    @Override
    public void removeGpuTask(GpuTask task) {
        runningTasks.remove(task);
        redistributeGpuResources();
    }
    
    private void redistributeGpuResources() {
        // Divide GFLOPS igualmente entre tarefas
        // Implementar lógica de time-slicing
    }
    
    // ... outros métodos
}
```

---

#### **Ponto 4: GpuTask**

**Localização:** `/home/ubuntu/EdgeCloudSim/src/edu/boun/edgecloudsim/edge_client/GpuTask.java`

**Estrutura Proposta:**

```java
package edu.boun.edgecloudsim.edge_client;

/**
 * Tarefa que requer processamento GPU.
 * Estende Task para adicionar requisitos e métricas GPU.
 */
public class GpuTask extends Task {
    private long gpuLength;             // Tamanho em GFLOPs
    private long gpuInputData;          // Dados de entrada para GPU (MB)
    private long gpuOutputData;         // Dados de saída da GPU (MB)
    private long requiredGpuMemory;     // Memória GPU necessária (MB)
    private double gpuUtilization;      // Utilização GPU (0-100%)
    
    // Métricas de tempo
    private double gpuDataTransferTime;  // Tempo de transferência CPU→GPU
    private double gpuExecutionTime;     // Tempo de execução na GPU
    private double gpuDataBackTime;      // Tempo de transferência GPU→CPU
    
    public GpuTask(
            int cloudletId,
            int taskId,
            long cloudletLength,
            long pesNumber,
            long cloudletFileSize,
            long cloudletOutputSize,
            long gpuLength,
            long gpuInputData,
            long gpuOutputData,
            long requiredGpuMemory) {
        
        super(cloudletId, taskId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize);
        
        this.gpuLength = gpuLength;
        this.gpuInputData = gpuInputData;
        this.gpuOutputData = gpuOutputData;
        this.requiredGpuMemory = requiredGpuMemory;
        this.gpuUtilization = 0.0;
        this.gpuDataTransferTime = 0.0;
        this.gpuExecutionTime = 0.0;
        this.gpuDataBackTime = 0.0;
    }
    
    // Getters
    public long getGpuLength() { return gpuLength; }
    public long getGpuInputData() { return gpuInputData; }
    public long getGpuOutputData() { return gpuOutputData; }
    public long getRequiredGpuMemory() { return requiredGpuMemory; }
    public double getGpuUtilization() { return gpuUtilization; }
    
    // Setters para métricas
    public void setGpuDataTransferTime(double time) { this.gpuDataTransferTime = time; }
    public void setGpuExecutionTime(double time) { this.gpuExecutionTime = time; }
    public void setGpuDataBackTime(double time) { this.gpuDataBackTime = time; }
    public void setGpuUtilization(double utilization) { this.gpuUtilization = utilization; }
    
    /**
     * Calcula tempo total de processamento GPU.
     */
    public double getTotalGpuTime() {
        return gpuDataTransferTime + gpuExecutionTime + gpuDataBackTime;
    }
    
    /**
     * Verifica se tarefa requer GPU.
     */
    public boolean requiresGpu() {
        return gpuLength > 0;
    }
}
```

---

#### **Ponto 5: GpuScenarioFactory**

**Localização:** `/home/ubuntu/EdgeCloudSim/src/edu/boun/edgecloudsim/applications/gpu_app/GpuScenarioFactory.java`

**Estrutura Proposta:**

```java
package edu.boun.edgecloudsim.applications.gpu_app;

import edu.boun.edgecloudsim.cloud_server.CloudServerManager;
import edu.boun.edgecloudsim.cloud_server.DefaultCloudServerManager;
import edu.boun.edgecloudsim.core.ScenarioFactory;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeServerManager;
import edu.boun.edgecloudsim.edge_server.GpuEdgeServerManager;  // ← NOVO
import edu.boun.edgecloudsim.edge_client.MobileDeviceManager;
import edu.boun.edgecloudsim.edge_client.mobile_processing_unit.MobileServerManager;
import edu.boun.edgecloudsim.edge_client.mobile_processing_unit.DefaultMobileServerManager;
import edu.boun.edgecloudsim.mobility.MobilityModel;
import edu.boun.edgecloudsim.mobility.NomadicMobility;
import edu.boun.edgecloudsim.task_generator.LoadGeneratorModel;
import edu.boun.edgecloudsim.network.NetworkModel;

/**
 * Factory para cenários com suporte a GPU.
 * Retorna implementações GPU das classes edge.
 */
public class GpuScenarioFactory implements ScenarioFactory {
    private int numOfMobileDevice;
    private double simulationTime;
    private String orchestratorPolicy;
    private String simScenario;
    
    public GpuScenarioFactory(
            int _numOfMobileDevice,
            double _simulationTime,
            String _orchestratorPolicy,
            String _simScenario) {
        
        this.orchestratorPolicy = _orchestratorPolicy;
        this.numOfMobileDevice = _numOfMobileDevice;
        this.simulationTime = _simulationTime;
        this.simScenario = _simScenario;
    }
    
    @Override
    public LoadGeneratorModel getLoadGeneratorModel() {
        // Pode usar gerador customizado que cria GpuTasks
        return new GpuLoadGenerator(numOfMobileDevice, simulationTime, simScenario);
    }
    
    @Override
    public EdgeOrchestrator getEdgeOrchestrator() {
        // Pode usar orchestrador que considera GPUs
        return new GpuEdgeOrchestrator(orchestratorPolicy, simScenario);
    }
    
    @Override
    public MobilityModel getMobilityModel() {
        return new NomadicMobility(numOfMobileDevice, simulationTime);
    }
    
    @Override
    public NetworkModel getNetworkModel() {
        return new GpuNetworkModel(numOfMobileDevice, simScenario);
    }
    
    @Override
    public EdgeServerManager getEdgeServerManager() {
        // ← PONTO CRÍTICO: Retorna GpuEdgeServerManager
        return new GpuEdgeServerManager();
    }
    
    @Override
    public CloudServerManager getCloudServerManager() {
        return new DefaultCloudServerManager();
    }
    
    @Override
    public MobileServerManager getMobileServerManager() {
        return new DefaultMobileServerManager();
    }
    
    @Override
    public MobileDeviceManager getMobileDeviceManager() throws Exception {
        return new GpuMobileDeviceManager();
    }
}
```

---

## 6. 📝 Recomendações para Implementação

### 6.1 Ordem de Implementação Sugerida

```
Fase 2.1: Classes Base GPU
├── Gpu.java (modelo físico de GPU)
├── GpuProvisioner.java e GpuProvisionerSimple.java
└── GpuTask.java (tarefa com requisitos GPU)

Fase 2.2: Infraestrutura Edge GPU
├── GpuEdgeHost.java (host com GPUs)
├── GpuEdgeVM.java (VM com GPU alocada)
├── GpuCloudletScheduler.java
└── GpuEdgeVmAllocationPolicy_Custom.java

Fase 2.3: Gerenciamento GPU
├── GpuEdgeServerManager.java
└── Atualizar SimSettings.VM_TYPES (adicionar GPU_EDGE_VM)

Fase 2.4: Cenário e Testes
├── GpuScenarioFactory.java
├── GpuEdgeOrchestrator.java (opcional)
├── GpuLoadGenerator.java (opcional)
└── Criar edge_devices_gpu.xml e applications_gpu.xml

Fase 2.5: MainApp e Validação
├── MainApp.java para gpu_app
└── Testes unitários e de integração
```

### 6.2 Configuração XML Proposta

**edge_devices_gpu.xml:**

```xml
<?xml version="1.0"?>
<edge_devices>
    <datacenter arch="x86" os="Linux" vmm="Xen">
        <costPerBw>0.1</costPerBw>
        <costPerSec>3.0</costPerSec>
        <costPerMem>0.05</costPerMem>
        <costPerStorage>0.1</costPerStorage>
        
        <location>
            <x_pos>1</x_pos>
            <y_pos>1</y_pos>
            <wlan_id>0</wlan_id>
            <attractiveness>0</attractiveness>
        </location>
        
        <hosts>
            <host>
                <!-- Recursos CPU -->
                <core>16</core>
                <mips>80000</mips>
                <ram>32000</ram>
                <storage>100000</storage>
                
                <!-- NOVO: Recursos GPU -->
                <gpus>
                    <gpu>
                        <gpu_id>0</gpu_id>
                        <gpu_type>NVIDIA_T4</gpu_type>
                        <cuda_cores>2560</cuda_cores>
                        <gflops>8100</gflops>
                        <gpu_memory>16000</gpu_memory>
                        <memory_bandwidth>320</memory_bandwidth>
                    </gpu>
                    <gpu>
                        <gpu_id>1</gpu_id>
                        <gpu_type>NVIDIA_A100</gpu_type>
                        <cuda_cores>6912</cuda_cores>
                        <gflops>19500</gflops>
                        <gpu_memory>40000</gpu_memory>
                        <memory_bandwidth>1555</memory_bandwidth>
                    </gpu>
                </gpus>
                
                <VMs>
                    <VM vmm="Xen">
                        <core>4</core>
                        <mips>20000</mips>
                        <ram>8000</ram>
                        <storage>50000</storage>
                        <!-- NOVO: Requisito GPU -->
                        <requires_gpu>true</requires_gpu>
                        <gpu_allocation>EXCLUSIVE</gpu_allocation>
                    </VM>
                    <VM vmm="Xen">
                        <core>4</core>
                        <mips>20000</mips>
                        <ram>8000</ram>
                        <storage>50000</storage>
                        <requires_gpu>false</requires_gpu>
                    </VM>
                </VMs>
            </host>
        </hosts>
    </datacenter>
</edge_devices>
```

**applications_gpu.xml:**

```xml
<?xml version="1.0"?>
<applications>
    <application name="DEEP_LEARNING_INFERENCE">
        <usage_percentage>30</usage_percentage>
        <prob_cloud_selection>10</prob_cloud_selection>
        <poisson_interarrival>5</poisson_interarrival>
        <delay_sensitivity>1</delay_sensitivity>
        <active_period>60</active_period>
        <idle_period>30</idle_period>
        
        <!-- CPU requirements -->
        <data_upload>5000</data_upload>
        <data_download>500</data_download>
        <task_length>50000</task_length>
        <required_core>2</required_core>
        <vm_utilization_on_edge>10</vm_utilization_on_edge>
        
        <!-- NOVO: GPU requirements -->
        <requires_gpu>true</requires_gpu>
        <gpu_length>150000</gpu_length>              <!-- GFLOPs -->
        <gpu_input_data>2000</gpu_input_data>        <!-- MB -->
        <gpu_output_data>100</gpu_output_data>       <!-- MB -->
        <required_gpu_memory>4000</required_gpu_memory> <!-- MB -->
        <gpu_utilization>80</gpu_utilization>        <!-- % -->
    </application>
    
    <application name="VIDEO_TRANSCODING">
        <usage_percentage>25</usage_percentage>
        <!-- ... configurações similares com GPU ... -->
    </application>
    
    <application name="AR_PROCESSING">
        <usage_percentage>20</usage_percentage>
        <!-- ... sem GPU (requires_gpu=false) ... -->
    </application>
</applications>
```

### 6.3 Considerações de Design

#### 6.3.1 Alocação de GPU

**Estratégias Possíveis:**

1. **Exclusive Mode** (1 GPU : 1 VM)
   - Vantagem: Performance máxima
   - Desvantagem: Subutilização de GPU

2. **Shared Mode** (1 GPU : N VMs)
   - Vantagem: Melhor utilização
   - Desvantagem: Contenção e scheduling complexo

3. **Hybrid Mode** (configurável por VM)
   - Vantagem: Flexibilidade
   - Desvantagem: Complexidade de implementação

**Recomendação:** Começar com **Exclusive Mode** para simplificar MVP, depois evoluir para Shared.

#### 6.3.2 Modelagem de Tempo de Execução GPU

**Componentes do Tempo Total:**

```
T_total = T_cpu_processing + T_cpu_to_gpu_transfer + T_gpu_processing + T_gpu_to_cpu_transfer
```

**Fórmulas Propostas:**

```java
// Tempo de processamento GPU
double gpuProcessingTime = (gpuTask.getGpuLength() * 1000000) / gpu.getGflops();

// Tempo de transferência CPU → GPU
double cpuToGpuTime = (gpuTask.getGpuInputData() * 8) / gpu.getMemoryBandwidth();

// Tempo de transferência GPU → CPU
double gpuToCpuTime = (gpuTask.getGpuOutputData() * 8) / gpu.getMemoryBandwidth();

// Tempo total
double totalGpuTime = cpuToGpuTime + gpuProcessingTime + gpuToCpuTime;
```

#### 6.3.3 Métricas a Coletar

**Métricas GPU Essenciais:**

| Métrica | Descrição | Unidade |
|---------|-----------|---------|
| GPU Utilization | % de uso da GPU | 0-100% |
| GPU Memory Usage | Memória GPU ocupada | MB |
| GPU Task Completion Time | Tempo total de execução | ms |
| GPU Waiting Time | Tempo em fila | ms |
| GPU Energy Consumption | Consumo energético | Joules |
| GPU Throughput | Tarefas/segundo | tasks/s |

**Implementação no SimLogger:**

```java
// Adicionar em SimLogger.java
public void addGpuTaskLog(
    double time,
    int mobileDeviceId,
    int vmId,
    int gpuId,
    int taskId,
    double gpuUtilization,
    double gpuMemoryUsage,
    double gpuExecutionTime,
    double gpuWaitingTime) {
    
    // Salvar em arquivo CSV
}
```

---

## 7. 🔍 Exemplos de Código Relevantes

### 7.1 Fluxo de Criação de Host (DefaultEdgeServerManager)

**Trecho Original:**

```java
// DefaultEdgeServerManager.java, linhas 280-288
EdgeHost host = new EdgeHost(
    hostIdCounter,
    new RamProvisionerSimple(ram),
    new BwProvisionerSimple(bandwidth),
    storage,
    peList,
    new VmSchedulerSpaceShared(peList)
);

host.setPlace(new Location(placeTypeIndex, wlan_id, x_pos, y_pos));
```

**Adaptação GPU:**

```java
// GpuEdgeServerManager.java
GpuEdgeHost host = new GpuEdgeHost(
    hostIdCounter,
    new RamProvisionerSimple(ram),
    new BwProvisionerSimple(bandwidth),
    storage,
    peList,
    new VmSchedulerSpaceShared(peList),
    gpuList,                             // ← NOVO
    new GpuProvisionerSimple(gpuList)    // ← NOVO
);

host.setPlace(new Location(placeTypeIndex, wlan_id, x_pos, y_pos));
```

### 7.2 Fluxo de Criação de VM (DefaultEdgeServerManager)

**Trecho Original:**

```java
// DefaultEdgeServerManager.java, linhas 147-148
EdgeVM vm = new EdgeVM(
    vmCounter, 
    brokerId, 
    mips, 
    numOfCores, 
    ram, 
    bandwidth, 
    storage, 
    vmm, 
    new CloudletSchedulerTimeShared()
);
vmList.get(hostCounter).add(vm);
```

**Adaptação GPU:**

```java
// GpuEdgeServerManager.java
GpuEdgeVM vm = new GpuEdgeVM(
    vmCounter,
    brokerId,
    mips,
    numOfCores,
    ram,
    bandwidth,
    storage,
    vmm,
    new CloudletSchedulerTimeShared(),
    new GpuCloudletSchedulerTimeShared(null)  // ← NOVO (GPU alocada depois)
);

// Se VM requer GPU, aloca uma
if (requiresGpu) {
    GpuEdgeHost host = (GpuEdgeHost) getHostList().get(hostCounter);
    Gpu availableGpu = host.getAvailableGpu();
    if (availableGpu != null) {
        host.allocateGpuForVm(vm, availableGpu);
        vm.setGpu(availableGpu);
    }
}

vmList.get(hostCounter).add(vm);
```

### 7.3 Alocação de VM (EdgeVmAllocationPolicy_Custom)

**Trecho Original:**

```java
// EdgeVmAllocationPolicy_Custom.java, linhas 117-125
if(vmFound && dataCenterIndex == DataCenterIndex && hostIndex < getHostList().size()){
    Host host = getHostList().get(hostIndex);
    result = host.vmCreate(vm);

    if (result) {
        getVmTable().put(vm.getUid(), host);
        createdVmNum++;
        Log.formatLine("%.2f: Edge VM #" + vm.getId() + " has been allocated...", CloudSim.clock());
    }
}
```

**Adaptação GPU:**

```java
// GpuEdgeVmAllocationPolicy_Custom.java
if(vmFound && dataCenterIndex == DataCenterIndex && hostIndex < getHostList().size()){
    GpuEdgeHost host = (GpuEdgeHost) getHostList().get(hostIndex);
    GpuEdgeVM gpuVm = (GpuEdgeVM) vm;
    
    // Verifica se VM requer GPU
    if (gpuVm.requiresGpu()) {
        if (!host.hasAvailableGpu()) {
            Log.formatLine("%.2f: No GPU available for VM #" + vm.getId(), CloudSim.clock());
            return false;
        }
        
        // Aloca GPU antes de criar VM
        Gpu gpu = host.getAvailableGpu();
        host.allocateGpuForVm(gpuVm, gpu);
        gpuVm.setGpu(gpu);
    }
    
    result = host.vmCreate(vm);

    if (result) {
        getVmTable().put(vm.getUid(), host);
        createdVmNum++;
        String gpuInfo = gpuVm.hasGpu() ? " with GPU #" + gpuVm.getGpu().getId() : "";
        Log.formatLine("%.2f: Edge VM #" + vm.getId() + gpuInfo + " has been allocated...", CloudSim.clock());
    } else {
        // Se falhou, desaloca GPU
        if (gpuVm.hasGpu()) {
            host.deallocateGpuForVm(gpuVm);
        }
    }
}
```

---

## 8. 📊 Diagrama de Sequência: Criação de Infraestrutura GPU

```
┌──────┐     ┌─────────┐     ┌─────────────────────┐     ┌────────────┐     ┌──────────┐     ┌─────┐
│MainApp│     │SimManager     │GpuEdgeServerManager │     │GpuEdgeHost │     │GpuEdgeVM │     │ Gpu │
└───┬──┘     └────┬────┘     └──────────┬──────────┘     └─────┬──────┘     └────┬─────┘     └──┬──┘
    │             │                       │                      │                  │              │
    │ new SimManager()                    │                      │                  │              │
    │─────────────>│                       │                      │                  │              │
    │             │                       │                      │                  │              │
    │             │ factory.getEdgeServerManager()               │                  │              │
    │             │──────────────────────>│                      │                  │              │
    │             │                       │                      │                  │              │
    │             │         new GpuEdgeServerManager()           │                  │              │
    │             │<──────────────────────│                      │                  │              │
    │             │                       │                      │                  │              │
    │ startSimulation()                   │                      │                  │              │
    │─────────────>│                       │                      │                  │              │
    │             │                       │                      │                  │              │
    │             │ startDatacenters()    │                      │                  │              │
    │             │──────────────────────>│                      │                  │              │
    │             │                       │                      │                  │              │
    │             │                       │  createGpuHosts()    │                  │              │
    │             │                       │─────────────────────>│                  │              │
    │             │                       │                      │                  │              │
    │             │                       │                      │  createGpuList() │              │
    │             │                       │                      │─────────────────>│              │
    │             │                       │                      │                  │              │
    │             │                       │                      │                  │  new Gpu()   │
    │             │                       │                      │                  │─────────────>│
    │             │                       │                      │                  │              │
    │             │                       │                      │<─────────────────│              │
    │             │                       │                      │                  │              │
    │             │                       │  new GpuEdgeHost(gpuList)              │              │
    │             │                       │─────────────────────>│                  │              │
    │             │                       │                      │                  │              │
    │             │                       │<─────────────────────│                  │              │
    │             │                       │                      │                  │              │
    │             │ createVmList()        │                      │                  │              │
    │             │──────────────────────>│                      │                  │              │
    │             │                       │                      │                  │              │
    │             │                       │                      │  new GpuEdgeVM() │              │
    │             │                       │                      │─────────────────>│              │
    │             │                       │                      │                  │              │
    │             │                       │                      │  getAvailableGpu()              │
    │             │                       │                      │──────────────────────────────────>│
    │             │                       │                      │                  │              │
    │             │                       │                      │<──────────────────────────────────│
    │             │                       │                      │                  │              │
    │             │                       │                      │  allocateGpuForVm(vm, gpu)      │
    │             │                       │                      │──────────────────────────────────>│
    │             │                       │                      │                  │              │
    │             │                       │                      │  setGpu(gpu)     │              │
    │             │                       │                      │─────────────────>│              │
    │             │                       │                      │                  │              │
    │             │                       │<─────────────────────│                  │              │
    │             │                       │                      │                  │              │
    │             │<──────────────────────│                      │                  │              │
    │             │                       │                      │                  │              │
    │<────────────│                       │                      │                  │              │
```

---

## 9. 🎓 Conclusões e Próximos Passos

### 9.1 Principais Descobertas

1. **EdgeCloudSim é Altamente Extensível**
   - O padrão Factory permite adicionar funcionalidades sem modificar o core
   - Estrutura modular facilita criação de novas classes especializadas

2. **Hierarquia Clara de Classes**
   - Manager → Datacenter → Host → VM
   - Cada nível tem responsabilidades bem definidas

3. **XML como Fonte de Verdade**
   - Configurações de hardware lidas de arquivos XML
   - Facilita experimentação com diferentes topologias

4. **Simulação por Eventos Discretos**
   - SimManager coordena eventos via CloudSim
   - Permite adicionar eventos customizados (ex: GPU_TASK_COMPLETED)

### 9.2 Roadmap da Fase 2

**Fase 2: Design das Classes GPU** (próximo passo)
- Definir APIs completas de todas as classes GPU
- Criar diagramas UML detalhados
- Especificar contratos de interfaces
- Planejar testes unitários

**Fase 3: Implementação das Classes Base**
- Implementar Gpu, GpuProvisioner, GpuTask
- Testes unitários de cada classe

**Fase 4: Implementação da Infraestrutura Edge**
- Implementar GpuEdgeHost, GpuEdgeVM, GpuEdgeServerManager
- Testes de integração

**Fase 5: Cenário de Validação**
- Criar GpuScenarioFactory e MainApp
- Executar simulação mínima (2 hosts, 4 VMs, 100 tasks)

**Fase 6: Testes e Métricas**
- Coletar dados de utilização GPU
- Validar tempos de execução
- Gerar gráficos com Python

**Fase 7: Documentação e Artigo**
- Escrever artigo científico
- Documentar decisões de design
- Preparar para publicação

### 9.3 Riscos e Mitigações

| Risco | Impacto | Mitigação |
|-------|---------|-----------|
| Incompatibilidade com CloudSim | Alto | Estender classes CloudSim sem modificar core |
| Complexidade de scheduling GPU | Médio | Começar com Exclusive Mode, evoluir depois |
| Performance de simulação | Médio | Otimizar estruturas de dados, limitar logs |
| Validação científica | Alto | Comparar com dados reais ou outros simuladores |

### 9.4 Referências Arquiteturais

**Classes Inspiradoras:**
- `EdgeHost` → modelo para `GpuEdgeHost`
- `EdgeVM` → modelo para `GpuEdgeVM`
- `DefaultEdgeServerManager` → modelo para `GpuEdgeServerManager`

**Padrões a Seguir:**
- Factory Pattern (ScenarioFactory)
- Template Method (EdgeServerManager)
- Strategy (VmAllocationPolicy)

---

## 10. 📚 Referências

1. **EdgeCloudSim Repository:** https://github.com/CagataySonmez/EdgeCloudSim
2. **CloudSim Documentation:** http://www.cloudbus.org/cloudsim/
3. **Artigo Original EdgeCloudSim:**
   - C. Sonmez, A. Ozgovde, C. Ersoy, "EdgeCloudSim: An environment for performance evaluation of edge computing systems," Transactions on Emerging Telecommunications Technologies, Vol. 29, No. 11, 2018
4. **Modelling Guide:** EdgeCloudSim_ModellingGuide.pdf (Cagatay Sonmez, 2025)
5. **PERSONA.MD:** Super-Persona EdgeCloudSim Architect & Java Developer Advanced

---

## 📄 Apêndice A: Estrutura Completa de Arquivos Proposta

```
/home/ubuntu/EdgeCloudSim/src/edu/boun/edgecloudsim/
├── edge_server/
│   ├── EdgeServerManager.java                (existente)
│   ├── DefaultEdgeServerManager.java         (existente)
│   ├── EdgeHost.java                         (existente)
│   ├── EdgeVM.java                           (existente)
│   ├── EdgeVmAllocationPolicy_Custom.java    (existente)
│   ├── GpuEdgeServerManager.java             (NOVO)
│   ├── GpuEdgeHost.java                      (NOVO)
│   ├── GpuEdgeVM.java                        (NOVO)
│   ├── GpuEdgeVmAllocationPolicy_Custom.java (NOVO)
│   ├── Gpu.java                              (NOVO)
│   ├── GpuProvisioner.java                   (NOVO)
│   ├── GpuProvisionerSimple.java             (NOVO)
│   ├── GpuCloudletScheduler.java             (NOVO)
│   └── GpuCloudletSchedulerTimeShared.java   (NOVO)
│
├── edge_client/
│   ├── Task.java                             (existente)
│   └── GpuTask.java                          (NOVO)
│
├── applications/
│   └── gpu_app/
│       ├── MainApp.java                      (NOVO)
│       ├── GpuScenarioFactory.java           (NOVO)
│       ├── GpuEdgeOrchestrator.java          (NOVO - opcional)
│       ├── GpuLoadGenerator.java             (NOVO - opcional)
│       └── GpuMobileDeviceManager.java       (NOVO - opcional)
│
└── scripts/
    └── gpu_app/
        ├── config/
        │   ├── default_config.properties
        │   ├── edge_devices_gpu.xml          (NOVO)
        │   └── applications_gpu.xml          (NOVO)
        ├── matlab/
        │   └── plotGenericResult.m
        └── python/
            └── plot_gpu_results.py           (NOVO)
```

---

## 📄 Apêndice B: Checklist de Implementação

```markdown
### Fase 2.1: Classes Base GPU
- [ ] Criar Gpu.java
- [ ] Criar GpuProvisioner.java (interface)
- [ ] Criar GpuProvisionerSimple.java
- [ ] Criar GpuTask.java
- [ ] Testes unitários para cada classe

### Fase 2.2: Infraestrutura Edge GPU
- [ ] Criar GpuEdgeHost.java
- [ ] Criar GpuEdgeVM.java
- [ ] Criar GpuCloudletScheduler.java
- [ ] Criar GpuCloudletSchedulerTimeShared.java
- [ ] Criar GpuEdgeVmAllocationPolicy_Custom.java
- [ ] Testes de integração Host ↔ VM ↔ GPU

### Fase 2.3: Gerenciamento GPU
- [ ] Criar GpuEdgeServerManager.java
- [ ] Implementar createGpuHosts()
- [ ] Implementar createGpuDatacenter()
- [ ] Implementar createVmList() com alocação GPU
- [ ] Atualizar SimSettings.VM_TYPES (adicionar GPU_EDGE_VM)
- [ ] Testes de criação completa de infraestrutura

### Fase 2.4: Configuração e Cenário
- [ ] Criar edge_devices_gpu.xml
- [ ] Criar applications_gpu.xml
- [ ] Criar GpuScenarioFactory.java
- [ ] Criar GpuEdgeOrchestrator.java (opcional)
- [ ] Criar GpuLoadGenerator.java (opcional)
- [ ] Criar GpuMobileDeviceManager.java (opcional)

### Fase 2.5: MainApp e Validação
- [ ] Criar MainApp.java para gpu_app
- [ ] Executar primeira simulação completa
- [ ] Validar criação de hosts, VMs e alocação de GPUs
- [ ] Verificar logs e saídas

### Fase 2.6: Testes e Métricas
- [ ] Adicionar métricas GPU ao SimLogger
- [ ] Coletar dados de utilização
- [ ] Criar plot_gpu_results.py
- [ ] Gerar gráficos comparativos CPU vs GPU
- [ ] Calcular intervalos de confiança

### Fase 2.7: Documentação e Artigo
- [ ] Escrever Fase2_Design_Classes_GPU.md
- [ ] Documentar decisões de design
- [ ] Escrever seção de metodologia do artigo
- [ ] Preparar figuras e tabelas
- [ ] Revisar e validar implementação
```

---

## 📝 Notas Finais

Este documento constitui a **base técnica completa** para a implementação das classes GPU no EdgeCloudSim. Todas as análises, diagramas e recomendações foram feitas com base no código fonte real do EdgeCloudSim e seguindo as melhores práticas definidas na PERSONA.MD.

**Próxima Ação:** Iniciar **Fase 2 - Design das Classes GPU** usando este documento como referência.

**Última Atualização:** 23 de Outubro de 2025  
**Status:** ✅ Completo e pronto para Fase 2

---

🏁 **Fim da Análise Arquitetural - GpuEdgeCloudSim Fase 1**
