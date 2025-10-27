
# 🎥 Cenário 2: Real-Time Video Processing at the Edge

**Autor:** Pabllo Borges Cardoso  
**Data:** 23 de Outubro de 2025  
**Versão:** 1.0  
**GpuEdgeCloudSim:** v1.0

---

## 1. Visão Geral do Cenário

### 1.1 Contexto
Processamento de vídeo em tempo real para streaming, transcoding, filtragem e análise de conteúdo visual em aplicações móveis e IoT.

### 1.2 Casos de Uso Reais
- **Live Streaming**: Plataformas tipo Twitch, YouTube Live
- **Video Conferencing**: Zoom, Teams, Google Meet
- **Video Surveillance**: Análise de vídeo em tempo real
- **Social Media**: Filtros AR/VR em Instagram, Snapchat
- **Autonomous Vehicles**: Processamento de câmeras em veículos autônomos

### 1.3 Motivação Científica
Processamento de vídeo é extremamente intensivo em GPU, com requisitos de latência rígidos para aplicações em tempo real. Este cenário investiga escalabilidade e QoS de sistemas edge com workloads de vídeo.

---

## 2. Configuração do Cenário

### 2.1 Infraestrutura Edge

```yaml
Edge Servers: 4 datacenters (mesma configuração Cenário 1)
  - Foco: GPUs com encoders de hardware (NVENC)
  - NVIDIA T4: 40 Encoders paralelos
  - NVIDIA A100: 60 Encoders paralelos

Total Encoding Capacity: 220 streams simultâneos
```

### 2.2 Dispositivos Móveis

```yaml
Mobile Devices: 500-2500 (incrementos de 500)
Device Types:
  - Smartphones (50%): HD video (1080p)
  - Tablets (20%): HD video
  - Streaming Cameras (20%): 4K video
  - Smart Glasses (10%): VR video (180°/360°)
```

### 2.3 Aplicações de Vídeo

```yaml
Application Mix:
  - Real-time Transcoding: 40% das tarefas
    - GPU: 800 GFLOPs, 8 GB VRAM
    - Input: 10 MB/chunk, Output: 5 MB/chunk
    - Latência máxima: 1 segundo
    
  - Video Filtering (AR filters): 35% das tarefas
    - GPU: 350 GFLOPs, 4 GB VRAM
    - Input: 4 MB/frame, Output: 4 MB/frame
    - Latência máxima: 100ms (para 30 fps)
    
  - Video Analytics: 25% das tarefas
    - GPU: 500 GFLOPs, 6 GB VRAM
    - Input: 8 MB/frame, Output: 2 MB/results
    - Latência máxima: 500ms

Task Arrival: Bursty (Active/Idle model)
  - Active Period: 60s, λ=0.2 tasks/s
  - Idle Period: 30s, λ=0
```

---

## 3. Políticas de Orquestração Testadas

### 3.1 NEAREST_GPU (Baseline)
- Minimiza latência de rede para streams em tempo real

### 3.2 BANDWIDTH_AWARE_GPU
- **Critério**: Considera largura de banda de rede disponível
- **Objetivo**: Evitar congestionamento de rede
- **Hipótese**: Largura de banda é gargalo para vídeo

### 3.3 ENCODER_AWARE_GPU
- **Critério**: Prioriza GPUs com encoders de hardware disponíveis
- **Objetivo**: Maximizar throughput de transcoding
- **Hipótese**: Encoders de hardware são críticos

### 3.4 ADAPTIVE_GPU
- **Critério**: Ajusta qualidade de vídeo dinamicamente baseado em carga
- **Objetivo**: Manter QoS degradando qualidade quando necessário
- **Hipótese**: Adaptação é melhor que rejeição de tarefas

---

## 4. Métricas de Avaliação

### 4.1 Métricas de QoS para Vídeo

| Métrica | Fórmula | Objetivo |
|---------|---------|----------|
| **Frame Rate** | $FPS = \frac{N_{frames}}{T_{duration}}$ | ≥ 30 fps |
| **Frame Drop Rate** | $FDR = \frac{N_{dropped}}{N_{total}} \times 100\%$ | < 5% |
| **End-to-End Latency** | $L_{e2e} = L_{network} + L_{GPU} + L_{encode}$ | < 1s |
| **Bitrate Stability** | $\sigma_{bitrate}$ | Baixa variância |
| **Video Quality (PSNR)** | $PSNR = 10 \log_{10}\frac{MAX^2}{MSE}$ | > 30 dB |

### 4.2 Métricas de Throughput

| Métrica | Objetivo |
|---------|----------|
| **Concurrent Streams** | Maximizar |
| **Transcode Throughput** | Streams/segundo |
| **Data Throughput** | MB/s processados |

### 4.3 Métricas de Rede

| Métrica | Objetivo |
|---------|----------|
| **Network Congestion** | Minimizar |
| **Packet Loss Rate** | < 1% |
| **Jitter** | < 50ms |

---

## 5. Metodologia de Validação

### 5.1 Experimentos

```yaml
Experiment Design:
  Type: Performance Stress Testing
  
  Factors:
    - Device Count: [500, 1000, 1500, 2000, 2500]
    - Video Resolution: [720p, 1080p, 4K]
    - Burst Intensity: [Low, Medium, High]
  
  Policies: [NEAREST_GPU, BANDWIDTH_AWARE, ENCODER_AWARE, ADAPTIVE]
  
  Replications: 10
  Simulation Time: 600s
```

### 5.2 Carga de Trabalho

```python
# Modelo de chegada de tarefas bursty
class BurstyVideoLoad:
    def __init__(self):
        self.active_duration = 60  # seconds
        self.idle_duration = 30    # seconds
        self.lambda_active = 0.2   # tasks/s during active
        self.lambda_idle = 0.0     # no tasks during idle
    
    def generate_arrivals(self):
        # Alterna entre períodos ativos e inativos
        # Simula comportamento de streaming real
        pass
```

---

## 6. Resultados Esperados

### 6.1 Hipóteses Científicas

**H1:** *BANDWIDTH_AWARE_GPU reduz congestionamento de rede em > 30%*  
**Justificativa:** Considerar largura de banda evita sobrecarga de links.

**H2:** *ENCODER_AWARE_GPU aumenta throughput de transcoding em > 40%*  
**Justificativa:** Encoders de hardware são 10x mais rápidos que software.

**H3:** *ADAPTIVE_GPU mantém QoS sob carga alta com < 10% degradação*  
**Justificativa:** Adaptação de qualidade é melhor que rejeição de tarefas.

**H4:** *Frame drop rate < 5% para 95% das streams com ADAPTIVE_GPU*  
**Justificativa:** Adaptação dinâmica previne drops catastróficos.

### 6.2 Análise de Escalabilidade

```yaml
Scalability Analysis:
  - Maximum Concurrent Streams vs Device Count
  - Throughput Saturation Point
  - QoS Degradation Under Load
  - Resource Utilization Efficiency
```

---

## 7. Configuração para Execução

### 7.1 Comando de Execução

```bash
#!/bin/bash
# run_scenario2.sh

java -Xmx6G -cp .:lib/* \
  edu.boun.edgecloudsim.applications.gpusim.GpuSimulationMain \
  10 1500 BANDWIDTH_AWARE_GPU VIDEO_PROCESSING_SCENARIO
```

---

## 8. Publicação Científica

### 8.1 Contribuições

1. **Modelo realista de workload de vídeo** com padrões bursty
2. **Política BANDWIDTH_AWARE_GPU** para evitar congestionamento
3. **Política ADAPTIVE_GPU** com degradação graceful de qualidade
4. **Análise de escalabilidade** de video processing no edge

### 8.2 Conferências Alvo

- **ACM Multimedia** (Multimedia Systems)
- **IEEE ICME** (Multimedia & Expo)
- **ACM MMSys** (Multimedia Systems)
- **IEEE INFOCOM** (Networking)

---

**Validação Científica:** Cenário baseado em workloads reais de plataformas de streaming e video conferencing, com métricas de QoS específicas para vídeo.
