---
navigation:
  title: "电力高炉"
  icon: "modern_industrialization:electric_blast_furnace"
  position: 106
  parent: modern_industrialization:electric_age.md
item_ids:
  - modern_industrialization:electric_blast_furnace
  - modern_industrialization:cupronickel_coil
  - modern_industrialization:lv_energy_input_hatch
  - modern_industrialization:mv_energy_input_hatch
---

# 电力高炉

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="electric_blast_furnace" />
</GameScene>

电力高炉是蒸汽高炉的电动版本。除了解锁新配方外，它与其他多方块电力机器一样，默认情况下其超频上限为128 EU/t。

<Recipe id="modern_industrialization:electric_age/machine/electric_blast_furnace_asbl" />

电力高炉由一层耐热机器外壳、两层中空的白铜线圈和另一层耐热机器外壳制成。控制器必须放在底层，且仓室需放在顶层或底层。

<Recipe id="modern_industrialization:materials/cupronickel/craft/coil" />

不要忘记添加能量输入仓，否则电力高炉将没有能量！低压能量输入仓仅能与低压电缆连接。

<Recipe id="modern_industrialization:hatches/basic/energy_input_hatch" />

中压能量输入仓能与中压电缆连接，如果你的电力高炉需要大量能量，这会非常有用！你还不能制作它们，但请记住它们的存在……

<Recipe id="modern_industrialization:hatches/advanced/energy_input_hatch" />

