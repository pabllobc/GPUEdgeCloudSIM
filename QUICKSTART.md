
# 🚀 Quick Start Guide - GpuEdgeCloudSim v1.0

Este guia rápido permite que você execute sua primeira simulação em **5 minutos**.

## Pré-requisitos

- Java JDK 21 instalado
- Git instalado
- 4 GB RAM disponível

## Passo 1: Clone o Repositório

```bash
git clone https://github.com/pabllobc/GPUEdgeCloudSIM.git
cd GPUEdgeCloudSIM
```

## Passo 2: Compile o Projeto

```bash
cd scripts
chmod +x compile.sh
./compile.sh
```

**Saída esperada:**
```
✅ Compilação concluída com sucesso!
```

## Passo 3: Execute sua Primeira Simulação

### Cenário 1 - Baseline CPU-only

```bash
java -Xmx4G -cp ".:lib/*:bin/" \
  edu.boun.edgecloudsim.applications.gpusim.GpuSimulationMain \
  1 1000 BASELINE_CPU SCENARIO1
```

Aguarde aproximadamente **2-3 minutos** para a simulação completar.

## Passo 4: Visualize os Resultados

Os resultados estarão em `sim_results/`:

```bash
ls -la sim_results/
cat sim_results/summary.log
```

## Passo 5: Análise Gráfica (Opcional)

Se você tem Python instalado:

```bash
cd scripts
python3 analyze_results.py ../sim_results/
```

Isto gerará gráficos em `sim_results/plots/`.

## Próximos Passos

1. **Execute outros cenários:**
   - Cenário 2 (GPU Offloading): veja [README Cenário 2](scenarios/scenario2_gpu_offloading/README.md)
   - Cenário 3 (Híbrido): veja [README Cenário 3](scenarios/scenario3_hybrid_scheduling/README.md)
   - Cenário 4 (Stress Test): veja [README Cenário 4](scenarios/scenario4_multigpu_balancing/README.md)

2. **Personalize configurações:**
   - Edite `config/config.properties`
   - Modifique `config/edge_devices.xml`
   - Ajuste `config/applications.xml`

3. **Explore a documentação:**
   - [Fase 1 - Análise Arquitetural](docs/fases/GpuEdgeCloudSim_Fase1_Analise_Arquitetural.md)
   - [Fase 2 - Design de Classes GPU](docs/fases/GpuEdgeCloudSim_Fase2_Design_Classes_GPU.md)
   - [Análise Científica Completa](docs/analises/GpuEdgeCloudSim_Analise_Resultados_Cientificos.md)

## Troubleshooting

### Erro: "Out of Memory"

Aumente a memória alocada:
```bash
java -Xmx8G -cp ...
```

### Erro: "Could not find or load main class"

Verifique se o classpath está correto:
```bash
java -cp ".:lib/*:bin/" edu.boun.edgecloudsim.applications.gpusim.GpuSimulationMain
```

### Erro de Compilação

Certifique-se de ter Java JDK 21:
```bash
java -version
javac -version
```

## Suporte

- 📚 [Documentação Completa](README.md)
- 🐛 [Relatar Issues](https://github.com/pabllobc/GPUEdgeCloudSIM/issues)
- 💬 [Discussões](https://github.com/pabllobc/GPUEdgeCloudSIM/discussions)

---

**Pronto!** Você executou sua primeira simulação GpuEdgeCloudSim. 🎉
