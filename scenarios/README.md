
# 🔬 Cenários Científicos - GpuEdgeCloudSim v1.0

Este diretório contém os **4 cenários científicos validados** do GpuEdgeCloudSim, projetados para avaliar diferentes aspectos de desempenho, eficiência energética e políticas de escalonamento em ambientes Edge heterogêneos com recursos CPU+GPU.

---

## 📊 Visão Geral dos Cenários

| Cenário | Tipo | GPU | Carga | Objetivo |
|---------|------|-----|-------|----------|
| **1** | Baseline | ❌ Não | Normal | Linha de base para comparação |
| **2** | GPU Offloading | ✅ Sim | Normal | Aceleração máxima via GPU |
| **3** | Híbrido | ✅ Sim | Normal | Balanceamento CPU+GPU |
| **4** | Multi-GPU Stress | ✅ Sim | Alta (4x) | Escalabilidade multi-GPU |

---

## 1️⃣ Cenário 1: Baseline CPU-only

### 📁 Diretório
[`scenario1_baseline_cpu/`](scenario1_baseline_cpu/)

### 🎯 Objetivo
Estabelecer uma **linha de base** para comparação, utilizando exclusivamente processamento CPU sem aceleração GPU.

### 🔧 Configuração
- **Processamento:** CPU-only
- **Servidores Edge:** 6
- **CPUs por Servidor:** 8 cores
- **GPU:** Nenhuma
- **Tarefas:** 90.000

### 📊 Resultados-Chave
- Latência média: **642.63 ms**
- Throughput: **146.27 t/s**
- Taxa de sucesso: **97.45%**
- Energia/tarefa: **26.01 J**

### 🚀 Como Executar
```bash
java -Xmx4G -cp ".:lib/*:bin/" \
  edu.boun.edgecloudsim.applications.gpusim.GpuSimulationMain \
  1 1000 BASELINE_CPU SCENARIO1
```

---

## 2️⃣ Cenário 2: GPU Offloading Básico

### 📁 Diretório
[`scenario2_gpu_offloading/`](scenario2_gpu_offloading/)

### 🎯 Objetivo
Avaliar a **aceleração máxima** via GPU com offloading prioritário e política FIFO.

### 🔧 Configuração
- **Processamento:** GPU prioritário
- **Servidores Edge:** 6
- **GPUs por Servidor:** 2x NVIDIA T4
- **Memória GPU:** 16 GB
- **Tarefas:** 90.000

### 📊 Resultados-Chave
- Latência média: **109.02 ms** (-83% vs Baseline)
- Energia/tarefa: **11.88 J** (-54% vs Baseline)
- Utilização GPU: **82.49%**

### 📈 Melhorias vs Baseline
| Métrica | Melhoria |
|---------|----------|
| Latência | **-83.04%** 🟢 |
| Energia | **-54.30%** 🟢 |

### 🚀 Como Executar
```bash
java -Xmx4G -cp ".:lib/*:bin/" \
  edu.boun.edgecloudsim.applications.gpusim.GpuSimulationMain \
  2 1000 GPU_OFFLOADING SCENARIO2
```

---

## 3️⃣ Cenário 3: Hybrid Scheduling Inteligente

### 📁 Diretório
[`scenario3_hybrid_scheduling/`](scenario3_hybrid_scheduling/)

### 🎯 Objetivo
Avaliar **balanceamento dinâmico** entre CPU e GPU com decisões inteligentes baseadas em características da tarefa.

### 🔧 Configuração
- **Processamento:** Híbrido CPU+GPU
- **Servidores Edge:** 6
- **GPUs por Servidor:** 2x NVIDIA T4
- **Threshold CPU:** 60%
- **Threshold GPU:** 70%
- **Tarefas:** 90.000

### 📊 Resultados-Chave
- Latência média: **252.33 ms** (-60% vs Baseline)
- Utilização CPU: **51.24%**
- Utilização GPU: **41.30%**
- Balanceamento: 55% CPU / 45% GPU

### 📈 Melhorias vs Baseline
| Métrica | Melhoria |
|---------|----------|
| Latência | **-60.74%** 🟢 |
| Energia | **-38.51%** 🟢 |
| Taxa Sucesso | **97.20%** ✅ |

### 🚀 Como Executar
```bash
java -Xmx4G -cp ".:lib/*:bin/" \
  edu.boun.edgecloudsim.applications.gpusim.GpuSimulationMain \
  3 1000 HYBRID_INTELLIGENT SCENARIO3
```

---

## 4️⃣ Cenário 4: Multi-GPU com Balanceamento (Stress Test)

### 📁 Diretório
[`scenario4_multigpu_balancing/`](scenario4_multigpu_balancing/)

### 🎯 Objetivo
Validar **escalabilidade** do sistema sob alta carga com múltiplas GPUs e balanceamento avançado.

### 🔧 Configuração
- **Processamento:** Multi-GPU
- **Servidores Edge:** 6
- **GPUs por Servidor:** 4x NVIDIA A100
- **Memória GPU:** 40 GB cada
- **Tarefas:** **360.000** (4x carga)

### 📊 Resultados-Chave
- Latência média: **108.71 ms** (-83% vs Baseline)
- Throughput: **570.41 t/s** (+290% vs Baseline)
- Tarefas completadas: **342.243**
- Utilização GPU: **82.52%**

### 📈 Melhorias vs Baseline
| Métrica | Melhoria |
|---------|----------|
| Latência | **-83.08%** 🟢 |
| Throughput | **+289.98%** 🟢 |
| Energia | **-54.27%** 🟢 |

### 🚀 Como Executar
```bash
java -Xmx8G -cp ".:lib/*:bin/" \
  edu.boun.edgecloudsim.applications.gpusim.GpuSimulationMain \
  4 1000 MULTIGPU_STRESS SCENARIO4
```

---

## 📊 Comparação Consolidada

### Tabela Resumida

| Métrica | Cenário 1 | Cenário 2 | Cenário 3 | Cenário 4 |
|---------|-----------|-----------|-----------|-----------|
| **Latência (ms)** | 642.63 | **109.02** ⭐ | 252.33 | **108.71** ⭐ |
| **Throughput (t/s)** | 146.27 | 144.32 | 146.43 | **570.41** ⭐ |
| **Taxa Sucesso (%)** | **97.45** ⭐ | 96.96 | 97.20 | 95.09 |
| **Energia/Tarefa (J)** | 26.01 | **11.88** ⭐ | 15.99 | **11.89** ⭐ |
| **Utilização GPU (%)** | 0.00 | **82.49** | 41.30 | **82.52** |

⭐ = Melhor resultado na categoria

### Gráficos Comparativos

Visualizações científicas completas estão disponíveis em [`/results/graficos/`](../results/graficos/):

1. `01_comparacao_latencia_completa.png` - Comparação de latências
2. `02_boxplot_latencias.png` - Distribuição de latências
3. `03_throughput_e_sucesso.png` - Throughput e taxa de sucesso
4. `04_utilizacao_recursos.png` - Utilização de CPU e GPU
5. `05_eficiencia_energetica.png` - Eficiência energética
6. `10_dashboard_consolidado.png` - Dashboard completo

---

## 🔧 Personalização de Cenários

### Modificando Parâmetros

Cada cenário possui seus próprios arquivos de configuração:

```
scenarioX_*/
├── README.md              # Documentação do cenário
├── applications.xml       # Definição de aplicações
└── edge_devices.xml       # Especificação de dispositivos
```

### Criando Novo Cenário

1. Duplique um cenário existente:
   ```bash
   cp -r scenario1_baseline_cpu/ scenario5_meu_cenario/
   ```

2. Edite os arquivos XML conforme necessário

3. Atualize o README.md com a descrição

4. Execute a simulação:
   ```bash
   java -Xmx4G -cp ".:lib/*:bin/" \
     edu.boun.edgecloudsim.applications.gpusim.GpuSimulationMain \
     5 1000 MEU_POLICY SCENARIO5
   ```

---

## 📚 Documentação Adicional

- **Análise Científica Completa:** [`/docs/analises/GpuEdgeCloudSim_Analise_Resultados_Cientificos.md`](../docs/analises/GpuEdgeCloudSim_Analise_Resultados_Cientificos.md)
- **Validação Científica:** [`/docs/analises/GpuEdgeCloudSim_Validacao_Cientifica_Explicacao.md`](../docs/analises/GpuEdgeCloudSim_Validacao_Cientifica_Explicacao.md)
- **Fase 4 - Integração e Testes:** [`/docs/fases/GpuEdgeCloudSim_Fase4_Integracao_Testes.md`](../docs/fases/GpuEdgeCloudSim_Fase4_Integracao_Testes.md)

---

## 🎓 Cenários de Aplicação

Além dos 4 cenários principais, o diretório também contém documentação de **5 cenários de aplicação específicos**:

1. **ML Inference** - [`SCENARIO1_ML_INFERENCE.md`](SCENARIO1_ML_INFERENCE.md)
2. **Video Processing** - [`SCENARIO2_VIDEO_PROCESSING.md`](SCENARIO2_VIDEO_PROCESSING.md)
3. **Scientific Computing** - [`SCENARIO3_SCIENTIFIC_COMPUTING.md`](SCENARIO3_SCIENTIFIC_COMPUTING.md)
4. **AR/VR Rendering** - [`SCENARIO4_ARVR_RENDERING.md`](SCENARIO4_ARVR_RENDERING.md)
5. **Mixed Workload** - [`SCENARIO5_MIXED_WORKLOAD.md`](SCENARIO5_MIXED_WORKLOAD.md)

Estes cenários demonstram aplicações práticas do GpuEdgeCloudSim em diferentes domínios.

---

## 🤝 Contribuindo com Novos Cenários

Tem um cenário interessante? Contribua!

1. Crie o cenário seguindo a estrutura existente
2. Valide os resultados
3. Documente adequadamente
4. Submeta um Pull Request

Veja [`CONTRIBUTING.md`](../CONTRIBUTING.md) para detalhes.

---

## 📞 Suporte

Dúvidas sobre os cenários?

- 📚 Consulte a [documentação completa](../README.md)
- 🐛 Reporte issues no [GitHub](https://github.com/pabllobc/GPUEdgeCloudSIM/issues)
- 💬 Inicie uma [discussão](https://github.com/pabllobc/GPUEdgeCloudSIM/discussions)

---

**Desenvolvido com 💻 e ☕ por Pabllo Borges Cardoso**

*Última atualização: Outubro 2025*
