import os
import csv
import numpy as np
import matplotlib
import matplotlib.pyplot as plt
import matplotlib.ticker as mticker
import numpy as np
import pylab

test_cases = 6
delim = ','
files = 180
runs = 30
generations = 50
row_start = 2
row_end = generations + row_start
time_line = 53
test_cases_str = ["64", "128", "256", "512", "1024", "2048"]
extension = ".dat"
experiment = "tinygp_java"

def graph_statistics(tc_data):
    # if not self.show_graphics:
    #    matplotlib.use('Agg')

    matplotlib.rcParams.update({'font.size': 16})

    lcnt = generations

    fig, ax = plt.subplots(1, 1)
    for i in range(test_cases):
        label_str = test_cases_str[i]
        ax.plot(range(lcnt), tc_data[i, ::, 0], linestyle='-', label=label_str)
    pylab.legend(loc='upper left')
    ax.set_xlabel('Generations')
    ax.set_ylabel('Fitness')
    ax.set_ylim(0, 10000)
    ax.get_xaxis().set_major_formatter(mticker.ScalarFormatter())
    ax.get_yaxis().set_major_formatter(mticker.ScalarFormatter())
    ax.set_title('Average Fitness across generations [' + experiment+ ']')
    #plt.savefig(experiment + '_avg_fitness.png')
    fig.set_size_inches(12, 8)

    plt.savefig(experiment + '_best_fitness.png')
    plt.show()
    plt.close(fig)

    fig, ax = plt.subplots(1, 1)
    for i in range(test_cases):
        label_str = test_cases_str[i]
        ax.plot(range(lcnt), tc_data[i, ::, 1], linestyle='-', label=label_str)
    pylab.legend(loc='upper left')
    ax.set_xlabel('Generations')
    ax.set_ylabel('Fitness')
    ax.set_ylim(0.4, 0.7)
    ax.get_xaxis().set_major_formatter(mticker.ScalarFormatter())
    ax.get_yaxis().set_major_formatter(mticker.ScalarFormatter())
    ax.set_title('Best Fitness across generations [' + experiment+ ']')
    fig.set_size_inches(12, 8)
    plt.savefig(experiment + '_best_fitness.png')
    plt.show()
    plt.close(fig)


if __name__ == "__main__":

    cdir = os.getcwd()
    print(cdir)
    list_files = []
    for i in range(test_cases):
        list_files.append([])
    for file in os.listdir(cdir):
        if file.endswith(extension):
            ind = 0
            for tcs in test_cases_str:
                if file.startswith("console_" + tcs):
                    list_files[ind].append((os.path.join(cdir, file)))
                ind += 1


    print(np.array(list_files))
    print(len(list_files))

    test_case_data = np.zeros((test_cases, runs, generations, 2))
    time_data = np.zeros((test_cases, runs))
    for j in range(test_cases):
        for i in range(runs):
            file = list_files[j][i]
            with open(file) as csv_file:

                csv_reader = csv.reader(csv_file, delimiter=delim)
                line_count = 0
                for row in csv_reader:
                    if row_start <= line_count < row_end:
                        #test_case_data[j][i][line_count - row_start][0] = int(row[0])
                        test_case_data[j][i][line_count - row_start][0] = -float(row[1])
                        test_case_data[j][i][line_count - row_start][1] = -float(row[2])
                    if line_count == time_line - 1:
                        time_data[j][i] = float(row[0].split(":", 1)[1][:-1])
                    line_count += 1

    """
    print(test_case_data)
    print(test_case_data.shape)

    print("Time data")
    print(time_data)
    """

    mean_run_data = np.mean(test_case_data, axis = 1)
    mean_time_data = np.mean(time_data, axis = 1)

    """
    print(mean_run_data)
    print(mean_run_data.shape)

    print("Average Time data")
    print(mean_time_data)
    """
    graph_statistics(mean_run_data)





