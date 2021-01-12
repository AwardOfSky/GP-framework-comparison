# Example:
# fitness function for the pagie polynomial classifier

from tensorgp.engine import *


# import numpy as np


def calc_fit(**kwargs):
    # read parameters
    population = kwargs.get('population')
    generation = kwargs.get('generation')
    tensors = kwargs.get('tensors')
    f_path = kwargs.get('f_path')
    _stf = kwargs.get('stf')
    target = kwargs.get('target')
    dim = kwargs.get('dim')
    display_images = True

    fn = f_path + "gen" + str(generation).zfill(5)
    fitness = []
    times = []
    best_ind = 0

    # set objective function according to min/max
    fit = 0
    condition = lambda: (fit < max_fit)  # minimizing
    max_fit = float('inf')

    for i in range(len(tensors)):

        # save individual (not for
        # if (generation % _stf) == 0 and display_images:
        #    save_image(tensors[i], i, fn, dim)

        # time fitness
        # print(tensors[i].numpy())
        # print(target.numpy())
        start_ind = time.time()
        # print("Dim, ", target.shape)
        fit = tf_rmse(tensors[i], target, target.shape)

        if condition():
            max_fit = fit
            best_ind = i

        times.append((time.time() - start_ind) * 1000.0)
        fitness.append(fit)
        population[i]['fitness'] = fit

    # save best
    # if display_images:
    #    save_image(tensors[best_ind], best_ind, fn, dim)
    if generation == gens:
        save_image(tensors[best_ind], best_ind, fn, dim)
    return population, population[best_ind]


fset = {'max', 'min', 'abs', 'add', 'and', 'or', 'mult', 'sub', 'xor', 'neg', 'cos', 'sin', 'tan', 'sqrt', 'div', 'exp', 'log', 'warp'}
small_fset = {'add', 'sub', 'mult', 'div', 'pow'}
karoo_set = {'add', 'mult', 'sub', 'div', 'cos', 'sin', 'tan', 'abs', 'sign', 'pow'}

if __name__ == "__main__":

    # GP params
    resolution = [512, 512, 3] # 512
    dev = '/gpu:0'  # device to run, write '/cpu_0' to tun on cpu

    gens = 50  # 50
    pop_size = 50  # 50
    tour_size = 3
    mut_rate = 0.1
    cross_rate = 0.9
    max_tree_dep = 10

    seeds = random.randint(0, 2147483647)
    #seeds = 827465

    # problem
    pagie = "mult(scalar(127.5), add(div(scalar(1.0), add(scalar(1.0), div(scalar(1.0), mult(mult(x, x), mult(x, x))))), div(scalar(1.0), add(scalar(1.0), div(scalar(1.0), mult(mult(y, y), mult(y, y)))))))"

    # create engine
    engine = Engine(fitness_func=calc_fit,
                    population_size=pop_size,
                    tournament_size=tour_size,
                    mutation_rate=mut_rate,
                    crossover_rate=cross_rate,
                    max_tree_depth=max_tree_dep,
                    target_dims=resolution,
                    target=pagie,
                    #method='ramped half-and-half',
                    method='full',
                    objective='minimizing',
                    device=dev,
                    stop_criteria='generation',
                    stop_value=gens,
                    effective_dims=2,
                    min_domain=-5,
                    max_domain=5,
                    operators=karoo_set,
                    immigration=10000,
                    seed=seeds,
                    debug=0,
                    save_to_file=10,
                    save_graphics=True,
                    show_graphics=False,
                    read_init_pop_from_file=None)
    # run evolutionary process
    engine.run()
    # engine.generate_pop_images('coswarp.txt')