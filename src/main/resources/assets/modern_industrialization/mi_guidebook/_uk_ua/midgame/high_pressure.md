---
navigation:
  title: "Високий тиск!"
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

# Високий тиск!

## Ущільнювач

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="pressurizer" />
</GameScene>

Ущільнювач — це новий багатоблок із титану, який може перетворювати воду на високотисну воду, пару — на високотисну пару та навпаки.

<Recipe id="modern_industrialization:electric_age/machine/pressurizer_asbl" />

## Високотискний великий паровий котел

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="high_pressure_large_steam_boiler" />
</GameScene>
      
Отримавши доступ до високотискної води, ви можете використовувати вискотискний великий паровий котел для виробництва високотискного пару.

Одне мВ високотискного пару коштує 8 мВ звичайного пару, тобто 8 EU.

<Recipe id="modern_industrialization:electric_age/machine/high_pressure_large_steam_boiler_asbl" />

## Покращений високотискний паровий котел

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="high_pressure_advanced_large_steam_boiler" />
</GameScene>

Пізніше ви також зможете створити покращену версію високотискного великого парового котла.

## Велика парова турбіна

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="large_steam_turbine" />
</GameScene>

Велика парова турбіна прийматиме в EU як звичайну пару (1 мВ = 1 EU), так високотискний пар (1 мВ = 8 EU) і генеруватиме до 16384 EU/т! **Однак це не поверне вам звичайну воду чи високотисну воду**.

<Recipe id="modern_industrialization:electric_age/machine/large_steam_turbine_asbl" />

## Heat Exchanger

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="heat_exchanger" />
</GameScene>

Пам'ятайте, що стискання води у високотискну воду вимагає багато енергії, але турбіна її не дасть! Ви можете використовувати теплообмінник для відновлення високотискної води для іншого циклу.

<Recipe id="modern_industrialization:electric_age/machine/heat_exchanger_asbl" />

## Парова турбіна ВН

Іншим варіантом для невеликих установок є використання парової турбіни високої напруги. Як і інші генератори високої напруги, він вироблятиме 512 EU/т. Він приймає лише звичайний пар.

<Recipe id="modern_industrialization:electric_age/machine/hv_steam_turbine_asbl" />

