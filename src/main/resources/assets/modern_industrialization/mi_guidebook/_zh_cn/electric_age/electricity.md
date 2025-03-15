---
navigation:
  title: "电"
  icon: "modern_industrialization:lv_steam_turbine"
  position: 103
  parent: modern_industrialization:electric_age.md
item_ids:
  - modern_industrialization:lv_mv_transformer
  - modern_industrialization:mv_lv_transformer
---

# 电

蒸汽涡轮机使用蒸汽来发电。它将每mb蒸汽转换为1EU电能，每游戏刻最多转换32mB。

<Recipe id="modern_industrialization:electric_age/machine/steam_turbine_asbl" />

蒸汽涡轮机将自动向直接连接到其输出端的任何机器供电。它只会连接到放置在其输出侧的电缆。它属于低压（LV）*层*。

每条电缆都有一个层级，决定了它每刻可以传输多少EU以及它可以连接到哪些机器。铜，银和锡电缆是低压，白铜和琥珀金电缆是中压（MV），等等。

电缆网络对进入网络的能量有限制：低压电缆最多256EU/t，中压电缆最多1024EU/t，高压（HV）电缆最多8192EU/t。由于网络没有输出限制，而且电缆拥有较小的的内部存储空间，所以低压网络可以在短时间内提供超过256EU/t的电力。

但请注意，单个电力机器只能连接到低压电缆！

要传输更多能量，你需要创建多个管道网络或使用变压器。正如你所料，X到Y的变压器可以连接到其输出侧的Y电缆和其他5侧的X电缆。



<Recipe id="modern_industrialization:electric_age/transformer/lv_mv/up_asbl" />

<Recipe id="modern_industrialization:electric_age/transformer/lv_mv/down_asbl" />

