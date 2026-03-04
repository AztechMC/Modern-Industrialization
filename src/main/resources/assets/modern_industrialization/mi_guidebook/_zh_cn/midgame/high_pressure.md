---
navigation:
  title: "高压！"
  icon: "modern_industrialization:pressurizer"
  position: 202
  parent: modern_industrialization:midgame.md
item_ids:
  - modern_industrialization:pressurizer
  - modern_industrialization:high_pressure_large_steam_boiler
  - modern_industrialization:high_pressure_advanced_large_steam_boiler
  - modern_industrialization:large_steam_turbine
  - modern_industrialization:heat_exchanger
  - modern_industrialization:hv_steam_turbine
---

# 高压！

## 增压器

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="pressurizer" />
</GameScene>

增压器是一种由钛制成的新型多方块结构，可以将水变成高压水，将蒸汽变成高压蒸汽，以此类推。

<Recipe id="modern_industrialization:electric_age/machine/pressurizer_asbl" />

## 高压大型蒸汽锅炉

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="high_pressure_large_steam_boiler" />
</GameScene>

一旦能够制造高压水，你就可以使用高压大型蒸汽锅炉来生产高压蒸汽。

1 mB高压蒸汽相当于8 mB普通蒸汽，等价于8 EU。

<Recipe id="modern_industrialization:electric_age/machine/high_pressure_large_steam_boiler_asbl" />

## 高压进阶大型蒸汽锅炉

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="high_pressure_advanced_large_steam_boiler" />
</GameScene>

之后，你还可以建造进阶版的高压大型蒸汽锅炉。

## 大型蒸汽涡轮机

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="large_steam_turbine" />
</GameScene>

大型蒸汽涡轮机可将常规蒸汽（1 mB = 1 EU）和高压蒸汽（1 mB = 8 EU）转化为EU，且产出最高可达16384 EU/t！**然而它既不会返还普通水，也不会返还高压水。**

<Recipe id="modern_industrialization:electric_age/machine/large_steam_turbine_asbl" />

## 热交换机

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="heat_exchanger" />
</GameScene>

请记住，将水加压成高压水会消耗大量能量，但蒸汽涡轮机不会将其返还！你可以使用热交换机回收高压水来用于另一个循环。

<Recipe id="modern_industrialization:electric_age/machine/heat_exchanger_asbl" />

## 高压蒸汽涡轮机

小型设施的另一种选择是使用高压蒸汽涡轮机。与其他高压发电机一样，它能产出512 EU/t。它只接受普通蒸汽。

<Recipe id="modern_industrialization:electric_age/machine/hv_steam_turbine_asbl" />

