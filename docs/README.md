# Developer-facing Documentation
This is the Modern Industrialization documentation aimed at mod pack or resource pack developers.
If you are a player, consult the guidebook and the Discord server for help.

## KubeJS recipe integration
MI comes with KubeJS integration for its custom recipes.
The only visible effect, is that KubeJS commands like `replaceInputs` and `replaceOutputs`
will work correctly on MI custom recipes.

## Advanced KubeJS integration
Modern Industrialization has advanced KubeJS integration starting from version 1.5.0,
which allows modpack developers to customize many aspects of MI.

**Please note that this integration is still experimental, and subject to change.**

### Preliminary steps
Some content added via the KubeJS integration will require custom resources (for example: machine model files, machine loot tables, translations, etc...).
MI can generate most of these resources for you if you ask it to.
- Set `datagenOnStartup` to `true` in the config file.
  - Towards the end of startup, MI will generate new resources in the `modern_industrialization/generated_resources` folder.
- Make sure that `loadRuntimeGeneratedResources` is set to `true` in the config file.
  - This will make sure that MI will load the resources generated in the previous step.
- Profit!

### Adding new machines
The feature and its documentation are a work in progress.

