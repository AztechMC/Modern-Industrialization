# Developer-facing Documentation
This is the Modern Industrialization documentation aimed at mod pack or resource pack developers.
For questions, please ask on the `#dev-talk` channel on the Discord server.

## Customizing machine models
Refer to [MACHINE_MODELS.md](MACHINE_MODELS.md) for an explanation of how the machine model format works.
This will be useful for resource pack developers,
or for pack developers who want to customize the look of their KubeJS-added machines.

## Recipe format and KubeJS integration
MI comes with KubeJS integration for its custom recipes.
Please refer to [ADDING_RECIPES.md](ADDING_RECIPES.md) for details on the syntax.

## KubeJS content customization
MI has more advanced KubeJS integration starting from version 1.5.0,
which allows modpack developers to customize many aspects of MI.

**Please note that this integration is still experimental, and might change at any time.
However, it is unlikely to change drastically, hence adjusting for newer versions shouldn't take too much effort.**

### Preliminary steps
Some content added via the KubeJS integration will require custom resources (for example: machine model files, machine loot tables, translations, etc...).
MI can generate most of these resources for you if you ask it to.
- Set `datagenOnStartup` to `true` in the config file.
  - During the end of startup, MI will generate new resources in the `modern_industrialization/generated_resources` folder.
- Set `loadRuntimeGeneratedResources` to `true` in the config file as well.
  - This will make sure that MI will automatically load the resources generated in the previous step.
- Profit!

#### More info regarding runtime datagen
Runtime datagen works by running the data generators during the end of MI startup.
- It uses `modern_industrialization/runtime_datagen` to produce all the files that are automatically generated.
  - These files include loot tables, most recipes, or material textures, for example.
- The files that are **different** from those in the jar then get copied to `modern_industrialization/generated_resources`.
- Finally, MI injects a data and resource pack to load these resources into the game.

Runtime datagen will only use the resources from the MI jar and the base vanilla assets.
**It will not use any resource pack, as it runs too early.**

However, files can be placed in the `modern_industrialization/extra_datagen_resources` folder if they are needed during datagen,
and they take precedence over the files in the MI jar.
At the moment, this only works for textures.
Here are some examples to get you started:
- Placing a texture in `modern_industrialization/extra_datagen_resources/assets/modern_industrialization/textures/materialsets/common/plate.png`
  will change the texture of most plates.
- The `datagen_texture_overrides` exists to manually override textures produced by datagen, for example specific material textures.
  Placing a texture in `modern_industrialization/extra_datagen_resources/assets/modern_industrialization/datagen_texture_overrides/item/aluminum_plate.png`
  will change the texture of the aluminum plate only.
- Refer to the contents of the MI .jar for more details on the file structure.

### Adding content
Refer to the page that interests you:
- [Cable tiers](ADDING_CABLE_TIERS.md)
- [Fluids](ADDING_FLUIDS.md)
- [Hatches](ADDING_HATCHES.md)
- [Machines](ADDING_MACHINES.md)
- [Materials](ADDING_MATERIALS.md)
- [Nuclear reactor components](ADDING_NUCLEAR_COMPONENTS.md)
