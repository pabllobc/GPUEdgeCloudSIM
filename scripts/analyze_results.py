#!/usr/bin/env python3
"""
GpuEdgeCloudSim - Análise e Visualização de Resultados
Autor: Pabllo Borges Cardoso
Data: 26 de Outubro de 2025
"""

import json
import pandas as pd
import numpy as np
import matplotlib
matplotlib.use('Agg')  # Backend sem display
import matplotlib.pyplot as plt
import seaborn as sns
from pathlib import Path

# Configuração de estilo
plt.style.use('seaborn-v0_8-darkgrid')
sns.set_palette("husl")

def load_results(results_file):
    """Carrega resultados JSON"""
    with open(results_file, 'r') as f:
        return json.load(f)

def create_comparison_table(results):
    """Cria tabela comparativa formatada"""
    df = pd.DataFrame(results).T
    df.index = ['Baseline CPU', 'GPU Básico', 'Híbrido', 'Stress Test']
    
    # Renomeia colunas
    df.columns = [
        'Latência Média (ms)', 'Latência Mediana (ms)', 'Desvio Padrão Latência',
        'P95 Latência (ms)', 'P99 Latência (ms)', 'CPU Util (%)', 'GPU Util (%)',
        'Energia Total (J)', 'Energia/Tarefa (J)', 'Tarefas OK', 'Tarefas Falhas',
        'Taxa Sucesso (%)', 'Throughput (t/s)'
    ]
    
    # Converte latências para ms
    for col in ['Latência Média (ms)', 'Latência Mediana (ms)', 'P95 Latência (ms)', 'P99 Latência (ms)']:
        df[col] = df[col] * 1000
    
    return df

def plot_latency_comparison(results, output_dir):
    """Gráfico de comparação de latência"""
    scenarios = ['Baseline CPU', 'GPU Básico', 'Híbrido', 'Stress Test']
    latencies = []
    p95s = []
    
    for key in ['scenario1_baseline', 'scenario2_gpu_basic', 'scenario3_hybrid', 'scenario4_stress']:
        latencies.append(results[key]['avg_latency'] * 1000)
        p95s.append(results[key]['p95_latency'] * 1000)
    
    fig, ax = plt.subplots(figsize=(10, 6))
    x = np.arange(len(scenarios))
    width = 0.35
    
    bars1 = ax.bar(x - width/2, latencies, width, label='Latência Média', color='steelblue')
    bars2 = ax.bar(x + width/2, p95s, width, label='P95 Latência', color='coral')
    
    ax.set_xlabel('Cenário', fontsize=12, fontweight='bold')
    ax.set_ylabel('Latência (ms)', fontsize=12, fontweight='bold')
    ax.set_title('Comparação de Latência entre Cenários', fontsize=14, fontweight='bold')
    ax.set_xticks(x)
    ax.set_xticklabels(scenarios, rotation=15, ha='right')
    ax.legend()
    ax.grid(axis='y', alpha=0.3)
    
    # Adiciona valores nas barras
    for bars in [bars1, bars2]:
        for bar in bars:
            height = bar.get_height()
            ax.text(bar.get_x() + bar.get_width()/2., height,
                   f'{height:.1f}',
                   ha='center', va='bottom', fontsize=9)
    
    plt.tight_layout()
    plt.savefig(output_dir / 'latency_comparison.png', dpi=300, bbox_inches='tight')
    plt.close()
    print("    ✓ Gráfico de latência salvo")

def plot_utilization_comparison(results, output_dir):
    """Gráfico de utilização de recursos"""
    scenarios = ['Baseline\nCPU', 'GPU\nBásico', 'Híbrido', 'Stress\nTest']
    cpu_utils = []
    gpu_utils = []
    
    for key in ['scenario1_baseline', 'scenario2_gpu_basic', 'scenario3_hybrid', 'scenario4_stress']:
        cpu_utils.append(results[key]['avg_cpu_util'])
        gpu_utils.append(results[key]['avg_gpu_util'])
    
    fig, ax = plt.subplots(figsize=(10, 6))
    x = np.arange(len(scenarios))
    width = 0.35
    
    bars1 = ax.bar(x - width/2, cpu_utils, width, label='CPU', color='#FF6B6B')
    bars2 = ax.bar(x + width/2, gpu_utils, width, label='GPU', color='#4ECDC4')
    
    ax.set_xlabel('Cenário', fontsize=12, fontweight='bold')
    ax.set_ylabel('Utilização (%)', fontsize=12, fontweight='bold')
    ax.set_title('Utilização de Recursos CPU vs GPU', fontsize=14, fontweight='bold')
    ax.set_xticks(x)
    ax.set_xticklabels(scenarios)
    ax.legend()
    ax.set_ylim(0, 100)
    ax.grid(axis='y', alpha=0.3)
    
    # Adiciona valores nas barras
    for bars in [bars1, bars2]:
        for bar in bars:
            height = bar.get_height()
            ax.text(bar.get_x() + bar.get_width()/2., height,
                   f'{height:.1f}%',
                   ha='center', va='bottom', fontsize=9)
    
    plt.tight_layout()
    plt.savefig(output_dir / 'utilization_comparison.png', dpi=300, bbox_inches='tight')
    plt.close()
    print("    ✓ Gráfico de utilização salvo")

def plot_energy_comparison(results, output_dir):
    """Gráfico de consumo energético"""
    scenarios = ['Baseline\nCPU', 'GPU\nBásico', 'Híbrido', 'Stress\nTest']
    energies = []
    energy_per_task = []
    
    for key in ['scenario1_baseline', 'scenario2_gpu_basic', 'scenario3_hybrid', 'scenario4_stress']:
        energies.append(results[key]['total_energy'] / 1000)  # Convert to kJ
        energy_per_task.append(results[key]['avg_energy_per_task'])
    
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 6))
    
    # Energia Total
    bars1 = ax1.bar(scenarios, energies, color=['#FF6B6B', '#4ECDC4', '#95E1D3', '#FFE66D'])
    ax1.set_ylabel('Energia Total (kJ)', fontsize=12, fontweight='bold')
    ax1.set_title('Energia Total Consumida', fontsize=13, fontweight='bold')
    ax1.grid(axis='y', alpha=0.3)
    
    for bar in bars1:
        height = bar.get_height()
        ax1.text(bar.get_x() + bar.get_width()/2., height,
               f'{height:.1f}',
               ha='center', va='bottom', fontsize=10)
    
    # Energia por Tarefa
    bars2 = ax2.bar(scenarios, energy_per_task, color=['#FF6B6B', '#4ECDC4', '#95E1D3', '#FFE66D'])
    ax2.set_ylabel('Energia por Tarefa (J)', fontsize=12, fontweight='bold')
    ax2.set_title('Eficiência Energética', fontsize=13, fontweight='bold')
    ax2.grid(axis='y', alpha=0.3)
    
    for bar in bars2:
        height = bar.get_height()
        ax2.text(bar.get_x() + bar.get_width()/2., height,
               f'{height:.2f}',
               ha='center', va='bottom', fontsize=10)
    
    plt.suptitle('Análise de Consumo Energético', fontsize=15, fontweight='bold', y=1.02)
    plt.tight_layout()
    plt.savefig(output_dir / 'energy_comparison.png', dpi=300, bbox_inches='tight')
    plt.close()
    print("    ✓ Gráfico de energia salvo")

def plot_throughput_comparison(results, output_dir):
    """Gráfico de throughput"""
    scenarios = ['Baseline CPU', 'GPU Básico', 'Híbrido', 'Stress Test']
    throughputs = []
    
    for key in ['scenario1_baseline', 'scenario2_gpu_basic', 'scenario3_hybrid', 'scenario4_stress']:
        throughputs.append(results[key]['throughput'])
    
    fig, ax = plt.subplots(figsize=(10, 6))
    bars = ax.bar(scenarios, throughputs, color=['#FF6B6B', '#4ECDC4', '#95E1D3', '#FFE66D'])
    
    ax.set_xlabel('Cenário', fontsize=12, fontweight='bold')
    ax.set_ylabel('Throughput (tarefas/segundo)', fontsize=12, fontweight='bold')
    ax.set_title('Throughput de Processamento', fontsize=14, fontweight='bold')
    ax.grid(axis='y', alpha=0.3)
    
    for bar in bars:
        height = bar.get_height()
        ax.text(bar.get_x() + bar.get_width()/2., height,
               f'{height:.1f}',
               ha='center', va='bottom', fontsize=10, fontweight='bold')
    
    plt.xticks(rotation=15, ha='right')
    plt.tight_layout()
    plt.savefig(output_dir / 'throughput_comparison.png', dpi=300, bbox_inches='tight')
    plt.close()
    print("    ✓ Gráfico de throughput salvo")

def plot_success_rate(results, output_dir):
    """Gráfico de taxa de sucesso"""
    scenarios = ['Baseline\nCPU', 'GPU\nBásico', 'Híbrido', 'Stress\nTest']
    success_rates = []
    
    for key in ['scenario1_baseline', 'scenario2_gpu_basic', 'scenario3_hybrid', 'scenario4_stress']:
        success_rates.append(results[key]['success_rate'])
    
    fig, ax = plt.subplots(figsize=(10, 6))
    bars = ax.bar(scenarios, success_rates, color=['#2ECC71', '#3498DB', '#9B59B6', '#E74C3C'])
    
    ax.set_ylabel('Taxa de Sucesso (%)', fontsize=12, fontweight='bold')
    ax.set_title('Confiabilidade: Taxa de Sucesso de Tarefas', fontsize=14, fontweight='bold')
    ax.set_ylim(90, 100)
    ax.grid(axis='y', alpha=0.3)
    
    for bar in bars:
        height = bar.get_height()
        ax.text(bar.get_x() + bar.get_width()/2., height,
               f'{height:.2f}%',
               ha='center', va='bottom', fontsize=10, fontweight='bold')
    
    plt.tight_layout()
    plt.savefig(output_dir / 'success_rate_comparison.png', dpi=300, bbox_inches='tight')
    plt.close()
    print("    ✓ Gráfico de taxa de sucesso salvo")

def calculate_improvements(results):
    """Calcula melhorias percentuais do GPU vs Baseline"""
    baseline = results['scenario1_baseline']
    gpu_basic = results['scenario2_gpu_basic']
    hybrid = results['scenario3_hybrid']
    
    improvements = {
        'GPU Básico vs Baseline': {
            'Redução Latência': ((baseline['avg_latency'] - gpu_basic['avg_latency']) / baseline['avg_latency']) * 100,
            'Redução Energia': ((baseline['total_energy'] - gpu_basic['total_energy']) / baseline['total_energy']) * 100,
            'Aumento GPU Util': gpu_basic['avg_gpu_util'] - baseline['avg_gpu_util'],
        },
        'Híbrido vs Baseline': {
            'Redução Latência': ((baseline['avg_latency'] - hybrid['avg_latency']) / baseline['avg_latency']) * 100,
            'Redução Energia': ((baseline['total_energy'] - hybrid['total_energy']) / baseline['total_energy']) * 100,
            'Balanceamento': f"CPU: {hybrid['avg_cpu_util']:.1f}%, GPU: {hybrid['avg_gpu_util']:.1f}%",
        }
    }
    
    return improvements

def main():
    print("\n" + "="*70)
    print("  GpuEdgeCloudSim - Análise e Visualização de Resultados")
    print("="*70 + "\n")
    
    # Localiza arquivo de resultados mais recente
    results_dir = Path('/home/ubuntu/sim_results')
    json_files = sorted(results_dir.glob('gpuedgecloudsim_results_*.json'))
    
    if not json_files:
        print("❌ Nenhum arquivo de resultados encontrado!")
        return
    
    latest_results = json_files[-1]
    print(f"  Analisando: {latest_results.name}\n")
    
    # Carrega resultados
    results = load_results(latest_results)
    
    # Cria tabela comparativa
    print("  Processando dados...")
    df = create_comparison_table(results)
    
    # Salva tabela em CSV formatado
    csv_output = results_dir / 'analysis_table.csv'
    df.to_csv(csv_output)
    print(f"  ✓ Tabela comparativa salva: {csv_output.name}\n")
    
    # Calcula melhorias
    improvements = calculate_improvements(results)
    
    # Gera gráficos
    print("  Gerando visualizações:")
    plot_latency_comparison(results, results_dir)
    plot_utilization_comparison(results, results_dir)
    plot_energy_comparison(results, results_dir)
    plot_throughput_comparison(results, results_dir)
    plot_success_rate(results, results_dir)
    
    print("\n  Melhorias GPU vs Baseline:")
    print("  " + "-"*50)
    for comparison, metrics in improvements.items():
        print(f"\n  {comparison}:")
        for metric, value in metrics.items():
            if isinstance(value, str):
                print(f"    • {metric}: {value}")
            else:
                print(f"    • {metric}: {value:+.2f}%")
    
    print("\n" + "="*70)
    print("  ✓ Análise concluída com sucesso!")
    print("="*70 + "\n")

if __name__ == "__main__":
    main()
