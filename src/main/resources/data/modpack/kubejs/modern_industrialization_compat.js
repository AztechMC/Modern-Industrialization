events.listen('recipes', function (event) {
    function addMiRecipe(machine, input, output, outputAmount) {
        var i = {amount: 1};
        if (input.startsWith('#')) {
            input = input.substring(1);
            i.tag = input;
        } else {
            i.item = input;
        }
        var o = {
            item: output,
            amount: outputAmount
        };
        event.recipes.modern_industrialization[machine]({
            eu: 2,
            duration: 200,
            item_inputs: i,
            item_outputs: o,
            id: 'modern_industrialization:compat_' + machine + '_' + input.split(':')[0] + '_' + input.split(':')[1]
        });
    }

    // Thank you NotSteven for not making me having to go through TR's files !
    if (mod.isLoaded('techreborn')) {
        console.log('Tech Reborn is detected, loading compatibility recipes for Modern Industrialization!');

        addMiRecipe('compressor', 'techreborn:advanced_alloy_ingot', 'techreborn:advanced_alloy_plate', 1)
        addMiRecipe('compressor', '#c:brass_ingots', 'techreborn:brass_plate', 1)
        addMiRecipe('compressor', 'techreborn:carbon_mesh', 'techreborn:carbon_plate', 1)
        addMiRecipe('compressor', 'techreborn:emerald_dust', 'techreborn:emerald_plate', 1)
        addMiRecipe('compressor', '#c:iridium_ingots', 'techreborn:iridium_plate', 1)
        addMiRecipe('compressor', 'techreborn:lazurite_dust', 'techreborn:lazurite_plate', 1)
        addMiRecipe('compressor', 'minecraft:obsidian', 'techreborn:obsidian_plate', 9)
        addMiRecipe('compressor', 'techreborn:obsidian_dust', 'techreborn:obsidian_plate', 1)
        addMiRecipe('compressor', '#c:peridot_dusts', 'techreborn:peridot_plate', 1)
        addMiRecipe('compressor', 'techreborn:plantball', 'techreborn:compressed_plantball', 1)
        addMiRecipe('compressor', '#c:platinum_ingots', 'techreborn:platinum_plate', 1)
        addMiRecipe('compressor', 'minecraft:prismarine_crystals', 'minecraft:prismarine_shard', 1) // TODO
        addMiRecipe('compressor', 'techreborn:quartz_dust', 'techreborn:quartz_plate', 1)
        addMiRecipe('compressor', 'techreborn:red_garnet_dust', 'techreborn:red_garnet_plate', 1)
        addMiRecipe('compressor', 'minecraft:redstone_block', 'techreborn:redstone_plate', 1)
        addMiRecipe('compressor', '#c:ruby_dusts', 'techreborn:ruby_plate', 1)
        addMiRecipe('compressor', '#c:sapphire_dusts', 'techreborn:sapphire_plate', 1)
        addMiRecipe('compressor', '#minecraft:planks', 'techreborn:wood_plate', 1)
        addMiRecipe('compressor', 'techreborn:yellow_garnet_dust', 'techreborn:yellow_garnet_plate', 1)
        addMiRecipe('compressor', '#c:zinc_ingots', 'techreborn:zinc_plate', 1)
        addMiRecipe('compressor', 'techreborn:refined_iron_ingot', 'techreborn:refined_iron_plate', 1)

        addMiRecipe('macerator', 'minecraft:andesite', 'techreborn:andesite_dust', 2)
        addMiRecipe('macerator', '#c:basalt', 'techreborn:basalt_dust', 2)
        addMiRecipe('macerator', '#c:brass_ingots', 'techreborn:brass_dust', 2)
        addMiRecipe('macerator', 'minecraft:charcoal', 'techreborn:charcoal_dust', 2)
        addMiRecipe('macerator', '#c:cinnabar_ores', 'techreborn:cinnabar_dust', 2)
        addMiRecipe('macerator', 'minecraft:clay_ball', 'techreborn:clay_dust', 1)
        addMiRecipe('macerator', 'minecraft:diorite', 'techreborn:diorite_dust', 2)
        addMiRecipe('macerator', 'minecraft:emerald', 'techreborn:emerald_dust', 1)
        addMiRecipe('macerator', 'minecraft:ender_eye', 'techreborn:ender_eye_dust', 2)
        addMiRecipe('macerator', 'minecraft:ender_pearl', 'techreborn:ender_pearl_dust', 1)
        addMiRecipe('macerator', 'minecraft:end_stone', 'techreborn:endstone_dust', 1)
        addMiRecipe('macerator', 'minecraft:flint', 'techreborn:flint_dust', 1)
        addMiRecipe('macerator', '#c:galena_ores', 'techreborn:galena_dust', 2)
        addMiRecipe('macerator', 'minecraft:granite', 'techreborn:granite_dust', 2)
        addMiRecipe('macerator', 'minecraft:netherrack', 'techreborn:netherrack_dust', 1)
        addMiRecipe('macerator', 'techreborn:peridot_gem', 'techreborn:peridot_dust', 1)
        addMiRecipe('macerator', '#c:peridot_ores', 'techreborn:peridot_dust', 2)
        addMiRecipe('macerator', '#c:pyrite_ores', 'techreborn:pyrite_dust', 2)
        addMiRecipe('macerator', 'techreborn:red_garnet_gem', 'techreborn:red_garnet_dust', 1)
        addMiRecipe('macerator', 'techreborn:ruby_gem', 'techreborn:ruby_dust', 1)
        addMiRecipe('macerator', '#c:ruby_ores', 'techreborn:ruby_dust', 2)
        addMiRecipe('macerator', 'techreborn:sapphire_gem', 'techreborn:sapphire_dust', 1)
        addMiRecipe('macerator', '#c:sapphire_ores', 'techreborn:sapphire_dust', 2)
        addMiRecipe('macerator', '#c:sodalite_ores', 'techreborn:sodalite_dust', 2)
        addMiRecipe('macerator', 'techreborn:yellow_garnet_gem', 'techreborn:yellow_garnet_dust', 1)
        addMiRecipe('macerator', '#c:zinc_ingots', 'techreborn:zinc_dust', 1)
        addMiRecipe('macerator', 'minecraft:flint', 'techreborn:flint_dust', 1)
        addMiRecipe('macerator', 'minecraft:flint', 'techreborn:flint_dust', 1)
    }

    if (mod.isLoaded('appliedenergistics2')) {
        console.log('Applied Energistics 2 is detected, loading compatibility recipes for Modern Industrialization!');

        addMiRecipe('macerator', '#c:certus_quartz_crystals', 'appliedenergistics2:certus_quartz_dust', 1)
        addMiRecipe('macerator', '#c:certus_quartz_ores', 'appliedenergistics2:certus_quartz_dust', 5)
        addMiRecipe('macerator', 'appliedenergistics2:fluix_crystal', 'appliedenergistics2:fluix_dust', 1)

        event.recipes.modern_industrialization.quarry({
            eu: 16,
            duration: 600,
            item_inputs: {
                item: 'appliedenergistics2:fluix_glass_cable',
                amount: 1,
                probability: 0.2
            },
            item_outputs: [
                {
                    item: 'appliedenergistics2:quartz_ore',
                    amount: 8,
                    probability: 0.02
                },
                {
                    item: 'appliedenergistics2:charged_quartz_ore',
                    amount: 4,
                    probability: 0.01
                },
            ],
            id: 'modern_industrialization:quarry_ae2'
        })
    }

    if (mod.isLoaded('indrev')) {
        console.log('Industrial Revolution is detected, loading compatibility recipes for Modern Industrialization!');

        addMiRecipe('macerator', 'indrev:nikolite_ore', 'indrev:nikolite_dust', 7)
        event.recipes.modern_industrialization.quarry({
            eu: 16,
            duration: 600,
            item_inputs: {
                item: 'indrev:cable_mk1',
                amount: 1,
                probability: 0.6
            },
            item_outputs: {
                item: 'indrev:nikolite_ore',
                amount: 6,
                probability: 0.03
            },
            id: 'modern_industrialization:quarry_nikolite'
        })
    }
})