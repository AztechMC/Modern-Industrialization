from collections import defaultdict
from pathlib import Path

import os
import json
import shutil

path_output = "src/main/resources/data/modern_industrialization/recipes/generated/petrochem/"

shutil.rmtree(path_output, ignore_errors=True)

# Generate all the distillation recipes, TODO: and the distillation tower
def gen_distillation(eu, duration, input, outputs):
    def split_fluid(x):
        amount, fluid = x.split()
        return int(amount), fluid

    in_amount, in_fluid = split_fluid(input)

    out_dir = path_output + in_fluid + "_distillation/"
    Path(out_dir).mkdir(parents=True, exist_ok=True)

    i = 0
    for out_amount, out_fluid in map(split_fluid, outputs):
        with open(out_dir + str(i) + ".json", "w") as f:
            json.dump({
                "type": "modern_industrialization:distillery",
                "eu": eu,
                "duration": duration,
                "fluid_inputs": {
                    "fluid": "modern_industrialization:" + in_fluid,
                    "amount": in_amount,
                },
                "fluid_outputs": {
                    "fluid": "modern_industrialization:" + out_fluid,
                    "amount": out_amount,
                }
            }, f, indent=4)
        i += 1
    with open(out_dir + "full.json", "w") as f:
        json.dump({
            "type": "modern_industrialization:distillation_tower",
            "eu": eu * len(outputs),
            "duration": duration,
            "fluid_inputs": {
                "fluid": "modern_industrialization:" + in_fluid,
                "amount": in_amount,
            },
            "fluid_outputs": [{
                "fluid": "modern_industrialization:" + out_fluid,
                "amount": out_amount
            } for out_amount, out_fluid in map(split_fluid, outputs)],
        }, f, indent=4)


# Generate the Sulfuric X to X recipe
def gen_sulfuric(fluid):
    out_dir = path_output + "sulfuric_purification/"
    Path(out_dir).mkdir(parents=True, exist_ok=True)

    with open(out_dir + fluid + ".json", "w") as f:
        json.dump({
            "type": "modern_industrialization:chemical_reactor",
            "eu": 16,
            "duration": 400,
            "fluid_inputs": [
                {
                    "fluid": "modern_industrialization:sulfuric_" + fluid,
                    "amount": 12000,
                },
                {
                    "fluid": "modern_industrialization:hydrogen",
                    "amount": 2000,
                },
            ],
            "fluid_outputs": [
                {
                    "fluid": "modern_industrialization:" + fluid,
                    "amount": 12000,
                },
                {
                    "fluid": "modern_industrialization:sulfuric_acid",
                    "amount": 2000,
                },
            ],
        }, f, indent=4)


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


gen_distillation(12, 200, "1000 crude_oil", [
    "500 sulfuric_light_fuel",
    "200 sulfuric_heavy_fuel",
    "300 sulfuric_naphtha",
])
gen_distillation(15, 200, "1000 steam_cracked_naphtha", [
    "150 methane",
    "50 acetylene",
    "250 ethylene",
    "75 propene",
    "125 butadiene",
    "150 benzene",
    "100 toluene",
    "100 ethylbenzene",
])
gen_distillation(10, 200, "1000 shale_oil", [
    "50 helium",
    "450 sulfuric_crude_oil",
    "500 sulfuric_naphtha",
])

gen_sulfuric("crude_oil")
gen_sulfuric("heavy_fuel")
gen_sulfuric("light_fuel")
gen_sulfuric("naphtha")

gen_polymerization("ethylene", "polyethylene")
gen_polymerization("vinyl_chloride", "polyvinyl_chloride")
gen_polymerization("caprolactam", "nylon")
gen_polymerization("acrylic_acid", "acrylic_glue")
gen_polymerization("styrene_butadiene", "styrene_butadiene_rubber")
