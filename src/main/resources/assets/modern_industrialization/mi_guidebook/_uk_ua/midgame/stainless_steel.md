---
navigation:
  title: "Нержавійна сталь"
  icon: "modern_industrialization:stainless_steel_dust"
  position: 201
  parent: modern_industrialization:midgame.md
item_ids:
  - modern_industrialization:vacuum_freezer
  - modern_industrialization:distillation_tower
---

# Нержавійна сталь

## Нержавійна сталь

<ItemImage id="modern_industrialization:stainless_steel_ingot" />

Наступним матеріалом, який вам знадобиться у великій кількості, є нержавійна сталь. Читайте далі, щоб дізнатися чому!

Перетоплюючи пил нержавійної сталі в електричній доменній печі, ви отримаєте розпечені злитки, які ви можете охолодити назад у звичайні злитки за допомогою вакуумної морозильної камери.

## Вакуумна морозильна камера

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="vacuum_freezer" />
</GameScene>

Основні матеріали можна перевірити за допомогою REI, а точну форму — за допомогою гайкового ключа!

<Recipe id="modern_industrialization:electric_age/machine/vacuum_freezer_asbl" />

## Distillation Tower

Коли ви отримаєте деякі цифрові схеми, обов’язково побудуйте дистиляційну вежу. Дистилятор дає вам лише один результат переробки нафти, але вежа дає вам ОДИН НА ШАР!

<Recipe id="modern_industrialization:electric_age/machine/distillation_tower_asbl" />

Ось приклад найменшого та найбільшого розмірів поруч.
Башта розміру 2 дасть вам лише перший вихід із рецепта, башта розміру 3 дасть перші два виходи, і так далі…


<GameScene zoom="1" interactive={true} fullWidth={true}>
    <MultiblockShape controller="distillation_tower" />
    <MultiblockShape controller="distillation_tower" x="-6" useBigShape={true} />
</GameScene>

