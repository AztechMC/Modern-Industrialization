import os
from pathlib import Path
import json
import shutil

path_output = "src/main/resources/data/modern_industrialization/advancements/"
path_translation = 'src/main/resources/assets/modern_industrialization/lang/en_us.json'
shutil.rmtree(path_output, ignore_errors=True)
Path(path_output).mkdir(parents=True, exist_ok=True)


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


createAdvancement('forge_hammer', 'Modern Industrialization',
                  'Craft a Forge Hammer and begin exploring Modern Industrialization', None, frame='goal')

createAdvancement('fire_clay_bricks', 'Almost Steel ?',
                  'Craft 3 Fire Clay Bricks', 'forge_hammer', item_count=3)

createAdvancement('bronze_furnace', 'Fourty Times more Fuel Efficient',
                  'Craft a Steam Furnace', 'fire_clay_bricks')

createAdvancement('bronze_boiler', 'Hot Water',
                  'Craft a Bronze Boiler', 'fire_clay_bricks')


createAdvancement('bronze_compressor', 'An Automatic Forge Mod !',
                  'Craft a Bronze Compressor', 'bronze_boiler')

createAdvancement('bronze_mixer', 'Mixing without Mixins',
                  'Craft a Bronze Mixer', 'bronze_boiler')

createAdvancement('bronze_macerator', 'Ore Tripling',
                  'Craft a Bronze Macerator', 'bronze_boiler')

createAdvancement('coke_oven', 'Coke-A Cola',
                  'Craft a Coke Oven to start Steel Production', 'forge_hammer', frame='goal')

createAdvancement('steam_blast_furnace', 'Almost Steel !',
                  'Craft a Steam Blast Furnace', 'fire_clay_bricks')

createAdvancement('steel_machine_casing', 'Cooked Uncooked Steel !',
                  'Use a Steam Blast Furnace to produce Steel and craft a Steel Machine Casing', 'steam_blast_furnace',  frame='goal')

createAdvancement('steel_wiremill', 'Neither a Wire or a Mill',
                  'Craft a Steel Wiremill', 'steel_machine_casing')

createAdvancement('quarry', 'From Minecraft to Craft',
                  'Craft a Steam Quarry and say goodbye to mining', 'steel_machine_casing', frame='goal')

createAdvancement('steel_packer', 'To Pack or Unpack',
                  'Craft a Steel Packer', 'steel_machine_casing')

createAdvancement('inductor', 'L',
                  'Craft an Inductor', 'steel_wiremill')

createAdvancement('resistor', 'R',
                  'Craft an Resistor', 'steel_wiremill')

createAdvancement('capacitor', 'C',
                  'Craft an Capacitor', 'steel_wiremill')

createAdvancement('lv_circuit', 'RLC',
                  'Craft an Analogic Circuit and start the Electric Age', 'resistor', frame='goal')

createAdvancement('lv_steam_turbine', 'Better than Solar Panels',
                  'Craft a Steam Turbine', 'lv_circuit')

createAdvancement('lv_polarizer', 'One recipe is still better than zero',
                  'Craft a Polarizer', 'lv_steam_turbine')

createAdvancement('large_steam_boiler', 'Kiss your Fuel goodbye !',
                  'Craft a Large Steam Boiler', 'lv_circuit')

createAdvancement('lv_assembler', 'Assemblers, ASSEMBLE !',
                  'Craft an Electric Assembling Machine', 'lv_polarizer', frame='goal')

createAdvancement('electric_blast_furnace', 'Electric Best Friend',
                  'Craft an Electric Blast Furnace to boostrap Aluminum Production', 'lv_steam_turbine', frame='goal')

createAdvancement('lv_centrifuge', 'Actually it\'s the CENTRIPEDE Force',
                  'Craft an Electric Centrifuge', 'electric_blast_furnace')

createAdvancement('lv_electrolyzer', 'It\'s got electrolytes ! It\'s what plants crave',
                  'Craft an Electrolyzer', 'electric_blast_furnace')

createAdvancement('lv_chemical_reactor', 'Walter White Approves',
                  'Craft a Chemical Reactor', 'electric_blast_furnace')

createAdvancement('lv_distillery', 'Al Capone Approves',
                  'Craft a Distillery', 'electric_blast_furnace')

createAdvancement('electric_quarry', 'Ressources GOES BRRRRRR !!!',
                  'Craft an Electric Quarry', 'electric_blast_furnace', frame='challenge')

createAdvancement('oil_drilling_rig', 'Bringin Freedom to your country',
                  'Craft an Oil Drilling Rig', 'electric_blast_furnace', frame='goal')

createAdvancement('vacuum_freezer', 'Enslaved Winter',
                  'Craft a Vacuum Freezer', 'electric_blast_furnace', frame='goal')

createAdvancement('diesel_generator', 'Fast and Furious',
                  'Craft a Diesel Generator', 'electric_blast_furnace')

createAdvancement('jetpack', 'The Most Expansive Jetpack TM',
                  'Craft a Jetpack', 'diesel_generator', frame='challenge')

createAdvancement('diesel_chainsaw', 'The Texas Chain Saw Massacre',
                  'Craft a Diesel Chainsaw', 'diesel_generator', frame='challenge')

createAdvancement('diesel_mining_drill', 'Trough the Walls of Ba Sing Se',
                  'Craft a Diesel Mining Drill', 'diesel_generator', frame='challenge')