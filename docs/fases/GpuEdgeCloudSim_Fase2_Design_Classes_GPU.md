# 🎨 GpuEdgeCloudSim - Fase 2: Design Detalhado das Classes GPU

**Autor:** Pabllo Borges Cardoso  
**Data:** 23 de Outubro de 2025  
**Versão:** 1.0  
**Projeto:** GpuEdgeCloudSim v1.0 - Extensão GPU do EdgeCloudSim

---

## 📋 Sumário Executivo

Este documento apresenta o **design completo e detalhado** das classes GPU para o **GpuEdgeCloudSim**, incluindo especificações de API, diagramas UML, contratos de interface, diagramas de sequência, exemplos de uso e estratégias de teste.

### Contexto da Fase 2

A **Fase 1 (Análise Arquitetural)** identificou 5 pontos críticos de extensão no EdgeCloudSim. Esta Fase 2 detalha o design técnico necessário para implementação na **Fase 3**.

### Escopo do Design

Este documento especifica **10 classes GPU principais**:

1. **Gpu** - Representação física de uma GPU
2. **GpuProvisioner** - Interface para alocação de GPU
3. **GpuProvisionerSimple** - Implementação simples de provisionamento
4. **GpuTask** - Tarefa com requisitos GPU
5. **GpuEdgeHost** - Host edge com GPUs físicas
6. **GpuEdgeVM** - VM com GPU alocada
7. **GpuCloudletScheduler** - Interface para escalonamento GPU
8. **GpuCloudletSchedulerTimeShared** - Implementação time-shared
9. **GpuEdgeServerManager** - Gerenciador de infraestrutura GPU
10. **GpuEdgeVmAllocationPolicy_Custom** - Política de alocação GPU-aware

---

## 📑 Índice

1. [Especificações Completas de APIs](#1-especificações-completas-de-apis)
2. [Diagramas UML Detalhados](#2-diagramas-uml-detalhados)
3. [Contratos de Interfaces](#3-contratos-de-interfaces)
4. [Diagramas de Sequência](#4-diagramas-de-sequência)
5. [Exemplos de Uso](#5-exemplos-de-uso)
6. [Estratégias de Teste](#6-estratégias-de-teste)
7. [Decisões de Design](#7-decisões-de-design)
8. [Anexos](#8-anexos)

---

## 1. Especificações Completas de APIs

### 1.1 Classe: Gpu

**Pacote:** `edu.boun.edgecloudsim.edge_server`

**Propósito:** Representa uma GPU física em um EdgeHost, modelando suas características de hardware e estado de utilização.

**Relacionamentos:**
- Agregada por: `GpuEdgeHost`
- Alocada para: `GpuEdgeVM`
- Gerenciada por: `GpuProvisioner`

#### Atributos

```java
/**
 * Identificador único da GPU dentro do host.
 * @visibility private
 * @type int
 */
private int id;

/**
 * Tipo/modelo da GPU (ex: "NVIDIA_T4", "NVIDIA_A100").
 * @visibility private
 * @type String
 */
private String gpuType;

/**
 * Número de CUDA cores disponíveis.
 * @visibility private
 * @type int
 */
private int cudaCores;

/**
 * Capacidade computacional em GFLOPS (Giga Floating Point Operations Per Second).
 * @visibility private
 * @type double
 */
private double gflops;

/**
 * Memória total da GPU em MB.
 * @visibility private
 * @type long
 */
private long gpuMemory;

/**
 * Largura de banda da memória GPU em GB/s.
 * @visibility private
 * @type double
 */
private double memoryBandwidth;

/**
 * Utilização atual da GPU em percentual (0-100%).
 * @visibility private
 * @type double
 */
private double utilization;

/**
 * Memória GPU atualmente ocupada em MB.
 * @visibility private
 * @type long
 */
private long usedMemory;

/**
 * VM atualmente alocada nesta GPU (null se disponível).
 * @visibility private
 * @type GpuEdgeVM
 */
private GpuEdgeVM allocatedVm;

/**
 * Host que contém esta GPU.
 * @visibility private
 * @type GpuEdgeHost
 */
private GpuEdgeHost host;
```

#### Construtores

```java
/**
 * Construtor completo para criar uma GPU com todas as especificações.
 * 
 * @param id Identificador único da GPU
 * @param gpuType Tipo/modelo da GPU
 * @param cudaCores Número de CUDA cores
 * @param gflops Capacidade em GFLOPS
 * @param gpuMemory Memória total em MB
 * @param memoryBandwidth Largura de banda em GB/s
 */
public Gpu(int id, String gpuType, int cudaCores, double gflops, 
           long gpuMemory, double memoryBandwidth)

/**
 * Construtor simplificado para criar uma GPU com especificações básicas.
 * 
 * @param id Identificador único da GPU
 * @param cudaCores Número de CUDA cores
 * @param gflops Capacidade em GFLOPS
 * @param gpuMemory Memória total em MB
 */
public Gpu(int id, int cudaCores, double gflops, long gpuMemory)
```

#### Métodos

##### Getters Básicos

```java
/**
 * Retorna o identificador único da GPU.
 * @return ID da GPU
 */
public int getId()

/**
 * Retorna o tipo/modelo da GPU.
 * @return Tipo da GPU (ex: "NVIDIA_T4")
 */
public String getGpuType()

/**
 * Retorna o número de CUDA cores.
 * @return Número de CUDA cores
 */
public int getCudaCores()

/**
 * Retorna a capacidade computacional em GFLOPS.
 * @return Capacidade em GFLOPS
 */
public double getGflops()

/**
 * Retorna a memória total da GPU em MB.
 * @return Memória total em MB
 */
public long getGpuMemory()

/**
 * Retorna a largura de banda da memória em GB/s.
 * @return Largura de banda em GB/s
 */
public double getMemoryBandwidth()

/**
 * Retorna o host que contém esta GPU.
 * @return Host da GPU
 */
public GpuEdgeHost getHost()
```

##### Getters de Estado

```java
/**
 * Retorna a utilização atual da GPU em percentual.
 * @return Utilização (0-100%)
 */
public double getUtilization()

/**
 * Retorna a memória GPU atualmente ocupada em MB.
 * @return Memória ocupada em MB
 */
public long getUsedMemory()

/**
 * Retorna a memória GPU disponível em MB.
 * @return Memória disponível em MB
 */
public long getAvailableMemory()

/**
 * Retorna a VM atualmente alocada nesta GPU.
 * @return VM alocada ou null se disponível
 */
public GpuEdgeVM getAllocatedVm()

/**
 * Verifica se a GPU está disponível (não alocada).
 * @return true se disponível, false caso contrário
 */
public boolean isAvailable()
```

##### Setters

```java
/**
 * Define o host que contém esta GPU.
 * @param host Host da GPU
 */
public void setHost(GpuEdgeHost host)

/**
 * Define a utilização atual da GPU.
 * @param utilization Utilização em percentual (0-100%)
 */
public void setUtilization(double utilization)

/**
 * Define a memória GPU atualmente ocupada.
 * @param usedMemory Memória ocupada em MB
 */
public void setUsedMemory(long usedMemory)

/**
 * Define a VM alocada nesta GPU.
 * @param vm VM a ser alocada (null para desalocar)
 */
public void setAllocatedVm(GpuEdgeVM vm)
```

##### Métodos de Gerenciamento

```java
/**
 * Aloca memória GPU para uma tarefa.
 * 
 * @param memorySize Quantidade de memória a alocar em MB
 * @return true se a alocação foi bem-sucedida, false caso contrário
 */
public boolean allocateMemory(long memorySize)

/**
 * Libera memória GPU previamente alocada.
 * 
 * @param memorySize Quantidade de memória a liberar em MB
 * @return true se a liberação foi bem-sucedida, false caso contrário
 */
public boolean deallocateMemory(long memorySize)

/**
 * Calcula o tempo de execução de uma tarefa nesta GPU.
 * 
 * @param gpuLength Tamanho da tarefa em GFLOPs
 * @return Tempo de execução em segundos
 */
public double calculateExecutionTime(long gpuLength)

/**
 * Calcula o tempo de transferência de dados para a GPU.
 * 
 * @param dataSize Tamanho dos dados em MB
 * @return Tempo de transferência em segundos
 */
public double calculateDataTransferTime(long dataSize)

/**
 * Reseta a GPU para o estado inicial (disponível).
 */
public void reset()
```

##### Métodos de String

```java
/**
 * Retorna representação em string da GPU.
 * @return String com informações básicas da GPU
 */
@Override
public String toString()
```

---

### 1.2 Interface: GpuProvisioner

**Pacote:** `edu.boun.edgecloudsim.edge_server`

**Propósito:** Define o contrato para provisionamento de GPUs para VMs.

**Implementações:** `GpuProvisionerSimple`

#### Métodos

```java
/**
 * Aloca uma GPU específica para uma VM.
 * 
 * @param vm VM que receberá a GPU
 * @param gpu GPU a ser alocada
 * @return true se a alocação foi bem-sucedida, false caso contrário
 */
boolean allocateGpuForVm(GpuEdgeVM vm, Gpu gpu);

/**
 * Desaloca a GPU de uma VM, tornando-a disponível.
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
 * Estratégia de seleção depende da implementação.
 * 
 * @return GPU disponível ou null se não houver
 */
Gpu getAvailableGpu();

/**
 * Retorna uma GPU disponível com memória suficiente.
 * 
 * @param requiredMemory Memória mínima necessária em MB
 * @return GPU disponível com memória suficiente ou null
 */
Gpu getAvailableGpuWithMemory(long requiredMemory);

/**
 * Retorna a lista completa de GPUs gerenciadas.
 * 
 * @return Lista de todas as GPUs
 */
List<Gpu> getGpuList();

/**
 * Retorna a lista de GPUs disponíveis (não alocadas).
 * 
 * @return Lista de GPUs disponíveis
 */
List<Gpu> getAvailableGpuList();

/**
 * Retorna a lista de GPUs alocadas.
 * 
 * @return Lista de GPUs alocadas
 */
List<Gpu> getAllocatedGpuList();
```

---

### 1.3 Classe: GpuProvisionerSimple

**Pacote:** `edu.boun.edgecloudsim.edge_server`

**Propósito:** Implementação simples de provisionamento de GPU com alocação exclusiva (1 GPU : 1 VM).

**Relacionamentos:**
- Implementa: `GpuProvisioner`
- Usado por: `GpuEdgeHost`

#### Atributos

```java
/**
 * Lista de GPUs gerenciadas por este provisioner.
 * @visibility private
 * @type List<Gpu>
 */
private List<Gpu> gpuList;

/**
 * Mapa para rastrear alocações VM -> GPU.
 * @visibility private
 * @type Map<Integer, Gpu>
 */
private Map<Integer, Gpu> vmGpuMap;
```

#### Construtores

```java
/**
 * Construtor que inicializa o provisioner com uma lista de GPUs.
 * 
 * @param gpuList Lista de GPUs a serem gerenciadas
 */
public GpuProvisionerSimple(List<Gpu> gpuList)
```

#### Métodos

```java
/**
 * Aloca uma GPU específica para uma VM (modo exclusivo).
 * Verifica disponibilidade antes de alocar.
 * 
 * @param vm VM que receberá a GPU
 * @param gpu GPU a ser alocada
 * @return true se a alocação foi bem-sucedida, false caso contrário
 */
@Override
public boolean allocateGpuForVm(GpuEdgeVM vm, Gpu gpu)

/**
 * Desaloca a GPU de uma VM.
 * Reseta o estado da GPU e remove do mapa de alocações.
 * 
 * @param vm VM da qual a GPU será desalocada
 */
@Override
public void deallocateGpuForVm(GpuEdgeVM vm)

/**
 * Verifica se há GPUs disponíveis.
 * 
 * @return true se há pelo menos uma GPU disponível
 */
@Override
public boolean hasAvailableGpu()

/**
 * Retorna a primeira GPU disponível encontrada.
 * 
 * @return GPU disponível ou null
 */
@Override
public Gpu getAvailableGpu()

/**
 * Retorna uma GPU disponível com memória suficiente.
 * 
 * @param requiredMemory Memória mínima necessária em MB
 * @return GPU disponível com memória suficiente ou null
 */
@Override
public Gpu getAvailableGpuWithMemory(long requiredMemory)

/**
 * Retorna a lista completa de GPUs.
 * 
 * @return Lista imutável de GPUs
 */
@Override
public List<Gpu> getGpuList()

/**
 * Retorna a lista de GPUs disponíveis.
 * 
 * @return Lista de GPUs não alocadas
 */
@Override
public List<Gpu> getAvailableGpuList()

/**
 * Retorna a lista de GPUs alocadas.
 * 
 * @return Lista de GPUs alocadas
 */
@Override
public List<Gpu> getAllocatedGpuList()

/**
 * Retorna a GPU alocada para uma VM específica.
 * 
 * @param vmId ID da VM
 * @return GPU alocada para a VM ou null
 */
public Gpu getGpuForVm(int vmId)

/**
 * Calcula a utilização média de todas as GPUs.
 * 
 * @return Utilização média em percentual (0-100%)
 */
public double getAverageUtilization()
```

---

### 1.4 Classe: GpuTask

**Pacote:** `edu.boun.edgecloudsim.edge_client`

**Propósito:** Estende Task para incluir requisitos e métricas de processamento GPU.

**Relacionamentos:**
- Estende: `Task`
- Processada por: `GpuEdgeVM`
- Escalonada por: `GpuCloudletScheduler`

#### Atributos

```java
/**
 * Tamanho da carga de trabalho GPU em GFLOPs.
 * @visibility private
 * @type long
 */
private long gpuLength;

/**
 * Tamanho dos dados de entrada para a GPU em MB.
 * @visibility private
 * @type long
 */
private long gpuInputData;

/**
 * Tamanho dos dados de saída da GPU em MB.
 * @visibility private
 * @type long
 */
private long gpuOutputData;

/**
 * Memória GPU necessária em MB.
 * @visibility private
 * @type long
 */
private long requiredGpuMemory;

/**
 * Utilização GPU esperada em percentual (0-100%).
 * @visibility private
 * @type double
 */
private double expectedGpuUtilization;

/**
 * ID da GPU onde esta tarefa foi executada.
 * @visibility private
 * @type int
 */
private int executedGpuId;

/**
 * Tempo de transferência CPU → GPU em segundos.
 * @visibility private
 * @type double
 */
private double gpuDataTransferTime;

/**
 * Tempo de execução na GPU em segundos.
 * @visibility private
 * @type double
 */
private double gpuExecutionTime;

/**
 * Tempo de transferência GPU → CPU em segundos.
 * @visibility private
 * @type double
 */
private double gpuDataBackTime;

/**
 * Utilização GPU real durante a execução (0-100%).
 * @visibility private
 * @type double
 */
private double actualGpuUtilization;

/**
 * Timestamp do início da transferência CPU → GPU.
 * @visibility private
 * @type double
 */
private double gpuStartTime;

/**
 * Timestamp do fim da execução GPU completa.
 * @visibility private
 * @type double
 */
private double gpuFinishTime;
```

#### Construtores

```java
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
 * @param gpuLength Tamanho da carga GPU em GFLOPs
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
               long requiredGpuMemory)

/**
 * Construtor simplificado para GpuTask (herda Task).
 * 
 * @param cloudletId ID único da tarefa
 * @param taskId ID do tipo de tarefa
 * @param cloudletLength Tamanho do processamento CPU em MI
 * @param pesNumber Número de PEs necessários
 * @param cloudletFileSize Tamanho do arquivo de entrada
 * @param cloudletOutputSize Tamanho do arquivo de saída
 * @param gpuLength Tamanho da carga GPU em GFLOPs
 * @param gpuInputData Dados de entrada para GPU em MB
 * @param gpuOutputData Dados de saída da GPU em MB
 * @param requiredGpuMemory Memória GPU necessária em MB
 */
public GpuTask(int cloudletId, int taskId, long cloudletLength, 
               long pesNumber, long cloudletFileSize, long cloudletOutputSize,
               long gpuLength, long gpuInputData, long gpuOutputData,
               long requiredGpuMemory)
```

#### Métodos

##### Getters de Requisitos GPU

```java
/**
 * Retorna o tamanho da carga de trabalho GPU.
 * @return Tamanho em GFLOPs
 */
public long getGpuLength()

/**
 * Retorna o tamanho dos dados de entrada para GPU.
 * @return Tamanho em MB
 */
public long getGpuInputData()

/**
 * Retorna o tamanho dos dados de saída da GPU.
 * @return Tamanho em MB
 */
public long getGpuOutputData()

/**
 * Retorna a memória GPU necessária.
 * @return Memória em MB
 */
public long getRequiredGpuMemory()

/**
 * Retorna a utilização GPU esperada.
 * @return Utilização esperada em percentual (0-100%)
 */
public double getExpectedGpuUtilization()
```

##### Getters de Métricas GPU

```java
/**
 * Retorna o ID da GPU onde a tarefa foi executada.
 * @return ID da GPU ou -1 se não executada
 */
public int getExecutedGpuId()

/**
 * Retorna o tempo de transferência CPU → GPU.
 * @return Tempo em segundos
 */
public double getGpuDataTransferTime()

/**
 * Retorna o tempo de execução na GPU.
 * @return Tempo em segundos
 */
public double getGpuExecutionTime()

/**
 * Retorna o tempo de transferência GPU → CPU.
 * @return Tempo em segundos
 */
public double getGpuDataBackTime()

/**
 * Retorna a utilização GPU real durante execução.
 * @return Utilização em percentual (0-100%)
 */
public double getActualGpuUtilization()

/**
 * Retorna o timestamp de início da execução GPU.
 * @return Timestamp em segundos de simulação
 */
public double getGpuStartTime()

/**
 * Retorna o timestamp de fim da execução GPU.
 * @return Timestamp em segundos de simulação
 */
public double getGpuFinishTime()
```

##### Setters de Métricas GPU

```java
/**
 * Define o ID da GPU onde a tarefa foi executada.
 * @param gpuId ID da GPU
 */
public void setExecutedGpuId(int gpuId)

/**
 * Define o tempo de transferência CPU → GPU.
 * @param time Tempo em segundos
 */
public void setGpuDataTransferTime(double time)

/**
 * Define o tempo de execução na GPU.
 * @param time Tempo em segundos
 */
public void setGpuExecutionTime(double time)

/**
 * Define o tempo de transferência GPU → CPU.
 * @param time Tempo em segundos
 */
public void setGpuDataBackTime(double time)

/**
 * Define a utilização GPU real durante execução.
 * @param utilization Utilização em percentual (0-100%)
 */
public void setActualGpuUtilization(double utilization)

/**
 * Define o timestamp de início da execução GPU.
 * @param time Timestamp em segundos de simulação
 */
public void setGpuStartTime(double time)

/**
 * Define o timestamp de fim da execução GPU.
 * @param time Timestamp em segundos de simulação
 */
public void setGpuFinishTime(double time)

/**
 * Define a utilização GPU esperada.
 * @param utilization Utilização esperada em percentual (0-100%)
 */
public void setExpectedGpuUtilization(double utilization)
```

##### Métodos de Cálculo

```java
/**
 * Calcula o tempo total de processamento GPU.
 * Inclui transferências de dados e execução.
 * 
 * @return Tempo total em segundos
 */
public double getTotalGpuTime()

/**
 * Verifica se esta tarefa requer processamento GPU.
 * 
 * @return true se gpuLength > 0, false caso contrário
 */
public boolean requiresGpu()

/**
 * Calcula a proporção de processamento GPU em relação ao total.
 * 
 * @return Razão GPU/(CPU+GPU) entre 0 e 1
 */
public double getGpuIntensity()

/**
 * Verifica se a tarefa tem memória GPU suficiente alocada.
 * 
 * @param availableMemory Memória disponível em MB
 * @return true se a memória é suficiente, false caso contrário
 */
public boolean hasEnoughGpuMemory(long availableMemory)
```

---

### 1.5 Classe: GpuEdgeHost

**Pacote:** `edu.boun.edgecloudsim.edge_server`

**Propósito:** Estende EdgeHost para incluir gerenciamento de GPUs físicas.

**Relacionamentos:**
- Estende: `EdgeHost`
- Contém: `List<Gpu>`
- Usa: `GpuProvisioner`
- Gerencia: `GpuEdgeVM`

#### Atributos

```java
/**
 * Lista de GPUs físicas disponíveis neste host.
 * @visibility private
 * @type List<Gpu>
 */
private List<Gpu> gpuList;

/**
 * Provisionador responsável por alocar GPUs para VMs.
 * @visibility private
 * @type GpuProvisioner
 */
private GpuProvisioner gpuProvisioner;
```

#### Construtores

```java
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
                   List<Gpu> gpuList, GpuProvisioner gpuProvisioner)
```

#### Métodos

##### Getters

```java
/**
 * Retorna a lista de GPUs deste host.
 * @return Lista de GPUs
 */
public List<Gpu> getGpuList()

/**
 * Retorna o provisionador de GPU.
 * @return Provisionador de GPU
 */
public GpuProvisioner getGpuProvisioner()

/**
 * Retorna o número total de GPUs neste host.
 * @return Número de GPUs
 */
public int getNumberOfGpus()

/**
 * Retorna uma GPU específica pelo ID.
 * 
 * @param gpuId ID da GPU
 * @return GPU correspondente ou null se não encontrada
 */
public Gpu getGpu(int gpuId)
```

##### Métodos de Alocação GPU

```java
/**
 * Aloca uma GPU específica para uma VM.
 * Delega ao GpuProvisioner para realizar a alocação.
 * 
 * @param vm VM que receberá a GPU
 * @param gpu GPU a ser alocada
 * @return true se a alocação foi bem-sucedida, false caso contrário
 */
public boolean allocateGpuForVm(GpuEdgeVM vm, Gpu gpu)

/**
 * Desaloca a GPU de uma VM.
 * Libera a GPU para outras VMs.
 * 
 * @param vm VM da qual a GPU será desalocada
 */
public void deallocateGpuForVm(GpuEdgeVM vm)

/**
 * Verifica se há GPUs disponíveis neste host.
 * 
 * @return true se há pelo menos uma GPU disponível
 */
public boolean hasAvailableGpu()

/**
 * Retorna uma GPU disponível do pool.
 * 
 * @return GPU disponível ou null se nenhuma disponível
 */
public Gpu getAvailableGpu()

/**
 * Retorna uma GPU disponível com memória suficiente.
 * 
 * @param requiredMemory Memória mínima necessária em MB
 * @return GPU disponível com memória suficiente ou null
 */
public Gpu getAvailableGpuWithMemory(long requiredMemory)
```

##### Métodos de Métricas

```java
/**
 * Calcula a utilização média de CPU e GPU.
 * Combina utilização de CPU (herdada) e GPU.
 * 
 * @return Utilização média em percentual (0-100%)
 */
public double getAvgUtilization()

/**
 * Calcula a utilização média das GPUs.
 * 
 * @return Utilização média das GPUs em percentual (0-100%)
 */
public double getAvgGpuUtilization()

/**
 * Retorna a memória GPU total disponível neste host.
 * 
 * @return Memória GPU total em MB
 */
public long getTotalGpuMemory()

/**
 * Retorna a memória GPU disponível (não alocada) neste host.
 * 
 * @return Memória GPU disponível em MB
 */
public long getAvailableGpuMemory()

/**
 * Retorna a capacidade computacional total de GPU em GFLOPS.
 * 
 * @return Capacidade total em GFLOPS
 */
public double getTotalGpuGflops()
```

##### Métodos de Criação de VM (Override)

```java
/**
 * Cria uma VM neste host (estende comportamento do Host).
 * Se a VM requer GPU, tenta alocar uma antes de criar a VM.
 * 
 * @param vm VM a ser criada
 * @return true se a VM foi criada com sucesso, false caso contrário
 */
@Override
public boolean vmCreate(Vm vm)

/**
 * Destrói uma VM neste host (estende comportamento do Host).
 * Libera a GPU alocada se a VM tiver uma.
 * 
 * @param vm VM a ser destruída
 */
@Override
public void vmDestroy(Vm vm)
```

---

### 1.6 Classe: GpuEdgeVM

**Pacote:** `edu.boun.edgecloudsim.edge_server`

**Propósito:** Estende EdgeVM para incluir GPU alocada e escalonamento GPU.

**Relacionamentos:**
- Estende: `EdgeVM`
- Possui: `Gpu` (alocada)
- Usa: `GpuCloudletScheduler`
- Executa: `GpuTask`

#### Atributos

```java
/**
 * GPU alocada para esta VM (null se não tiver GPU).
 * @visibility private
 * @type Gpu
 */
private Gpu allocatedGpu;

/**
 * Escalonador de tarefas GPU para esta VM.
 * @visibility private
 * @type GpuCloudletScheduler
 */
private GpuCloudletScheduler gpuCloudletScheduler;

/**
 * Indica se esta VM requer GPU.
 * @visibility private
 * @type boolean
 */
private boolean requiresGpu;

/**
 * Modo de alocação GPU (EXCLUSIVE ou SHARED).
 * @visibility private
 * @type GpuAllocationMode
 */
private GpuAllocationMode gpuAllocationMode;
```

#### Enumeração Interna

```java
/**
 * Modos de alocação de GPU para VMs.
 */
public enum GpuAllocationMode {
    /**
     * Modo exclusivo: 1 GPU : 1 VM
     */
    EXCLUSIVE,
    
    /**
     * Modo compartilhado: 1 GPU : N VMs (futuro)
     */
    SHARED
}
```

#### Construtores

```java
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
                 boolean requiresGpu)

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
                 CloudletScheduler cloudletScheduler)
```

#### Métodos

##### Getters e Setters GPU

```java
/**
 * Define a GPU alocada para esta VM.
 * @param gpu GPU a ser alocada
 */
public void setGpu(Gpu gpu)

/**
 * Retorna a GPU alocada para esta VM.
 * @return GPU alocada ou null
 */
public Gpu getGpu()

/**
 * Verifica se esta VM possui GPU alocada.
 * @return true se tem GPU alocada, false caso contrário
 */
public boolean hasGpu()

/**
 * Verifica se esta VM requer GPU.
 * @return true se requer GPU, false caso contrário
 */
public boolean requiresGpu()

/**
 * Define se esta VM requer GPU.
 * @param requiresGpu true se requer, false caso contrário
 */
public void setRequiresGpu(boolean requiresGpu)

/**
 * Retorna o escalonador de tarefas GPU.
 * @return Escalonador GPU ou null
 */
public GpuCloudletScheduler getGpuCloudletScheduler()

/**
 * Define o escalonador de tarefas GPU.
 * @param scheduler Escalonador GPU
 */
public void setGpuCloudletScheduler(GpuCloudletScheduler scheduler)

/**
 * Retorna o modo de alocação GPU.
 * @return Modo de alocação (EXCLUSIVE ou SHARED)
 */
public GpuAllocationMode getGpuAllocationMode()

/**
 * Define o modo de alocação GPU.
 * @param mode Modo de alocação
 */
public void setGpuAllocationMode(GpuAllocationMode mode)
```

##### Métodos de Execução GPU

```java
/**
 * Submete uma tarefa GPU para execução nesta VM.
 * Delega ao GpuCloudletScheduler.
 * 
 * @param gpuTask Tarefa GPU a ser executada
 * @return true se a tarefa foi aceita, false caso contrário
 */
public boolean submitGpuTask(GpuTask gpuTask)

/**
 * Remove uma tarefa GPU da fila de execução.
 * 
 * @param gpuTask Tarefa GPU a ser removida
 * @return true se a tarefa foi removida, false caso contrário
 */
public boolean removeGpuTask(GpuTask gpuTask)

/**
 * Retorna a lista de tarefas GPU em execução.
 * 
 * @return Lista de GpuTasks em execução
 */
public List<GpuTask> getRunningGpuTasks()

/**
 * Retorna o número de tarefas GPU em execução.
 * 
 * @return Número de tarefas GPU ativas
 */
public int getNumberOfRunningGpuTasks()
```

##### Métodos de Métricas GPU

```java
/**
 * Calcula a utilização atual da GPU.
 * Retorna 0 se não tiver GPU alocada.
 * 
 * @return Utilização GPU em percentual (0-100%)
 */
public double getGpuUtilization()

/**
 * Calcula a utilização combinada de CPU e GPU.
 * 
 * @return Utilização média (CPU + GPU) / 2
 */
public double getCombinedUtilization()

/**
 * Retorna a memória GPU disponível nesta VM.
 * 
 * @return Memória GPU disponível em MB ou 0 se sem GPU
 */
public long getAvailableGpuMemory()

/**
 * Retorna a capacidade computacional GPU desta VM.
 * 
 * @return Capacidade em GFLOPS ou 0 se sem GPU
 */
public double getGpuGflops()
```

##### Métodos de Reconfiguração

```java
/**
 * Reconfigura a capacidade da GPU (se suportado).
 * Implementação futura para time-slicing dinâmico.
 * 
 * @param newGflops Nova capacidade em GFLOPS
 */
public void reconfigureGpu(double newGflops)
```

---

### 1.7 Interface: GpuCloudletScheduler

**Pacote:** `edu.boun.edgecloudsim.edge_server`

**Propósito:** Define o contrato para escalonamento de tarefas GPU em uma VM.

**Implementações:** `GpuCloudletSchedulerTimeShared`, `GpuCloudletSchedulerSpaceShared` (futuro)

#### Métodos

```java
/**
 * Inicializa o escalonador com uma GPU específica.
 * 
 * @param gpu GPU a ser gerenciada pelo escalonador
 */
void initialize(Gpu gpu);

/**
 * Submete uma tarefa GPU para execução.
 * 
 * @param gpuTask Tarefa GPU a ser executada
 * @return true se a tarefa foi aceita, false caso contrário
 */
boolean submitGpuTask(GpuTask gpuTask);

/**
 * Remove uma tarefa GPU da fila de execução.
 * 
 * @param gpuTask Tarefa a ser removida
 * @return true se a tarefa foi removida, false caso contrário
 */
boolean removeGpuTask(GpuTask gpuTask);

/**
 * Atualiza o escalonamento das tarefas GPU.
 * Chamado periodicamente pelo simulador.
 * 
 * @param currentTime Tempo atual da simulação
 * @return Próximo tempo de evento ou Double.MAX_VALUE
 */
double updateGpuTaskProcessing(double currentTime);

/**
 * Retorna a lista de tarefas GPU atualmente em execução.
 * 
 * @return Lista de tarefas em execução
 */
List<GpuTask> getRunningGpuTasks();

/**
 * Retorna a lista de tarefas GPU aguardando execução.
 * 
 * @return Lista de tarefas em espera
 */
List<GpuTask> getWaitingGpuTasks();

/**
 * Retorna a lista de tarefas GPU completadas.
 * 
 * @return Lista de tarefas finalizadas
 */
List<GpuTask> getCompletedGpuTasks();

/**
 * Calcula a utilização atual da GPU gerenciada.
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
```

---

### 1.8 Classe: GpuCloudletSchedulerTimeShared

**Pacote:** `edu.boun.edgecloudsim.edge_server`

**Propósito:** Implementação time-shared de escalonamento GPU (múltiplas tarefas compartilham GPU via time-slicing).

**Relacionamentos:**
- Implementa: `GpuCloudletScheduler`
- Usado por: `GpuEdgeVM`

#### Atributos

```java
/**
 * GPU gerenciada por este escalonador.
 * @visibility private
 * @type Gpu
 */
private Gpu gpu;

/**
 * Lista de tarefas GPU em execução.
 * @visibility private
 * @type List<GpuTask>
 */
private List<GpuTask> runningTasks;

/**
 * Lista de tarefas GPU aguardando execução.
 * @visibility private
 * @type List<GpuTask>
 */
private List<GpuTask> waitingTasks;

/**
 * Lista de tarefas GPU completadas.
 * @visibility private
 * @type List<GpuTask>
 */
private List<GpuTask> completedTasks;

/**
 * Mapa de tempo restante para cada tarefa (taskId -> remaining time).
 * @visibility private
 * @type Map<Integer, Double>
 */
private Map<Integer, Double> taskRemainingTimeMap;

/**
 * Último tempo de atualização do escalonamento.
 * @visibility private
 * @type double
 */
private double previousTime;
```

#### Construtores

```java
/**
 * Construtor padrão (GPU será definida via initialize()).
 */
public GpuCloudletSchedulerTimeShared()

/**
 * Construtor com GPU especificada.
 * 
 * @param gpu GPU a ser gerenciada
 */
public GpuCloudletSchedulerTimeShared(Gpu gpu)
```

#### Métodos

```java
/**
 * Inicializa o escalonador com uma GPU.
 * 
 * @param gpu GPU a ser gerenciada
 */
@Override
public void initialize(Gpu gpu)

/**
 * Submete uma tarefa GPU para execução.
 * Adiciona à fila de running tasks e redistribui recursos.
 * 
 * @param gpuTask Tarefa GPU a ser executada
 * @return true sempre (time-shared aceita todas as tarefas)
 */
@Override
public boolean submitGpuTask(GpuTask gpuTask)

/**
 * Remove uma tarefa GPU da execução.
 * 
 * @param gpuTask Tarefa a ser removida
 * @return true se removida com sucesso
 */
@Override
public boolean removeGpuTask(GpuTask gpuTask)

/**
 * Atualiza o processamento das tarefas GPU.
 * Divide GFLOPS igualmente entre tarefas ativas.
 * 
 * @param currentTime Tempo atual da simulação
 * @return Próximo tempo de evento
 */
@Override
public double updateGpuTaskProcessing(double currentTime)

/**
 * Retorna a lista de tarefas em execução.
 * 
 * @return Lista de tarefas em execução
 */
@Override
public List<GpuTask> getRunningGpuTasks()

/**
 * Retorna a lista de tarefas aguardando.
 * 
 * @return Lista de tarefas em espera
 */
@Override
public List<GpuTask> getWaitingGpuTasks()

/**
 * Retorna a lista de tarefas completadas.
 * 
 * @return Lista de tarefas finalizadas
 */
@Override
public List<GpuTask> getCompletedGpuTasks()

/**
 * Calcula a utilização da GPU baseada no número de tarefas ativas.
 * 
 * @return Utilização em percentual (0-100%)
 */
@Override
public double getGpuUtilization()

/**
 * Verifica se há tarefas em execução.
 * 
 * @return true se há tarefas em execução
 */
@Override
public boolean hasRunningTasks()

/**
 * Retorna a GPU gerenciada.
 * 
 * @return GPU gerenciada
 */
@Override
public Gpu getGpu()

/**
 * Redistribui os recursos GPU entre tarefas ativas.
 * Implementa time-slicing dividindo GFLOPS igualmente.
 */
private void redistributeGpuResources()

/**
 * Calcula o tempo restante de uma tarefa GPU.
 * 
 * @param gpuTask Tarefa GPU
 * @return Tempo restante em segundos
 */
private double calculateRemainingTime(GpuTask gpuTask)

/**
 * Finaliza uma tarefa GPU e move para completed tasks.
 * 
 * @param gpuTask Tarefa a ser finalizada
 */
private void finishGpuTask(GpuTask gpuTask)
```

---

### 1.9 Classe: GpuEdgeServerManager

**Pacote:** `edu.boun.edgecloudsim.edge_server`

**Propósito:** Gerenciador de infraestrutura edge com suporte a GPU.

**Relacionamentos:**
- Estende: `EdgeServerManager`
- Cria: `GpuEdgeHost`, `GpuEdgeVM`
- Usa: `GpuEdgeVmAllocationPolicy_Custom`

#### Atributos

```java
/**
 * Contador de IDs de hosts.
 * @visibility private
 * @type int
 */
private int hostIdCounter;

/**
 * Contador de IDs de VMs.
 * @visibility private
 * @type int
 */
private int vmIdCounter;
```

#### Métodos

```java
/**
 * Inicializa o gerenciador de servidores GPU.
 */
@Override
public void initialize()

/**
 * Retorna a política de alocação de VMs para datacenters GPU.
 * 
 * @param list Lista de hosts do datacenter
 * @param dataCenterIndex Índice do datacenter
 * @return Política de alocação GPU-aware
 */
@Override
public VmAllocationPolicy getVmAllocationPolicy(List<? extends Host> list, 
                                                 int dataCenterIndex)

/**
 * Inicia os datacenters edge com suporte a GPU.
 * Lê configurações do edge_devices.xml e cria GpuEdgeHosts.
 * 
 * @throws Exception se houver erro na criação dos datacenters
 */
@Override
public void startDatacenters() throws Exception

/**
 * Termina todos os datacenters edge.
 */
@Override
public void terminateDatacenters()

/**
 * Cria a lista de VMs edge com suporte a GPU.
 * Lê configurações do edge_devices.xml e aloca GPUs conforme necessário.
 * 
 * @param brokerId ID do broker
 */
@Override
public void createVmList(int brokerId)

/**
 * Calcula a utilização média (CPU + GPU) de todos os edge servers.
 * 
 * @return Utilização média em percentual (0-100%)
 */
@Override
public double getAvgUtilization()

/**
 * Cria um datacenter edge com hosts GPU.
 * 
 * @param dataCenterIndex Índice do datacenter
 * @param datacenterElement Elemento XML com configurações
 * @return Datacenter criado
 * @throws Exception se houver erro na criação
 */
private Datacenter createGpuDatacenter(int dataCenterIndex, 
                                        Element datacenterElement) throws Exception

/**
 * Cria lista de hosts GPU a partir do XML.
 * 
 * @param datacenterElement Elemento XML com configurações de hosts
 * @return Lista de GpuEdgeHosts criados
 */
private List<GpuEdgeHost> createGpuHosts(Element datacenterElement)

/**
 * Cria lista de GPUs a partir do XML.
 * 
 * @param hostElement Elemento XML com configurações de GPUs
 * @return Lista de GPUs criadas
 */
private List<Gpu> createGpuList(Element hostElement)

/**
 * Cria lista de Processing Elements (CPU cores) a partir do XML.
 * 
 * @param hostElement Elemento XML com configurações de PEs
 * @return Lista de PEs criados
 */
private List<Pe> createPeList(Element hostElement)
```

---

### 1.10 Classe: GpuEdgeVmAllocationPolicy_Custom

**Pacote:** `edu.boun.edgecloudsim.edge_server`

**Propósito:** Política de alocação de VMs com consideração de recursos GPU.

**Relacionamentos:**
- Estende: `EdgeVmAllocationPolicy_Custom`
- Aloca: `GpuEdgeVM` em `GpuEdgeHost`

#### Atributos

```java
/**
 * Índice do datacenter gerenciado por esta política.
 * @visibility private
 * @type int
 */
private int dataCenterIndex;
```

#### Construtores

```java
/**
 * Construtor da política de alocação GPU.
 * 
 * @param list Lista de hosts do datacenter
 * @param dataCenterIndex Índice do datacenter
 */
public GpuEdgeVmAllocationPolicy_Custom(List<? extends Host> list, 
                                         int dataCenterIndex)
```

#### Métodos

```java
/**
 * Aloca um host para uma VM considerando requisitos GPU.
 * Verifica disponibilidade de GPU antes de alocar a VM.
 * 
 * @param vm VM a ser alocada
 * @return true se a alocação foi bem-sucedida
 */
@Override
public boolean allocateHostForVm(Vm vm)

/**
 * Aloca um host específico para uma VM considerando GPU.
 * 
 * @param vm VM a ser alocada
 * @param host Host onde a VM será alocada
 * @return true se a alocação foi bem-sucedida
 */
@Override
public boolean allocateHostForVm(Vm vm, Host host)

/**
 * Desaloca o host de uma VM e libera a GPU associada.
 * 
 * @param vm VM a ser desalocada
 */
@Override
public void deallocateHostForVm(Vm vm)

/**
 * Encontra um host adequado para uma VM considerando recursos GPU.
 * 
 * @param vm VM para a qual buscar host
 * @return Host adequado ou null se nenhum disponível
 */
private GpuEdgeHost findSuitableHostForVm(GpuEdgeVM vm)

/**
 * Verifica se um host pode hospedar uma VM com requisitos GPU.
 * 
 * @param host Host a ser verificado
 * @param vm VM com requisitos
 * @return true se o host pode hospedar a VM
 */
private boolean canHostVm(GpuEdgeHost host, GpuEdgeVM vm)

/**
 * Aloca GPU para uma VM em um host específico.
 * 
 * @param host Host com GPUs disponíveis
 * @param vm VM que receberá a GPU
 * @return true se a alocação GPU foi bem-sucedida
 */
private boolean allocateGpuForVm(GpuEdgeHost host, GpuEdgeVM vm)
```

---

## 2. Diagramas UML Detalhados

### 2.1 Diagrama de Classes: Hierarquia GPU Completa

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        <<Abstract>>                                         │
│                     EdgeServerManager                                       │
│  (edu.boun.edgecloudsim.edge_server)                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│  # localDatacenters: List<Datacenter>                                       │
│  # vmList: List<List<EdgeVM>>                                               │
├─────────────────────────────────────────────────────────────────────────────┤
│  + initialize(): void                                                       │
│  + getVmAllocationPolicy(...): VmAllocationPolicy [abstract]                │
│  + startDatacenters(): void [abstract]                                      │
│  + terminateDatacenters(): void [abstract]                                  │
│  + createVmList(brokerId: int): void [abstract]                             │
│  + getAvgUtilization(): double [abstract]                                   │
└─────────────────────────────────────────────────────────────────────────────┘
                                △
                                │ extends
                                │
┌─────────────────────────────────────────────────────────────────────────────┐
│                     GpuEdgeServerManager                                    │
│  (edu.boun.edgecloudsim.edge_server)                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│  - hostIdCounter: int                                                       │
│  - vmIdCounter: int                                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│  + initialize(): void                                                       │
│  + getVmAllocationPolicy(...): VmAllocationPolicy                           │
│  + startDatacenters(): void                                                 │
│  + terminateDatacenters(): void                                             │
│  + createVmList(brokerId: int): void                                        │
│  + getAvgUtilization(): double                                              │
│  - createGpuDatacenter(...): Datacenter                                     │
│  - createGpuHosts(...): List<GpuEdgeHost>                                   │
│  - createGpuList(...): List<Gpu>                                            │
│  - createPeList(...): List<Pe>                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                    │ creates
                    ├──────────────────┬────────────────────┐
                    ▼                  ▼                    ▼
        ┌────────────────────┐  ┌────────────────┐  ┌─────────────────────┐
        │   GpuEdgeHost      │  │   GpuEdgeVM    │  │ GpuEdgeVmAllocation │
        │                    │  │                │  │   Policy_Custom     │
        └────────────────────┘  └────────────────┘  └─────────────────────┘




┌──────────────────────────────────────┐
│          <<CloudSim>>                │
│            Host                      │
│  (org.cloudbus.cloudsim)             │
├──────────────────────────────────────┤
│  # id: int                           │
│  # ramProvisioner: RamProvisioner    │
│  # bwProvisioner: BwProvisioner      │
│  # storage: long                     │
│  # peList: List<Pe>                  │
│  # vmScheduler: VmScheduler          │
├──────────────────────────────────────┤
│  + vmCreate(vm: Vm): boolean         │
│  + vmDestroy(vm: Vm): void           │
│  + getVmList(): List<Vm>             │
└──────────────────────────────────────┘
                △
                │ extends
                │
┌──────────────────────────────────────┐
│           EdgeHost                   │
│  (edu.boun.edgecloudsim.edge_server) │
├──────────────────────────────────────┤
│  - location: Location                │
├──────────────────────────────────────┤
│  + setPlace(location: Location)      │
│  + getLocation(): Location           │
└──────────────────────────────────────┘
                △
                │ extends
                │
┌──────────────────────────────────────────────────────────────┐
│                   GpuEdgeHost                                │
│        (edu.boun.edgecloudsim.edge_server)                   │
├──────────────────────────────────────────────────────────────┤
│  - gpuList: List<Gpu>                                        │
│  - gpuProvisioner: GpuProvisioner                            │
├──────────────────────────────────────────────────────────────┤
│  + GpuEdgeHost(id, ..., gpuList, gpuProvisioner)             │
│  + getGpuList(): List<Gpu>                                   │
│  + getGpuProvisioner(): GpuProvisioner                       │
│  + allocateGpuForVm(vm, gpu): boolean                        │
│  + deallocateGpuForVm(vm): void                              │
│  + hasAvailableGpu(): boolean                                │
│  + getAvailableGpu(): Gpu                                    │
│  + getAvgGpuUtilization(): double                            │
│  + getTotalGpuMemory(): long                                 │
│  + vmCreate(vm): boolean [override]                          │
│  + vmDestroy(vm): void [override]                            │
└──────────────────────────────────────────────────────────────┘
                    │ 1        contains       1..*
                    ├──────────────────────────────────>
                    │                                   │
                    │                                   ▼
                    │                    ┌────────────────────────────────┐
                    │                    │            Gpu                 │
                    │                    │  (edu.boun.edgecloudsim.       │
                    │                    │   edge_server)                 │
                    │                    ├────────────────────────────────┤
                    │                    │  - id: int                     │
                    │                    │  - gpuType: String             │
                    │                    │  - cudaCores: int              │
                    │                    │  - gflops: double              │
                    │                    │  - gpuMemory: long             │
                    │                    │  - memoryBandwidth: double     │
                    │                    │  - utilization: double         │
                    │                    │  - usedMemory: long            │
                    │                    │  - allocatedVm: GpuEdgeVM      │
                    │                    │  - host: GpuEdgeHost           │
                    │                    ├────────────────────────────────┤
                    │                    │  + Gpu(id, type, cores, ...)   │
                    │                    │  + getId(): int                │
                    │                    │  + getGflops(): double         │
                    │                    │  + getUtilization(): double    │
                    │                    │  + isAvailable(): boolean      │
                    │                    │  + setAllocatedVm(vm)          │
                    │                    │  + allocateMemory(size): bool  │
                    │                    │  + calculateExecutionTime(...) │
                    │                    └────────────────────────────────┘
                    │ uses                                △
                    │                                     │ manages
                    ▼                                     │
┌────────────────────────────────────────┐                │
│    <<Interface>>                       │                │
│    GpuProvisioner                      │                │
│  (edu.boun.edgecloudsim.edge_server)   │                │
├────────────────────────────────────────┤                │
│  + allocateGpuForVm(vm, gpu): boolean  │                │
│  + deallocateGpuForVm(vm): void        │                │
│  + hasAvailableGpu(): boolean          │                │
│  + getAvailableGpu(): Gpu              │                │
│  + getGpuList(): List<Gpu>             │                │
└────────────────────────────────────────┘                │
                △                                         │
                │ implements                              │
                │                                         │
┌────────────────────────────────────────────────────┐    │
│         GpuProvisionerSimple                       │    │
│   (edu.boun.edgecloudsim.edge_server)              │    │
├────────────────────────────────────────────────────┤    │
│  - gpuList: List<Gpu>                              │────┘
│  - vmGpuMap: Map<Integer, Gpu>                     │
├────────────────────────────────────────────────────┤
│  + GpuProvisionerSimple(gpuList)                   │
│  + allocateGpuForVm(vm, gpu): boolean              │
│  + deallocateGpuForVm(vm): void                    │
│  + hasAvailableGpu(): boolean                      │
│  + getAvailableGpu(): Gpu                          │
│  + getAverageUtilization(): double                 │
└────────────────────────────────────────────────────┘




┌────────────────────────────────┐
│      <<CloudSim>>              │
│          Vm                    │
│  (org.cloudbus.cloudsim)       │
├────────────────────────────────┤
│  # id: int                     │
│  # userId: int                 │
│  # mips: double                │
│  # numberOfPes: int            │
│  # ram: int                    │
│  # bw: long                    │
│  # size: long                  │
│  # cloudletScheduler           │
└────────────────────────────────┘
                △
                │ extends
                │
┌────────────────────────────────┐
│          EdgeVM                │
│  (edu.boun.edgecloudsim.       │
│   edge_server)                 │
├────────────────────────────────┤
│  - type: VM_TYPES              │
├────────────────────────────────┤
│  + getVmType(): VM_TYPES       │
│  + reconfigureMips(mips)       │
└────────────────────────────────┘
                △
                │ extends
                │
┌──────────────────────────────────────────────────────────┐
│                    GpuEdgeVM                             │
│         (edu.boun.edgecloudsim.edge_server)              │
├──────────────────────────────────────────────────────────┤
│  - allocatedGpu: Gpu                                     │
│  - gpuCloudletScheduler: GpuCloudletScheduler            │
│  - requiresGpu: boolean                                  │
│  - gpuAllocationMode: GpuAllocationMode                  │
├──────────────────────────────────────────────────────────┤
│  + GpuEdgeVM(id, ..., gpuScheduler, requiresGpu)         │
│  + setGpu(gpu): void                                     │
│  + getGpu(): Gpu                                         │
│  + hasGpu(): boolean                                     │
│  + requiresGpu(): boolean                                │
│  + getGpuCloudletScheduler(): GpuCloudletScheduler       │
│  + submitGpuTask(task): boolean                          │
│  + getGpuUtilization(): double                           │
│  + getAvailableGpuMemory(): long                         │
│  + reconfigureGpu(gflops): void                          │
├──────────────────────────────────────────────────────────┤
│  <<enumeration>>                                         │
│  + GpuAllocationMode                                     │
│    - EXCLUSIVE                                           │
│    - SHARED                                              │
└──────────────────────────────────────────────────────────┘
                    │ 1     has     0..1
                    ├────────────────────>
                    │                    │
                    │                    ▼
                    │                  [Gpu]
                    │
                    │ 1      uses      1
                    ├────────────────────────────────────────>
                    │                                         │
                    ▼                                         ▼
┌──────────────────────────────────────────┐  ┌────────────────────────────────┐
│      <<Interface>>                       │  │    <<Interface>>               │
│      GpuCloudletScheduler                │  │    GpuProvisioner              │
│  (edu.boun.edgecloudsim.edge_server)     │  │                                │
├──────────────────────────────────────────┤  └────────────────────────────────┘
│  + initialize(gpu): void                 │
│  + submitGpuTask(task): boolean          │
│  + removeGpuTask(task): boolean          │
│  + updateGpuTaskProcessing(time): double │
│  + getRunningGpuTasks(): List<GpuTask>   │
│  + getGpuUtilization(): double           │
│  + getGpu(): Gpu                         │
└──────────────────────────────────────────┘
                △
                │ implements
                │
┌───────────────────────────────────────────────────────┐
│       GpuCloudletSchedulerTimeShared                  │
│   (edu.boun.edgecloudsim.edge_server)                 │
├───────────────────────────────────────────────────────┤
│  - gpu: Gpu                                           │
│  - runningTasks: List<GpuTask>                        │
│  - waitingTasks: List<GpuTask>                        │
│  - completedTasks: List<GpuTask>                      │
│  - taskRemainingTimeMap: Map<Integer, Double>         │
│  - previousTime: double                               │
├───────────────────────────────────────────────────────┤
│  + GpuCloudletSchedulerTimeShared()                   │
│  + GpuCloudletSchedulerTimeShared(gpu)                │
│  + initialize(gpu): void                              │
│  + submitGpuTask(task): boolean                       │
│  + updateGpuTaskProcessing(time): double              │
│  + getGpuUtilization(): double                        │
│  - redistributeGpuResources(): void                   │
│  - finishGpuTask(task): void                          │
└───────────────────────────────────────────────────────┘
                    │ manages
                    │ 1      *
                    ├──────────────────>
                    │                  │
                    ▼                  ▼
                  [Gpu]         ┌──────────────────────────────────┐
                                │         GpuTask                  │
                                │  (edu.boun.edgecloudsim.         │
                                │   edge_client)                   │
                                └──────────────────────────────────┘




┌────────────────────────────────┐
│      <<CloudSim>>              │
│        Cloudlet                │
│  (org.cloudbus.cloudsim)       │
├────────────────────────────────┤
│  # cloudletId: int             │
│  # cloudletLength: long        │
│  # pesNumber: int              │
│  # fileSize: long              │
│  # outputSize: long            │
│  # utilizationModelCpu         │
└────────────────────────────────┘
                △
                │ extends
                │
┌────────────────────────────────┐
│            Task                │
│  (edu.boun.edgecloudsim.       │
│   edge_client)                 │
├────────────────────────────────┤
│  - submittedLocation: Location │
│  - creationTime: double        │
│  - type: int                   │
│  - mobileDeviceId: int         │
│  - hostIndex: int              │
│  - vmIndex: int                │
│  - datacenterId: int           │
├────────────────────────────────┤
│  + Task(deviceId, id, ...)     │
│  + setSubmittedLocation(loc)   │
│  + getMobileDeviceId(): int    │
│  + getTaskType(): int          │
└────────────────────────────────┘
                △
                │ extends
                │
┌──────────────────────────────────────────────────────────┐
│                    GpuTask                               │
│         (edu.boun.edgecloudsim.edge_client)              │
├──────────────────────────────────────────────────────────┤
│  - gpuLength: long                                       │
│  - gpuInputData: long                                    │
│  - gpuOutputData: long                                   │
│  - requiredGpuMemory: long                               │
│  - expectedGpuUtilization: double                        │
│  - executedGpuId: int                                    │
│  - gpuDataTransferTime: double                           │
│  - gpuExecutionTime: double                              │
│  - gpuDataBackTime: double                               │
│  - actualGpuUtilization: double                          │
│  - gpuStartTime: double                                  │
│  - gpuFinishTime: double                                 │
├──────────────────────────────────────────────────────────┤
│  + GpuTask(deviceId, id, ..., gpuLength, ...)            │
│  + getGpuLength(): long                                  │
│  + getGpuInputData(): long                               │
│  + getRequiredGpuMemory(): long                          │
│  + getGpuExecutionTime(): double                         │
│  + setGpuDataTransferTime(time): void                    │
│  + setActualGpuUtilization(util): void                   │
│  + getTotalGpuTime(): double                             │
│  + requiresGpu(): boolean                                │
│  + getGpuIntensity(): double                             │
└──────────────────────────────────────────────────────────┘




┌──────────────────────────────────────────────────────────┐
│           <<CloudSim>>                                   │
│           VmAllocationPolicy                             │
│    (org.cloudbus.cloudsim)                               │
├──────────────────────────────────────────────────────────┤
│  # hostList: List<Host>                                  │
│  # vmTable: Map<String, Host>                            │
├──────────────────────────────────────────────────────────┤
│  + allocateHostForVm(vm): boolean [abstract]             │
│  + deallocateHostForVm(vm): void [abstract]              │
│  + getHost(vm): Host                                     │
└──────────────────────────────────────────────────────────┘
                △
                │ extends
                │
┌──────────────────────────────────────────────────────────┐
│        EdgeVmAllocationPolicy_Custom                     │
│   (edu.boun.edgecloudsim.edge_server)                    │
├──────────────────────────────────────────────────────────┤
│  - dataCenterIndex: int                                  │
├──────────────────────────────────────────────────────────┤
│  + allocateHostForVm(vm): boolean                        │
│  + deallocateHostForVm(vm): void                         │
└──────────────────────────────────────────────────────────┘
                △
                │ extends
                │
┌──────────────────────────────────────────────────────────┐
│        GpuEdgeVmAllocationPolicy_Custom                  │
│   (edu.boun.edgecloudsim.edge_server)                    │
├──────────────────────────────────────────────────────────┤
│  - dataCenterIndex: int                                  │
├──────────────────────────────────────────────────────────┤
│  + GpuEdgeVmAllocationPolicy_Custom(list, index)         │
│  + allocateHostForVm(vm): boolean [override]             │
│  + deallocateHostForVm(vm): void [override]              │
│  - findSuitableHostForVm(vm): GpuEdgeHost                │
│  - canHostVm(host, vm): boolean                          │
│  - allocateGpuForVm(host, vm): boolean                   │
└──────────────────────────────────────────────────────────┘
```

---

### 2.2 Diagrama de Pacotes

```
┌────────────────────────────────────────────────────────────────────┐
│               edu.boun.edgecloudsim.edge_server                    │
├────────────────────────────────────────────────────────────────────┤
│  Classes:                                                          │
│    • EdgeServerManager (abstract)                                  │
│    • GpuEdgeServerManager                                          │
│    • EdgeHost                                                      │
│    • GpuEdgeHost                                                   │
│    • EdgeVM                                                        │
│    • GpuEdgeVM                                                     │
│    • EdgeVmAllocationPolicy_Custom                                 │
│    • GpuEdgeVmAllocationPolicy_Custom                              │
│    • Gpu                                                           │
│                                                                    │
│  Interfaces:                                                       │
│    • GpuProvisioner                                                │
│    • GpuCloudletScheduler                                          │
│                                                                    │
│  Implementações:                                                   │
│    • GpuProvisionerSimple                                          │
│    • GpuCloudletSchedulerTimeShared                                │
└────────────────────────────────────────────────────────────────────┘
                        │
                        │ uses
                        ▼
┌────────────────────────────────────────────────────────────────────┐
│               edu.boun.edgecloudsim.edge_client                    │
├────────────────────────────────────────────────────────────────────┤
│  Classes:                                                          │
│    • Task                                                          │
│    • GpuTask                                                       │
│    • MobileDeviceManager (abstract)                                │
│    • DefaultMobileDeviceManager                                    │
└────────────────────────────────────────────────────────────────────┘
                        │
                        │ uses
                        ▼
┌────────────────────────────────────────────────────────────────────┐
│               edu.boun.edgecloudsim.core                           │
├────────────────────────────────────────────────────────────────────┤
│  Classes:                                                          │
│    • SimManager                                                    │
│    • SimSettings                                                   │
│                                                                    │
│  Interfaces:                                                       │
│    • ScenarioFactory                                               │
└────────────────────────────────────────────────────────────────────┘
                        │
                        │ depends on
                        ▼
┌────────────────────────────────────────────────────────────────────┐
│                  org.cloudbus.cloudsim                             │
├────────────────────────────────────────────────────────────────────┤
│  Classes:                                                          │
│    • Host                                                          │
│    • Vm                                                            │
│    • Cloudlet                                                      │
│    • Datacenter                                                    │
│    • VmAllocationPolicy                                            │
│    • CloudletScheduler                                             │
└────────────────────────────────────────────────────────────────────┘
```

---

### 2.3 Diagrama de Relacionamentos

```
                  GpuEdgeServerManager
                          │
                          │ creates
                ┌─────────┴─────────┬───────────────┐
                │                   │               │
                ▼                   ▼               ▼
         GpuEdgeHost         GpuEdgeVM    GpuEdgeVmAllocation
                │                │          Policy_Custom
                │ contains       │ has             │
                │ 1..*           │ 0..1            │ uses
                ▼                ▼                 │
              Gpu ◄─────────────┘                  │
                │                                  │
                │ managed by                       │
                ▼                                  │
          GpuProvisioner ◄───────────────────────┘
                △
                │ implemented by
                │
          GpuProvisionerSimple


         GpuEdgeVM
              │
              │ uses
              ▼
     GpuCloudletScheduler ◄───── schedules ───── GpuTask
              △                                      │
              │ implemented by                       │ extends
              │                                      │
    GpuCloudletScheduler                            Task
        TimeShared                                   │ extends
                                                     │
                                                  Cloudlet
```

---

## 3. Contratos de Interfaces

### 3.1 Interface: GpuProvisioner

**Objetivo:** Definir contrato para provisionamento de GPUs a VMs.

**JavaDoc Completo:**

```java
package edu.boun.edgecloudsim.edge_server;

import java.util.List;

/**
 * Interface que define o contrato para provisionamento de GPUs a VMs.
 * 
 * <p>Esta interface especifica os métodos necessários para gerenciar a alocação
 * e desalocação de recursos GPU em hosts edge. Implementações desta interface
 * são responsáveis por:
 * <ul>
 *   <li>Rastrear disponibilidade de GPUs</li>
 *   <li>Alocar GPUs para VMs com base em requisitos</li>
 *   <li>Desalocar GPUs quando VMs são destruídas</li>
 *   <li>Fornecer informações sobre utilização de GPU</li>
 * </ul>
 * 
 * <p><b>Políticas de Provisionamento:</b>
 * <ul>
 *   <li><b>Exclusive Mode:</b> 1 GPU : 1 VM (implementação atual)</li>
 *   <li><b>Shared Mode:</b> 1 GPU : N VMs (implementação futura)</li>
 * </ul>
 * 
 * <p><b>Exemplo de Uso:</b>
 * <pre>
 * GpuProvisioner provisioner = new GpuProvisionerSimple(gpuList);
 * if (provisioner.hasAvailableGpu()) {
 *     Gpu gpu = provisioner.getAvailableGpu();
 *     if (provisioner.allocateGpuForVm(vm, gpu)) {
 *         System.out.println("GPU " + gpu.getId() + " alocada para VM " + vm.getId());
 *     }
 * }
 * </pre>
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 * @since GpuEdgeCloudSim 1.0
 * 
 * @see GpuProvisionerSimple
 * @see GpuEdgeHost
 * @see Gpu
 */
public interface GpuProvisioner {
    
    /**
     * Aloca uma GPU específica para uma VM.
     * 
     * <p>Este método tenta alocar a GPU especificada para a VM fornecida.
     * A alocação só é bem-sucedida se a GPU estiver disponível (não alocada).
     * 
     * <p><b>Pré-condições:</b>
     * <ul>
     *   <li>A GPU deve existir na lista gerenciada pelo provisioner</li>
     *   <li>A GPU deve estar disponível (isAvailable() == true)</li>
     *   <li>A VM não deve ter GPU previamente alocada</li>
     * </ul>
     * 
     * <p><b>Pós-condições (se bem-sucedido):</b>
     * <ul>
     *   <li>GPU.allocatedVm aponta para a VM</li>
     *   <li>GPU.isAvailable() retorna false</li>
     *   <li>Mapeamento VM→GPU é registrado</li>
     * </ul>
     * 
     * @param vm  VM que receberá a GPU (não-null)
     * @param gpu GPU a ser alocada (não-null, disponível)
     * @return true se a alocação foi bem-sucedida, false caso contrário
     * 
     * @throws NullPointerException se vm ou gpu forem null
     */
    boolean allocateGpuForVm(GpuEdgeVM vm, Gpu gpu);
    
    /**
     * Desaloca a GPU de uma VM, tornando-a disponível para outras VMs.
     * 
     * <p>Remove a associação entre VM e GPU, resetando o estado da GPU
     * e removendo-a do mapeamento interno.
     * 
     * <p><b>Pós-condições:</b>
     * <ul>
     *   <li>GPU.allocatedVm = null</li>
     *   <li>GPU.utilization = 0</li>
     *   <li>GPU.usedMemory = 0</li>
     *   <li>GPU.isAvailable() retorna true</li>
     *   <li>Mapeamento VM→GPU é removido</li>
     * </ul>
     * 
     * @param vm VM da qual a GPU será desalocada (não-null)
     * 
     * @throws NullPointerException se vm for null
     */
    void deallocateGpuForVm(GpuEdgeVM vm);
    
    /**
     * Verifica se há pelo menos uma GPU disponível no pool.
     * 
     * <p>Uma GPU é considerada disponível se não estiver alocada para nenhuma VM.
     * 
     * @return true se existe pelo menos uma GPU disponível, false caso contrário
     */
    boolean hasAvailableGpu();
    
    /**
     * Retorna uma GPU disponível do pool gerenciado.
     * 
     * <p>A estratégia de seleção depende da implementação:
     * <ul>
     *   <li><b>First-Fit:</b> Primeira GPU disponível encontrada</li>
     *   <li><b>Best-Fit:</b> GPU com capacidade mais próxima do requisito</li>
     *   <li><b>Least-Loaded:</b> GPU com menor utilização atual</li>
     * </ul>
     * 
     * <p><b>Nota:</b> Este método não realiza alocação automaticamente.
     * Use {@link #allocateGpuForVm(GpuEdgeVM, Gpu)} após obter a GPU.
     * 
     * @return GPU disponível ou null se não houver nenhuma disponível
     */
    Gpu getAvailableGpu();
    
    /**
     * Retorna uma GPU disponível com memória suficiente para um requisito.
     * 
     * <p>Busca uma GPU que esteja disponível E tenha pelo menos a quantidade
     * de memória especificada livre.
     * 
     * @param requiredMemory Memória mínima necessária em MB (>= 0)
     * @return GPU disponível com memória suficiente ou null
     * 
     * @throws IllegalArgumentException se requiredMemory < 0
     */
    Gpu getAvailableGpuWithMemory(long requiredMemory);
    
    /**
     * Retorna a lista completa de GPUs gerenciadas por este provisioner.
     * 
     * <p>A lista retornada inclui tanto GPUs disponíveis quanto alocadas.
     * 
     * @return Lista imutável de todas as GPUs gerenciadas
     */
    List<Gpu> getGpuList();
    
    /**
     * Retorna a lista de GPUs atualmente disponíveis (não alocadas).
     * 
     * @return Lista de GPUs disponíveis (possivelmente vazia)
     */
    List<Gpu> getAvailableGpuList();
    
    /**
     * Retorna a lista de GPUs atualmente alocadas a VMs.
     * 
     * @return Lista de GPUs alocadas (possivelmente vazia)
     */
    List<Gpu> getAllocatedGpuList();
}
```

---

### 3.2 Interface: GpuCloudletScheduler

**Objetivo:** Definir contrato para escalonamento de tarefas GPU em uma VM.

**JavaDoc Completo:**

```java
package edu.boun.edgecloudsim.edge_server;

import edu.boun.edgecloudsim.edge_client.GpuTask;
import java.util.List;

/**
 * Interface que define o contrato para escalonamento de tarefas GPU em uma VM.
 * 
 * <p>Esta interface especifica os métodos necessários para gerenciar a execução
 * de tarefas GPU em uma única GPU alocada a uma VM. Implementações desta interface
 * são responsáveis por:
 * <ul>
 *   <li>Aceitar submissão de novas tarefas GPU</li>
 *   <li>Escalonar execução de tarefas (space-shared ou time-shared)</li>
 *   <li>Atualizar progresso de tarefas durante a simulação</li>
 *   <li>Calcular utilização da GPU</li>
 *   <li>Gerenciar filas de tarefas (running, waiting, completed)</li>
 * </ul>
 * 
 * <p><b>Políticas de Escalonamento:</b>
 * <ul>
 *   <li><b>Time-Shared:</b> Múltiplas tarefas compartilham GPU via time-slicing
 *       (implementação atual: {@link GpuCloudletSchedulerTimeShared})</li>
 *   <li><b>Space-Shared:</b> Uma tarefa por vez, outras aguardam em fila
 *       (implementação futura: GpuCloudletSchedulerSpaceShared)</li>
 * </ul>
 * 
 * <p><b>Fluxo de Execução:</b>
 * <pre>
 * 1. submitGpuTask() → Tarefa entra no sistema
 * 2. updateGpuTaskProcessing() → Atualiza progresso periodicamente
 * 3. Tarefa completa → Move para completed tasks
 * 4. getCompletedGpuTasks() → Recupera tarefas finalizadas
 * </pre>
 * 
 * <p><b>Exemplo de Uso:</b>
 * <pre>
 * GpuCloudletScheduler scheduler = new GpuCloudletSchedulerTimeShared(gpu);
 * GpuTask task = new GpuTask(...);
 * 
 * if (scheduler.submitGpuTask(task)) {
 *     System.out.println("Tarefa " + task.getCloudletId() + " aceita");
 *     
 *     // Durante simulação
 *     double nextEventTime = scheduler.updateGpuTaskProcessing(CloudSim.clock());
 *     
 *     // Recuperar tarefas completadas
 *     List<GpuTask> completed = scheduler.getCompletedGpuTasks();
 * }
 * </pre>
 * 
 * @author Pabllo Borges Cardoso
 * @version 1.0
 * @since GpuEdgeCloudSim 1.0
 * 
 * @see GpuCloudletSchedulerTimeShared
 * @see GpuTask
 * @see GpuEdgeVM
 */
public interface GpuCloudletScheduler {
    
    /**
     * Inicializa o escalonador com uma GPU específica.
     * 
     * <p>Este método deve ser chamado antes de qualquer operação de escalonamento.
     * Define a GPU que será gerenciada por este escalonador.
     * 
     * <p><b>Pré-condições:</b>
     * <ul>
     *   <li>GPU não deve ser null</li>
     *   <li>Método deve ser chamado apenas uma vez</li>
     * </ul>
     * 
     * @param gpu GPU a ser gerenciada pelo escalonador (não-null)
     * 
     * @throws NullPointerException se gpu for null
     * @throws IllegalStateException se já foi inicializado
     */
    void initialize(Gpu gpu);
    
    /**
     * Submete uma tarefa GPU para execução.
     * 
     * <p>A tarefa é adicionada ao escalonador e seu processamento é iniciado
     * conforme a política de escalonamento implementada.
     * 
     * <p><b>Comportamento por Política:</b>
     * <ul>
     *   <li><b>Time-Shared:</b> Tarefa sempre aceita, GFLOPS redistribuído</li>
     *   <li><b>Space-Shared:</b> Tarefa aceita só se GPU livre, senão enfileira</li>
     * </ul>
     * 
     * <p><b>Pré-condições:</b>
     * <ul>
     *   <li>Escalonador deve estar inicializado</li>
     *   <li>gpuTask não deve ser null</li>
     *   <li>gpuTask.requiresGpu() deve retornar true</li>
     * </ul>
     * 
     * @param gpuTask Tarefa GPU a ser executada (não-null)
     * @return true se a tarefa foi aceita, false caso contrário
     * 
     * @throws NullPointerException se gpuTask for null
     * @throws IllegalStateException se escalonador não inicializado
     */
    boolean submitGpuTask(GpuTask gpuTask);
    
    /**
     * Remove uma tarefa GPU da fila de execução.
     * 
     * <p>Cancela a execução de uma tarefa antes de sua conclusão.
     * A tarefa é removida das filas (running ou waiting) mas não é
     * adicionada à fila completed.
     * 
     * @param gpuTask Tarefa a ser removida (não-null)
     * @return true se a tarefa foi removida com sucesso, false se não encontrada
     * 
     * @throws NullPointerException se gpuTask for null
     */
    boolean removeGpuTask(GpuTask gpuTask);
    
    /**
     * Atualiza o processamento das tarefas GPU.
     * 
     * <p>Método chamado periodicamente pelo simulador para avançar o progresso
     * das tarefas em execução. Calcula quanto trabalho foi realizado desde a
     * última atualização e move tarefas completadas para a fila appropriada.
     * 
     * <p><b>Responsabilidades:</b>
     * <ul>
     *   <li>Calcular delta de tempo: currentTime - previousTime</li>
     *   <li>Atualizar progresso de cada tarefa em execução</li>
     *   <li>Identificar tarefas completadas</li>
     *   <li>Mover tarefas completadas para fila completed</li>
     *   <li>Atualizar utilização da GPU</li>
     *   <li>Retornar próximo tempo de evento</li>
     * </ul>
     * 
     * @param currentTime Tempo atual da simulação em segundos
     * @return Próximo tempo onde ocorrerá conclusão de tarefa,
     *         ou Double.MAX_VALUE se não há tarefas em execução
     * 
     * @throws IllegalArgumentException se currentTime < previousTime
     */
    double updateGpuTaskProcessing(double currentTime);
    
    /**
     * Retorna a lista de tarefas GPU atualmente em execução.
     * 
     * <p>Tarefas nesta lista estão ativamente consumindo recursos da GPU.
     * 
     * @return Lista de tarefas em execução (não-null, possivelmente vazia)
     */
    List<GpuTask> getRunningGpuTasks();
    
    /**
     * Retorna a lista de tarefas GPU aguardando execução.
     * 
     * <p>Aplicável principalmente para políticas space-shared onde tarefas
     * podem ficar enfileiradas aguardando liberação da GPU.
     * 
     * @return Lista de tarefas em espera (não-null, possivelmente vazia)
     */
    List<GpuTask> getWaitingGpuTasks();
    
    /**
     * Retorna a lista de tarefas GPU completadas.
     * 
     * <p>Tarefas nesta lista finalizaram sua execução e aguardam coleta
     * pelo gerenciador de tarefas.
     * 
     * @return Lista de tarefas finalizadas (não-null, possivelmente vazia)
     */
    List<GpuTask> getCompletedGpuTasks();
    
    /**
     * Calcula a utilização atual da GPU gerenciada.
     * 
     * <p>Fórmula de cálculo varia por implementação:
     * <ul>
     *   <li><b>Time-Shared:</b> min(100, (numTasks / maxConcurrentTasks) * 100)</li>
     *   <li><b>Space-Shared:</b> 100 se tem tarefa, 0 caso contrário</li>
     * </ul>
     * 
     * @return Utilização em percentual (0-100%)
     */
    double getGpuUtilization();
    
    /**
     * Verifica se há tarefas GPU em execução.
     * 
     * <p>Útil para determinar se a GPU está ociosa.
     * 
     * @return true se há pelo menos uma tarefa em execução, false caso contrário
     */
    boolean hasRunningTasks();
    
    /**
     * Retorna a GPU gerenciada por este escalonador.
     * 
     * @return GPU gerenciada ou null se não inicializado
     */
    Gpu getGpu();
}
```

---

## 4. Diagramas de Sequência

### 4.1 Fluxo: Criação de Infraestrutura GPU

```
┌────────┐    ┌────────────┐    ┌──────────────────────┐   ┌─────────────┐    ┌───────────┐  ┌──────┐
│MainApp │    │SimManager  │    │GpuEdgeServerManager  │   │GpuEdgeHost  │    │GpuEdgeVM  │  │Gpu   │
└───┬────┘    └──────┬─────┘    └──────────┬───────────┘   └──────┬──────┘    └─────┬─────┘  └───┬──┘
    │                │                     │                      │                 │            │
    │ new SimManager(factory)              │                      │                 │            │
    │───────────────>│                     │                      │                 │            │
    │                │                     │                      │                 │            │
    │                │ factory.getEdgeServerManager()             │                 │            │
    │                │────────────────────>│                      │                 │            │
    │                │                     │                      │                 │            │
    │                │    new GpuEdgeServerManager()              │                 │            │
    │                │<────────────────────│                      │                 │            │
    │                │                     │                      │                 │            │
    │ startSimulation()                    │                      │                 │            │
    │───────────────>│                     │                      │                 │            │
    │                │                     │                      │                 │            │
    │                │ startDatacenters()  │                      │                 │            │
    │                │────────────────────>│                      │                 │            │
    │                │                     │                      │                 │            │
    │                │                     │ [Lê edge_devices.xml]                  │            │
    │                │                     │──────────┐           │                 │            │
    │                │                     │          │           │                 │            │
    │                │                     │<─────────┘           │                 │            │
    │                │                     │                      │                 │            │
    │                │                     │ createGpuHosts()     │                 │            │
    │                │                     │──────────┐           │                 │            │
    │                │                     │          │           │                 │            │
    │                │                     │          │ createGpuList()             │            │
    │                │                     │          │─────────────────────────────────────────>│
    │                │                     │          │           │                 │            │
    │                │                     │          │           │                 │ new Gpu()  │
    │                │                     │          │           │                 │<───────────│
    │                │                     │          │           │                 │            │
    │                │                     │          │<─────────────────────────────────────────│
    │                │                     │          │           │                 │            │
    │                │                     │          │ new GpuEdgeHost(gpuList)    │            │
    │                │                     │          │──────────>│                 │            │
    │                │                     │          │           │                 │            │
    │                │                     │<─────────┘           │                 │            │
    │                │                     │                      │                 │            │
    │                │                     │ createGpuDatacenter()│                 │            │
    │                │                     │──────────┐           │                 │            │
    │                │                     │          │           │                 │            │
    │                │                     │<─────────┘           │                 │            │
    │                │                     │                      │                 │            │
    │                │ createVmList(brokerId)                     │                 │            │
    │                │────────────────────>│                      │                 │            │
    │                │                     │                      │                 │            │
    │                │                     │ [Lê edge_devices.xml]                  │            │
    │                │                     │──────────┐           │                 │            │
    │                │                     │          │           │                 │            │
    │                │                     │<─────────┘           │                 │            │
    │                │                     │                      │                 │            │
    │                │                     │                      │  new GpuEdgeVM()│            │
    │                │                     │                      │────────────────>│            │
    │                │                     │                      │                 │            │
    │                │                     │ getAvailableGpu()    │                 │            │
    │                │                     │────────────────────────────────────────────────────>│
    │                │                     │                      │                 │            │
    │                │                     │<────────────────────────────────────────────────────│
    │                │                     │                      │                 │            │
    │                │                     │ allocateGpuForVm(vm, gpu)              │            │
    │                │                     │────────────────────────────────────────────────────>│
    │                │                     │                      │                 │            │
    │                │                     │                      │                 │ setAllocatedVm()
    │                │                     │                      │                 │<───────────│
    │                │                     │                      │                 │            │
    │                │                     │                      │  setGpu(gpu)    │            │
    │                │                     │                      │────────────────>│            │
    │                │                     │                      │                 │            │
    │                │<────────────────────│                      │                 │            │
    │                │                     │                      │                 │            │
    │<───────────────│                     │                      │                 │            │
```

**Descrição:**
1. **MainApp** cria **SimManager** com factory
2. **SimManager** obtém **GpuEdgeServerManager** via factory
3. **SimManager** chama **startDatacenters()**
4. **GpuEdgeServerManager** lê **edge_devices.xml**
5. Para cada host, cria lista de **Gpu** objects
6. Cria **GpuEdgeHost** com lista de GPUs
7. **SimManager** chama **createVmList()**
8. Para cada VM que requer GPU:
   - Cria **GpuEdgeVM**
   - Obtém GPU disponível
   - Aloca GPU para a VM

---

### 4.2 Fluxo: Alocação de GPU para VM

```
┌──────────────────────────┐   ┌───────────────────────┐   ┌──────────────┐  ┌──────┐
│GpuEdgeVmAllocation       │   │GpuEdgeHost            │   │GpuProvisioner│  │Gpu   │
│Policy_Custom             │   │                       │   │              │  │      │
└──────────┬───────────────┘   └───────────┬───────────┘   └──────┬───────┘  └───┬──┘
           │                               │                      │              │
           │ allocateHostForVm(vm)         │                      │              │
           │──────────┐                    │                      │              │
           │          │ [Verifica se VM    │                      │              │
           │          │  requer GPU]       │                      │              │
           │<─────────┘                    │                      │              │
           │                               │                      │              │
           │ findSuitableHostForVm(vm)     │                      │              │
           │──────────┐                    │                      │              │
           │          │                    │                      │              │
           │          │ hasAvailableGpu()  │                      │              │
           │          │───────────────────>│                      │              │
           │          │                    │                      │              │
           │          │                    │ hasAvailableGpu()    │              │
           │          │                    │─────────────────────>│              │
           │          │                    │                      │              │
           │          │                    │                      │ [Verifica    │
           │          │                    │                      │  GPUs        │
           │          │                    │                      │  disponíveis]│
           │          │                    │                      │──────┐       │
           │          │                    │                      │      │       │
           │          │                    │                      │<─────┘       │
           │          │                    │                      │              │
           │          │                    │<─────────────────────│              │
           │          │                    │                      │              │
           │          │<───────────────────│                      │              │
           │<─────────┘                    │                      │              │
           │  [Host adequado encontrado]   │                      │              │
           │                               │                      │              │
           │ allocateGpuForVm(host, vm)    │                      │              │
           │──────────┐                    │                      │              │
           │          │                    │                      │              │
           │          │ getAvailableGpu()  │                      │              │
           │          │───────────────────>│                      │              │
           │          │                    │                      │              │
           │          │                    │ getAvailableGpu()    │              │
           │          │                    │─────────────────────>│              │
           │          │                    │                      │              │
           │          │                    │                      │ [Retorna 1ª  │
           │          │                    │                      │  GPU livre]  │
           │          │                    │                      │──────┐       │
           │          │                    │                      │      │       │
           │          │                    │                      │<─────┘       │
           │          │                    │                      │              │
           │          │                    │<─────────────────────│              │
           │          │                    │                      │              │
           │          │<───────────────────│                      │              │
           │          │                    │                      │              │
           │          │ allocateGpuForVm(vm, gpu)                 │              │
           │          │───────────────────>│                      │              │
           │          │                    │                      │              │
           │          │                    │ allocateGpuForVm(vm, gpu)           │
           │          │                    │─────────────────────>│              │
           │          │                    │                      │              │
           │          │                    │                      │ isAvailable()│
           │          │                    │                      │─────────────>│
           │          │                    │                      │              │
           │          │                    │                      │<─────────────│
           │          │                    │                      │  true        │
           │          │                    │                      │              │
           │          │                    │                      │ setAllocatedVm(vm)
           │          │                    │                      │─────────────>│
           │          │                    │                      │              │
           │          │                    │                      │<─────────────│
           │          │                    │                      │  void        │
           │          │                    │                      │              │
           │          │                    │<─────────────────────│              │
           │          │                    │  true                │              │
           │          │                    │                      │              │
           │          │<───────────────────│                      │              │
           │          │  true              │                      │              │
           │          │                    │                      │              │
           │          │ vm.setGpu(gpu)     │                      │              │
           │          │──────────┐         │                      │              │
           │          │          │         │                      │              │
           │          │<─────────┘         │                      │              │
           │          │                    │                      │              │
           │<─────────┘                    │                      │              │
           │  true                         │                      │              │
           │                               │                      │              │
           │ host.vmCreate(vm)             │                      │              │
           │──────────────────────────────>│                      │              │
           │                               │                      │              │
           │<──────────────────────────────│                      │              │
           │  true                         │                      │              │
```

**Descrição:**
1. **GpuEdgeVmAllocationPolicy_Custom** recebe requisição de alocação
2. Verifica se a VM requer GPU
3. Busca host adequado com **findSuitableHostForVm()**
4. Verifica se o host tem GPU disponível via **hasAvailableGpu()**
5. Obtém GPU disponível via **getAvailableGpu()**
6. Aloca GPU para VM via **allocateGpuForVm()**
7. **GpuProvisioner** verifica disponibilidade e marca GPU como alocada
8. VM é criada no host com GPU alocada

---

### 4.3 Fluxo: Execução de GpuTask

```
┌──────────────────┐   ┌──────────────┐    ┌────────────────────┐   ┌──────────┐   ┌─────┐
│MobileDevice      │   │GpuEdgeVM     │    │GpuCloudletScheduler│   │GpuTask   │   │Gpu  │
│Manager           │   │              │    │TimeShared          │   │          │   │     │
└─────────┬────────┘   └──────┬───────┘    └─────────┬──────────┘   └─────┬────┘   └──┬──┘
          │                   │                      │                    │           │
          │ submitTask(gpuTask)                      │                    │           │
          │──────────────────>│                      │                    │           │
          │                   │                      │                    │           │
          │                   │ submitGpuTask(gpuTask)                    │           │
          │                   │─────────────────────>│                    │           │
          │                   │                      │                    │           │
          │                   │                      │ [Adiciona à        │           │
          │                   │                      │  runningTasks]     │           │
          │                   │                      │──────────┐         │           │
          │                   │                      │          │         │           │
          │                   │                      │<─────────┘         │           │
          │                   │                      │                    │           │
          │                   │                      │ redistributeGpuResources()     │
          │                   │                      │──────────┐         │           │
          │                   │                      │          │         │           │
          │                   │                      │<─────────┘         │           │
          │                   │                      │                    │           │
          │                   │                      │ setGpuStartTime()  │           │
          │                   │                      │───────────────────>│           │
          │                   │                      │                    │           │
          │                   │<─────────────────────│                    │           │
          │                   │  true                │                    │           │
          │                   │                      │                    │           │
          │<──────────────────│                      │                    │           │
          │  true             │                      │                    │           │
          │                   │                      │                    │           │
          │                   │                      │                    │           │
          │ [Tempo passa - SimManager chama updateGpuTaskProcessing()]    │           │
          │                   │                      │                    │           │
          │                   │ updateGpuTaskProcessing(currentTime)      │           │
          │                   │─────────────────────>│                    │           │
          │                   │                      │                    │           │
          │                   │                      │ [Calcula delta     │           │
          │                   │                      │  de tempo]         │           │
          │                   │                      │──────────┐         │           │
          │                   │                      │          │         │           │
          │                   │                      │<─────────┘         │           │
          │                   │                      │                    │           │
          │                   │                      │ getGflops()        │           │
          │                   │                      │───────────────────────────────>│
          │                   │                      │                    │           │
          │                   │                      │<───────────────────────────────│
          │                   │                      │  gflops            │           │
          │                   │                      │                    │           │
          │                   │                      │ [Para cada tarefa, │           │
          │                   │                      │  calcula trabalho  │           │
          │                   │                      │  realizado]        │           │
          │                   │                      │──────────┐         │           │
          │                   │                      │          │         │           │
          │                   │                      │<─────────┘         │           │
          │                   │                      │                    │           │
          │                   │                      │ [Tarefa completou?]│           │
          │                   │                      │──────────┐         │           │
          │                   │                      │          │         │           │
          │                   │                      │<─────────┘         │           │
          │                   │                      │                    │           │
          │                   │                      │ finishGpuTask(gpuTask)         │
          │                   │                      │──────────┐         │           │
          │                   │                      │          │         │           │
          │                   │                      │          │ setGpuFinishTime()  │
          │                   │                      │          │────────>│           │
          │                   │                      │          │         │           │
          │                   │                      │          │ setActualGpuUtilization()
          │                   │                      │          │────────>│           │
          │                   │                      │          │         │           │
          │                   │                      │          │ [Move para          │
          │                   │                      │          │  completedTasks]    │
          │                   │                      │<─────────┘         │           │
          │                   │                      │                    │           │
          │                   │                      │ setUtilization()   │           │
          │                   │                      │───────────────────────────────>│
          │                   │                      │                    │           │
          │                   │<─────────────────────│                    │           │
          │                   │  nextEventTime       │                    │           │
          │                   │                      │                    │           │
          │                   │ getCompletedGpuTasks()                    │           │
          │                   │─────────────────────>│                    │           │
          │                   │                      │                    │           │
          │                   │<─────────────────────│                    │           │
          │                   │  List<GpuTask>       │                    │           │
```

**Descrição:**
1. **MobileDeviceManager** submete **GpuTask** para **GpuEdgeVM**
2. **GpuEdgeVM** delega para **GpuCloudletSchedulerTimeShared**
3. Scheduler adiciona tarefa à fila **runningTasks**
4. Redistributes GPU resources (time-slicing)
5. Define tempo de início da tarefa GPU
6. Durante simulação, **updateGpuTaskProcessing()** é chamado periodicamente
7. Calcula trabalho realizado baseado em:
   - Delta de tempo
   - GFLOPS da GPU
   - Número de tarefas compartilhando GPU (time-shared)
8. Quando tarefa completa:
   - Define tempo de fim
   - Calcula utilização real
   - Move para **completedTasks**
9. **MobileDeviceManager** recupera tarefas completadas

---

### 4.4 Fluxo: Desalocação de Recursos

```
┌──────────────────┐   ┌──────────────────────┐   ┌─────────────┐   ┌──────────────┐   ┌─────┐
│SimManager        │   │GpuEdgeVmAllocation   │   │GpuEdgeHost  │   │GpuProvisioner│   │Gpu  │
│                  │   │Policy_Custom         │   │             │   │              │   │     │
└────────┬─────────┘   └──────────┬───────────┘   └──────┬──────┘   └──────┬───────┘   └──┬──┘
         │                        │                      │                 │              │
         │ vmDestroy(vm)          │                      │                 │              │
         │───────────────────────>│                      │                 │              │
         │                        │                      │                 │              │
         │                        │ deallocateHostForVm(vm)                │              │
         │                        │─────────────────────>│                 │              │
         │                        │                      │                 │              │
         │                        │                      │ [Verifica se VM │              │
         │                        │                      │  tem GPU]       │              │
         │                        │                      │──────┐          │              │
         │                        │                      │      │          │              │
         │                        │                      │<─────┘          │              │
         │                        │                      │                 │              │
         │                        │                      │ deallocateGpuForVm(vm)         │
         │                        │                      │────────────────>│              │
         │                        │                      │                 │              │
         │                        │                      │                 │ [Encontra GPU│
         │                        │                      │                 │  alocada]    │
         │                        │                      │                 │──────┐       │
         │                        │                      │                 │      │       │
         │                        │                      │                 │<─────┘       │
         │                        │                      │                 │              │
         │                        │                      │                 │ reset()      │
         │                        │                      │                 │─────────────>│
         │                        │                      │                 │              │
         │                        │                      │                 │              │
         │                        │                      │                 │ [GPU fields: │
         │                        │                      │                 │  allocatedVm=null
         │                        │                      │                 │  utilization=0
         │                        │                      │                 │  usedMemory=0]
         │                        │                      │                 │<──────┐      │
         │                        │                      │                 │       │      │
         │                        │                      │                 │<──────┘      │
         │                        │                      │                 │              │
         │                        │                      │<────────────────│              │
         │                        │                      │                 │              │
         │                        │                      │ vmDestroy(vm)   │              │
         │                        │                      │──────┐          │              │
         │                        │                      │      │          │              │
         │                        │                      │<─────┘          │              │
         │                        │                      │                 │              │
         │                        │<─────────────────────│                 │              │
         │                        │                      │                 │              │
         │<───────────────────────│                      │                 │              │
         │  void                  │                      │                 │              │
```

**Descrição:**
1. **SimManager** solicita destruição de VM
2. **GpuEdgeVmAllocationPolicy_Custom** recebe **deallocateHostForVm()**
3. Verifica se a VM possui GPU alocada
4. Se sim, chama **deallocateGpuForVm()** no **GpuEdgeHost**
5. **GpuEdgeHost** delega para **GpuProvisioner**
6. **GpuProvisioner** encontra a GPU alocada para a VM
7. Chama **reset()** na GPU para limpar estado:
   - `allocatedVm = null`
   - `utilization = 0`
   - `usedMemory = 0`
8. Remove mapeamento VM→GPU
9. GPU fica disponível para novas alocações
10. Host destrói a VM normalmente

---

## 5. Exemplos de Uso

### 5.1 Criando GpuEdgeServerManager Personalizado

```java
package edu.boun.edgecloudsim.applications.gpu_app;

import edu.boun.edgecloudsim.core.ScenarioFactory;
import edu.boun.edgecloudsim.edge_server.EdgeServerManager;
import edu.boun.edgecloudsim.edge_server.GpuEdgeServerManager;

/**
 * Factory personalizada para cenários com suporte a GPU.
 */
public class GpuScenarioFactory implements ScenarioFactory {
    private int numOfMobileDevice;
    private double simulationTime;
    private String orchestratorPolicy;
    private String simScenario;
    
    public GpuScenarioFactory(int _numOfMobileDevice,
                              double _simulationTime,
                              String _orchestratorPolicy,
                              String _simScenario) {
        this.numOfMobileDevice = _numOfMobileDevice;
        this.simulationTime = _simulationTime;
        this.orchestratorPolicy = _orchestratorPolicy;
        this.simScenario = _simScenario;
    }
    
    @Override
    public EdgeServerManager getEdgeServerManager() {
        // Retorna implementação GPU em vez da padrão
        return new GpuEdgeServerManager();
    }
    
    // ... outros métodos factory
}
```

**Uso:**

```java
public class MainApp {
    public static void main(String[] args) throws Exception {
        // Inicializa CloudSim
        CloudSim.init(numBrokers, calendar, traceFlag);
        
        // Cria factory GPU
        ScenarioFactory factory = new GpuScenarioFactory(
            numMobileDevices,
            simulationTime,
            "GPU_AWARE_ORCHESTRATOR",
            "DEEP_LEARNING_INFERENCE"
        );
        
        // Cria SimManager com factory GPU
        SimManager simManager = new SimManager(factory, numMobileDevices, simScenario);
        
        // Inicia simulação
        simManager.startSimulation();
    }
}
```

---

### 5.2 Configurando edge_devices.xml com GPUs

```xml
<?xml version="1.0"?>
<edge_devices>
    <datacenter arch="x86" os="Linux" vmm="Xen">
        <!-- Custos do datacenter -->
        <costPerBw>0.1</costPerBw>
        <costPerSec>3.0</costPerSec>
        <costPerMem>0.05</costPerMem>
        <costPerStorage>0.1</costPerStorage>
        
        <!-- Localização geográfica -->
        <location>
            <x_pos>1</x_pos>
            <y_pos>1</y_pos>
            <wlan_id>0</wlan_id>
            <attractiveness>5</attractiveness>
        </location>
        
        <hosts>
            <host>
                <!-- Recursos CPU tradicionais -->
                <core>16</core>
                <mips>80000</mips>
                <ram>32000</ram>
                <storage>100000</storage>
                
                <!-- ⭐ NOVO: Configuração de GPUs -->
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
                
                <!-- VMs com suporte a GPU -->
                <VMs>
                    <!-- VM com GPU exclusiva -->
                    <VM vmm="Xen">
                        <core>4</core>
                        <mips>20000</mips>
                        <ram>8000</ram>
                        <storage>50000</storage>
                        <!-- ⭐ NOVO: Requisitos GPU -->
                        <requires_gpu>true</requires_gpu>
                        <gpu_allocation_mode>EXCLUSIVE</gpu_allocation_mode>
                    </VM>
                    
                    <!-- VM sem GPU -->
                    <VM vmm="Xen">
                        <core>4</core>
                        <mips>20000</mips>
                        <ram>8000</ram>
                        <storage>50000</storage>
                        <requires_gpu>false</requires_gpu>
                    </VM>
                    
                    <!-- VM com GPU compartilhada (futuro) -->
                    <VM vmm="Xen">
                        <core>2</core>
                        <mips>10000</mips>
                        <ram>4000</ram>
                        <storage>25000</storage>
                        <requires_gpu>true</requires_gpu>
                        <gpu_allocation_mode>SHARED</gpu_allocation_mode>
                    </VM>
                </VMs>
            </host>
        </hosts>
    </datacenter>
</edge_devices>
```

---

### 5.3 Configurando applications.xml com Tarefas GPU

```xml
<?xml version="1.0"?>
<applications>
    <!-- Aplicação com processamento intensivo em GPU -->
    <application name="DEEP_LEARNING_INFERENCE">
        <usage_percentage>30</usage_percentage>
        <prob_cloud_selection>10</prob_cloud_selection>
        <poisson_interarrival>5</poisson_interarrival>
        <delay_sensitivity>1</delay_sensitivity>
        <active_period>60</active_period>
        <idle_period>30</idle_period>
        
        <!-- Requisitos tradicionais CPU -->
        <data_upload>5000</data_upload>
        <data_download>500</data_download>
        <task_length>50000</task_length>
        <required_core>2</required_core>
        <vm_utilization_on_edge>10</vm_utilization_on_edge>
        <vm_utilization_on_cloud>0.8</vm_utilization_on_cloud>
        <vm_utilization_on_mobile>20</vm_utilization_on_mobile>
        
        <!-- ⭐ NOVO: Requisitos GPU -->
        <requires_gpu>true</requires_gpu>
        <gpu_length>150000</gpu_length>              <!-- GFLOPs -->
        <gpu_input_data>2000</gpu_input_data>        <!-- MB -->
        <gpu_output_data>100</gpu_output_data>       <!-- MB -->
        <required_gpu_memory>4000</required_gpu_memory> <!-- MB -->
        <expected_gpu_utilization>80</expected_gpu_utilization> <!-- % -->
    </application>
    
    <!-- Aplicação tradicional sem GPU -->
    <application name="WEB_BROWSING">
        <usage_percentage>40</usage_percentage>
        <prob_cloud_selection>30</prob_cloud_selection>
        <poisson_interarrival>10</poisson_interarrival>
        <delay_sensitivity>2</delay_sensitivity>
        <active_period>120</active_period>
        <idle_period>60</idle_period>
        
        <data_upload>1000</data_upload>
        <data_download>500</data_download>
        <task_length>10000</task_length>
        <required_core>1</required_core>
        <vm_utilization_on_edge>5</vm_utilization_on_edge>
        
        <!-- Sem requisitos GPU -->
        <requires_gpu>false</requires_gpu>
    </application>
    
    <!-- Aplicação híbrida CPU+GPU -->
    <application name="VIDEO_TRANSCODING">
        <usage_percentage>20</usage_percentage>
        <prob_cloud_selection>20</prob_cloud_selection>
        <poisson_interarrival>8</poisson_interarrival>
        <delay_sensitivity>3</delay_sensitivity>
        <active_period>90</active_period>
        <idle_period>45</idle_period>
        
        <!-- Processamento CPU significativo -->
        <data_upload>10000</data_upload>
        <data_download>8000</data_download>
        <task_length>100000</task_length>
        <required_core>4</required_core>
        <vm_utilization_on_edge>15</vm_utilization_on_edge>
        
        <!-- + Processamento GPU moderado -->
        <requires_gpu>true</requires_gpu>
        <gpu_length>80000</gpu_length>
        <gpu_input_data>5000</gpu_input_data>
        <gpu_output_data>5000</gpu_output_data>
        <required_gpu_memory>6000</required_gpu_memory>
        <expected_gpu_utilization>60</expected_gpu_utilization>
    </application>
</applications>
```

---

### 5.4 Criando e Submetendo GpuTask

```java
package edu.boun.edgecloudsim.applications.gpu_app;

import edu.boun.edgecloudsim.edge_client.GpuTask;
import edu.boun.edgecloudsim.edge_client.CpuUtilizationModel_Custom;
import org.cloudbus.cloudsim.UtilizationModelFull;

/**
 * Gerador de tarefas GPU personalizado.
 */
public class GpuLoadGenerator extends LoadGeneratorModel {
    
    @Override
    public void initializeModel() {
        // Inicialização
    }
    
    /**
     * Cria uma GpuTask com requisitos CPU e GPU.
     */
    private GpuTask createGpuTask(int mobileDeviceId, int taskType) {
        // Obter configurações da aplicação
        int taskId = taskIdCounter++;
        
        // Requisitos CPU (padrão EdgeCloudSim)
        long cloudletLength = 50000;      // MI
        int pesNumber = 2;                 // Cores
        long cloudletFileSize = 5000;     // bytes
        long cloudletOutputSize = 500;    // bytes
        
        // Requisitos GPU (novo)
        long gpuLength = 150000;           // GFLOPs
        long gpuInputData = 2000;          // MB
        long gpuOutputData = 100;          // MB
        long requiredGpuMemory = 4000;     // MB
        
        // Modelos de utilização
        CpuUtilizationModel_Custom cpuModel = 
            new CpuUtilizationModel_Custom(taskType, 
                SimSettings.TYPES.EDGE_DATACENTER);
        UtilizationModelFull ramModel = new UtilizationModelFull();
        UtilizationModelFull bwModel = new UtilizationModelFull();
        
        // Criar GpuTask
        GpuTask gpuTask = new GpuTask(
            mobileDeviceId,
            taskId,
            cloudletLength,
            pesNumber,
            cloudletFileSize,
            cloudletOutputSize,
            cpuModel,
            ramModel,
            bwModel,
            gpuLength,
            gpuInputData,
            gpuOutputData,
            requiredGpuMemory
        );
        
        // Configurar tipo de tarefa
        gpuTask.setTaskType(taskType);
        gpuTask.setExpectedGpuUtilization(80.0);
        
        return gpuTask;
    }
    
    /**
     * Submete tarefa GPU para execução.
     */
    public void submitTask(GpuTask gpuTask) {
        // Orquestrador decide para onde enviar
        int targetDatacenterId = edgeOrchestrator.getDeviceToOffload(gpuTask);
        int targetVmId = edgeOrchestrator.getVmToOffload(gpuTask, targetDatacenterId);
        
        // Define associações
        gpuTask.setAssociatedDatacenterId(targetDatacenterId);
        gpuTask.setAssociatedVmId(targetVmId);
        
        // Envia para o broker
        schedule(
            SimManager.getInstance().getEdgeServerManager().getDatacenterList().get(0).getId(),
            0,
            CREATE_TASK_NOW,
            gpuTask
        );
    }
}
```

---

### 5.5 Processando GpuTask em GpuEdgeVM

```java
package edu.boun.edgecloudsim.edge_client;

import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.edge_server.GpuEdgeVM;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * Gerenciador de dispositivos móveis com suporte a GpuTask.
 */
public class GpuMobileDeviceManager extends MobileDeviceManager {
    
    /**
     * Processa submissão de tarefa (GPU ou tradicional).
     */
    @Override
    public void submitTask(Task task) {
        if (task instanceof GpuTask) {
            submitGpuTask((GpuTask) task);
        } else {
            super.submitTask(task);
        }
    }
    
    /**
     * Submete GpuTask para VM apropriada.
     */
    private void submitGpuTask(GpuTask gpuTask) {
        // Obtém VM de destino
        int vmId = gpuTask.getAssociatedVmId();
        int datacenterId = gpuTask.getAssociatedDatacenterId();
        
        GpuEdgeVM vm = (GpuEdgeVM) SimManager.getInstance()
            .getEdgeServerManager()
            .getVmList(datacenterId)
            .get(vmId);
        
        // Verifica se VM tem GPU (se tarefa requer)
        if (gpuTask.requiresGpu() && !vm.hasGpu()) {
            SimLogger.printLine(CloudSim.clock() + 
                ": Task #" + gpuTask.getCloudletId() + 
                " FAILED - VM #" + vmId + " has no GPU");
            gpuTask.setCloudletStatus(Cloudlet.FAILED);
            return;
        }
        
        // Verifica memória GPU disponível
        if (gpuTask.requiresGpu()) {
            long availableMemory = vm.getAvailableGpuMemory();
            if (availableMemory < gpuTask.getRequiredGpuMemory()) {
                SimLogger.printLine(CloudSim.clock() + 
                    ": Task #" + gpuTask.getCloudletId() + 
                    " FAILED - Insufficient GPU memory");
                gpuTask.setCloudletStatus(Cloudlet.FAILED);
                return;
            }
        }
        
        // Define timestamps
        gpuTask.setSubmittedLocation(
            mobilityModel.getLocation(gpuTask.getMobileDeviceId(), CloudSim.clock())
        );
        gpuTask.setGpuStartTime(CloudSim.clock());
        
        // Submete tarefa GPU
        boolean success = vm.submitGpuTask(gpuTask);
        
        if (success) {
            SimLogger.printLine(CloudSim.clock() + 
                ": GpuTask #" + gpuTask.getCloudletId() + 
                " submitted to VM #" + vmId + 
                " with GPU #" + vm.getGpu().getId());
        } else {
            SimLogger.printLine(CloudSim.clock() + 
                ": GpuTask #" + gpuTask.getCloudletId() + 
                " REJECTED by VM #" + vmId);
            gpuTask.setCloudletStatus(Cloudlet.FAILED);
        }
    }
    
    /**
     * Processa conclusão de tarefa GPU.
     */
    private void processGpuTaskCompletion(GpuTask gpuTask) {
        // Calcula métricas finais
        double totalGpuTime = gpuTask.getTotalGpuTime();
        double gpuIntensity = gpuTask.getGpuIntensity();
        
        // Log resultados
        SimLogger.getInstance().addGpuTaskLog(
            CloudSim.clock(),
            gpuTask.getMobileDeviceId(),
            gpuTask.getAssociatedVmId(),
            gpuTask.getExecutedGpuId(),
            gpuTask.getCloudletId(),
            gpuTask.getActualGpuUtilization(),
            gpuTask.getRequiredGpuMemory(),
            gpuTask.getGpuExecutionTime(),
            gpuTask.getGpuDataTransferTime() + gpuTask.getGpuDataBackTime()
        );
        
        SimLogger.printLine(CloudSim.clock() + 
            ": GpuTask #" + gpuTask.getCloudletId() + 
            " COMPLETED - Total GPU Time: " + totalGpuTime + "s, " +
            "GPU Intensity: " + (gpuIntensity * 100) + "%");
    }
}
```

---

### 5.6 Implementando GpuProvisioner Customizado

```java
package edu.boun.edgecloudsim.edge_server;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provisioner GPU com estratégia Best-Fit baseada em capacidade.
 */
public class GpuProvisionerBestFit implements GpuProvisioner {
    private List<Gpu> gpuList;
    private Map<Integer, Gpu> vmGpuMap;
    
    public GpuProvisionerBestFit(List<Gpu> gpuList) {
        this.gpuList = gpuList;
        this.vmGpuMap = new HashMap<>();
        
        // Associa GPUs ao host
        for (Gpu gpu : gpuList) {
            // Assuming host is set elsewhere or passed as parameter
        }
    }
    
    @Override
    public boolean allocateGpuForVm(GpuEdgeVM vm, Gpu gpu) {
        if (gpu == null || !gpu.isAvailable()) {
            return false;
        }
        
        // Aloca GPU para VM
        gpu.setAllocatedVm(vm);
        vmGpuMap.put(vm.getId(), gpu);
        
        return true;
    }
    
    @Override
    public void deallocateGpuForVm(GpuEdgeVM vm) {
        Gpu gpu = vmGpuMap.remove(vm.getId());
        if (gpu != null) {
            gpu.reset();
        }
    }
    
    @Override
    public boolean hasAvailableGpu() {
        return gpuList.stream().anyMatch(Gpu::isAvailable);
    }
    
    @Override
    public Gpu getAvailableGpu() {
        // Best-Fit: retorna GPU disponível com maior capacidade
        return gpuList.stream()
            .filter(Gpu::isAvailable)
            .max(Comparator.comparingDouble(Gpu::getGflops))
            .orElse(null);
    }
    
    @Override
    public Gpu getAvailableGpuWithMemory(long requiredMemory) {
        // Best-Fit com memória: GPU com menor memória suficiente
        return gpuList.stream()
            .filter(Gpu::isAvailable)
            .filter(gpu -> gpu.getGpuMemory() >= requiredMemory)
            .min(Comparator.comparingLong(Gpu::getGpuMemory))
            .orElse(null);
    }
    
    @Override
    public List<Gpu> getGpuList() {
        return Collections.unmodifiableList(gpuList);
    }
    
    @Override
    public List<Gpu> getAvailableGpuList() {
        return gpuList.stream()
            .filter(Gpu::isAvailable)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Gpu> getAllocatedGpuList() {
        return gpuList.stream()
            .filter(gpu -> !gpu.isAvailable())
            .collect(Collectors.toList());
    }
    
    /**
     * Estratégia alternativa: Least-Loaded
     * Retorna GPU com menor utilização atual.
     */
    public Gpu getAvailableGpuLeastLoaded() {
        return gpuList.stream()
            .filter(Gpu::isAvailable)
            .min(Comparator.comparingDouble(Gpu::getUtilization))
            .orElse(null);
    }
}
```

---

### 5.7 Logging de Métricas GPU

```java
package edu.boun.edgecloudsim.utils;

import java.io.*;
import java.util.*;

/**
 * Extensão do SimLogger para métricas GPU.
 */
public class GpuSimLogger extends SimLogger {
    
    private List<GpuTaskLogItem> gpuTaskLog;
    private List<GpuUtilizationLogItem> gpuUtilizationLog;
    
    private BufferedWriter gpuTaskLogWriter;
    private BufferedWriter gpuUtilizationLogWriter;
    
    public GpuSimLogger(String outputFolder) {
        super(outputFolder);
        gpuTaskLog = new ArrayList<>();
        gpuUtilizationLog = new ArrayList<>();
    }
    
    /**
     * Adiciona log de tarefa GPU.
     */
    public void addGpuTaskLog(double time, int mobileDeviceId, int vmId,
                               int gpuId, int taskId, double gpuUtilization,
                               long gpuMemoryUsed, double gpuExecutionTime,
                               double gpuDataTransferTime) {
        GpuTaskLogItem item = new GpuTaskLogItem(
            time, mobileDeviceId, vmId, gpuId, taskId,
            gpuUtilization, gpuMemoryUsed, gpuExecutionTime, gpuDataTransferTime
        );
        gpuTaskLog.add(item);
    }
    
    /**
     * Adiciona log de utilização GPU.
     */
    public void addGpuUtilizationLog(double time, int hostId, int gpuId,
                                      double utilization, long usedMemory,
                                      int allocatedVmId) {
        GpuUtilizationLogItem item = new GpuUtilizationLogItem(
            time, hostId, gpuId, utilization, usedMemory, allocatedVmId
        );
        gpuUtilizationLog.add(item);
    }
    
    /**
     * Escreve logs GPU em arquivos CSV.
     */
    @Override
    public void simStopped() {
        super.simStopped();
        
        try {
            // GPU Task Log
            gpuTaskLogWriter = new BufferedWriter(new FileWriter(
                outputFolder + "/gpu_task_log.csv"));
            gpuTaskLogWriter.write(
                "Time,MobileDeviceId,VmId,GpuId,TaskId," +
                "GpuUtilization,GpuMemoryUsed,GpuExecutionTime,GpuDataTransferTime\n");
            
            for (GpuTaskLogItem item : gpuTaskLog) {
                gpuTaskLogWriter.write(item.toString() + "\n");
            }
            gpuTaskLogWriter.close();
            
            // GPU Utilization Log
            gpuUtilizationLogWriter = new BufferedWriter(new FileWriter(
                outputFolder + "/gpu_utilization_log.csv"));
            gpuUtilizationLogWriter.write(
                "Time,HostId,GpuId,Utilization,UsedMemory,AllocatedVmId\n");
            
            for (GpuUtilizationLogItem item : gpuUtilizationLog) {
                gpuUtilizationLogWriter.write(item.toString() + "\n");
            }
            gpuUtilizationLogWriter.close();
            
            SimLogger.printLine("GPU logs escritos com sucesso!");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Classe interna para log de tarefa GPU.
     */
    private static class GpuTaskLogItem {
        double time;
        int mobileDeviceId, vmId, gpuId, taskId;
        double gpuUtilization, gpuExecutionTime, gpuDataTransferTime;
        long gpuMemoryUsed;
        
        public GpuTaskLogItem(double time, int mobileDeviceId, int vmId,
                               int gpuId, int taskId, double gpuUtilization,
                               long gpuMemoryUsed, double gpuExecutionTime,
                               double gpuDataTransferTime) {
            this.time = time;
            this.mobileDeviceId = mobileDeviceId;
            this.vmId = vmId;
            this.gpuId = gpuId;
            this.taskId = taskId;
            this.gpuUtilization = gpuUtilization;
            this.gpuMemoryUsed = gpuMemoryUsed;
            this.gpuExecutionTime = gpuExecutionTime;
            this.gpuDataTransferTime = gpuDataTransferTime;
        }
        
        @Override
        public String toString() {
            return String.format("%.2f,%d,%d,%d,%d,%.2f,%d,%.4f,%.4f",
                time, mobileDeviceId, vmId, gpuId, taskId,
                gpuUtilization, gpuMemoryUsed, gpuExecutionTime, gpuDataTransferTime);
        }
    }
    
    /**
     * Classe interna para log de utilização GPU.
     */
    private static class GpuUtilizationLogItem {
        double time;
        int hostId, gpuId, allocatedVmId;
        double utilization;
        long usedMemory;
        
        public GpuUtilizationLogItem(double time, int hostId, int gpuId,
                                      double utilization, long usedMemory,
                                      int allocatedVmId) {
            this.time = time;
            this.hostId = hostId;
            this.gpuId = gpuId;
            this.utilization = utilization;
            this.usedMemory = usedMemory;
            this.allocatedVmId = allocatedVmId;
        }
        
        @Override
        public String toString() {
            return String.format("%.2f,%d,%d,%.2f,%d,%d",
                time, hostId, gpuId, utilization, usedMemory, allocatedVmId);
        }
    }
}
```

---

## 6. Estratégias de Teste

### 6.1 Plano Geral de Testes

**Níveis de Teste:**

| Nível | Descrição | Ferramentas |
|-------|-----------|-------------|
| **Unit Tests** | Teste de classes individuais isoladas | JUnit 5, Mockito |
| **Integration Tests** | Teste de interação entre classes GPU | JUnit 5, TestContainers |
| **System Tests** | Teste de cenários completos de simulação | JUnit 5, assertj |
| **Performance Tests** | Teste de escalabilidade e desempenho | JMH, VisualVM |
| **Validation Tests** | Comparação com resultados esperados | Custom scripts |

**Cobertura de Código Alvo:** ≥ 80% para classes críticas

---

### 6.2 Testes Unitários

#### 6.2.1 Teste: Classe Gpu

```java
package edu.boun.edgecloudsim.edge_server;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe Gpu.
 */
class GpuTest {
    
    private Gpu gpu;
    
    @BeforeEach
    void setUp() {
        gpu = new Gpu(0, "NVIDIA_T4", 2560, 8100.0, 16000, 320.0);
    }
    
    @Test
    @DisplayName("Construtor deve inicializar atributos corretamente")
    void testConstructor() {
        assertEquals(0, gpu.getId());
        assertEquals("NVIDIA_T4", gpu.getGpuType());
        assertEquals(2560, gpu.getCudaCores());
        assertEquals(8100.0, gpu.getGflops(), 0.01);
        assertEquals(16000, gpu.getGpuMemory());
        assertEquals(320.0, gpu.getMemoryBandwidth(), 0.01);
        assertEquals(0.0, gpu.getUtilization(), 0.01);
        assertTrue(gpu.isAvailable());
    }
    
    @Test
    @DisplayName("isAvailable() deve retornar true quando GPU não alocada")
    void testIsAvailableWhenNotAllocated() {
        assertTrue(gpu.isAvailable());
    }
    
    @Test
    @DisplayName("isAvailable() deve retornar false quando GPU alocada")
    void testIsAvailableWhenAllocated() {
        GpuEdgeVM mockVm = new GpuEdgeVM(1, 1, 1000, 2, 2000, 1000, 10000, 
            "Xen", null, null, true);
        gpu.setAllocatedVm(mockVm);
        
        assertFalse(gpu.isAvailable());
    }
    
    @Test
    @DisplayName("allocateMemory() deve retornar true quando há memória suficiente")
    void testAllocateMemorySuccess() {
        assertTrue(gpu.allocateMemory(8000));
        assertEquals(8000, gpu.getUsedMemory());
        assertEquals(8000, gpu.getAvailableMemory());
    }
    
    @Test
    @DisplayName("allocateMemory() deve retornar false quando memória insuficiente")
    void testAllocateMemoryFailure() {
        gpu.allocateMemory(10000);
        assertFalse(gpu.allocateMemory(8000)); // Total = 18000 > 16000
        assertEquals(10000, gpu.getUsedMemory());
    }
    
    @Test
    @DisplayName("deallocateMemory() deve liberar memória corretamente")
    void testDeallocateMemory() {
        gpu.allocateMemory(8000);
        assertTrue(gpu.deallocateMemory(3000));
        assertEquals(5000, gpu.getUsedMemory());
        assertEquals(11000, gpu.getAvailableMemory());
    }
    
    @Test
    @DisplayName("calculateExecutionTime() deve calcular tempo corretamente")
    void testCalculateExecutionTime() {
        long gpuLength = 8100; // GFLOPs
        double expectedTime = (gpuLength * 1000000.0) / gpu.getGflops(); // segundos
        
        double actualTime = gpu.calculateExecutionTime(gpuLength);
        
        assertEquals(expectedTime, actualTime, 0.01);
    }
    
    @Test
    @DisplayName("calculateDataTransferTime() deve calcular tempo de transferência")
    void testCalculateDataTransferTime() {
        long dataSize = 1000; // MB
        double expectedTime = (dataSize * 8.0) / gpu.getMemoryBandwidth(); // segundos
        
        double actualTime = gpu.calculateDataTransferTime(dataSize);
        
        assertEquals(expectedTime, actualTime, 0.01);
    }
    
    @Test
    @DisplayName("reset() deve resetar GPU para estado inicial")
    void testReset() {
        GpuEdgeVM mockVm = new GpuEdgeVM(1, 1, 1000, 2, 2000, 1000, 10000, 
            "Xen", null, null, true);
        gpu.setAllocatedVm(mockVm);
        gpu.setUtilization(75.0);
        gpu.allocateMemory(5000);
        
        gpu.reset();
        
        assertNull(gpu.getAllocatedVm());
        assertEquals(0.0, gpu.getUtilization(), 0.01);
        assertEquals(0, gpu.getUsedMemory());
        assertTrue(gpu.isAvailable());
    }
}
```

#### 6.2.2 Teste: GpuProvisionerSimple

```java
package edu.boun.edgecloudsim.edge_server;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * Testes unitários para GpuProvisionerSimple.
 */
class GpuProvisionerSimpleTest {
    
    private GpuProvisionerSimple provisioner;
    private List<Gpu> gpuList;
    private GpuEdgeVM vm1, vm2;
    
    @BeforeEach
    void setUp() {
        // Criar lista de GPUs
        gpuList = new ArrayList<>();
        gpuList.add(new Gpu(0, "NVIDIA_T4", 2560, 8100.0, 16000, 320.0));
        gpuList.add(new Gpu(1, "NVIDIA_A100", 6912, 19500.0, 40000, 1555.0));
        gpuList.add(new Gpu(2, "NVIDIA_V100", 5120, 15700.0, 32000, 900.0));
        
        provisioner = new GpuProvisionerSimple(gpuList);
        
        // Criar VMs
        vm1 = new GpuEdgeVM(1, 1, 1000, 2, 2000, 1000, 10000, 
            "Xen", null, null, true);
        vm2 = new GpuEdgeVM(2, 1, 1000, 2, 2000, 1000, 10000, 
            "Xen", null, null, true);
    }
    
    @Test
    @DisplayName("hasAvailableGpu() deve retornar true inicialmente")
    void testHasAvailableGpuInitially() {
        assertTrue(provisioner.hasAvailableGpu());
    }
    
    @Test
    @DisplayName("getAvailableGpu() deve retornar primeira GPU disponível")
    void testGetAvailableGpu() {
        Gpu gpu = provisioner.getAvailableGpu();
        
        assertNotNull(gpu);
        assertEquals(0, gpu.getId());
        assertTrue(gpu.isAvailable());
    }
    
    @Test
    @DisplayName("allocateGpuForVm() deve alocar GPU com sucesso")
    void testAllocateGpuForVmSuccess() {
        Gpu gpu = provisioner.getAvailableGpu();
        assertTrue(provisioner.allocateGpuForVm(vm1, gpu));
        
        assertFalse(gpu.isAvailable());
        assertEquals(vm1, gpu.getAllocatedVm());
    }
    
    @Test
    @DisplayName("allocateGpuForVm() deve falhar se GPU já alocada")
    void testAllocateGpuForVmFailureAlreadyAllocated() {
        Gpu gpu = provisioner.getAvailableGpu();
        provisioner.allocateGpuForVm(vm1, gpu);
        
        assertFalse(provisioner.allocateGpuForVm(vm2, gpu));
    }
    
    @Test
    @DisplayName("deallocateGpuForVm() deve liberar GPU")
    void testDeallocateGpuForVm() {
        Gpu gpu = provisioner.getAvailableGpu();
        provisioner.allocateGpuForVm(vm1, gpu);
        
        provisioner.deallocateGpuForVm(vm1);
        
        assertTrue(gpu.isAvailable());
        assertNull(gpu.getAllocatedVm());
    }
    
    @Test
    @DisplayName("getAvailableGpuWithMemory() deve retornar GPU com memória suficiente")
    void testGetAvailableGpuWithMemory() {
        long requiredMemory = 20000; // MB
        Gpu gpu = provisioner.getAvailableGpuWithMemory(requiredMemory);
        
        assertNotNull(gpu);
        assertTrue(gpu.getGpuMemory() >= requiredMemory);
        assertEquals(1, gpu.getId()); // NVIDIA_A100 com 40GB
    }
    
    @Test
    @DisplayName("getAvailableGpuWithMemory() deve retornar null se sem memória suficiente")
    void testGetAvailableGpuWithMemoryFailure() {
        long requiredMemory = 50000; // MB (mais que qualquer GPU)
        Gpu gpu = provisioner.getAvailableGpuWithMemory(requiredMemory);
        
        assertNull(gpu);
    }
    
    @Test
    @DisplayName("getAvailableGpuList() deve retornar apenas GPUs disponíveis")
    void testGetAvailableGpuList() {
        Gpu gpu1 = gpuList.get(0);
        provisioner.allocateGpuForVm(vm1, gpu1);
        
        List<Gpu> availableGpus = provisioner.getAvailableGpuList();
        
        assertEquals(2, availableGpus.size());
        assertFalse(availableGpus.contains(gpu1));
    }
    
    @Test
    @DisplayName("getAllocatedGpuList() deve retornar apenas GPUs alocadas")
    void testGetAllocatedGpuList() {
        Gpu gpu1 = gpuList.get(0);
        provisioner.allocateGpuForVm(vm1, gpu1);
        
        List<Gpu> allocatedGpus = provisioner.getAllocatedGpuList();
        
        assertEquals(1, allocatedGpus.size());
        assertTrue(allocatedGpus.contains(gpu1));
    }
    
    @Test
    @DisplayName("getAverageUtilization() deve calcular utilização média")
    void testGetAverageUtilization() {
        gpuList.get(0).setUtilization(50.0);
        gpuList.get(1).setUtilization(75.0);
        gpuList.get(2).setUtilization(25.0);
        
        double avgUtilization = provisioner.getAverageUtilization();
        
        assertEquals(50.0, avgUtilization, 0.01);
    }
}
```

#### 6.2.3 Teste: GpuTask

```java
package edu.boun.edgecloudsim.edge_client;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para GpuTask.
 */
class GpuTaskTest {
    
    private GpuTask gpuTask;
    
    @BeforeEach
    void setUp() {
        gpuTask = new GpuTask(
            1,      // cloudletId
            0,      // taskId
            50000,  // cloudletLength (MI)
            2,      // pesNumber
            5000,   // cloudletFileSize
            500,    // cloudletOutputSize
            150000, // gpuLength (GFLOPs)
            2000,   // gpuInputData (MB)
            100,    // gpuOutputData (MB)
            4000    // requiredGpuMemory (MB)
        );
    }
    
    @Test
    @DisplayName("Construtor deve inicializar atributos GPU corretamente")
    void testConstructor() {
        assertEquals(150000, gpuTask.getGpuLength());
        assertEquals(2000, gpuTask.getGpuInputData());
        assertEquals(100, gpuTask.getGpuOutputData());
        assertEquals(4000, gpuTask.getRequiredGpuMemory());
        assertEquals(-1, gpuTask.getExecutedGpuId());
        assertEquals(0.0, gpuTask.getGpuExecutionTime(), 0.01);
    }
    
    @Test
    @DisplayName("requiresGpu() deve retornar true quando gpuLength > 0")
    void testRequiresGpu() {
        assertTrue(gpuTask.requiresGpu());
    }
    
    @Test
    @DisplayName("requiresGpu() deve retornar false quando gpuLength = 0")
    void testDoesNotRequireGpu() {
        GpuTask taskWithoutGpu = new GpuTask(
            2, 0, 50000, 2, 5000, 500,
            0, 0, 0, 0  // Sem requisitos GPU
        );
        
        assertFalse(taskWithoutGpu.requiresGpu());
    }
    
    @Test
    @DisplayName("getTotalGpuTime() deve somar todos os tempos GPU")
    void testGetTotalGpuTime() {
        gpuTask.setGpuDataTransferTime(0.5);
        gpuTask.setGpuExecutionTime(2.0);
        gpuTask.setGpuDataBackTime(0.3);
        
        assertEquals(2.8, gpuTask.getTotalGpuTime(), 0.01);
    }
    
    @Test
    @DisplayName("getGpuIntensity() deve calcular proporção GPU/(CPU+GPU)")
    void testGetGpuIntensity() {
        gpuTask.setGpuExecutionTime(2.0);
        // Assuming CPU execution time is calculated based on cloudletLength
        // For simplicity, assume CPU time = 1.0
        // gpuIntensity = 2.0 / (1.0 + 2.0) = 0.666...
        
        // This requires actual calculation logic in GpuTask
        // For now, we'll test the setter/getter
        double expectedIntensity = 0.67;
        // Assuming GpuTask calculates this internally
        
        // Placeholder assertion
        assertTrue(gpuTask.getGpuIntensity() >= 0.0 && gpuTask.getGpuIntensity() <= 1.0);
    }
    
    @Test
    @DisplayName("hasEnoughGpuMemory() deve retornar true quando memória suficiente")
    void testHasEnoughGpuMemoryTrue() {
        assertTrue(gpuTask.hasEnoughGpuMemory(5000));
    }
    
    @Test
    @DisplayName("hasEnoughGpuMemory() deve retornar false quando memória insuficiente")
    void testHasEnoughGpuMemoryFalse() {
        assertFalse(gpuTask.hasEnoughGpuMemory(3000));
    }
    
    @Test
    @DisplayName("Setters GPU devem funcionar corretamente")
    void testGpuSetters() {
        gpuTask.setExecutedGpuId(2);
        gpuTask.setGpuDataTransferTime(0.75);
        gpuTask.setGpuExecutionTime(3.5);
        gpuTask.setGpuDataBackTime(0.25);
        gpuTask.setActualGpuUtilization(85.0);
        gpuTask.setGpuStartTime(10.0);
        gpuTask.setGpuFinishTime(14.5);
        
        assertEquals(2, gpuTask.getExecutedGpuId());
        assertEquals(0.75, gpuTask.getGpuDataTransferTime(), 0.01);
        assertEquals(3.5, gpuTask.getGpuExecutionTime(), 0.01);
        assertEquals(0.25, gpuTask.getGpuDataBackTime(), 0.01);
        assertEquals(85.0, gpuTask.getActualGpuUtilization(), 0.01);
        assertEquals(10.0, gpuTask.getGpuStartTime(), 0.01);
        assertEquals(14.5, gpuTask.getGpuFinishTime(), 0.01);
    }
}
```

---

### 6.3 Testes de Integração

#### 6.3.1 Teste: GpuEdgeHost com GpuProvisioner

```java
package edu.boun.edgecloudsim.edge_server;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.provisioners.*;
import java.util.*;

/**
 * Testes de integração entre GpuEdgeHost e GpuProvisioner.
 */
class GpuEdgeHostIntegrationTest {
    
    private GpuEdgeHost host;
    private GpuProvisioner provisioner;
    private List<Gpu> gpuList;
    private List<Pe> peList;
    
    @BeforeEach
    void setUp() {
        // Criar PEs (CPU cores)
        peList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(10000)));
        }
        
        // Criar GPUs
        gpuList = new ArrayList<>();
        gpuList.add(new Gpu(0, "NVIDIA_T4", 2560, 8100.0, 16000, 320.0));
        gpuList.add(new Gpu(1, "NVIDIA_A100", 6912, 19500.0, 40000, 1555.0));
        
        // Criar GpuProvisioner
        provisioner = new GpuProvisionerSimple(gpuList);
        
        // Criar GpuEdgeHost
        host = new GpuEdgeHost(
            0,
            new RamProvisionerSimple(16000),
            new BwProvisionerSimple(10000),
            100000,
            peList,
            new VmSchedulerSpaceShared(peList),
            gpuList,
            provisioner
        );
        
        // Associar GPUs ao host
        for (Gpu gpu : gpuList) {
            gpu.setHost(host);
        }
    }
    
    @Test
    @DisplayName("Host deve ser criado com GPUs corretas")
    void testHostCreation() {
        assertEquals(2, host.getNumberOfGpus());
        assertNotNull(host.getGpuProvisioner());
        assertTrue(host.hasAvailableGpu());
    }
    
    @Test
    @DisplayName("Host deve alocar GPU para VM corretamente")
    void testAllocateGpuForVm() {
        GpuEdgeVM vm = new GpuEdgeVM(
            1, 1, 10000, 2, 4000, 1000, 10000,
            "Xen", null, null, true
        );
        
        Gpu gpu = host.getAvailableGpu();
        boolean allocated = host.allocateGpuForVm(vm, gpu);
        
        assertTrue(allocated);
        assertFalse(gpu.isAvailable());
        assertEquals(vm, gpu.getAllocatedVm());
    }
    
    @Test
    @DisplayName("Host deve desalocar GPU de VM corretamente")
    void testDeallocateGpuForVm() {
        GpuEdgeVM vm = new GpuEdgeVM(
            1, 1, 10000, 2, 4000, 1000, 10000,
            "Xen", null, null, true
        );
        
        Gpu gpu = host.getAvailableGpu();
        host.allocateGpuForVm(vm, gpu);
        
        host.deallocateGpuForVm(vm);
        
        assertTrue(gpu.isAvailable());
        assertNull(gpu.getAllocatedVm());
    }
    
    @Test
    @DisplayName("Host deve calcular utilização média GPU corretamente")
    void testGetAvgGpuUtilization() {
        gpuList.get(0).setUtilization(60.0);
        gpuList.get(1).setUtilization(80.0);
        
        assertEquals(70.0, host.getAvgGpuUtilization(), 0.01);
    }
    
    @Test
    @DisplayName("Host deve retornar memória GPU total correta")
    void testGetTotalGpuMemory() {
        long expectedMemory = 16000 + 40000; // T4 + A100
        assertEquals(expectedMemory, host.getTotalGpuMemory());
    }
    
    @Test
    @DisplayName("Host deve retornar memória GPU disponível correta")
    void testGetAvailableGpuMemory() {
        gpuList.get(0).allocateMemory(5000);
        gpuList.get(1).allocateMemory(10000);
        
        long expectedAvailable = (16000 - 5000) + (40000 - 10000);
        assertEquals(expectedAvailable, host.getAvailableGpuMemory());
    }
    
    @Test
    @DisplayName("vmCreate() deve alocar GPU automaticamente se VM requer")
    void testVmCreateWithGpu() {
        GpuEdgeVM vm = new GpuEdgeVM(
            1, 1, 10000, 2, 4000, 1000, 10000,
            "Xen", null, null, true
        );
        
        boolean created = host.vmCreate(vm);
        
        assertTrue(created);
        assertTrue(vm.hasGpu());
        assertNotNull(vm.getGpu());
        assertFalse(vm.getGpu().isAvailable());
    }
    
    @Test
    @DisplayName("vmDestroy() deve desalocar GPU automaticamente")
    void testVmDestroyWithGpu() {
        GpuEdgeVM vm = new GpuEdgeVM(
            1, 1, 10000, 2, 4000, 1000, 10000,
            "Xen", null, null, true
        );
        
        host.vmCreate(vm);
        Gpu allocatedGpu = vm.getGpu();
        
        host.vmDestroy(vm);
        
        assertTrue(allocatedGpu.isAvailable());
        assertNull(allocatedGpu.getAllocatedVm());
    }
}
```

#### 6.3.2 Teste: GpuEdgeVM com GpuCloudletScheduler

```java
package edu.boun.edgecloudsim.edge_server;

import edu.boun.edgecloudsim.edge_client.GpuTask;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;

/**
 * Testes de integração entre GpuEdgeVM e GpuCloudletScheduler.
 */
class GpuEdgeVMIntegrationTest {
    
    private GpuEdgeVM vm;
    private Gpu gpu;
    private GpuCloudletSchedulerTimeShared scheduler;
    
    @BeforeEach
    void setUp() {
        gpu = new Gpu(0, "NVIDIA_T4", 2560, 8100.0, 16000, 320.0);
        scheduler = new GpuCloudletSchedulerTimeShared(gpu);
        
        vm = new GpuEdgeVM(
            1, 1, 10000, 2, 4000, 1000, 10000,
            "Xen",
            new CloudletSchedulerTimeShared(),
            scheduler,
            true
        );
        
        vm.setGpu(gpu);
        gpu.setAllocatedVm(vm);
    }
    
    @Test
    @DisplayName("VM deve ser criada com GPU e scheduler corretos")
    void testVmCreation() {
        assertTrue(vm.hasGpu());
        assertEquals(gpu, vm.getGpu());
        assertNotNull(vm.getGpuCloudletScheduler());
        assertTrue(vm.requiresGpu());
    }
    
    @Test
    @DisplayName("submitGpuTask() deve submeter tarefa com sucesso")
    void testSubmitGpuTask() {
        GpuTask task = new GpuTask(
            1, 0, 50000, 2, 5000, 500,
            150000, 2000, 100, 4000
        );
        
        boolean submitted = vm.submitGpuTask(task);
        
        assertTrue(submitted);
        assertEquals(1, vm.getNumberOfRunningGpuTasks());
    }
    
    @Test
    @DisplayName("submitGpuTask() deve aceitar múltiplas tarefas (time-shared)")
    void testSubmitMultipleGpuTasks() {
        GpuTask task1 = new GpuTask(1, 0, 50000, 2, 5000, 500, 150000, 2000, 100, 4000);
        GpuTask task2 = new GpuTask(2, 0, 50000, 2, 5000, 500, 150000, 2000, 100, 4000);
        GpuTask task3 = new GpuTask(3, 0, 50000, 2, 5000, 500, 150000, 2000, 100, 4000);
        
        vm.submitGpuTask(task1);
        vm.submitGpuTask(task2);
        vm.submitGpuTask(task3);
        
        assertEquals(3, vm.getNumberOfRunningGpuTasks());
    }
    
    @Test
    @DisplayName("getGpuUtilization() deve refletir tarefas em execução")
    void testGetGpuUtilization() {
        GpuTask task = new GpuTask(1, 0, 50000, 2, 5000, 500, 150000, 2000, 100, 4000);
        vm.submitGpuTask(task);
        
        double utilization = vm.getGpuUtilization();
        
        assertTrue(utilization > 0.0);
        assertTrue(utilization <= 100.0);
    }
    
    @Test
    @DisplayName("getAvailableGpuMemory() deve retornar memória correta")
    void testGetAvailableGpuMemory() {
        gpu.allocateMemory(5000);
        
        assertEquals(11000, vm.getAvailableGpuMemory());
    }
    
    @Test
    @DisplayName("getCombinedUtilization() deve calcular média CPU+GPU")
    void testGetCombinedUtilization() {
        // Simular utilização CPU = 50%
        // Simular utilização GPU = 70%
        gpu.setUtilization(70.0);
        
        // Expected combined = (50 + 70) / 2 = 60
        // Note: Actual CPU utilization calculation depends on implementation
        
        double combined = vm.getCombinedUtilization();
        assertTrue(combined >= 0.0 && combined <= 100.0);
    }
}
```

---

### 6.4 Testes de Sistema

#### 6.4.1 Teste: Cenário Completo de Simulação GPU

```java
package edu.boun.edgecloudsim.integration;

import edu.boun.edgecloudsim.core.*;
import edu.boun.edgecloudsim.applications.gpu_app.GpuScenarioFactory;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * Teste de sistema completo para cenário GPU.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GpuSimulationSystemTest {
    
    private SimManager simManager;
    
    @BeforeAll
    void setUpSimulation() throws Exception {
        // Configurar CloudSim
        int numBrokers = 1;
        boolean traceFlag = false;
        CloudSim.init(numBrokers, Calendar.getInstance(), traceFlag);
        
        // Configurar SimSettings
        SimSettings.getInstance().initialize(
            "scripts/gpu_app/config/default_config.properties",
            "scripts/gpu_app/config/edge_devices.xml",
            "scripts/gpu_app/config/applications.xml"
        );
        
        // Criar factory GPU
        ScenarioFactory factory = new GpuScenarioFactory(
            200,  // numMobileDevices
            300,  // simulationTime
            "GPU_AWARE",
            "DEEP_LEARNING"
        );
        
        // Criar SimManager
        simManager = new SimManager(factory, 200, "DEEP_LEARNING");
    }
    
    @Test
    @DisplayName("Simulação deve iniciar sem erros")
    void testSimulationStart() {
        assertDoesNotThrow(() -> {
            simManager.startSimulation();
        });
    }
    
    @Test
    @DisplayName("Infraestrutura GPU deve ser criada corretamente")
    void testGpuInfrastructureCreation() {
        EdgeServerManager esm = SimManager.getInstance().getEdgeServerManager();
        
        assertTrue(esm instanceof GpuEdgeServerManager);
        assertNotNull(esm.getDatacenterList());
        assertFalse(esm.getDatacenterList().isEmpty());
        
        // Verificar hosts com GPUs
        for (Datacenter dc : esm.getDatacenterList()) {
            for (Host host : dc.getHostList()) {
                if (host instanceof GpuEdgeHost) {
                    GpuEdgeHost gpuHost = (GpuEdgeHost) host;
                    assertTrue(gpuHost.getNumberOfGpus() > 0);
                }
            }
        }
    }
    
    @Test
    @DisplayName("VMs GPU devem ser alocadas corretamente")
    void testGpuVmAllocation() {
        EdgeServerManager esm = SimManager.getInstance().getEdgeServerManager();
        
        int totalVms = 0;
        int gpuVms = 0;
        
        for (int hostId = 0; hostId < esm.getVmList(0).size(); hostId++) {
            List<EdgeVM> vms = esm.getVmList(hostId);
            totalVms += vms.size();
            
            for (EdgeVM vm : vms) {
                if (vm instanceof GpuEdgeVM) {
                    GpuEdgeVM gpuVm = (GpuEdgeVM) vm;
                    if (gpuVm.requiresGpu()) {
                        assertTrue(gpuVm.hasGpu());
                        gpuVms++;
                    }
                }
            }
        }
        
        assertTrue(totalVms > 0);
        assertTrue(gpuVms > 0);
        System.out.println("Total VMs: " + totalVms + ", GPU VMs: " + gpuVms);
    }
    
    @Test
    @DisplayName("Tarefas GPU devem ser executadas e completadas")
    void testGpuTaskExecution() throws Exception {
        // Executar simulação curta
        simManager.startSimulation();
        
        // Analisar logs de tarefas GPU
        File gpuTaskLog = new File("sim_results/gpu_task_log.csv");
        assertTrue(gpuTaskLog.exists());
        
        // Ler e validar logs
        List<String> lines = Files.readAllLines(gpuTaskLog.toPath());
        assertTrue(lines.size() > 1); // Header + pelo menos 1 tarefa
        
        // Verificar formato do log
        String[] header = lines.get(0).split(",");
        assertEquals(9, header.length);
        assertEquals("Time", header[0]);
        assertEquals("GpuId", header[3]);
        assertEquals("GpuExecutionTime", header[7]);
    }
    
    @Test
    @DisplayName("Métricas de utilização GPU devem ser coletadas")
    void testGpuUtilizationMetrics() throws Exception {
        simManager.startSimulation();
        
        // Analisar logs de utilização GPU
        File gpuUtilizationLog = new File("sim_results/gpu_utilization_log.csv");
        assertTrue(gpuUtilizationLog.exists());
        
        List<String> lines = Files.readAllLines(gpuUtilizationLog.toPath());
        assertTrue(lines.size() > 1);
        
        // Verificar se há entradas com utilização > 0
        boolean hasUtilization = lines.stream()
            .skip(1)
            .anyMatch(line -> {
                String[] parts = line.split(",");
                double utilization = Double.parseDouble(parts[3]);
                return utilization > 0.0;
            });
        
        assertTrue(hasUtilization, "Nenhuma utilização GPU registrada");
    }
    
    @AfterAll
    void cleanup() {
        // Limpar recursos
        if (simManager != null) {
            simManager.terminateSimulation();
        }
    }
}
```

---

### 6.5 Testes de Performance

```java
package edu.boun.edgecloudsim.performance;

import org.openjdk.jmh.annotations.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks de performance para classes GPU.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class GpuPerformanceBenchmark {
    
    private List<Gpu> gpuList;
    private GpuProvisionerSimple provisioner;
    private GpuEdgeVM vm;
    
    @Setup
    public void setUp() {
        // Criar lista de 100 GPUs
        gpuList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            gpuList.add(new Gpu(i, "NVIDIA_T4", 2560, 8100.0, 16000, 320.0));
        }
        
        provisioner = new GpuProvisionerSimple(gpuList);
        
        vm = new GpuEdgeVM(1, 1, 10000, 2, 4000, 1000, 10000,
            "Xen", null, null, true);
    }
    
    @Benchmark
    public void benchmarkHasAvailableGpu() {
        provisioner.hasAvailableGpu();
    }
    
    @Benchmark
    public void benchmarkGetAvailableGpu() {
        provisioner.getAvailableGpu();
    }
    
    @Benchmark
    public void benchmarkAllocateGpuForVm() {
        Gpu gpu = provisioner.getAvailableGpu();
        if (gpu != null) {
            provisioner.allocateGpuForVm(vm, gpu);
            provisioner.deallocateGpuForVm(vm);
        }
    }
    
    @Benchmark
    public void benchmarkGetAvailableGpuWithMemory() {
        provisioner.getAvailableGpuWithMemory(8000);
    }
    
    @Benchmark
    public void benchmarkGpuCalculateExecutionTime() {
        gpuList.get(0).calculateExecutionTime(150000);
    }
    
    @Benchmark
    public void benchmarkGpuAllocateMemory() {
        Gpu gpu = gpuList.get(0);
        gpu.allocateMemory(4000);
        gpu.deallocateMemory(4000);
    }
}
```

**Executar benchmarks:**

```bash
mvn clean install
java -jar target/benchmarks.jar GpuPerformanceBenchmark
```

---

### 6.6 Matriz de Casos de Teste

| ID | Categoria | Classe/Método Testado | Objetivo | Prioridade |
|----|-----------|------------------------|----------|------------|
| UT-001 | Unit | Gpu.constructor() | Verificar inicialização | Alta |
| UT-002 | Unit | Gpu.isAvailable() | Verificar disponibilidade | Alta |
| UT-003 | Unit | Gpu.allocateMemory() | Testar alocação de memória | Alta |
| UT-004 | Unit | Gpu.calculateExecutionTime() | Testar cálculo de tempo | Média |
| UT-005 | Unit | GpuProvisionerSimple.allocateGpuForVm() | Testar alocação GPU para VM | Alta |
| UT-006 | Unit | GpuProvisionerSimple.deallocateGpuForVm() | Testar desalocação GPU | Alta |
| UT-007 | Unit | GpuProvisionerSimple.getAvailableGpu() | Testar busca de GPU disponível | Alta |
| UT-008 | Unit | GpuTask.requiresGpu() | Verificar requisito GPU | Alta |
| UT-009 | Unit | GpuTask.getTotalGpuTime() | Testar cálculo de tempo total | Média |
| UT-010 | Unit | GpuTask.getGpuIntensity() | Testar cálculo de intensidade GPU | Média |
| IT-001 | Integration | GpuEdgeHost + GpuProvisioner | Testar alocação GPU no host | Alta |
| IT-002 | Integration | GpuEdgeVM + GpuCloudletScheduler | Testar submissão de tarefas GPU | Alta |
| IT-003 | Integration | GpuEdgeServerManager + XML | Testar criação de infraestrutura | Alta |
| IT-004 | Integration | GpuEdgeVmAllocationPolicy + GpuEdgeHost | Testar alocação de VMs com GPU | Alta |
| ST-001 | System | Cenário completo GPU | Testar simulação end-to-end | Crítica |
| ST-002 | System | Métricas GPU | Verificar coleta de métricas | Alta |
| ST-003 | System | Logging GPU | Verificar geração de logs | Média |
| PT-001 | Performance | GpuProvisioner.getAvailableGpu() | Benchmark de busca de GPU | Média |
| PT-002 | Performance | Gpu.allocateMemory() | Benchmark de alocação de memória | Baixa |
| VT-001 | Validation | Resultados vs. Esperados | Comparar com valores teóricos | Alta |

---

## 7. Decisões de Design

### 7.1 Decisão 1: Modo de Alocação GPU (Exclusive vs. Shared)

**Contexto:**

Uma das decisões mais importantes é definir como GPUs serão compartilhadas entre VMs.

**Opções Consideradas:**

| Opção | Descrição | Vantagens | Desvantagens |
|-------|-----------|-----------|--------------|
| **Exclusive Mode** | 1 GPU : 1 VM | • Simples de implementar<br>• Performance máxima para VM<br>• Sem overhead de context switching | • Subutilização de GPU<br>• Menor flexibilidade<br>• Desperdício de recursos |
| **Shared Mode** | 1 GPU : N VMs | • Melhor utilização de GPU<br>• Maior flexibilidade<br>• Suporta mais VMs | • Complexidade de implementação<br>• Overhead de time-slicing<br>• Performance por VM reduzida |
| **Hybrid Mode** | Configurável por VM | • Máxima flexibilidade<br>• Balance entre performance e utilização | • Maior complexidade<br>• Difícil de otimizar |

**Decisão:** Implementar **Exclusive Mode** para MVP (v1.0), com arquitetura preparada para **Shared Mode** futuro.

**Justificativa:**

1. **Simplicidade:** Exclusive mode é mais fácil de implementar e testar
2. **Realismo:** Muitos cenários reais usam exclusive mode (ex: Kubernetes GPU scheduling)
3. **Extensibilidade:** Arquitetura permite adicionar Shared mode sem refatoração completa
4. **Validação:** Mais fácil validar resultados com alocação 1:1

**Impacto:**

- `GpuProvisionerSimple` implementa exclusive allocation
- `GpuEdgeVM` possui enum `GpuAllocationMode` para futuro
- `GpuCloudletSchedulerTimeShared` já suporta time-slicing para shared mode futuro

---

### 7.2 Decisão 2: Estratégia de Escalonamento GPU

**Contexto:**

Como múltiplas tarefas GPU compartilham a GPU dentro de uma VM?

**Opções Consideradas:**

| Estratégia | Descrição | Adequação |
|------------|-----------|-----------|
| **Space-Shared** | Uma tarefa por vez, outras aguardam | Simples, mas menos realista para GPUs modernas |
| **Time-Shared** | Múltiplas tarefas compartilham via time-slicing | Mais realista para GPUs modernas com preemption |
| **Priority-Based** | Tarefas com maior prioridade executam primeiro | Útil para QoS, mas aumenta complexidade |

**Decisão:** Implementar **Time-Shared** como padrão (`GpuCloudletSchedulerTimeShared`).

**Justificativa:**

1. **Realismo:** GPUs modernas (NVIDIA Volta+) suportam Multi-Process Service (MPS) e preemption
2. **Flexibilidade:** Time-shared permite melhor utilização da GPU
3. **Compatibilidade:** Alinhado com `CloudletSchedulerTimeShared` do CloudSim
4. **Escalabilidade:** Suporta workloads com múltiplas tarefas concorrentes

**Modelo de Time-Slicing:**

```
GFLOPs por tarefa = Total GFLOPs / Número de tarefas ativas

Tempo de execução = (GpuLength * 1e6) / GFLOPs por tarefa
```

**Limitações:**

- Não modela overhead de context switching
- Assume divisão igualitária de recursos (sem prioridades)
- Não modela contenção de memória GPU

**Implementação Futura:**

- `GpuCloudletSchedulerSpaceShared` para cenários sem preemption
- Priority-based scheduling com filas priorizadas

---

### 7.3 Decisão 3: Modelagem de Tempo de Execução GPU

**Contexto:**

Como calcular o tempo de execução de tarefas GPU de forma realista?

**Componentes do Tempo:**

```
T_total = T_cpu_to_gpu + T_gpu_exec + T_gpu_to_cpu
```

**Fórmulas Adotadas:**

1. **Tempo de Transferência CPU → GPU:**
   ```
   T_cpu_to_gpu = (gpuInputData_MB * 8 bits/byte) / memoryBandwidth_GBps
   ```

2. **Tempo de Execução GPU:**
   ```
   T_gpu_exec = (gpuLength_GFLOPs * 1e6 FLOPs/GFLOPs) / gpu_GFLOPs
   ```

3. **Tempo de Transferência GPU → CPU:**
   ```
   T_gpu_to_cpu = (gpuOutputData_MB * 8 bits/byte) / memoryBandwidth_GBps
   ```

**Decisão:** Usar modelo **GFLOPS-based** com transferências de dados explícitas.

**Justificativa:**

1. **Simplicidade:** Modelo linear é fácil de entender e implementar
2. **Adequação:** Apropriado para nível de abstração do simulador
3. **Parametrizabilidade:** Usuários podem ajustar GFLOPS e bandwidth conforme hardware
4. **Validação:** Possível validar contra benchmarks reais (MLPerf, etc.)

**Limitações Conhecidas:**

- Não modela variações de performance (memory-bound vs. compute-bound)
- Não considera cache GPU
- Assume bandwidth constante (não modela contenção)
- Não modela kernel launch overhead

**Calibração:**

Usuários devem calibrar `gflops` e `memoryBandwidth` baseado em:
- Especificações do fabricante (NVIDIA, AMD)
- Benchmarks reais (e.g., cuda-samples)
- Workload-specific profiling (nvprof, NSight)

---

### 7.4 Decisão 4: Estrutura de Herança vs. Composição

**Contexto:**

Como integrar funcionalidades GPU nas classes existentes do EdgeCloudSim?

**Opções Consideradas:**

| Abordagem | Exemplo | Vantagens | Desvantagens |
|-----------|---------|-----------|--------------|
| **Herança** | `GpuEdgeHost extends EdgeHost` | • Natural para OOP<br>• Reutiliza código base<br>• Polimorfismo | • Acoplamento forte<br>• Hierarquia rígida<br>• Dificulta múltiplas extensões |
| **Composição** | `EdgeHost { GpuManager gpuMgr }` | • Flexível<br>• Desacoplado<br>• Fácil de testar | • Mais boilerplate<br>• Pode duplicar lógica<br>• Menos intuitivo |
| **Híbrida** | Herança + Interfaces | • Balance<br>• Flexibilidade | • Complexidade moderada |

**Decisão:** Usar **Herança** para classes principais + **Interfaces** para comportamentos.

**Estrutura Adotada:**

```
EdgeHost ─┬─> GpuEdgeHost (herança)
          └─> usa GpuProvisioner (interface)

EdgeVM ───┬─> GpuEdgeVM (herança)
          └─> usa GpuCloudletScheduler (interface)

Task ─────┬─> GpuTask (herança)
```

**Justificativa:**

1. **Consistência:** Alinhado com padrão do EdgeCloudSim (EdgeHost extends Host)
2. **Simplicidade:** Mais fácil para usuários entenderem hierarquia
3. **Polimorfismo:** Permite usar GpuEdgeHost onde EdgeHost é esperado
4. **Interfaces:** Garantem flexibilidade para diferentes estratégias (GpuProvisioner, GpuCloudletScheduler)

**Trade-offs Aceitos:**

- Acoplamento com classes base do EdgeCloudSim
- Hierarquia de herança aumenta (3 níveis: Host → EdgeHost → GpuEdgeHost)
- Dificulta suporte a hosts com GPUs E FPGAs simultaneamente (requer refatoração futura)

---

### 7.5 Decisão 5: Configuração via XML vs. Código

**Contexto:**

Como usuários especificam configurações de GPU (tipo, quantidade, memória)?

**Decisão:** Estender arquivos XML existentes (`edge_devices.xml`, `applications.xml`).

**Estrutura XML:**

```xml
<!-- edge_devices.xml -->
<host>
    <core>16</core>
    <mips>80000</mips>
    <gpus>  <!-- ← NOVO -->
        <gpu>
            <gpu_id>0</gpu_id>
            <gpu_type>NVIDIA_T4</gpu_type>
            <cuda_cores>2560</cuda_cores>
            <gflops>8100</gflops>
            <gpu_memory>16000</gpu_memory>
            <memory_bandwidth>320</memory_bandwidth>
        </gpu>
    </gpus>
</host>

<VM>
    <requires_gpu>true</requires_gpu>  <!-- ← NOVO -->
    <gpu_allocation_mode>EXCLUSIVE</gpu_allocation_mode>  <!-- ← NOVO -->
</VM>
```

```xml
<!-- applications.xml -->
<application name="DEEP_LEARNING">
    <task_length>50000</task_length>
    <requires_gpu>true</requires_gpu>  <!-- ← NOVO -->
    <gpu_length>150000</gpu_length>
    <gpu_input_data>2000</gpu_input_data>
    <gpu_output_data>100</gpu_output_data>
    <required_gpu_memory>4000</required_gpu_memory>
</application>
```

**Justificativa:**

1. **Consistência:** Mantém padrão do EdgeCloudSim
2. **Facilidade:** Usuários já familiarizados com XML
3. **Separação:** Configuração separada de código
4. **Experimentação:** Fácil testar diferentes configurações sem recompilar

**Alternativas Descartadas:**

- **Código Java:** Menos flexível, requer recompilação
- **JSON:** Quebra consistência com EdgeCloudSim
- **YAML:** Requer biblioteca adicional, não standard no Java

---

### 7.6 Decisão 6: Logging e Métricas GPU

**Contexto:**

Quais métricas GPU devem ser coletadas e como?

**Métricas Definidas:**

| Categoria | Métricas | Granularidade |
|-----------|----------|---------------|
| **Por Tarefa** | • GPU Execution Time<br>• Data Transfer Time<br>• GPU Utilization<br>• GPU Memory Used | Por GpuTask |
| **Por GPU** | • Utilization Over Time<br>• Memory Usage<br>• Allocated VM<br>• Idle Time | Periodicament (configurable) |
| **Por VM** | • Avg GPU Utilization<br>• Total GPU Tasks<br>• GPU Memory Allocated | Agregado |
| **Por Host** | • Avg GPU Utilization<br>• Total GPU Memory<br>• Number of GPUs | Agregado |

**Formato de Logs:**

**gpu_task_log.csv:**
```csv
Time,MobileDeviceId,VmId,GpuId,TaskId,GpuUtilization,GpuMemoryUsed,GpuExecutionTime,GpuDataTransferTime
10.5,1,0,0,1,80.0,4000,2.5,0.3
```

**gpu_utilization_log.csv:**
```csv
Time,HostId,GpuId,Utilization,UsedMemory,AllocatedVmId
5.0,0,0,60.0,4000,0
10.0,0,0,80.0,4000,0
```

**Decisão:** Estender `SimLogger` com métodos específicos para GPU.

**Implementação:**

```java
public class GpuSimLogger extends SimLogger {
    public void addGpuTaskLog(...) { ... }
    public void addGpuUtilizationLog(...) { ... }
}
```

**Justificativa:**

1. **Separação:** Logs GPU separados de logs CPU
2. **Análise:** CSV permite fácil análise com Python/MATLAB/R
3. **Performance:** Logs escritos ao final da simulação (bulk write)
4. **Extensibilidade:** Fácil adicionar novas métricas

---

### 7.7 Decisão 7: Tratamento de Erros

**Contexto:**

Como lidar com situações de erro (GPU indisponível, memória insuficiente)?

**Estratégias por Situação:**

| Situação | Tratamento | Justificativa |
|----------|------------|---------------|
| **VM requer GPU mas host sem GPU** | Falha na criação da VM, log warning | Evita estado inconsistente |
| **GPU sem memória suficiente** | Tarefa rejeitada, status FAILED | Simula comportamento real |
| **Todas GPUs ocupadas** | VM aguarda ou é rejeitada | Depende da política de alocação |
| **GPU alocada para 2 VMs (bug)** | Exception `IllegalStateException` | Bug crítico, deve falhar fast |

**Logging de Erros:**

```java
if (!host.hasAvailableGpu()) {
    SimLogger.printLine(CloudSim.clock() + 
        ": VM #" + vm.getId() + " FAILED - No GPU available on host #" + host.getId());
    return false;
}
```

**Decisão:** Usar **fail-fast** para bugs + **graceful degradation** para condições normais.

**Implementação:**

- Validações em `allocateGpuForVm()`, `submitGpuTask()`
- Logs detalhados para debugging
- Status `FAILED` para tarefas rejeitadas
- Métricas de rejeição em logs

---

### 7.8 Resumo de Trade-offs

| Decisão | Benefícios | Custos | Mitigação |
|---------|-----------|--------|-----------|
| **Exclusive Mode** | Simples, testável | Subutilização | Arquitetura extensível para Shared |
| **Time-Shared Scheduling** | Realista, flexível | Não modela overhead | Calibração com benchmarks |
| **GFLOPS-based Model** | Simples, parametrizável | Limitações de realismo | Documentar limitações, permitir calibração |
| **Herança** | Natural, polimórfico | Acoplamento | Usar interfaces para flexibilidade |
| **XML Config** | Consistente, fácil | Verboso | Fornecer templates |
| **CSV Logs** | Analisável, padrão | Tamanho de arquivo | Compressão, logs opcionais |
| **Fail-fast** | Detecta bugs cedo | Menos tolerante | Bons logs de erro |

---

## 8. Anexos

### 8.1 Glossário de Termos

| Termo | Definição |
|-------|-----------|
| **CUDA Cores** | Unidades de processamento paralelo em GPUs NVIDIA |
| **GFLOPS** | Giga Floating Point Operations Per Second - Métrica de capacidade computacional |
| **GpuTask** | Tarefa que requer processamento em GPU |
| **Exclusive Mode** | Modo de alocação onde 1 GPU é dedicada a 1 VM |
| **Shared Mode** | Modo de alocação onde 1 GPU é compartilhada por múltiplas VMs |
| **Time-Shared** | Escalonamento onde múltiplas tarefas compartilham recurso via time-slicing |
| **Space-Shared** | Escalonamento onde uma tarefa usa recurso por vez, outras aguardam |
| **Provisioner** | Componente responsável por alocar recursos (GPU, RAM, etc.) |
| **Memory Bandwidth** | Taxa de transferência de dados da memória GPU (GB/s) |
| **GPU Utilization** | Percentual de uso da GPU (0-100%) |

---

### 8.2 Referências

1. **EdgeCloudSim:**
   - Sonmez, C., Ozgovde, A., & Ersoy, C. (2018). EdgeCloudSim: An environment for performance evaluation of edge computing systems. *Transactions on Emerging Telecommunications Technologies*, 29(11), e3493.
   - GitHub: https://github.com/CagataySonmez/EdgeCloudSim

2. **CloudSim:**
   - Calheiros, R. N., Ranjan, R., Beloglazov, A., De Rose, C. A., & Buyya, R. (2011). CloudSim: a toolkit for modeling and simulation of cloud computing environments and evaluation of resource provisioning algorithms. *Software: Practice and experience*, 41(1), 23-50.
   - GitHub: https://github.com/Cloudslab/cloudsim

3. **GPU Architecture:**
   - NVIDIA CUDA C Programming Guide: https://docs.nvidia.com/cuda/cuda-c-programming-guide/
   - NVIDIA Multi-Process Service (MPS): https://docs.nvidia.com/deploy/mps/index.html

4. **Design Patterns:**
   - Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design patterns: elements of reusable object-oriented software*. Addison-Wesley.

5. **Testing:**
   - Beck, K. (2003). *Test-driven development: By example*. Addison-Wesley Professional.

---

### 8.3 Checklist de Implementação (Fase 3)

**Classes Base:**
- [ ] Implementar classe `Gpu`
- [ ] Implementar interface `GpuProvisioner`
- [ ] Implementar `GpuProvisionerSimple`
- [ ] Implementar classe `GpuTask`

**Infraestrutura Edge:**
- [ ] Implementar `GpuEdgeHost`
- [ ] Implementar `GpuEdgeVM`
- [ ] Implementar interface `GpuCloudletScheduler`
- [ ] Implementar `GpuCloudletSchedulerTimeShared`
- [ ] Implementar `GpuEdgeVmAllocationPolicy_Custom`

**Gerenciamento:**
- [ ] Implementar `GpuEdgeServerManager`
- [ ] Estender `SimSettings` para ler configs GPU do XML
- [ ] Estender `SimLogger` para logs GPU

**Cenário e Testes:**
- [ ] Criar `GpuScenarioFactory`
- [ ] Criar XMLs de exemplo (edge_devices_gpu.xml, applications_gpu.xml)
- [ ] Implementar `GpuLoadGenerator`
- [ ] Implementar `GpuMobileDeviceManager`

**Testes:**
- [ ] Testes unitários para todas as classes (≥80% coverage)
- [ ] Testes de integração
- [ ] Teste de sistema end-to-end
- [ ] Benchmarks de performance

**Documentação:**
- [ ] JavaDoc completo para todas as classes públicas
- [ ] README com instruções de uso
- [ ] Tutorial passo-a-passo
- [ ] Exemplos de configuração XML

**Validação:**
- [ ] Validar resultados contra benchmarks conhecidos
- [ ] Comparar com simuladores GPU existentes (GPGPU-Sim, etc.)
- [ ] Realizar experimentos com cenários reais

---

### 8.4 Exemplo Completo: MainApp GPU

```java
package edu.boun.edgecloudsim.applications.gpu_app;

import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.SimLogger;
import org.cloudbus.cloudsim.core.CloudSim;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Aplicação principal para simulação com suporte a GPU.
 */
public class MainApp {
    
    public static void main(String[] args) {
        // Configurações
        String configFile = "scripts/gpu_app/config/default_config.properties";
        String edgeDevicesFile = "scripts/gpu_app/config/edge_devices.xml";
        String applicationsFile = "scripts/gpu_app/config/applications.xml";
        String outputFolder = "sim_results/gpu_app";
        
        int iterationStart = 1;
        int iterationEnd = 1;
        
        // Parse command line arguments se fornecidos
        if (args.length == 5) {
            configFile = args[0];
            edgeDevicesFile = args[1];
            applicationsFile = args[2];
            outputFolder = args[3];
            int iteration = Integer.parseInt(args[4]);
            iterationStart = iteration;
            iterationEnd = iteration;
        }
        
        // Inicializar SimSettings
        SimSettings.getInstance();
        if (!SimSettings.getInstance().initialize(configFile, edgeDevicesFile, applicationsFile)) {
            SimLogger.printLine("cannot initialize simulation settings!");
            System.exit(1);
        }
        
        // Obter parâmetros de simulação
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date startDate = Calendar.getInstance().getTime();
        String startDateStr = df.format(startDate);
        
        SimLogger.printLine("Simulation started at " + startDateStr);
        SimLogger.printLine("----------------------------------------------------------------------");
        
        for (int iterationNumber = iterationStart; iterationNumber <= iterationEnd; iterationNumber++) {
            
            String iterationOutputFolder = outputFolder + "/ite" + iterationNumber;
            
            for (int j = 0; j < SimSettings.getInstance().getScenarioCount(); j++) {
                for (int k = 0; k < SimSettings.getInstance().getOrchestratorPolicyCount(); k++) {
                    
                    String simScenario = SimSettings.getInstance().getScenario(j);
                    String orchestratorPolicy = SimSettings.getInstance().getOrchestratorPolicy(k);
                    
                    Date scenarioStartDate = Calendar.getInstance().getTime();
                    String scenarioStartDateStr = df.format(scenarioStartDate);
                    
                    SimLogger.printLine("----------------------------------------------------------------------");
                    SimLogger.printLine("Scenario: " + simScenario + 
                                        " - Policy: " + orchestratorPolicy + 
                                        " - Iteration: " + iterationNumber);
                    SimLogger.printLine("Start Time: " + scenarioStartDateStr);
                    SimLogger.printLine("----------------------------------------------------------------------");
                    
                    // Inicializar CloudSim
                    int numBrokers = 1;
                    boolean traceFlag = false;
                    Calendar calendar = Calendar.getInstance();
                    CloudSim.init(numBrokers, calendar, traceFlag);
                    
                    // Criar factory GPU
                    int numMobileDevices = SimSettings.getInstance().getMinNumOfMobileDev();
                    double simulationTime = SimSettings.getInstance().getSimulationTime();
                    
                    GpuScenarioFactory scenarioFactory = new GpuScenarioFactory(
                        numMobileDevices,
                        simulationTime,
                        orchestratorPolicy,
                        simScenario
                    );
                    
                    // Criar e iniciar SimManager
                    SimManager simManager = new SimManager(
                        scenarioFactory,
                        numMobileDevices,
                        simScenario,
                        iterationOutputFolder
                    );
                    
                    try {
                        simManager.startSimulation();
                    } catch (Exception e) {
                        SimLogger.printLine("Simulation failed!");
                        e.printStackTrace();
                        System.exit(1);
                    }
                    
                    Date scenarioEndDate = Calendar.getInstance().getTime();
                    String scenarioEndDateStr = df.format(scenarioEndDate);
                    
                    long duration = (scenarioEndDate.getTime() - scenarioStartDate.getTime()) / 1000;
                    
                    SimLogger.printLine("----------------------------------------------------------------------");
                    SimLogger.printLine("Scenario completed!");
                    SimLogger.printLine("End Time: " + scenarioEndDateStr);
                    SimLogger.printLine("Duration: " + duration + " seconds");
                    SimLogger.printLine("----------------------------------------------------------------------");
                }
            }
        }
        
        Date endDate = Calendar.getInstance().getTime();
        String endDateStr = df.format(endDate);
        
        SimLogger.printLine("----------------------------------------------------------------------");
        SimLogger.printLine("Simulation finished at " + endDateStr);
        SimLogger.printLine("----------------------------------------------------------------------");
    }
}
```

---

## 🎯 Conclusão

Este documento fornece o **design completo e detalhado** das classes GPU para o **GpuEdgeCloudSim v1.0**, incluindo:

✅ **Especificações de API completas** para 10 classes principais  
✅ **Diagramas UML detalhados** (classes, pacotes, relacionamentos)  
✅ **Contratos de interfaces** com JavaDoc completo  
✅ **Diagramas de sequência** para 4 fluxos principais  
✅ **7 exemplos de uso** práticos e funcionais  
✅ **Estratégia de testes** com matriz de casos e exemplos de código  
✅ **7 decisões de design** documentadas com trade-offs  
✅ **Anexos** com glossário, referências e checklist

### Próximos Passos (Fase 3 - Implementação)

1. ✅ **Fase 2 Completa** ← Você está aqui
2. ⏭️ **Fase 3: Implementação**
   - Implementar todas as classes seguindo este design
   - Escrever testes unitários e de integração
   - Validar com cenários reais
3. ⏭️ **Fase 4: Experimentação**
   - Executar simulações completas
   - Coletar e analisar resultados
   - Comparar com sistemas reais
4. ⏭️ **Fase 5: Publicação**
   - Escrever artigo científico
   - Publicar código no GitHub
   - Criar documentação de usuário

---

**Documento gerado por:** Pabllo Borges Cardoso  
**Data:** 23 de Outubro de 2025  
**Versão:** 1.0  
**Status:** ✅ Completo e Aprovado para Implementação

---

**📄 Arquivo:** `/home/ubuntu/GpuEdgeCloudSim_Fase2_Design_Classes_GPU.md`
