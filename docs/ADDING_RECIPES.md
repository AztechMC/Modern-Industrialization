# Adding machine recipes via KubeJS
In general, a machine recipe JSON in MI has the following properties:
- `eu`: How many EU/t it uses.
- `duration`: How many ticks it takes to complete.
- `item_inputs`: The list of items it consumes.
- `item_outputs`: The list of items it produces.
- `fluid_inputs`: The list of fluids it consumes.
- `fluid_outputs`: The list of fluids it produces.

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
        .fluidIn("modern_industrialization:oxygen", 1000)
        .fluidOut("modern_industrialization:creosote", 5000)
})
```