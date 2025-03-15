---
navigation:
  title: "Electricity"
  icon: "modern_industrialization:lv_steam_turbine"
  position: 104
  parent: modern_industrialization:electric_age.md
item_ids:
  - modern_industrialization:lv_steam_turbine
  - modern_industrialization:lv_diesel_generator
  - modern_industrialization:lv_mv_transformer
  - modern_industrialization:mv_lv_transformer
---

# Electricity

The Steam Turbine uses Steam to produce electricity. It converts every mb of Steam to 1 EU, up to 32 mb converted every tick.

<Recipe id="modern_industrialization:electric_age/machine/lv_steam_turbine_asbl" />

The Steam Turbine will automatically send electricity to any machine connected directly to its output side. It will only connect to cables placed on its output side. It has the Low Voltage (LV) *Tier*.

Every electric cable has a Tier which determines how many EU/t it can transfer and to which machines it can connect. Copper, Silver and Tin are LV, Cupronickel and Electrum are MV, and so on...

The Diesel Generator is an alternative to the Steam Turbine. It uses various fuels to produce electricity. For now, you can burn Creosote. (See REI for the list of burnable fuels).

<Recipe id="modern_industrialization:electric_age/machine/lv_diesel_generator_asbl" />

Cable networks have limitations on the amount of energy they will pull into the network: at most 256 EU/t for LV cables, 1024 EU/t for MV cables and 8192 EU/t for HV cables. Because there is no output limit for the network and because cables have a small internal storage, an LV network can provide more than 256 EU/t for a short amount of time.

Note however that single block electric machines will only connect to LV cables!

To transfer more energy, you need to either create multiple pipe networks or use Transformers.

A low tier to high tier Transformer (for example LV to MV) has 5 inputs and 1 output. A high tier to low tier (for example MV to LV) Transformer has 1 input and 5 outputs.



<Recipe id="modern_industrialization:electric_age/transformer/lv_mv/up_asbl" />

<Recipe id="modern_industrialization:electric_age/transformer/lv_mv/down_asbl" />

