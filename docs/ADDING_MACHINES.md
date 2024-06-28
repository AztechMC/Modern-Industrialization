# Adding Machines
To add machines, you will need to use a startup script and the events in `MIMachineEvents`.

## Adding new tiers for the Electric Blast Furnace
MI supports adding new EBF tiers from KubeJS using the `addEbfTiers` event. The two builtin tiers cannot be removed however.

For each tier, call `event.add` with:
- The ID of the coil block, used inside of the shape. Can be any block that also has an item.
- The maximum allowed EU/t of the recipe. This is the maximum EU/t that the coil can handle.
- The English name for display in the EBF shape selector and in REI. MI will automatically wrap that in `EBF (... Tier)` for REI.

Here is an example that adds gold blocks as an EBF coil with a maximum EU/t of 64:
```js
MIMachineEvents.addEbfTiers(event => {
    // ID of the coil block, max EU/t, English name
    event.add("minecraft:gold_block", 64, "Gold");
})
```

## Adding electric machine upgrades
You can add electric machine upgrades using KubeJS.
This will allow them to be used in all the electric machines to increase the maximum EU that the machine can handle.

Example:
```js
MIMachineEvents.registerUpgrades(event => {
  // id of the upgrade item, number of added EU per upgrade
  event.register("minecraft:diamond", 42);
});
```

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

**This is the low-level system, please read on for a nicer way to define multiblock shapes.**

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

### Simplified shape creation using layered shapes
It is hard to write a shape using the previous method.
MI offers an alternative way to build shapes using layer definitions, similarly to crafting recipes.

The keys are arranged in such a way that each 2D group of characters represents a horizontal slice of the multiblock.
Each key must be assigned a block and a set of hatches.
The character `#` is used for the controller, and a space is used for air.

```js
MIMachineEvents.registerMachines(event => {
    const pyrolyseHatch = event.hatchOf("item_input", "item_output", "fluid_input", "fluid_output", "energy_input");
    const heatproofMember = event.memberOfBlock("modern_industrialization:heatproof_machine_casing");
    const cupronickelCoilMember = event.memberOfBlock("modern_industrialization:cupronickel_coil");
    const pyrolyseShape = event.layeredShape("heatproof_machine_casing", [
        [ "HHH", "HHH", "HHH" ],
        [ "CCC", "C C", "CCC" ],
        [ "CCC", "C C", "CCC" ],
        [ "HHH", "H#H", "HHH" ],
    ])
        .key("H", heatproofMember, pyrolyseHatch)
        .key("C", cupronickelCoilMember, event.noHatch())
        .build();

    // register multiblock as steam or electric next...
```

In this example, the bottom layer (`y = -1`) of the shape is represented by
```
HHH
CCC
CCC
HHH
```
Then the middle layer (`y = 0`) is
```
HHH
C C
C C
H#H
```
Finally the top layer (`y = 1`) is
```
HHH
CCC
CCC
HHH
```

Here is how the axes work in a single layer:
```
^
| behind
| the
| controller
|
|
|                    right
|                    of the
|                    controller
+ ----------------------------->
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
You can add new casings using the `MIMachineEvents.registerCasings` event.
- Use the `register` function if you will also be adding a JSON model for the casing.
- Use the `registerBlockImitation` function to add a casing that will imitate another block.

If you use `register` and want a texture-based casing model,
remember that the top, side and bottom textures of a casing must be `modern_industrialization:textures/block/casings/<casing name>/{top,side,bottom}.png`.

For example:
```js
MIMachineEvents.registerCasings(event => {
    // Register two casings.
    // This doesn't register any model! Either add models or add the top/side/bottom textures.
    event.register("my_fancy_casing", "my_other_casing");

    // This registers a new casing with the same model as a diamond block!
    event.registerBlockImitation("my_diamond_casing", "minecraft:diamond_block");
})
```

**This only registers the casing for use in a machine model, but does not create a casing block.
To add casings, use either KubeJS custom blocks or the material system.**


## Adding a single block generator

A single block generator is a block that generates energy at a constant rate from consuming fluids or item, like the diesel generator or the steam turbine.
The registration is similar to a single block crafting machine but will take different parameters.

For example, 
```js
MIMachineEvents.registerMachines(event => {
    event.simpleGeneratorSingleBlock(
        "EV Diesel Generator", // the generator english name
        "ev_diesel_generator", // its internal name/id
        "ev", // the cable tier it can connect to (eg: lv, mv, hv, ev, superconductor)
        8192, // its maximum energy generation rate (eu/tick)
        50000, // its internal energy storage (eu)
        128000, // its fluid storage (mB), this is optional if it doesn't consume fluids (default is 0)
        builder => {
            builder.fluidFuels() // the builder is used to specify which kind of fuel it will accept and 
            // how much energy it will generate from it. (See below)
        }, 
            // ---- SAME AS FOR A SINGLE BLOCK CRAFTING MACHINE ----
        "ev",  // the casing 
        "diesel_generator", // the folder of the model
            // front overlay?, top overlay?, side overlay?
        true, true, true
    )
});
```
The builder accepts the following methods (which can be chained):

```js

builder.fluidFuels() // This will make the generator accept any fluid fuels with their standard EU/mb value (same fuels as the diesel generator)
builder.furnaceFuels() // This will make the generator any combustible items. The EU/item is 20 x the number of burning ticks.
builder.item("minecraft:coal", 100) // This will make the generator accept coal and generate 100 EU/item. Cannot be combined with furnaceFuels().
builder.fluid("minecraft:lava", 10) // This will make the generator accept lava and generate 10 EU/mb. Cannot be combined with fluidFuels().

// multiple fuels can be added ex:
builder.item("minecraft:coal", 100).fluid("minecraft:lava", 10); 
// will automatically add the correct input slot in the machine inventory
```

### Adding a Multiblock Generator

The logic is the same as for a single block generator, but the registration method is different.
```js
MIMachineEvents.registerMachines(event => {
     const largeSolidDieselGeneratorShape = //... Define your shape here as for any multiblock

    event.simpleGeneratorMultiBlock(
        "Large Solid Diesel Generator", // The english name
        "large_solid_diesel_generator", // The internal name
        largeSolidDieselGeneratorShape, // The multiblock shape
        512, // Maximum energy generation rate (eu/tick)
        builder => { // The builder (same as for a single block generator)
            builder.furnaceFuels().fluid("modern_industrialization:synthetic_oil", 100);
        },
            // --- Standard model configuration --- //
        "heatproof_machine_casing", // casing
        "diesel_generator", // model folder
        true, false, false // front overlay?, top overlay?, side overlay?
    );
});
```
