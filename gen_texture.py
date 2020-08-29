from glob import glob
from PIL import Image
from PIL.ImageOps import grayscale, colorize
from pathlib import Path

import os
import json

MC = "minecraft:"
MI = "modern_industrialization:"


def image_tint(src, tint='#ffffff'):
    src = Image.open(src).convert('RGBA')
    r, g, b, alpha = src.split()
    gray = grayscale(src)
    result = colorize(gray, (0, 0, 0, 0), tint)
    result.putalpha(alpha)
    return result


def gen_texture(id, hex, vanilla, ore):

    item = glob("template/item/*.png")
    if not vanilla:
        item.extend(glob("template/item_vanilla/*.png"))

    if ore:
        item.append("template/crushed_dust.png")

    for filename in item:
        output_path = ("src/main/resources/assets/modern_industrialization/textures/items/materials/" +
                       id + "/" + os.path.basename(filename))
        try:
            os.mkdir(
                "src/main/resources/assets/modern_industrialization/textures/items/materials/" + id)
        except:
            pass
        print(filename)
        result = image_tint(filename, hex)
        result.save(output_path)

    if not vanilla:
        result_block = image_tint("template/block.png", hex)
        try:
            os.mkdir(
                "src/main/resources/assets/modern_industrialization/textures/blocks/materials/" + id)
        except:
            pass

        result_block.save(
            "src/main/resources/assets/modern_industrialization/textures/blocks/materials/" + id + "/block.png")

        if ore:
            result_ore = image_tint("template/ore.png", hex)
            stone = Image.open("template/stone.png")
            Image.alpha_composite(stone, result_ore).save(
                "src/main/resources/assets/modern_industrialization/textures/blocks/materials/" + id + "/ore.png")


def getIdentifier(id, item_type, vanilla):
    if item_type == "pipe_item" or item_type == "pipe_fluid":
        return MI + item_type + "_" + id
    if not vanilla:
        return MI + id + '_' + item_type
    elif item_type == "ore" or item_type == "block" or item_type == "ingot" or item_type == "nugget":
        return MC + id + '_' + item_type
    else:
        return MI + id + '_' + item_type


def genForgeHammer(id, vanilla, ore):
    forge_hammer_path = "src/main/resources/data/modern_industrialization/recipes/generated/forge_hammer/" + id
    Path(forge_hammer_path).mkdir(parents=True, exist_ok=True)

    hammer = "modern_industrialization:forge_hammer_hammer"
    saw = "modern_industrialization:forge_hammer_saw"

    list_todo = [('plate', 'curved_plate', 1, hammer),
                 ('double_ingot', 'plate', 1, hammer),
                 ('nugget', 'small_dust', 1, hammer),
                 ('ingot', 'rod', 1, saw),
                 ('large_plate', 'gear', 1, saw),
                 ('rod', 'bolt', 1, saw),
                 ('pipe_item', 'ring', 3, saw)]

    if ore:
        list_todo.append(('ore', 'crushed_dust', 2, hammer))

    for a, b, c, d in list_todo:
        json_file = {}
        json_file["type"] = d
        json_file["eu"] = 1
        json_file["duration"] = 1
        json_file["item_inputs"] = {
            "item": getIdentifier(id, a, vanilla), "amount": 1}
        json_file["item_outputs"] = {
            "item": getIdentifier(id, b, vanilla), "amount": c}

        with open(forge_hammer_path + "/" + b + ".json", "w") as file:
            json.dump(json_file, file, indent=4)


def genCraft(id, vanilla, ore):
    path = "src/main/resources/data/modern_industrialization/recipes/generated/materials/" + id
    Path(path).mkdir(parents=True, exist_ok=True)

    list_full = [('small_dust', 'dust')]

    if not vanilla:
        list_full.append(('nugget', 'ingot'))
        list_full.append(('ingot', 'block'))

    for (a, b) in list_full:
        jsonf = {}
        jsonf["type"] = "minecraft:crafting_shaped"
        jsonf["pattern"] = ["###", "###", "###"],
        jsonf["key"] = {"#": {"item": getIdentifier(id, a, vanilla)}}
        jsonf["result"] = {"item": getIdentifier(id, b, vanilla)}
        with open(path + "/" + b + "_from_" + a + ".json", "w") as file:
            json.dump(jsonf, file, indent=4)

        jsonf = {}
        jsonf["type"] = "minecraft:crafting_shapeless"
        jsonf["ingredients"] = [{"item": getIdentifier(id, b, vanilla)}]
        jsonf["result"] = {"item": getIdentifier(id, a, vanilla), "count": 9}
        with open(path + "/" + a + "_from_" + b + ".json", "w") as file:
            json.dump(jsonf, file, indent=4)

    # blade
    jsonf = {}
    jsonf["type"] = "minecraft:crafting_shaped"
    jsonf["pattern"] = [
        "#",
        "#",
        "I"
    ],
    jsonf["key"] = {
        "#": {
            "item": getIdentifier(id, "plate", vanilla)
        },
        "I": {
            "item": getIdentifier(id, "rod", vanilla)
        }
    }
    jsonf["result"] = {
        "item": getIdentifier(id, "blade", vanilla),
        "count": 4
    }
    with open(path + "/blade.json", "w") as file:
        json.dump(jsonf, file, indent=4)

    # double_ingot
    jsonf = {}
    jsonf["type"] = "minecraft:crafting_shaped"
    jsonf["pattern"] = [
        "#",
        "#"
    ],
    jsonf["key"] = {
        "#": {
            "item": getIdentifier(id, "ingot", vanilla)
        }
    }
    jsonf["result"] = {
        "item": getIdentifier(id, "double_ingot", vanilla),
        "count": 1
    }
    with open(path + "/double_ingot.json", "w") as file:
        json.dump(jsonf, file, indent=4)

    # large_plate
    jsonf = {}
    jsonf["type"] = "minecraft:large_plate"
    jsonf["pattern"] = [
        "##",
        "##"
    ],
    jsonf["key"] = {
        "#": {
            "item": getIdentifier(id, "plate", vanilla)
        }
    }
    jsonf["result"] = {
        "item": getIdentifier(id, "large_plate", vanilla),
        "count": 1
    }
    with open(path + "/large_plate.json", "w") as file:
        json.dump(jsonf, file, indent=4)

    # rotor
    jsonf = {}
    jsonf["type"] = "minecraft:crafting_shaped"
    jsonf["pattern"] = [
        "bBb",
        "BRB",
        "bBb"
    ],
    jsonf["key"] = {
        "b": {
            "item": getIdentifier(id, "bolt", vanilla)
        },
        "B": {
            "item": getIdentifier(id, "blade", vanilla)
        },
        "R": {
            "item": getIdentifier(id, "ring", vanilla)
        }
    }
    jsonf["result"] = {
        "item": getIdentifier(id, "rotor", vanilla),
        "count": 1
    }
    with open(path + "/rotor.json", "w") as file:
        json.dump(jsonf, file, indent=4)

    # pipes
    for fluid in [True, False]:
        jsonf = {}
        jsonf["type"] = "minecraft:crafting_shaped"
        jsonf["pattern"] = [
            "###",
            "   " if not fluid else " g ",
            "###"]
        ingredients = {
            "#": {
                "item": getIdentifier(id, "curved_plate", vanilla)
            },
        }
        if fluid:
            ingredients["g"] = {"item": "minecraft:glass"}
        jsonf["key"] = ingredients
        jsonf["result"] = {
            "item": getIdentifier(id, "pipe_fluid" if fluid else "pipe_item", vanilla),
            "count": 6
        }
        with open(path + "/" + ("pipe_fluid" if fluid else "pipe_item") + ".json", "w") as file:
            json.dump(jsonf, file, indent=4)


def genSmelting(id, vanilla, ore):
    path = "src/main/resources/data/modern_industrialization/recipes/generated/materials/" + id + "/smelting/"
    Path(path).mkdir(parents=True, exist_ok=True)

    list_todo = [('small_dust', 'nugget', 0.08), ('dust', 'ingot', 0.7)]
    if ore:
        list_todo.append(['crushed_dust', 'ingot', 0.7])
        if not vanilla:
            list_todo.append(['ore', 'ingot', 0.7])

    for a, b, c in list_todo:
        for d, e in [("smelting", 200), ("blasting", 100)]:
            jsonf = {}
            jsonf["type"] = MC + d
            jsonf["ingredient"] = {"item": getIdentifier(id, a, vanilla)}
            jsonf["result"] = getIdentifier(id, b, vanilla)
            jsonf["experience"] = c
            jsonf["cookingtime"] = e
            with open(path + "/" + a + "_" + d + ".json", "w") as file:
                json.dump(jsonf, file, indent=4)


def gen(id, hex, vanilla, ore, forge_hammer):
    gen_texture(id, hex, vanilla, ore)
    if forge_hammer:
        genForgeHammer(id, vanilla, ore)
    genCraft(id, vanilla, ore)
    genSmelting(id, vanilla, ore)


if __name__ == '__main__':
    gen('gold', '#ffe100', True, True, False)
    gen('iron', '#f0f0f0', True, True, True)
    gen('copper', '#ff6600', False, True, True)
    gen('bronze', '#ffcc00', False, False, True)
    gen('tin', '#cbe4e4', False, True, True)
    gen('steel', '#3f3f3f', False, False, False)
    gen('aluminum', '#3fcaff', False, True, False)
