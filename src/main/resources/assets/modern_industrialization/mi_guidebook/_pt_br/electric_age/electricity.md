---
navigation:
  title: "Eletricidade"
  icon: "modern_industrialization:lv_steam_turbine"
  position: 104
  parent: modern_industrialization:electric_age.md
item_ids:
  - modern_industrialization:lv_steam_turbine
  - modern_industrialization:lv_diesel_generator
  - modern_industrialization:lv_mv_transformer
  - modern_industrialization:mv_lv_transformer
---

# Eletricidade

A Turbina de Vapor usa vapor para produzir Eletricidade. Ela converte cada mb de vapor em 1 EU, até 32 mb convertidos por tick.

<Recipe id="modern_industrialization:electric_age/machine/lv_steam_turbine_asbl" />

A Turbina de Vapor enviará automaticamente Eletricidade para qualquer máquina conectada diretamente ao seu lado de saída. Ela só se conecta a cabos colocados no seu lado de saída. Ela tem o Tier Baixa Voltagem (LV).

Cada cabo elétrico tem um Tier que determina quantos EU/t ele pode transferir e a quais máquinas pode se conectar. Cobre, Prata e Estanho são LV, Cuproníquel e Electrum são MV, e assim por diante...

O Gerador a Diesel é uma alternativa à Turbina de Vapor. Ele usa vários combustíveis para produzir Eletricidade. Por enquanto, você pode queimar Creosoto. (Consulte o REI para a lista de combustíveis que podem ser queimados).

<Recipe id="modern_industrialization:electric_age/machine/lv_diesel_generator_asbl" />

Redes de cabos têm limitações na quantidade de energia que podem puxar para a rede: no máximo 256 EU/t para cabos LV, 1024 EU/t para cabos MV e 8192 EU/t para cabos HV. Como não há limite de saída para a rede e os cabos têm um pequeno armazenamento interno, uma rede LV pode fornecer mais de 256 EU/t por um curto período.

Note, no entanto, que máquinas elétricas de bloco único só se conectarão a cabos LV!

Para transferir mais energia, você precisa criar múltiplas redes de cabos ou usar Transformadores.

Um Transformador de tier baixo para alto (por exemplo, LV para MV) tem 5 entradas e 1 saída. Um Transformador de tier alto para baixo (por exemplo, MV para LV) tem 1 entrada e 5 saídas.



<Recipe id="modern_industrialization:electric_age/transformer/lv_mv/up_asbl" />

<Recipe id="modern_industrialization:electric_age/transformer/lv_mv/down_asbl" />

