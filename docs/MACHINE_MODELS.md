# Machine models
Each machine model in MI consists of two parts:
- A casing: the core cube of the machine. It that can never be rotated, and may be reused for multiple machines or by multiblock hatches.
- A set of overlays: extra textures applied on some sides.

## Casing models
Casing models can either be based on a set of textures, or pull their model directly from a block

### Texture-based
The top, side and bottom textures of a casing must be `modern_industrialization:textures/block/casings/<casing name>/{top,side,bottom}.png`.
The textures are loaded automatically for all registered casings, hence there is no JSON model for casings.

### Block-based
To use a block's model for a casing, you must define a JSON file at `modern_industrialization:models/machine_casing/<casing name>.json`.
That file must only contain a single `block` key, telling MI which block to use for the casing.
```json5
{
  "block": "<block id to use for the casing>"
}
```
**Connected textures applied to the target block (for example with Athena) will be applied to the casing.** 

## Machine models
A machine model JSON file defines which overlays are applied on which sides of the machine.
The JSON file must be located at `modern_industrialization:models/machine/<machine name>.json`.

Example JSON:
```json5
{
  // Set of overlays to use for all casings unless overridden below.
  "default_overlays": {
    // Top overlays
    "top": "<id of top overlay texture to use when the machine is inactive>",
    "top_active": "<id of top overlay texture to use when the machine is active>", // omit to use "top" when the machine is active
    // Bottom overlays
    "bottom": "...",
    "bottom_active": "...",
    // Optionally you can override a top texture or bottom depending on the orientation of the machine.
    "top_s": "<id of the top overlay texture to use when the machine is pointing toward south>",
    "top_s_active": "<id of the top overlay texture to use when the machine is pointing toward south and active>",
    // top_w, top_n, top_e, bottom_s, bottom_w, bottom_n and bottom_e are also supported.
    // Side overlays
    "side": "...",
    "side_active": "...",
    // You can then override each side specifically if you want, otherwise "side" will be used if it is present:
    "front": "...",
    "front_active": "...",
    "left": "...",
    "left_active": "...",
    "right": "...",
    "right_active": "...",
    "back": "...",
    "back_active": "...",
    // Output, item auto input/output, fluid auto input/output
    "output": "...",
    "item_auto": "...",
    "fluid_auto": "...",
  },
  // Optionally, you can override some of these overlays for a given casing tier:
  "tiered_overlays": {
    "<casing name>": {
      // By default values from default_overlays above will be used, unless overridden here specifically.
    }
  }
}
```
