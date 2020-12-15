from glob import glob
from PIL import Image
from PIL.ImageOps import grayscale, colorize
from PIL.ImageChops import multiply
from pathlib import Path
from collections import defaultdict

import os
import json
import shutil


def image_tint(src, tint='#ffffff'):
    src = Image.open(src).convert('RGBA')
    r, g, b, alpha = src.split()
    gray = grayscale(src)
    result = colorize(gray, (0, 0, 0, 0), tint)
    result.putalpha(alpha)
    return result


def clean(string):
    return " ".join([word.capitalize() for word in string.split('_')])


def gen_name(ty):
    id = ty.id

    with open('src/main/resources/assets/modern_industrialization/lang/en_us.json', 'r') as lang_file:
        lang_json = json.load(lang_file)
        lang_file.close()

    for item in ty.mi_items:
        if item != id:
            lang_id = 'item.modern_industrialization.' + (id + '_' + item)
            name = clean(id) + " " + clean(item)
        else:
            lang_id = 'item.modern_industrialization.' + id
            name = clean(id)
        lang_json[lang_id] = name

    if 'fluid_pipe' in ty.overrides:
        lang_json['item.modern_industrialization.pipe_fluid_' +
                  id] = clean(id) + ' Fluid Pipe'
    if 'item_pipe' in ty.overrides:
        lang_json['item.modern_industrialization.pipe_item_' +
                  id] = clean(id) + ' Item Pipe'
    if 'cable' in ty.overrides:
        lang_json['item.modern_industrialization.pipe_electricity_' +
                  id] = clean(id) + ' Cable'

    for block in ty.mi_blocks:
        lang_id = 'block.modern_industrialization.' + (id + '_' + block)
        if block == "block":
            name = "Block of " + clean(id)
        else:
            name = clean(id) + " " + clean(block)
        lang_json[lang_id] = name

        if block == "ore":
            lang_json['text.autoconfig.modern_industrialization.option.ores.generate%s' % id.replace('_', ' ').title().replace(' ', '')] = "Generate " + clean(id) + " Ore"

    with open('src/main/resources/assets/modern_industrialization/lang/en_us.json', 'w') as lang_file:
        json.dump(lang_json, lang_file, indent=4, sort_keys=True)
        lang_file.close()


def gen_texture(id, hex, icon_set, item_set, block_set, special_texture=''):

    item = glob("template/item/"+ icon_set +"/*.png")

    output_path = (
        "src/main/resources/assets/modern_industrialization/textures/items/materials/" + id)
    Path(output_path).mkdir(parents=True, exist_ok=True)

    for filename in item:
        t = os.path.basename(filename).split('.')[0]
        if t in item_set:
            print(filename)
            result = image_tint(filename, hex)
            if t in TEXTURE_UNDERLAYS:
                underlay = Image.open("template/item/"+ icon_set +"/%s_underlay.png" % t)
                result = Image.alpha_composite(underlay, result)
            if t in TEXTURE_OVERLAYS:
                overlay = Image.open("template/item/"+ icon_set +"/%s_overlay.png" % t)
                result = Image.alpha_composite(result, overlay)
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
            result = image_tint(filename, hex)
            if t in TEXTURE_UNDERLAYS:
                underlay = Image.open("template/block/%s_underlay.png" % t)
                result = Image.alpha_composite(underlay, result)
            if t in TEXTURE_OVERLAYS:
                overlay = Image.open("template/block/%s_overlay.png" % t)
                result = Image.alpha_composite(result, overlay)
            result.save(output_path + '/' + os.path.basename(filename))

loaded_items = {'modern_industrialization:rubber_sheet'}
tags = defaultdict(lambda: [])
tags["c:gold_ores"].append("#minecraft:gold_ores")
tags["c:redstone_ores"].append("minecraft:redstone_ore")
tags["c:ruby_dusts"].append("modern_industrialization:ruby_dust")

# check if the item json is valid based on the loaded items


def allow_recipe(jsonf):
    if "fluid" in jsonf:
        return True
    if "item" not in jsonf:
        return jsonf["tag"] in tags
    namespace, _ = jsonf["item"].split(":")
    return namespace == "minecraft" or jsonf["item"] in loaded_items


class MIRecipe:

    def __init__(self, type, eu=2, duration=200):
        self.type = type
        self.eu = eu
        self.duration = duration

    def __ensure_list(self, attr):
        if not hasattr(self, attr):
            setattr(self, attr, [])

    def input(self, input, amount=1):
        self.__ensure_list("item_inputs")
        input_json = get_input_json(input)
        input_json["amount"] = amount
        self.item_inputs.append(input_json)
        return self

    def fluid_input(self, fluid, amount):
        self.__ensure_list("fluid_inputs")
        self.fluid_inputs.append({"fluid": fluid, "amount": amount})
        return self

    def output(self, item, amount=1):
        self.__ensure_list("item_outputs")
        self.item_outputs.append({"item": item, "amount": amount})
        return self

    def fluid_output(self, fluid, amount):
        self.__ensure_list("fluid_outputs")
        self.fluid_outputs.append({"fluid": fluid, "amount": amount})
        return self

    def save(self, id, suffix):
        allowed = True
        jsonf = {"type": "modern_industrialization:" +
                         self.type, "eu": self.eu, "duration": self.duration}
        optionals = ["item_inputs", "fluid_inputs",
                     "item_outputs", "fluid_outputs"]
        for opt in optionals:
            if hasattr(self, opt):
                inputs = []
                for i in getattr(self, opt):
                    allowed = allowed and allow_recipe(i)
                    inputs.append(i)
                jsonf[opt] = inputs
        if allowed:
            path = "src/main/resources/data/modern_industrialization/recipes/generated/materials/" + \
                   id + "/" + self.type + "/"
            Path(path).mkdir(parents=True, exist_ok=True)
            with open(path + suffix + ".json", "w") as file:
                json.dump(jsonf, file, indent=4)


def get_input_json(string):
    return {"tag": string[1:]} if string[0] == '#' else {"item": string}


class CraftingRecipe:

    def __init__(self, pattern, output, count=1, **kwargs):
        self.pattern = pattern
        self.output = output
        self.count = count
        self.kwargs = kwargs

    def save(self, id, suffix):
        jsonf = {"type": "minecraft:crafting_shaped", "pattern": self.pattern,
                 "result": {"item": self.output, "count": self.count}}
        keys = {}
        allowed = allow_recipe(jsonf["result"])
        for line in self.pattern:
            for c in line:
                if c != " ":
                    keys[c] = get_input_json(self.kwargs[c])
                    allowed = allowed and allow_recipe(keys[c])
        jsonf["key"] = keys
        if allowed:
            path = "src/main/resources/data/modern_industrialization/recipes/generated/materials/" + id + "/craft/"
            Path(path).mkdir(parents=True, exist_ok=True)
            with open(path + suffix + ".json", "w") as file:
                json.dump(jsonf, file, indent=4)
        return self

    def export(self, other_type, id, suffix, **kwargs):  # will also save
        recipe = MIRecipe(other_type, **kwargs)
        recipe.output(self.output, self.count)
        keycount = defaultdict(lambda: 0)
        keys = {}
        for line in self.pattern:
            for c in line:
                if c != " ":
                    keys[c] = {"tag": self.kwargs[c][1:]} if self.kwargs[
                        c][0] == '#' else {"item": self.kwargs[c]}
                    keycount[c] += 1
        for k in keys:
            recipe.input("#" + keys[k]["tag"] if "tag" in keys[k]
                         else keys[k]["item"], amount=keycount[k])
        recipe.save(id, suffix)


def genForgeHammer(ty, tyo):
    hammer = "forge_hammer_hammer"
    saw = "forge_hammer_saw"

    list_todo = [('large_plate', 1, 'curved_plate', 3, hammer),
                 ('double_ingot', 1, 'plate', 1, hammer),
                 ('nugget', 1, 'tiny_dust', 1, hammer),
                 ('main', 1, 'rod', 1, saw),
                 ('large_plate', 1, 'gear', 1, saw),
                 ('rod', 1, 'bolt', 1, saw),
                 ('ore', 1, 'crushed_dust', 2, hammer),
                 ('item_pipe', 1, 'ring', 1, saw),
                 ('main', 2, 'double_ingot', 1, hammer),
                 ]

    for a, inputCount, b, c, d in list_todo:
        MIRecipe(d).input(tyo[a], inputCount).output(ty[b], c).save(ty.id, b)


def genCraft(vanilla, ty, tyo):
    list_full = [('tiny_dust', 'dust')]

    if not vanilla:
        list_full.append(('nugget', 'main'))
        list_full.append(('main', 'block'))

    for (a, b) in list_full:
        CraftingRecipe(
            ["yxx", "xxx", "xxx"],
            ty[b],
            x=tyo[a],
            y=ty[a],
        ).save(ty.id, "%s_from_%s" % (b, a))
        CraftingRecipe(
            ["x"],
            ty[a],
            9,
            x=ty[b],
        ).save(ty.id, "%s_from_%s" % (a, b))

    CraftingRecipe([
        "P",
        "P",
        "I"
    ],
        ty["blade"],
        4,
        P=tyo["plate"],
        I=tyo["rod"],
    ).save(ty.id, "blade").export("assembler", ty.id, "blade", eu=8)

    CraftingRecipe([
        "PPP",
        "P P",
        "PPP"
    ],
        ty["coil"],
        1,
        P=tyo["wire"],
    ).save(ty.id, "coil").export("assembler", ty.id, "coil", eu=8)

    CraftingRecipe([
        "PP",
        "PP"
    ],
        ty["large_plate"],
        P=tyo["plate"],
    ).save(ty.id, "large_plate").export("packer", ty.id, "large_plate")

    CraftingRecipe([
        "bBb",
        "BRB",
        "bBb"
    ],
        ty["rotor"],
        b=tyo["bolt"],
        B=tyo["blade"],
        R=tyo["ring"],
    ).save(ty.id, "rotor")

    CraftingRecipe([
        "ppp",
        "   ",
        "ppp",
    ],
        ty["item_pipe"],
        6,
        p=tyo["curved_plate"],
    ).save(ty.id, "item_pipe").export("packer", ty.id, "item_pipe")

    CraftingRecipe([
        "ppp",
        "ggg",
        "ppp",
    ],
        ty["fluid_pipe"],
        6,
        g="minecraft:glass_pane",
        p=tyo["curved_plate"],
    ).save(ty.id, "fluid_pipe")

    CraftingRecipe([
        "rrr",
        "www",
        "rrr",
    ],
        ty["cable"],
        3,
        r="modern_industrialization:rubber_sheet",
        w=ty["wire"],
    ).save(ty.id, "cable").export("assembler", ty.id, "cable", eu=8)


def genSmelting(vanilla, ty, isMetal):
    list_todo = [('tiny_dust', 'nugget', 0.08),
                 ('crushed_dust', 'main', 0.7),  ('dust', 'main', 0.7)]

    if not vanilla:
        list_todo.append(('ore', 'main', 0.7))

    for a, b, c in list_todo:
        if ty[a] in loaded_items and ty[b] in loaded_items:
            list_recipe = [("smelting", 200)]
            if isMetal:
                list_recipe.append(('blasting', 100))
            for d, e in list_recipe:
                jsonf = {}
                jsonf["type"] = "minecraft:" + d
                jsonf["ingredient"] = {"item": ty[a]}
                jsonf["result"] = ty[b]
                jsonf["experience"] = c
                jsonf["cookingtime"] = e

                path = "src/main/resources/data/modern_industrialization/recipes/generated/materials/" + \
                       ty.id + "/smelting/"
                Path(path).mkdir(parents=True, exist_ok=True)
                with open(path + "/" + a + "_" + d + ".json", "w") as file:
                    json.dump(jsonf, file, indent=4)


def genMacerator(ty, tyo, macerator_disable):
    list_todo = [('double_ingot', 18), ('plate', 9), ('curved_plate', 9),
                 ('nugget', 1), ('large_plate', 36), ('gear', 18), ('ring', 4),
                 ('bolt', 2), ('rod', 4), ('item_pipe', 9), ('fluid_pipe', 9),
                 ('rotor', 27), ('main', 9), ('blade', 5)]
    for a, b in list_todo:
        recipe = MIRecipe('macerator').input(tyo[a])
        if b // 9 != 0:
            recipe.output(ty["dust"], b // 9)
        if b % 9 != 0:
            recipe.output(ty["tiny_dust"], b % 9)
        recipe.save(ty.id, a)

    if 'crushed_dust' not in macerator_disable:
        MIRecipe("macerator").input(tyo["ore"]).output(
            ty["crushed_dust"], amount=2).save(ty.id, "ore")
    if 'dust' not in macerator_disable:
        MIRecipe("macerator").input(ty["crushed_dust"], amount=2).output(
            ty["dust"], amount=3).save(ty.id, "crushed_dust")


def genCompressor(ty, tyo):
    for a, b, c in [('main', 'plate', 1), ('plate', 'curved_plate', 1), ('double_ingot', 'plate', 2)]:
        MIRecipe("compressor").input(tyo[a]).output(
            ty[b], amount=c).save(ty.id, a)


def genCuttingSaw(ty, tyo):
    for a, b, c in [('main', 'rod', 2), ('rod', 'bolt', 2), ('large_plate', 'gear', 2), ('item_pipe', 'ring', 2)]:
        MIRecipe("cutting_machine").input(tyo[a]).fluid_input(
            'minecraft:water', amount=1).output(ty[b], amount=c).save(ty.id, a)


def genPacker(ty, tyo):
    MIRecipe("packer").input(tyo["main"], amount=2).output(
        ty["double_ingot"]).save(ty.id, "double_ingot")
    MIRecipe("packer").input(ty["item_pipe"], amount=2).input(
        "minecraft:glass_pane").output(ty["fluid_pipe"], amount=2).save(ty.id, "fluid_pipe")
    MIRecipe("packer").input(tyo["tiny_dust"], amount=9).output(
        ty["dust"]).save(ty.id, "dust")
    MIRecipe("packer").input(tyo["nugget"], amount=9).output(
        ty["ingot"]).save(ty.id, "ingot")


def genWiremill(ty, tyo):
    for i, ic, o, oc in [('plate', 1, 'wire', 2), ('wire', 1, 'fine_wire', 4)]:
        MIRecipe("wiremill").input(tyo[i], amount=ic).output(
            ty[o], amount=oc).save(ty.id, o)


def genAssembler(ty, tyo):
    MIRecipe("assembler", eu=8).input(tyo["blade"], amount=4).input(
        tyo["ring"], amount=1).output(ty["rotor"]).save(ty.id, "rotor")

material_lines = []
ore_config = []


def gen(file, ty, hex, set="dull", vanilla=False, forge_hammer=False, smelting=True, isMetal=True, veinsPerChunk=0, veinsSize=0, maxYLevel=64, macerator_disable={}):

    item_to_add = ','.join([(lambda s: "\"%s\"" % s)(s)
                            for s in sorted(list(ty.mi_items))])

    line = "    public static MIMaterial %s = new MIMaterial(\"%s\", %s)" % (
        ty.id, ty.id, ("%s" % vanilla).lower())

    line += ".addItemType(new String [] { %s})" % item_to_add

    if len(ty.mi_blocks) > 0:
        block_to_add = ','.join([(lambda s: "\"%s\"" % s)(s)
                                 for s in sorted(list(ty.mi_blocks))])
        line += ".addBlockType(new String [] { %s })" % block_to_add

    if 'ore' in ty.mi_blocks:
        config_field = 'generate' + ty.id.replace('_', ' ').title().replace(' ', '')
        line += '.setupOreGenerator(%d, %d, %d, MIConfig.getConfig().ores.%s)' % (veinsPerChunk,
                                                    veinsSize, maxYLevel, config_field)
        ore_config.append(config_field)

    gen_name(ty)

    line += ';'
    global material_lines
    material_lines.append(line)

    print(line)

    gen_texture(ty.id, hex, set, ty.mi_items, ty.mi_blocks)

    tyo = ty.get_oredicted()

    if forge_hammer:
        genForgeHammer(ty, tyo)

    genCraft(vanilla, ty, tyo)

    if smelting:
        genSmelting(vanilla, ty, isMetal)

    genMacerator(ty, tyo, macerator_disable)
    genCompressor(ty, tyo)
    genCuttingSaw(ty, tyo)
    genPacker(ty, tyo)
    genWiremill(ty, tyo)
    genAssembler(ty, tyo)


BLOCK_ONLY = {'block'}
ORE_ONLY = {'ore'}
BOTH = {'block', 'ore'}

ITEM_BASE = {'ingot', 'plate', 'large_plate', 'nugget', 'double_ingot',
             'tiny_dust', 'dust', 'curved_plate', 'crushed_dust'}  # TODO: pipes

PURE_METAL = {'ingot', 'nugget', 'tiny_dust', 'dust', 'crushed_dust'}
PURE_NON_METAL = {'tiny_dust', 'dust', 'crushed_dust'}

ITEM_ALL = ITEM_BASE | {'bolt', 'blade',
                        'ring', 'rotor', 'gear', 'rod'}

ITEM_ALL_NO_ORE = ITEM_ALL - {'crushed_dust'}
TEXTURE_UNDERLAYS = {'ore'}
TEXTURE_OVERLAYS = {'fine_wire', 'oxide', 'double_ingot', 'dust', 'gear', 'ingot', 'nugget', 'ring', 'rod', 'tiny_dust'}
DEFAULT_OREDICT = {'nugget': '_nuggets', 'ore': '_ores', 'plate': '_plates',
                   'gear': '_gears', 'dust': '_dusts', 'tiny_dust': '_tiny_dusts'}
RESTRICTIVE_OREDICT = {'ore': '_ores'}


class Material:

    def __init__(self, id, mi_items, mi_blocks, overrides=None, oredicted=None):
        if overrides is None:
            overrides = {}
        if oredicted is None:
            oredicted = {}
        self.id = id
        self.mi_items = mi_items
        self.mi_blocks = mi_blocks
        if "ingot" in mi_items:
            overrides["main"] = "modern_industrialization:" + id + "_ingot"
            if oredicted == {}:
                oredicted["main"] = "c:" + id + "_ingots"
                for key in DEFAULT_OREDICT:
                    if key in mi_items or key in mi_blocks:
                        oredicted[key] = "c:" + id + DEFAULT_OREDICT[key]
        self.overrides = overrides
        self.oredicted = oredicted
        self.__load()

    def __load(self):
        global loaded_items
        for item in self.mi_items | self.mi_blocks:
            loaded_items |= {
                "modern_industrialization:" + self.id + "_" + item}
        for ov in self.overrides.values():
            loaded_items |= {ov}
        for key, suffix in self.oredicted.items():
            tags[suffix].append(self[key])
            if key == "ore":
                tags["c:ores"].append(self[key])
        return self

    def get_oredicted(self):
        class OredictedMaterial:

            def __init__(self, outer):
                self.outer = outer

            def __getitem__(self, item):
                if item in self.outer.oredicted:
                    return '#' + self.outer.oredicted[item]
                else:
                    return self.outer[item]

        return OredictedMaterial(self)

    def __getitem__(self, item):
        return "modern_industrialization:" + self.id + "_" + item if item not in self.overrides else self.overrides[item]

file = open(
    "src/main/java/aztech/modern_industrialization/material/MIMaterials.java", "w")
file.write("""package aztech.modern_industrialization.material;

import aztech.modern_industrialization.MIConfig;

public class MIMaterials {

""")
file.close()
file = open(
    "src/main/java/aztech/modern_industrialization/material/MIMaterials.java", "a")

shutil.rmtree(
    "src/main/resources/assets/modern_industrialization/textures/blocks/materials", ignore_errors=True)
shutil.rmtree(
    "src/main/resources/assets/modern_industrialization/textures/items/materials", ignore_errors=True)
shutil.rmtree("src/main/resources/data/c/tags/items", ignore_errors=True)
shutil.rmtree(
    "src/main/resources/data/modern_industrialization/recipes/generated/materials", ignore_errors=True)

gen(
    file,
    Material('gold', ITEM_BASE - {'ingot', 'nugget'}, set(), overrides={
        "main": "minecraft:gold_ingot",
        "nugget": "minecraft:gold_nugget",
        "ore": "minecraft:gold_ore",
        "item_pipe": "modern_industrialization:pipe_item_gold",
        "fluid_pipe": "modern_industrialization:pipe_fluid_gold",
    }, oredicted={
        "ore": "c:gold_ores",
        "plate": "c:gold_plates",
        "dust": "c:gold_dusts",
        "tiny_dust": "c:gold_tiny_dusts",
    }),
    '#FFE650', 'shiny', vanilla=True,
)
gen(
    file,
    Material('iron', ITEM_BASE - {'ingot', 'nugget'}, set(), overrides={
        "main": "minecraft:iron_ingot",
        "nugget": "minecraft:iron_nugget",
        "ore": "minecraft:iron_ore",
        "item_pipe": "modern_industrialization:pipe_item_iron",
        "fluid_pipe": "modern_industrialization:pipe_fluid_iron",
    }, oredicted={
        "ore": "c:iron_ores",
        "plate": "c:iron_plates",
        "dust": "c:iron_dusts",
        "tiny_dust": "c:iron_tiny_dusts",
    }),
    '#C8C8C8', 'metallic', vanilla=True, forge_hammer=True,
)
gen(
    file,
    Material('coal', PURE_NON_METAL, set(), overrides={
        "main": "minecraft:coal",
        "ore": "minecraft:coal_ore",
    }, oredicted={
        "ore": "c:coal_ores",
        "dust": "c:coal_dusts",
        "tiny_dust": "c:coal_tiny_dusts",
    }),
    '#282828', 'stone', vanilla=True, forge_hammer=True, isMetal=False, smelting=False,
)
gen(
    file,
    Material('copper', ITEM_ALL | {'wire', 'fine_wire'}, BOTH, overrides={
        "item_pipe": "modern_industrialization:pipe_item_copper",
        "fluid_pipe": "modern_industrialization:pipe_fluid_copper",
        "cable": "modern_industrialization:pipe_electricity_copper",
    }),
    '#FF6400', 'shiny', forge_hammer=True, veinsPerChunk=30, veinsSize=9, maxYLevel=128,
)
gen(
    file,
    Material('bronze', ITEM_ALL_NO_ORE, BLOCK_ONLY, overrides={
        "item_pipe": "modern_industrialization:pipe_item_bronze",
        "fluid_pipe": "modern_industrialization:pipe_fluid_bronze",
    }),
    '#D2823C', 'metallic', forge_hammer=True,
)
gen(
    file,
    Material('tin', ITEM_ALL | {'wire'}, BOTH, overrides={
        "item_pipe": "modern_industrialization:pipe_item_tin",
        "fluid_pipe": "modern_industrialization:pipe_fluid_tin",
        "cable": "modern_industrialization:pipe_electricity_tin",
    }),
    '#DCDCDC', 'dull', forge_hammer=True, veinsPerChunk=8, veinsSize=9,
)
gen(
    file,
    Material('steel', ITEM_ALL_NO_ORE, BLOCK_ONLY, overrides={
        "item_pipe": "modern_industrialization:pipe_item_steel",
        "fluid_pipe": "modern_industrialization:pipe_fluid_steel",
    }),
    '#828282', 'metallic'
)
gen(
    file,
    Material('aluminum', ITEM_ALL | {'ingot', 'wire'}, BLOCK_ONLY, overrides={
        "item_pipe": "modern_industrialization:pipe_item_aluminum",
        "fluid_pipe": "modern_industrialization:pipe_fluid_aluminum",
        "cable": "modern_industrialization:pipe_electricity_aluminum",
    }),
    '#80C8F0', 'metallic', smelting=False,
)
gen(
    file,
    Material('bauxite', PURE_NON_METAL, ORE_ONLY, oredicted={
        "dust": "c:bauxite_dusts",
        "ore": "c:bauxite_ores",
        "tiny_dust": "c:bauxite_tiny_dusts",
    }),
    '#C86400', 'dull', isMetal=False, smelting=False, veinsPerChunk=8, veinsSize=7, maxYLevel=32,
)
gen(
    file,
    Material('lignite_coal', PURE_NON_METAL | {'lignite_coal'}, ORE_ONLY, overrides={
        'main': 'modern_industrialization:lignite_coal',
    }),
    '#644646', 'stone', forge_hammer=True, isMetal=False, smelting=False, veinsPerChunk=10, veinsSize=17, maxYLevel=128,
)
gen(
    file,
    Material('lead', ITEM_BASE, BOTH, overrides={
        "item_pipe": "modern_industrialization:pipe_item_lead",
        "fluid_pipe": "modern_industrialization:pipe_fluid_lead",
    }),
    '#3C286E', 'dull', veinsPerChunk=4, veinsSize=8, maxYLevel=64,
    macerator_disable={'dust'}
)
gen(
    file,
    Material('battery_alloy', {'tiny_dust', 'dust',
                               'plate', 'nugget', 'curved_plate', 'ingot',
                               'double_ingot'}, BLOCK_ONLY),
    '#9C7CA0', 'dull'
)
gen(
    file,
    Material('invar', {'tiny_dust', 'dust', 'plate',
                       'ingot', 'double_ingot', 'nugget', 'large_plate', 'gear'}, BLOCK_ONLY),
    '#DCDC96', 'metallic'
)

gen(
    file,
    Material('cupronickel', {'tiny_dust', 'dust', 'plate',
                             'ingot', 'nugget', 'wire', 'double_ingot'}, BLOCK_ONLY | {'coil'}, overrides={
        "cable": "modern_industrialization:pipe_electricity_cupronickel",
    }),
    '#E39681', 'metallic'
)

gen(
    file,
    Material('antimony', PURE_METAL, BOTH),
    '#DCDCF0', 'shiny', veinsPerChunk=4, veinsSize=6, maxYLevel=64,
)
gen(
    file,
    Material('nickel', ITEM_BASE, BOTH, overrides={
        "item_pipe": "modern_industrialization:pipe_item_nickel",
        "fluid_pipe": "modern_industrialization:pipe_fluid_nickel",
    }),
    '#FAFAC8', 'metallic', veinsPerChunk=7, veinsSize=6, maxYLevel=64,
)
gen(
    file,
    Material('silver', ITEM_BASE, BOTH, overrides={
        "item_pipe": "modern_industrialization:pipe_item_silver",
        "fluid_pipe": "modern_industrialization:pipe_fluid_silver",
    }),
    '#DCDCFF', 'shiny', veinsPerChunk=4, veinsSize=6, maxYLevel=64,
)
gen(
    file,
    Material('redstone', {'crushed_dust', 'tiny_dust'}, set(), overrides={
        "dust": "minecraft:redstone",
    }, oredicted={
        "tiny_dust": "c:redstone_tiny_dusts",
    }),
    '#d20000', 'gem', macerator_disable={'crushed_dust', 'dust'}
)
gen(
    file,
    Material('sodium', PURE_METAL - {'crushed_dust'}, BLOCK_ONLY),
    '#071CB8', 'stone'
)
gen(
    file,
    Material('salt', PURE_NON_METAL, ORE_ONLY, oredicted={
        "dust": "c:salt_dusts",
        "ore": "c:salt_ores",
        "tiny_dust": "c:salt_tiny_dusts",
    }),
    '#c7d6c5', 'gem', isMetal=False, smelting=False, veinsPerChunk=2, veinsSize=7, maxYLevel=32
)
gen(
    file,
    Material('titanium', ITEM_ALL_NO_ORE, BLOCK_ONLY),
    '#DCA0F0', 'metallic', smelting=False
)

gen(
    file,
    Material('quartz', {'crushed_dust', 'dust', 'tiny_dust'}, set(), overrides={
        "main": "minecraft:quartz"
    }, oredicted={
        "tiny_dust": "c:quartz_tiny_dusts",
        "dust": "c:quartz_dusts"
    }),
    '#f0ebe4', 'gem', smelting=False
)

gen(
    file,
    Material('electrum', ITEM_BASE - {'crushed_dust'} | {'wire', 'fine_wire'}, BLOCK_ONLY, overrides={
        "item_pipe": "modern_industrialization:pipe_item_electrum",
        "fluid_pipe": "modern_industrialization:pipe_fluid_electrum",
        "cable": "modern_industrialization:pipe_electricity_electrum",

    }),
    '#FFFF64', 'shiny')

gen(
    file,
    Material('silicon', (PURE_METAL - {'crushed_dust'})
             | {'plate', 'double_ingot'}, BLOCK_ONLY),
    '#3C3C50', 'metallic'
)

gen(
    file,
    Material('stainless_steel', ITEM_ALL_NO_ORE, BLOCK_ONLY),
    '#C8C8DC', 'shiny', smelting=False
)

gen(
    file,
    Material('manganese', PURE_METAL, BLOCK_ONLY),
    '#C1C1C1', 'dull', smelting=False, macerator_disable={'dust'}
)

gen(
    file,
    Material('chrome', PURE_METAL | {
             'plate', 'large_plate', 'double_ingot'}, BLOCK_ONLY),
    '#FFE6E6', 'shiny', smelting=False, macerator_disable={'dust'}
)


file.write("\n".join(sorted(material_lines)))
file.write("\n")
file.write("}")
file.close()

print(loaded_items)

# save tags
Path("src/main/resources/data/c/tags/items/").mkdir(parents=True, exist_ok=True)
for key, values in tags.items():
    with open("src/main/resources/data/c/tags/items/" + key[2:] + ".json", "w") as f:
        jsonf = {
            "replace": False,
            "values": values,
        }
        json.dump(jsonf, f, indent=4)

java_class = """
package aztech.modern_industrialization;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

/**
 * This is auto-generated, don't edit by hand!
 */
public class MITags {\n"""
for key in sorted(list(tags.keys())):
    java_class += '    public static final Tag<Item> %s = TagRegistry.item(new Identifier("c", "%s"));\n' % (key[
        2:].upper(), key[2:])
java_class += """
    public static void setup() {
        // Will register the tags by loading the static fields!
    }
}
"""
with open("src/main/java/aztech/modern_industrialization/MITags.java", "w") as f:
    f.write(java_class)

ore_config.sort()
ore_config_class = """
package aztech.modern_industrialization.material;

/**
 * This is auto-generated, don't edit by hand!
 */
public class MIOreGenerators {
"""
ore_config_class += ''.join([ "    public boolean %s = true;\n" % ore_config_entry for ore_config_entry in ore_config ])
ore_config_class += '}\n'
with open("src/main/java/aztech/modern_industrialization/material/MIOreGenerators.java", "w") as f:
    f.write(ore_config_class)
