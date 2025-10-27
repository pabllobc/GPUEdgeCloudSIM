# 🌐 Cenário 5: Mixed Heterogeneous Workload

**Cenário:** Carga de trabalho mista representando deployment realista  
**Aplicações:** 30% ML, 25% Vídeo, 20% AR/VR, 15% Científica, 10% Imagem  
**Métricas Chave:** QoS por tipo de aplicação, Fairness de recursos, Multi-tenancy  
**Políticas Testadas:** FAIR_SHARE_GPU, SLA_AWARE_GPU, WORKLOAD_AWARE_GPU  
**Carga:** Heterogênea com diferentes requisitos de latência e recursos  
**Validação:** Análise de fairness (Jain's Index), SLA compliance, Resource efficiency  

## Objetivos Científicos:
1. Validar robustez das políticas em cenários realistas
2. Analisar interferência entre aplicações co-localizadas
3. Propor scheduling multi-objetivo para workloads heterogêneos
