# Developer-facing Documentation
This is the Modern Industrialization documentation aimed at mod pack or resource pack developers.
If you are a player, consult the guidebook and the Discord server for help.

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
- Make sure that `loadRuntimeGeneratedResources` is set to `true` in the config file.
  - This will make sure that MI will automatically load the resources generated in the previous step.
- Profit!

### Adding new machines
Refer to [ADDING_MACHINES.md](ADDING_MACHINES.md) for more information on how to add machines via KubeJS.
