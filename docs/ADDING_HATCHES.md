# Adding hatches
To add new energy, fluid, or item hatches, use the `MIRegistrationEvents.registerHatches` startup event.

## Energy
Use the `energy` registration method to register energy input and output hatches for the given cable tier.
For example:
```js
MIMachineEvents.registerHatches(event => {
    // Takes the internal name for the cable tier.
    event.energy("iv");
});
```

The default generated model for energy hatches will use the casing corresponding to the cable tier.

See also [Adding cable tiers](ADDING_CABLE_TIERS.md) for more information on how to add a cable tier.

## Fluid
Use the `fluid` registration method to register fluid input and output hatches.
These hatches are not necessarily bound to a cable or progression tier.

Here is an example:
```js
MIMachineEvents.registerHatches(event => {
    event.fluid(
        // English name for the hatch tier. " Fluid Input/Output Hatch" will be added by MI.
        "Huge",
        // Internal name for the tier.
        // In this cases, the block IDs of the hatches will be huge_fluid_input_hatch and huge_fluid_output_hatch.
        "huge",
        // Machine casing for the model of the hatch.
        "superconductor",
        // Number of buckets that the hatch can hold.
        1000,
    );
});
```

## Item
Use the `item` registration method to register item input and output hatches.
These hatches are not necessarily bound to a cable or progression tier.

Here is an example:
```js
MIMachineEvents.registerHatches(event => {
    event.item(
        // English name for the hatch tier. " Item Input/Output Hatch" will be added by MI.
        "Huge",
        // Internal name for the tier.
        // In this cases, the block IDs of the hatches will be huge_item_input_hatch and huge_item_output_hatch.
        "huge",
        // Machine casing for the model of the hatch.
        "superconductor",
        // Number of slot rows and columns for the hatch slots:
        3, 7, // 3 rows and 7 columns here
        // Starting position of the slot grid:
        10, 18, // the first slot will be at (10, 18) inside the GUI
    );
});
```
