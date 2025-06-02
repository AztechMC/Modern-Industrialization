---
navigation:
  title: "전력 생산"
  icon: "modern_industrialization:lv_steam_turbine"
  position: 103
  parent: modern_industrialization:electric_age.md
item_ids:
  - modern_industrialization:lv_mv_transformer
  - modern_industrialization:mv_lv_transformer
---

# 전력 생산

증기 터빈은 증기로 전력을 생산합니다. 1 mB 의 증기당 1 EU 만큼의 전력을 생산하며, 최대 생산량은 32 EU/t 입니다.

<Recipe id="modern_industrialization:electric_age/machine/steam_turbine_asbl" />

증기 터빈은 출력면에 부착된 기계에 자동으로 전력을 전송합니다. 전선도 오로지 출력면에만 연결됩니다. 증기 터빈의 *티어*는 LV(Low Voltage)입니다.

,모든 전선들은 최대 전송량 및 기계와의 연결 여부를 결정하는 티어를 가지고 있습니다. LV와 MV, 그리고 HV 등등...

전선 네트워크는 티어에 따라 최대 입력 제한이 있습니다. LV는 256 EU/t, MV는 1024 EU/t, HV는 8192 EU/t 만큼의 제한을 가집니다. 출력에는 제한이 없고, 전선 자체가 작은 에너지 저장소 역할도 하기 때문에 LV 전선 네트워크는 잠시동안은 256 EU/t 이상의 에너지를 공급할 수 있습니다.

단일 블록 기계는 LV 전선에만 연결됩니다.

에너지를 더 전송하기 위해서는, 전선 네트워크를 여럿 만들거나 변압기를 만들어야 합니다. X to Y 변압기는 X 티어가 입력이고, Y 티어가 출력입니다.



<Recipe id="modern_industrialization:electric_age/transformer/lv_mv/up_asbl" />

<Recipe id="modern_industrialization:electric_age/transformer/lv_mv/down_asbl" />

