# Adding fluids
MI offers a very easy way to add fluids with KubeJS.
However, they cannot be placed in the world at the moment (like all MI fluids).

You can use the `MIRegistrationEvents.registerFluids` event to add fluids.
The event has the following method: `event.register(englishName, internalName, color, texture, isGas, opacity)`:
- `englishName`: English name of the fluid. This is the name that players will see in tooltips.
- `internalName`: Internal name of the fluid. The registration id of the fluid is always `modern_industrialization:` followed by the internal name.
- `color`: Color of the fluid in hexadecimal RGB format. For example `0x00ff00` for green.
- `texture`: Which base texture MI will use to generate the texture for the fluid. The following options are supported: `"lava"`, `"plasma"`, `"steam"`, `"water"`.
- `isGas`: `true` if the fluid is a gas (i.e. it flows upside down) or `false` otherwise.
- `opacity`: How opaque (inverse of transparent) the texture should be.
  The following options are supported, from the most transparent to least transparent: `"low"`, `"medium"`, `"high"`, `"full"`.

Example:
```js
MIRegistrationEvents.registerFluids(event => {
    event.register(
        "Alien Oil", "alien_oil", // English name and internal name
        0x09a837, // Green-ish color, see https://www.color-hex.com/color/09a837
        "steam", // Steam-like texture
        true, // true for upside down
        "medium", // medium transparency
    );
});
```

# Adding fluid fuels
You can add fluid fuels using data maps.
This will allow them to be used in diesel generators, diesel jetpacks, etc...
See `data/modern_industrialization/data_maps/fluid/fluid_fuels.json` in the MI jar.
