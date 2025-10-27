# 🔬 Cenário 1: Baseline CPU-only

## Descrição

Este cenário representa o baseline para comparação, utilizando **exclusivamente processamento CPU** sem qualquer aceleração GPU. É o cenário de referência para avaliar as melhorias introduzidas pelo GpuEdgeCloudSim.

## Características

- **Processamento:** CPU-only (sem GPU)
- **Escalonamento:** FIFO tradicional
- **Objetivo:** Estabelecer linha de base para métricas de desempenho

## Configuração

```properties
# Cenário 1 - Baseline CPU-only
SCENARIO_TYPE=BASELINE_CPU
GPU_ENABLED=false
SCHEDULING_POLICY=CPU_FIFO
EDGE_SERVER_CPU_CORES=8
EDGE_SERVER_GPU_COUNT=0
```

## Parâmetros de Simulação

| Parâmetro | Valor |
|-----------|-------|
| Servidores Edge | 6 |
| CPU por Servidor | 8 cores |
| Memória RAM | 32 GB |
| GPU | Nenhuma |
| Tarefas Simuladas | 90.000 |
| Duração Simulação | 600s |

## Métricas Coletadas

- ✅ Latência média: **642.63 ms**
- ✅ Throughput: **146.27 tarefas/s**
- ✅ Taxa de sucesso: **97.45%**
- ✅ Utilização CPU: **84.97%**
- ✅ Energia por tarefa: **26.01 J**

## Como Executar

```bash
# Compilar
cd scripts
./compile.sh

# Executar Cenário 1
java -Xmx4G -cp ".:lib/*:bin/" \
  edu.boun.edgecloudsim.applications.gpusim.GpuSimulationMain \
  1 1000 BASELINE_CPU SCENARIO1
```

## Resultados

Os resultados deste cenário servirão como baseline para comparação com os cenários GPU-enabled. Todos os resultados estão disponíveis em `/results/dados/`.

## Referências

- Documentação completa: `/docs/fases/GpuEdgeCloudSim_Fase4_Integracao_Testes.md`
- Análise científica: `/docs/analises/GpuEdgeCloudSim_Analise_Resultados_Cientificos.md`
