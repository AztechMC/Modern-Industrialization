# Adding nuclear fuels
To add fission fuel rods and control rods, you will need to use a startup script and the events in `MIMaterialEvents`
Fuel rods and control rods are parts that can be added to a new or preexisting material.

Isotope Fuel Parameters and Isotope Parameters can also be added using the `MIRegistrationEvents.registerNuclearParams` event

## Add a fuel rod
Here is an example script that adds a new `LE Americium MOX` material that has fuel rods.
For a full tutorial on how to add new materials, refer to `ADDING_MATERIALS.md`

```javascript
MIMaterialEvents.addMaterials(event => {
    event.createMaterial('LE Americium MOX', 'le_americium_mox', 0x83867B,
    builder => {
        // Every material with a fuel rod needs at least the ingot and rod parts
        builder.addParts('ingot', 'rod') 
               .fuelRod(
                    0.6,     // Thermal absorption probabilty
                    0.35,    // Thermal scattering
                    2400,    // Max temperature
                    900,     // Temperature limit (high)
                    2300,    // Temperature limit (low)
                    8,       // Neutron multiplication
                    0.5),    // Direct energy factor
               .defaultRecipes();
    });
});
```

You can also mix two different isotope fuel parameters. This can be used to simulate MOX and MOX-type fuels.
The resulting parameters will be `r * a + (1 - b) * b`, where `r` is the factor, `a` is the first set of parameters and `b` is the second 
The example below illustrates how a fuel rod can be created from the U238 and Pu239 parameters
This generates the same parameters as LE MOX fuel

``` javascript
MIMaterialEvents.addMaterials(event => {
    event.createMaterial('LE Americium MOX', 'le_americium_mox', 0x83867B,
    builder => {
        builder.addParts('ingot', 'rod') 
               .fuelRod(
                  U238,       // First params
                  Pu239,      // Second params
                  0.11111),   // Factor
               .defaultRecipes();
    });
});
```

Alternatively, you can use preexisting isotope fuel parameters:

``` javascript
MIMaterialEvents.addMaterials(event => {
    event.createMaterial('LE Americium MOX', 'le_americium_mox', 0x83867B,
    builder => {
        builder.addParts('ingot', 'rod') 
               // MI adds the following entries: U235, U238, Pu239, U, LEU, HEU, LE_MOX and HE_MOX
               .fuelRod('LE_MOX'), 
               .defaultRecipes();
    });
});
```

If you want to, you can create your own isotope fuel parameters and then add them to a control rod.
You will need to use a startup script and the `MIRegistrationEvents.registerNuclearParams` event:

``` javascript
MIRegistrationEvents.registerNuclearParams(event => {
    event.create(
        'AMERICIUM',   // Parameters name
        0.9,           // Thermal absorption probability
        0.35,          // Thermal scattering
        3500,          // Max temperature
        1200,          // Temperature limit (low)
        3200,          // Temperature limit (high)
        11,            // Neutron multiplication
        0.7);          // Direct energy factor
});
```

Then, you can add your new parameters to a fuel rod:
``` javascript
MIMaterialEvents.addMaterials(event => {
    event.createMaterial('LE Americium MOX', 'le_americium_mox', 0x83867B,
    builder => {
        builder.addParts('ingot', 'rod') 
               .fuelRod('AMERICIUM'),     // The isotope fuel params go here
               .defaultRecipes();
    });
});
```

## Add a control rod
Here is an example script that adds a new `Boron` material that has a control rod:

```javascript
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
                    1),       // Size
               .defaultRecipes();
    });
});
```

You can also use preexisting isotope parameters:
``` javascript
MIMaterialEvents.addMaterials(event => {
    event.createMaterial('Boron', 'boron', 0x493D35,
    builder => {
        // Every material that has a control rod needs at least the ingot and rod parts
        builder.addParts('ingot', 'rod')
                // MI adds the following entries: HYDROGEN, DEUTERIUM, CADMIUM, CARBON and INVAR
                // You still need to add the max temperature, heat conduction, scattering type and size
               .controlRod(
                    1900,       // Max temperature
                    0.5,        // Heat conduction
                    'HEAVY',    // Scattering type
                    'CADMIUM',  // Isotope params
                    1),         // Size
               .defaultRecipes();
    });
});
```

If you want to, you can create your own isotope parameters and then add them to a control rod.
You will need to use a startup script and the `MIRegistrationEvents.registerNuclearParams` event:

``` javascript
MIRegistrationEvents.registerNuclearParams(event => {
    event.create(
         'BORON',  // Parameter name
         0.95,     // Thermal absorption probability
         0.9,      // Fast absorption probability
         0.05,     // Thermal scattering probability
         0.1);     // Fast scattering probability
});
```

Then, you can add them to your control rod:
``` javascript
MIMaterialEvents.addMaterials(event => {
    event.createMaterial('Boron', 'boron', 0x493D35,
    builder => {
        // Every material that has a control rod needs at least the ingot and rod parts
        builder.addParts('ingot', 'rod')
                // You still need to add the max temperature, heat conduction, scattering type and size
               .controlRod(
                    1900,       // Max temperature
                    0.5,        // Heat conduction
                    'HEAVY',    // Scattering type
                    'BORON',    // Isotope params
                    1),         // Size
               .defaultRecipes();
    });
});
```

## Add a fluid nuclear component
These types of fluids are the ones that have interactions inside a Nuclear Reactor (think water producing deuterium)
You can add your own using a startup script and the `MIRegistrationEvents.registerFluidNuclearComponents` event
Remember you can add fluids and isotope parameters using KubeJS
Here's an example script that adds tritium turning into hydrogen-4:

``` javascript
MIRegistrationEvents.registerFluidNuclearComponents(event => {
    event.register(
    'modern_industrialization:tritium',    // The fluid being added as a nuclear component 
    5,                                     // Heat conduction (multiplied by the base heat conduction, 0.01)
    1,                                     // Density
    'ULTRA_LIGHT',                         // Scattering type
    'TRITIUM',                             // Isotope parameters for the fluid nuclear component
    'modern_industrialization:hydrogen_4', // The fluid product
    false);                                // Is the fluid a high pressure one?
});
```

### Removing and modifying a fluid nuclear component
You can remove a fluid nuclear component:

``` javascript
MIRegistrationEvents.registerFluidNuclearComponents(event => {
    event.remove('minecraft:water');     // Remove water as a fluid nuclear component
});
```

If, instead of removing the fluid nuclear component, you'd rather modify it, you can:
``` javascript
MIRegistrationEvents.registerFluidNuclearComponents(event => {
    event.modify(
    'minecraft:water',                        // The fluid nuclear component being modified
    5,                                        // New heat conduction (multiplied by the base heat conduction, 0.01)
    1,                                        // New density
    'ULTRA_LIGHT',                            // New scattering type
    'TRITIUM',                                // New isotope parameters
    'modern_industrialization:heavy_water',   // New fluid product
    false);                                   // New high pressure boolean
});
```