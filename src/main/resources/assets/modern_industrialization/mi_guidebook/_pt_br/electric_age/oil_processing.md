---
navigation:
  title: "Processamento de Petróleo"
  icon: "modern_industrialization:diesel_bucket"
  position: 109
  parent: modern_industrialization:electric_age.md
item_ids:
  - modern_industrialization:oil_drilling_rig
  - modern_industrialization:mv_diesel_generator
---

# Processamento de Petróleo

<GameScene zoom="1.5" interactive={true} fullWidth={true}>
    <MultiblockShape controller="oil_drilling_rig" />
</GameScene>

A Plataforma de Perfuração de Petróleo é um enorme multibloco que pode perfurar para encontrar Petróleo Bruto sob a base de pedra usando brocas. Sim, basicamente é uma mina de petróleo. O processamento do petróleo te dará muitos subprodutos e energia!

<Recipe id="modern_industrialization:oil/oil_drilling_rig_asbl" />

A estrutura do multibloco é feita de: Estruturas de Máquina de Aço, Estruturas de Máquina com Tubulação de Aço e Correntes, como você pode ver ao colocar o controlador e segurar uma Chave Inglesa. As Escotilhas podem ser substituídas por Estruturas de Máquina de Aço, mas certifique-se de ter pelo menos Escotilhas de Entrada de Itens, Saída de Fluidos e Entrada de Energia!

O Petróleo Bruto pode ser transformado em vários combustíveis e usado para uma produção mais eficiente de Folhas de Borracha.

Diversos combustíveis podem ser queimados no Gerador a Diesel, e você pode verificar no JEI quantos EU cada combustível produz. O Gerador a Diesel produzirá até 256 EU/t e só se conectará a cabos MV.

<Recipe id="modern_industrialization:electric_age/machine/mv_diesel_generator_asbl" />

Os Geradores a Diesel só usarão combustível quando precisarem, mas se você precisar de um suprimento constante de energia, pode colocar combustíveis líquidos na Caldeira de Vapor Grande em vez de itens.

Uma Caldeira de Vapor Grande totalmente aquecida produzirá aproximadamente o dobro da energia que um Gerador a Diesel produziria usando a mesma quantidade de combustível.

