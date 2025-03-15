---
navigation:
  title: "Elektrik"
  icon: "modern_industrialization:lv_steam_turbine"
  position: 104
  parent: modern_industrialization:electric_age.md
item_ids:
  - modern_industrialization:lv_steam_turbine
  - modern_industrialization:lv_diesel_generator
  - modern_industrialization:lv_mv_transformer
  - modern_industrialization:mv_lv_transformer
---

# Elektrik

Buhar Türbini elektrik üretmek için Buhar kullanır. Her mb Buhar'ı 1 EU'ya dönüştürür, her tikte 32 mb'a kadar dönüştürülür.

<Recipe id="modern_industrialization:electric_age/machine/lv_steam_turbine_asbl" />

Buhar Türbini, doğrudan çıkış tarafına bağlanan herhangi bir makineye otomatik olarak elektrik gönderecektir. Sadece çıkış tarafına yerleştirilen kablolara bağlanacaktır. Alçak Gerilime sahiptir (AG) *Tier*.

Her elektrik kablosunun kaç EU/t aktarabileceğini ve hangi makinelere bağlanabileceğini belirleyen bir Kademesi vardır. Bakır, Gümüş ve Kalay AG, Kupronikel ve Elektrum OG, vb....

Dizel Jeneratör, Buhar Türbinine bir alternatiftir. Elektrik üretmek için çeşitli yakıtlar kullanır. Şimdilik Kreozot yakabilirsiniz. (Yakılabilir yakıtların listesi için REI'ye bakın).

<Recipe id="modern_industrialization:electric_age/machine/lv_diesel_generator_asbl" />

Kablo ağları, ağa çekecekleri enerji miktarı konusunda sınırlamalara sahiptir: AG kabloları için en fazla 256 EU/t, OG kabloları için 1024 EU/t ve YG kabloları için 8192 EU/t. Şebeke için çıkış sınırı olmadığından ve kablolar küçük bir dahili depoya sahip olduğundan, bir AG şebekesi kısa bir süre için 256 EU/t'den daha fazlasını sağlayabilir.

Ancak tek bloklu elektrikli makinelerin yalnızca AG kablolarına bağlanacağını unutmayın!

Daha fazla enerji aktarmak için ya birden fazla boru ağı oluşturmanız ya da Transformatör kullanmanız gerekir.

Düşük kademeden yüksek kademeye bir Transformatörün (örneğin AG'den OG'ye) 5 girişi ve 1 çıkışı vardır. Yüksek kademeden düşük kademeye (örneğin OG'den AG'ye) Transformatörün 1 girişi ve 5 çıkışı vardır.



<Recipe id="modern_industrialization:electric_age/transformer/lv_mv/up_asbl" />

<Recipe id="modern_industrialization:electric_age/transformer/lv_mv/down_asbl" />

