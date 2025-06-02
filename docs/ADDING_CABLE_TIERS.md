# Adding cable tiers
Adding cable tiers in MI is very easy.

If you add a cable tier, the following things are also done for you:
- Transformers are registered.
- Storage Units are registered.
- A corresponding [casing type](ADDING_MACHINES.md#adding-new-casing-types) is registered.
You will need to provide the corresponding casing textures.

Keep in mind that the transformers from unmodified MI will
always be registered even if you add tiers in between the default MI ones.
For example, if you register a tier between `ev` and `superconductor`,
it is your job to remove the recipes for the EV <-> Superconductor transformers.

Use the `MIRegistrationEvents.registerCableTiers` startup event:
```js
MIRegistrationEvents.registerCableTiers(event => {
    event.register(
        // Internal name for the tier
        "iv",
        // Short English name for the tier
        "IV",
        // Long English name for the tier
        "Insane Voltage",
        // Cable EU/t transfer for this tier
        65536,
        // Resource location of the machine hull for this tier,
        // to change the tier of machines.
        "mypack:iv_machine_hull",
    );
});
```

To register a corresponding cable with the [material system](ADDING_MATERIALS.md),
use `.cable("iv")` on a material builder.

To register corresponding energy input and output hatches,
see the page on [adding hatches](ADDING_HATCHES.md).
