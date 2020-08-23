from glob import glob
from PIL import Image
from PIL.ImageOps import grayscale, colorize

import os


#  copy-pasta
def image_tint(src, tint='#ffffff'):
    src = Image.open(src)
    r, g, b, alpha = src.split()
    gray = grayscale(src)
    result = colorize(gray, (0, 0, 0, 0), tint)
    result.putalpha(alpha)
    return result


id = input("Id ?")
hex = input("RGB Hex ?")  # format #ffffff
vanilla = input("Vanilla ?").lower() == "true"
ore = False
if not vanilla:
    ore = input("Ore ?").lower() == "true"

item = glob("template/item/*.png")
if not vanilla:
    item.extend(glob("template/item_vanilla/*.png"))

for filename in item:
    output_path = ("src/main/resources/assets/modern_industrialization/textures/items/materials/" +
                   id + "/" + os.path.basename(filename))
    try:
        os.mkdir(
            "src/main/resources/assets/modern_industrialization/textures/items/materials/" + id)
    except:
        pass
    result = image_tint(filename, hex)
    result.save(output_path)

if not vanilla:
    result_block = image_tint("template/block.png", hex)
    try:
        os.mkdir(
            "src/main/resources/assets/modern_industrialization/textures/blocks/materials/" + id)
        os.mkdir(
            "src/main/resources/assets/modern_industrialization/textures/blocks/materials/" + id + "/block")
    except:
        pass

    for f in ["top.png", "side.png", "bottom.png"]:
        result_block.save(
            "src/main/resources/assets/modern_industrialization/textures/blocks/materials/" + id + "/block/" + f)

    if ore:
        try:
            os.mkdir(
                "src/main/resources/assets/modern_industrialization/textures/blocks/materials/" + id + "/ore")
        except:
            pass

        result_ore = image_tint("template/ore.png", hex)
        stone = Image.open("template/stone.png")
        for f in ["top.png", "side.png", "bottom.png"]:
            Image.alpha_composite(stone, result_ore).save(
                "src/main/resources/assets/modern_industrialization/textures/blocks/materials/" + id + "/ore/" + f)
