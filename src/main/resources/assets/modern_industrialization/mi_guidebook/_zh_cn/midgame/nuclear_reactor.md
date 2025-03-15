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

核反应堆是一个大型多方块结构，其目的是通过消耗核燃料来产生大量能量。它的功率比柴油高100倍。它有多种尺寸，从小到大兼备。

<Recipe id="modern_industrialization:electric_age/machine/nuclear_reactor_asbl" />

核反应堆的底层工作原理可能有点令人摸不着头脑，但是接下来几页会尽力把它解释清楚。尽管如此，你并不需要了解所有的细节，同样能设计出一个强大的反应堆。

我们建议你在创造模式下测试多种设计，直到发现适合你的那一种。

请注意，核反应堆不会以爆炸、释放辐射或其他任何方式损坏地图。如果温度过高，它唯一可能损坏的是你放入其中的物品（稍后会详细介绍）。

你可以自由地进行实验，享受作为核科学家的新生活！

核反应堆的主要部件是由核合金——镉、铍和防爆合金的混合物——制成的核外壳。

<Recipe id="modern_industrialization:electric_age/casing/nuclear_casing_asbl" />

结构的上部可以放置核物品或流体接口仓（或只放外壳）。它们是反应堆的输入和输出。

<Recipe id="modern_industrialization:electric_age/casing/nuclear_item_hatch_asbl" />

每个接口仓都有一个输入槽（物品或流体）和两个输出槽。输入槽将形成一个网格，显示在反应堆GUI中（可通过右键点击控制器访问）。

<Recipe id="modern_industrialization:electric_age/casing/nuclear_fluid_hatch_asbl" />

每个接口仓都有一个温度并积蓄热量，热量可以通过多种方式消散。接口仓的热量会自然转移到相邻的接口仓或外界（如果接口仓位于边缘）。此过程的速度等于接口仓内物品/流体的导热系数（可在REI中查看）乘以温差。热量也可以通过在流体仓中产生蒸汽导出。超过物品的最大温度会将物品摧毁。

核反应堆的核心元素是中子。中子由核燃料产生。有两种类型的中子，快中子和热中子：快中子携带能量而热中子没有。中子沿直线运动，直到撞到某个物品或反应堆边缘（对于快中子来说，所携带的能量会丢失）。中子的运动可以在反应堆GUI中查看。

当中子遇到非空接口仓时，会发生两件事：中子被吸收或被散射。散射的中子会随机改变方向。如果被散射的中子是快中子，快中子有概率减速变成热中子。这会使快中子所携带的能量以热量的形式释放给接口仓。被吸收的中子会停止运动，且快中子所携带的能量也会被释放。

在GUI中可以看到单个接口仓所吸收的中子数。每个过程的概率可在REI中查看。这些数值会大幅度地受接口仓内物品/流体种类和中子类型（快中子或热中子）所影响。核燃料能更好地吸收热中子。

当核燃料吸收快中子或热中子时，会产生更多中子。所产生的中子永远是快中子且运动方向随机。它们的产生伴随着能量的直接释放，导致接口仓的升温。

超过某个阈值，燃料产生的中子数会随着温度的升高而减少，直到达到零。这个过程会浪费一些能量，但保证了反应堆的稳定性。产生的中子数（和有效效率）、直接释放的能量和温度阈值都可在REI中查看。

每个核部件都有最大中子吸收次数。到达最大次数时，该物品要么被销毁，要么转换为枯竭的版本。这对核燃料特别有用，因为铀238的某些部分会转化为枯竭版本的钚，意味着其中的一些可以转化回燃料。

流体也会发生同样的事情：在每次吸收中子后，都会有一点流体发生变化。这可用于同位素的大量生产，如氘和氚。两种事件的相关数值都显示在REI中。

