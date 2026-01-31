---
navigation:
  title: "石油加工"
  icon: "modern_industrialization:diesel_bucket"
  position: 109
  parent: modern_industrialization:electric_age.md
item_ids:
  - modern_industrialization:oil_drilling_rig
  - modern_industrialization:mv_diesel_generator
---

# 石油加工

<GameScene zoom="1.5" interactive={true} fullWidth={true}>
    <MultiblockShape controller="oil_drilling_rig" />
</GameScene>

石油钻机是一个巨大的多方块结构，它能利用钻头穿透基岩开采原油。是的，它本质上就是一个采油场。石油加工将为你提供大量副产品和能源！

<Recipe id="modern_industrialization:oil/oil_drilling_rig_asbl" />

该多方块结构由以下部件构成：钢机器外壳、钢管道机器外壳和锁链，放置控制器后手持扳手即可查看。仓室可用钢机器外壳替代，但请确保至少保留物品输入仓、流体输出仓和能量输入仓！

原油可以转化为多种燃料，并且可用于更高效的橡胶片生产。

柴油发电机可以燃烧多种燃料，你可以在REI中查看每种燃料产生的EU量。柴油发电机最高可产出256 EU/t，且只能与中压电缆连接。

<Recipe id="modern_industrialization:electric_age/machine/diesel_generator_asbl" />

柴油发电机仅在需要时消耗燃料，但如果你需要稳定的能量供应，也可以将液态燃料注入大型蒸汽锅炉以替代物品。

完全加热的大型蒸汽锅炉产生的能量，大约是使用等量燃料的柴油发电机产出的两倍。

