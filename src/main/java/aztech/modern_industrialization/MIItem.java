/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization;

import static aztech.modern_industrialization.items.SortOrder.*;

import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.definition.ItemDefinition;
import aztech.modern_industrialization.items.*;
import aztech.modern_industrialization.items.armor.GraviChestPlateItem;
import aztech.modern_industrialization.items.armor.JetpackItem;
import aztech.modern_industrialization.items.armor.QuantumArmorItem;
import aztech.modern_industrialization.items.armor.RubberArmorMaterial;
import aztech.modern_industrialization.items.diesel_tools.DieselToolItem;
import aztech.modern_industrialization.items.tools.QuantumSword;
import aztech.modern_industrialization.nuclear.INeutronBehaviour;
import aztech.modern_industrialization.nuclear.NuclearComponentItem;
import aztech.modern_industrialization.nuclear.NuclearConstant;
import dev.technici4n.grandpower.api.ISimpleEnergyItem;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public final class MIItem {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MI.ID);
    public static final SortedMap<ResourceLocation, ItemDefinition<?>> ITEM_DEFINITIONS = new TreeMap<>();

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    // @formatter:off
    // Guide book first so people read it!
    public static final ItemDefinition<GuideBookItem> GUIDE_BOOK = item("MI Guidebook", "guidebook", GuideBookItem::new, SortOrder.GUIDE_BOOK);

    // Forge hammer, then the various hammers!
    public static final ItemDefinition<ForgeTool> IRON_HAMMER = itemHandheld("Iron Hammer", "iron_hammer", p -> new ForgeTool(Tiers.IRON, p), HAMMER);
    public static final ItemDefinition<ForgeTool> STEEL_HAMMER = itemHandheld("Steel Hammer", "steel_hammer", p -> new ForgeTool(ForgeTool.STEEL, p), HAMMER);
    public static final ItemDefinition<ForgeTool> DIAMOND_HAMMER = itemHandheld("Diamond Hammer", "diamond_hammer", p -> new ForgeTool(Tiers.DIAMOND, p), HAMMER);
    public static final ItemDefinition<ForgeTool> NETHERITE_HAMMER = itemHandheld("Netherite Hammer", "netherite_hammer", p -> new ForgeTool(Tiers.NETHERITE, p), HAMMER);

    // Steam tier stuff
    public static final ItemDefinition<Item> STEEL_UPGRADE = item("Steel Upgrade", "steel_upgrade", SteelUpgradeItem::new, STEAM_TIER);
    public static final ItemDefinition<Item> RUBBER_SHEET = item("Rubber Sheet", "rubber_sheet", STEAM_TIER);
    public static final ItemDefinition<Item> PACKER_BLOCK_TEMPLATE = item("Packer Block Template", "packer_block_template", p -> new Item(p.rarity(Rarity.RARE).stacksTo(1)), STEAM_TIER);
    public static final ItemDefinition<Item> PACKER_DOUBLE_INGOT_TEMPLATE = item("Packer Double Ingot Template", "packer_double_ingot_template", p -> new Item(p.rarity(Rarity.RARE).stacksTo(1)), STEAM_TIER);

    // Mechanical components: motors
    public static final ItemDefinition<Item> MOTOR = item("Motor", "motor", ITEMS_OTHER);
    public static final ItemDefinition<Item> LARGE_MOTOR = item("Large Motor", "large_motor", ITEMS_OTHER);
    public static final ItemDefinition<Item> ADVANCED_MOTOR = item("Advanced Motor", "advanced_motor", ITEMS_OTHER);
    public static final ItemDefinition<Item> LARGE_ADVANCED_MOTOR = item("Large Advanced Motor", "large_advanced_motor", ITEMS_OTHER);
    // Mechanical components: pumps
    public static final ItemDefinition<Item> PUMP = item("Pump", "pump", ITEMS_OTHER);
    public static final ItemDefinition<Item> LARGE_PUMP = item("Large Pump", "large_pump", ITEMS_OTHER);
    public static final ItemDefinition<Item> ADVANCED_PUMP = item("Advanced Pump", "advanced_pump", ITEMS_OTHER);
    public static final ItemDefinition<Item> LARGE_ADVANCED_PUMP = item("Large Advanced Pump", "large_advanced_pump", ITEMS_OTHER);
    // Mechanical components: others
    public static final ItemDefinition<Item> PISTON = item("Piston", "piston", ITEMS_OTHER);
    public static final ItemDefinition<Item> CONVEYOR = item("Conveyor", "conveyor", ITEMS_OTHER);
    public static final ItemDefinition<Item> ROBOT_ARM = item("Robot Arm", "robot_arm", ITEMS_OTHER);

    // Circuits
    public static final ItemDefinition<Item> CIRCUIT_BOARD = item("Analog Circuit Board", "analog_circuit_board", ITEMS_OTHER);
    public static final ItemDefinition<Item> ANALOG_CIRCUIT = item("Analog Circuit", "analog_circuit", ITEMS_OTHER);
    public static final ItemDefinition<Item> ELECTRONIC_CIRCUIT_BOARD = item("Electronic Circuit Board", "electronic_circuit_board", ITEMS_OTHER);
    public static final ItemDefinition<Item> ELECTRONIC_CIRCUIT = item("Electronic Circuit", "electronic_circuit", ITEMS_OTHER);
    public static final ItemDefinition<Item> PROCESSING_UNIT_BOARD = item("Processing Unit Board", "processing_unit_board", ITEMS_OTHER);
    public static final ItemDefinition<Item> PROCESSING_UNIT = item("Processing Unit", "processing_unit", ITEMS_OTHER);
    public static final ItemDefinition<Item> DIGITAL_CIRCUIT_BOARD = item("Digital Circuit Board", "digital_circuit_board", ITEMS_OTHER);
    public static final ItemDefinition<Item> DIGITAL_CIRCUIT = item("Digital Circuit", "digital_circuit", ITEMS_OTHER);
    public static final ItemDefinition<Item> QUANTUM_CIRCUIT_BOARD = item("Quantum Circuit Board", "quantum_circuit_board", (p) -> new Item(p.rarity(Rarity.RARE)), ITEMS_OTHER);
    public static final ItemDefinition<Item> QUANTUM_CIRCUIT = item("Quantum Circuit", "quantum_circuit", (p) -> new Item(p.rarity(Rarity.RARE)), ITEMS_OTHER);

    // LV circuits
    public static final ItemDefinition<Item> RESISTOR = item("Resistor", "resistor", ITEMS_OTHER);
    public static final ItemDefinition<Item> CAPACITOR = item("Capacitor", "capacitor", ITEMS_OTHER);
    public static final ItemDefinition<Item> INDUCTOR = item("Inductor", "inductor", ITEMS_OTHER);
    public static final ItemDefinition<Item> WOOD_PULP = item("Wood Pulp", "wood_pulp", ITEMS_OTHER);
    public static final ItemDefinition<Item> INVAR_ROTARY_BLADE = item("Invar Rotary Blade", "invar_rotary_blade", ITEMS_OTHER);

    // MV circuits
    public static final ItemDefinition<Item> DIODE = item("Diode", "diode", ITEMS_OTHER);
    public static final ItemDefinition<Item> TRANSISTOR = item("Transistor", "transistor", ITEMS_OTHER);

    // HV circuits
    public static final ItemDefinition<Item> OP_AMP = item("Op Amp", "op_amp", ITEMS_OTHER);
    public static final ItemDefinition<Item> AND_GATE = item("AND Gate", "and_gate", ITEMS_OTHER);
    public static final ItemDefinition<Item> OR_GATE = item("OR Gate", "or_gate", ITEMS_OTHER);
    public static final ItemDefinition<Item> NOT_GATE = item("NOT Gate", "not_gate", ITEMS_OTHER);

    // EV circuits
    public static final ItemDefinition<Item> AIR_INTAKE = item("Air Intake", "air_intake", p -> new Item(p.stacksTo(1)), ITEMS_OTHER);
    public static final ItemDefinition<Item> MONOCRYSTALLINE_SILICON = item("Monocrystalline Silicon", "monocrystalline_silicon", ITEMS_OTHER);
    public static final ItemDefinition<Item> SILICON_WAFER = item("Silicon Wafer", "silicon_wafer", ITEMS_OTHER);
    public static final ItemDefinition<Item> ARITHMETIC_LOGIC_UNIT = item("Arithmetic Logic Unit", "arithmetic_logic_unit", ITEMS_OTHER);
    public static final ItemDefinition<Item> MEMORY_MANAGEMENT_UNIT = item("Memory Management Unit", "memory_management_unit", ITEMS_OTHER);
    public static final ItemDefinition<Item> RANDOM_ACCESS_MEMORY = item("Random Access Memory", "random_access_memory", ITEMS_OTHER);

    // Quantum circuits
    // TODO NEO: Change item id to "qubit"
    public static final ItemDefinition<Item> QUBIT = item("Qubit", "qbit", (p) -> new Item(p.rarity(Rarity.RARE)), ITEMS_OTHER);
    public static final ItemDefinition<Item> COOLING_CELL = item("Cooling Cell", "cooling_cell", ITEMS_OTHER);
    public static final ItemDefinition<Item> ULTRADENSE_METAL_BALL = item("Ultradense Metal Ball", "ultradense_metal_ball", ITEMS_OTHER);
    public static final ItemDefinition<Item> SINGULARITY = item("Singularity", "singularity", p -> new Item(p.rarity(Rarity.EPIC)), ITEMS_OTHER);

    // Upgrades
    public static final ItemDefinition<Item> BASIC_UPGRADE = item("Basic Upgrade", "basic_upgrade", ITEMS_OTHER);
    public static final ItemDefinition<Item> ADVANCED_UPGRADE = item("Advanced Upgrade", "advanced_upgrade", ITEMS_OTHER);
    public static final ItemDefinition<Item> TURBO_UPGRADE = item("Turbo Upgrade", "turbo_upgrade", (p) -> new Item(p.rarity(Rarity.UNCOMMON)), ITEMS_OTHER);
    public static final ItemDefinition<Item> HIGHLY_ADVANCED_UPGRADE = item("Highly Advanced Upgrade", "highly_advanced_upgrade", (p) -> new Item(p.rarity(Rarity.RARE)), ITEMS_OTHER);
    public static final ItemDefinition<Item> QUANTUM_UPGRADE = item("Quantum Upgrade", "quantum_upgrade", (p) -> new Item(p.stacksTo(1).rarity(Rarity.RARE)), ITEMS_OTHER);

    public static final ItemDefinition<Item> REDSTONE_CONTROL_MODULE = item("Redstone Control Module", "redstone_control_module", RedstoneControlModuleItem::new, (item, itemModelGenerators) -> {}, ITEMS_OTHER);

    // Tools
    public static final ItemDefinition<Item> WRENCH = itemNoModel("Wrench", "wrench", ITEMS_OTHER);

    public static final ItemDefinition<SteamDrillItem> STEAM_MINING_DRILL = itemHandheld("Steam Mining Drill", "steam_mining_drill",SteamDrillItem::new);

    public static final ItemDefinition<DieselToolItem> DIESEL_MINING_DRILL = itemHandheld("Diesel Mining Drill", "diesel_mining_drill", s -> new DieselToolItem(s, 7))
            .withItemRegistrationEvent((item) -> {
                MICapabilities.onEvent(event -> {
                    event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidFuelItemHelper.ItemStorage(stack, DieselToolItem.CAPACITY), item);
                });
            });
    public static final ItemDefinition<DieselToolItem> DIESEL_CHAINSAW = itemHandheld("Diesel Chainsaw", "diesel_chainsaw", p -> new DieselToolItem(p, 12))
            .withItemRegistrationEvent((item) -> {
                MICapabilities.onEvent(event -> {
                    event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidFuelItemHelper.ItemStorage(stack, DieselToolItem.CAPACITY), item);
                });
            });

    public static final ItemDefinition<PortableStorageUnit> PORTABLE_STORAGE_UNIT = itemHandheld("Portable Storage Unit", "portable_storage_unit", PortableStorageUnit::new)
            .withItemRegistrationEvent(item -> {
                MICapabilities.onEvent(event -> {
                    event.registerItem(EnergyApi.ITEM, (stack, ctx) -> ISimpleEnergyItem.createStorage(stack, item.getEnergyCapacity(stack), item.getEnergyMaxInput(stack), item.getEnergyMaxOutput(stack)), item);
                });
            });

    // Armor
    public static final ItemDefinition<ArmorItem> RUBBER_HELMET = item("Rubber Helmet", "rubber_helmet", s -> new ArmorItem(RubberArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, s.stacksTo(1)), ITEMS_OTHER);
    public static final ItemDefinition<ArmorItem> RUBBER_BOOTS = item("Rubber Boots", "rubber_boots", s -> new ArmorItem(RubberArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, s.stacksTo(1)), ITEMS_OTHER);
    public static final ItemDefinition<JetpackItem> DIESEL_JETPACK = item("Diesel Jetpack", "diesel_jetpack", JetpackItem::new, ITEMS_OTHER)
            .withItemRegistrationEvent(item -> {
                MICapabilities.onEvent(event -> {
                    event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidFuelItemHelper.ItemStorage(stack, JetpackItem.CAPACITY), item);
                });
            });

    public static final ItemDefinition<GraviChestPlateItem> GRAVICHESTPLATE = item("Gravichestplate", "gravichestplate", GraviChestPlateItem::new, ITEMS_OTHER)
            .withItemRegistrationEvent(item -> {
                MICapabilities.onEvent(event -> {
                    event.registerItem(EnergyApi.ITEM, (stack, ctx) -> ISimpleEnergyItem.createStorage(stack, item.getEnergyCapacity(stack), item.getEnergyMaxInput(stack), item.getEnergyMaxOutput(stack)), item);
                });
            });

    public static final ItemDefinition<QuantumSword> QUANTUM_SWORD = itemHandheld("Quantum Sword", "quantum_sword", QuantumSword::new);
    public static final ItemDefinition<QuantumArmorItem> QUANTUM_HELMET = item("Quantum Helmet", "quantum_helmet", s -> new QuantumArmorItem(ArmorItem.Type.HELMET, s), ITEMS_OTHER);
    public static final ItemDefinition<QuantumArmorItem> QUANTUM_CHESTPLATE = item("Quantum Chestplate", "quantum_chestplate", s -> new QuantumArmorItem(ArmorItem.Type.CHESTPLATE, s), ITEMS_OTHER);
    public static final ItemDefinition<QuantumArmorItem> QUANTUM_LEGGINGS = item("Quantum Leggings", "quantum_leggings", s -> new QuantumArmorItem(ArmorItem.Type.LEGGINGS, s), ITEMS_OTHER);
    public static final ItemDefinition<QuantumArmorItem> QUANTUM_BOOTS = item("Quantum Boots", "quantum_boots", s -> new QuantumArmorItem(ArmorItem.Type.BOOTS, s), ITEMS_OTHER);

    // Material-like items
    public static final ItemDefinition<Item> UNCOOKED_STEEL_DUST = item("Uncooked Steel Dust", "uncooked_steel_dust", MATERIALS.and("steel"));
    public static final ItemDefinition<Item> MIXED_INGOT_BLASTPROOF = item("Mixed Blastproof Ingot", "mixed_ingot_blastproof", MATERIALS.and("blastproof"));
    public static final ItemDefinition<Item> MIXED_INGOT_IRIDIUM = item("Mixed Iridium Ingot", "mixed_ingot_iridium", s -> new Item(s.food(new FoodProperties.Builder().nutrition(20).saturationMod(1).build())), MATERIALS.and("iridium"));
    public static final ItemDefinition<Item> MIXED_PLATE_NUCLEAR = item("Nuclear Mixed Plate", "mixed_plate_nuclear", MATERIALS.and("nuclear"));

    // Others
    public static final ItemDefinition<Item> WAX = item("Wax", "wax", HoneycombItem::new, ITEMS_OTHER);
    public static final ItemDefinition<NuclearComponentItem> SMALL_HEAT_EXCHANGER = NuclearComponentItem.of(
            "Small Heat Exchanger", "small_heat_exchanger",
            2500, 15 * NuclearConstant.BASE_HEAT_CONDUCTION, INeutronBehaviour.NO_INTERACTION);
    public static final ItemDefinition<NuclearComponentItem> LARGE_HEAT_EXCHANGER = NuclearComponentItem.of(
            "Large Heat Exchanger", "large_heat_exchanger",
            1800, 30 * NuclearConstant.BASE_HEAT_CONDUCTION, INeutronBehaviour.NO_INTERACTION);

    public static final ItemDefinition<ConfigCardItem> CONFIG_CARD = item("Pipe Config Card", "config_card", ConfigCardItem::new, PIPES);

    // @formatter:on

    public static <T extends Item> ItemDefinition<T> item(
            String englishName,
            String path,
            Function<Item.Properties, T> ctor,
            BiConsumer<Item, ItemModelProvider> modelGenerator,
            SortOrder sortOrder) {

        var holder = ITEMS.registerItem(path, ctor);
        var def = new ItemDefinition<>(englishName, holder, modelGenerator, sortOrder);
        ITEM_DEFINITIONS.put(holder.getId(), def);
        return def;
    }

    public static ItemDefinition<Item> item(String englishName, String path, SortOrder sortOrder) {
        return MIItem.item(englishName, path, Item::new, (item, modelGenerator) -> modelGenerator.basicItem(item), sortOrder);
    }

    public static <T extends Item> ItemDefinition<T> item(String englishName, String path, Function<Item.Properties, T> ctor,
            SortOrder sortOrder) {
        return MIItem.item(englishName, path, ctor, (item, modelGenerator) -> modelGenerator.basicItem(item), sortOrder);
    }

    public static ItemDefinition<Item> itemHandheld(String englishName, String path) {
        return MIItem.itemHandheld(englishName, path, Item::new);
    }

    public static ItemDefinition<Item> itemNoModel(String englishName, String path, SortOrder sortOrder) {
        return MIItem.item(englishName, path, Item::new, (item, modelGenerator) -> {
        }, sortOrder);
    }

    public static <T extends Item> ItemDefinition<T> itemNoModel(String englishName, String path, Function<Item.Properties, T> ctor,
            SortOrder sortOrder) {
        return MIItem.item(englishName, path, ctor, (item, modelGenerator) -> {
        }, sortOrder);
    }

    public static <T extends Item> ItemDefinition<T> itemHandheld(String englishName, String path, Function<Item.Properties, T> ctor) {
        return itemHandheld(englishName, path, ctor, ITEMS_OTHER);
    }

    public static <T extends Item> ItemDefinition<T> itemHandheld(String englishName, String path, Function<Item.Properties, T> ctor,
            SortOrder sortOrder) {
        return MIItem.item(englishName, path, p -> ctor.apply(p.stacksTo(1)), (item, modelGenerator) -> {
            modelGenerator.basicItem(item).parent(modelGenerator.getExistingFile(new ResourceLocation("minecraft:item/handheld")));
        }, sortOrder);
    }

    private MIItem() {
    }
}
