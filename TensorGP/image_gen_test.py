from tensorgp.engine import *

if __name__ == "__main__":

    Engine(target_dims = [64, 64]).generate_pop_images('images_file.txt', 'images_dir/')