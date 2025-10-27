
# 📊 Cenário 1: Machine Learning Inference at the Edge

**Autor:** Pabllo Borges Cardoso  
**Data:** 23 de Outubro de 2025  
**Versão:** 1.0  
**GpuEdgeCloudSim:** v1.0

---

## 1. Visão Geral do Cenário

### 1.1 Contexto
Aplicações de inferência de Machine Learning (ML) em tempo real para detecção de objetos e classificação de imagens em dispositivos móveis (smartphones, câmeras inteligentes, drones).

### 1.2 Casos de Uso Reais
- **Vigilância Inteligente**: Detecção de objetos em câmeras de segurança
- **Comércio Inteligente**: Reconhecimento de produtos em lojas automatizadas
- **Assistentes Visuais**: Detecção de objetos para deficientes visuais
- **Controle de Tráfego**: Detecção de veículos e pedestres

### 1.3 Motivação Científica
ML inference no edge reduz latência crítica para aplicações em tempo real, mas requer GPUs para throughput adequado. Este cenário investiga o trade-off entre latência, energia e custo.

---

## 2. Configuração do Cenário

### 2.1 Infraestrutura Edge

```yaml
Edge Servers: 4 datacenters
  - Datacenter 1: 1x NVIDIA T4 (8.1 TFLOPs)
  - Datacenter 2: 1x NVIDIA A100 (19.5 TFLOPs)
  - Datacenter 3: 2x NVIDIA T4 (16.2 TFLOPs total)
  - Datacenter 4: 1x NVIDIA T4 (8.1 TFLOPs)

Total GPU Capacity: 51.9 TFLOPs
```

### 2.2 Dispositivos Móveis

```yaml
Mobile Devices: 1000-3000 (incrementos de 500)
Device Types:
  - Smartphones (60%): Câmeras HD, sensores
  - Smart Cameras (30%): Câmeras 4K, conexão 5G
  - Drones (10%): Câmeras especializadas, GPS
```

### 2.3 Aplicações ML

```yaml
Application Mix:
  - Object Detection (YOLOv5): 50% das tarefas
    - GPU: 250 GFLOPs, 4 GB VRAM
    - Latência máxima: 500ms
    
  - Image Classification (ResNet-50): 30% das tarefas
    - GPU: 150 GFLOPs, 2 GB VRAM
    - Latência máxima: 300ms
    
  - Image Segmentation (U-Net): 20% das tarefas
    - GPU: 400 GFLOPs, 6 GB VRAM
    - Latência máxima: 800ms

Task Arrival: Poisson(λ=0.1 tasks/second/device)
```

---

## 3. Políticas de Orquestração Testadas

### 3.1 NEAREST_GPU
- **Critério**: Seleciona edge server mais próximo com GPU disponível
- **Objetivo**: Minimizar latência de rede
- **Hipótese**: Proximidade é fator dominante para QoS

### 3.2 LOAD_BALANCED_GPU
- **Critério**: Distribui tarefas para GPU com menor utilização
- **Objetivo**: Maximizar throughput e evitar sobrecarga
- **Hipótese**: Balanceamento previne hotspots

### 3.3 MEMORY_AWARE_GPU
- **Critério**: Prioriza GPUs com mais memória disponível
- **Objetivo**: Evitar falhas por falta de VRAM
- **Hipótese**: Memória é gargalo para modelos grandes

### 3.4 ENERGY_EFFICIENT_GPU
- **Critério**: Minimiza consumo energético total
- **Objetivo**: Reduzir custo operacional e pegada de carbono
- **Hipótese**: Eficiência energética é crítica para sustentabilidade

### 3.5 HYBRID_GPU (Proposta)
- **Critério**: Combinação ponderada de latência (40%), utilização (30%), memória (30%)
- **Objetivo**: Balancear múltiplos objetivos
- **Hipótese**: Abordagem híbrida oferece melhor QoS geral

---

## 4. Métricas de Avaliação

### 4.1 Métricas de Performance

| Métrica | Fórmula | Objetivo |
|---------|---------|----------|
| **Latência Média** | $\bar{L} = \frac{1}{N}\sum_{i=1}^{N} (T_{finish}^i - T_{submit}^i)$ | Minimizar |
| **Throughput** | $T = \frac{N_{completed}}{T_{simulation}}$ | Maximizar |
| **Taxa de Sucesso** | $S = \frac{N_{completed}}{N_{submitted}} \times 100\%$ | Maximizar |
| **Latência P95** | Latência do 95º percentil | < 1 segundo |
| **Latência P99** | Latência do 99º percentil | < 2 segundos |

### 4.2 Métricas de Recursos

| Métrica | Fórmula | Objetivo |
|---------|---------|----------|
| **Utilização GPU Média** | $\bar{U}_{GPU} = \frac{1}{M}\sum_{j=1}^{M} U_j$ | 60-80% (balanceado) |
| **Utilização GPU Máxima** | $U_{GPU}^{max} = \max(U_1, ..., U_M)$ | < 95% |
| **Uso de Memória GPU** | $M_{used} = \sum_{j=1}^{M} VRAM_j^{used}$ | < 90% da capacidade |
| **Taxa de Rejeição** | $R = \frac{N_{rejected}}{N_{submitted}} \times 100\%$ | Minimizar |

### 4.3 Métricas de Energia

| Métrica | Fórmula | Objetivo |
|---------|---------|----------|
| **Energia Total** | $E_{total} = \sum_{j=1}^{M} \int_0^T P_j(t) dt$ | Minimizar |
| **Energia por Tarefa** | $E_{task} = \frac{E_{total}}{N_{completed}}$ | Minimizar |
| **Eficiência Energética** | $\eta = \frac{N_{completed}}{E_{total}}$ | Maximizar |

### 4.4 Métricas de Custo

| Métrica | Fórmula | Objetivo |
|---------|---------|----------|
| **Custo Computacional** | $C_{compute} = \sum_{j=1}^{M} (T_j \times r_j)$ | Minimizar |
| **Custo de Rede** | $C_{network} = \sum_{i=1}^{N} (Data_i \times b)$ | Minimizar |
| **Custo Total** | $C_{total} = C_{compute} + C_{network}$ | Minimizar |

---

## 5. Metodologia de Validação

### 5.1 Experimentos

```yaml
Experiment Design:
  Type: Factorial Design 2^3
  Factors:
    - Device Count: [1000, 1500, 2000, 2500, 3000]
    - Orchestration Policy: [NEAREST, LOAD_BALANCED, MEMORY_AWARE, ENERGY, HYBRID]
    - Application Mix: [Light, Medium, Heavy]
  
  Total Configurations: 5 × 5 × 3 = 75
  
  Replications: 10 (para intervalos de confiança de 95%)
  
  Simulation Time: 600 segundos (10 minutos)
  
  Warm-up Period: 60 segundos
```

### 5.2 Análise Estatística

```python
# Análise de Variância (ANOVA)
# H0: Não há diferença entre políticas de orquestração
# H1: Pelo menos uma política difere significativamente

alpha = 0.05  # Nível de significância

# Teste de Tukey HSD para comparações pairwise
# Intervalos de confiança de 95%
```

### 5.3 Validação de Resultados

**Critérios de Aceitação:**
- Latência média < 500ms para 90% das tarefas
- Taxa de sucesso > 95%
- Utilização GPU balanceada (CV < 0.3)
- Energia por tarefa < 100 Joules

---

## 6. Resultados Esperados

### 6.1 Hipóteses Científicas

**H1:** *NEAREST_GPU oferece menor latência para cargas baixas*  
**Justificativa:** Proximidade minimiza atraso de rede quando GPUs não estão saturadas.

**H2:** *LOAD_BALANCED_GPU oferece maior throughput para cargas altas*  
**Justificativa:** Distribuição uniforme previne sobrecarga de servidores.

**H3:** *HYBRID_GPU oferece melhor QoS geral em todos os cenários*  
**Justificativa:** Abordagem multi-objetivo balanceia trade-offs.

**H4:** *ENERGY_EFFICIENT_GPU reduz custo energético em > 20%*  
**Justificativa:** Consolidação de tarefas reduz GPUs ociosas.

### 6.2 Gráficos de Validação

```yaml
Gráficos Esperados:
  1. Latência vs Device Count (por política)
  2. Throughput vs Device Count (por política)
  3. Utilização GPU (boxplot por datacenter)
  4. Energia Total vs Device Count
  5. Trade-off: Latência vs Energia (Pareto frontier)
  6. CDF de Latências (comparação entre políticas)
  7. Heatmap de Utilização GPU ao longo do tempo
```

---

## 7. Configuração para Execução

### 7.1 Arquivo de Configuração

```bash
# config/scenario1_ml_inference.properties

simulation_time=600
warm_up_period=60
mobile_device_counter_size=500
min_number_of_mobile_devices=1000
max_number_of_mobile_devices=3000

orchestrator_policies=NEAREST_GPU,LOAD_BALANCED_GPU,MEMORY_AWARE_GPU,ENERGY_EFFICIENT_GPU,HYBRID_GPU

simulation_scenarios=ML_INFERENCE_SCENARIO

edge_devices_file=config/edge_devices.xml
applications_file=config/applications_ml_inference.xml

gpu_enabled=true
gpu_energy_enabled=true
latency_breakdown_tracking=true
```

### 7.2 Comando de Execução

```bash
#!/bin/bash
# run_scenario1.sh

NUM_ITERATIONS=10

for DEVICES in 1000 1500 2000 2500 3000; do
  for POLICY in NEAREST_GPU LOAD_BALANCED_GPU HYBRID_GPU; do
    echo "Running: Devices=$DEVICES, Policy=$POLICY"
    
    java -Xmx4G -cp .:lib/* \
      edu.boun.edgecloudsim.applications.gpusim.GpuSimulationMain \
      $NUM_ITERATIONS $DEVICES $POLICY ML_INFERENCE_SCENARIO
      
    echo "Completed: Devices=$DEVICES, Policy=$POLICY"
  done
done

echo "Scenario 1 completed. Results in sim_results/"
```

---

## 8. Publicação Científica

### 8.1 Contribuições Científicas

1. **Benchmark realista** de ML inference no edge com GPUs
2. **Comparação empírica** de políticas de orquestração GPU
3. **Análise de trade-offs** latência-energia-custo
4. **Proposta de política híbrida** otimizada

### 8.2 Conferências Alvo

- **IEEE INFOCOM** (Networking)
- **ACM EdgeSys** (Edge Computing)
- **IEEE/ACM CCGrid** (Cloud and Grid Computing)
- **ACM SoCC** (Symposium on Cloud Computing)

### 8.3 Estrutura do Paper

```markdown
1. Introduction
   - Motivação: ML inference at the edge
   - Problema: Orquestração eficiente de GPUs
   - Contribuições: Benchmark + Política híbrida

2. Related Work
   - Edge computing simulators
   - GPU resource management
   - ML inference optimization

3. System Model
   - Arquitetura GpuEdgeCloudSim
   - Modelo de GPU
   - Modelo de energia

4. Orchestration Policies
   - NEAREST_GPU
   - LOAD_BALANCED_GPU
   - HYBRID_GPU (proposta)

5. Experimental Setup
   - Cenário ML Inference
   - Configurações
   - Métricas

6. Results and Analysis
   - Performance comparison
   - Energy analysis
   - Trade-off analysis

7. Conclusion and Future Work
```

---

## 9. Referências

[1] Sonmez, C., Ozgovde, A., & Ersoy, C. (2018). EdgeCloudSim: An environment for performance evaluation of edge computing systems. *Transactions on Emerging Telecommunications Technologies*, 29(11), e3493.

[2] Redmon, J., & Farhadi, A. (2018). YOLOv3: An incremental improvement. *arXiv preprint arXiv:1804.02767*.

[3] He, K., Zhang, X., Ren, S., & Sun, J. (2016). Deep residual learning for image recognition. In *CVPR* (pp. 770-778).

[4] Nvidia. (2021). NVIDIA T4 Tensor Core GPU Datasheet.

[5] Nvidia. (2020). NVIDIA A100 Tensor Core GPU Architecture.

---

**Validação Científica:** Este cenário foi projetado para ser **reproduzível**, **realista** e **cientificamente rigoroso**, permitindo publicação em conferências de alto impacto.
