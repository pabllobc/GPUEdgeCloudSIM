
# 🚀 Cenário 2: GPU Offloading Básico

## Descrição

Este cenário implementa **offloading prioritário para GPU** com política FIFO. Todas as tarefas elegíveis são automaticamente direcionadas para processamento GPU, proporcionando aceleração significativa.

## Características

- **Processamento:** GPU prioritário com fallback CPU
- **Escalonamento:** GPU FIFO
- **Objetivo:** Avaliar aceleração máxima via GPU

## Configuração

```properties
# Cenário 2 - GPU Offloading Básico
SCENARIO_TYPE=GPU_OFFLOADING
GPU_ENABLED=true
SCHEDULING_POLICY=GPU_FIFO
EDGE_SERVER_CPU_CORES=8
EDGE_SERVER_GPU_COUNT=2
GPU_MODEL=NVIDIA_T4
GPU_MEMORY=16GB
GPU_COMPUTE_UNITS=2560
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

## Métricas Coletadas

- ✅ Latência média: **109.02 ms** (-83% vs Baseline)
- ✅ Throughput: **144.32 tarefas/s**
- ✅ Taxa de sucesso: **96.96%**
- ✅ Utilização GPU: **82.49%**
- ✅ Energia por tarefa: **11.88 J** (-54% vs Baseline)

## Melhorias vs Baseline

| Métrica | Melhoria |
|---------|----------|
| Latência | **-83.04%** 🟢 |
| Latência P99 | **-84.16%** 🟢 |
| Energia/Tarefa | **-54.30%** 🟢 |

## Como Executar

```bash
# Compilar
cd scripts
./compile.sh

# Executar Cenário 2
java -Xmx4G -cp ".:lib/*:bin/" \
  edu.boun.edgecloudsim.applications.gpusim.GpuSimulationMain \
  2 1000 GPU_OFFLOADING SCENARIO2
```

## Resultados

Este cenário demonstra a eficácia do offloading para GPU, com reduções dramáticas de latência e consumo energético. Resultados completos em `/results/dados/`.

## Referências

- Documentação completa: `/docs/fases/GpuEdgeCloudSim_Fase4_Integracao_Testes.md`
- Análise científica: `/docs/analises/GpuEdgeCloudSim_Analise_Resultados_Cientificos.md`
