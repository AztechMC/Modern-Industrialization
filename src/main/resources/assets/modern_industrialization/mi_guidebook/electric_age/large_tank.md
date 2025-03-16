---
navigation:
  title: "Large Tank"
  icon: "modern_industrialization:large_tank"
  position: 101
  parent: modern_industrialization:electric_age.md
item_ids:
  - modern_industrialization:large_tank
  - modern_industrialization:large_tank_hatch
---

# Large Tank

<GameScene zoom="1" interactive={true} fullWidth={true}>
    <MultiblockShape controller="large_tank" />
    <MultiblockShape controller="large_tank" useBigShape={true} x="-8" z="-2" />
</GameScene>

The Large Tank is a multiblock that allows you to store large amounts of some fluid, for example steam. It can store 64 buckets worth of fluid for every block in the structure (including sides).

<Recipe id="modern_industrialization:electric_age/machine/large_tank_asbl" />

The Large Tank has many possible sizes depending on how much storage you need. You can open the size configuration panel by clicking on a button in the controller.

Only pipes may access the tank, either through the controller or through a Large Tank Hatch (see next page).

Don't break the controller or you will lose all of the stored fluid!

The Large Tank Hatch acts as an extension of the Large Tank block. You can right-click it to open the Large Tank's menu, and pipes connected to it will directly access the Large Tank's storage.

<Recipe id="modern_industrialization:electric_age/machine/large_tank_hatch_asbl" />

