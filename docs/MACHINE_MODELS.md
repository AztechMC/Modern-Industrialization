# Machine models
Each machine model in MI consists of two parts:
- A casing: the core cube of the machine. It that can never be rotated, and may be reused for multiple machines or by multiblock hatches.
- A set of overlays: extra textures applied on some sides.

## Casing models
The top, side and bottom textures of a casing must be `modern_industrialization:textures/block/casings/<casing name>/{top,side,bottom}.png`.
The textures are loaded automatically for all registered casings, hence there is no JSON model for casings.

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