---
navigation:
  title: "不锈钢"
  icon: "modern_industrialization:stainless_steel_dust"
  position: 201
  parent: modern_industrialization:midgame.md
item_ids:
  - modern_industrialization:vacuum_freezer
  - modern_industrialization:distillation_tower
---

# 不锈钢

## 不锈钢

<ItemImage id="modern_industrialization:stainless_steel_ingot" />

下一种你需要大量使用的材料就是不锈钢。继续阅读以了解原因！

在电力高炉中熔炼不锈钢粉会获得热不锈钢锭，你可以使用真空冷却机将其冷却回普通锭。

## 真空冷却机

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="vacuum_freezer" />
</GameScene>

你可以用REI查看基础材料，用扳手检查正确的形状！

<Recipe id="modern_industrialization:electric_age/machine/vacuum_freezer_asbl" />

## 蒸馏塔

一旦开始制作数字电路，务必建造一座蒸馏塔。蒸馏室只能提供单一的石油加工产物，但蒸馏塔保证能每层产出一种产物！

<Recipe id="modern_industrialization:electric_age/machine/distillation_tower_asbl" />

这里以最小尺寸与最大尺寸的并排对比为例。
一座2层高的蒸馏塔只能提供配方中的第一项产出，3层高的则能提供前两项产出，依此类推……


<GameScene zoom="1" interactive={true} fullWidth={true}>
    <MultiblockShape controller="distillation_tower" />
    <MultiblockShape controller="distillation_tower" x="-6" useBigShape={true} />
</GameScene>

