---
navigation:
  title: "核反应堆"
  icon: "modern_industrialization:uranium_fuel_rod_quad"
  position: 206
  parent: modern_industrialization:midgame.md
item_ids:
  - modern_industrialization:nuclear_reactor
  - modern_industrialization:nuclear_casing
  - modern_industrialization:nuclear_item_hatch
  - modern_industrialization:nuclear_fluid_hatch
---

# 核反应堆

<GameScene zoom="1" interactive={true} fullWidth={true}>
    <MultiblockShape controller="nuclear_reactor" />
    <MultiblockShape controller="nuclear_reactor" useBigShape={true} x="-11" z="-3" />
</GameScene>

核反应堆是一种大型多块结构，其目的是通过消耗核燃料来产生巨量能量。它的EU/t产量可以是柴油发电机的数百倍。反应堆有多种尺寸，从小型到大型不等。

<Recipe id="modern_industrialization:electric_age/machine/nuclear_reactor_asbl" />

要使用核反应堆发电，你需要在核流体仓中放入某种形式的水。水将转化为蒸汽，随后可用于发电——可能需要先通过热交换器处理。

核反应堆也是生产某些材料（如钚）的唯一途径。

此外，它还能产出用于核聚变的流体：氘和氚。

尽管后续页面会尝试解释，但其内部工作原理可能有些复杂。不过，你无需理解所有细节也能设计出强大的反应堆。

我们建议你在创造模式下测试各种设计，直到找到适合你的方案。

请注意，核反应堆不会爆炸、不会释放辐射，也不会对地图造成其他损害。它唯一可能损坏的是你放入其中的物品——如果温度过高的话（稍后会详细介绍）。

你可以自由进行实验，享受作为核科学家的新生活！

核反应堆的主要部件是核合金外壳，由镉、铍和防爆合金混合制成。

<Recipe id="modern_industrialization:electric_age/casing/nuclear_casing_asbl" />

结构的上部可以放置核物品或流体仓室（或只放外壳）。它们是反应堆的输入输出接口。

<Recipe id="modern_industrialization:electric_age/casing/nuclear_item_hatch_asbl" />

每个仓室都有一个输入槽（物品或流体）和两个输出槽。输入槽将形成一个网格，显示在反应堆GUI中（可通过右键点击控制器访问）。

<Recipe id="modern_industrialization:electric_age/casing/nuclear_fluid_hatch_asbl" />

每个仓室都有具备温度并会积蓄热量，热量可以通过多种方式耗散。热量会自然转移到相邻的仓室，如果仓室位于边缘，则会向外界散失（热量就此损失）。此过程的速度等于仓室内容物的导热系数（可在REI中查看）乘以温差。热量也可以通过在流体仓中产生蒸汽的方式提取。超过物品的最大温度时，物品将被摧毁。

核反应堆的核心要素是中子。中子由核燃料产生。中子有两种类型：快中子和热中子；快中子携带能量，而热中子不携带。中子沿直线运动，直到遇到某个元素或离开反应堆（快中子的能量随之损失）。中子的运动可以在反应堆GUI中查看。

当中子遇到非空仓室时，可能发生两种情况：中子被吸收或散射。散射的中子会随机改变方向。如果是快中子散射，则有一定概率减速成为热中子。此过程会将中子的能量以热能形式传递给仓室。被吸收的中子停止运动，如果是快中子，其能量也会传递出去。

在GUI中可以查看单个仓室所吸收的中子数量。每种过程的概率可在REI中查看。这些概率很大程度上取决于仓室内容物和中子类型（快中子或热中子）。核燃料能更好地吸收热中子。

当中子（快中子或热中子）在核燃料中被吸收时，会产生更多中子。新产生的中子总是快中子，且方向随机。其生成过程伴随着能量以额外热量的形式直接在仓室中释放。

超过特定阈值后，产生的中子数量会随温度升高而减少，直至为零。此过程会浪费部分能量，但能保证反应堆的稳定性。产生的中子数（及实际效率）、直接释放的能量和温度阈值均可在REI中查看。

每个核部件都有最大吸收次数限制。达到上限后，物品要么被摧毁，要么转化为枯竭版本。这对核燃料尤其有用，因为枯竭过程中部分铀-238会转化为钚，意味着部分材料可以重新转化为燃料。

流体也会发生类似情况：每次吸收中子后，都会有少量流体发生转化。这可用于大规模生产氘、氚等有用同位素。两种情况的产物均可在REI中查看。

