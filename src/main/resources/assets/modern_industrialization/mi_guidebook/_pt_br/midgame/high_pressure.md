---
navigation:
  title: "Alta Pressão!"
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

# Alta Pressão!

## Pressurizador

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="pressurizer" />
</GameScene>

O Pressurizador é um novo multibloco feito de titânio que pode transformar água em água de Alta Pressão (HP), vapor em vapor de Alta Pressão e vice-versa.

<Recipe id="modern_industrialization:electric_age/machine/pressurizer_asbl" />

## Caldeira de Vapor Grande HP

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="high_pressure_large_steam_boiler" />
</GameScene>

Quando você tiver acesso à água HP, poderá usar uma Caldeira de Vapor Grande de Alta Pressão para produzir vapor HP.

Um milibalde de vapor HP vale 8 mb de vapor comum, ou seja, 8 EU.

<Recipe id="modern_industrialization:electric_age/machine/high_pressure_large_steam_boiler_asbl" />

## Caldeira de Vapor Grande HP Avançada

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="high_pressure_advanced_large_steam_boiler" />
</GameScene>

Mais adiante, você também poderá construir a versão avançada da Caldeira de Vapor Grande HP.

## Turbina de Vapor Grande

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="large_steam_turbine" />
</GameScene>

Uma Turbina de Vapor Grande aceitará tanto vapor comum (1 mb = 1 EU) quanto vapor HP (1 mb = 8 EU) e os converterá em EU, podendo gerar até 16384 EU/t! **No entanto, ela não devolverá nem água comum nem água HP.**

<Recipe id="modern_industrialization:electric_age/machine/large_steam_turbine_asbl" />

## Trocador de Calor

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="heat_exchanger" />
</GameScene>

Lembre-se de que pressurizar água em água HP consome muita energia, e a turbina não a devolverá! Você pode usar um Trocador de Calor para recuperar a água HP para outro ciclo.

<Recipe id="modern_industrialization:electric_age/machine/heat_exchanger_asbl" />

## Turbina a Vapor HV

Outra opção para instalações menores é usar uma turbina a vapor HV. Como outros geradores HV, ela produzirá 512 EU/t. Esta aceitará apenas vapor comum.

<Recipe id="modern_industrialization:electric_age/machine/hv_steam_turbine_asbl" />

