---
navigation:
  title: "Stainless Steel"
  icon: "modern_industrialization:stainless_steel_dust"
  position: 201
  parent: modern_industrialization:midgame.md
item_ids:
  - modern_industrialization:vacuum_freezer
  - modern_industrialization:distillation_tower
---

# Stainless Steel

## Stainless Steel

<ItemImage id="modern_industrialization:stainless_steel_ingot" />

Stainless Steel is the next material you will want a large quantity of. Read on to know why!

Smelting stainless steel dust in an EBF will give you hot ingots, which you can cool back into regular ingots with a Vacuum Freezer.

## Vacuum Freezer

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="vacuum_freezer" />
</GameScene>

You can check the base materials with REI, and the exact shape with a wrench!

<Recipe id="modern_industrialization:electric_age/machine/vacuum_freezer_asbl" />

## Distillation Tower

Once you get some digital circuits going, make sure to build a Distillation Tower. The Distillery only gives you one oil processing output, but the tower gives you ONE PER LAYER!

<Recipe id="modern_industrialization:electric_age/machine/distillation_tower_asbl" />

Here is an example of the smallest size and the largest size side by side.
A tower of size 2 will only give you the first output from the recipe, a tower of size 3 will give the first two outputs, and so on...


<GameScene zoom="1" interactive={true} fullWidth={true}>
    <MultiblockShape controller="distillation_tower" />
    <MultiblockShape controller="distillation_tower" x="-6" useBigShape={true} />
</GameScene>

