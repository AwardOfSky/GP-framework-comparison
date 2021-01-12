import time
import random
import numpy as np

n = 1000000
raw = True
_np_ = True

if raw:
    array = [random.randint(-2147483647, 2147483647) for i in range(n)]
    if len(array) < 10: print(array)

    s = time.time()
    for i in range(n - 1, -1, -1):
        for j in range(i):
            if array[j] > array[j + 1]:
                array[j], array[j + 1] = array[j + 1], array[j]
    t = time.time() - s

    if len(array) < 10: print(array)
    print("Raw took", str(t), "s")

if _np_:
    array = [random.randint(-2147483647, 2147483647) for i in range(n)]
    s = time.time()
    array = np.sort(array)
    t = time.time() - s
    print("Numpy took", str(t), "s")

