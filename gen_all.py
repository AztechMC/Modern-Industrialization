import PIL
if PIL.__version__ != "7.0.0":
    raise RuntimeError("Must have pillow version 7.0.0")

#import gen_asset
import gen_fluids
import gen_asbl
import gen_plank
import gen_advancement
import gen_petrochem