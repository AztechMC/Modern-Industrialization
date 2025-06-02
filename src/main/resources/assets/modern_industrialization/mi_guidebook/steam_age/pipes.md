---
navigation:
  title: "Pipes"
  icon: "modern_industrialization:wrench"
  position: 4
  parent: modern_industrialization:steam_age.md
---

# Pipes

Pipes are used in the recipes of some machines, but they can also be placed in the world. Up to 3 pipes can fit in a single block.

Pipes of different colors will not connect.

Right-clicking a pipe with a wrench will change the connection type, shift-right-clicking will drop the pipe.

## Fluid Pipes

<ItemImage id="modern_industrialization:fluid_pipe" />

Fluid pipes can only contain one type of fluid, and by default they won't connect to empty pipes or blocks. You can force a connection by right-clicking the center of the pipe with the wrench, or by right-clicking a block with a pipe in your hand: it will connect, but not consume the item in your hand.

## Item Pipes

<ItemImage id="modern_industrialization:item_pipe" />

Item pipes will not connect to inventories by default, but again you can force a connection.

Right-click a connection without holding a wrench and will see a filter. By default, whitelist mode is enabled, so no items will be inserted or extracted directly.

The different types of pipes don't work exactly the same way.

Every tick, fluid pipes will try to evenly extract for all connected blocks, and then try to insert that evenly. [Read more about their speed here.](../midgame/fluid_transfer.md)

Item pipes will insert items into higher priorities first, and they will transfer 16 items every few seconds. **They can be upgraded with motors.**

## Note

The item pipes are on whitelist by default (which mean only the items in the filter will be moved). You need to switch to blacklist (only the items NOT in the filter will be moved) or add the item you want to move to the filter. Be careful that you need to configure both sides of the pipe, otherwise items will not move.

