---
navigation:
  title: "Você gosta de Coque?"
  icon: "modern_industrialization:coke_oven"
  position: 9
  parent: modern_industrialization:steam_age.md
item_ids:
  - modern_industrialization:coke_oven
  - modern_industrialization:bronze_item_input_hatch
  - modern_industrialization:bronze_item_output_hatch
  - modern_industrialization:bronze_fluid_input_hatch
  - modern_industrialization:bronze_fluid_output_hatch
---

# Você gosta de Coque?

<GameScene zoom="3" interactive={true} fullWidth={true}>
    <MultiblockShape controller="coke_oven" />
</GameScene>

Depois de se cansar das máquinas de bronze, você pode começar a trabalhar para fabricar aço. O objetivo final é construir a pedreira, um multibloco que cava minérios para você!

O primeiro passo é fazer coque, aquecendo carvão sem oxigênio. Para isso, você precisará construir um multibloco Forno de Coque.

Para esse primeiro multibloco, você precisará do próprio Forno de Coque, 21 Tijolos e 3 Escotilhas: uma de entrada de itens, uma de saída de itens e uma de entrada de fluidos.

Opcionalmente, adicione uma escotilha de saída de fluido para coletar creosoto também.

<Recipe id="modern_industrialization:steam_age/fireclay/coke_oven" />



<Recipe id="modern_industrialization:hatches/bronze/item_input_hatch" />

<Recipe id="modern_industrialization:hatches/bronze/item_output_hatch" />



<Recipe id="modern_industrialization:hatches/bronze/fluid_input_hatch" />

<Recipe id="modern_industrialization:hatches/bronze/fluid_output_hatch" />

O bloco do Forno de Coque funciona como um *Controlador*. Todo multibloco é gerenciado por um controlador, mas normalmente você não interage diretamente com ele: toda entrada e saída ocorre pelas escotilhas. Precisamos de uma escotilha de entrada de fluido porque o forno é alimentado por vapor, uma de entrada de itens para o carvão e uma de saída para o coque.

Podemos opcionalmente adicionar uma escotilha de saída de fluido para o creosoto. Essa saída é por chance e será descartada se não houver espaço para ela.

Se esquecermos uma das escotilhas, o forno de coque não vai iniciar!

**Segure uma chave inglesa para ver os blocos faltando e os erros!** Você também pode segurar uma escotilha para saber onde ela pode ser colocada.

Precisamos de 21 Tijolos para esse multibloco! Confira no REI, que mostra 24 no total, mas como temos 3 escotilhas, só precisamos de tijolos para os 21 blocos restantes!

## Forno de Coque!

Existem várias maneiras de posicionar as escotilhas, essa é uma delas!

![](coke_oven.png)

Quando o Forno de Coque indicar *Forma Válida*, encha a escotilha de entrada de fluido com vapor, coloque carvão na escotilha de entrada de itens e pronto para começar!

O coque será muito útil para o aço, mas também é um combustível poderoso. Dura 4 vezes mais que o carvão!

