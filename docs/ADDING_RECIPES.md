# Recipe format and KubeJS integration
In general, a machine recipe JSON in MI has the following properties:
- `eu`: How many EU/t it uses.
- `duration`: How many ticks it takes to complete.
- `item_inputs`: The list of items it consumes.
- `item_outputs`: The list of items it produces.
- `fluid_inputs`: The list of fluids it consumes.
- `fluid_outputs`: The list of fluids it produces.
- `process_conditions`: A list of additional conditions for the recipe to be processed by the machine.

You can find plenty of examples for the JSON format in [our files](../src/main/resources/data/modern_industrialization/recipes).
For example, the assembler recipe for the trash can looks [like this](../src/main/resources/data/modern_industrialization/recipes/trash_can_assembler.json).

MI adds a nicer syntax when the recipes are being registered by KubeJS:
- You need to create the recipe by specifying the values for `eu` and `duration`.
- Then you can add inputs and outputs by calling the following functions. The can be called multiple times of course:
  - `itemIn(input specification)`: Add a new input.
  - `itemIn(input specification, consumption chance)`: Add a new chanced input. Chance can be `0` if the input should not be consumable.
  - `itemOut(output specification)` and `itemOut(output specification, production chance)`: Add a new output.
  - `fluidIn(fluid, amount in millibuckets)` and `fluidIn(fluid, amount in millibuckets, consumption chance)`: Add a new fluid input. Chance can be `0` if the input should not be consumable.
  - `fluidOut(fluid, amount in millibuckets)` and `fluidOut(fluid, amount in millibuckets, production chance)`: Add a new fluid output.

Here is an example to get you started:
```js
ServerEvents.recipes(event => {
    // start a new recipe for the blast furnace, with 128 EU/t and a duration of 100 ticks
    event.recipes.modern_industrialization.blast_furnace(128, 100)
        // add all the inputs and outputs:
        .itemIn("64x #minecraft:logs_that_burn")
        .itemOut("64x minecraft:charcoal")
        .fluidIn("1000x modern_industrialization:oxygen")
        .fluidOut("5000x modern_industrialization:creosote")
})
```

## Process conditions
The easiest way to add process conditions is via KubeJS, similarly to how inputs and outputs are added.
Here is the list of currently supported conditions:
- `dimension(dimension key)`: Requires the machine to be in the specified dimension.
- `biome(biome key)`: Requires the machine to be in the specified biome.
- `biomeTag(biome tag key)`: Requires the machine to be in a biome of the specified tag.
- `adjacentBlock(block, position)`: Requires a specific block to be next to the machine.
  - Position indicates where the block should be.
  - For multiblocks, the position is always relative to the controller.
  - For now, the only supported positions are `"below"` and `"behind"`.

Here is an example that removes the default bronze drill quarry recipe, and adds one that requires the machine to be in the overworld and right above bedrock:
```js
ServerEvents.recipes(event => {
    event.remove({id: "modern_industrialization:quarry/bronze"})
    event.recipes.modern_industrialization.quarry(4, 600)
        .itemIn("modern_industrialization:bronze_drill", 0.04)
        .itemOut("minecraft:iron_ore", 0.4)
        .itemOut("minecraft:coal_ore", 0.4)
        .itemOut("minecraft:copper_ore", 0.2)
        .itemOut("modern_industrialization:tin_ore", 0.3)
        .itemOut("minecraft:gold_ore", 0.15)
        .itemOut("minecraft:redstone_ore", 0.2)
        .dimension("overworld")
        .adjacentBlock("minecraft:bedrock", "below")
})
```

## Custom process conditions
You can also add your own process conditions by writing KubeJS checks directly, they can be slow so don't use too many of them.

First, we need to register the condition and its description using the `MIRecipeEvents.customConditions` event.
The description is mandatory and will be used in the display of the recipe in REI.
Then, we can use it in the recipe. Here is a full example:

```js
// Add "odd_x_pos" condition that requires the machine to be in an odd X position
MIRecipeEvents.customCondition(event => {
    event.register("odd_x_pos", // ID of the condition
            // condition itself, receives the machine context and the recipe that is being checked
            (context, recipe) => {
                return context.getBlockEntity().getBlockPos().getX() % 2 !== 0;
            },
            // description for REI-like mods
            Text.of("Must be placed on an odd X position"));
});

// Add recipe that uses the condition, see above
ServerEvents.recipes(event => {
    event.recipes.modern_industrialization.compressor(2, 200)
        .itemIn("dirt")
        .itemOut("diamond")
        // Use custom condition defined above
        .customCondition("odd_x_pos")
});
```

## Registered process conditions
If a process condition doesn't have direct KubeJS support (like from an addon mod), you can manually write its parameters using `registeredCondition(JsonObject)`:

```js
// Manually adding a dimension condition - of course, this is just an example and you don't have a reason to actually do it this way.
ServerEvents.recipes(event => {
    event.recipes.modern_industrialization.compressor(2, 200)
        .itemIn("dirt")
        .itemOut("diamond")
        .registeredCondition({
          "modern_industrialization:dimension": {
            "dimension": "minecraft:the_nether"
          }
        })
});
```

## Adding multiblock slots
Multiblock machines always have an unlimited number of input and output slots
(provided the recipe type allows the relevant input/output types).
However, only a limited number of slots is shown in EMI/JEI/REI.

MI adds an event that you can use to add slots to multiblock machines.
The event is called `MIMachineEvents.addMultiblockSlots` **and must be in a startup script**.

The event registration needs a parameter which is the identifier of the category in EMI/JEI/REI.
In the event, you can use `event.<type of slots>.addSlot(slot x position, slot y position)` to add a slot.
The `<type of slots>` can be one of the following:
- `itemInputs`,
- `itemOutputs`,
- `fluidInputs`,
- `fluidOutputs`.

Here is an example to add a fluid output slot to all 3 blast furnace categories:
```js
MIMachineEvents.addMultiblockSlots("steam_blast_furnace", event => {
    event.fluidOutputs.addSlot(122, 53);
})
MIMachineEvents.addMultiblockSlots("electric_blast_furnace_cupronickel_coil", event => {
    event.fluidOutputs.addSlot(122, 53);
})
MIMachineEvents.addMultiblockSlots("electric_blast_furnace_kanthal_coil", event => {
    event.fluidOutputs.addSlot(122, 53);
})
```

The category ID can be found in various ways, for example by inspecting the `en_us.json` lang file:
```json
{
  "rei_categories.modern_industrialization.electric_blast_furnace_0": "EBF (Cupronickel Tier)",
  "rei_categories.modern_industrialization.electric_blast_furnace_1": "EBF (Kanthal Tier)"
}
```
From this we deduce that the EBF categories are `electric_blast_furnace_0` and `electric_blast_furnace_1`.
