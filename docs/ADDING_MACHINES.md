# Adding Machines
To add machines, you will need to use a startup script and the events in `MIMachineEvents`.

## Registering a recipe type
Recipe types can be added by KubeJS.
Generally you will want to store them in a variable to use them later.

Item/fluid input/outputs need to be enabled manually for the type to accept them.

Here is an example where two new recipe types are registered:
```js
let PYROLYSE_OVEN;
let CIRCUIT_ASSEMBLER;

MIMachineEvents.registerRecipeTypes(event => {
    PYROLYSE_OVEN = event.register("pyrolyse_oven")
        .withItemInputs() // enable item inputs
        .withItemOutputs() // enable item outputs
        .withFluidInputs() // enable fluid inputs
        .withFluidOutputs(); // enable fluid outputs
    CIRCUIT_ASSEMBLER = event.register("circuit_assembler")
        .withItemInputs()
        .withItemOutputs();
});
``` 

## Adding a single block crafting machine
Single block crafting machines are the "standard" single block crafting machines.
Examples include the macerators, the chemical reactor, the distillery, etc...

They come in 3 tiers:
- `bronze`: Limited to 2 EU/t recipes, steam powered.
- `steel`: Limited to 4 EU/t recipes, steam powered.
- `electric`: Base speed of 8 EU/t recipes, overclock up to 32 EU/t without upgrades. Supports upgrades. Electricity powered, casings can be installed to change the cable tier.

The following parameters need to be provided, in order:
- First, a few general parameters:
  - 1: English name of the machine. This is the name that players see when they over the stack, look at the machine, etc...
  - 2: Internal name of the machine. The registration id of the machine is always `modern_industrialization:` followed by the internal name.
  - 3: Recipe type. The recipes that the machine can process. See above how to register a recipe type.
  - 4: List of tiers for the machine. List containing any of `"bronze"`, `"steel"` and/or `"electric"`.
- GUI configuration:
  - 5: Height of the background in the machine screen.
  - 6: Where the progress bar is located, in pixels, and the type of progress bar to use. You can look at the available progress bars [here](../src/main/resources/assets/modern_industrialization/textures/gui/progress_bar/).
  - 7: Where the efficiency bar goes. This is where the current overclock is displayed (only in electric machines).
  - 8: Where the energy bar goes. This is where the current energy is displayed (only in electric machines).
- Slot configuration:
  - 9: Number of item input slots.
  - 10: Number of item output slots.
  - 11: Number of fluid input slots. (Note that steam tier machines always have an extra input slot for steam).
  - 12: Number of fluid output slots.
  - 13: Capacity of each fluid slot, i.e. how many buckets of fluids it can hold.
  - 14: Positions of the item input and output slots. Grids of slots are added using `.addSlots(x in pixels, y in pixels, grid width in slots, grid height in slots)`.
  - 15: Positions of the fluid input and output slots.
- Model configuration. Here you can choose which overlays are available in your machine. The overlays are the machine-specific textures.
  - You can find overlay examples [here](../src/main/resources/assets/modern_industrialization/textures/block/machines/).
  - 16: Choose whether the machine has a front overlay (`overlay_front.png` and `overlay_front_active.png`).
  - 17: Choose whether the machine has a top overlay (`overlay_top.png` and `overlay_top_active.png`).
  - 18: Choose whether the machine has a side overlay (`overlay_side.png` and `overlay_side_active.png`).

Here is an example, where we add a circuit assembler that looks very much like a normal assembler:
```js
MIMachineEvents.registerMachines(event => {
  event.craftingSingleBlock(
      /* GENERAL PARAMETERS FIRST */
      // English name, internal name, recipe type (see above), list of tiers (can be bronze/steel/electric)
      "Circuit Assembler", "circuit_assembler", CIRCUIT_ASSEMBLER, ["bronze", "steel", "electric"],
      /* GUI CONFIGURATION */
      // Background height (or -1 for default value), progress bar, efficiency bar, energy bar
      186, event.progressBar(105, 45, "circuit"), event.efficiencyBar(48, 86), event.energyBar(14, 44),
      /* SLOT CONFIGURATION */
      // Number of slots: item inputs, item outputs, fluid inputs, fluid outputs
      9, 3, 0, 0,
      // Capacity for fluid slots (unused here)
      16,
      // Slot positions: items and fluids.
      // Explanation: 3x3 grid of item slots starting at position (42, 27), then 1x3 grid of item slots starting at position (139, 27).
      items => items.addSlots(42, 27, 3, 3).addSlots(139, 27, 1, 3), fluids => {},
      /* MODEL CONFIGURATION */
      // front overlay?, top overlay?, side overlay?
      true, true, false,
  );
})
```
### Overclocking
If bronze or steel is in the list of tiers, the machine behavior defaults to allow gunpowder to double the speed of the machine. You can change this behavior by adding a config lambda to the end.

Example:
```js
config.steamCustomOverclock({
    "minecraft:redstone": {
        multiplier: 3,
        ticks: 400
    },
    "minecraft:redstone_block": {
        multiplier: 3,
        ticks: 3600
    },
    "minecraft:glowstone_dust": {
        multiplier: 6,
        ticks: 200
    }
})
```
This example adds a 3x speed multiplier for 400 ticks when redstone is applied or 3600 ticks for a redstone block.
If the player adds glowstone dust they get a 6x speed multiplier for 200 ticks.
If the multipliers on the items are the same the ticks become additive otherwise the highest multiplier is consumed first before ticking a lower multiplier.
Use `config.steamCustomOverclock({})` to disable the default gunpowder overclocking behavior.

## Adding a multiblock crafting machine
There are two types of multiblock crafting machines: steam and electric.
* Steam multiblock machines include the coke oven, steam blast furnace, steam quarry, etc...
* Electric multiblock machines include the pressurizer, the electric quarry, etc...

### Create the shape
The registration of multiblock crafting machines is similar to that of singleblock crafting machines.
The main difference is that the shape of the multiblock has to be specified.
Here are a few explanations about the shape system:
- The shape is a 3D grid, and each position contains two things: the block that should be there, and which hatches are allowed to replace that block.
- To add something to the shape, use `.add(x, y, z, block, hatches)`.
  - The block can be created using `event.memberOfBlock(block id)`.
  - The hatches can be created using `event.hatchOf(list of accepted hatches)`. The hatch types are `item_input`, `item_output`, `fluid_input`, `fluid_output`, `energy_input`, and `energy_output`.
- The controller is always at position `(0, 0, 0)`, and will replace whatever block is there.
  - Positive `x` is for blocks to the right of the controller, negative `x` for blocks to the left.
  - Positive `y` is for blocks above the controller, negative `y` for blocks below.
  - Positive `z` is for blocks behind the controller, negative `z` for blocks in front of it.

Here is an example of how to create the shape for a pyrolyse oven multiblock:
```js
MIMachineEvents.registerMachines(event => {
    const pyrolyseHatch = event.hatchOf("item_input", "item_output", "fluid_input", "fluid_output", "energy_input");
    const heatproofMember = event.memberOfBlock("modern_industrialization:heatproof_machine_casing");
    const cupronickelCoilMember = event.memberOfBlock("modern_industrialization:cupronickel_coil");
    const pyrolyseShapeBuilder = event.startShape("heatproof_machine_casing");
    for (let x = -1; x <= 1; x++) {
        for (let y= -1; y <= 1; y++) {
            for (let z = 0; z <= 3; z++) {
                if (z === 1 || z === 2) {
                    if (x !== 0 || y !== 0) {
                        pyrolyseShapeBuilder.add(x, y, z, cupronickelCoilMember, event.noHatch());
                    }
                } else {
                    pyrolyseShapeBuilder.add(x, y, z, heatproofMember, pyrolyseHatch);
                }
            }
        }
    }
    const pyrolyseShape = pyrolyseShapeBuilder.build();
    
    // register multiblock as steam or electric next...
```
### Register as steam or electric
Both steam and electric methods take the same parameters
* Use `event.simpleSteamCraftingMultiBlock` function to register a steam multiblock.
* Use `event.simpleElectricCraftingMultiBlock` function to register an electric multiblock.

Here is an example creating an electric pyrolyse oven:
```js
    event.simpleElectricCraftingMultiBlock(
        /* GENERAL PARAMETERS */
        // English name, internal name, recipe type, multiblock shape
        "Pyrolyse Oven", "pyrolyse_oven", PYROLYSE_OVEN, pyrolyseShape,
        /* REI DISPLAY CONFIGURATION */
        // REI progress bar
        event.progressBar(77, 33, "arrow"),
        // REI item inputs, item outputs, fluid inputs, fluid outputs
        itemInputs => itemInputs.addSlots(56, 35, 1, 2), itemOutputs => itemOutputs.addSlot(102, 35),
        fluidInputs => fluidInputs.addSlot(36, 35), fluidOutputs => fluidOutputs.addSlot(122, 35),
        /* MODEL CONFIGUATION */
        // casing of the controller, overlay folder, front overlay?, top overlay?, side overlay?
        "heatproof_machine_casing", "pyrolyse_overlays", true, false, false,
    );
})
```

For this example, we only register a front overlay, and the overlay folder is `pyrolyse_overlays`.
Hence, we need 2 textures for the controller to render correctly:
- `modern_industrialization:textures/blocks/machines/pyrolyse_overlays/overlay_front.png` used when the machine is not working.
- `modern_industrialization:textures/blocks/machines/pyrolyse_overlays/overlay_front_active.png` used when the machine is working (i.e. active).

With KubeJS, the texture can go in the following folders respectively:
- `kubejs/assets/modern_industrialization/textures/blocks/machines/pyrolyse_overlays/overlay_front.png`.
- `kubejs/assets/modern_industrialization/textures/blocks/machines/pyrolyse_overlays/overlay_front_active.png`.

### Extra multiblock configuration options
Both steam and electric have an optional config function parameter that can be added to the end for further customization. 
```
    event.simpleSteamCraftingMultiBlock(
        /* GENERAL PARAMETERS */
        // English name, internal name, recipe type, multiblock shape
        "Pyrolyse Oven", "pyrolyse_oven", PYROLYSE_OVEN, pyrolyseShape,
        /* REI DISPLAY CONFIGURATION */
        // REI progress bar
        event.progressBar(77, 33, "arrow"),
        // REI item inputs, item outputs, fluid inputs, fluid outputs
        itemInputs => itemInputs.addSlots(56, 35, 1, 2), itemOutputs => itemOutputs.addSlot(102, 35),
        fluidInputs => fluidInputs.addSlot(36, 35), fluidOutputs => fluidOutputs.addSlot(122, 35),
        /* MODEL CONFIGUATION */
        // casing of the controller, overlay folder, front overlay?, top overlay?, side overlay?
        "heatproof_machine_casing", "pyrolyse_overlays", true, false, false,
        /* OPTIONAL CONFIGURATION */
        config => {}
    );
})
```
Methods that config exposes:

### `config.steamCustomOverclock()`
* Only on steam multiblocks, allows changing the overclock amount and duration for the given items or blocks.
* Same as single block [overclocking](#overclocking).



### `config.reiExtra(rei => {})`
* Allows extra / advanced REI configuration. Methods can be chained.
* Default for steam machines sets `steam(true)` and calls nothing for electric.
* `reiExtra(rei => rei.workstations('pyrolyse_oven', 'steam_quarry'))`
  * Groups or omits other machines into the workstations part of REI regardless of recipe type.
* `reiExtra(rei => rei.steam(false))`
  * True to display the machine as steam only, false to show as both electric and steam.
* `reiExtra(rei => rei.extraTest(recipe => recipe.eu > 4))`
  * Filters out showing REI recipes that don't match the test criteria.

## Adding new casing types
See [MACHINE_MODELS.md](MACHINE_MODELS.md) for an explanation of machine models and what casings are.
You can add new casings using the `register` function in the `MIMachineEvents.registerCasings` event.

Remember that the top, side and bottom textures of a casing must be `modern_industrialization:textures/block/casings/<casing name>/{top,side,bottom}.png`.

For example, to register two new casings:
```js
MIMachineEvents.registerCasings(event => {
    event.register("my_fancy_casing", "my_other_casing");
})
```