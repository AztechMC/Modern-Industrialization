---
navigation:
  title: "코크스 만들기"
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

# 코크스 만들기

충분한 양의 청동 기계들을 제작했다면, 강철을 제작할 차례입니다. 강철 단계에서의 최종 목적은 채석기를 제작하는 것으로, 자원을 자동으로 모아주는 기계입니다.

강철을 얻기 위한 첫 단계는 코크스로입니다. 석탄을 코크스로에서 가열해서 코크스를 만들어야 합니다.

첫 멀티블록을 위해, 코크스로 블록 자체를 만들어야 합니다. 그 외에 21개의 벽돌들과 3개의 *해치*들이 필요합니다: 아이템 반입 해치, 아이템 반출 해치, 액체 반입 해치

<Recipe id="modern_industrialization:steam_age/fireclay/coke_oven" />



<Recipe id="modern_industrialization:hatches/bronze/item_input_hatch" />

<Recipe id="modern_industrialization:hatches/bronze/item_output_hatch" />



<Recipe id="modern_industrialization:hatches/bronze/fluid_input_hatch" />

<Recipe id="modern_industrialization:hatches/bronze/fluid_output_hatch" />

코크스로 블록은 *컨트롤러*의 역할을 합니다. 모든 멀티블록은 컨트롤러가 있지만, 여기에 직접 아이템을 넣는 등의 상호 작용은 불가능합니다. 모든 반입과 반출은 해치를 통해서 이루어집니다. 코크스로는 증기로 가동되기 때문에, 액체 반입 해치가 있어야 하고, 석탄을 넣을 아이템 반입 해치와 코크스를 받을 아이템 반출 해치가 필요합니다.

이 중 하나라도 빠진다면, 코크스로는 작동하지 않습니다.

**렌치를 들고 있으면 멀티블록의 빈 부분과 잘못된 부분을 알 수 있습니다.**해치를 들고 있으면 들고 있는 해치가 어디에 들어갈 수 있는지 알 수 있습니다.

REI에서는 24개의 벽돌 블록이 필요하다고 나오지만, 실제로는 3 개의 해치가 필요하므로 21 개의 벽돌 블록만 있으면 됩니다.

## Coke Oven!

해치를 놓는 방법은 다양하며, 위의 배치는 예시입니다.

![](coke_oven.png)

코크스로 블록을 우클릭했을 때 *모양 유효*라고 나온다면, 액체 반입 해치에 증기를 채우고 아이템 반입 해치에 석탄을 넣어주세요. 이제 코크스로가 작동하기 시작합니다!

코크스는 강철의 중요한 재료이지만, 매우 좋은 연료이기도 합니다. 코크스는 석탄보다 4 배 좋은 연료입니다!

