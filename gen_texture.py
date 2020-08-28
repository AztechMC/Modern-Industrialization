from glob import glob
from PIL import Image
from PIL.ImageOps import grayscale, colorize

import os


#  copy-pasta
def image_tint(src, tint='#ffffff'):
    src = Image.open(src).convert('RGBA')
    r, g, b, alpha = src.split()
    gray = grayscale(src)
    result = colorize(gray, (0, 0, 0, 0), tint)
    result.putalpha(alpha)
    return result


def gen(id, hex, vanilla, ore):

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


if __name__ == '__main__':
    gen('gold', '#ffe100', True, False)
    gen('iron', '#f0f0f0', True, False)
    gen('copper', '#ffcc00', False, True)
    gen('bronze', '#ff6600', False, False)
    gen('tin', '#cbe4e4', False, True)
    gen('steel', '#3f3f3f', False, False)
    gen('aluminum', '#3fcaff', False, True)