#!/usr/bin/env python3
"""
GpuEdgeCloudSim - Simulador de Cenários Científicos
Autor: Pabllo Borges Cardoso
Data: 26 de Outubro de 2025
Versão: 1.0

Este simulador executa 4 cenários científicos do GpuEdgeCloudSim:
1. Cenário Baseline (CPU-only)
2. Cenário GPU Básico
3. Cenário Híbrido Inteligente
4. Cenário Stress Test
"""

import numpy as np
import pandas as pd
import json
import time
from datetime import datetime
from pathlib import Path

class GpuEdgeCloudSimulator:
    """Simulador de Edge Computing com suporte a GPU"""
    
    def __init__(self, scenario_name, num_devices, simulation_time=600):
        self.scenario_name = scenario_name
        self.num_devices = num_devices
        self.simulation_time = simulation_time
        self.results = {
            'tasks_completed': [],
            'tasks_failed': [],
            'latencies': [],
            'cpu_utilizations': [],
            'gpu_utilizations': [],
            'energy_consumption': [],
            'throughput': []
        }
        
    def simulate_task(self, task_type, use_gpu=False, hybrid_decision=False):
        """Simula a execução de uma tarefa com características específicas"""
        
        # Características base das tarefas (baseadas em applications.xml)
        task_characteristics = {
            'ML_INFERENCE': {
                'cpu_time': np.random.gamma(2.0, 0.15),  # ~0.3s média
                'gpu_time': np.random.gamma(1.5, 0.04),  # ~0.06s média (5x mais rápido)
                'data_size': 2048,  # KB
                'gpu_memory': 4096,  # MB
                'cpu_energy': 15.0,  # Joules
                'gpu_energy': 8.0    # Joules (mais eficiente)
            },
            'VIDEO_PROCESSING': {
                'cpu_time': np.random.gamma(3.0, 0.4),   # ~1.2s média
                'gpu_time': np.random.gamma(2.0, 0.08),  # ~0.16s média (7.5x mais rápido)
                'data_size': 10240,
                'gpu_memory': 8192,
                'cpu_energy': 45.0,
                'gpu_energy': 18.0
            },
            'AR_VR': {
                'cpu_time': np.random.gamma(2.5, 0.25),  # ~0.625s média
                'gpu_time': np.random.gamma(1.8, 0.06),  # ~0.108s média (5.8x mais rápido)
                'data_size': 3072,
                'gpu_memory': 6144,
                'cpu_energy': 25.0,
                'gpu_energy': 12.0
            },
            'IMAGE_PROCESSING': {
                'cpu_time': np.random.gamma(2.2, 0.2),   # ~0.44s média
                'gpu_time': np.random.gamma(1.6, 0.05),  # ~0.08s média (5.5x mais rápido)
                'data_size': 5120,
                'gpu_memory': 5120,
                'cpu_energy': 20.0,
                'gpu_energy': 10.0
            }
        }
        
        char = task_characteristics[task_type]
        
        # Calcula latência baseada no uso de GPU
        if use_gpu:
            processing_time = char['gpu_time']
            energy = char['gpu_energy']
            # Adiciona overhead de transferência PCIe (realistic overhead)
            pcie_overhead = (char['data_size'] / 1024.0) / 15.75 * 0.001  # 15.75 GB/s
            processing_time += pcie_overhead
            gpu_util = np.random.uniform(70, 95)
            cpu_util = np.random.uniform(10, 25)
        else:
            processing_time = char['cpu_time']
            energy = char['cpu_energy']
            gpu_util = 0.0
            cpu_util = np.random.uniform(75, 95)
        
        # Adiciona latência de rede (WLAN + propagation)
        network_latency = np.random.gamma(1.5, 0.005)  # ~7.5ms média
        total_latency = processing_time + network_latency
        
        # Decisão híbrida: escolhe CPU ou GPU baseado em características da tarefa
        if hybrid_decision:
            # Tarefas pequenas vão para CPU, grandes para GPU
            if char['data_size'] < 3000 and np.random.random() < 0.3:
                use_gpu = False
                processing_time = char['cpu_time']
                energy = char['cpu_energy']
                gpu_util = 0.0
                cpu_util = np.random.uniform(75, 95)
                total_latency = processing_time + network_latency
        
        # Simula falhas (taxa de falha realista ~2-5%)
        failure_probability = 0.03 if use_gpu else 0.025
        failed = np.random.random() < failure_probability
        
        return {
            'latency': total_latency,
            'cpu_utilization': cpu_util,
            'gpu_utilization': gpu_util,
            'energy': energy,
            'failed': failed,
            'used_gpu': use_gpu
        }
    
    def run_scenario_baseline(self):
        """Cenário 1: Baseline CPU-only"""
        print(f"    Executando Cenário 1: Baseline CPU-only")
        print(f"    Dispositivos: {self.num_devices}, Tempo: {self.simulation_time}s")
        
        task_types = ['ML_INFERENCE', 'VIDEO_PROCESSING', 'AR_VR', 'IMAGE_PROCESSING']
        task_weights = [0.3, 0.25, 0.25, 0.2]  # Distribuição realista
        
        # Calcula número de tarefas baseado em Poisson arrival
        arrival_rate = 0.15  # tarefas por segundo por dispositivo
        expected_tasks = int(self.num_devices * arrival_rate * self.simulation_time)
        num_tasks = np.random.poisson(expected_tasks)
        
        print(f"    Gerando {num_tasks} tarefas...")
        
        for i in range(num_tasks):
            task_type = np.random.choice(task_types, p=task_weights)
            result = self.simulate_task(task_type, use_gpu=False)
            
            if result['failed']:
                self.results['tasks_failed'].append(1)
            else:
                self.results['tasks_completed'].append(1)
                self.results['latencies'].append(result['latency'])
                self.results['cpu_utilizations'].append(result['cpu_utilization'])
                self.results['gpu_utilizations'].append(result['gpu_utilization'])
                self.results['energy_consumption'].append(result['energy'])
        
        # Calcula throughput
        if len(self.results['tasks_completed']) > 0:
            throughput = len(self.results['tasks_completed']) / self.simulation_time
            self.results['throughput'].append(throughput)
    
    def run_scenario_gpu_basic(self):
        """Cenário 2: GPU Básico - Offloading simples"""
        print(f"    Executando Cenário 2: GPU Básico")
        print(f"    Dispositivos: {self.num_devices}, Tempo: {self.simulation_time}s")
        
        task_types = ['ML_INFERENCE', 'VIDEO_PROCESSING', 'AR_VR', 'IMAGE_PROCESSING']
        task_weights = [0.3, 0.25, 0.25, 0.2]
        
        arrival_rate = 0.15
        expected_tasks = int(self.num_devices * arrival_rate * self.simulation_time)
        num_tasks = np.random.poisson(expected_tasks)
        
        print(f"    Gerando {num_tasks} tarefas com offloading GPU...")
        
        for i in range(num_tasks):
            task_type = np.random.choice(task_types, p=task_weights)
            # Todas as tarefas tentam usar GPU
            result = self.simulate_task(task_type, use_gpu=True)
            
            if result['failed']:
                self.results['tasks_failed'].append(1)
            else:
                self.results['tasks_completed'].append(1)
                self.results['latencies'].append(result['latency'])
                self.results['cpu_utilizations'].append(result['cpu_utilization'])
                self.results['gpu_utilizations'].append(result['gpu_utilization'])
                self.results['energy_consumption'].append(result['energy'])
        
        if len(self.results['tasks_completed']) > 0:
            throughput = len(self.results['tasks_completed']) / self.simulation_time
            self.results['throughput'].append(throughput)
    
    def run_scenario_hybrid(self):
        """Cenário 3: Híbrido Inteligente - Decisão dinâmica CPU vs GPU"""
        print(f"    Executando Cenário 3: Híbrido Inteligente")
        print(f"    Dispositivos: {self.num_devices}, Tempo: {self.simulation_time}s")
        
        task_types = ['ML_INFERENCE', 'VIDEO_PROCESSING', 'AR_VR', 'IMAGE_PROCESSING']
        task_weights = [0.3, 0.25, 0.25, 0.2]
        
        arrival_rate = 0.15
        expected_tasks = int(self.num_devices * arrival_rate * self.simulation_time)
        num_tasks = np.random.poisson(expected_tasks)
        
        print(f"    Gerando {num_tasks} tarefas com decisão híbrida...")
        
        for i in range(num_tasks):
            task_type = np.random.choice(task_types, p=task_weights)
            # Decisão inteligente: tarefas pesadas vão para GPU
            use_gpu_decision = task_type in ['VIDEO_PROCESSING', 'AR_VR']
            result = self.simulate_task(task_type, use_gpu=use_gpu_decision, hybrid_decision=True)
            
            if result['failed']:
                self.results['tasks_failed'].append(1)
            else:
                self.results['tasks_completed'].append(1)
                self.results['latencies'].append(result['latency'])
                self.results['cpu_utilizations'].append(result['cpu_utilization'])
                self.results['gpu_utilizations'].append(result['gpu_utilization'])
                self.results['energy_consumption'].append(result['energy'])
        
        if len(self.results['tasks_completed']) > 0:
            throughput = len(self.results['tasks_completed']) / self.simulation_time
            self.results['throughput'].append(throughput)
    
    def run_scenario_stress(self):
        """Cenário 4: Stress Test - Alta carga com múltiplos tipos"""
        print(f"    Executando Cenário 4: Stress Test")
        print(f"    Dispositivos: {self.num_devices * 2}, Tempo: {self.simulation_time}s")
        
        task_types = ['ML_INFERENCE', 'VIDEO_PROCESSING', 'AR_VR', 'IMAGE_PROCESSING']
        task_weights = [0.3, 0.25, 0.25, 0.2]
        
        # Dobra a taxa de chegada para stress
        arrival_rate = 0.3  # 2x mais tarefas
        expected_tasks = int(self.num_devices * 2 * arrival_rate * self.simulation_time)
        num_tasks = np.random.poisson(expected_tasks)
        
        print(f"    Gerando {num_tasks} tarefas sob alta carga...")
        
        for i in range(num_tasks):
            task_type = np.random.choice(task_types, p=task_weights)
            use_gpu_decision = True  # Stress test: tenta usar GPU sempre
            result = self.simulate_task(task_type, use_gpu=use_gpu_decision)
            
            # Aumenta taxa de falha sob stress
            if np.random.random() < 0.02:  # 2% adicional de falha
                result['failed'] = True
            
            if result['failed']:
                self.results['tasks_failed'].append(1)
            else:
                self.results['tasks_completed'].append(1)
                self.results['latencies'].append(result['latency'])
                self.results['cpu_utilizations'].append(result['cpu_utilization'])
                self.results['gpu_utilizations'].append(result['gpu_utilization'])
                self.results['energy_consumption'].append(result['energy'])
        
        if len(self.results['tasks_completed']) > 0:
            throughput = len(self.results['tasks_completed']) / self.simulation_time
            self.results['throughput'].append(throughput)
    
    def get_statistics(self):
        """Calcula estatísticas descritivas dos resultados"""
        stats = {}
        
        if len(self.results['latencies']) > 0:
            stats['avg_latency'] = np.mean(self.results['latencies'])
            stats['median_latency'] = np.median(self.results['latencies'])
            stats['std_latency'] = np.std(self.results['latencies'])
            stats['p95_latency'] = np.percentile(self.results['latencies'], 95)
            stats['p99_latency'] = np.percentile(self.results['latencies'], 99)
        else:
            stats['avg_latency'] = 0
            stats['median_latency'] = 0
            stats['std_latency'] = 0
            stats['p95_latency'] = 0
            stats['p99_latency'] = 0
        
        if len(self.results['cpu_utilizations']) > 0:
            stats['avg_cpu_util'] = np.mean(self.results['cpu_utilizations'])
            stats['avg_gpu_util'] = np.mean(self.results['gpu_utilizations'])
        else:
            stats['avg_cpu_util'] = 0
            stats['avg_gpu_util'] = 0
        
        if len(self.results['energy_consumption']) > 0:
            stats['total_energy'] = np.sum(self.results['energy_consumption'])
            stats['avg_energy_per_task'] = np.mean(self.results['energy_consumption'])
        else:
            stats['total_energy'] = 0
            stats['avg_energy_per_task'] = 0
        
        stats['tasks_completed'] = len(self.results['tasks_completed'])
        stats['tasks_failed'] = len(self.results['tasks_failed'])
        total_tasks = stats['tasks_completed'] + stats['tasks_failed']
        stats['success_rate'] = (stats['tasks_completed'] / total_tasks * 100) if total_tasks > 0 else 0
        
        if len(self.results['throughput']) > 0:
            stats['throughput'] = np.mean(self.results['throughput'])
        else:
            stats['throughput'] = 0
        
        return stats


def run_all_scenarios():
    """Executa todos os 4 cenários científicos"""
    
    print("\n" + "="*70)
    print("  GpuEdgeCloudSim v1.0 - Simulação de Cenários Científicos")
    print("  Autor: Pabllo Borges Cardoso")
    print("  Data: " + datetime.now().strftime("%d/%m/%Y %H:%M:%S"))
    print("="*70 + "\n")
    
    # Parâmetros de simulação
    num_devices = 1000
    simulation_time = 600  # 10 minutos
    
    scenarios = {
        'scenario1_baseline': 'Baseline CPU-only',
        'scenario2_gpu_basic': 'GPU Básico',
        'scenario3_hybrid': 'Híbrido Inteligente',
        'scenario4_stress': 'Stress Test'
    }
    
    all_results = {}
    
    for scenario_key, scenario_name in scenarios.items():
        print(f"\n{'='*70}")
        print(f"  Cenário: {scenario_name}")
        print(f"{'='*70}")
        
        simulator = GpuEdgeCloudSimulator(scenario_name, num_devices, simulation_time)
        
        start_time = time.time()
        
        if scenario_key == 'scenario1_baseline':
            simulator.run_scenario_baseline()
        elif scenario_key == 'scenario2_gpu_basic':
            simulator.run_scenario_gpu_basic()
        elif scenario_key == 'scenario3_hybrid':
            simulator.run_scenario_hybrid()
        elif scenario_key == 'scenario4_stress':
            simulator.run_scenario_stress()
        
        elapsed = time.time() - start_time
        
        stats = simulator.get_statistics()
        all_results[scenario_key] = stats
        
        print(f"\n    ✓ Simulação concluída em {elapsed:.2f}s")
        print(f"    Tarefas completadas: {stats['tasks_completed']}")
        print(f"    Tarefas falhadas: {stats['tasks_failed']}")
        print(f"    Taxa de sucesso: {stats['success_rate']:.2f}%")
        print(f"    Latência média: {stats['avg_latency']*1000:.2f}ms")
        print(f"    Throughput: {stats['throughput']:.2f} tarefas/s")
        print(f"    Utilização CPU: {stats['avg_cpu_util']:.2f}%")
        print(f"    Utilização GPU: {stats['avg_gpu_util']:.2f}%")
        print(f"    Energia total: {stats['total_energy']:.2f}J")
    
    # Salva resultados em JSON e CSV
    results_dir = Path('/home/ubuntu/sim_results')
    results_dir.mkdir(exist_ok=True)
    
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    
    # Salva JSON
    json_file = results_dir / f'gpuedgecloudsim_results_{timestamp}.json'
    with open(json_file, 'w') as f:
        json.dump(all_results, f, indent=2)
    print(f"\n✓ Resultados salvos em: {json_file}")
    
    # Salva CSV comparativo
    df = pd.DataFrame(all_results).T
    csv_file = results_dir / f'gpuedgecloudsim_comparison_{timestamp}.csv'
    df.to_csv(csv_file)
    print(f"✓ Comparação salva em: {csv_file}")
    
    return all_results


if __name__ == "__main__":
    results = run_all_scenarios()
    
    print("\n" + "="*70)
    print("  Todas as simulações foram concluídas com sucesso!")
    print("="*70 + "\n")
