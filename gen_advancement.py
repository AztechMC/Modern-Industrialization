import os
from pathlib import Path
import json
import shutil

path_output = "src/main/resources/data/modern_industrialization/advancements/"
path_translation = 'src/main/resources/assets/modern_industrialization/lang/en_us.json'
shutil.rmtree(path_output, ignore_errors=True)
Path(path_output).mkdir(parents=True, exist_ok=True)

par = {}

def createAdvancement(item, title, description, parent, item_count=1, frame='task'):
    advancement_tag = 'modern_industrialization:' + item
    title_json = 'advancements.modern_industrialization.' + item
    description_json = 'advancements.modern_industrialization.' + item + '.description'

    advancement = {'display': {'icon': {'item': 'modern_industrialization:' + item},
                               'title': {'translate': title_json},
                               'frame': frame, 'description': {'translate': description_json}
                               }
                   }

    if parent is None:
        advancement['display'][
            'background'] = 'modern_industrialization:textures/blocks/fire_clay_bricks.png'
    else:
        advancement['parent'] = 'modern_industrialization:' + parent

    advancement['hidden'] = False

    advancement['criteria'] = {"checkInv": {
        "trigger": "minecraft:inventory_changed",
        "conditions": {
            "items": [
                {
                    "item": advancement_tag,
                    "count": {
                        "min": item_count
                    }
                }
            ]
        }
    }
    }

    lang_json = []

    with open(path_translation, 'r') as lang_file:
        lang_json = json.load(lang_file)
        lang_file.close()

    lang_json[title_json] = title
    lang_json[description_json] = description

    with open(path_translation, 'w') as lang_file:
        json.dump(lang_json, lang_file, indent=4, sort_keys=True)
        lang_file.close()

    with open(path_output + item + '.json', 'w') as adv_file:
        json.dump(advancement, adv_file, indent=4, sort_keys=True)
        adv_file.close()

    par[item] = parent
    cur = item
    for i in range(1000):
        if i == 999:
            raise RuntimeError("Achievement %s has more than 999 parents!" % item)
        if cur in par:
            cur = par[cur]
        else:
            break

createAdvancement('forge_hammer', 'Is This A Forge Mod ?',
                  'Craft a Forge Hammer and begin exploring Modern Industrialization', None, frame='goal')

createAdvancement('fire_clay_bricks', 'Almost Steam ?',
                  'Craft 3 Fire Clay Bricks', 'forge_hammer', item_count=3)

createAdvancement('bronze_furnace', 'Twenty Times More Fuel Efficient',
                  'Craft a Steam Furnace', 'fire_clay_bricks')

createAdvancement('bronze_boiler', 'Hot Water',
                  'Craft a Bronze Boiler', 'fire_clay_bricks')


createAdvancement('bronze_compressor', 'An Automatic Forge Mod !',
                  'Craft a Bronze Compressor', 'bronze_boiler')

createAdvancement('bronze_mixer', 'Mixing Without Mixins',
                  'Craft a Bronze Mixer', 'bronze_boiler')

createAdvancement('bronze_macerator', 'Ore Tripling',
                  'Craft a Bronze Macerator', 'bronze_boiler')

createAdvancement('coke_oven', 'Coke-A Cola',
                  'Craft a Coke Oven to start Steel Production', 'forge_hammer', frame='goal')

createAdvancement('steam_blast_furnace', 'Almost Steel !',
                  'Craft a Steam Blast Furnace', 'fire_clay_bricks')

createAdvancement('steel_machine_casing', 'Cooked Uncooked Steel !',
                  'Use a Steam Blast Furnace to produce Steel and craft a Steel Machine Casing', 'steam_blast_furnace',  frame='goal')

createAdvancement('steel_wiremill', 'Neither a Wire nor a Mill',
                  'Craft a Steel Wiremill', 'steel_machine_casing')

createAdvancement('quarry', 'From Minecraft to Craft',
                  'Craft a Steam Quarry and say goodbye to mining', 'steel_machine_casing', frame='goal')

createAdvancement('steel_packer', 'To Pack Or Not To Pack',
                  'Craft a Steel Packer', 'steel_machine_casing')

createAdvancement('inductor', 'The L in RLC',
                  'Craft an Inductor', 'steel_wiremill')

createAdvancement('resistor', 'The R in RLC',
                  'Craft a Resistor', 'steel_wiremill')

createAdvancement('capacitor', 'The C in RLC',
                  'Craft a Capacitor', 'steel_wiremill')

createAdvancement('lv_circuit', 'RLC Circuits',
                  'Craft an Analog Circuit and start the Electric Age', 'resistor', frame='goal')

createAdvancement('lv_steam_turbine', 'Better Than Solar Panels',
                  'Craft a Steam Turbine', 'lv_circuit')

createAdvancement('lv_polarizer', 'One Recipe To Rule Them All',
                  'Craft a Polarizer', 'lv_steam_turbine')

createAdvancement('large_steam_boiler', 'Kiss Your Fuel Goodbye !',
                  'Craft a Large Steam Boiler', 'lv_circuit')

createAdvancement('lv_assembler', 'Avengers, Assemble !',
                  'Craft an Assembler', 'lv_circuit', frame='goal')

createAdvancement('electric_blast_furnace', 'Electric Best Friend',
                  'Craft an Electric Blast Furnace to start producing Aluminum', 'lv_steam_turbine', frame='goal')

createAdvancement('lv_centrifuge', 'Actually It\'s The Centripetal Force',
                  'Craft a Centrifuge', 'electric_blast_furnace')

createAdvancement('lv_electrolyzer', 'It\'s Got What Plants Crave',
                  'Craft an Electrolyzer', 'electric_blast_furnace')

createAdvancement('lv_chemical_reactor', 'Walter White Approves',
                  'Craft a Chemical Reactor', 'electric_blast_furnace')

createAdvancement('lv_distillery', 'Al Capone Approves',
                  'Craft a Distillery', 'electric_blast_furnace')

createAdvancement('electric_quarry', 'Resources GO BRRRRRR !!!',
                  'Craft an Electric Quarry', 'electric_blast_furnace', frame='challenge')

createAdvancement('oil_drilling_rig', 'Bringing Freedom To Your Country',
                  'Craft an Oil Drilling Rig', 'electric_blast_furnace', frame='goal')

createAdvancement('vacuum_freezer', 'Enslaved Winter',
                  'Craft a Vacuum Freezer', 'electric_blast_furnace', frame='goal')

createAdvancement('mv_steam_turbine', 'Better Than Wind Mills',
                  'Craft an Advanced Steam Turbine', 'electric_blast_furnace', frame='goal')

createAdvancement('diesel_generator', 'Fast and Furious',
                  'Craft a Diesel Generator', 'lv_distillery')

createAdvancement('jetpack', 'The Most Expensive Jetpack TM',
                  'Craft a Diesel Jetpack', 'lv_distillery', frame='challenge')

createAdvancement('diesel_chainsaw', 'The Texas Chain Saw Massacre',
                  'Craft a Diesel Chainsaw', 'lv_distillery', frame='challenge')

createAdvancement('diesel_mining_drill', 'Through The Walls Of Ba Sing Se',
                  'Craft a Diesel Mining Drill', 'lv_distillery', frame='challenge')