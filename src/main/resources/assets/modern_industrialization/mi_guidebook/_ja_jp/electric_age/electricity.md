---
navigation:
  title: "電気"
  icon: "modern_industrialization:lv_steam_turbine"
  position: 104
  parent: modern_industrialization:electric_age.md
item_ids:
  - modern_industrialization:lv_steam_turbine
  - modern_industrialization:lv_diesel_generator
  - modern_industrialization:lv_mv_transformer
  - modern_industrialization:mv_lv_transformer
---

# 電気

蒸気タービンは蒸気を利用して発電する。1mbの蒸気を1EUに、最大1Tickあたり32mbまで変換する。

<Recipe id="modern_industrialization:electric_age/machine/lv_steam_turbine_asbl" />

蒸気タービンは、出力側に直接接続された機械に自動的に電気を送り、出力側に置かれたケーブルにのみ接続する。この発電機は低電圧(LV)*級*である。

すべての電気ケーブルには級があり、何EU/tの電力を転送できるか、どの機械に接続できるかを決定する。銅、銀、錫はLV、キュプロニッケル、エレクトラムはMV、そして……といった具合だ。

ディーゼル発電機は、蒸気タービンに代わるものである。様々な燃料を使って電気を作る。今のところ、クレオソートを燃やすことができる。(燃やせる燃料のリストはREIを参照)

<Recipe id="modern_industrialization:electric_age/machine/lv_diesel_generator_asbl" />

ケーブルネットワークではネットワークに引き込むことのできるエネルギー量に制限がある: LVケーブルでは最大256EU/t、MVケーブルでは最大1024EU/t、HVケーブルでは最大8192EU/t。 ネットワークには出力制限がなく、ケーブルには小さな内部ストレージがあるため、LVネットワークは短時間なら256EU/t以上を供給することができる。

ただし、単一ブロックの電気機械はLVケーブルにしか接続しないことに注意！

より多くのエネルギーを送るには、複数のパイプ・ネットワークを作るか、変圧器を使う必要がある。

低圧から高圧への変圧器(たとえばLVからMV)には、5つの入力と1つの出力がある。高圧から低圧への(たとえばMVからLVへの)変圧器には、1つの入力と5つの出力がある。



<Recipe id="modern_industrialization:electric_age/transformer/lv_mv/up_asbl" />

<Recipe id="modern_industrialization:electric_age/transformer/lv_mv/down_asbl" />

