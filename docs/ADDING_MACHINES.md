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

Here is an example, where we add a circuit assembler that looks very much like a normal assembler:
```js
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
    items => items.addSlots(42, 27, 3, 3).addSlots(139, 27, 3, 1), fluids => {},
    /* MODEL CONFIGURATION */
    // front overlay?, top overlay?, side overlay?
    true, true, false,
);
```

## Adding an electric multiblock crafting machine
These are the standard electric multiblock machines, such as the pressurizer, the electric quarry, etc...

Here is an example, where we add a pyrolyse oven multiblock:
```js
MIMachineEvents.registerMachines(event => {
    const pyrolyseHatch = event.hatchOf("item_input", "item_output", "fluid_input", "fluid_output");
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

    event.simpleElectricCraftingMultiBlock(
        /* GENERAL PARAMETERS */
        // English name, internal name, recipe type, multiblock shape
        "Pyrolyse Oven", "pyrolyse_oven", PYROLYSE_OVEN, pyrolyseShape,
        /* REI DISPLAY CONFIGURATION */
        // REI progress bar
        event.progressBar(77, 33, "arrow"),
        // REI item inputs, item outputs, fluid inputs, fluid outputs
        itemInputs => itemInputs.addSlots(56, 35, 2, 1), itemOutputs => itemOutputs.addSlot(102, 35),
        fluidInputs => fluidInputs.addSlot(36, 35), fluidOutputs => fluidOutputs.addSlot(122, 35),
        /* MODEL CONFIGUATION */
        // casing, overlay folder, front overlay?, top overlay?, side overlay?
        "heatproof_machine_casing", "electric_blast_furnace", true, false, false,
    );
})
```