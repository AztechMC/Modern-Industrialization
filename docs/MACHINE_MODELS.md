# JSON machine models
Each machine model in MI consists of a casing, and a set of overlays.
The casing is the core cube of the machine that can never be rotated, and may be reused for multiple machines or by multiblock hatches.

## Casing models
The top, side and bottom textures of a casing must be `modern_industrialization:textures/casing/<casing name>/{top,side,bottom}.png`.
For the rest, casings are loaded automatically when requested by a machine model.

## Machine models
Example JSON:
```json5
{
  // Set of overlays to use for all casings unless overridden below.
  "default_overlays": {
    // Top overlays
    "top": "<id of top overlay texture to use when the machine is inactive>",
    "top_active": "<id of top overlay texture to use when the machine is active>", // omit to use "top" when the machine is active
    // Side overlays
    "side": "...",
    "side_active": "...",
    // Bottom overlays
    "bottom": "...",
    "bottom_active": "...",
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