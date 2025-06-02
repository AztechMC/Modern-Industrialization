---
navigation:
  title: "Oil Processing"
  icon: "modern_industrialization:diesel_bucket"
  position: 109
  parent: modern_industrialization:electric_age.md
item_ids:
  - modern_industrialization:oil_drilling_rig
  - modern_industrialization:mv_diesel_generator
---

# Oil Processing

<GameScene zoom="1.5" interactive={true} fullWidth={true}>
    <MultiblockShape controller="oil_drilling_rig" />
</GameScene>

The Oil Drilling Rig is a huge multiblock that can dig for Crude Oil under the bedrock using drills. Yes, it's an oil quarry basically. Oil processing will give you a ton of byproducts and energy!

<Recipe id="modern_industrialization:oil/oil_drilling_rig_asbl" />

The structure of the multiblock is made of: Steel Machine Casings, Steel Pipe Machine Casings and Chains as you can see by placing the controller and holding a Wrench. The Hatches can be replaced by Steel Machine Casings, but be sure to have at least Item Input, Fluid Output and Energy Input Hatches!

Crude Oil can be turned into various fuels and used for more efficient Rubber Sheet production.

Various fuels can be burned in the Diesel Generator, and you can check in REI how many EU each fuel produces. The Diesel Generator will produce up to 256 EU/t and will only connect to MV cables.

<Recipe id="modern_industrialization:electric_age/machine/mv_diesel_generator_asbl" />

Diesel Generators will only use fuel when they need to, but if you need a constant supply of energy, you can put liquid fuels in the Large Steam Boiler instead of items.

A fully heated Large Steam Boiler will roughly produce twice the amount of energy a Diesel Generator would produce using the same quantity of fuel.

