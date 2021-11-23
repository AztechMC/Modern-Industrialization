from collections import defaultdict
from pathlib import Path

import os
import json
import shutil

path_output = "src/main/resources/data/modern_industrialization/recipes/generated/petrochem/"

shutil.rmtree(path_output, ignore_errors=True)

# Generate the two polymerization recipes
def gen_polymerization(input, output):
    out_dir = path_output + "polymerization/"
    Path(out_dir).mkdir(parents=True, exist_ok=True)

    names = ["lead", "chrome"]
    in_counts = [4, 1]
    out_counts = [300, 700]
    probabilities = [1.0, 0.5]
    for i in range(2):
        with open(out_dir + input + "_" + names[i] + ".json", "w") as f:
            json.dump({
                "type": "modern_industrialization:chemical_reactor",
                "eu": 12,
                "duration": 700,
                "fluid_inputs": [
                    {
                        "fluid": "modern_industrialization:" + input,
                        "amount": 500,
                    }
                ],
                "item_inputs": {
                    "tag": "c:" + names[i] + "_tiny_dusts",
                    "amount": in_counts[i],
                },
                "fluid_outputs": [
                    {
                        "fluid": "modern_industrialization:" + output,
                        "amount": out_counts[i],
                    }
                ],
            }, f, indent=4)

gen_polymerization("ethylene", "polyethylene")
gen_polymerization("vinyl_chloride", "polyvinyl_chloride")
gen_polymerization("caprolactam", "nylon")
gen_polymerization("acrylic_acid", "acrylic_glue")
gen_polymerization("styrene_butadiene", "styrene_butadiene_rubber")
