# tiny genetic programming by © moshe sipper, www.moshesipper.com
import csv
import os
from random import random, randint, seed, choice
from statistics import mean
from copy import deepcopy
import numpy as np
import math

POP_SIZE = 50  # population size
MIN_DEPTH = 2  # minimal initial random tree depth
MAX_DEPTH = 10  # maximal initial random tree depth
GENERATIONS = 50  # maximal number of generations to run evolution
TOURNAMENT_SIZE = 3  # size of tournament for tournament selection
XO_RATE = 0.9  # crossover rate
PROB_MUTATION = 0.05  # per-node mutation probability

def add(x, y): return x + y
def sub(x, y): return x - y
def mul(x, y): return x * y
def div(x, y): return 0 if y == 0 else (x / y)
def _pow(x, y):
    #print("Attempting pow between ", x, "and", y)
    return 0 if x == 0 else (min(abs(x), 100) ** min(abs(y), 100))
def cos(x): return math.cos(x * math.pi)
def sin(x): return math.sin(x * math.pi)
def tan(x): return 0 if (x == math.pi * 0.5) else math.tan(x * math.pi)
def _abs(x): return abs(x)
def sig(x): return math.copysign(1, x)

# FUNCTIONS = [add, sub, mul]
# TERMINALS = ['x', -2, -1, 0, 1, 2]

max_prob_mutation = 2
min_dom = -5
max_dom = 5
domain_delta = max_dom - min_dom
dim = 2
FUNCTIONS = {add:2, sub:2, mul:2, div:2, _pow:2, cos:1, sin:1, tan:1, _abs:1, sig:1}
TERMINALS_S = ['x', 'y', 'scalar']
TERMINALS = dict(zip(iter(TERMINALS_S), range(len(TERMINALS_S)))) # put indeces

def clamp(a, n, b,):
    return max(min(n, b), a)

def target_func(x, y):  # evolution's target
    return (1 / (1 + (1 / (x ** 4)))) + (1 / (1 + (1 / (y ** 4))))

def generate_dataset():  # generate 101 data points from target_func
    dataset = []
    print(resolution)
    for x in np.linspace(min_dom, max_dom, resolution):
        for y in np.linspace(min_dom, max_dom, resolution):
            dataset.append([x, y, target_func(x, y)])
    #print("Initial dataset: ", np.reshape(dataset, (resolution, resolution, 3)))
    return dataset

class GPTree:
    def __init__(self, data=None, children=[]):
        #print("fui chamado!")
        self.data = data
        self.children = children

    def node_label(self):  # string label
        if self.data in FUNCTIONS:
            return self.data.__name__
        else:
            return str(self.data)

    def print_tree(self, prefix=""):  # textual printout
        label = self.node_label()

        if label != 'scalar':
            print("%s%s" % (prefix, label))
            for c in self.children:
                c.print_tree(prefix + "...")
        else:
            print("%s%s" % (prefix, str(self.children[0])))

    def get_str(self):
        if self.data in FUNCTIONS:
            mstr = self.node_label() + "("
            for c in range(len(self.children)):
                mstr += self.children[c].get_str()
                if c < len(self.children) - 1:
                    mstr += ", "
            return mstr + ")"
        else:
            mstr = self.node_label()
            if mstr == 'scalar':
                return str(self.children[0])
            else:
                return mstr

    def compute_tree(self, term_vars):
        #print("x, y =", term_vars[0], ",", term_vars[1])
        if self.data in FUNCTIONS:
            #return self.data(self.left.compute_tree(x), self.right.compute_tree(x))
            arg_list = [c.compute_tree(term_vars) for c in self.children]
            #print("Argument list debug!")
            #print(arg_list)
            res = self.data(*arg_list)
            res = clamp(-2147483647, res, 2147483647) # clamp to prevent overflow on _pow


            #print("checking result for ", self.node_label(), " with arguments ", arg_list, ":", res)
            return res

        elif self.data in TERMINALS:
            if self.node_label() != 'scalar':
                return term_vars[TERMINALS[self.data]]
            else:
                return self.children[0]

    def generate_random_terminal(self):
        rnd = randint(0, len(TERMINALS) - 1)
        if rnd == dim:  # means we are in scalar
            self.data = TERMINALS_S[dim]
            self.children = [random() * domain_delta + min_dom]
        else:
            #print("print ranodm")
            #print(rnd)
            self.data = TERMINALS_S[rnd]
            self.children = []

    def random_tree(self, grow, max_depth, depth=0):  # create random tree using either grow or full method
        if depth < MIN_DEPTH or (depth < max_depth and not grow):
            #print("Eu devia estar aqui2")
            #self.data = FUNCTIONS[randint(0, len(FUNCTIONS) - 1)]
            self.data = choice(list(FUNCTIONS))

        elif depth >= max_depth:
            #print("Eu devia estar aqui!1")
            self.generate_random_terminal()
        else:  # intermediate depth, grow
            #print("Eu devia estar aqui!")
            if random() > 0.5:
                self.generate_random_terminal()
            else:
                #self.data = FUNCTIONS[randint(0, len(FUNCTIONS) - 1)]
                self.data = choice(list(FUNCTIONS))
        if self.data in FUNCTIONS:
            children = []
            for c in range(FUNCTIONS[self.data]):
                t = GPTree()
                t.random_tree(grow, max_depth, depth=depth + 1)
                children.append(t)
            self.children = children


    def mutation(self):
        if random() < PROB_MUTATION:  # mutate at this node
            self.random_tree(grow=True, max_depth=max_prob_mutation)
            #print("Like like this")
        else:
            if self.node_label() != 'scalar':
                # print("Node labelk:", c.node_label(), ", ", str(c.children))
                for c in self.children:
                    c.mutation()
            else:
                self.generate_random_terminal()


    def size(self):  # tree size in nodes
        if self.data in TERMINALS_S: return 1
        cnt = 0
        for c in self.children:
            cnt += c.size()
        return 1 + cnt

    def build_subtree(self):  # count is list in order to pass "by reference"
        t = GPTree()
        t.data = self.data
        t.children = []
        #print("Enetered node", self.node_label())

        if self.node_label() != 'scalar':
            for c in self.children:
                #print("Enetering node ", c.node_label())
                t.children.append(c.build_subtree())
        else:
            t.children = [self.children[0]]

        return deepcopy(t)

    def scan_tree(self, count, second):  # note: count is list, so it's passed "by reference"
        count[0] -= 1
        #print("Ok, entao entramos aqui uma vez")
        if count[0] <= 1:
            if not second:  # return subtree rooted here
                #print("Depois vamos aqui")
                return self.build_subtree()
            else:  # glue subtree here
                self.data = second.data
                self.children = second.children
                #self.left = second.left
                #self.right = second.right
                #for s in second.children:
                #    self.children.append(s)
        else:
            ret = None
            #if self.left and count[0] > 1: ret = self.left.scan_tree(count, second)
            #if self.right and count[0] > 1: ret = self.right.scan_tree(count, second)
            if self.node_label() != 'scalar':
                for c in self.children:
                    if count[0] > 1:
                        ret = c.scan_tree(count, second)
            return ret

    def get_depth(self, dl = 0): # depth level = 1 -> count nodes, 0 -> count edges
        return max(dl + 1 if c.node_label() in TERMINALS_S else c.get_depth(dl + 1) for c in self.children) if self.data in FUNCTIONS else 0

    def crossover(self, other):  # xo 2 trees at random nodes
        #if random() < XO_RATE:

        #print("Making crossover between: \n")
        #self.print_tree()
        #print("and\n")
        #other.print_tree()
        #print("\n\n\n")

        count = [randint(1, other.size())]
        #count = [2]
        #print("FandF, this is the count list:")
        #print(count)
        second = other.scan_tree(count, None)  # 2nd random subtree
        #print("E ja esta?")


        #print("Ok, now let's go to the second print")
        #second.print_tree()
        #print("Print second is over")

        #print("Making crossover between: \n","and:\n", other.print_tree())

        self.scan_tree([randint(1, self.size())], second)  # 2nd subtree "glued" inside 1st tree


# end class GPTree

def init_population():  # ramped half-and-half
    pop = []

    divisions = MAX_DEPTH - (MIN_DEPTH - 1)
    parts = math.floor(POP_SIZE / divisions)
    last_part = POP_SIZE - (divisions - 1) * parts
    load_balance_index = (MAX_DEPTH + 1) - (last_part - parts)
    num_parts = parts
    mfull = math.floor(num_parts / 2)

    for md in range(MIN_DEPTH, MAX_DEPTH + 1):

        if md == load_balance_index:
            num_parts += 1
            mfull += 1
        do_grow = False
        for i in range(num_parts):
            if i >= mfull:
                do_grow = True
            t = GPTree()
            t.random_tree(grow=do_grow, max_depth=md)  # grow
            pop.append(t)

    print("Population:", len(pop))

    return pop


def fitness(individual, dataset):  # RMSE
    return math.sqrt(mean([((individual.compute_tree(ds[:dim]) - ds[dim]) ** 2) for ds in dataset]))
    #return math.sqrt(mean([((0.0 - ds[dim]) ** 2) for ds in dataset])) # passed fitness test

def selection(population, fitnesses):  # select one individual using tournament selection
    tournament = [randint(0, len(population) - 1) for i in range(TOURNAMENT_SIZE)]  # select tournament contenders
    tournament_fitnesses = [fitnesses[tournament[i]] for i in range(TOURNAMENT_SIZE)]

    """
    print("[sel] This are the tournament fitnesses: ", tournament_fitnesses)
    ind = population[tournament[tournament_fitnesses.index(max(tournament_fitnesses))]]
    print("[sel] This is the selected ind")

    ind.print_tree()

    print("[sel] This is the final ind")
    final = deepcopy(ind)
    final.print_tree()
    print("[sel] ending final ind print\n\n\n\n\n\n\n")
    """

    #return final
    return deepcopy(population[tournament[tournament_fitnesses.index(min(tournament_fitnesses))]])

def print_pop(pop):
    for p in pop:
        p.print_tree()
        print("Depth:", str(p.get_depth()))
        print()


def print_stats(fits, gen, tree):
    _fp = os.getcwd() + '/runs/' + str(rseed) + '_stats.csv'
    with open(_fp, mode='a', newline='') as file:
        fwriter = csv.writer(file, delimiter='|')

        if gen == 0:
            print("[gen, fit_avg, fit_std, fit_best, best_tree]")
            fwriter.writerow(["[gen, fit_avg, fit_std, fit_best, best_tree]"])
        fit_avg = np.average(fits)
        fit_std = np.std(fits)
        fit_best = np.min(fits)

        fwriter.writerow([gen, fit_avg, fit_std, fit_best, tree])
        print(gen, ",", fit_avg, ",", fit_std, ",", fit_best, ",", tree)

def main():
    # init stuff
    #seed(24987)# init internal state of random number generator
    print("Random seed", str(rseed))
    seed(rseed)
    dataset = generate_dataset()
    population = init_population()

    print("Initial Population: \n\n\n")
    print_pop(population)
    print("End initial population")

    best_of_run = None
    best_of_run_f = float('inf')
    best_of_run_gen = 0
    fitnesses = [fitness(population[i], dataset) for i in range(POP_SIZE)]

    #print("Fitnesses debug!")
    #print(fitnesses[0])

    # go evolution!
    for gen in range(GENERATIONS):
        nextgen_population = []
        for i in range(POP_SIZE):

            member_depth = float('inf')
            rcnt = 0
            while member_depth > MAX_DEPTH:
                parent1 = selection(population, fitnesses)
                parent2 = selection(population, fitnesses)
                parent1.crossover(parent2)
                parent1.mutation()
                #print("Parent1:")
                #parent1.print_tree()
                member_depth = parent1.get_depth()
                rcnt += 1
            #print("recounts: ", str(rcnt))

            nextgen_population.append(parent1)
        population = nextgen_population
        fitnesses = [fitness(population[i], dataset) for i in range(POP_SIZE)]


        #print("fitnesses for gen ", gen, ":", fitnesses)
        if min(fitnesses) < best_of_run_f:
            best_of_run_f = min(fitnesses)
            best_of_run_gen = gen
            best_of_run = deepcopy(population[fitnesses.index(min(fitnesses))])
            print("________________________")
            print("gen:", gen, ", best_of_run_f:", round(min(fitnesses), 3), ", best_of_run:")
            best_of_run.print_tree()

        print_stats(fitnesses, gen, best_of_run.get_str())

    print("\n\n_________________________________________________\nEND OF RUN\nbest_of_run attained at gen " + str(
        best_of_run_gen) + \
          " and has f=" + str(round(best_of_run_f, 3)))
    best_of_run.print_tree()


if __name__ == "__main__":

    runs = 30
    test_cases = [64, 128, 256, 512, 1024, 2048]

    #runs = 1
    #test_cases = [64]


    for i in range(runs):
        for res in test_cases:
            #rseed = randint(0, 2147483647)
            rseed = 1190942560
            resolution = res
            main()

#TODO: o best não está na proxima geraçao?
#TODO: fazer prints bonitos