events.listen('recipes', function (event) {
    event.forEachRecipe({ type: "minecraft:stonecutting" }, function (recipe) {
        var json = utils.mapOf(recipe.json)
        var ingredient = json.ingredient
        var i = { amount: 1, ingredient: json.ingredient }
        event.recipes.modern_industrialization.cutting_machine({
            eu: 2,
            duration: 200,
            item_inputs: i,
            fluid_inputs: {
                fluid: "minecraft:water",
                amount: 1
            },
            item_outputs: {
                item: json.result,
                amount: json.count,
            },
            id: recipe.id + "_exported_mi_cutting_machine",
        })
    })
})