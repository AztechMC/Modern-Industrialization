---
navigation:
  title: "大型储罐"
  icon: "modern_industrialization:large_tank"
  position: 101
  parent: modern_industrialization:electric_age.md
item_ids:
  - modern_industrialization:large_tank
  - modern_industrialization:large_tank_hatch
---

# 大型储罐

<GameScene zoom="1" interactive={true} fullWidth={true}>
    <MultiblockShape controller="large_tank" />
    <MultiblockShape controller="large_tank" useBigShape={true} x="-8" z="-2" />
</GameScene>

大型储罐是一个多方块结构，可以储存大量流体，例如蒸汽。结构中的每个方块（包括侧面）都可以存储64桶流体。

<Recipe id="modern_industrialization:electric_age/machine/large_tank_asbl" />

大型储罐可根据存储需求配置多种尺寸。点击控制器上的按钮即可打开尺寸配置面板。

储罐仅能通过管道进行访问，管道可与控制器或大型储罐仓连接（详见下页）。

不要破坏控制器，否则你将丢失所有储存的流体！

大型储罐仓是大型储罐方块的延伸。右击仓室可打开大型储罐的界面，与之连接的管道将直接访问大型储罐的存储空间。

<Recipe id="modern_industrialization:electric_age/machine/large_tank_hatch_asbl" />

