
# ⚖️ Cenário 3: Hybrid Scheduling Inteligente

## Descrição

Este cenário implementa **escalonamento híbrido inteligente** que balanceia dinamicamente cargas entre CPU e GPU com base em características da tarefa, disponibilidade de recursos e políticas de QoS.

## Características

- **Processamento:** Híbrido CPU+GPU com balanceamento dinâmico
- **Escalonamento:** Decisão inteligente baseada em características da tarefa
- **Objetivo:** Otimizar uso de recursos e QoS

## Configuração

```properties
# Cenário 3 - Hybrid Scheduling Inteligente
SCENARIO_TYPE=HYBRID_SCHEDULING
GPU_ENABLED=true
SCHEDULING_POLICY=HYBRID_INTELLIGENT
EDGE_SERVER_CPU_CORES=8
EDGE_SERVER_GPU_COUNT=2
GPU_MODEL=NVIDIA_T4
GPU_MEMORY=16GB
GPU_COMPUTE_UNITS=2560
HYBRID_THRESHOLD_CPU=60
HYBRID_THRESHOLD_GPU=70
```

## Parâmetros de Simulação

| Parâmetro | Valor |
|-----------|-------|
| Servidores Edge | 6 |
| CPU por Servidor | 8 cores |
| GPUs por Servidor | 2x NVIDIA T4 |
| Memória GPU | 16 GB |
| Tarefas Simuladas | 90.000 |
| Duração Simulação | 600s |
| Threshold CPU | 60% |
| Threshold GPU | 70% |

## Algoritmo de Decisão

O escalonador híbrido considera:
1. **Tipo de tarefa** (ML, Video, AR/VR, IoT)
2. **Utilização atual** de CPU e GPU
3. **Fila de espera** em cada recurso
4. **Requisitos de QoS** da aplicação

## Métricas Coletadas

- ✅ Latência média: **252.33 ms** (-60% vs Baseline)
- ✅ Throughput: **146.43 tarefas/s**
- ✅ Taxa de sucesso: **97.20%**
- ✅ Utilização CPU: **51.24%**
- ✅ Utilização GPU: **41.30%**
- ✅ Energia por tarefa: **15.99 J** (-38% vs Baseline)

## Balanceamento de Recursos

- CPU: **55%** das tarefas
- GPU: **45%** das tarefas
- Balanceamento equilibrado entre recursos

## Melhorias vs Baseline

| Métrica | Melhoria |
|---------|----------|
| Latência | **-60.74%** 🟢 |
| Latência P99 | **-59.66%** 🟢 |
| Energia/Tarefa | **-38.51%** 🟢 |

## Como Executar

```bash
# Compilar
cd scripts
./compile.sh

# Executar Cenário 3
java -Xmx4G -cp ".:lib/*:bin/" \
  edu.boun.edgecloudsim.applications.gpusim.GpuSimulationMain \
  3 1000 HYBRID_INTELLIGENT SCENARIO3
```

## Resultados

O cenário híbrido oferece excelente trade-off entre desempenho e utilização de recursos, mantendo alta taxa de sucesso. Resultados completos em `/results/dados/`.

## Referências

- Documentação completa: `/docs/fases/GpuEdgeCloudSim_Fase4_Integracao_Testes.md`
- Análise científica: `/docs/analises/GpuEdgeCloudSim_Analise_Resultados_Cientificos.md`
