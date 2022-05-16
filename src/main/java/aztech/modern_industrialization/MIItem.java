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

import aztech.modern_industrialization.api.pipes.item.SpeedUpgrade;
import aztech.modern_industrialization.definition.ItemDefinition;
import aztech.modern_industrialization.items.FluidFuelItemHelper;
import aztech.modern_industrialization.items.ForgeTool;
import aztech.modern_industrialization.items.GuideBookItem;
import aztech.modern_industrialization.items.SteamDrillItem;
import aztech.modern_industrialization.items.armor.GraviChestPlateItem;
import aztech.modern_industrialization.items.armor.JetpackItem;
import aztech.modern_industrialization.items.armor.QuantumArmorItem;
import aztech.modern_industrialization.items.armor.RubberArmorMaterial;
import aztech.modern_industrialization.items.diesel_tools.DieselToolItem;
import aztech.modern_industrialization.items.tools.CrowbarItem;
import aztech.modern_industrialization.items.tools.QuantumSword;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;

@SuppressWarnings("unused")
public final class MIItem {

    public static SortedMap<ResourceLocation, ItemDefinition<?>> ITEMS = new TreeMap<>();

    public static final ItemDefinition<Item> STEEL_UPGRADE = item("Steel Upgrade", "steel_upgrade");

    public static final ItemDefinition<GuideBookItem> ITEM_GUIDE_BOOK = item("Modern Industrialization Guidebook", "guidebook", GuideBookItem::new);
    public static final ItemDefinition<Item> ITEM_UNCOOKED_STEEL_DUST = item("Uncooked Steel Dust", "uncooked_steel_dust");

    public static final ItemDefinition<ArmorItem> RUBBER_HELMET = item("Rubber Helmet", "rubber_helmet",
            s -> new ArmorItem(RubberArmorMaterial.INSTANCE, EquipmentSlot.HEAD, s.maxCount(1)));
    public static final ItemDefinition<ArmorItem> RUBBER_BOOTS = item("Rubber Boots", "rubber_boots",
            s -> new ArmorItem(RubberArmorMaterial.INSTANCE, EquipmentSlot.FEET, s.maxCount(1)));

    public static final ItemDefinition<Item> ITEM_MOTOR = item("Motor", "motor")
            .withItemRegistrationEvent((item) -> SpeedUpgrade.LOOKUP.registerForItems((key, vd) -> () -> 2, item));
    public static final ItemDefinition<Item> ITEM_PISTON = item("Piston", "piston");
    public static final ItemDefinition<Item> ITEM_CONVEYOR = item("Conveyor", "conveyor");
    public static final ItemDefinition<Item> ITEM_ROBOT_ARM = item("Robot Arm", "robot_arm");
    public static final ItemDefinition<Item> ITEM_CIRCUIT = item("Analog Circuit", "analog_circuit");
    public static final ItemDefinition<Item> ITEM_CIRCUIT_BOARD = item("Analog Circuit Board", "analog_circuit_board");
    public static final ItemDefinition<Item> ITEM_PUMP = item("Pump", "pump");
    public static final ItemDefinition<Item> ITEM_RESISTOR = item("Resistor", "resistor");
    public static final ItemDefinition<Item> ITEM_CAPACITOR = item("Capacitor", "capacitor");
    public static final ItemDefinition<Item> ITEM_INDUCTOR = item("Inductor", "inductor");
    public static final ItemDefinition<Item> ITEM_WOOD_PULP = item("Wood Pulp", "wood_pulp");
    public static final ItemDefinition<Item> ITEM_RUBBER_SHEET = item("Rubber Sheet", "rubber_sheet");
    public static final ItemDefinition<Item> ITEM_INVAR_ROTARY_BLADE = item("Invar Rotary Blade", "invar_rotary_blade");

    public static final ItemDefinition<Item> ITEM_ELECTRONIC_CIRCUIT = item("Electronic Circuit", "electronic_circuit");
    public static final ItemDefinition<Item> ITEM_DIODE = item("Diode", "diode");
    public static final ItemDefinition<Item> ITEM_ELECTRONIC_CIRCUIT_BOARD = item("Electronic Circuit Board", "electronic_circuit_board");
    public static final ItemDefinition<Item> ITEM_TRANSISTOR = item("Transistor", "transistor");
    public static final ItemDefinition<Item> ITEM_LARGE_MOTOR = item("Large Motor", "large_motor")
            .withItemRegistrationEvent((item) -> SpeedUpgrade.LOOKUP.registerForItems((key, vd) -> () -> 8, item));

    public static final ItemDefinition<Item> ITEM_LARGE_PUMP = item("Large Pump", "large_pump");

    public static final ItemDefinition<Item> ITEM_DIGITAL_CIRCUIT = item("Digital Circuit", "digital_circuit");
    public static final ItemDefinition<Item> ITEM_DIGITAL_CIRCUIT_BOARD = item("Digital Circuit Board", "digital_circuit_board");
    public static final ItemDefinition<Item> ITEM_OP_AMP = item("Op Amp", "op_amp");
    public static final ItemDefinition<Item> ITEM_AND_GATE = item("AND Gate", "and_gate");
    public static final ItemDefinition<Item> ITEM_OR_GATE = item("OR Gate", "or_gate");
    public static final ItemDefinition<Item> ITEM_NOT_GATE = item("NOT Gate", "not_gate");

    public static final ItemDefinition<Item> ITEM_PROCESSING_UNIT = item("Processing Unit", "processing_unit");
    public static final ItemDefinition<Item> ITEM_PROCESSING_UNIT_BOARD = item("Processing Unit Board", "processing_unit_board");
    public static final ItemDefinition<Item> ITEM_ARITHMETIC_LOGIC_UNIT = item("Arithmetic Logic Unit", "arithmetic_logic_unit");
    public static final ItemDefinition<Item> ITEM_RANDOM_ACCESS_MEMORY = item("Random Access Memory", "random_access_memory");
    public static final ItemDefinition<Item> ITEM_MEMORY_MANAGEMENT_UNIT = item("Memory Management Unit", "memory_management_unit");

    public static final ItemDefinition<Item> ITEM_QUANTUM_CIRCUIT_BOARD = item("Quantum Circuit Board", "quantum_circuit_board",
            (p) -> new Item(p.rarity(Rarity.RARE)));
    public static final ItemDefinition<Item> ITEM_QUANTUM_CIRCUIT = item("Quantum Circuit", "quantum_circuit",
            (p) -> new Item(p.rarity(Rarity.RARE)));
    public static final ItemDefinition<Item> ITEM_QBIT = item("QBit", "qbit", (p) -> new Item(p.rarity(Rarity.RARE)));

    public static final ItemDefinition<Item> ITEM_MONOCRYSTALLINE_SILICON = item("Monocrystalline Silicon", "monocrystalline_silicon");
    public static final ItemDefinition<Item> ITEM_SILICON_WAFER = item("Silicon Wafer", "silicon_wafer");

    public static final ItemDefinition<Item> BASIC_UPGRADE = item("Basic Upgrade", "basic_upgrade");
    public static final ItemDefinition<Item> ADVANCED_UPGRADE = item("Advanced Upgrade", "advanced_upgrade");
    public static final ItemDefinition<Item> TURBO_UPGRADE = item("Turbo Upgrade", "turbo_upgrade", (p) -> new Item(p.rarity(Rarity.UNCOMMON)));
    public static final ItemDefinition<Item> HIGHLY_ADVANCED_UPGRADE = item("Highly Advanced Upgrade", "highly_advanced_upgrade",
            (p) -> new Item(p.rarity(Rarity.RARE)));
    public static final ItemDefinition<Item> QUANTUM_UPGRADE = item("Quantum Upgrade", "quantum_upgrade",
            (p) -> new Item(p.maxCount(1).rarity(Rarity.RARE)));

    public static final ItemDefinition<Item> ADVANCED_MOTOR = item("Advanced Motor", "advanced_motor")
            .withItemRegistrationEvent((item) -> SpeedUpgrade.LOOKUP.registerForItems((key, vd) -> () -> 32, item));
    public static final ItemDefinition<Item> LARGE_ADVANCED_MOTOR = item("Large Advanced Motor", "large_advanced_motor").withItemRegistrationEvent(
            (item) -> SpeedUpgrade.LOOKUP.registerForItems((key, vd) -> () -> 64, item));
    public static final ItemDefinition<Item> ADVANCED_PUMP = item("Advanced Pump", "advanced_pump");
    public static final ItemDefinition<Item> LARGE_ADVANCED_PUMP = item("Large Advanced Pump", "large_advanced_pump");

    public static final ItemDefinition<Item> MIXED_INGOT_BLASTPROOF = item("Mixed Ingot Blastproof", "mixed_ingot_blastproof");
    public static final ItemDefinition<Item> MIXED_INGOT_IRIDIUM = item("Mixed Iridium Ingot", "mixed_ingot_iridium");

    public static final ItemDefinition<Item> MIXED_PLATE_NUCLEAR = item("Nuclear Mixed Plate", "mixed_plate_nuclear");

    public static final ItemDefinition<Item> AIR_INTAKE = item("Air Intake", "air_intake", p -> new Item(p.maxCount(1)));

    public static final ItemDefinition<Item> ITEM_PACKER_BLOCK_TEMPLATE = item("Packer Block Template", "packer_block_template",
            p -> new Item(p.rarity(Rarity.RARE).maxCount(1)));
    public static final ItemDefinition<Item> ITEM_PACKER_DOUBLE_INGOT_TEMPLATE = item("Packer Double Ingot Template", "packer_double_ingot_template",
            p -> new Item(p.rarity(Rarity.RARE).maxCount(1)));

    public static final ItemDefinition<Item> ITEM_SCREWDRIVER = itemHandheld("Screwdriver", "screwdriver");
    public static final ItemDefinition<Item> ITEM_WRENCH = itemHandheld("Wrench", "wrench");

    public static final ItemDefinition<JetpackItem> ITEM_DIESEL_JETPACK = item("Diesel Jetpack", "diesel_jetpack", JetpackItem::new)
            .withItemRegistrationEvent(
                    (item) -> FluidStorage.ITEM.registerForItems((stack, ctx) -> new FluidFuelItemHelper.ItemStorage(JetpackItem.CAPACITY, ctx),
                            item));

    public static final ItemDefinition<DieselToolItem> ITEM_DIESEL_CHAINSAW = itemHandheld("Diesel Chainsaw", "diesel_chainsaw",
            p -> new DieselToolItem(p, 12)).withItemRegistrationEvent(
                    (item) -> FluidStorage.ITEM.registerForItems((stack, ctx) -> new FluidFuelItemHelper.ItemStorage(DieselToolItem.CAPACITY, ctx),
                            item));

    public static final ItemDefinition<DieselToolItem> ITEM_DIESEL_MINING_DRILL = itemHandheld("Diesel Mining Drill", "diesel_mining_drill",
            s -> new DieselToolItem(s, 7)).withItemRegistrationEvent(
                    (item) -> FluidStorage.ITEM.registerForItems((stack, ctx) -> new FluidFuelItemHelper.ItemStorage(DieselToolItem.CAPACITY, ctx),
                            item));

    public static final ItemDefinition<SteamDrillItem> ITEM_STEAM_MINING_DRILL = itemHandheld("Steam Mining Drill", "steam_mining_drill",
            SteamDrillItem::new);
    public static final ItemDefinition<CrowbarItem> ITEM_CROWBAR = itemHandheld("Crowbar", "crowbar", CrowbarItem::new);

    public static final ItemDefinition<Item> COOLING_CELL = item("Cooling Cell", "cooling_cell");

    public static final ItemDefinition<GraviChestPlateItem> GRAVICHESTPLATE = item("Gravichestplate", "gravichestplate", GraviChestPlateItem::new);

    public static final ItemDefinition<QuantumArmorItem> QUANTUM_BOOTS = item("Quantum Boots", "quantum_boots",
            s -> new QuantumArmorItem(EquipmentSlot.FEET, s));
    public static final ItemDefinition<QuantumArmorItem> QUANTUM_LEGGINGS = item("Quantum Leggings", "quantum_leggings",
            s -> new QuantumArmorItem(EquipmentSlot.LEGS, s));
    public static final ItemDefinition<QuantumArmorItem> QUANTUM_CHESTPLATE = item("Quantum Chestplate", "quantum_chestplate",
            s -> new QuantumArmorItem(EquipmentSlot.CHEST, s));
    public static final ItemDefinition<QuantumArmorItem> QUANTUM_HELMET = item("Quantum Helmet", "quantum_helmet",
            s -> new QuantumArmorItem(EquipmentSlot.HEAD, s));
    public static final ItemDefinition<QuantumSword> QUANTUM_SWORD = itemHandheld("Quantum Sword", "quantum_sword", QuantumSword::new);

    public static final ItemDefinition<Item> ULTRADENSE_METAL_BALL = item("Ultradense Metal Ball", "ultradense_metal_ball");
    public static final ItemDefinition<Item> SINGULARITY = item("Singularity", "singularity", p -> new Item(p.rarity(Rarity.EPIC)));

    public static final ItemDefinition<ForgeTool> IRON_HAMMER = itemHandheld("Iron Hammer", "iron_hammer", p -> new ForgeTool(Tiers.IRON, p));
    public static final ItemDefinition<ForgeTool> STEEL_HAMMER = itemHandheld("Steel Hammer", "steel_hammer", p -> new ForgeTool(ForgeTool.STEEL, p));
    public static final ItemDefinition<ForgeTool> DIAMOND_HAMMER = itemHandheld("Diamond Hammer", "diamond_hammer",
            p -> new ForgeTool(Tiers.DIAMOND, p));
    public static final ItemDefinition<ForgeTool> NETHERITE_HAMMER = itemHandheld("Netherite Hammer", "netherite_hammer",
            p -> new ForgeTool(Tiers.NETHERITE, p));

    public static <T extends Item> ItemDefinition<T> item(
            String englishName,
            String path,
            Function<? super FabricItemSettings, T> ctor,
            BiConsumer<Item, ItemModelGenerators> modelGenerator) {

        T item = ctor.apply((FabricItemSettings) new FabricItemSettings().tab(ModernIndustrialization.ITEM_GROUP));
        ItemDefinition<T> definition = new ItemDefinition<>(englishName, path, item, modelGenerator);

        if (ITEMS.put(definition.getId(), definition) != null) {
            throw new IllegalArgumentException("Item id already taken : " + definition.getId());
        }

        return definition;
    }

    public static ItemDefinition<Item> item(String englishName, String path) {
        return MIItem.item(englishName, path, Item::new, (item, modelGenerator) -> modelGenerator.generateFlatItem(item,
                ModelTemplates.FLAT_ITEM));
    }

    public static <T extends Item> ItemDefinition<T> item(String englishName, String path, Function<? super FabricItemSettings, T> ctor) {
        return MIItem.item(englishName, path, ctor, (item, modelGenerator) -> modelGenerator.generateFlatItem(item,
                ModelTemplates.FLAT_ITEM));
    }

    public static ItemDefinition<Item> itemHandheld(String englishName, String path) {
        return MIItem.itemHandheld(englishName, path, Item::new);
    }

    public static <T extends Item> ItemDefinition<T> itemHandheld(String englishName, String path, Function<? super FabricItemSettings, T> ctor) {
        return MIItem.item(englishName, path, p -> ctor.apply(p.maxCount(1)), (item, modelGenerator) -> modelGenerator.generateFlatItem(item,
                ModelTemplates.FLAT_HANDHELD_ITEM));
    }

}
