---
navigation:
  title: "Alto-forno Elétrico"
  icon: "modern_industrialization:electric_blast_furnace"
  position: 106
  parent: modern_industrialization:electric_age.md
item_ids:
  - modern_industrialization:electric_blast_furnace
  - modern_industrialization:cupronickel_coil
  - modern_industrialization:lv_energy_input_hatch
  - modern_industrialization:mv_energy_input_hatch
---

# Alto-forno Elétrico

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="electric_blast_furnace" />
</GameScene>

O Alto-forno Elétrico é a versão elétrica do Alto-forno a Vapor. Ele desbloqueia novas receitas e, como outras máquinas multibloco elétricas, seu overclock é limitado a 128 EU/t por padrão.

<Recipe id="modern_industrialization:electric_age/machine/electric_blast_furnace_asbl" />

O EBF é composto por uma camada de Estruturas de Máquina à Prova de Calor, duas camadas vazadas de Bobinas de Cuproníquel e mais uma camada de Estruturas de Máquina à Prova de Calor. O controlador deve ficar na camada inferior, e as aberturas nas camadas superior ou inferior.

<Recipe id="modern_industrialization:materials/cupronickel/craft/coil" />

Não esqueça de adicionar Escotilha(s) de Entrada de Energia ou o EBF não terá energia! A Escotilha de Entrada de Energia LV conecta-se apenas a cabos LV.

<Recipe id="modern_industrialization:hatches/basic/energy_input_hatch" />

Escotilha de Entrada de Energia MV conectam-se apenas a cabos MV, o que pode ser útil se você precisar de muita energia para o seu EBF! Você ainda não pode fabricá-los, mas fique sabendo que eles existem...

<Recipe id="modern_industrialization:hatches/advanced/energy_input_hatch" />

