import os
from pathlib import Path
import json

path_output = "src/main/resources/data/modern_industrialization/recipes/generated/planks/"
Path(path_output).mkdir(parents=True, exist_ok=True)

wood_types = ["oak_logs", "spruce_logs", "birch_logs", "jungle_logs",
              "acacia_logs", "dark_oak_logs", "crimson_stems", "warped_stems"]

for wood_type in wood_types:
    json_output = {}
    json_output["type"] = "modern_industrialization:cutting_machine"
    json_output["eu"] = 2
    json_output["duration"] = 200
    json_output["fluid_inputs"] = {"fluid": "minecraft:water", "amount": 1}
    json_output["item_inputs"] = {"tag": "minecraft:"+wood_type, "amount": 1}
    json_output["item_outputs"] = {"item": "minecraft:"+wood_type.rsplit("_", 1)[0]
                                   + "_planks", "amount": 4}
    json.dump(json_output, open(path_output+wood_type.rsplit("_", 1)[0]+".json", "w"), indent=4)