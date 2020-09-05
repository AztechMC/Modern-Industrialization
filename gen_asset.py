from glob import glob
from PIL import Image
from PIL.ImageOps import grayscale, colorize
from pathlib import Path

import os
import json

C = "c:"
MC = "minecraft:"
MI = "modern_industrialization:"


def image_tint(src, tint='#ffffff'):
    src = Image.open(src).convert('RGBA')
    r, g, b, alpha = src.split()
    gray = grayscale(src)
    result = colorize(gray, (0, 0, 0, 0), tint)
    result.putalpha(alpha)
    return result


def gen_texture(id, hex, item_set, block_set, special_texture=''):

    item = glob("template/item/*.png")

    output_path = (
        "src/main/resources/assets/modern_industrialization/textures/items/materials/" + id)
    Path(output_path).mkdir(parents=True, exist_ok=True)

    for filename in item:
        t = os.path.basename(filename).split('.')[0]
        if t in item_set:
            print(filename)
            result = image_tint(filename, hex)
            if t != special_texture:
                result.save(output_path + '/' + os.path.basename(filename))
            else:
                result.save(output_path + '/' + t + ".png")

    block = glob("template/block/*.png")
    output_path = "src/main/resources/assets/modern_industrialization/textures/blocks/materials/" + id
    Path(output_path).mkdir(parents=True, exist_ok=True)

    for filename in block:
        t = os.path.basename(filename).split('.')[0]
        if t in block_set:
            if t == 'ore':
                result_ore = image_tint(filename, hex)
                stone = Image.open("template/block/stone.png")
                Image.alpha_composite(stone, result_ore).save(
                    output_path + "/ore.png")
            else:
                result_block = image_tint(filename, hex)
                result_block.save(output_path + '/' +
                                  os.path.basename(filename))


def getIdentifier(id, item_type, vanilla=False, isMetal=True):
    if item_type == "pipe_item" or item_type == "pipe_fluid":
        return MI + item_type + "_" + id

    if item_type == "main":  # ingot for metal, gem for gem, ...
        return (MC if vanilla else MI) + (id + "_ingot" if isMetal else id)

    if not vanilla:
        return MI + id + '_' + item_type
    elif item_type == "ore" or item_type == "block" or item_type == "nugget":
        return MC + id + '_' + item_type
    else:
        return MI + id + '_' + item_type

def getTypeTags(id):
    return ["ore"] if id in TAG_BLACKLIST else ['main', 'nugget', 'ore']

def addItemInput(itemInputs, id, item_type, vanilla=False, isMetal=True):
    if not vanilla and isMetal and item_type in getTypeTags(id):
        itemInputs["tag"] = C + id + "_" + ("ingot" if item_type == "main" else item_type) + "s"
    else:
        itemInputs["item"] = getIdentifier(id, item_type, vanilla, isMetal)


def genForgeHammer(id, vanilla, item_set, isMetal):
    forge_hammer_path = "src/main/resources/data/modern_industrialization/recipes/generated/materials/" + id + "/forge_hammer"
    Path(forge_hammer_path).mkdir(parents=True, exist_ok=True)

    hammer = "modern_industrialization:forge_hammer_hammer"
    saw = "modern_industrialization:forge_hammer_saw"

    list_todo = [('large_plate', 'curved_plate', 3, hammer),
                 ('double_ingot', 'plate', 1, hammer),
                 ('nugget', 'small_dust', 1, hammer),
                 ('main', 'rod', 1, saw),
                 ('large_plate', 'gear', 1, saw),
                 ('rod', 'bolt', 1, saw),
                 ('ore', 'crushed_dust', 2, hammer),
                 ('pipe_item', 'ring', 1, saw)]

    for a, b, c, d in list_todo:
        if a in item_set and b in item_set:
            json_file = {}
            json_file["type"] = d
            json_file["eu"] = 1
            json_file["duration"] = 1
            json_file["item_inputs"] = {"amount": 1}
            addItemInput(json_file["item_inputs"], id, a, vanilla, isMetal)
            json_file["item_outputs"] = {
                "item": getIdentifier(id, b, vanilla, isMetal), "amount": c}

            with open(forge_hammer_path + "/" + b + ".json", "w") as file:
                json.dump(json_file, file, indent=4)


def genCraft(id, vanilla, item_set, isMetal, pipe):
    path = "src/main/resources/data/modern_industrialization/recipes/generated/materials/" + id + "/craft"
    Path(path).mkdir(parents=True, exist_ok=True)

    list_full = [('small_dust', 'dust')]

    if not vanilla:
        list_full.append(('nugget', 'main'))
        list_full.append(('main', 'block'))

    for (a, b) in list_full:
        if a in item_set and b in item_set:
            jsonf = {}
            jsonf["type"] = "minecraft:crafting_shaped"
            jsonf["pattern"] = ["###", "###", "###"]
            jsonf["key"] = {
                "#": {"item": getIdentifier(id, a, vanilla, isMetal)}}
            jsonf["result"] = {"item": getIdentifier(id, b, vanilla, isMetal)}
            with open(path + "/" + b + "_from_" + a + ".json", "w") as file:
                json.dump(jsonf, file, indent=4)

            jsonf = {}
            jsonf["type"] = "minecraft:crafting_shapeless"
            jsonf["ingredients"] = [
                {"item": getIdentifier(id, b, vanilla, isMetal)}]
            jsonf["result"] = {"item": getIdentifier(
                id, a, vanilla, isMetal), "count": 9}
            with open(path + "/" + a + "_from_" + b + ".json", "w") as file:
                json.dump(jsonf, file, indent=4)

    # blade
    if 'blade' in item_set:
        jsonf = {}
        jsonf["type"] = "minecraft:crafting_shaped"
        jsonf["pattern"] = [
            "#",
            "#",
            "I"
        ]
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
    if 'double_ingot' in item_set:
        jsonf = {}
        jsonf["type"] = "minecraft:crafting_shaped"
        jsonf["pattern"] = [
            "#",
            "#"
        ]
        ingredient = {}
        addItemInput(ingredient, id, "main", vanilla, isMetal)
        jsonf["key"] = {
            "#": ingredient
        }
        jsonf["result"] = {
            "item": getIdentifier(id, "double_ingot", vanilla),
            "count": 1
        }
        with open(path + "/double_ingot.json", "w") as file:
            json.dump(jsonf, file, indent=4)

        # large_plate
        jsonf = {}
        jsonf["type"] = "minecraft:crafting_shaped"
        jsonf["pattern"] = [
            "##",
            "##"
        ]
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
    if 'rotor' in item_set:
        jsonf = {}
        jsonf["type"] = "minecraft:crafting_shaped"
        jsonf["pattern"] = [
            "bBb",
            "BRB",
            "bBb"
        ]
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
    if pipe:
        for fluid in [True, False]:
            jsonf = {}
            jsonf["type"] = "minecraft:crafting_shaped"
            jsonf["pattern"] = [
                "###",
                "   " if not fluid else "ggg",
                "###"]
            ingredients = {
                "#": {
                    "item": getIdentifier(id, "curved_plate", vanilla)
                },
            }
            if fluid:
                ingredients["g"] = {"item": "minecraft:glass_pane"}
            jsonf["key"] = ingredients
            jsonf["result"] = {
                "item": getIdentifier(id, "pipe_fluid" if fluid else "pipe_item", vanilla),
                "count": 6
            }
            with open(path + "/" + ("pipe_fluid" if fluid else "pipe_item") + ".json", "w") as file:
                json.dump(jsonf, file, indent=4)


def genSmelting(id, vanilla, item_set, isMetal):
    path = "src/main/resources/data/modern_industrialization/recipes/generated/materials/" + id + "/smelting/"
    Path(path).mkdir(parents=True, exist_ok=True)

    list_todo = [('small_dust', 'nugget', 0.08),
                 ('crushed_dust', 'main', 0.7),  ('dust', 'main', 0.7)]

    if not vanilla:
        list_todo.append(('ore', 'main', 0.7))

    for a, b, c in list_todo:
        if a in item_set and b in item_set:
            list_recipe = [("smelting", 200)]
            if isMetal:
                list_recipe.append(('blasting', 100))
            for d, e in list_recipe:
                jsonf = {}
                jsonf["type"] = MC + d
                jsonf["ingredient"] = {"item": getIdentifier(id, a, vanilla)}
                jsonf["result"] = getIdentifier(id, b, vanilla, isMetal)
                jsonf["experience"] = c
                jsonf["cookingtime"] = e
                with open(path + "/" + a + "_" + d + ".json", "w") as file:
                    json.dump(jsonf, file, indent=4)


def genMacerator(id, vanilla, item_set, isMetal):
    path = "src/main/resources/data/modern_industrialization/recipes/generated/materials/" + id + "/macerator"
    Path(path).mkdir(parents=True, exist_ok=True)

    mac = "modern_industrialization:macerator"

    if 'dust' in item_set:
        list_todo = [('main', 9), ('double_ingot', 18), ('plate', 9), ('curved_plate', 9),
                     ('nugget', 1), ('large_plate', 36), ('gear', 18), ('ring', 4),
                     ('bolt', 2), ('rod', 4), ('pipe_item', 9), ('pipe_fluid', 9),
                     ('rotor', 27)]
        for a, b in list_todo:
            if a in item_set:
                jsonf = {}
                jsonf["type"] = mac
                jsonf["eu"] = 2
                jsonf["duration"] = 200
                jsonf["item_inputs"] = {"amount": 1}
                addItemInput(jsonf["item_inputs"], id, a, vanilla, isMetal)

                jsonf["item_outputs"] = []

                out = False

                if b // 9 != 0:
                    jsonf["item_outputs"].append({
                        "item": getIdentifier(id, "dust", vanilla), "amount": b // 9})
                    out = True
                if b % 9 != 0 and 'small_dust' in item_set:
                    jsonf["item_outputs"].append({
                        "item": getIdentifier(id, "small_dust", vanilla), "amount": b % 9})
                    out = True

                if out:
                    with open(path + "/" + a + ".json", "w") as file:
                        json.dump(jsonf, file, indent=4)

    if 'dust' in item_set and 'crushed_dust' in item_set and 'ore' in item_set:
        for crushed_dust in [True, False]:
            jsonf = {}
            jsonf["type"] = mac
            jsonf["eu"] = 2
            jsonf["duration"] = 200
            jsonf["item_inputs"] = {"amount": 2 if crushed_dust else 1}
            addItemInput(jsonf["item_inputs"], id, "crushed_dust" if crushed_dust else "ore", vanilla)

            jsonf["item_outputs"] = {
                "item": getIdentifier(id, "dust" if crushed_dust else "crushed_dust", vanilla), "amount": 3 if crushed_dust else 2}

            with open(path + ("/crushed_dust" if crushed_dust else "/ore") + ".json", "w") as file:
                json.dump(jsonf, file, indent=4)


def genCompressor(id, vanilla, item_set, isMetal):
    path = "src/main/resources/data/modern_industrialization/recipes/generated/materials/" + \
        id + "/compressor"
    Path(path).mkdir(parents=True, exist_ok=True)
    mac = "modern_industrialization:compressor"

    for a, b, c in [('main', 'plate', 1), ('plate', 'curved_plate', 1), ('double_ingot', 'plate', 2)]:
        if a in item_set and b in item_set:
            jsonf = {}
            jsonf["type"] = mac
            jsonf["eu"] = 2
            jsonf["duration"] = 200
            jsonf["item_inputs"] = {"amount": 1}
            addItemInput(jsonf["item_inputs"], id, a, vanilla, isMetal)

            jsonf["item_outputs"] = {
                "item": getIdentifier(id, b, vanilla, isMetal), "amount": c}

            with open(path + "/" + a + ".json", "w") as file:
                json.dump(jsonf, file, indent=4)


def genCuttingSaw(id, vanilla, item_set, isMetal):
    path = "src/main/resources/data/modern_industrialization/recipes/generated/materials/" + \
        id + "/cutting_machine"
    Path(path).mkdir(parents=True, exist_ok=True)
    mac = "modern_industrialization:cutting_machine"

    for a, b, c in [('main', 'rod', 2), ('rod', 'bolt', 2), ('large_plate', 'gear', 2), ('pipe_item', 'ring', 2)]:
        if a in item_set and b in item_set:
            jsonf = {}
            jsonf["type"] = mac
            jsonf["eu"] = 2
            jsonf["duration"] = 200
            jsonf["item_inputs"] = {"amount": 1}
            addItemInput(jsonf["item_inputs"], id, a, vanilla, isMetal)

            jsonf["fluid_inputs"] = {
                "fluid": "minecraft:water", "amount": 1}

            jsonf["item_outputs"] = {
                "item": getIdentifier(id, b, vanilla, isMetal), "amount": c}

            with open(path + "/" + a + ".json", "w") as file:
                json.dump(jsonf, file, indent=4)

def genPacker(id, vanilla, item_set, isMetal, hasPipe):
    path = "src/main/resources/data/modern_industrialization/recipes/generated/materials/" + \
           id + "/packer"
    Path(path).mkdir(parents=True, exist_ok=True)
    mac = "modern_industrialization:packer"

    list_todo = [('main', 2, 'double_ingot', 1), ('plate', 4, 'large_plate', 1)]
    if hasPipe: list_todo.append(('curved_plate', 6, 'pipe_item', 6))

    for i, ic, o, oc in list_todo:
        if i in item_set and o in item_set:
            jsonf = {}
            jsonf["type"] = mac
            jsonf["eu"] = 2
            jsonf["duration"] = 200
            jsonf["item_inputs"] = {"amount": ic}

            addItemInput(jsonf["item_inputs"], id, i, vanilla, isMetal)

            jsonf["item_outputs"] = {
                "item": getIdentifier(id, o, vanilla, isMetal), "amount": oc}

            with open(path + "/" + i + ".json", "w") as file:
                json.dump(jsonf, file, indent=4)

    if hasPipe:
        jsonf = {}
        jsonf["type"] = mac
        jsonf["eu"] = 2
        jsonf["duration"] = 200
        jsonf["item_inputs"] = [
            {
                "item": getIdentifier(id, "pipe_item", vanilla, isMetal), "amount": 2
            },
            {
                "item": "minecraft:glass_pane", "amount": 1
            }
        ]

        jsonf["item_outputs"] = {
            "item": getIdentifier(id, "pipe_fluid", vanilla, isMetal), "amount": 2
        }

        with open(path + "/" + "item_to_fluid_pipes" + ".json", "w") as file:
            json.dump(jsonf, file, indent=4)

def gen(file, id, hex, item_set, block_set, vanilla=False,  forge_hammer=False, smelting=True, isMetal=True, veinsPerChunk=0, veinsSize=0, maxYLevel=64, texture=''):

    pipe = False

    if 'pipe' in item_set:
        pipe = True
        item_set = item_set - {'pipe'}

    item_set_to_add = item_set

    if vanilla and isMetal:
        item_set_to_add = item_set_to_add - {'nugget'}
    else:
        item_set_to_add = item_set_to_add | ({'ingot'} if isMetal else {id})

    item_to_add = ','.join([(lambda s: "\"%s\"" % s)(s)
                            for s in sorted(list(item_set_to_add))])

    line = "    public static MIMaterial %s = new MIMaterial(\"%s\", %s)" % (
        id, id, ("%s" % vanilla).lower())

    line += ".addItemType(new String [] { %s})" % item_to_add

    if not vanilla and len(block_set) > 0:
        block_to_add = ','.join([(lambda s: "\"%s\"" % s)(s)
                                 for s in sorted(list(block_set))])
        line += ".addBlockType(new String [] { %s })" % block_to_add

    if not vanilla and 'ore' in block_set:
        line += '.setupOreGenerator(%d, %d, %d)' % (veinsPerChunk,
                                                    veinsSize, maxYLevel)

    line += ';'
    file.write(line + "\n")

    print(line)

    if not vanilla:
        if not isMetal:
            gen_texture(id, hex, item_set | {texture}, block_set, special_texture=texture)
        else:
            gen_texture(id, hex, item_set | {'ingot'}, block_set)
    else:
        if isMetal:
            gen_texture(id, hex, item_set - {'ingot', 'nugget'}, set())
        else:
            gen_texture(id, hex, item_set - {id}, set())

    item_set = item_set | block_set | {'main'}
    if pipe:
        item_set |= {'pipe_item', 'pipe_fluid'}

    if forge_hammer:
        genForgeHammer(id, vanilla, item_set, isMetal)

    genCraft(id, vanilla, item_set, isMetal, pipe)

    if smelting:
        genSmelting(id, vanilla, item_set, isMetal)

    genMacerator(id, vanilla, item_set, isMetal)
    genCompressor(id, vanilla, item_set, isMetal)
    genCuttingSaw(id, vanilla, item_set, isMetal)
    genPacker(id, vanilla, item_set, isMetal, pipe)


BLOCK_ONLY = {'block'}
ORE_ONLY = {'ore'}
BOTH = {'block', 'ore'}

ITEM_BASE = {'plate', 'large_plate', 'nugget', 'double_ingot',
             'small_dust', 'dust', 'curved_plate', 'pipe', 'rod', 'crushed_dust'}
ITEM_ALL = ITEM_BASE | {'bolt', 'blade',
                        'ring', 'rotor', 'gear'}

ITEM_ALL_NO_ORE = ITEM_ALL - {'crushed_dust'}
TAG_BLACKLIST = { 'aluminum' } # will only allow ores from this material as tag, but nothing else

if __name__ == '__main__':
    file = open(
        "src/main/java/aztech/modern_industrialization/material/MIMaterials.java", "w")
    file.write("""package aztech.modern_industrialization.material;

public class MIMaterials {

""")
    file.close()
    file = open(
        "src/main/java/aztech/modern_industrialization/material/MIMaterials.java", "a")

    gen(file, 'gold', '#ffe100', ITEM_BASE, BOTH, vanilla=True)
    gen(file, 'iron', '#f0f0f0', ITEM_ALL, BOTH, vanilla=True, forge_hammer=True)
    gen(file, 'copper', '#ff6600', ITEM_ALL, BOTH, forge_hammer=True,
        veinsPerChunk=20, veinsSize=9, maxYLevel=128)
    gen(file, 'bronze', '#ffcc00', ITEM_ALL_NO_ORE, BLOCK_ONLY, forge_hammer=True)
    gen(file, 'tin', '#cbe4e4', ITEM_ALL, BOTH,
        forge_hammer=True, veinsPerChunk=8, veinsSize=9)
    gen(file, 'steel', '#3f3f3f', ITEM_ALL_NO_ORE, BLOCK_ONLY)
    gen(file, 'aluminum', '#3fcaff', ITEM_BASE, BOTH,
        smelting=False, veinsPerChunk=6, veinsSize=6)
    gen(file, 'lignite_coal', '#604020', {
        'dust', 'crushed_dust'}, ORE_ONLY, forge_hammer=True, isMetal=False,
        veinsPerChunk=20, veinsSize=17, maxYLevel=128, texture='lignite_coal')

    file.write("\n")
    file.write("}")
    file.close()
