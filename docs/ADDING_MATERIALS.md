# Adding Machines
To add machines, you will need to use a startup script and the events in `MIMaterialEvents`

## Material System

Modern Industrialization uses a material system to determine the properties of items and 
blocks. Each material has a list of parts, each one associated to one and only one `PartKey`.
Modern Industrialization will create the item/block and its texture automatically. 

## Add a Material 

Here is an example script that adds a new `Zinc` material:


```javascript
MIMaterialEvents.addMaterials(event => {
    event.createMaterial("Zinc", "zinc", 0xd68b7a, // english name, internal name, and material color in hex
        builder => {
        builder.addParts("ingot", "nugget", "dust", "tiny_dust", "rod", "gear", "ring", "blade", "rotor", "coil", "plate", "bolt", "large_plate")
            // addParts adds the simple parts to the material ie, the one already defined in MI and that don't need more parameters 
            .barrel("Super Barrel", "super_barrel", 69) // add a barrel with 69 stacks capacity and custom english name and path (both optional)
            .tank("Super Tank", "super_tank", 42) // add a tank same as above but for buckets capacity
            .block("copper") // add a simple block with the "copper" texture (found in "textures/materialsets/blocks") 
            .cable("ev") // add an EV tiers cable 
            .specialCasing("Super Zinc Casing", "super_zinc_casing", 8.0) // add a special casing with custom english name and path (both required) 
            // and 8.0 blast resistance (also optional, default is 6.0)
            .machineCasing(8.0) // same as above but for machine casings but the custom name and path are optional
            .pipeCasing(8.0) // add a pipe casing, only blast resistance can be specified
            .ore({ 
                "generate": true, // does the ore generate in the world
                "ore_set": "copper", // texture set, same principle as for blocks (found in "textures/materialsets/ores")
                "vein_size": 8, // the vein size
                "veins_per_chunk": 8, // the number of veins per chunk
                "max_y": 64, // the maximum y level
                // the generation parameters are only required if generate is true
                /*
                "min_xp" : 0
                "max_xp" : 0
                 max and min xp dropped by the ore, default is 0 for both. Must be zero if the ore drop raw ores 
                 */
            }) // }, true) a second boolean optional parameter can be passed to only add the deepslate or if true or normal ore if false
            // default is to add both
            .rawMetal("copper") // add a raw metal with the "copper" texture (found in "textures/materialsets/raw"), same principle as for blocks
            // as for ore, a second boolean optional parameter can be passed to only add the raw ore block if true or the raw ore item if false
            // default is to add both, equivalent to .rawMetal("copper", true).rawMetal("copper", false)
            .defaultRecipes() // add the default recipes for the material (crafting, smelting and machines)
            .forgeHammerRecipes(); // add the forge hammer recipes 
    });
});
```