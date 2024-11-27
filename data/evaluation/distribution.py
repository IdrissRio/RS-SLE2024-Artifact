# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # 

import matplotlib.pyplot as plt
import sys
import numpy as np
from matplotlib.ticker import ScalarFormatter

file_path = sys.argv[1]
dir = file_path + sys.argv[2] 

file_path = dir + "current_distribution.txt"

# Read data from file
with open(file_path, 'r') as file:
    content = file.read().strip('{}')
    items = content.split('], ')
    data = {}
    for item in items:
        key, values_str = item.split('=[')
        values = list(map(int, values_str.strip(']').split(', ')))
        data[int(key)] = values

# Create a colormap
cmap = plt.cm.viridis
color_range = np.linspace(0, 1, len(data))

fig, ax = plt.subplots(figsize=(10, 5))

# Plot the frequency of each value with a color for each key
for i, (key, values) in enumerate(data.items()):
    unique_values, counts = np.unique(values, return_counts=True)
    ax.bar(unique_values, counts, color=cmap(color_range[i]), edgecolor='black', linewidth=0.2, alpha=0.7, label=f'Partition {key}')

ax.set_title('Frequency of Values for Each Key')
ax.set_xlabel('Value')
ax.set_ylabel('Frequency')

ax.set_yscale('log')
ax.yaxis.set_major_formatter(ScalarFormatter())


# Add a legend and a grid
ax.legend()
ax.grid(True)

# Display the plot
plt.tight_layout()
plt.savefig(dir + "current_distribution.png")
plt.show()



# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # 

file_path = dir + "complete_distribution.txt"

# Read data from file
with open(file_path, 'r') as file:
    content = file.read().strip('{}')
    items = content.split(', ')
    data = {int(item.split('=')[0]): int(item.split('=')[1]) for item in items}

all_values = [value for value in data.values()]

# Create a colormap
cmap = plt.cm.viridis

fig, ax = plt.subplots(figsize=(10, 5))

# Plot the distribution of all data on a logarithmic scale
unique_values, counts = np.unique(all_values, return_counts=True)
ax.bar(unique_values, counts, color=cmap(0.5), edgecolor='black', linewidth=0.2, alpha=0.7)

ax.set_title('Distribution')
ax.set_xlabel('# of CFGNodes')
ax.set_ylabel('Frequency (Log Scale)')

# Set y-axis to logarithmic scale
ax.set_yscale('log')
ax.yaxis.set_major_formatter(ScalarFormatter())

ax.grid(True)

# Display the plot
plt.tight_layout()
plt.savefig(dir + "complete_distribution.png")
plt.show()


