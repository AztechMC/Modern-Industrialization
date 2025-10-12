# Adding nuclear reactor components
MI allows you to add new nuclear reactor components using KubeJS.
Currently, fuel rods, control rods and fluid-neutron interactions can be registered.

To add fission fuel rods and control rods, you will need to use a startup script and the events in `MIMaterialEvents`
Fuel rods and control rods are parts that can be added to a new or preexisting material.

Isotopes are used in the registration of rods.
They can be added using the `MIRegistrationEvents.registerNuclearIsotopes` event.

## Add a fuel rod
Read the [material system documentation](`ADDING_MATERIALS.md`) first for an explanation of the material system.

To register fuel rods for a material, you must first register the material as a fission fuel.
This can be done using `.nuclearFuel(...)` on the material builder with a number of parameters:
```js
MIMaterialEvents.addMaterials(event => {
    event.createMaterial('LE Americium MOX', 'le_americium_mox', 0x83867B,
        builder => {
        builder
            .nuclearFuel(
                0.6,     // Thermal absorption probabilty
                0.35,    // Thermal scattering
                2400,    // Max temperature
                900,     // Low temperature limit (neutron production starts to decrease)
                2300,    // High temperature limit (neutron production completely stops)
                8,       // Neutron multiplication
                0.5)     // Direct energy factor
        });
});
```

You can also combine the parameters of two existing materials using the `.nuclearFuelMix(materialA, materialB, mix)` method.
This can be used to simulate MOX and MOX-type fuels.
The resulting parameters will be `(1 - r) * a + r * b`, where `r` is the factor, `a` is the first set of parameters and `b` is the second.
The example below illustrates how a fuel rod can be created from the uranium 238 and plutonium parameters.
This generates the same parameters as LE MOX fuel.
```js
MIMaterialEvents.addMaterials(event => {
    event.createMaterial('LE Americium MOX', 'le_americium_mox', 0x83867B,
        builder => {
        builder
            .nuclearFuelMix("uranium_238", "plutonium", 1/9)
        });
});
```

You then need to call the `.fuelRods()` method on the material builder to register the rods.
Here is a complete example:
```js
MIMaterialEvents.addMaterials(event => {
    event.createMaterial('LE Americium MOX', 'le_americium_mox', 0x83867B,
        builder => {
        builder
            .nuclearFuelMix("uranium_238", "plutonium", 1/9)
            // Every material with a fuel rod needs at least the ingot and rod parts
            .addParts('ingot', 'rod')
            .fuelRods()
        });
});
```

## Add a control rod
To add a control rod, use the `.controlRod(...)` material builder method.

Here is an example script that adds a new `Boron` material that has a control rod:
```js
MIMaterialEvents.addMaterials(event => {
    event.createMaterial('Boron', 'boron', 0x493D35,
        builder => {
            // Every material that has a control rod needs at least the ingot and rod parts
            builder.addParts('ingot', 'rod')
                   .controlRod(
                        1900,     // Max temperature
                        0.5,      // Heat conduction
                        0.95,     // Thermal absorption probability
                        0.9,      // Fast absorption probability
                        0.05,     // Thermal scattering probability
                        0.1,      // Fast scattering probability
                        'HEAVY',  // Scattering type, can be ULTRA_LIGHT (0.05), LIGHT (0.2), MEDIUM (0.5) or HEAVY (0.85)
                        1)        // Size
                   .defaultRecipes();
        });
});
```

## Add a fluid-neutron interaction
These types of fluids are the ones that can absorb neutrons inside a Nuclear Reactor (think water producing deuterium).
You can add your own using a startup script and the `MIRegistrationEvents.registerFluidNeutronInteractions` event.

To create the fluid in the first place, refer to the [fluid creation tutorial](`ADDING_FLUIDS.md`).

Here's an example script that adds tritium turning into hydrogen-4:

```js
MIRegistrationEvents.registerFluidNeutronInteractions(event => {
    event.register(
        'modern_industrialization:tritium',    // The fluid being added as a nuclear component
        5,                                     // Heat conduction factor (later multiplied by the base heat conduction, 0.01)
        1,                                     // Density
        'ultra_light',                         // Scattering type
        0.02,                                  // Thermal absorption probability
        0.01,                                  // Fast absorption probability
        0.15,                                  // Thermal scattering probability
        0.65,                                  // Fast scattering probability
        'modern_industrialization:hydrogen_4', // The fluid product
        1,                                     // Amount of converted product fluid per consumed input fluid
        1.0);                                  // Probability of converting 1 of input fluid per received neutron
});
```
For a high-pressure fluid (with 8x the density), the last two parameters would be `8, 0.125`.

### Removing and modifying a fluid nuclear component
You can remove a fluid nuclear component:

```js
MIRegistrationEvents.registerFluidNeutronInteractions(event => {
    event.remove('minecraft:water');     // Remove water as a fluid nuclear component
});
```

If, instead of removing the fluid nuclear component, you'd rather modify it, you can:
```js
MIRegistrationEvents.registerFluidNeutronInteractions(event => {
    // Same parameters as `register(...)`, see above.
    event.modify(
        'minecraft:water',
        5,
        1,
        'ultra_light',
        0.02,
        0.01,
        0.15,
        0.65,
        'modern_industrialization:heavy_water',
        1,
        1.0);
});
```
