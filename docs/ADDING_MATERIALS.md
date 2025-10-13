# Adding Materials
To add materials, you will need to use a startup script and the events in `MIMaterialEvents`

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
        builder
            .hardness("soft") // hardness controls the speed of some recipes, can be "soft", "average", "hard", "very_hard" (default is "average")
            .materialSet("shiny") // controls the texture set used by the material, can be "metallic", "shiny", "stone", "dull" (default is "metallic")
            // addParts adds the simple parts to the material ie, the one already defined in MI and that don't need more parameters
            .addParts("ingot", "nugget", "dust", "tiny_dust", "rod", "gear", "ring", "blade", "rotor", "coil", "plate", "bolt", "large_plate")
            // add a part that doesn't exist in MI
            // template texture goes in modern_industrialization/extra_datagen_resources/assets/modern_industrialization/textures/materialsets/common/long_rod.png
            .customRegularPart("Long Rod", "long_rod")
            .barrel("Super Barrel", "super_barrel", 69) // add a barrel with 69 stacks capacity and custom english name and path (both optional)
            .tank("Super Tank", "super_tank", 42) // add a tank same as above but for buckets capacity
            .block("copper") // add a simple block with the "copper" texture (found in "textures/materialsets/blocks")
            .cable("ev") // add an EV tiers cable
            .specialCasing("Super Zinc Casing", "super_zinc_casing", 8.0) // add a special casing with custom english name and path (both required)
            // and 8.0 blast resistance (also optional, default is 6.0)
            .machineCasing(8.0) // same as above but for machine casings but the custom name and path are optional
            .pipeCasing(8.0) // add a pipe casing, only blast resistance can be specified
            .battery(5000000) // add a battery with 5M EU capacity for the Portable Storage Unit
            .ore({
                "ore_set": "copper", // texture set, same principle as for blocks (found in "textures/materialsets/ores")
                "generate": true, // does the ore generate in the world? (default: true)
                // these generation parameters are only required if generate is true
                "vein_size": 8, // the vein size
                "veins_per_chunk": 8, // the number of veins per chunk
                "max_y": 64, // the maximum y level
                "biome_tag": "minecraft:is_overworld", // which biome(s) the ore should generate in. (default: "minecraft:is_overworld")
                /*
                 max and min xp dropped by the ore, default is 0 for both. Must be zero if the ore drops raw ores
                "min_xp" : 0
                "max_xp" : 0
                 */
            }) // }, "minecraft:tuff") a second optional string parameter can be passed to dictate what stone type the ore is made of.
            // default behavior will create stone and deepslate ores
            .rawMetal("copper") // add a raw metal with the "copper" texture (found in "textures/materialsets/raw"), same principle as for blocks
            // as for ore, a second boolean optional parameter can be passed to only add the raw ore block if true or the raw ore item if false
            // default is to add both, equivalent to .rawMetal("copper", true).rawMetal("copper", false)
            .defaultRecipes() // add the default recipes for the material (crafting, smelting and machines)
            .forgeHammerRecipes(); // add the forge hammer recipes
    });
});
```

<!-- TODO: add gem example -->

### Using already defined items as parts

If you want a material for which items (from either vanilla or other mod) already exist, you can use the `addExternalPart` method

```javascript
MIMaterialEvents.addMaterials(event => {
    event.createMaterial("Obsidian", "obsidian", 0x332938, builder => {
        builder.addParts("ingot", "nugget", "dust", "tiny_dust",  "plate", "double_ingot", "large_plate")
            .addExternalPart("block", "minecraft:obsidian") // this will use the vanilla obsidian block as the 'block' part in every recipes
            .defaultRecipes();
    });
});

MIMaterialEvents.addMaterials(event => {
    event.createMaterial("Wood", "wood", 0x40321D, builder => {
        builder.addParts("nugget", "dust", "tiny_dust", "plate", "double_ingot", "large_plate")
            .addExternalPart("ingot", "minecraft:oak_log", "#minecraft:logs")
            // the method can take a third argument to specify the tag to use in recipes input (musts start with an '#').
            //  Be careful that the item must be in the tag, otherwise the recipe will not work
            // (To avoid conflicts bewteen mods, tags are not used in vanilla recipe types (crafting, smelting, etc.))
            .defaultRecipes();
    });
});

```

## Modify a Material

Most of the syntax is shared with adding a material once you get access to the material builder.
For example, here is how to add a diamond tank:
```js
// Material name to only modify that specific material
MIMaterialEvents.modifyMaterial("diamond", event => {
    event.builder
        // Call any builder function, see above
        .tank(64)
})
```

## Modify ALL Materials

Don't specify a material name in the `modifyMaterial` event registration to modify all materials.
For example, here is how to add a small gear to all materials:
```js
// No material name to modify all materials
MIMaterialEvents.modifyMaterial(event => {
    event.builder
        // Add a custom small gear part to ALL materials, see above
        .customRegularPart("Small Gear", "small_gear")
})
```
