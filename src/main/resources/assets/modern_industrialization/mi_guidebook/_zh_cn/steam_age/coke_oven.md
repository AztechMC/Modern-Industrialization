---
navigation:
  title: "你喜欢焦炭（Coke）吗？"
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

# 你喜欢焦炭（Coke）吗？

<GameScene zoom="3" interactive={true} fullWidth={true}>
    <MultiblockShape controller="coke_oven" />
</GameScene>

一旦你拥有足够的青铜机器，你就可以开始着手于制作钢铁了。最终的目标是建造采石机，一个可以为你挖掘矿石的多方块结构！

第一步是在无氧环境下加热煤炭来制造焦炭。为此，你需要建造一个焦炉多方块结构。

对于第一个多方块结构，你当然需要一个焦炉方块本身，外加21块砖块和3个*仓室*：物品输入、物品输出和流体输入仓。

此外，你还可以选择添加一个流体输出仓来收集杂酚油。

<Recipe id="modern_industrialization:steam_age/fireclay/coke_oven" />



<Recipe id="modern_industrialization:hatches/bronze/item_input_hatch" />

<Recipe id="modern_industrialization:hatches/bronze/item_output_hatch" />



<Recipe id="modern_industrialization:hatches/bronze/fluid_input_hatch" />

<Recipe id="modern_industrialization:hatches/bronze/fluid_output_hatch" />

这里的焦炉方块具有*控制器*的作用。每个多方块结构都由一个控制器管理，但你通常无法直接与控制器交互：所有输入和输出都通过仓室进行。我们需要流体输入，因为焦炉由蒸汽驱动，需要物品输入仓来放入煤，以及物品输出仓来取出焦炭。

我们可以选择性地添加一个流体输出仓来收集杂酚油。这是一个概率性产物，因此如果足够的存放空间，它将被销毁。

如果我们忘记了其中一个仓室，焦炉将无法启动！

**手持扳手可以查看丢失和错误的方块！** 你还可以手持仓室以了解它可以放置在哪里。

这个多方块结构需要21块砖块！REI中显示说总共要24个，但我们有3个仓室，所以只需要为剩下的空间提供21块砖块。

## 焦炉！

仓室的放置方法有很多种，下图是其中一种示例！

![](coke_oven.png)

一旦焦炉显示*结构有效*，向流体输入仓输入蒸汽，将煤炭放入物品输入仓，就可以开始运行了！

焦炭对炼钢非常有用，但它也是一种强力燃料。它的燃烧时间是煤炭的4倍！

