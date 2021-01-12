import os
import csv
import numpy as np
import matplotlib
import matplotlib.pyplot as plt
import matplotlib.ticker as mticker
import numpy as np
import pylab

#1906840092_stats is the one that copies
# the origginal is saved in the folder original corruped files on fir up

delim = '|'
files = 180
runs = 30
generations = 50
row_start = 0
row_end = generations + row_start
time_line = 53
res_line = 52
test_cases_str = ["64", "128", "256", "512", "1024"]
test_cases = len(test_cases_str)
extension = ".csv"
experiment = "karoo"

avg_nan = 0
avg_inf = 0
std_nan = 0
std_inf = 0
best_nan = 0
best_inf = 0

def graph_statistics(tc_data):
    #if not self.show_graphics:
    #    matplotlib.use('Agg')

    matplotlib.rcParams.update({'font.size': 16})

    lcnt = generations

    do_average = False

    if do_average:
        fig, ax = plt.subplots(1, 1)
        for i in range(test_cases):
            label_str = test_cases_str[i]
            ax.plot(range(lcnt), tc_data[i,::,1], linestyle='-', label=label_str)
        pylab.legend(loc='upper left')
        ax.set_xlabel('Generations')
        ax.set_ylabel('Fitness')
        ax.set_ylim(0, 100000000)
        ax.get_xaxis().set_major_formatter(mticker.ScalarFormatter())
        ax.get_yaxis().set_major_formatter(mticker.ScalarFormatter())
        ax.set_title('Average Fitness across generations [' + experiment+ ']')
        fig.set_size_inches(12, 8)
        #plt.savefig(experiment + '_avg_fitness.png')
        plt.show()
        plt.close(fig)

    fig, ax = plt.subplots(1, 1)
    for i in range(test_cases):
        label_str = test_cases_str[i]
        ax.plot(range(lcnt), tc_data[i,::,3], linestyle='-', label=label_str)
    pylab.legend(loc='upper left')
    ax.set_xlabel('Generations')
    ax.set_ylabel('Fitness')
    ax.set_ylim(0, 10)
    ax.get_xaxis().set_major_formatter(mticker.ScalarFormatter())
    ax.get_yaxis().set_major_formatter(mticker.ScalarFormatter())
    ax.set_title('Best Fitness across generations [' + experiment+ ']')
    fig.set_size_inches(12, 8)
    plt.savefig(experiment + '_best_fitness.png')
    plt.show()
    plt.close(fig)



if __name__ == "__main__":


    total_valid = 0

    cdir = os.getcwd()
    print(cdir)
    list_files = []

    for i in range(test_cases):
        list_files.append([])

    for file in os.listdir(cdir):
        if file.endswith(extension):

            with open(file) as csv_file:
                csv_reader = csv.reader(csv_file, delimiter=delim)
                rows = list(csv_reader)
                #print(file)
                if len(rows) >= 50:
                    res_str = str(file).split("_", 1)[0]
                    seed_str = str(file).split("_")[1]
                    #print(seed_str)
                    #print("Found res: ", res_str)
                    total_valid += 1
                    ind = 0
                    if res_str not in test_cases_str:
                        print("The hell? ", res_str)
                    for tcs in test_cases_str:
                        if res_str == test_cases_str[ind]:
                            list_files[ind].append((os.path.join(cdir, file)))
                        ind += 1

    """
    print(total_valid)
    print(np.array(list_files))
    for l in list_files:
        print(len(l))
    """

    test_case_data = np.zeros((test_cases, runs, generations, 4))
    time_data = np.zeros((test_cases, runs))
    for j in range(test_cases):
        for i in range(runs):
            file = list_files[j][i]

            seed_str = str(file).split("_")[1]
            with open(file) as csv_file:

                csv_reader = csv.reader(csv_file, delimiter=delim)
                line_count = 0
                for row in csv_reader:
                    if row_start <= line_count < row_end:
                        test_case_data[j][i][line_count - row_start][0] = int(row[0])
                        test_case_data[j][i][line_count - row_start][1] = float(row[1]) # avg
                        test_case_data[j][i][line_count - row_start][2] = float(row[2]) # std
                        test_case_data[j][i][line_count - row_start][3] = float(row[3]) # best

                        avg_str = str(float(row[1]))
                        if avg_str == 'nan':
                            avg_nan += 1
                        elif avg_str == 'inf' or avg_str == 'inf':
                            avg_inf += 1

                        std_str = str(float(row[2]))
                        if std_str == 'nan':
                            std_nan += 1
                        elif std_str == 'inf' or std_str == 'inf':
                            std_inf += 1

                        best_str = str(float(row[3]))
                        if best_str == 'nan':
                            best_nan += 1
                        elif best_str == 'inf' or best_str == 'inf':
                            best_inf += 1



                    #if line_count == time_line - 1:
                    #    time_data[j][i] = float(row[0])
                    line_count += 1

            time_file = seed_str + "_stats.csv"
            with open(time_file) as csv_file:
                csv_reader = csv.reader(csv_file, delimiter=delim)
                rows = list(csv_reader)
                time_temp = float(rows[2][0])
                print(time_temp, "on file", time_file)
                time_data[j][i] = float(time_temp)



    print("start Nan inf stats: ")
    print("avg_nan :", avg_nan)
    print("avg_inf :", avg_inf)
    print("std_nan :", std_nan)
    print("std_inf :", std_inf)
    print("best_nan :", best_nan)
    print("best_inf :", best_inf)
    print("End nan inf stats")

    #print(test_case_data)
    #print(test_case_data.shape)

    #print("Time data")
    #print(time_data)

    mean_run_data = np.mean(test_case_data, axis = 1)
    mean_time_data = np.mean(time_data, axis = 1)


    print("Average Time data")
    print(mean_run_data)
    print(mean_run_data.shape)

    graph_statistics(mean_run_data)


