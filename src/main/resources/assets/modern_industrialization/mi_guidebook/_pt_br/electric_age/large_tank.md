---
navigation:
  title: "Tanque Grande"
  icon: "modern_industrialization:large_tank"
  position: 101
  parent: modern_industrialization:electric_age.md
item_ids:
  - modern_industrialization:large_tank
  - modern_industrialization:large_tank_hatch
---

# Tanque Grande

<GameScene zoom="1" interactive={true} fullWidth={true}>
    <MultiblockShape controller="large_tank" />
    <MultiblockShape controller="large_tank" useBigShape={true} x="-8" z="-2" />
</GameScene>

O Tanque Grande é um multibloco que permite armazenar grandes quantidades de um fluido, por exemplo vapor. Ele pode armazenar 64 baldes de fluido para cada bloco na estrutura (incluindo as laterais).

<Recipe id="modern_industrialization:electric_age/machine/large_tank_asbl" />

O Tanque Grande tem vários tamanhos possíveis, dependendo de quanto armazenamento você precisa. Você pode abrir o painel de configuração de tamanho clicando em um botão no controlador.

Apenas tubos podem acessar o tanque, seja pelo controlador ou por uma Escotilha Grande de Tanque (veja a próxima página).

Não quebre o controlador ou você perderá todo o fluido armazenado!

A Escotilha Grande de Tanque funciona como uma extensão do bloco do Tanque Grande. Você pode clicar com o botão direito nela para abrir o menu do Tanque Grande, e os tubos conectados a ela acessarão diretamente o armazenamento do Tanque Grande.

<Recipe id="modern_industrialization:electric_age/machine/large_tank_hatch_asbl" />

