---
navigation:
  title: "电力"
  icon: "modern_industrialization:lv_steam_turbine"
  position: 104
  parent: modern_industrialization:electric_age.md
item_ids:
  - modern_industrialization:lv_steam_turbine
  - modern_industrialization:lv_diesel_generator
  - modern_industrialization:lv_mv_transformer
  - modern_industrialization:mv_lv_transformer
---

# 电力

蒸汽涡轮机使用蒸汽来发电。它将每mB蒸汽转换为1 EU，每刻最多转换32 mB。

<Recipe id="modern_industrialization:electric_age/machine/lv_steam_turbine_asbl" />

蒸汽涡轮机会自动向与其输出端直接相连的任意机器输送电力，且仅会连接放置在其输出端上的电缆。它属于低压（LV）*等级*。

每钟电缆均有其等级，这决定了它能传输的EU/t上限，以及可连接的机器类型。铜、银和锡电缆为低压等级，白铜和琥珀金电缆为中压（MV）等级，依此类推……

柴油发电机是蒸汽涡轮机的替代选择。它使用多种燃料发电。目前你可使用杂酚油。（可用燃料列表请查阅REI）。

<Recipe id="modern_industrialization:electric_age/machine/lv_diesel_generator_asbl" />

电缆网络存在能量输入上限：低压电缆网络最多接收256 EU/t，中压电缆为1024 EU/t，高压（HV）电缆为8192 EU/t。由于网络输出无限制，且电缆具有少量内部储能，低压网络可在短时间内提供超过256 EU/t的能量。

但请注意，单个电力机器只能连接到低压电缆！

要传输更多能量，你需要创建多个电缆网络，或使用变压器。

低等级向高等级转换的变压器（例如低压至中压）具有5个输入面和1个输出面。高等级向低等级转换的变压器（例如中压至高压）则具有1个输入面和5个输出面。



<Recipe id="modern_industrialization:electric_age/transformer/lv_mv/up_asbl" />

<Recipe id="modern_industrialization:electric_age/transformer/lv_mv/down_asbl" />

