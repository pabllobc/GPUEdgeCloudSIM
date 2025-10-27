#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Análise Completa e Validação Científica dos Resultados do GpuEdgeCloudSim v1.0
Autor: Pabllo Borges Cardoso
Data: Outubro 2025
"""

import pandas as pd
import numpy as np
import json
import matplotlib.pyplot as plt
import seaborn as sns
from pathlib import Path
import warnings
warnings.filterwarnings('ignore')

# Configurar estilo dos gráficos
plt.style.use('seaborn-v0_8-darkgrid')
sns.set_palette("husl")
plt.rcParams['figure.figsize'] = (14, 8)
plt.rcParams['font.size'] = 11
plt.rcParams['axes.titlesize'] = 14
plt.rcParams['axes.labelsize'] = 12
plt.rcParams['xtick.labelsize'] = 10
plt.rcParams['ytick.labelsize'] = 10
plt.rcParams['legend.fontsize'] = 10

# Caminhos
RESULTS_DIR = Path('/home/ubuntu/sim_results')
GRAPHS_DIR = Path('/home/ubuntu/graficos_gpuedgecloudsim')
GRAPHS_DIR.mkdir(parents=True, exist_ok=True)

# Desabilitar truncamento do pandas
pd.set_option('display.max_columns', None)
pd.set_option('display.width', None)
pd.set_option('display.max_colwidth', None)

print("="*80)
print("ANÁLISE CIENTÍFICA COMPLETA - GpuEdgeCloudSim v1.0")
print("="*80)
print()

# ============================================================================
# 1. CARREGAR DADOS
# ============================================================================
print("1. CARREGANDO DADOS DA SIMULAÇÃO...")
print("-"*80)

# Carregar JSON
with open(RESULTS_DIR / 'gpuedgecloudsim_results_20251026_193924.json', 'r') as f:
    data_json = json.load(f)

# Carregar CSV
df = pd.read_csv(RESULTS_DIR / 'gpuedgecloudsim_comparison_20251026_193924.csv', index_col=0)

# Renomear cenários para facilitar visualização
cenarios_map = {
    'scenario1_baseline': 'Cenário 1: CPU-Only (Baseline)',
    'scenario2_gpu_basic': 'Cenário 2: GPU FIFO',
    'scenario3_hybrid': 'Cenário 3: Híbrido CPU+GPU',
    'scenario4_stress': 'Cenário 4: Stress Test (Alta Carga)'
}

df.index = [cenarios_map.get(idx, idx) for idx in df.index]

print("✓ Dados carregados com sucesso!")
print(f"✓ Total de cenários analisados: {len(df)}")
print(f"✓ Total de métricas coletadas: {len(df.columns)}")
print()

# ============================================================================
# 2. ESTATÍSTICAS DESCRITIVAS
# ============================================================================
print("2. ESTATÍSTICAS DESCRITIVAS")
print("-"*80)
print(df.to_string())
print()

# ============================================================================
# 3. ANÁLISE DE MELHORIAS PERCENTUAIS (vs Baseline)
# ============================================================================
print("3. ANÁLISE DE MELHORIAS PERCENTUAIS (vs Baseline)")
print("-"*80)

baseline = df.iloc[0]
melhorias = pd.DataFrame(index=df.index[1:])

# Métricas onde MENOR é MELHOR
metricas_menor_melhor = ['avg_latency', 'median_latency', 'std_latency', 
                           'p95_latency', 'p99_latency', 'total_energy', 
                           'avg_energy_per_task', 'tasks_failed']

# Métricas onde MAIOR é MELHOR
metricas_maior_melhor = ['avg_gpu_util', 'tasks_completed', 'success_rate', 'throughput']

for col in df.columns:
    if col in metricas_menor_melhor:
        # Redução percentual (negativo = melhoria)
        melhorias[f'Δ {col}'] = ((df[col][1:] - baseline[col]) / baseline[col] * 100)
    elif col in metricas_maior_melhor and col != 'avg_cpu_util':
        # Aumento percentual (positivo = melhoria)
        melhorias[f'Δ {col}'] = ((df[col][1:] - baseline[col]) / baseline[col] * 100)

print(melhorias.to_string())
print()

# ============================================================================
# 4. ANÁLISE ESTATÍSTICA DETALHADA
# ============================================================================
print("4. ANÁLISE ESTATÍSTICA DETALHADA")
print("-"*80)

# 4.1 Latência
print("\n4.1 ANÁLISE DE LATÊNCIA")
print("-"*40)
latencia_stats = df[['avg_latency', 'median_latency', 'std_latency', 'p95_latency', 'p99_latency']]
print(latencia_stats.to_string())
print()
print("📊 Insights de Latência:")
melhor_latencia_media = df['avg_latency'].idxmin()
melhor_latencia_p99 = df['p99_latency'].idxmin()
print(f"  • Melhor latência média: {melhor_latencia_media} ({df.loc[melhor_latencia_media, 'avg_latency']:.4f} ms)")
print(f"  • Melhor latência P99: {melhor_latencia_p99} ({df.loc[melhor_latencia_p99, 'p99_latency']:.4f} ms)")
print(f"  • Redução média vs Baseline: {((baseline['avg_latency'] - df['avg_latency'].iloc[1:].mean()) / baseline['avg_latency'] * 100):.2f}%")

# 4.2 Throughput e Taxa de Sucesso
print("\n4.2 ANÁLISE DE THROUGHPUT E TAXA DE SUCESSO")
print("-"*40)
throughput_stats = df[['tasks_completed', 'tasks_failed', 'success_rate', 'throughput']]
print(throughput_stats.to_string())
print()
print("📊 Insights de Throughput:")
melhor_throughput = df['throughput'].idxmax()
melhor_taxa_sucesso = df['success_rate'].idxmax()
print(f"  • Melhor throughput: {melhor_throughput} ({df.loc[melhor_throughput, 'throughput']:.2f} tasks/s)")
print(f"  • Melhor taxa de sucesso: {melhor_taxa_sucesso} ({df.loc[melhor_taxa_sucesso, 'success_rate']:.2f}%)")

# 4.3 Utilização de Recursos
print("\n4.3 ANÁLISE DE UTILIZAÇÃO DE RECURSOS (CPU/GPU)")
print("-"*40)
util_stats = df[['avg_cpu_util', 'avg_gpu_util']]
print(util_stats.to_string())
print()
print("📊 Insights de Utilização:")
for idx in df.index:
    cpu = df.loc[idx, 'avg_cpu_util']
    gpu = df.loc[idx, 'avg_gpu_util']
    total = cpu + gpu
    print(f"  • {idx}:")
    print(f"      - CPU: {cpu:.2f}%, GPU: {gpu:.2f}%")
    if gpu > 0:
        print(f"      - Balanceamento: {(gpu/(cpu+gpu)*100):.1f}% GPU / {(cpu/(cpu+gpu)*100):.1f}% CPU")

# 4.4 Eficiência Energética
print("\n4.4 ANÁLISE DE EFICIÊNCIA ENERGÉTICA")
print("-"*40)
energia_stats = df[['total_energy', 'avg_energy_per_task']]
print(energia_stats.to_string())
print()
print("📊 Insights de Energia:")
melhor_energia = df['avg_energy_per_task'].idxmin()
print(f"  • Melhor eficiência energética: {melhor_energia} ({df.loc[melhor_energia, 'avg_energy_per_task']:.2f} J/task)")
reducao_energia = (baseline['avg_energy_per_task'] - df['avg_energy_per_task'].iloc[1]) / baseline['avg_energy_per_task'] * 100
print(f"  • Redução energética (GPU FIFO vs Baseline): {reducao_energia:.2f}%")

# ============================================================================
# 5. ANÁLISE DE TRADE-OFFS
# ============================================================================
print("\n5. ANÁLISE DE TRADE-OFFS")
print("-"*80)

print("\n5.1 LATÊNCIA vs ENERGIA")
df['eficiencia'] = df['avg_energy_per_task'] / df['avg_latency']
print(df[['avg_latency', 'avg_energy_per_task', 'eficiencia']].to_string())

print("\n5.2 THROUGHPUT vs TAXA DE SUCESSO")
print(df[['throughput', 'success_rate']].to_string())

# ============================================================================
# 6. GERAR GRÁFICOS COMPARATIVOS
# ============================================================================
print("\n6. GERANDO GRÁFICOS COMPARATIVOS PROFISSIONAIS...")
print("-"*80)

# 6.1 Comparação de Latência (Todas as métricas)
fig, axes = plt.subplots(2, 3, figsize=(18, 10))
fig.suptitle('Análise Comparativa de Latência - GpuEdgeCloudSim v1.0', fontsize=16, fontweight='bold')

metricas_latencia = ['avg_latency', 'median_latency', 'std_latency', 'p95_latency', 'p99_latency']
titulos = ['Latência Média (ms)', 'Latência Mediana (ms)', 'Desvio Padrão (ms)', 
           'Latência P95 (ms)', 'Latência P99 (ms)']

for i, (metrica, titulo) in enumerate(zip(metricas_latencia, titulos)):
    ax = axes[i//3, i%3]
    valores = df[metrica]
    cores = ['#d62728' if idx == df.index[0] else '#2ca02c' if 'GPU' in idx else '#1f77b4' for idx in df.index]
    bars = ax.bar(range(len(valores)), valores, color=cores, alpha=0.8, edgecolor='black', linewidth=1.5)
    ax.set_xticks(range(len(valores)))
    ax.set_xticklabels([f'C{i+1}' for i in range(len(valores))], rotation=0)
    ax.set_title(titulo, fontweight='bold')
    ax.set_ylabel('Tempo (ms)')
    ax.grid(axis='y', alpha=0.3)
    
    # Adicionar valores nas barras
    for j, (bar, val) in enumerate(zip(bars, valores)):
        height = bar.get_height()
        ax.text(bar.get_x() + bar.get_width()/2., height,
                f'{val:.2f}',
                ha='center', va='bottom', fontsize=9, fontweight='bold')

# Remover subplot extra
axes[1, 2].axis('off')

# Adicionar legenda
legend_elements = [
    plt.Rectangle((0,0),1,1, fc='#d62728', alpha=0.8, edgecolor='black', label='Baseline CPU'),
    plt.Rectangle((0,0),1,1, fc='#2ca02c', alpha=0.8, edgecolor='black', label='GPU/Híbrido'),
    plt.Rectangle((0,0),1,1, fc='#1f77b4', alpha=0.8, edgecolor='black', label='Stress Test')
]
axes[1, 2].legend(handles=legend_elements, loc='center', fontsize=12)

plt.tight_layout()
plt.savefig(GRAPHS_DIR / '01_comparacao_latencia_completa.png', dpi=300, bbox_inches='tight')
print("✓ Gráfico 1: Comparação de Latência salvo")
plt.close()

# 6.2 Box Plot de Latências Principais
fig, ax = plt.subplots(figsize=(14, 8))
data_boxplot = df[['avg_latency', 'median_latency', 'p95_latency', 'p99_latency']].T
colors = ['#d62728', '#2ca02c', '#ff7f0e', '#1f77b4']
bp = ax.boxplot([df['avg_latency'], df['median_latency'], df['p95_latency'], df['p99_latency']],
                 labels=['Latência Média', 'Mediana', 'P95', 'P99'],
                 patch_artist=True, notch=True, showmeans=True)

for patch, color in zip(bp['boxes'], colors):
    patch.set_facecolor(color)
    patch.set_alpha(0.7)

ax.set_title('Distribuição de Latências - Todos os Cenários', fontsize=16, fontweight='bold')
ax.set_ylabel('Latência (ms)', fontsize=12)
ax.grid(axis='y', alpha=0.3)
plt.tight_layout()
plt.savefig(GRAPHS_DIR / '02_boxplot_latencias.png', dpi=300, bbox_inches='tight')
print("✓ Gráfico 2: Box Plot de Latências salvo")
plt.close()

# 6.3 Comparação de Throughput e Taxa de Sucesso
fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 7))
fig.suptitle('Análise de Throughput e Taxa de Sucesso', fontsize=16, fontweight='bold')

# Throughput
cores = ['#d62728', '#2ca02c', '#ff7f0e', '#1f77b4']
bars1 = ax1.bar(range(len(df)), df['throughput'], color=cores, alpha=0.8, edgecolor='black', linewidth=1.5)
ax1.set_xticks(range(len(df)))
ax1.set_xticklabels([f'Cenário {i+1}' for i in range(len(df))], rotation=45, ha='right')
ax1.set_ylabel('Throughput (tasks/s)', fontsize=12)
ax1.set_title('Throughput por Cenário', fontweight='bold')
ax1.grid(axis='y', alpha=0.3)

for bar, val in zip(bars1, df['throughput']):
    height = bar.get_height()
    ax1.text(bar.get_x() + bar.get_width()/2., height,
            f'{val:.1f}',
            ha='center', va='bottom', fontsize=10, fontweight='bold')

# Taxa de Sucesso
bars2 = ax2.bar(range(len(df)), df['success_rate'], color=cores, alpha=0.8, edgecolor='black', linewidth=1.5)
ax2.set_xticks(range(len(df)))
ax2.set_xticklabels([f'Cenário {i+1}' for i in range(len(df))], rotation=45, ha='right')
ax2.set_ylabel('Taxa de Sucesso (%)', fontsize=12)
ax2.set_title('Taxa de Sucesso por Cenário', fontweight='bold')
ax2.set_ylim([94, 98])
ax2.grid(axis='y', alpha=0.3)

for bar, val in zip(bars2, df['success_rate']):
    height = bar.get_height()
    ax2.text(bar.get_x() + bar.get_width()/2., height,
            f'{val:.2f}%',
            ha='center', va='bottom', fontsize=10, fontweight='bold')

plt.tight_layout()
plt.savefig(GRAPHS_DIR / '03_throughput_e_sucesso.png', dpi=300, bbox_inches='tight')
print("✓ Gráfico 3: Throughput e Taxa de Sucesso salvo")
plt.close()

# 6.4 Utilização de Recursos (CPU vs GPU)
fig, ax = plt.subplots(figsize=(14, 8))
x = np.arange(len(df))
width = 0.35

bars1 = ax.bar(x - width/2, df['avg_cpu_util'], width, label='CPU', 
               color='#1f77b4', alpha=0.8, edgecolor='black', linewidth=1.5)
bars2 = ax.bar(x + width/2, df['avg_gpu_util'], width, label='GPU', 
               color='#2ca02c', alpha=0.8, edgecolor='black', linewidth=1.5)

ax.set_xlabel('Cenários', fontsize=12)
ax.set_ylabel('Utilização (%)', fontsize=12)
ax.set_title('Utilização de Recursos CPU vs GPU por Cenário', fontsize=16, fontweight='bold')
ax.set_xticks(x)
ax.set_xticklabels([f'Cenário {i+1}' for i in range(len(df))], rotation=45, ha='right')
ax.legend(fontsize=12)
ax.grid(axis='y', alpha=0.3)

# Adicionar valores nas barras
for bars in [bars1, bars2]:
    for bar in bars:
        height = bar.get_height()
        if height > 0:
            ax.text(bar.get_x() + bar.get_width()/2., height,
                   f'{height:.1f}%',
                   ha='center', va='bottom', fontsize=9, fontweight='bold')

plt.tight_layout()
plt.savefig(GRAPHS_DIR / '04_utilizacao_recursos.png', dpi=300, bbox_inches='tight')
print("✓ Gráfico 4: Utilização de Recursos salvo")
plt.close()

# 6.5 Eficiência Energética
fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 7))
fig.suptitle('Análise de Eficiência Energética', fontsize=16, fontweight='bold')

# Energia por Tarefa
cores = ['#d62728', '#2ca02c', '#ff7f0e', '#1f77b4']
bars1 = ax1.bar(range(len(df)), df['avg_energy_per_task'], color=cores, alpha=0.8, edgecolor='black', linewidth=1.5)
ax1.set_xticks(range(len(df)))
ax1.set_xticklabels([f'Cenário {i+1}' for i in range(len(df))], rotation=45, ha='right')
ax1.set_ylabel('Energia por Tarefa (J)', fontsize=12)
ax1.set_title('Energia Consumida por Tarefa', fontweight='bold')
ax1.grid(axis='y', alpha=0.3)

for bar, val in zip(bars1, df['avg_energy_per_task']):
    height = bar.get_height()
    ax1.text(bar.get_x() + bar.get_width()/2., height,
            f'{val:.2f} J',
            ha='center', va='bottom', fontsize=10, fontweight='bold')

# Energia Total
bars2 = ax2.bar(range(len(df)), df['total_energy']/1000, color=cores, alpha=0.8, edgecolor='black', linewidth=1.5)
ax2.set_xticks(range(len(df)))
ax2.set_xticklabels([f'Cenário {i+1}' for i in range(len(df))], rotation=45, ha='right')
ax2.set_ylabel('Energia Total (kJ)', fontsize=12)
ax2.set_title('Energia Total Consumida', fontweight='bold')
ax2.grid(axis='y', alpha=0.3)

for bar, val in zip(bars2, df['total_energy']/1000):
    height = bar.get_height()
    ax2.text(bar.get_x() + bar.get_width()/2., height,
            f'{val:.1f} kJ',
            ha='center', va='bottom', fontsize=10, fontweight='bold')

plt.tight_layout()
plt.savefig(GRAPHS_DIR / '05_eficiencia_energetica.png', dpi=300, bbox_inches='tight')
print("✓ Gráfico 5: Eficiência Energética salvo")
plt.close()

# 6.6 Tarefas Completadas vs Falhadas
fig, ax = plt.subplots(figsize=(14, 8))
x = np.arange(len(df))
width = 0.35

bars1 = ax.bar(x - width/2, df['tasks_completed'], width, label='Completadas', 
               color='#2ca02c', alpha=0.8, edgecolor='black', linewidth=1.5)
bars2 = ax.bar(x + width/2, df['tasks_failed'], width, label='Falhadas', 
               color='#d62728', alpha=0.8, edgecolor='black', linewidth=1.5)

ax.set_xlabel('Cenários', fontsize=12)
ax.set_ylabel('Número de Tarefas', fontsize=12)
ax.set_title('Tarefas Completadas vs Falhadas por Cenário', fontsize=16, fontweight='bold')
ax.set_xticks(x)
ax.set_xticklabels([f'Cenário {i+1}' for i in range(len(df))], rotation=45, ha='right')
ax.legend(fontsize=12)
ax.grid(axis='y', alpha=0.3)

# Adicionar valores nas barras
for bar in bars1:
    height = bar.get_height()
    ax.text(bar.get_x() + bar.get_width()/2., height,
           f'{int(height)}',
           ha='center', va='bottom', fontsize=9, fontweight='bold')

for bar in bars2:
    height = bar.get_height()
    ax.text(bar.get_x() + bar.get_width()/2., height,
           f'{int(height)}',
           ha='center', va='bottom', fontsize=9, fontweight='bold')

plt.tight_layout()
plt.savefig(GRAPHS_DIR / '06_tarefas_completadas_vs_falhadas.png', dpi=300, bbox_inches='tight')
print("✓ Gráfico 6: Tarefas Completadas vs Falhadas salvo")
plt.close()

# 6.7 Radar Chart - Comparação Multi-dimensional
from math import pi

fig, ax = plt.subplots(figsize=(12, 10), subplot_kw=dict(projection='polar'))

# Normalizar métricas para escala 0-100
metricas_radar = ['avg_latency', 'throughput', 'success_rate', 'avg_energy_per_task', 'avg_cpu_util', 'avg_gpu_util']
labels = ['Latência\n(invertido)', 'Throughput', 'Taxa Sucesso', 'Energia\n(invertido)', 'CPU Util', 'GPU Util']

# Normalizar dados (0-100)
df_norm = df.copy()
df_norm['avg_latency'] = 100 - (df['avg_latency'] / df['avg_latency'].max() * 100)  # Inverter (menor é melhor)
df_norm['throughput'] = df['throughput'] / df['throughput'].max() * 100
df_norm['success_rate'] = df['success_rate']  # Já está em %
df_norm['avg_energy_per_task'] = 100 - (df['avg_energy_per_task'] / df['avg_energy_per_task'].max() * 100)  # Inverter
df_norm['avg_cpu_util'] = df['avg_cpu_util']  # Já está em %
df_norm['avg_gpu_util'] = df['avg_gpu_util']  # Já está em %

# Configurar ângulos
angles = [n / float(len(metricas_radar)) * 2 * pi for n in range(len(metricas_radar))]
angles += angles[:1]

# Plotar cada cenário
cores_radar = ['#d62728', '#2ca02c', '#ff7f0e', '#1f77b4']
for idx, (cenario, cor) in enumerate(zip(df_norm.index, cores_radar)):
    values = df_norm.loc[cenario, metricas_radar].values.tolist()
    values += values[:1]
    ax.plot(angles, values, 'o-', linewidth=2, label=f'Cenário {idx+1}', color=cor)
    ax.fill(angles, values, alpha=0.15, color=cor)

ax.set_xticks(angles[:-1])
ax.set_xticklabels(labels, fontsize=11)
ax.set_ylim(0, 100)
ax.set_title('Comparação Multi-dimensional dos Cenários\n(Escala Normalizada 0-100)', 
             fontsize=16, fontweight='bold', pad=20)
ax.legend(loc='upper right', bbox_to_anchor=(1.3, 1.1), fontsize=11)
ax.grid(True)

plt.tight_layout()
plt.savefig(GRAPHS_DIR / '07_radar_chart_comparacao.png', dpi=300, bbox_inches='tight')
print("✓ Gráfico 7: Radar Chart Multi-dimensional salvo")
plt.close()

# 6.8 Heatmap de Melhorias Percentuais
fig, ax = plt.subplots(figsize=(14, 8))

# Criar matriz de melhorias
melhorias_matrix = melhorias.copy()
melhorias_matrix.columns = [col.replace('Δ ', '') for col in melhorias_matrix.columns]

# Selecionar métricas principais
metricas_principais = ['avg_latency', 'p99_latency', 'avg_energy_per_task', 'throughput', 'success_rate']
melhorias_plot = melhorias_matrix[[col for col in melhorias_matrix.columns if any(m in col for m in metricas_principais)]]

# Criar labels mais legíveis
labels_map = {
    'avg_latency': 'Latência Média',
    'p99_latency': 'Latência P99',
    'avg_energy_per_task': 'Energia/Tarefa',
    'throughput': 'Throughput',
    'success_rate': 'Taxa Sucesso'
}
melhorias_plot.columns = [labels_map.get(col, col) for col in melhorias_plot.columns]

# Plotar heatmap
sns.heatmap(melhorias_plot, annot=True, fmt='.2f', cmap='RdYlGn', center=0, 
            cbar_kws={'label': 'Melhoria vs Baseline (%)'}, ax=ax, linewidths=0.5)
ax.set_title('Melhorias Percentuais vs Baseline (CPU-Only)\n(Verde = Melhor | Vermelho = Pior)', 
             fontsize=16, fontweight='bold')
ax.set_ylabel('Cenários', fontsize=12)
ax.set_xlabel('Métricas', fontsize=12)
ax.set_yticklabels([f'Cenário {i+2}' for i in range(len(melhorias_plot))], rotation=0)

plt.tight_layout()
plt.savefig(GRAPHS_DIR / '08_heatmap_melhorias.png', dpi=300, bbox_inches='tight')
print("✓ Gráfico 8: Heatmap de Melhorias salvo")
plt.close()

# 6.9 Gráfico de Trade-off: Latência vs Energia
fig, ax = plt.subplots(figsize=(12, 8))

cores = ['#d62728', '#2ca02c', '#ff7f0e', '#1f77b4']
markers = ['o', 's', '^', 'D']

for idx, (cenario, cor, marker) in enumerate(zip(df.index, cores, markers)):
    ax.scatter(df.loc[cenario, 'avg_latency'], df.loc[cenario, 'avg_energy_per_task'],
               s=300, c=cor, marker=marker, alpha=0.7, edgecolor='black', linewidth=2,
               label=f'Cenário {idx+1}')
    
    # Adicionar anotações
    ax.annotate(f'C{idx+1}', 
                xy=(df.loc[cenario, 'avg_latency'], df.loc[cenario, 'avg_energy_per_task']),
                xytext=(10, 10), textcoords='offset points', fontsize=12, fontweight='bold')

ax.set_xlabel('Latência Média (ms)', fontsize=14)
ax.set_ylabel('Energia por Tarefa (J)', fontsize=14)
ax.set_title('Trade-off: Latência vs Eficiência Energética\n(Ideal = Canto Inferior Esquerdo)', 
             fontsize=16, fontweight='bold')
ax.legend(fontsize=11, loc='best')
ax.grid(True, alpha=0.3)

# Destacar região ideal
ax.axhline(y=15, color='green', linestyle='--', alpha=0.3, linewidth=2, label='Meta Energia')
ax.axvline(x=0.2, color='blue', linestyle='--', alpha=0.3, linewidth=2, label='Meta Latência')

plt.tight_layout()
plt.savefig(GRAPHS_DIR / '09_tradeoff_latencia_energia.png', dpi=300, bbox_inches='tight')
print("✓ Gráfico 9: Trade-off Latência vs Energia salvo")
plt.close()

# 6.10 Comparação Final - Dashboard Consolidado
fig = plt.figure(figsize=(20, 12))
gs = fig.add_gridspec(3, 3, hspace=0.3, wspace=0.3)

# Subplot 1: Latência Média
ax1 = fig.add_subplot(gs[0, 0])
bars = ax1.bar(range(len(df)), df['avg_latency'], color=cores, alpha=0.8, edgecolor='black')
ax1.set_title('Latência Média', fontweight='bold')
ax1.set_ylabel('ms')
ax1.set_xticks(range(len(df)))
ax1.set_xticklabels([f'C{i+1}' for i in range(len(df))])
ax1.grid(axis='y', alpha=0.3)

# Subplot 2: Throughput
ax2 = fig.add_subplot(gs[0, 1])
bars = ax2.bar(range(len(df)), df['throughput'], color=cores, alpha=0.8, edgecolor='black')
ax2.set_title('Throughput', fontweight='bold')
ax2.set_ylabel('tasks/s')
ax2.set_xticks(range(len(df)))
ax2.set_xticklabels([f'C{i+1}' for i in range(len(df))])
ax2.grid(axis='y', alpha=0.3)

# Subplot 3: Taxa de Sucesso
ax3 = fig.add_subplot(gs[0, 2])
bars = ax3.bar(range(len(df)), df['success_rate'], color=cores, alpha=0.8, edgecolor='black')
ax3.set_title('Taxa de Sucesso', fontweight='bold')
ax3.set_ylabel('%')
ax3.set_xticks(range(len(df)))
ax3.set_xticklabels([f'C{i+1}' for i in range(len(df))])
ax3.set_ylim([94, 98])
ax3.grid(axis='y', alpha=0.3)

# Subplot 4: Energia por Tarefa
ax4 = fig.add_subplot(gs[1, 0])
bars = ax4.bar(range(len(df)), df['avg_energy_per_task'], color=cores, alpha=0.8, edgecolor='black')
ax4.set_title('Energia por Tarefa', fontweight='bold')
ax4.set_ylabel('J')
ax4.set_xticks(range(len(df)))
ax4.set_xticklabels([f'C{i+1}' for i in range(len(df))])
ax4.grid(axis='y', alpha=0.3)

# Subplot 5: Utilização CPU
ax5 = fig.add_subplot(gs[1, 1])
bars = ax5.bar(range(len(df)), df['avg_cpu_util'], color=cores, alpha=0.8, edgecolor='black')
ax5.set_title('Utilização CPU', fontweight='bold')
ax5.set_ylabel('%')
ax5.set_xticks(range(len(df)))
ax5.set_xticklabels([f'C{i+1}' for i in range(len(df))])
ax5.grid(axis='y', alpha=0.3)

# Subplot 6: Utilização GPU
ax6 = fig.add_subplot(gs[1, 2])
bars = ax6.bar(range(len(df)), df['avg_gpu_util'], color=cores, alpha=0.8, edgecolor='black')
ax6.set_title('Utilização GPU', fontweight='bold')
ax6.set_ylabel('%')
ax6.set_xticks(range(len(df)))
ax6.set_xticklabels([f'C{i+1}' for i in range(len(df))])
ax6.grid(axis='y', alpha=0.3)

# Subplot 7: Latência P99
ax7 = fig.add_subplot(gs[2, 0])
bars = ax7.bar(range(len(df)), df['p99_latency'], color=cores, alpha=0.8, edgecolor='black')
ax7.set_title('Latência P99', fontweight='bold')
ax7.set_ylabel('ms')
ax7.set_xticks(range(len(df)))
ax7.set_xticklabels([f'C{i+1}' for i in range(len(df))])
ax7.grid(axis='y', alpha=0.3)

# Subplot 8: Tarefas Completadas
ax8 = fig.add_subplot(gs[2, 1])
bars = ax8.bar(range(len(df)), df['tasks_completed'], color=cores, alpha=0.8, edgecolor='black')
ax8.set_title('Tarefas Completadas', fontweight='bold')
ax8.set_ylabel('Quantidade')
ax8.set_xticks(range(len(df)))
ax8.set_xticklabels([f'C{i+1}' for i in range(len(df))])
ax8.grid(axis='y', alpha=0.3)

# Subplot 9: Tarefas Falhadas
ax9 = fig.add_subplot(gs[2, 2])
bars = ax9.bar(range(len(df)), df['tasks_failed'], color=cores, alpha=0.8, edgecolor='black')
ax9.set_title('Tarefas Falhadas', fontweight='bold')
ax9.set_ylabel('Quantidade')
ax9.set_xticks(range(len(df)))
ax9.set_xticklabels([f'C{i+1}' for i in range(len(df))])
ax9.grid(axis='y', alpha=0.3)

fig.suptitle('Dashboard Consolidado - GpuEdgeCloudSim v1.0\nComparação de Todos os Cenários', 
             fontsize=18, fontweight='bold', y=0.995)

plt.savefig(GRAPHS_DIR / '10_dashboard_consolidado.png', dpi=300, bbox_inches='tight')
print("✓ Gráfico 10: Dashboard Consolidado salvo")
plt.close()

print()
print("✓ Todos os gráficos foram salvos em:", GRAPHS_DIR)
print()

# ============================================================================
# 7. ANÁLISE DE SIGNIFICÂNCIA ESTATÍSTICA
# ============================================================================
print("7. ANÁLISE DE SIGNIFICÂNCIA E VALIDAÇÃO CIENTÍFICA")
print("-"*80)

# 7.1 Calcular Coeficiente de Variação (CV)
print("\n7.1 COEFICIENTE DE VARIAÇÃO (CV = std/mean)")
print("    (Valores baixos indicam consistência)")
print("-"*40)
cv_latencia = (df['std_latency'] / df['avg_latency'] * 100)
for idx, cv in cv_latencia.items():
    print(f"  • {idx}: CV = {cv:.2f}%")

# 7.2 Análise de Eficiência Global
print("\n7.2 ANÁLISE DE EFICIÊNCIA GLOBAL")
print("-"*40)
# Score ponderado: menor latência + maior throughput + menor energia
df['score_eficiencia'] = (
    (1 / df['avg_latency']) * 0.4 +  # 40% peso para latência
    (df['throughput'] / 100) * 0.3 +  # 30% peso para throughput
    (1 / df['avg_energy_per_task']) * 0.3  # 30% peso para energia
)
df_sorted = df.sort_values('score_eficiencia', ascending=False)
print("\nRanking de Eficiência Global (Score Ponderado):")
for i, idx in enumerate(df_sorted.index, 1):
    print(f"  {i}º. {idx}: Score = {df_sorted.loc[idx, 'score_eficiencia']:.4f}")

print()
print("="*80)
print("ANÁLISE CONCLUÍDA COM SUCESSO!")
print("="*80)
print()
print(f"📊 Total de gráficos gerados: 10")
print(f"📁 Diretório de gráficos: {GRAPHS_DIR}")
print(f"📄 Próximo passo: Gerar relatório consolidado em Markdown")
print()
