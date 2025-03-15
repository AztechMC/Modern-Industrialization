---
navigation:
  title: "High Pressure!"
  icon: "modern_industrialization:pressurizer"
  position: 202
  parent: modern_industrialization:midgame.md
item_ids:
  - modern_industrialization:pressurizer
  - modern_industrialization:high_pressure_large_steam_boiler
  - modern_industrialization:large_steam_turbine
  - modern_industrialization:heat_exchanger
  - modern_industrialization:hv_steam_turbine
---

# High Pressure!

The Pressurizer is a new multiblock made of titanium that can turn water into HP (high pressure) water, steam into HP steam, and the other way around.

<Recipe id="modern_industrialization:electric_age/machine/pressurizer_asbl" />

Once you have access to HP water, you can use a High Pressure Large Steam Boiler to produce HP steam.

One millibucket of HP steam is worth 8 mb of regular steam, that is 8 EU.

<Recipe id="modern_industrialization:electric_age/machine/high_pressure_large_steam_boiler_asbl" />

A Large Steam Turbine will accept both regular steam (1 mb = 1 EU) and HP steam (1 mb = 8 EU) into EU, and will generate up to 16384 EU/t! **However it will not give you back regular nor HP water.**

<Recipe id="modern_industrialization:electric_age/machine/large_steam_turbine_asbl" />

Remember that pressurizing water into HP water costs a lot of energy, but the turbine will not give it back! You can use a Heat Exchanger to recover the HP water for another cycle.

<Recipe id="modern_industrialization:electric_age/machine/heat_exchanger_asbl" />

Another option for small setups is to use an HV steam turbine. Like other HV generators, it will produce 512 EU/t. This one will only accept regular steam.

<Recipe id="modern_industrialization:electric_age/machine/hv_steam_turbine_asbl" />

