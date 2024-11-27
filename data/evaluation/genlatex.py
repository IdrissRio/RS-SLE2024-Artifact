import sys
import os
import json
import numpy as np
import matplotlib.pyplot as plt
import scipy.stats as stats
import seaborn as sns
from prettytable import PrettyTable
import pandas as pd
import numpy as np
from matplotlib.ticker import ScalarFormatter

import warnings
warnings.filterwarnings("ignore")





import matplotlib.pyplot as plt
from matplotlib.ticker import ScalarFormatter

def plot_project_data2(project_names, eval_type_relaxedmonolithic, eval_type_relaxedstacked, df, df2):
    # Create a 1x1 grid of subplots
    fig, axs = plt.subplots(4,2, figsize=(20, 20))
    axs = axs.flatten() #with more than one plot
    fig.subplots_adjust(wspace=1)

    # Store legend handles and labels
    handles = []
    labels = []

    # Plot for each project
    for i, project in enumerate(project_names):
        project_df = df[df['Project Name'] == project]
        project_df_2 = df2[df2['Project Name'] == project]

        org_data = project_df[project_df['Evaluation Type'] == eval_type_relaxedmonolithic]
        org_data2 = project_df_2[(project_df_2['Attribute'] == 'succ()') & (project_df_2['Evaluation Type'] == eval_type_relaxedmonolithic)]

        dnc_data = project_df[project_df['Evaluation Type'] == eval_type_relaxedstacked]
        dnc_data2 = project_df_2[(project_df_2['Attribute'] == 'succ()') & (project_df_2['Evaluation Type'] == eval_type_relaxedstacked)]

        # Set the current subplot
        ax = axs[i]

        # Plotting
        ax.plot(org_data['# Iteration'], org_data['Analysis Median'], label='$\mathtt{RelaxedMonolithic}$ execution time', marker='o', linestyle='-', color='red')
        ax.plot(dnc_data['# Iteration'], dnc_data['Analysis Median'], label='$\mathtt{RelaxedStacked}$ execution time', marker='o',
                linestyle='-', color='green')

        # Calculate the range of y values for each plot
        y_range = max(org_data['Analysis Median'].max(), dnc_data['Analysis Median'].max()) - min(
            org_data['Analysis Median'].min(), dnc_data['Analysis Median'].min())

        # Calculate a suitable offset based on the range of y values
        offset = y_range * 0.02  # Adjust this multiplier as needed to control the offset

        for x, y in zip(dnc_data['# Iteration'], dnc_data['Analysis Median']):
            org_execution_time = org_data[org_data['# Iteration'] == x]['Analysis Median'].values[0]
            speedup = org_execution_time / y
            ax.text(x, y + offset, f'x{speedup:.1f}', color='black', fontsize=12, ha='center', fontweight='bold')

        # Create a twin Axes sharing the same x-axis
        ax2 = ax.twinx()
        ax2.plot(org_data2['Iteration'], org_data2['Counter'], label='$\mathtt{RelaxedMonolithic}$ Successor Evaluation Count', marker='x',
                 linestyle='--', color='red')  # Different scale
        ax2.plot(dnc_data2['Iteration'], dnc_data2['Counter'], label='$\mathtt{RelaxedStacked}$ Successor Evaluation Count', marker='x',
                 linestyle='--', color='green')  # Different scale

        # Error bars
        ax.errorbar(org_data['# Iteration'], org_data['Analysis Median'], yerr=org_data['Analysis CI'], linestyle='None',
                    color='red', capsize=5)
        ax.errorbar(dnc_data['# Iteration'], dnc_data['Analysis Median'], yerr=dnc_data['Analysis CI'],
                    linestyle='None', color='green', capsize=5)

        # Set labels and title with increased font size
        ax.set_xlabel('# Methods', fontsize=16)
        ax.set_ylabel('Execution Time (s)', color='black', fontsize=16)
        ax.set_title(f'{project}', fontsize=18)
        ax.grid(True)

        # Setting labels and legend for the right scale
        ax2.set_ylabel('Successor Evaluation Count', color='black', fontsize=14)

        # # only if handles in not empty
        if handles==[] :

            handles_, labels_ = ax.get_legend_handles_labels()
            handles.extend(handles_)
            labels.extend(labels_)

            handles_, labels_ = ax2.get_legend_handles_labels()
            handles.extend(handles_)
            labels.extend(labels_)

        ax.xaxis.set_major_formatter(ScalarFormatter(useMathText=True))
        ax.yaxis.set_major_formatter(ScalarFormatter(useMathText=True))
        ax2.yaxis.set_major_formatter(ScalarFormatter(useMathText=True))

        # Set explicit tick label size
        ax.tick_params(axis='both', which='major', labelsize=12)
        ax2.tick_params(axis='both', which='major', labelsize=12)

    # Create combined legend
    fig.legend(handles, labels, loc='upper left', fontsize=12, bbox_to_anchor=(0.040, 0.98), ncol=1)

    # Adjust layout
    plt.tight_layout()

    # Save the figure
    plt.savefig('latex/' + eval_type_relaxedmonolithic + '_vs_' + eval_type_relaxedstacked + 'on-demand.pdf', format='pdf')





def plot_project_data(project_names, eval_type_relaxedmonolithic, eval_type_relaxedstacked, df):
    # Create a 3x3 grid of subplots
    fig, axs = plt.subplots(3, 3, figsize=(15, 15))

    means = pd.DataFrame()



    # Calculate mean values for each # Iteration
    # Plot for each project
    for i, project in enumerate(project_names):
        project_df = df[df['Project Name'] == project]

        org_data = project_df[project_df['Evaluation Type'] == eval_type_relaxedmonolithic]
        dnc_data = project_df[project_df['Evaluation Type'] == eval_type_relaxedstacked]

        # Set the current subplot
        plt.sca(axs[i])

        # Plotting using mean values for # Iteration
        plt.plot(means[project], org_data['Analysis Median'], label='RelaxedMonolithic', marker='o', color='red')
        plt.plot(means[project], dnc_data['Analysis Median'], label='RelaxedStacked', marker='o', color='green')

        # Error bars
        plt.errorbar(means[project], org_data['Analysis Median'], yerr=org_data['Analysis CI'], linestyle='None', color='red', capsize=5)
        plt.errorbar(means[project], dnc_data['Analysis Median'], yerr=dnc_data['Analysis CI'], linestyle='None', color='green', capsize=5)

        # Set labels and title
        plt.xlabel('Methods size')
        plt.ylabel('Execution Time (s)')
        plt.title(f'Analysis Median for {project}')
        plt.legend()
        plt.grid(True)

    # Adjust layout
    plt.tight_layout()

    plt.savefig('latex/'+eval_type_relaxedmonolithic+'_vs_'+eval_type_relaxedstacked+'permethod.png')


def generate_latex_table(evaluation_type, caption, df):
    latex_table = "\\begin{table}[H]\n"
    latex_table += "\t\\begin{tabular}{|l|rrr|rrr|}\n"
    latex_table += "\t\\hline\n"
    latex_table += "\t\\multirow{3}{*}{\\textsc{Benchmark}} & \\multicolumn{3}{c|}{\\textsc{Start up}} & \\multicolumn{3}{c|}{\\textsc{Steady State}} \\\\ \\cline{2-7}\n"
    latex_table += "\t& \\multicolumn{1}{c|}{\\textsc{Relaxed-}} & \\multicolumn{2}{c|}{\\textsc{Relaxed-}} & \\multicolumn{1}{c|}{\\textsc{Relaxed-}} & \\multicolumn{2}{c|}{\\textsc{Relaxed-}} \\\\\n"
    latex_table += "\t& \\multicolumn{1}{c|}{\\textsc{Monolithic}} & \\multicolumn{2}{c|}{\\textsc{Stacked}} & \\multicolumn{1}{c|}{\\textsc{Monolithic}} & \\multicolumn{2}{c|}{\\textsc{Stacked}} \\\\ \\cline{2-7}\n"
    latex_table += "\t& \\multicolumn{1}{c|}{Time (s)} & \\multicolumn{1}{c|}{Time (s)} & \\textsc{Speedup} & \\multicolumn{1}{c|}{Time (s)} & \\multicolumn{1}{c|}{Time (s)} & \\textsc{Speedup} \\\\ \\hline\n"

    # Iterate over all the projects
    for project in df['Project Name'].unique():
        df_filtered = df[(df['Project Name'] == project) & (df['Evaluation Type'].str.contains(evaluation_type))]

        # Get the rows for 'warmup' and 'steady' state types
        org_warmup = df_filtered[(df_filtered['State Type'] == 'warmup') & (df_filtered['Evaluation Type'].str.contains('relaxedmonolithic'))]
        dnc_warmup = df_filtered[(df_filtered['State Type'] == 'warmup') & (df_filtered['Evaluation Type'].str.contains('relaxedstacked'))]
        org_steady = df_filtered[(df_filtered['State Type'] == 'steady') & (df_filtered['Evaluation Type'].str.contains('relaxedmonolithic'))]
        dnc_steady = df_filtered[(df_filtered['State Type'] == 'steady') & (df_filtered['Evaluation Type'].str.contains('relaxedstacked'))]

        # Check for timeout
        org_warmup_timeout = org_warmup['Timeout'].values[0] if not org_warmup.empty else False
        dnc_warmup_timeout = dnc_warmup['Timeout'].values[0] if not dnc_warmup.empty else False
        org_steady_timeout = org_steady['Timeout'].values[0] if not org_steady.empty else False
        dnc_steady_timeout = dnc_steady['Timeout'].values[0] if not dnc_steady.empty else False

        # Calculate speedup
        if not org_warmup_timeout and not dnc_warmup_timeout:
            speedup_warmup = org_warmup['Analysis Median'].values[0] / dnc_warmup['Analysis Median'].values[0]
        else:
            speedup_warmup = float('inf')

        if not org_steady_timeout and not dnc_steady_timeout:
            speedup_steady = org_steady['Analysis Median'].values[0] / dnc_steady['Analysis Median'].values[0]
        else:
            speedup_steady = float('inf')

        # Format speedup
        def format_speedup(speedup):
            if speedup == float('inf'):
                return "\\gespeedup{0}"
            elif speedup > 1.02:
                return f"\\speedupnew{{{speedup:.2f}}}"
            elif speedup < 0.98:
                return f"\\slowdownnew{{{speedup:.2f}}}"
            else:
                return "\\same{}"

        speedup_warmup = format_speedup(speedup_warmup)
        speedup_steady = format_speedup(speedup_steady)

        # escape underscores
        project = project.replace("_", "\_")

        # Format evaluation times
        def format_time(row, timeout):
            if timeout:
                return f"\\evaltimeout{{{row['Analysis Median'].values[0]:.2f}}}"
            else:
                return f"\\eval{{{row['Analysis Median'].values[0]:.2f}}}{{{row['Analysis CI'].values[0]:.2f}}}"

        org_warmup_time = format_time(org_warmup, org_warmup_timeout)
        dnc_warmup_time = format_time(dnc_warmup, dnc_warmup_timeout)
        org_steady_time = format_time(org_steady, org_steady_timeout)
        dnc_steady_time = format_time(dnc_steady, dnc_steady_timeout)

        # Add the data to the LaTeX table
        latex_table += f"\t \\code{{{project}}} & \\multicolumn{{1}}{{r|}}{{{org_warmup_time}}} & \\multicolumn{{1}}{{r|}}{{{dnc_warmup_time}}} & {speedup_warmup} & \\multicolumn{{1}}{{r|}}{{{org_steady_time}}} & \\multicolumn{{1}}{{r|}}{{{dnc_steady_time}}} & {speedup_steady} \\\\ \\hline\n"

    latex_table += "\t\\end{tabular}"
    latex_table += f"""
    \caption{{\label{{tab:{evaluation_type}}} {caption}}}
    """
    latex_table += "\n\\end{table}"
    return latex_table





# Get the path to the directory containing the evaluation results and project.json
results_dir = sys.argv[1]
isFast = sys.argv[2]
if isFast == "true":
    project_file = "./projects_fast.json"
else:
    project_file = "./projects.json"

# Load the project data from project.json
with open(project_file, 'r') as f:
    project_data = json.load(f)

# Sort the project data by LOC
sorted_project_data = sorted(project_data['benchmarks'], key=lambda x: x['LOC'])

# Extract the project names and LOC values
project_names = [p['name'] for p in sorted_project_data if p['enable']]
loc_values = [p['LOC'] for p in sorted_project_data if p['enable']]

# Define the evaluation types and warmup/steady state
evaluation_types_standard = ["intraj_npa_relaxedmonolithic", "intraj_daa_relaxedmonolithic", "intraj_npa_relaxedstacked", "intraj_daa_relaxedstacked"]
state_types_standard = ["warmup", "steady"]

 ####################################################################     INTRAJ

# Create a table to display the results
table = PrettyTable()
table.field_names = ["Project Name", "Evaluation Type", "State Type", "Analysis median", "Analysis CI", "Timeout"]
data_list = []
# Loop through each project and add the results to the table
for i, project_name in enumerate(project_names):
    for j, eval_type in enumerate(evaluation_types_standard):
        for k, state_type in enumerate(state_types_standard):
            # Construct the filename for the current project and evaluation type/state type combination
            file_path = os.path.join(results_dir, f"{project_name}_{eval_type}_{state_type}_results.new")

            # Read the data from the file
            with open(file_path, 'r') as f:
                data_lines = f.readlines() # Skip first line

            # Extract the analysis and error data from each line
            analysis_data = []
            error_data = []
            timeout = False
            for line in data_lines:
                if line.__contains__("Analysis"):
                    analysis_data.append(float(line.split(":")[1].strip()))
                elif line.__contains__("Timeout"):
                    analysis_data.append(float(line.split(":")[1].strip()))
                    timeout = True
                elif line.__contains__("Error"):
                    error_data.append(float(line.split(":")[1].strip()))
                else:
                    values = [float(value.strip()) for value in line.split(" ") if value.strip() != '']
                    analysis_data.extend(values)

            # Compute the median and confidence interval of the analysis and error data
            analysis_median = np.median(analysis_data)
            analysis_stderr = stats.sem(analysis_data)
            analysis_ci = stats.t.interval(0.95, len(analysis_data)-1, loc=analysis_median, scale=analysis_stderr)
            error_median = np.median(error_data)
            error_stderr = stats.sem(error_data)
            error_ci = stats.t.interval(0.95, len(error_data)-1, loc=error_median, scale=error_stderr)
            table.add_row([
                project_name,
                eval_type,
                state_type,
                '{:.10f}'.format(analysis_median),
                '({:.10f})'.format((analysis_ci[1]-analysis_ci[0]) ),
                timeout
            ])

            data_list.append([
                project_name,
                eval_type,
                state_type,
                analysis_median,
                (analysis_ci[1] - analysis_ci[0]),
                timeout
            ])
columns = ['Project Name', 'Evaluation Type', 'State Type', 'Analysis Median', 'Analysis CI', 'Timeout']
df = pd.DataFrame(data_list, columns=columns)

res = generate_latex_table('npa', 'Evaluation results for \emph{Null-Pointer Analysis} computing performance, comparing the start-up and steady-state execution times of the \intrajbaseline{} and \intrajrelaxed{}. Times are reported in seconds and speedup is reported as a ratio relative to the \intrajbaseline{} for both start-up and steady-state phases. Results are considered equivalent ({\same{}}) if the ratio is within the range [0.98, 1.02].', df)


with open('latex/intraj-table-npa.tex', 'w') as f:
    f.write(res)

res = generate_latex_table('daa', 'Evaluation results for \emph{Dead Assignment Analysis} computing performance, comparing the start-up and steady-state execution times of the \intrajbaseline{} and \intrajrelaxed{}. Times are reported in seconds and speedup is reported as a ratio relative to the \intrajbaseline{} for both start-up and steady-state phases. ', df)

with open('latex/intraj-table-daa.tex', 'w') as f:
    f.write(res)

# Save PrettyTable to a text file
with open(os.path.join(results_dir, 'results_table_intraj.txt'), 'w') as f:
    f.write(table.get_string())

########################## ON-DEMAND ##########################


# # Get the path to the directory containing the evaluation results and project.json
results_dir = sys.argv[1]
# project_file = "./projects.json"

# Load the project data from project.json
with open(project_file, 'r') as f:
    project_data = json.load(f)

# Sort the project data by LOC
sorted_project_data = sorted(project_data['benchmarks'], key=lambda x: x['LOC'])

# Extract the project names and LOC values
project_names = [p['name'] for p in sorted_project_data if p['enable']]
# Projects methods
project_methods = [p['methods'] for p in sorted_project_data if p['enable']]
loc_values = [p['LOC'] for p in sorted_project_data if p['enable']]

# Define the evaluation types and warmup/steady state
evaluation_types = ["intraj_ondemand_npa_relaxedmonolithic", "intraj_ondemand_daa_relaxedmonolithic", "intraj_ondemand_npa_relaxedstacked",  "intraj_ondemand_daa_relaxedstacked"]
state_types = ["ondemand"]
iterations = [10, 20, 50, 100, 200]

# Create a table to display the results
table = PrettyTable()
table.field_names = ["Project Name", "Evaluation Type", "State Type", "# Iteration",  "Analysis median", "Analysis CI"]
data_list = []
for i, project_name in enumerate(project_names):
    for j, eval_type in enumerate(evaluation_types):
        for k, state_type in enumerate(state_types):
            for l, iteration in enumerate(iterations):
                # Construct the filename for the current project and evaluation type/state type combination
                file_path = os.path.join(results_dir, f"{project_name}_{eval_type}_{state_type}_{iteration}_results.new")

                # Read the data from the file
                with open(file_path, 'r') as f:
                    data_lines = f.readlines() # Skip first line

                # Extract the analysis and error data from each line
                analysis_data = []
                error_data = []
                for line in data_lines:
                    if line.__contains__("Analysis"):
                        analysis_data.append(float(line.split(":")[1].strip()))
                    elif line.__contains__("Error"):
                        error_data.append(float(line.split(":")[1].strip()))

                # Compute the median and confidence interval of the analysis and error data
                analysis_median = np.median(analysis_data)
                analysis_stderr = stats.sem(analysis_data)
                analysis_ci = stats.t.interval(0.95, len(analysis_data)-1, loc=analysis_median, scale=analysis_stderr)
                error_median = np.median(error_data)
                error_stderr = stats.sem(error_data)
                error_ci = stats.t.interval(0.95, len(error_data)-1, loc=error_median, scale=error_stderr)

                table.add_row([
                    project_name,
                    eval_type,
                    state_type,
                    iteration,
                    '{:.10f}'.format(analysis_median),
                    '({:.10f})'.format((analysis_ci[1]-analysis_ci[0]) ),
                ])
                data_list.append([
                    project_name,
                    eval_type,
                    state_type,
                    iteration,
                    analysis_median,
                    (analysis_ci[1] - analysis_ci[0]),
                ])
columns = ['Project Name', 'Evaluation Type', 'State Type', '# Iteration', 'Analysis Median', 'Analysis CI']
df = pd.DataFrame(data_list, columns=columns)

with open(os.path.join(results_dir, 'results_table_ondemand.txt'), 'w') as f:
    f.write(table.get_string())



# Create a table to display the results
table = PrettyTable()
table.field_names = ["Project Name",  "Attribute", "Iteration",  "Counter", "Evaluation Type"]
data_list = []
for i, project_name in enumerate(project_names):
    for j, eval_type in enumerate(evaluation_types):
        for k, state_type in enumerate(state_types):
            for l, iteration in enumerate(iterations):
                # Construct the filename for the current project and evaluation type/state type combination
                file_path = os.path.join(results_dir, f"{project_name}_{eval_type}_{iteration}computeBegin.txt")

                # Read the data from the file
                with open(file_path, 'r') as f:
                    data_lines = f.readlines() # Skip first line

                # Extract the analysis and error data from each line
                for line in data_lines:
                        counter = (float(line.split(":")[1].strip()))
                        attributeName= line.split(":")[0].strip()
                        data_list.append([
                            project_name,
                            attributeName,
                            iteration,
                            counter,
                            eval_type

                        ])
columns = ["Project Name",  "Attribute", "Iteration",  "Counter", "Evaluation Type"]
df2 = pd.DataFrame(data_list, columns=columns)
plot_project_data2(project_names, "intraj_ondemand_npa_relaxedmonolithic", "intraj_ondemand_npa_relaxedstacked", df, df2)
plot_project_data2(project_names, "intraj_ondemand_daa_relaxedmonolithic", "intraj_ondemand_daa_relaxedstacked", df, df2)

####################################################################### EXTENDJ


# Define the evaluation types and warmup/steady state
evaluation_types_standard = ["extendj_relaxedmonolithic", "extendj_relaxedstacked"]
state_types_standard = ["warmup", "steady"]

# Create a table to display the results
table = PrettyTable()
table.field_names = ["Project Name", "Evaluation Type", "State Type", "Analysis median", "Analysis CI", "Timeout"]
data_list = []
# Loop through each project and add the results to the table
for i, project_name in enumerate(project_names):
    for j, eval_type in enumerate(evaluation_types_standard):
        for k, state_type in enumerate(state_types_standard):
            # Construct the filename for the current project and evaluation type/state type combination
            file_path = os.path.join(results_dir, f"{project_name}_{eval_type}_{state_type}_results.new")

            # Read the data from the file
            with open(file_path, 'r') as f:
                data_lines = f.readlines() # Skip first line

            # Extract the analysis and error data from each line
            analysis_data = []
            error_data = []
            timeout = False
            for line in data_lines:
                if line.__contains__("Analysis"):
                    analysis_data.append(float(line.split(":")[1].strip()))
                elif line.__contains__("Timeout"):
                    analysis_data.append(float(line.split(":")[1].strip()))
                    timeout = True
                elif line.__contains__("Error"):
                    error_data.append(float(line.split(":")[1].strip()))
                else:
                    values = [float(value.strip()) for value in line.split(" ") if value.strip() != '']
                    analysis_data.extend(values)

            # Compute the median and confidence interval of the analysis and error data
            analysis_median = np.median(analysis_data)
            analysis_stderr = stats.sem(analysis_data)
            analysis_ci = stats.t.interval(0.95, len(analysis_data)-1, loc=analysis_median, scale=analysis_stderr)
            error_median = np.median(error_data)
            error_stderr = stats.sem(error_data)
            error_ci = stats.t.interval(0.95, len(error_data)-1, loc=error_median, scale=error_stderr)

            data_list.append([
                project_name,
                eval_type,
                state_type,
                analysis_median,
                (analysis_ci[1] - analysis_ci[0]),
                timeout
            ])
columns = ['Project Name', 'Evaluation Type', 'State Type', 'Analysis Median', 'Analysis CI', 'Timeout']
df = pd.DataFrame(data_list, columns=columns)

res = generate_latex_table('extendj', 'Performance comparison of ExtendJ’s execution time during startup and steady state.', df)

with open('latex/extendj-table.tex', 'w') as f:
    f.write(res)


with open(os.path.join(results_dir, 'results_table_extendj.txt'), 'w') as f:
    f.write(table.get_string())

def read_data_from_file(file_path):
    """Reads numbers from a file and returns the list of numbers."""
    with open(file_path, 'r') as file:
        data = file.readlines()
    numbers = [float(line.strip()) * 1000 for line in data]
    return numbers

def calculate_confidence_interval(data):
    """Calculates the 95% confidence interval for a list of numbers."""
    mean = np.mean(data)
    std_dev = np.std(data, ddof=1)  # Sample standard deviation
    n = len(data)
    margin_of_error = 1.96 * (std_dev / np.sqrt(n))
    return mean, margin_of_error

def generate_latex_table(results_dir):
    # Read data from files
    old_stacked_data = read_data_from_file(os.path.join(results_dir, 'bs-old.txt'))
    basic_stacked_data = read_data_from_file(os.path.join(results_dir, 'bs.txt'))
    relaxed_monolithic_data = read_data_from_file(os.path.join(results_dir, 'rm.txt'))
    relaxed_stacked_data = read_data_from_file(os.path.join(results_dir, 'rs.txt'))

    # Calculate confidence intervals
    old_stacked_mean, old_stacked_error = calculate_confidence_interval(old_stacked_data)
    basic_stacked_mean, basic_stacked_error = calculate_confidence_interval(basic_stacked_data)
    relaxed_monolithic_mean, relaxed_monolithic_error = calculate_confidence_interval(relaxed_monolithic_data)
    relaxed_stacked_mean, relaxed_stacked_error = calculate_confidence_interval(relaxed_stacked_data)

    # LaTeX table string
    latex_table = f"""
\\begin{{table}}[H]
    \\setlength{{\\tabcolsep}}{{2pt}}
    \\begin{{tabular}}{{|l|c|c|c|c|c|c|}}
        \\hline
        \\textsc{{Language}} & \\textbf{{\\#T}} & \\textbf{{\\#P}} & \\textbf{{Old Stacked}} &\\textbf{{Basic Stacked}} & \\textbf{{Relaxed Monolithic}} & \\textbf{{Relaxed Stacked}} \\\\ \\cline{{2-5}}
        \\hline
        \\textsc{{Java 1.2}}    & 155 & 332 & \\eval{{{old_stacked_mean:.2f}}}{{{old_stacked_error:.2f}}} &   \\eval{{{basic_stacked_mean:.2f}}}{{{basic_stacked_error:.2f}}}           &      \\eval{{{relaxed_monolithic_mean:.2f}}}{{{relaxed_monolithic_error:.2f}}}              &    \\eval{{{relaxed_stacked_mean:.2f}}}{{{relaxed_stacked_error:.2f}}}   \\\\ \\hline
    \\end{{tabular}}
    \\caption{{Startup performance results for the \\textsc{{Java 1.2 grammar}} benchmark. Includes the number of terminals (\\#T) and productions (\\#P). The measurements are reported in milliseconds. }}
    \\label{{tab:cfg}}
\\end{{table}}
"""

    return latex_table

# Usage
latex_table = generate_latex_table(results_dir)
# save
with open('latex/cfg-table.tex', 'w') as f:
    f.write(latex_table)




import os
import subprocess

# Specify the path to your .tex file
tex_file = "latex/Results.tex"

# Get the directory of your .tex file
tex_dir = os.path.dirname(tex_file)

# Change the working directory
os.chdir(tex_dir)

# Call pdflatex to compile the .tex file into a .pdf
subprocess.run(["pdflatex", os.path.basename(tex_file)])
subprocess.run(["pdflatex", os.path.basename(tex_file)])
subprocess.run(["pdflatex", os.path.basename(tex_file)])
