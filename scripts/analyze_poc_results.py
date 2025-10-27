#!/usr/bin/env python3
"""
GpuEdgeCloudSim v1.0 - PoC Results Analysis
Generates visualizations and advanced metrics from simulation results
"""

import pandas as pd
import plotly.graph_objects as go
from plotly.subplots import make_subplots
import plotly.express as px

# Load data
df = pd.read_csv('/home/ubuntu/poc_results.csv')

print("=" * 70)
print("  GpuEdgeCloudSim v1.0 - Results Analysis")
print("=" * 70)
print()

# Basic Statistics
print("[1/5] Calculating Statistics...")
print()
print("Task Type Distribution:")
print(df['TaskType'].value_counts())
print()
print("Execution Time Statistics:")
print(df['TotalTime'].describe())
print()
print("GPU Utilization Statistics:")
print(df['GpuUtilization'].describe())
print()

# Create visualizations
print("[2/5] Creating Visualizations...")

# Figure 1: Execution Time by Task Type
fig1 = px.box(df, x='TaskType', y='TotalTime', 
              title='Execution Time Distribution by Task Type',
              labels={'TotalTime': 'Execution Time (ms)', 'TaskType': 'Task Type'},
              color='TaskType',
              template='plotly_white')
fig1.write_html('/home/ubuntu/plot_execution_time.html', include_plotlyjs='cdn')
print("  ✓ Generated: plot_execution_time.html")

# Figure 2: GPU Utilization by Task Type
fig2 = px.bar(df, x='TaskID', y='GpuUtilization', color='TaskType',
              title='GPU Utilization per Task',
              labels={'GpuUtilization': 'GPU Utilization (%)', 'TaskID': 'Task ID'},
              template='plotly_white')
fig2.write_html('/home/ubuntu/plot_gpu_utilization.html', include_plotlyjs='cdn')
print("  ✓ Generated: plot_gpu_utilization.html")

# Figure 3: Timeline View
fig3 = px.timeline(df, x_start='StartTime', x_end=df['StartTime'] + df['TotalTime'],
                   y='TaskType', color='TaskType',
                   title='Task Execution Timeline',
                   labels={'StartTime': 'Time (ms)'},
                   template='plotly_white')
fig3.update_yaxes(autorange="reversed")
fig3.write_html('/home/ubuntu/plot_timeline.html', include_plotlyjs='cdn')
print("  ✓ Generated: plot_timeline.html")

# Figure 4: Comprehensive Dashboard
print()
print("[3/5] Creating Comprehensive Dashboard...")

fig4 = make_subplots(
    rows=2, cols=2,
    subplot_titles=('Execution Time by Task Type', 'GPU Utilization Distribution',
                    'Task Execution Timeline', 'Performance Metrics'),
    specs=[[{'type': 'bar'}, {'type': 'box'}],
           [{'type': 'scatter'}, {'type': 'indicator'}]]
)

# Subplot 1: Execution Time
for task_type in df['TaskType'].unique():
    task_df = df[df['TaskType'] == task_type]
    fig4.add_trace(
        go.Bar(x=task_df['TaskID'], y=task_df['TotalTime'], name=task_type),
        row=1, col=1
    )

# Subplot 2: GPU Utilization Box Plot
for task_type in df['TaskType'].unique():
    task_df = df[df['TaskType'] == task_type]
    fig4.add_trace(
        go.Box(y=task_df['GpuUtilization'], name=task_type),
        row=1, col=2
    )

# Subplot 3: Timeline
fig4.add_trace(
    go.Scatter(x=df['StartTime'], y=df['TaskID'], 
               mode='markers+lines', name='Execution',
               marker=dict(size=10, color=df['GpuUtilization'], 
                          colorscale='Viridis', showscale=True)),
    row=2, col=1
)

# Subplot 4: Key Metrics
avg_exec_time = df['TotalTime'].mean()
fig4.add_trace(
    go.Indicator(
        mode="number+delta",
        value=avg_exec_time,
        title={'text': "Avg Execution Time (ms)"},
        delta={'reference': 0.2, 'relative': True}
    ),
    row=2, col=2
)

fig4.update_layout(height=800, showlegend=True, template='plotly_white',
                   title_text="GpuEdgeCloudSim v1.0 - Performance Dashboard")
fig4.write_html('/home/ubuntu/dashboard.html', include_plotlyjs='cdn')
print("  ✓ Generated: dashboard.html")

# Figure 5: Comparative Analysis
print()
print("[4/5] Creating Comparative Analysis...")

task_summary = df.groupby('TaskType').agg({
    'TotalTime': ['mean', 'std', 'min', 'max'],
    'GpuUtilization': ['mean', 'std'],
    'TaskID': 'count'
}).round(3)

task_summary.columns = ['_'.join(col).strip() for col in task_summary.columns.values]
task_summary = task_summary.reset_index()

fig5 = go.Figure()

fig5.add_trace(go.Bar(
    x=task_summary['TaskType'],
    y=task_summary['TotalTime_mean'],
    name='Avg Execution Time',
    error_y=dict(type='data', array=task_summary['TotalTime_std'])
))

fig5.update_layout(
    title='Average Execution Time by Task Type (with std dev)',
    xaxis_title='Task Type',
    yaxis_title='Execution Time (ms)',
    template='plotly_white'
)

fig5.write_html('/home/ubuntu/plot_comparative.html', include_plotlyjs='cdn')
print("  ✓ Generated: plot_comparative.html")

# Generate Summary Statistics Table
print()
print("[5/5] Generating Summary Report...")
print()
print("=" * 70)
print("  SUMMARY STATISTICS BY TASK TYPE")
print("=" * 70)
print()
print(task_summary.to_string(index=False))
print()

# Performance Insights
print("=" * 70)
print("  PERFORMANCE INSIGHTS")
print("=" * 70)
print()
print(f"1. Fastest Task Type: {df.groupby('TaskType')['TotalTime'].mean().idxmin()}")
print(f"   Average Time: {df.groupby('TaskType')['TotalTime'].mean().min():.3f} ms")
print()
print(f"2. Most GPU-Intensive: {df.groupby('TaskType')['GpuUtilization'].mean().idxmax()}")
print(f"   Average Utilization: {df.groupby('TaskType')['GpuUtilization'].mean().max():.2f}%")
print()
print(f"3. Most Common Task: {df['TaskType'].value_counts().idxmax()}")
print(f"   Occurrences: {df['TaskType'].value_counts().max()}")
print()
print(f"4. Overall Throughput: {len(df) / df['TotalTime'].sum() * 1000:.2f} tasks/sec")
print()
print(f"5. GPU Efficiency: {df['GpuUtilization'].mean():.2f}% average utilization")
print()

print("=" * 70)
print("  Analysis completed successfully!")
print("=" * 70)
print()
print("Generated files:")
print("  - /home/ubuntu/plot_execution_time.html")
print("  - /home/ubuntu/plot_gpu_utilization.html")
print("  - /home/ubuntu/plot_timeline.html")
print("  - /home/ubuntu/plot_comparative.html")
print("  - /home/ubuntu/dashboard.html")
print()
